$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

Write-Host 'Java runtime:'
& java -version
if ($LASTEXITCODE -ne 0) { throw 'Java nije dostupna.' }

Write-Host 'Java compiler:'
& javac -version
if ($LASTEXITCODE -ne 0) { throw 'Potreban je puni JDK.' }

Write-Host 'Maven Wrapper i Java koju koristi:'
& .\mvnw.cmd -v
if ($LASTEXITCODE -ne 0) { throw 'Maven Wrapper se ne može pokrenuti.' }

Write-Host 'Provjerite da Maven Wrapper koristi JDK 25.'
