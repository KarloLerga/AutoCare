param([string]$ConfigPath,[string]$DatabaseName)
$ErrorActionPreference='Stop'
Set-Location (Split-Path -Parent $PSScriptRoot)
$arguments=@()
if($ConfigPath){
 if(-not (Test-Path -LiteralPath $ConfigPath)){throw 'Local SQL configuration does not exist.'}
 $arguments += "-Dautocare.config=$ConfigPath"
}
& java @arguments -jar target/autocare-1.0.0.jar
if($LASTEXITCODE -ne 0){throw 'Application failed.'}
