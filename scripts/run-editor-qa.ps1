[CmdletBinding()]
param(
    [ValidateSet('NeoForge', 'Fabric')]
    [string[]]$Loaders = @('NeoForge', 'Fabric'),
    [string[]]$NeoForgeVersions = @(
        '1.21', '1.21.1', '1.21.2', '1.21.3', '1.21.4', '1.21.5',
        '1.21.6', '1.21.7', '1.21.8', '1.21.9', '1.21.10', '1.21.11',
        '26.1', '26.1.1', '26.1.2', '26.2'
    ),
    [string[]]$FabricVersions = @(
        '1.21', '1.21.1', '1.21.2', '1.21.3', '1.21.4', '1.21.5',
        '1.21.6', '1.21.7', '1.21.8', '1.21.9', '1.21.10', '1.21.11',
        '26.1', '26.1.1', '26.1.2', '26.2'
    ),
    [ValidateSet('zh_cn', 'en_us')]
    [string[]]$Languages = @('zh_cn', 'en_us'),
    [ValidateRange(60, 1800)]
    [int]$TimeoutSeconds = 360,
    [switch]$SkipClean,
    [switch]$CompileOnly
)

$ErrorActionPreference = 'Stop'
[Console]::OutputEncoding = [System.Text.UTF8Encoding]::new($false)
$repoRoot = Split-Path -Parent $PSScriptRoot
$javaHome = 'C:\Program Files\Zulu\zulu-21'
$rootGradle = Join-Path $repoRoot 'gradlew.bat'

if (-not (Test-Path -LiteralPath (Join-Path $javaHome 'bin\java.exe') -PathType Leaf)) {
    throw "找不到用于启动 Gradle 的 Java 21：$javaHome"
}
if (-not (Test-Path -LiteralPath $rootGradle -PathType Leaf)) {
    throw "找不到根 Gradle Wrapper：$rootGradle"
}

$env:JAVA_HOME = $javaHome
$env:PATH = "$(Join-Path $javaHome 'bin');$env:PATH"

function Test-TranslationParity {
    $languageRoot = Join-Path $repoRoot 'src\main\resources\assets\ankinbt\lang'
    $englishPath = Join-Path $languageRoot 'en_us.json'
    $chinesePath = Join-Path $languageRoot 'zh_cn.json'
    $english = Get-Content -LiteralPath $englishPath -Raw -Encoding UTF8 | ConvertFrom-Json -AsHashtable
    $chinese = Get-Content -LiteralPath $chinesePath -Raw -Encoding UTF8 | ConvertFrom-Json -AsHashtable
    $missingChinese = @($english.Keys | Where-Object { -not $chinese.ContainsKey($_) })
    $missingEnglish = @($chinese.Keys | Where-Object { -not $english.ContainsKey($_) })
    $emptyValues = @(
        $english.GetEnumerator() | Where-Object { [string]::IsNullOrWhiteSpace([string]$_.Value) }
        $chinese.GetEnumerator() | Where-Object { [string]::IsNullOrWhiteSpace([string]$_.Value) }
    )
    if ($missingChinese.Count -gt 0 -or $missingEnglish.Count -gt 0 -or $emptyValues.Count -gt 0) {
        throw "中英文翻译资源不完整：缺中文=$($missingChinese.Count)，缺英文=$($missingEnglish.Count)，空值=$($emptyValues.Count)"
    }
    return $english.Count
}

