param()

$ErrorActionPreference = "Stop"
$AppVersion = "1.1.0"
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

function Assert-Java17 {
    Assert-Command "java" "O Java não foi encontrado no PATH."
    Assert-Command "jpackage" "O jpackage do JDK 17 não foi encontrado no PATH."

    $previousPreference = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    $javaVersion = (& java -version 2>&1) -join "`n"
    $javaExitCode = $LASTEXITCODE
    $jpackageVersion = (& jpackage --version 2>&1) -join "`n"
    $jpackageExitCode = $LASTEXITCODE
    $ErrorActionPreference = $previousPreference

    if ($javaExitCode -ne 0 -or -not ($javaVersion -match 'version "17\.')) {
        throw "O empacotamento exige JDK 17."
    }
    if ($jpackageExitCode -ne 0 -or -not ($jpackageVersion -match '^17\.')) {
        throw "O jpackage encontrado não é do JDK 17."
    }
}

function Remove-DirectoryInsideProject {
    param([string] $Path)

    if (-not (Test-Path $Path)) {
        return
    }

    $resolvedProject = [System.IO.Path]::GetFullPath($ProjectRoot)
    $resolvedTarget = [System.IO.Path]::GetFullPath((Resolve-Path -LiteralPath $Path).Path)
    if (-not $resolvedTarget.StartsWith($resolvedProject, [System.StringComparison]::OrdinalIgnoreCase)) {
        throw "Recusa de remoção fora do projeto: $resolvedTarget"
    }
    Remove-Item -LiteralPath $resolvedTarget -Recurse -Force
}

function Test-WixAvailable {
    return [bool] (Get-Command "candle.exe" -ErrorAction SilentlyContinue) -and `
           [bool] (Get-Command "light.exe" -ErrorAction SilentlyContinue)
}

Assert-Command "mvn" "O Maven não foi encontrado no PATH."
Assert-Java17

$InputDir = Join-Path $ProjectRoot "target\package\input"
$OutputDir = Join-Path $ProjectRoot "target\package"
$AppImageDir = Join-Path $OutputDir "HFinance"
$JarName = "hfinance-$AppVersion.jar"
$JarPath = Join-Path $ProjectRoot "target\$JarName"
$IconPath = Join-Path $ProjectRoot "src\main\resources\images\app-icon.ico"

if (-not (Test-Path $IconPath)) {
    throw "Ícone não encontrado: $IconPath"
}

Remove-DirectoryInsideProject $InputDir
Remove-DirectoryInsideProject $AppImageDir
New-Item -ItemType Directory -Force -Path $InputDir | Out-Null

Write-Host "Compilando, testando e preparando dependências..."
mvn clean package dependency:copy-dependencies "-DincludeScope=runtime" "-DoutputDirectory=$InputDir"

if (-not (Test-Path $JarPath)) {
    throw "JAR não encontrado após o build: $JarPath"
}
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
    "--dest", $OutputDir,
    "--java-options", "-Dfile.encoding=UTF-8"
)

Write-Host "Gerando imagem da aplicação com jpackage..."
& jpackage "--type" "app-image" @CommonJPackageArgs

$ExePath = Join-Path $AppImageDir "HFinance.exe"
if (Test-Path $ExePath) {
    Write-Host "Aplicação empacotada com sucesso: $ExePath"
} else {
    throw "O jpackage terminou, mas HFinance.exe não foi encontrado em $AppImageDir."
}

if (Test-WixAvailable) {
    Write-Host "WiX encontrado. Gerando instalador Windows..."
    & jpackage "--type" "exe" "--win-menu" "--win-shortcut" "--win-dir-chooser" "--win-per-user-install" @CommonJPackageArgs
    $InstallerPath = Join-Path $OutputDir "HFinance-$AppVersion.exe"
    if (Test-Path $InstallerPath) {
        Write-Host "Instalador gerado com sucesso: $InstallerPath"
    } else {
        Write-Host "O jpackage executou o instalador, mas o arquivo esperado não foi encontrado em: $InstallerPath"
    }
} else {
    Write-Host "WiX não encontrado; o instalador .exe não foi gerado."
    Write-Host "A imagem da aplicação foi criada e pode ser distribuída como pasta compactada."
}

Write-Host "Dados do usuário não são gravados na pasta de instalação. O diretório oficial é %APPDATA%\HFinance."
