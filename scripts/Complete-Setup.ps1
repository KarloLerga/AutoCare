param(
 [Parameter(Mandatory=$true)][string]$ConfigPath,
 [string]$DatabaseName,
 [switch]$InitializeSchema,
 [switch]$FullData,
 [switch]$WithReferencedIntervals,
 [switch]$PlainJdbc,
 [switch]$Launch
)
$ErrorActionPreference='Stop'
$root=Split-Path -Parent $PSScriptRoot
Set-Location $root
. (Join-Path $PSScriptRoot 'Load-Connection.ps1') -ConfigPath $ConfigPath -DatabaseName $DatabaseName
function Invoke-Native([string]$Exe, [string[]]$Arguments) {
 & $Exe @Arguments
 if($LASTEXITCODE -ne 0) { throw "Command failed with exit code $LASTEXITCODE. Inspect its error before retrying." }
}
# Check compiler as well as java launcher; Maven must use the same JDK.
function Get-NativeVersion([string]$Exe) {
 $saved=$ErrorActionPreference
 try { $ErrorActionPreference='Continue'; $text=(& $Exe -version 2>&1 | Out-String); $exit=$LASTEXITCODE }
 finally { $ErrorActionPreference=$saved }
 if($exit -ne 0){throw "Cannot execute $Exe."}
 return $text
}
$javaVersion=Get-NativeVersion 'java'
$javacVersion=Get-NativeVersion 'javac'
if($javaVersion -notmatch 'version "25\.' -or $javacVersion -notmatch 'javac 25\.') { throw 'JDK 25 java AND javac are required. Correct JAVA_HOME/PATH; do not change release to 21.' }
if(Test-Path './mvnw.cmd') { $maven=(Resolve-Path './mvnw.cmd').Path }
else {
 if(-not (Get-Command mvn.cmd -ErrorAction SilentlyContinue)) { . (Join-Path $PSScriptRoot 'bootstrap-maven.ps1') }
 $maven=(Get-Command mvn.cmd -ErrorAction Stop).Source
 Invoke-Native $maven @('-N','org.apache.maven.plugins:maven-wrapper-plugin:3.3.4:wrapper','-Dmaven=3.9.16','-Dtype=only-script')
 $maven=(Resolve-Path './mvnw.cmd').Path
}
$mv=Get-NativeVersion $maven
if($mv -notmatch 'Java version: 25\.') { throw 'Maven is not using JDK 25. Fix JAVA_HOME.' }
Invoke-Native $maven @('clean','verify')
$jar=Join-Path $root 'target/autocare-1.0.0.jar'
if(-not $env:AUTOCARE_DB_NAME) {
 Write-Host 'Discovering database names only; not creating resources or changing billing.'
 $names=@(& java -jar $jar db-list)
 if($LASTEXITCODE -ne 0) { throw 'Read-only discovery failed. Supply actual DatabaseName from Azure portal and check firewall/SQL authentication.' }
 $names=@($names | Where-Object { -not [string]::IsNullOrWhiteSpace($_) })
 if($names.Count -eq 1) { $env:AUTOCARE_DB_NAME=[string]$names[0] }
 elseif($names.Count -gt 1) {
   $names | ForEach-Object { Write-Host $_ }
   $env:AUTOCARE_DB_NAME=Read-Host 'Exact existing development database name'
   if($names -notcontains $env:AUTOCARE_DB_NAME) { throw 'Selected name is not in discovered list.' }
 } else { throw 'No accessible user database. Supply exact existing name; never create a paid substitute automatically.' }
 Write-Host "Selected existing database: $env:AUTOCARE_DB_NAME"
}
Invoke-Native 'java' @('-jar',$jar,'sql-check')
$local=Get-Content -LiteralPath $ConfigPath -Raw | ConvertFrom-Json
$local.database=$env:AUTOCARE_DB_NAME
$local | ConvertTo-Json | Set-Content -LiteralPath $ConfigPath -Encoding UTF8
if($InitializeSchema) {
 $env:AUTOCARE_SCHEMA_TARGET=$env:AUTOCARE_DB_NAME
 Invoke-Native 'java' @('-jar',$jar,'schema-update','--confirm-development-schema')
}
Invoke-Native 'java' @('-jar',$jar,'db-check')
$env:AUTOCARE_SEED_TARGET=$env:AUTOCARE_DB_NAME
$data=Join-Path $root 'tools/reference-data/data'
if(-not $FullData) { $data=Join-Path $data 'sample' }
$seedArgs=@('-Xmx768m','-jar',$jar,'seed-all',$data,'--apply','--acknowledge-model-estimates','--with-diagnostics')
if($WithReferencedIntervals) { $seedArgs+='--with-referenced-intervals' }
if($PlainJdbc) { $seedArgs+='--plain-jdbc' }
Invoke-Native 'java' $seedArgs
Invoke-Native 'java' @('-jar',$jar,'db-check')
Invoke-Native $maven @('javadoc:javadoc')
Write-Host 'Build and selected seed finished. GUI/manual/integration tests still must be recorded separately.'
Write-Host 'Disconnect SQL Object Explorer and close the application when finished to allow serverless auto-pause.'
if($Launch) { Invoke-Native 'java' @('-jar',$jar) }
