[CmdletBinding()]
param(
    [string]$Version = '2.0.0',
    [string]$OutputDirectory = ''
)

$ErrorActionPreference = 'Stop'
[Console]::OutputEncoding = [System.Text.UTF8Encoding]::new($false)

$repoRoot = Split-Path -Parent $PSScriptRoot
$minecraftVersions = @(
    '1.21', '1.21.1', '1.21.2', '1.21.3', '1.21.4', '1.21.5',
    '1.21.6', '1.21.7', '1.21.8', '1.21.9', '1.21.10', '1.21.11',
    '26.1', '26.1.1', '26.1.2', '26.2'
)

if ([string]::IsNullOrWhiteSpace($OutputDirectory)) {
    $stamp = Get-Date -Format 'yyyyMMdd-HHmmss'
    $OutputDirectory = Join-Path $repoRoot "release\$stamp-$Version-update"
} elseif (-not [IO.Path]::IsPathRooted($OutputDirectory)) {
    $OutputDirectory = Join-Path $repoRoot $OutputDirectory
}

if (Test-Path -LiteralPath $OutputDirectory) {
    throw "发布目录已存在，拒绝覆盖：$OutputDirectory"
}

$assetsDirectory = Join-Path $OutputDirectory 'assets'
$packageDirectory = Join-Path $OutputDirectory 'package'
$fabricDirectory = Join-Path $packageDirectory 'Fabric'
$neoForgeDirectory = Join-Path $packageDirectory 'NeoForge'
New-Item -ItemType Directory -Path $assetsDirectory, $fabricDirectory, $neoForgeDirectory -Force | Out-Null

$releaseJars = [System.Collections.Generic.List[object]]::new()
foreach ($minecraftVersion in $minecraftVersions) {
    $neoForgeName = "AnkiNBT-NeoForge-mc$minecraftVersion-$Version.jar"
    $neoForgePath = Join-Path $repoRoot "versions\$minecraftVersion\build\libs\$neoForgeName"
    $fabricName = "AnkiNBT-Fabric-mc$minecraftVersion-$Version.jar"
    $fabricPath = if ($minecraftVersion -eq '1.21.1') {
        Join-Path $repoRoot "fabric\build\libs\$fabricName"
    } else {
        Join-Path $repoRoot "fabric-versions\$minecraftVersion\build\libs\$fabricName"
    }

    foreach ($entry in @(
        [pscustomobject]@{ Loader = 'NeoForge'; Source = $neoForgePath; Name = $neoForgeName; Package = $neoForgeDirectory },
        [pscustomobject]@{ Loader = 'Fabric'; Source = $fabricPath; Name = $fabricName; Package = $fabricDirectory }
    )) {
        if (-not (Test-Path -LiteralPath $entry.Source -PathType Leaf)) {
            throw "缺少发布 JAR：$($entry.Source)"
        }
        Copy-Item -LiteralPath $entry.Source -Destination (Join-Path $assetsDirectory $entry.Name)
        Copy-Item -LiteralPath $entry.Source -Destination (Join-Path $entry.Package $entry.Name)
        $releaseJars.Add($entry)
    }
}

if ($releaseJars.Count -ne 32) {
    throw "发布 JAR 数量错误：$($releaseJars.Count)，预期 32"
}

$names = @($releaseJars | ForEach-Object Name)
if (@($names | Sort-Object -Unique).Count -ne 32) {
    throw '发布 JAR 文件名存在重复'
}

foreach ($document in @('README.md', 'CHANGELOG.md', 'RELEASE_NOTES_2.0.0.md', 'LICENSE')) {
    $source = Join-Path $repoRoot $document
    if (Test-Path -LiteralPath $source -PathType Leaf) {
        Copy-Item -LiteralPath $source -Destination (Join-Path $packageDirectory $document)
    }
}

$zipName = "AnkiNBT-$Version-all-versions.zip"
$zipPath = Join-Path $assetsDirectory $zipName
Compress-Archive -Path (Join-Path $packageDirectory '*') -DestinationPath $zipPath -CompressionLevel Optimal

$checksumEntries = @(
    Get-ChildItem -LiteralPath $assetsDirectory -File |
        Where-Object Name -NE 'SHA256SUMS.txt' |
        Sort-Object Name |
        ForEach-Object {
            $hash = (Get-FileHash -LiteralPath $_.FullName -Algorithm SHA256).Hash.ToLowerInvariant()
            "$hash  $($_.Name)"
        }
)
$checksumPath = Join-Path $assetsDirectory 'SHA256SUMS.txt'
[IO.File]::WriteAllLines($checksumPath, $checksumEntries, [Text.UTF8Encoding]::new($false))

$manifest = [ordered]@{
    version = $Version
    generatedAt = (Get-Date).ToUniversalTime().ToString('o')
    minecraftVersions = $minecraftVersions
    loaders = @('Fabric', 'NeoForge')
    jarCount = $releaseJars.Count
    assets = @(Get-ChildItem -LiteralPath $assetsDirectory -File | Sort-Object Name | ForEach-Object Name)
}
$manifestPath = Join-Path $OutputDirectory 'manifest.json'
$manifestJson = $manifest | ConvertTo-Json -Depth 4
[IO.File]::WriteAllText($manifestPath, $manifestJson, [Text.UTF8Encoding]::new($false))

Write-Output "发布目录：$OutputDirectory"
Write-Output "JAR 数量：$($releaseJars.Count)"
Write-Output "上传资产：$(@(Get-ChildItem -LiteralPath $assetsDirectory -File).Count)"
Write-Output "校验文件：$checksumPath"

