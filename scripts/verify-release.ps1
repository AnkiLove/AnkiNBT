[CmdletBinding()]
param(
    [Parameter()]
    [ValidateNotNullOrEmpty()]
    [string]$RepositoryRoot = (Split-Path -Parent $PSScriptRoot),

    [Parameter()]
    [switch]$PassThru,

    [Parameter()]
    [switch]$Quiet
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

Add-Type -AssemblyName System.IO.Compression

$releaseVersion = '2.0.0'
$expectedLogoSha256 = $null
$expectedLicenseSha256 = $null
$repositoryPath = [System.IO.Path]::GetFullPath($RepositoryRoot)

if (-not (Test-Path -LiteralPath $repositoryPath -PathType Container)) {
    throw "仓库目录不存在：$repositoryPath"
}

$matrix = @(
    [pscustomobject]@{ Minecraft = '1.21.1';  FabricRange = '=1.21.1';  NeoForgeRange = '[1.21.1,1.21.2)' }
    [pscustomobject]@{ Minecraft = '1.21.2';  FabricRange = '=1.21.2';  NeoForgeRange = '[1.21.2,1.21.3)' }
    [pscustomobject]@{ Minecraft = '1.21.3';  FabricRange = '=1.21.3';  NeoForgeRange = '[1.21.3,1.21.4)' }
    [pscustomobject]@{ Minecraft = '1.21.4';  FabricRange = '=1.21.4';  NeoForgeRange = '[1.21.4,1.21.5)' }
    [pscustomobject]@{ Minecraft = '1.21.5';  FabricRange = '=1.21.5';  NeoForgeRange = '[1.21.5,1.21.6)' }
    [pscustomobject]@{ Minecraft = '1.21.6';  FabricRange = '=1.21.6';  NeoForgeRange = '[1.21.6,1.21.7)' }
    [pscustomobject]@{ Minecraft = '1.21.7';  FabricRange = '=1.21.7';  NeoForgeRange = '[1.21.7,1.21.8)' }
    [pscustomobject]@{ Minecraft = '1.21.8';  FabricRange = '=1.21.8';  NeoForgeRange = '[1.21.8,1.21.9)' }
    [pscustomobject]@{ Minecraft = '1.21.9';  FabricRange = '=1.21.9';  NeoForgeRange = '[1.21.9,1.21.10)' }
    [pscustomobject]@{ Minecraft = '1.21.10'; FabricRange = '=1.21.10'; NeoForgeRange = '[1.21.10,1.21.11)' }
    [pscustomobject]@{ Minecraft = '1.21.11'; FabricRange = '=1.21.11'; NeoForgeRange = '[1.21.11,1.21.12)' }
    [pscustomobject]@{ Minecraft = '26.1';    FabricRange = '~26.1';    NeoForgeRange = '[26.1,26.1.1)' }
    [pscustomobject]@{ Minecraft = '26.1.1';  FabricRange = '~26.1.1';  NeoForgeRange = '[26.1.1,26.1.2)' }
    [pscustomobject]@{ Minecraft = '26.1.2';  FabricRange = '~26.1.2';  NeoForgeRange = '[26.1.2,26.1.3)' }
    [pscustomobject]@{ Minecraft = '26.2';    FabricRange = '~26.2';    NeoForgeRange = '[26.2,26.3)' }
)

$requiredEntries = @(
    'com/ankinbt/gui/EditorDock.class'
    'com/ankinbt/gui/EditorBrandLayer.class'
    'com/ankinbt/gui/InventoryEditorOverlay.class'
    'com/ankinbt/gui/EntityEditorScreen.class'
    'com/ankinbt/gui/VillagerTradeEditorScreen.class'
    'assets/ankinbt/font/mynaui.ttf'
    'assets/ankinbt/textures/gui/editor-logo.png'
    'assets/ankinbt/lang/en_us.json'
    'assets/ankinbt/lang/zh_cn.json'
)

$forbiddenTerms = @(
    ('Fancy' + 'Menu')
    ('Code' + 'x')
    ('Chat' + 'GPT')
    ('Open' + 'AI')
    ('Co-' + 'authored-by')
)

$textEntryPattern = '\.(?:class|json|toml|txt|properties|md|xml|ya?ml|mf)$'
$verificationErrors = [System.Collections.Generic.List[string]]::new()
$artifacts = [System.Collections.Generic.List[object]]::new()

function Add-VerificationError {
    param(
        [Parameter(Mandatory)]
        [string]$ArtifactLabel,

        [Parameter(Mandatory)]
        [string]$Message
    )

    $verificationErrors.Add("[$ArtifactLabel] $Message")
}

function Get-ZipEntries {
    param(
        [Parameter(Mandatory)]
        [System.IO.Compression.ZipArchive]$Archive,

        [Parameter(Mandatory)]
        [string]$FullName
    )

    return @($Archive.Entries | Where-Object { $_.FullName -ceq $FullName })
}

function Get-SingleZipEntry {
    param(
        [Parameter(Mandatory)]
        [System.IO.Compression.ZipArchive]$Archive,

        [Parameter(Mandatory)]
        [string]$FullName,

        [Parameter(Mandatory)]
        [string]$ArtifactLabel,

        [Parameter()]
        [switch]$Required
    )

    $matches = @(Get-ZipEntries -Archive $Archive -FullName $FullName)
    if ($matches.Count -eq 0) {
        if ($Required) {
            Add-VerificationError -ArtifactLabel $ArtifactLabel -Message "缺少 JAR 条目：$FullName"
        }
        return $null
    }

    if ($matches.Count -gt 1) {
        Add-VerificationError -ArtifactLabel $ArtifactLabel -Message "JAR 条目重复：$FullName"
        return $null
    }

    return $matches[0]
}

function Read-ZipEntryBytes {
    param(
        [Parameter(Mandatory)]
        [System.IO.Compression.ZipArchiveEntry]$Entry
    )

    $entryStream = $Entry.Open()
    $memoryStream = [System.IO.MemoryStream]::new()
    try {
        $entryStream.CopyTo($memoryStream)
        return ,$memoryStream.ToArray()
    }
    finally {
        $memoryStream.Dispose()
        $entryStream.Dispose()
    }
}

function Read-ZipEntryText {
    param(
        [Parameter(Mandatory)]
        [System.IO.Compression.ZipArchiveEntry]$Entry
    )

    $bytes = Read-ZipEntryBytes -Entry $Entry
    return [System.Text.Encoding]::UTF8.GetString($bytes)
}

function Get-BytesSha256 {
    param(
        [Parameter(Mandatory)]
        [byte[]]$Bytes
    )

    $hashBytes = [System.Security.Cryptography.SHA256]::HashData($Bytes)
    return [System.Convert]::ToHexString($hashBytes)
}

function Get-TomlQuotedAssignment {
    param(
        [Parameter(Mandatory)]
        [string]$Text,

        [Parameter(Mandatory)]
        [string]$Name
    )

    $pattern = '(?m)^\s*' + [System.Text.RegularExpressions.Regex]::Escape($Name) + '\s*=\s*"([^"]*)"\s*(?:#.*)?$'
    $match = [System.Text.RegularExpressions.Regex]::Match($Text, $pattern)
    if (-not $match.Success) {
        return $null
    }

    return $match.Groups[1].Value
}

function Test-FabricMetadata {
    param(
        [Parameter(Mandatory)]
        [System.IO.Compression.ZipArchive]$Archive,

        [Parameter(Mandatory)]
        [pscustomobject]$Spec,

        [Parameter(Mandatory)]
        [string]$ArtifactLabel
    )

    $metadataEntry = Get-SingleZipEntry -Archive $Archive -FullName 'fabric.mod.json' -ArtifactLabel $ArtifactLabel -Required
    if ($null -eq $metadataEntry) {
        return
    }

    try {
        $metadata = Read-ZipEntryText -Entry $metadataEntry | ConvertFrom-Json -Depth 32
    }
    catch {
        Add-VerificationError -ArtifactLabel $ArtifactLabel -Message "fabric.mod.json 不是有效 JSON：$($_.Exception.Message)"
        return
    }

    if ([string]$metadata.id -cne 'ankinbt') {
        Add-VerificationError -ArtifactLabel $ArtifactLabel -Message "Fabric mod id 应为 ankinbt，实际为 '$($metadata.id)'"
    }
    if ([string]$metadata.version -cne $releaseVersion) {
        Add-VerificationError -ArtifactLabel $ArtifactLabel -Message "Fabric 版本应为 $releaseVersion，实际为 '$($metadata.version)'"
    }
    if ([string]$metadata.license -cne 'GPL-3.0-only') {
        Add-VerificationError -ArtifactLabel $ArtifactLabel -Message "Fabric 许可证应为 GPL-3.0-only，实际为 '$($metadata.license)'"
    }
    if ([string]$metadata.environment -cne 'client') {
        Add-VerificationError -ArtifactLabel $ArtifactLabel -Message "Fabric environment 应为 client，实际为 '$($metadata.environment)'"
    }

    $minecraftRange = $null
    if ($null -ne $metadata.depends) {
        $minecraftProperty = $metadata.depends.PSObject.Properties['minecraft']
        if ($null -ne $minecraftProperty) {
            $minecraftRange = [string]$minecraftProperty.Value
        }
    }
    if ($minecraftRange -cne $Spec.FabricRange) {
        Add-VerificationError -ArtifactLabel $ArtifactLabel -Message "Fabric Minecraft 范围应为 '$($Spec.FabricRange)'，实际为 '$minecraftRange'"
    }

    $declaredMixins = @($metadata.mixins | ForEach-Object {
        if ($_ -is [string]) {
            [string]$_
        }
        elseif ($null -ne $_.config) {
            [string]$_.config
        }
    })
    if ($declaredMixins -cnotcontains 'ankinbt.mixins.json') {
        Add-VerificationError -ArtifactLabel $ArtifactLabel -Message 'fabric.mod.json 未声明 ankinbt.mixins.json'
    }

    $unexpectedNeoMetadata = @(Get-ZipEntries -Archive $Archive -FullName 'META-INF/neoforge.mods.toml')
    if ($unexpectedNeoMetadata.Count -gt 0) {
        Add-VerificationError -ArtifactLabel $ArtifactLabel -Message 'Fabric JAR 不应包含 NeoForge 元数据'
    }
}

function Test-NeoForgeMetadata {
    param(
        [Parameter(Mandatory)]
        [System.IO.Compression.ZipArchive]$Archive,

        [Parameter(Mandatory)]
        [pscustomobject]$Spec,

        [Parameter(Mandatory)]
        [string]$ArtifactLabel
    )

    $metadataEntry = Get-SingleZipEntry -Archive $Archive -FullName 'META-INF/neoforge.mods.toml' -ArtifactLabel $ArtifactLabel -Required
    if ($null -eq $metadataEntry) {
        return
    }

    $metadataText = Read-ZipEntryText -Entry $metadataEntry
    $modLoader = Get-TomlQuotedAssignment -Text $metadataText -Name 'modLoader'
    $license = Get-TomlQuotedAssignment -Text $metadataText -Name 'license'
    if ($modLoader -cne 'javafml') {
        Add-VerificationError -ArtifactLabel $ArtifactLabel -Message "NeoForge modLoader 应为 javafml，实际为 '$modLoader'"
    }
    if ($license -cne 'GPL-3.0-only') {
        Add-VerificationError -ArtifactLabel $ArtifactLabel -Message "NeoForge 许可证应为 GPL-3.0-only，实际为 '$license'"
    }

    $modBlocks = [System.Text.RegularExpressions.Regex]::Matches(
        $metadataText,
        '(?ms)^\s*\[\[mods\]\]\s*(.*?)(?=^\s*\[\[|\z)'
    )
    $ankinbtModBlock = $null
    foreach ($blockMatch in $modBlocks) {
        $blockText = $blockMatch.Groups[1].Value
        if ((Get-TomlQuotedAssignment -Text $blockText -Name 'modId') -ceq 'ankinbt') {
            $ankinbtModBlock = $blockText
            break
        }
    }

    if ($null -eq $ankinbtModBlock) {
        Add-VerificationError -ArtifactLabel $ArtifactLabel -Message 'NeoForge 元数据缺少 [[mods]] ankinbt 区块'
    }
    else {
        $modVersion = Get-TomlQuotedAssignment -Text $ankinbtModBlock -Name 'version'
        if ($modVersion -cne $releaseVersion) {
            Add-VerificationError -ArtifactLabel $ArtifactLabel -Message "NeoForge 版本应为 $releaseVersion，实际为 '$modVersion'"
        }
    }

    $dependencyBlocks = [System.Text.RegularExpressions.Regex]::Matches(
        $metadataText,
        '(?ms)^\s*\[\[dependencies\.ankinbt\]\]\s*(.*?)(?=^\s*\[\[|\z)'
    )
    $minecraftDependency = $null
    foreach ($blockMatch in $dependencyBlocks) {
        $blockText = $blockMatch.Groups[1].Value
        if ((Get-TomlQuotedAssignment -Text $blockText -Name 'modId') -ceq 'minecraft') {
            $minecraftDependency = $blockText
            break
        }
    }

    if ($null -eq $minecraftDependency) {
        Add-VerificationError -ArtifactLabel $ArtifactLabel -Message 'NeoForge 元数据缺少 Minecraft 必需依赖'
    }
    else {
        $minecraftRange = Get-TomlQuotedAssignment -Text $minecraftDependency -Name 'versionRange'
        $dependencyType = Get-TomlQuotedAssignment -Text $minecraftDependency -Name 'type'
        $dependencySide = Get-TomlQuotedAssignment -Text $minecraftDependency -Name 'side'
        if ($minecraftRange -cne $Spec.NeoForgeRange) {
            Add-VerificationError -ArtifactLabel $ArtifactLabel -Message "NeoForge Minecraft 范围应为 '$($Spec.NeoForgeRange)'，实际为 '$minecraftRange'"
        }
        if ($dependencyType -cne 'required') {
            Add-VerificationError -ArtifactLabel $ArtifactLabel -Message "NeoForge Minecraft 依赖 type 应为 required，实际为 '$dependencyType'"
        }
        if ($dependencySide -cne 'CLIENT') {
            Add-VerificationError -ArtifactLabel $ArtifactLabel -Message "NeoForge Minecraft 依赖 side 应为 CLIENT，实际为 '$dependencySide'"
        }
    }

    $unexpectedFabricMetadata = @(Get-ZipEntries -Archive $Archive -FullName 'fabric.mod.json')
    if ($unexpectedFabricMetadata.Count -gt 0) {
        Add-VerificationError -ArtifactLabel $ArtifactLabel -Message 'NeoForge JAR 不应包含 Fabric 元数据'
    }
}

function Test-SharedJarContents {
    param(
        [Parameter(Mandatory)]
        [System.IO.Compression.ZipArchive]$Archive,

        [Parameter(Mandatory)]
        [string]$ArtifactLabel,

        [Parameter(Mandatory)]
        [string]$Loader
    )

    foreach ($entryName in $requiredEntries) {
        $entry = Get-SingleZipEntry -Archive $Archive -FullName $entryName -ArtifactLabel $ArtifactLabel -Required
        if ($null -ne $entry -and $entry.Length -le 0) {
            Add-VerificationError -ArtifactLabel $ArtifactLabel -Message "JAR 条目为空：$entryName"
        }
    }

    $licenseEntries = @(
        $Archive.Entries | Where-Object {
            $_.FullName -ceq 'LICENSE' -or $_.FullName -ceq 'LICENSE_AnkiNBT'
        }
    )
    if ($licenseEntries.Count -eq 0) {
        Add-VerificationError -ArtifactLabel $ArtifactLabel -Message 'JAR 根目录缺少 GPL 3.0 许可证正文（LICENSE 或 LICENSE_AnkiNBT）'
    }
    elseif (-not [string]::IsNullOrEmpty($expectedLicenseSha256)) {
        $matchingLicense = @($licenseEntries | Where-Object {
            (Get-BytesSha256 -Bytes (Read-ZipEntryBytes -Entry $_)) -ceq $expectedLicenseSha256
        })
        if ($matchingLicense.Count -eq 0) {
            Add-VerificationError -ArtifactLabel $ArtifactLabel -Message 'JAR 内许可证正文与仓库 LICENSE 不一致'
        }
    }

    $logoEntry = Get-SingleZipEntry -Archive $Archive -FullName 'assets/ankinbt/textures/gui/editor-logo.png' -ArtifactLabel $ArtifactLabel
    if ($null -ne $logoEntry -and -not [string]::IsNullOrEmpty($expectedLogoSha256)) {
        $logoHash = Get-BytesSha256 -Bytes (Read-ZipEntryBytes -Entry $logoEntry)
        if ($logoHash -cne $expectedLogoSha256) {
            Add-VerificationError -ArtifactLabel $ArtifactLabel -Message "editor-logo.png 与仓库目标 Logo 源文件不一致，SHA-256 为 $logoHash"
        }
    }

    $languageKeys = @{}
    foreach ($languageEntryName in @('assets/ankinbt/lang/en_us.json', 'assets/ankinbt/lang/zh_cn.json')) {
        $languageEntry = Get-SingleZipEntry -Archive $Archive -FullName $languageEntryName -ArtifactLabel $ArtifactLabel
        if ($null -eq $languageEntry) {
            continue
        }

        try {
            $languageText = Read-ZipEntryText -Entry $languageEntry
            if ($languageText.Contains([char]0xFFFD)) {
                Add-VerificationError -ArtifactLabel $ArtifactLabel -Message "语言文件包含 Unicode 替换字符：$languageEntryName"
            }
            $languageData = $languageText | ConvertFrom-Json -Depth 64
            if (@($languageData.PSObject.Properties).Count -eq 0) {
                Add-VerificationError -ArtifactLabel $ArtifactLabel -Message "语言文件没有翻译项：$languageEntryName"
            }
            $languageKeys[$languageEntryName] = @($languageData.PSObject.Properties.Name | Sort-Object)
        }
        catch {
            Add-VerificationError -ArtifactLabel $ArtifactLabel -Message "语言文件不是有效 JSON：$languageEntryName；$($_.Exception.Message)"
        }
    }

    $englishLanguage = 'assets/ankinbt/lang/en_us.json'
    $chineseLanguage = 'assets/ankinbt/lang/zh_cn.json'
    if ($languageKeys.ContainsKey($englishLanguage) -and $languageKeys.ContainsKey($chineseLanguage)) {
        $languageKeyDiff = @(
            Compare-Object -ReferenceObject $languageKeys[$englishLanguage] `
                -DifferenceObject $languageKeys[$chineseLanguage] -CaseSensitive
        )
        if ($languageKeyDiff.Count -gt 0) {
            $missingEnglish = @($languageKeyDiff | Where-Object SideIndicator -ceq '=>' | ForEach-Object InputObject)
            $missingChinese = @($languageKeyDiff | Where-Object SideIndicator -ceq '<=' | ForEach-Object InputObject)
            Add-VerificationError -ArtifactLabel $ArtifactLabel -Message (
                "中英文翻译键不一致：英文缺少 $($missingEnglish.Count) 项，中文缺少 $($missingChinese.Count) 项"
            )
        }
    }

    if ($Loader -ceq 'Fabric') {
        $mixinEntry = Get-SingleZipEntry -Archive $Archive -FullName 'ankinbt.mixins.json' -ArtifactLabel $ArtifactLabel -Required
        $mixinClassEntry = Get-SingleZipEntry -Archive $Archive -FullName 'com/ankinbt/mixin/KeyboardHandlerMixin.class' -ArtifactLabel $ArtifactLabel -Required
        if ($null -ne $mixinClassEntry -and $mixinClassEntry.Length -le 0) {
            Add-VerificationError -ArtifactLabel $ArtifactLabel -Message 'KeyboardHandlerMixin.class 为空'
        }

        if ($null -ne $mixinEntry) {
            try {
                $mixinData = Read-ZipEntryText -Entry $mixinEntry | ConvertFrom-Json -Depth 32
                if ([string]$mixinData.package -cne 'com.ankinbt.mixin') {
                    Add-VerificationError -ArtifactLabel $ArtifactLabel -Message "Mixin package 应为 com.ankinbt.mixin，实际为 '$($mixinData.package)'"
                }
                if (@($mixinData.client) -cnotcontains 'KeyboardHandlerMixin') {
                    Add-VerificationError -ArtifactLabel $ArtifactLabel -Message 'ankinbt.mixins.json 未在 client 中声明 KeyboardHandlerMixin'
                }
            }
            catch {
                Add-VerificationError -ArtifactLabel $ArtifactLabel -Message "ankinbt.mixins.json 不是有效 JSON：$($_.Exception.Message)"
            }
        }
    }

    foreach ($entry in $Archive.Entries) {
        if ($entry.FullName -notmatch $textEntryPattern) {
            continue
        }

        $entryBytes = Read-ZipEntryBytes -Entry $entry
        $entryText = [System.Text.Encoding]::Latin1.GetString($entryBytes)
        foreach ($term in $forbiddenTerms) {
            if ($entryText.IndexOf($term, [System.StringComparison]::OrdinalIgnoreCase) -ge 0) {
                Add-VerificationError -ArtifactLabel $ArtifactLabel -Message "禁止文本 '$term' 出现在 $($entry.FullName)"
            }
        }
    }
}

$canonicalLogoPath = Join-Path $repositoryPath 'src/main/resources/assets/ankinbt/textures/gui/editor-logo.png'
if (-not (Test-Path -LiteralPath $canonicalLogoPath -PathType Leaf)) {
    Add-VerificationError -ArtifactLabel '仓库资源' -Message "缺少统一 Logo：$canonicalLogoPath"
}
else {
    $canonicalLogo = Get-Item -LiteralPath $canonicalLogoPath
    if ($canonicalLogo.Length -le 0) {
        Add-VerificationError -ArtifactLabel '仓库资源' -Message "统一 editor-logo.png 为空：$canonicalLogoPath"
    }
    else {
        $expectedLogoSha256 = (Get-FileHash -LiteralPath $canonicalLogoPath -Algorithm SHA256).Hash
    }
}

$canonicalLicensePath = Join-Path $repositoryPath 'LICENSE'
if (-not (Test-Path -LiteralPath $canonicalLicensePath -PathType Leaf)) {
    Add-VerificationError -ArtifactLabel '仓库许可证' -Message "缺少 GPL 3.0 许可证文件：$canonicalLicensePath"
}
else {
    $canonicalLicenseText = Get-Content -LiteralPath $canonicalLicensePath -Raw
    $requiredLicenseMarkers = @(
        'GNU GENERAL PUBLIC LICENSE'
        'Version 3, 29 June 2007'
        'END OF TERMS AND CONDITIONS'
    )
    foreach ($marker in $requiredLicenseMarkers) {
        if ($canonicalLicenseText.IndexOf($marker, [System.StringComparison]::Ordinal) -lt 0) {
            Add-VerificationError -ArtifactLabel '仓库许可证' -Message "LICENSE 缺少 GPL 3.0 正文标记：$marker"
        }
    }
    $expectedLicenseSha256 = (Get-FileHash -LiteralPath $canonicalLicensePath -Algorithm SHA256).Hash
}

foreach ($spec in $matrix) {
    $fabricBase = if ($spec.Minecraft -ceq '1.21.1') {
        Join-Path $repositoryPath 'fabric'
    }
    else {
        Join-Path $repositoryPath "fabric-versions/$($spec.Minecraft)"
    }

    $artifactSpecs = @(
        [pscustomobject]@{
            Loader = 'Fabric'
            Minecraft = $spec.Minecraft
            ExpectedRange = $spec.FabricRange
            Path = Join-Path $fabricBase "build/libs/AnkiNBT-Fabric-mc$($spec.Minecraft)-$releaseVersion.jar"
        }
        [pscustomobject]@{
            Loader = 'NeoForge'
            Minecraft = $spec.Minecraft
            ExpectedRange = $spec.NeoForgeRange
            Path = Join-Path $repositoryPath "versions/$($spec.Minecraft)/build/libs/AnkiNBT-NeoForge-mc$($spec.Minecraft)-$releaseVersion.jar"
        }
    )

    foreach ($artifactSpec in $artifactSpecs) {
        $artifactLabel = "$($artifactSpec.Loader) $($artifactSpec.Minecraft)"
        $artifactPath = [System.IO.Path]::GetFullPath($artifactSpec.Path)
        $artifact = [pscustomobject]@{
            Loader = $artifactSpec.Loader
            MinecraftVersion = $artifactSpec.Minecraft
            ExpectedMinecraftRange = $artifactSpec.ExpectedRange
            JarPath = $artifactPath
            FileName = [System.IO.Path]::GetFileName($artifactPath)
            Sha256 = $null
        }
        $artifacts.Add($artifact)

        if (-not (Test-Path -LiteralPath $artifactPath -PathType Leaf)) {
            Add-VerificationError -ArtifactLabel $artifactLabel -Message "缺少发布 JAR：$artifactPath"
            continue
        }

        $artifact.Sha256 = (Get-FileHash -LiteralPath $artifactPath -Algorithm SHA256).Hash
        $archive = $null
        try {
            $archive = [System.IO.Compression.ZipFile]::OpenRead($artifactPath)
            Test-SharedJarContents -Archive $archive -ArtifactLabel $artifactLabel -Loader $artifactSpec.Loader
            if ($artifactSpec.Loader -ceq 'Fabric') {
                Test-FabricMetadata -Archive $archive -Spec $spec -ArtifactLabel $artifactLabel
            }
            else {
                Test-NeoForgeMetadata -Archive $archive -Spec $spec -ArtifactLabel $artifactLabel
            }
        }
        catch {
            Add-VerificationError -ArtifactLabel $artifactLabel -Message "无法校验 JAR：$($_.Exception.Message)"
        }
        finally {
            if ($null -ne $archive) {
                $archive.Dispose()
            }
        }
    }
}

$result = [pscustomobject]@{
    Success = ($verificationErrors.Count -eq 0)
    ReleaseVersion = $releaseVersion
    ExpectedArtifactCount = 30
    VerifiedArtifactCount = @($artifacts | Where-Object { $null -ne $_.Sha256 }).Count
    Artifacts = @($artifacts)
    Errors = @($verificationErrors)
}

if (-not $Quiet) {
    if ($result.Success) {
        Write-Host "发布校验通过：$($result.VerifiedArtifactCount)/$($result.ExpectedArtifactCount) 个 JAR，版本 $releaseVersion。" -ForegroundColor Green
    }
    else {
        Write-Host "发布校验失败：发现 $($result.Errors.Count) 个问题。" -ForegroundColor Red
        foreach ($verificationError in $result.Errors) {
            Write-Host "  - $verificationError" -ForegroundColor Red
        }
    }
}

if ($PassThru) {
    return $result
}

if (-not $result.Success) {
    throw "AnkiNBT $releaseVersion 发布校验未通过。"
}
