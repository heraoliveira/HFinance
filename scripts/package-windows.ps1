param()

$ErrorActionPreference = "Stop"
$AppVersion = "1.2.2"
$ProjectRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
Set-Location $ProjectRoot

function Assert-Command {
    param(
        [string] $Name,
        [string] $Message
    )

    if (-not (Get-Command $Name -ErrorAction SilentlyContinue)) {
        throw $Message
    }
}

function Invoke-VersionCommand {
    param(
        [string] $Command,
        [string[]] $Arguments = @()
    )

    $previousPreference = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    $output = (& $Command @Arguments 2>&1) -join "`n"
    $exitCode = $LASTEXITCODE
    $ErrorActionPreference = $previousPreference
    return [PSCustomObject]@{
        Output = $output
        ExitCode = $exitCode
    }
}

function Assert-Java17 {
    Assert-Command "java" "O Java não foi encontrado no PATH."
    Assert-Command "jpackage" "O jpackage do JDK 17 não foi encontrado no PATH."

    $javaVersion = Invoke-VersionCommand "java" @("-version")
    $jpackageVersion = Invoke-VersionCommand "jpackage" @("--version")

    if ($javaVersion.ExitCode -ne 0 -or -not ($javaVersion.Output -match 'version "17\.')) {
        throw "O empacotamento exige JDK 17."
    }
    if ($jpackageVersion.ExitCode -ne 0 -or -not ($jpackageVersion.Output -match '^17\.')) {
        throw "O jpackage encontrado não é do JDK 17."
    }

    $javaFirstLine = ($javaVersion.Output -split "`n")[0].Trim()
    Write-Host "Java validado: $javaFirstLine"
    Write-Host "jpackage validado: $($jpackageVersion.Output.Trim())"
}

function Assert-Wix {
    $wix = Get-Command "wix" -ErrorAction SilentlyContinue
    if ($wix) {
        $wixVersion = Invoke-VersionCommand "wix" @("--version")
        if ($wixVersion.ExitCode -ne 0) {
            throw "O comando wix foi encontrado, mas não retornou versão válida."
        }
        Write-Host "WiX validado: $($wixVersion.Output.Trim())"
        return
    }

    $candle = Get-Command "candle.exe" -ErrorAction SilentlyContinue
    $light = Get-Command "light.exe" -ErrorAction SilentlyContinue
    if ($candle -and $light) {
        Write-Host "WiX validado: candle.exe=$($candle.Source); light.exe=$($light.Source)"
        return
    }

    throw "WiX não encontrado. Instale o WiX Toolset e confirme com: wix --version"
}

function Resolve-InsideProject {
    param([string] $Path)

    $resolvedProject = [System.IO.Path]::GetFullPath($ProjectRoot)
    $resolvedTarget = [System.IO.Path]::GetFullPath($Path)
    if (-not $resolvedTarget.StartsWith($resolvedProject, [System.StringComparison]::OrdinalIgnoreCase)) {
        throw "Caminho fora do projeto recusado: $resolvedTarget"
    }
    return $resolvedTarget
}

function Remove-DirectoryInsideProject {
    param([string] $Path)

    if (-not (Test-Path $Path)) {
        return
    }
    $resolvedTarget = Resolve-InsideProject (Resolve-Path -LiteralPath $Path).Path
    Remove-Item -LiteralPath $resolvedTarget -Recurse -Force
}

function Remove-FileInsideProject {
    param([string] $Path)

    if (-not (Test-Path $Path)) {
        return
    }
    $resolvedTarget = Resolve-InsideProject (Resolve-Path -LiteralPath $Path).Path
    Remove-Item -LiteralPath $resolvedTarget -Force
}

Assert-Command "mvn" "O Maven não foi encontrado no PATH."
Assert-Java17
Assert-Wix

$InputDir = Join-Path $ProjectRoot "target\package\input"
$PackageDir = Join-Path $ProjectRoot "target\package"
$ReleaseDir = Join-Path $ProjectRoot "target\release"
$AppImageDir = Join-Path $PackageDir "HFinance"
$JarName = "hfinance-$AppVersion.jar"
$JarPath = Join-Path $ProjectRoot "target\$JarName"
$IconPath = Join-Path $ProjectRoot "src\main\resources\images\app-icon.ico"
$PortableZipPath = Join-Path $ReleaseDir "HFinance-v$AppVersion-windows.zip"
$FinalInstallerPath = Join-Path $ReleaseDir "HFinance-Setup-v$AppVersion.exe"