function Set-QaOptions {
    param(
        [Parameter(Mandatory)] [string]$RunDirectory,
        [Parameter(Mandatory)] [string]$Language
    )
    New-Item -ItemType Directory -Path $RunDirectory -Force | Out-Null
    $optionsPath = Join-Path $RunDirectory 'options.txt'
    $updates = [ordered]@{
        fullscreen = 'true'
        guiScale = '4'
        lang = $Language
        narrator = '0'
        enableVsync = 'true'
    }
    $output = [System.Collections.Generic.List[string]]::new()
    $written = [System.Collections.Generic.HashSet[string]]::new([System.StringComparer]::OrdinalIgnoreCase)
    if (Test-Path -LiteralPath $optionsPath -PathType Leaf) {
        foreach ($line in Get-Content -LiteralPath $optionsPath -Encoding UTF8) {
            $separator = $line.IndexOf(':')
            $key = if ($separator -gt 0) { $line.Substring(0, $separator) } else { '' }
            if ($updates.Contains($key)) {
                if ($written.Add($key)) { $output.Add("$key`:$($updates[$key])") }
            } else {
                $output.Add($line)
            }
        }
    }
    foreach ($entry in $updates.GetEnumerator()) {
        if ($written.Add([string]$entry.Key)) { $output.Add("$($entry.Key)`:$($entry.Value)") }
    }
    $output | Set-Content -LiteralPath $optionsPath -Encoding utf8NoBOM
}

function Resolve-Project {
    param(
        [Parameter(Mandatory)] [string]$Loader,
        [Parameter(Mandatory)] [string]$Version
    )
    if ($Loader -eq 'NeoForge') {
        return [pscustomobject]@{
            Directory = Join-Path $repoRoot "versions\$Version"
            Gradle = $rootGradle
        }
    }
    if ($Version -eq '1.21.1') {
        $directory = Join-Path $repoRoot 'fabric'
    } else {
        $directory = Join-Path $repoRoot "fabric-versions\$Version"
    }
    return [pscustomobject]@{
        Directory = $directory
        Gradle = Join-Path $directory 'gradlew.bat'
    }
}

function Invoke-GradleLogged {
    param(
        [Parameter(Mandatory)] [string]$ProjectDirectory,
        [Parameter(Mandatory)] [string]$Gradle,
        [Parameter(Mandatory)] [string[]]$Arguments,
        [Parameter(Mandatory)] [string]$LogPath
    )
    Push-Location -LiteralPath $ProjectDirectory
    try {
        & $Gradle @Arguments 2>&1 | Tee-Object -FilePath $LogPath | Out-Host
        return [int]$LASTEXITCODE
    } finally {
        Pop-Location
    }
}

function Get-RuntimeIssues {
    param([Parameter(Mandatory)] [string]$LogPath)
    if (-not (Test-Path -LiteralPath $LogPath -PathType Leaf)) { return @('运行日志不存在') }
    $patterns = @(
        '########## GL ERROR ##########',
        'Invalid input mode 0x00033007',
        'Scissor stack underflow',
        'Reading from the scissor stack with no entries',
        'AnkiNBT QA.*OpenGL错误',
        'Reported exception thrown!',
        'This crash report has been saved to'
    )
    $issues = [System.Collections.Generic.List[string]]::new()
    foreach ($pattern in $patterns) {
        $matches = @(Select-String -LiteralPath $LogPath -Pattern $pattern -Encoding UTF8)
        foreach ($match in $matches) { $issues.Add($match.Line.Trim()) }
    }
    return $issues.ToArray()
}

$translationCount = Test-TranslationParity
$runId = Get-Date -Format 'yyyyMMdd-HHmmss'
$reportRoot = Join-Path $repoRoot "build\reports\editor-qa\$runId"
New-Item -ItemType Directory -Path $reportRoot -Force | Out-Null
$results = [System.Collections.Generic.List[object]]::new()

