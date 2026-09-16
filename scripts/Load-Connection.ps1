param([Parameter(Mandatory=$true)][string]$ConfigPath, [string]$DatabaseName)
$ErrorActionPreference='Stop'
$config=Get-Content -LiteralPath $ConfigPath -Raw | ConvertFrom-Json
if (-not $config.host -or -not $config.user -or -not $config.password) { throw 'Incomplete local SQL configuration.' }
$env:AUTOCARE_DB_HOST=[string]$config.host
$env:AUTOCARE_DB_PORT=[string]$config.port
$env:AUTOCARE_DB_USER=[string]$config.user
$env:AUTOCARE_DB_PASSWORD=[string]$config.password
$env:AUTOCARE_DB_NAME=if($DatabaseName){$DatabaseName}else{[string]$config.database}
# Never print the config object, environment dump or password.
