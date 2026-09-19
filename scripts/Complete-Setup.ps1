param(
 [Parameter(Mandatory=$true)][string]$ConfigPath,
 [string]$DatabaseName,
 [switch]$ApplyProfessorModel,
 [switch]$Launch
)
$ErrorActionPreference='Stop'
$root=Split-Path -Parent $PSScriptRoot
Set-Location $root
. (Join-Path $PSScriptRoot 'Load-Connection.ps1') -ConfigPath $ConfigPath -DatabaseName $DatabaseName

function Invoke-Native([string]$Exe, [string[]]$Arguments) {
 & $Exe @Arguments
 if($LASTEXITCODE -ne 0) { throw "Command failed with exit code $LASTEXITCODE." }
}

if(-not (Test-Path '.\mvnw.cmd')) { throw 'Maven Wrapper nedostaje.' }
Invoke-Native '.\mvnw.cmd' @('clean','verify','javadoc:javadoc')
Invoke-Native '.\mvnw.cmd' @('-f','tools/setup/pom.xml','clean','package')

$jar=Join-Path $root 'tools/setup/target/autocare-setup-1.0.0.jar'
Invoke-Native 'java' @('-jar',$jar,'sql-check')

$migration=Join-Path $root 'schema/11_professor_model.sql'
if($ApplyProfessorModel) {
 Invoke-Native 'java' @('-jar',$jar,'apply-final-schema',$migration,'--apply','--confirm-final-schema')
 Invoke-Native 'java' @('-jar',$jar,'final-audit',(Join-Path $root 'schema/12_professor_model_audit.sql'))
} else {
 Invoke-Native 'java' @('-jar',$jar,'apply-final-schema',$migration)
 Write-Host 'Dry run je prošao. Za stvarnu migraciju pokrenite isti script s -ApplyProfessorModel.'
}

if($Launch) {
 if(-not $ApplyProfessorModel) { throw 'Za Launch nakon promjene modela prvo dodajte -ApplyProfessorModel.' }
 Invoke-Native 'powershell.exe' @('-NoProfile','-ExecutionPolicy','Bypass','-File',(Join-Path $root 'scripts/Run-App.ps1'),'-ConfigPath',$ConfigPath)
}
