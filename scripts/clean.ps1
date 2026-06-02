param(
    [switch] $WithLocalData
)

$ErrorActionPreference = "Stop"
$ProjectRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
Set-Location $ProjectRoot

if (Test-Path "target") {
    Remove-Item -LiteralPath "target" -Recurse -Force
}

if ($WithLocalData -and (Test-Path "data")) {
    Remove-Item -LiteralPath "data" -Recurse -Force
}

Write-Host "Arquivos temporários de build removidos."
