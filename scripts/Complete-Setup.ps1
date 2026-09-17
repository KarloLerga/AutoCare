param(
 [Parameter(Mandatory=$true)][string]$ConfigPath,
 [string]$DatabaseName,
 [switch]$ImportCompleteCatalog,
 [switch]$ApplyFinalSchema,
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
Invoke-Native '.\mvnw.cmd' @('clean','install')
Invoke-Native '.\mvnw.cmd' @('-f','tools/setup/pom.xml','clean','package')

$jar=Join-Path $root 'tools/setup/target/autocare-setup-1.0.0.jar'
Invoke-Native 'java' @('-jar',$jar,'sql-check')

$catalog=Join-Path $root 'tools/reference-data/data/vehicle_work_rules_complete.csv.gz'
if($ImportCompleteCatalog) {
 if(-not (Test-Path -LiteralPath $catalog)) { throw 'Kompletni katalog nedostaje.' }
 Invoke-Native 'java' @('-Xmx768m','-jar',$jar,'import-complete-catalog',$catalog,'--apply','--confirm-complete-catalog')
} else {
 Invoke-Native 'java' @('-jar',$jar,'import-complete-catalog',$catalog)
}

if($ApplyFinalSchema) {
 $env:AUTOCARE_SCHEMA_TARGET=$env:AUTOCARE_DB_NAME
 Invoke-Native 'java' @('-jar',$jar,'apply-final-schema',(Join-Path $root 'schema/09_complete_catalog_and_runtime_cleanup.sql'),'--apply','--confirm-final-schema')
 Invoke-Native 'java' @('-jar',$jar,'apply-final-schema',(Join-Path $root 'schema/10_croatian_work_names.sql'),'--apply','--confirm-final-schema')
} else {
 Invoke-Native 'java' @('-jar',$jar,'apply-final-schema',(Join-Path $root 'schema/09_complete_catalog_and_runtime_cleanup.sql'))
 Invoke-Native 'java' @('-jar',$jar,'apply-final-schema',(Join-Path $root 'schema/10_croatian_work_names.sql'))
}

Invoke-Native 'java' @('-jar',$jar,'final-audit',(Join-Path $root 'schema/final_catalog_audit.sql'))
if($Launch) {
 Invoke-Native 'powershell.exe' @('-NoProfile','-ExecutionPolicy','Bypass','-File',(Join-Path $root 'scripts/Run-App.ps1'),'-ConfigPath',$ConfigPath)
}
