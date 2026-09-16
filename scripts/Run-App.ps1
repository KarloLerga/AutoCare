param([Parameter(Mandatory=$true)][string]$ConfigPath,[string]$DatabaseName)
$ErrorActionPreference='Stop'
Set-Location (Split-Path -Parent $PSScriptRoot)
. (Join-Path $PSScriptRoot 'Load-Connection.ps1') -ConfigPath $ConfigPath -DatabaseName $DatabaseName
if(-not $env:AUTOCARE_DB_NAME){throw 'Supply exact DatabaseName, or store discovered name in private local JSON.'}
& java -jar target/autocare-1.0.0.jar
if($LASTEXITCODE -ne 0){throw 'Application failed.'}
