[CmdletBinding()]
param(
    [string[]]$Versions = @(
        '1.21', '1.21.1', '1.21.2', '1.21.3', '1.21.4', '1.21.5',
        '1.21.6', '1.21.7', '1.21.8', '1.21.9', '1.21.10', '1.21.11',
        '26.1', '26.1.1', '26.1.2', '26.2'
    ),
    [ValidateRange(60, 1800)]
    [int]$TimeoutSeconds = 300,
    [switch]$SkipClean,
    [switch]$CompileOnly
)

$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path -Parent $PSScriptRoot
$gradle = Join-Path $repoRoot 'gradlew.bat'
$javaHome = 'C:\Program Files\Zulu\zulu-21'

if (-not (Test-Path -LiteralPath $gradle -PathType Leaf)) {
    throw "找不到 Gradle Wrapper：$gradle"
}
if (-not (Test-Path -LiteralPath (Join-Path $javaHome 'bin\java.exe') -PathType Leaf)) {
    throw "找不到用于启动 Gradle 的 Java 21：$javaHome"
}

$env:JAVA_HOME = $javaHome
$env:PATH = "$(Join-Path $javaHome 'bin');$env:PATH"

$runId = Get-Date -Format 'yyyyMMdd-HHmmss'
$reportRoot = Join-Path $repoRoot "build\reports\neoforge-qa\$runId"
New-Item -ItemType Directory -Path $reportRoot -Force | Out-Null

$results = [System.Collections.Generic.List[object]]::new()

function Invoke-GradleLogged {
    param(
        [Parameter(Mandatory)] [string]$VersionDirectory,
        [Parameter(Mandatory)] [string[]]$Arguments,
        [Parameter(Mandatory)] [string]$LogPath
    )

    Push-Location -LiteralPath $VersionDirectory
    try {
        & $gradle @Arguments 2>&1 | Tee-Object -FilePath $LogPath | Out-Host
        $exitCode = $LASTEXITCODE
        return [int]$exitCode
    } finally {
        Pop-Location
    }
}

