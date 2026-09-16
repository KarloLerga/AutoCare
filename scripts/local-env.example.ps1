# No password in committed examples. This script sets values for the current process only.
$env:AUTOCARE_DB_HOST = 'auto-care.database.windows.net'
$env:AUTOCARE_DB_PORT = '1433'
$env:AUTOCARE_DB_NAME = 'REPLACE_WITH_EXISTING_DATABASE_NAME'
$env:AUTOCARE_DB_USER = 'karlolerga'
$secure = Read-Host 'SQL password' -AsSecureString
$pointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure)
try { $env:AUTOCARE_DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($pointer) }
finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($pointer) }
