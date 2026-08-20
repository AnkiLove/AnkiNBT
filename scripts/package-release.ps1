[CmdletBinding()]
param(
    [Parameter()]
    [ValidateNotNullOrEmpty()]
    [string]$RepositoryRoot = (Split-Path -Parent $PSScriptRoot)
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

Add-Type -AssemblyName System.IO.Compression

$releaseVersion = '2.0.0'
$repositoryPath = [System.IO.Path]::GetFullPath($RepositoryRoot)
$verifyScript = Join-Path $PSScriptRoot 'verify-release.ps1'

if (-not (Test-Path -LiteralPath $verifyScript -PathType Leaf)) {
    throw "缺少发布校验脚本：$verifyScript"
}

Write-Host "正在校验 AnkiNBT $releaseVersion 的 30 个发布 JAR..." -ForegroundColor Cyan
$verification = & $verifyScript -RepositoryRoot $repositoryPath -PassThru -Quiet
if (-not $verification.Success) {
    Write-Host "发布校验失败：发现 $($verification.Errors.Count) 个问题，未复制或压缩任何文件。" -ForegroundColor Red
    foreach ($verificationError in $verification.Errors) {
        Write-Host "  - $verificationError" -ForegroundColor Red
    }
    throw "AnkiNBT $releaseVersion 未通过发布校验。"
}

if ($verification.ExpectedArtifactCount -ne 30 -or $verification.Artifacts.Count -ne 30) {
    throw "发布矩阵不是固定的 30 个 JAR，拒绝打包。"
}

$releaseRoot = Join-Path $repositoryPath "release/$releaseVersion"
$fabricReleasePath = Join-Path $releaseRoot 'Fabric'
$neoForgeReleasePath = Join-Path $releaseRoot 'NeoForge'
$zipPath = Join-Path $releaseRoot "AnkiNBT-$releaseVersion-all-versions.zip"

$destinationRecords = @($verification.Artifacts | ForEach-Object {
    $loaderDirectory = if ($_.Loader -ceq 'Fabric') { $fabricReleasePath } else { $neoForgeReleasePath }
    [pscustomobject]@{
        Loader = $_.Loader
        MinecraftVersion = $_.MinecraftVersion
        SourcePath = $_.JarPath
        DestinationPath = Join-Path $loaderDirectory $_.FileName
        ZipEntryName = "$($_.Loader)/$($_.FileName)"
        Sha256 = $_.Sha256
    }
})

$expectedFabricCount = @($destinationRecords | Where-Object { $_.Loader -ceq 'Fabric' }).Count
$expectedNeoForgeCount = @($destinationRecords | Where-Object { $_.Loader -ceq 'NeoForge' }).Count
if ($expectedFabricCount -ne 15 -or $expectedNeoForgeCount -ne 15) {
    throw "发布矩阵加载器数量错误：Fabric=$expectedFabricCount，NeoForge=$expectedNeoForgeCount。"
}

foreach ($record in $destinationRecords) {
    if (-not (Test-Path -LiteralPath $record.DestinationPath -PathType Leaf)) {
        continue
    }

    $existingHash = (Get-FileHash -LiteralPath $record.DestinationPath -Algorithm SHA256).Hash
    if ($existingHash -cne $record.Sha256) {
        throw "发布目录已有同名但内容不同的文件，拒绝覆盖：$($record.DestinationPath)"
    }
}

foreach ($loaderPath in @($fabricReleasePath, $neoForgeReleasePath)) {
    if (-not (Test-Path -LiteralPath $loaderPath)) {
        continue
    }

    $expectedNames = @($destinationRecords | Where-Object {
        [System.IO.Path]::GetDirectoryName($_.DestinationPath) -ceq $loaderPath
    } | ForEach-Object { [System.IO.Path]::GetFileName($_.DestinationPath) })

    foreach ($unexpectedJar in Get-ChildItem -LiteralPath $loaderPath -File -Filter '*.jar') {
        if ($expectedNames -cnotcontains $unexpectedJar.Name) {
            throw "发布目录存在矩阵外 JAR，拒绝生成混合发布包：$($unexpectedJar.FullName)"
        }
    }
}

New-Item -ItemType Directory -Path $fabricReleasePath -Force | Out-Null
New-Item -ItemType Directory -Path $neoForgeReleasePath -Force | Out-Null

foreach ($record in $destinationRecords) {
    if (-not (Test-Path -LiteralPath $record.DestinationPath -PathType Leaf)) {
        Copy-Item -LiteralPath $record.SourcePath -Destination $record.DestinationPath
    }
}

$temporaryRoot = Join-Path ([System.IO.Path]::GetTempPath()) ("ankinbt-release-$releaseVersion-" + [System.Guid]::NewGuid().ToString('N'))
$temporaryZip = Join-Path ([System.IO.Path]::GetTempPath()) ("ankinbt-release-$releaseVersion-" + [System.Guid]::NewGuid().ToString('N') + '.zip')

try {
    $temporaryFabricPath = Join-Path $temporaryRoot 'Fabric'
    $temporaryNeoForgePath = Join-Path $temporaryRoot 'NeoForge'
    New-Item -ItemType Directory -Path $temporaryFabricPath -Force | Out-Null
    New-Item -ItemType Directory -Path $temporaryNeoForgePath -Force | Out-Null

    foreach ($record in $destinationRecords) {
        $temporaryLoaderPath = if ($record.Loader -ceq 'Fabric') { $temporaryFabricPath } else { $temporaryNeoForgePath }
        Copy-Item -LiteralPath $record.DestinationPath -Destination (Join-Path $temporaryLoaderPath ([System.IO.Path]::GetFileName($record.DestinationPath)))
    }

    [System.IO.Compression.ZipFile]::CreateFromDirectory(
        $temporaryRoot,
        $temporaryZip,
        [System.IO.Compression.CompressionLevel]::Optimal,
        $false
    )

    if (Test-Path -LiteralPath $zipPath -PathType Leaf) {
        $existingArchiveMatches = $true
        $archive = $null
        try {
            $archive = [System.IO.Compression.ZipFile]::OpenRead($zipPath)
            $fileEntries = @($archive.Entries | Where-Object { -not [string]::IsNullOrEmpty($_.Name) })
            if ($fileEntries.Count -ne $destinationRecords.Count) {
                $existingArchiveMatches = $false
            }

            if ($existingArchiveMatches) {
                foreach ($record in $destinationRecords) {
                    $entryMatches = @($fileEntries | Where-Object { $_.FullName -ceq $record.ZipEntryName })
                    if ($entryMatches.Count -ne 1) {
                        $existingArchiveMatches = $false
                        break
                    }

                    $entryStream = $entryMatches[0].Open()
                    $hashStream = [System.Security.Cryptography.SHA256]::Create()
                    try {
                        $entryHash = [System.Convert]::ToHexString($hashStream.ComputeHash($entryStream))
                    }
                    finally {
                        $hashStream.Dispose()
                        $entryStream.Dispose()
                    }

                    if ($entryHash -cne $record.Sha256) {
                        $existingArchiveMatches = $false
                        break
                    }
                }
            }
        }
        finally {
            if ($null -ne $archive) {
                $archive.Dispose()
            }
        }

        if (-not $existingArchiveMatches) {
            throw "已有发布 ZIP 与当前 30 个 JAR 不一致，拒绝覆盖：$zipPath"
        }
    }
    else {
        Move-Item -LiteralPath $temporaryZip -Destination $zipPath
    }
}
finally {
    if (Test-Path -LiteralPath $temporaryZip -PathType Leaf) {
        Remove-Item -LiteralPath $temporaryZip -Force
    }
    if (Test-Path -LiteralPath $temporaryRoot -PathType Container) {
        Remove-Item -LiteralPath $temporaryRoot -Recurse -Force
    }
}

$packagedJars = @(
    Get-ChildItem -LiteralPath $fabricReleasePath -File -Filter '*.jar'
    Get-ChildItem -LiteralPath $neoForgeReleasePath -File -Filter '*.jar'
)
if ($packagedJars.Count -ne 30) {
    throw "打包后的 JAR 数量应为 30，实际为 $($packagedJars.Count)。"
}

Write-Host "发布包已生成：$releaseRoot" -ForegroundColor Green
Write-Host "  Fabric：15 个 JAR" -ForegroundColor Green
Write-Host "  NeoForge：15 个 JAR" -ForegroundColor Green
Write-Host "  ZIP：$zipPath" -ForegroundColor Green