if (-not (Test-Path $IconPath)) {
    throw "Ícone não encontrado: $IconPath"
}

Remove-DirectoryInsideProject $InputDir
Remove-DirectoryInsideProject $AppImageDir
Remove-FileInsideProject $PortableZipPath
Remove-FileInsideProject $FinalInstallerPath
New-Item -ItemType Directory -Force -Path $InputDir | Out-Null

Write-Host "Compilando, testando e preparando dependências..."
mvn clean package dependency:copy-dependencies "-DincludeScope=runtime" "-DoutputDirectory=$InputDir"

if (-not (Test-Path $JarPath)) {
    throw "JAR não encontrado após o build: $JarPath"
}
New-Item -ItemType Directory -Force -Path $ReleaseDir | Out-Null
Copy-Item -LiteralPath $JarPath -Destination $InputDir -Force

$CommonJPackageArgs = @(
    "--name", "HFinance",
    "--app-version", $AppVersion,
    "--vendor", "HFinance",
    "--description", "Solução desktop local para finanças pessoais",
    "--icon", $IconPath,
    "--input", $InputDir,
    "--main-jar", $JarName,
    "--main-class", "com.hfinance.HFinanceLauncher",
    "--java-options", "-Dfile.encoding=UTF-8"
)

$InstallerJPackageArgs = @(
    "--win-menu",
    "--win-menu-group", "HFinance",
    "--win-shortcut",
    "--win-dir-chooser",
    "--win-per-user-install",
    "--win-upgrade-uuid", "2f02df7a-bb02-44ed-9ed5-4bc9f40df910"
)

Write-Host "Gerando imagem da aplicação com jpackage..."
& jpackage "--type" "app-image" "--dest" $PackageDir @CommonJPackageArgs

$ExePath = Join-Path $AppImageDir "HFinance.exe"
if (-not (Test-Path $ExePath)) {
    throw "O jpackage terminou, mas HFinance.exe não foi encontrado em $AppImageDir."
}
Write-Host "Aplicação empacotada com sucesso: $ExePath"

$LooseIconPath = Join-Path $AppImageDir "HFinance.ico"
Remove-FileInsideProject $LooseIconPath

Write-Host "Gerando ZIP portátil..."
Compress-Archive -LiteralPath $AppImageDir -DestinationPath $PortableZipPath -CompressionLevel Optimal
if (-not (Test-Path $PortableZipPath)) {
    throw "ZIP portátil não foi criado em: $PortableZipPath"
}
Write-Host "ZIP portátil criado com sucesso: $PortableZipPath"

Write-Host "Gerando instalador Windows com jpackage e WiX..."
& jpackage "--type" "exe" "--dest" $ReleaseDir @CommonJPackageArgs @InstallerJPackageArgs

$generatedInstaller = Get-ChildItem -LiteralPath $ReleaseDir -Filter "*.exe" |
        Where-Object { $_.Name -like "HFinance*.exe" } |
        Sort-Object LastWriteTime -Descending |
        Select-Object -First 1

if (-not $generatedInstaller) {
    throw "O jpackage terminou, mas nenhum instalador .exe foi encontrado em $ReleaseDir."
}

if ($generatedInstaller.FullName -ne $FinalInstallerPath) {
    Move-Item -LiteralPath $generatedInstaller.FullName -Destination $FinalInstallerPath -Force
}

if (-not (Test-Path $FinalInstallerPath)) {
    throw "Instalador final não foi encontrado em: $FinalInstallerPath"
}
Write-Host "Instalador Windows criado com sucesso: $FinalInstallerPath"

Write-Host "Artefatos finais:"
Write-Host " - $PortableZipPath"
Write-Host " - $FinalInstallerPath"
Write-Host "Dados do usuário não são gravados na pasta de instalação. O diretório oficial é %APPDATA%\HFinance."
