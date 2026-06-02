param()

$ErrorActionPreference = "Stop"
$ProjectRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
Set-Location $ProjectRoot

function Assert-Java17 {
    $previousPreference = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    $version = (& java -version 2>&1) -join "`n"
    $javaExitCode = $LASTEXITCODE
    $jpackageVersion = (& jpackage --version 2>&1) -join "`n"
    $jpackageExitCode = $LASTEXITCODE
    $ErrorActionPreference = $previousPreference
    if ($javaExitCode -ne 0 -or -not ($version -match 'version "17\.')) {
        throw "O empacotamento exige JDK 17 com jpackage disponível."
    }
    if ($jpackageExitCode -ne 0 -or -not ($jpackageVersion -match '^17\.')) {
        throw "O jpackage do JDK 17 não foi encontrado no PATH."
    }
}

Assert-Java17

$InputDir = Join-Path $ProjectRoot "target\package\input"
$OutputDir = Join-Path $ProjectRoot "target\package"
$AppImageDir = Join-Path $OutputDir "HFinance"

if (Test-Path $InputDir) {
    Remove-Item -LiteralPath $InputDir -Recurse -Force
}
if (Test-Path $AppImageDir) {
    Remove-Item -LiteralPath $AppImageDir -Recurse -Force
}
New-Item -ItemType Directory -Force -Path $InputDir | Out-Null

mvn -DskipTests package dependency:copy-dependencies "-DincludeScope=runtime" "-DoutputDirectory=$InputDir"
Copy-Item -LiteralPath (Join-Path $ProjectRoot "target\hfinance-1.0.1.jar") -Destination $InputDir -Force

jpackage `
    --type app-image `
    --name HFinance `
    --input $InputDir `
    --main-jar hfinance-1.0.1.jar `
    --main-class com.hfinance.HFinanceLauncher `
    --dest $OutputDir `
    --java-options "-Dfile.encoding=UTF-8"

$ExePath = Join-Path $AppImageDir "HFinance.exe"
if (Test-Path $ExePath) {
    Write-Host "Aplicação empacotada com sucesso: $ExePath"
} else {
    throw "O jpackage terminou, mas HFinance.exe não foi encontrado em $AppImageDir."
}
