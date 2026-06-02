param()

$ErrorActionPreference = "Stop"
$ProjectRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
Set-Location $ProjectRoot

function Assert-Java17 {
    $previousPreference = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    $version = (& java -version 2>&1) -join "`n"
    $javaExitCode = $LASTEXITCODE
    $ErrorActionPreference = $previousPreference
    if ($javaExitCode -ne 0 -or -not ($version -match 'version "17\.')) {
        throw "O HFinance exige Java 17. Instale ou selecione um JDK 17 antes de executar a aplicação."
    }
}

Assert-Java17
mvn javafx:run