foreach ($loader in $Loaders) {
    $versions = if ($loader -eq 'NeoForge') { $NeoForgeVersions } else { $FabricVersions }
    foreach ($version in $versions) {
        $project = Resolve-Project -Loader $loader -Version $version
        $missingBuild = -not (Test-Path -LiteralPath (Join-Path $project.Directory 'build.gradle') -PathType Leaf)
        $missingGradle = -not (Test-Path -LiteralPath $project.Gradle -PathType Leaf)
        if ($missingBuild -or $missingGradle) {
            foreach ($language in $Languages) {
                $results.Add([pscustomobject]@{
                    loader=$loader; version=$version; language=$language; status='INVALID_VERSION'
                    buildExitCode=-1; runExitCode=-1; passed=0; failed=1; total=0
                    captures=0; runtimeIssues=0; durationSeconds=0; report=$null; jar=$null
                    detail="项目目录或 Gradle Wrapper 不完整：$($project.Directory)"
                })
            }
            continue
        }

        $safeBase = "$(($loader.ToLowerInvariant()))-$($version -replace '[^0-9A-Za-z._-]', '_')"
        $buildLog = Join-Path $reportRoot "$safeBase-build.log"
        $buildArgs = [System.Collections.Generic.List[string]]::new()
        if (-not $SkipClean) { $buildArgs.Add('clean') }
        $buildArgs.Add('build')
        $buildArgs.Add('compileQaJava')
        $buildArgs.Add('--console=plain')

        Write-Host "`n===== $loader $version：干净构建与 QA 编译 =====" -ForegroundColor Cyan
        $buildStarted = Get-Date
        $buildExit = Invoke-GradleLogged -ProjectDirectory $project.Directory -Gradle $project.Gradle `
                -Arguments $buildArgs.ToArray() -LogPath $buildLog

        $jar = Get-ChildItem -LiteralPath (Join-Path $project.Directory 'build\libs') -Filter '*.jar' -File -ErrorAction SilentlyContinue |
            Where-Object { $_.Name -notmatch 'sources|javadoc' } |
            Sort-Object LastWriteTime -Descending |
            Select-Object -First 1

        foreach ($language in $Languages) {
            $started = Get-Date
            $caseRoot = Join-Path $reportRoot "$safeBase-$language"
            New-Item -ItemType Directory -Path $caseRoot -Force | Out-Null
            $qaReport = Join-Path $caseRoot 'report.json'
            $screenshotDir = Join-Path $caseRoot 'screenshots'
            $runLog = Join-Path $caseRoot 'runtime.log'
            $runExit = -1
            $status = if ($buildExit -eq 0) { 'COMPILED' } else { 'BUILD_FAILED' }
            $passed = 0
            $failed = if ($buildExit -eq 0) { 0 } else { 1 }
            $total = 0
            $captureCount = 0
            $runtimeIssueCount = 0
            $detail = if ($buildExit -eq 0) { '编译通过' } else { '构建或 QA 源集编译失败' }

            if ($buildExit -eq 0 -and -not $CompileOnly) {
                Set-QaOptions -RunDirectory (Join-Path $project.Directory 'run-qa') -Language $language
                Write-Host "`n===== $loader $version / $language：全屏、GUI缩放4真实客户端自动化 =====" -ForegroundColor Cyan
                $runArgs = @(
                    'runQaClient', '--console=plain',
                    "-PqaTimeoutSeconds=$TimeoutSeconds",
                    "-PqaLanguage=$language",
                    "-PqaReport=$qaReport",
                    "-PqaScreenshotDir=$screenshotDir"
                )
                $runExit = Invoke-GradleLogged -ProjectDirectory $project.Directory -Gradle $project.Gradle `
                        -Arguments $runArgs -LogPath $runLog
                $runtimeIssues = @(Get-RuntimeIssues -LogPath $runLog)
                $runtimeIssueCount = $runtimeIssues.Count

                if (Test-Path -LiteralPath $qaReport -PathType Leaf) {
                    try {
                        $parsed = Get-Content -LiteralPath $qaReport -Raw -Encoding UTF8 | ConvertFrom-Json
                        $status = [string]$parsed.status
                        $passed = [int]$parsed.passed
                        $failed = [int]$parsed.failed
                        $total = [int]$parsed.total
                        $captureCount = @($parsed.captures).Count
                        $detail = if ($status -eq 'PASSED' -and $failed -eq 0) {
                            '功能、显示与稳定性自动化全部通过'
                        } else {
                            '自动化报告存在失败项'
                        }
                    } catch {
                        $status = 'REPORT_INVALID'
                        $failed = 1
                        $detail = "自动化报告无法解析：$($_.Exception.Message)"
                    }
                } else {
                    $status = 'REPORT_MISSING'
                    $failed = 1
                    $detail = '客户端没有生成自动化报告'
                }
                if ($runtimeIssueCount -gt 0) {
                    $status = 'DISPLAY_RUNTIME_FAILED'
                    $failed = [Math]::Max(1, $failed)
                    $detail = "检测到 $runtimeIssueCount 条 GL、剪裁栈、闪屏或崩溃日志"
                } elseif ($runExit -ne 0 -and $status -eq 'PASSED') {
                    $status = 'RUNTIME_FAILED'
                    $failed = [Math]::Max(1, $failed)
                    $detail = "报告通过，但客户端进程退出码为 $runExit"
                }
            }

            $duration = [Math]::Round(((Get-Date) - $started).TotalSeconds, 2)
            $result = [pscustomobject]@{
                loader = $loader
                version = $version
                language = $language
                status = $status
                buildExitCode = $buildExit
                runExitCode = $runExit
                passed = $passed
                failed = $failed
                total = $total
                captures = $captureCount
                runtimeIssues = $runtimeIssueCount
                durationSeconds = $duration
                report = if (Test-Path -LiteralPath $qaReport) { $qaReport } else { $null }
                jar = if ($jar) { $jar.FullName } else { $null }
                detail = $detail
            }
            $results.Add($result)
            $passedCase = if ($CompileOnly) { $buildExit -eq 0 } else {
                $status -eq 'PASSED' -and $failed -eq 0 -and $runtimeIssueCount -eq 0
            }
            Write-Host ("{0} {1} / {2}：{3}，{4}/{5}，截图 {6}，日志问题 {7}，{8}s" -f `
                    $loader, $version, $language, $status, $passed, $total, $captureCount, $runtimeIssueCount, $duration) `
                    -ForegroundColor $(if ($passedCase) { 'Green' } else { 'Red' })
        }
    }
}

$summary = [pscustomobject]@{
    schema = 2
    runId = $runId
    generatedAt = (Get-Date).ToUniversalTime().ToString('o')
    compileOnly = [bool]$CompileOnly
    fullscreen = $true
    guiScale = 4
    languages = $Languages
    translationKeysPerLanguage = $translationCount
    results = $results
}
$summaryJson = Join-Path $reportRoot 'summary.json'
$summary | ConvertTo-Json -Depth 10 | Set-Content -LiteralPath $summaryJson -Encoding utf8NoBOM

$markdown = [System.Collections.Generic.List[string]]::new()
$markdown.Add('# AnkiNBT 双加载器自动化测试报告')
$markdown.Add('')
$markdown.Add("- 批次：$runId")
$markdown.Add('- 显示：全屏，GUI 缩放 4')
$markdown.Add("- 语言：$($Languages -join ', ')")
$markdown.Add("- 中英文翻译键：每种 $translationCount 条，键集合一致")
$markdown.Add('')
$markdown.Add('| 加载器 | Minecraft | 语言 | 状态 | 通过/总数 | 截图 | 日志问题 | 耗时 |')
$markdown.Add('|---|---:|---:|---:|---:|---:|---:|---:|')
foreach ($result in $results) {
    $markdown.Add("| $($result.loader) | $($result.version) | $($result.language) | $($result.status) | $($result.passed)/$($result.total) | $($result.captures) | $($result.runtimeIssues) | $($result.durationSeconds)s |")
}
$summaryMarkdown = Join-Path $reportRoot 'summary.md'
$markdown | Set-Content -LiteralPath $summaryMarkdown -Encoding utf8NoBOM

$failedResults = @($results | Where-Object {
    if ($CompileOnly) { $_.buildExitCode -ne 0 }
    else { $_.status -ne 'PASSED' -or $_.failed -ne 0 -or $_.runtimeIssues -ne 0 }
})

Write-Host "`n汇总 JSON：$summaryJson"
Write-Host "汇总 Markdown：$summaryMarkdown"
if ($failedResults.Count -gt 0) {
    Write-Host "失败用例：$($failedResults.Count) / $($results.Count)" -ForegroundColor Red
    exit 1
}
Write-Host "全部 $($results.Count) 个加载器/版本/语言用例通过。" -ForegroundColor Green
