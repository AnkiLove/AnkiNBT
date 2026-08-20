param(
    [string]$IconFontPath = 'D:\mynaui-icons-main\packages\icons\mynaui.ttf',
    [string]$OutputPath = ''
)

$ErrorActionPreference = 'Stop'

if (-not (Test-Path -LiteralPath $IconFontPath -PathType Leaf)) {
    throw "Mynaui icon font not found: $IconFontPath"
}

$repositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
if ([string]::IsNullOrWhiteSpace($OutputPath)) {
    $OutputPath = Join-Path $repositoryRoot 'src/main/resources/assets/ankinbt/textures/font/mynaui.png'
}

$outputDirectory = Split-Path -Parent $OutputPath
New-Item -ItemType Directory -Path $outputDirectory -Force | Out-Null

Add-Type -AssemblyName System.Drawing.Common

$codePoints = @(
    0xEAA5, 0xEE59, 0xEEBC, 0xEBC2, 0xEB4C, 0xEE09, 0xEEF9, 0xEAFB,
    0xEB0F, 0xEE7A, 0xEA93, 0xEC6A, 0xEE31, 0xEE1C, 0xED83, 0xEB54,
    0xEDDF, 0xEDD7, 0xEB60, 0xEEA4, 0xEAFF, 0xEB03, 0xEDEF, 0xEC58,
    0xEECF, 0xEC3D, 0xEA05, 0xED34, 0xEAC2, 0xEDF0
)

$cellSize = 20
$bitmap = [System.Drawing.Bitmap]::new(
    $cellSize * $codePoints.Count,
    $cellSize,
    [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
$graphics = [System.Drawing.Graphics]::FromImage($bitmap)
$fontCollection = [System.Drawing.Text.PrivateFontCollection]::new()
$font = $null
$format = $null

try {
    $graphics.Clear([System.Drawing.Color]::Transparent)
    $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
    $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
    $graphics.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::AntiAliasGridFit

    $fontCollection.AddFontFile((Resolve-Path -LiteralPath $IconFontPath).Path)
    $font = [System.Drawing.Font]::new(
        $fontCollection.Families[0],
        17,
        [System.Drawing.FontStyle]::Regular,
        [System.Drawing.GraphicsUnit]::Pixel)
    $format = [System.Drawing.StringFormat]::GenericTypographic.Clone()
    $format.FormatFlags = $format.FormatFlags -bor [System.Drawing.StringFormatFlags]::NoClip

    for ($index = 0; $index -lt $codePoints.Count; $index++) {
        $glyph = [string][char]$codePoints[$index]
        $x = [single]($index * $cellSize + 1)
        $graphics.DrawString($glyph, $font, [System.Drawing.Brushes]::White, $x, [single]-1, $format)
    }

    $bitmap.Save($OutputPath, [System.Drawing.Imaging.ImageFormat]::Png)
}
finally {
    if ($null -ne $format) { $format.Dispose() }
    if ($null -ne $font) { $font.Dispose() }
    $fontCollection.Dispose()
    $graphics.Dispose()
    $bitmap.Dispose()
}

Write-Output (Resolve-Path -LiteralPath $OutputPath).Path