foreach ($version in $Versions) {
    $versionDirectory = Join-Path $repoRoot "versions\$version"
    if (-not (Test-Path -LiteralPath (Join-Path $versionDirectory 'build.gradle') -PathType Leaf)) {
        $results.Add([pscustomobject]@{
            version = $version; status = 'INVALID_VERSION'; buildExitCode = -1; runExitCode = -1
            passed = 0; failed = 1; total = 0; durationSeconds = 0; report = $null; jar = $null
            detail = "版本目录不存在或缺少 build.gradle：$versionDirectory"
        })
        continue
    }

    Write-Host "`n===== NeoForge $version：干净构建与 QA 编译 =====" -ForegroundColor Cyan
    $started = Get-Date
    $safeVersion = $version -replace '[^0-9A-Za-z._-]', '_'
    $buildLog = Join-Path $reportRoot "$safeVersion-build.log"
    $runLog = Join-Path $reportRoot "$safeVersion-runtime.log"
    $buildArgs = [System.Collections.Generic.List[string]]::new()
    if (-not $SkipClean) { $buildArgs.Add('clean') }
    $buildArgs.Add('build')
    $buildArgs.Add('compileQaJava')
    $buildArgs.Add('--console=plain')

    $buildExit = Invoke-GradleLogged -VersionDirectory $versionDirectory -Arguments $buildArgs.ToArray() -LogPath $buildLog
    $runExit = -1
    $qaStatus = if ($buildExit -eq 0) { 'COMPILED' } else { 'BUILD_FAILED' }
    $passed = 0
    $failed = if ($buildExit -eq 0) { 0 } else { 1 }
    $total = 0
    $detail = if ($buildExit -eq 0) { '编译通过' } else { '构建或 QA 源集编译失败' }
    $qaReport = Join-Path $versionDirectory 'build\reports\ankinbt-qa\report.json'

    if ($buildExit -eq 0 -and -not $CompileOnly) {
        Write-Host "`n===== NeoForge $version：启动真实客户端自动化 =====" -ForegroundColor Cyan
        $runArgs = @('runQaClient', '--console=plain', "-PqaTimeoutSeconds=$TimeoutSeconds")
        $runExit = Invoke-GradleLogged -VersionDirectory $versionDirectory -Arguments $runArgs -LogPath $runLog

        if (Test-Path -LiteralPath $qaReport -PathType Leaf) {
            try {
                $parsed = Get-Content -LiteralPath $qaReport -Raw -Encoding UTF8 | ConvertFrom-Json
                $qaStatus = [string]$parsed.status
                $passed = [int]$parsed.passed
                $failed = [int]$parsed.failed
                $total = [int]$parsed.total
                $detail = if ($qaStatus -eq 'PASSED' -and $failed -eq 0) {
                    '真实客户端自动化全部通过'
                } else {
                    '自动化报告存在失败项'
                }
            } catch {
                $qaStatus = 'REPORT_INVALID'
                $failed = 1
                $detail = "自动化报告无法解析：$($_.Exception.Message)"
            }
        } else {
            $qaStatus = 'REPORT_MISSING'
            $failed = 1
            $detail = '客户端没有生成自动化报告'
        }

        if ($runExit -ne 0 -and $qaStatus -eq 'PASSED') {
            $qaStatus = 'RUNTIME_FAILED'
            $failed = [Math]::Max(1, $failed)
            $detail = "自动化报告通过，但 Gradle 客户端进程退出码为 $runExit"
        }
    }

    $jar = Get-ChildItem -LiteralPath (Join-Path $versionDirectory 'build\libs') -Filter '*.jar' -File -ErrorAction SilentlyContinue |
        Where-Object { $_.Name -notmatch 'sources|javadoc' } |
        Sort-Object LastWriteTime -Descending |
        Select-Object -First 1
    $duration = [Math]::Round(((Get-Date) - $started).TotalSeconds, 2)
    $result = [pscustomobject]@{
        version = $version
        status = $qaStatus
        buildExitCode = $buildExit
        runExitCode = $runExit
        passed = $passed
        failed = $failed
        total = $total
        durationSeconds = $duration
        report = if (Test-Path -LiteralPath $qaReport) { $qaReport } else { $null }
        jar = if ($jar) { $jar.FullName } else { $null }
        detail = $detail
    }
    $results.Add($result)

    $color = if (($CompileOnly -and $buildExit -eq 0) -or ($qaStatus -eq 'PASSED' -and $failed -eq 0)) { 'Green' } else { 'Red' }
    Write-Host ("NeoForge {0}：{1}，{2}/{3} 通过，耗时 {4}s" -f $version, $qaStatus, $passed, $total, $duration) -ForegroundColor $color
}

$summary = [pscustomobject]@{
    schema = 1
    runId = $runId
    generatedAt = (Get-Date).ToUniversalTime().ToString('o')
    compileOnly = [bool]$CompileOnly
    versions = $results
}
$summaryJson = Join-Path $reportRoot 'summary.json'
$summary | ConvertTo-Json -Depth 8 | Set-Content -LiteralPath $summaryJson -Encoding UTF8

$markdown = [System.Collections.Generic.List[string]]::new()
$markdown.Add('# AnkiNBT NeoForge 自动化测试报告')
$markdown.Add('')
$markdown.Add("- 批次：$runId")
$markdown.Add("- 模式：$(if ($CompileOnly) { '仅构建和 QA 编译' } else { '真实客户端自动化' })")
$markdown.Add("- Java：$javaHome")
$markdown.Add('')
$markdown.Add('| Minecraft | 状态 | 通过/总数 | 构建退出码 | 客户端退出码 | 耗时 |')
$markdown.Add('|---|---:|---:|---:|---:|---:|')
foreach ($result in $results) {
    $markdown.Add("| $($result.version) | $($result.status) | $($result.passed)/$($result.total) | $($result.buildExitCode) | $($result.runExitCode) | $($result.durationSeconds)s |")
}
$summaryMarkdown = Join-Path $reportRoot 'summary.md'
$markdown | Set-Content -LiteralPath $summaryMarkdown -Encoding UTF8

$failedResults = @($results | Where-Object {
    if ($CompileOnly) { $_.buildExitCode -ne 0 } else { $_.status -ne 'PASSED' -or $_.failed -ne 0 }
})

Write-Host "`n汇总 JSON：$summaryJson"
Write-Host "汇总 Markdown：$summaryMarkdown"
if ($failedResults.Count -gt 0) {
    Write-Host "失败版本：$($failedResults.version -join ', ')" -ForegroundColor Red
    exit 1
}

Write-Host "全部 $($results.Count) 个 NeoForge 版本通过。" -ForegroundColor Green
