# Optional one-time Maven download into ignored .tools, verified against Apache's published SHA-512.
# This is a bootstrap helper, NOT an imitation of the official Maven Wrapper.
$ErrorActionPreference = 'Stop'
$version = '3.9.16'
$root = Split-Path -Parent $PSScriptRoot
$tools = Join-Path $root '.tools'
$zip = Join-Path $tools "apache-maven-$version-bin.zip"
$mavenHome = Join-Path $tools "apache-maven-$version"
New-Item -ItemType Directory -Force $tools | Out-Null
if (-not (Test-Path (Join-Path $mavenHome 'bin/mvn.cmd'))) {
    $base = "https://downloads.apache.org/maven/maven-3/$version/binaries/apache-maven-$version-bin.zip"
    Invoke-WebRequest -Uri $base -OutFile $zip
    $checksumText = (& curl.exe -fsSL --max-time 30 "$base.sha512" | Out-String)
    if ($LASTEXITCODE -ne 0) { throw 'Apache SHA-512 download failed.' }
    $expected = ([regex]::Match($checksumText, '(?i)\b[0-9a-f]{128}\b')).Value
    if (-not $expected) { throw 'Apache SHA-512 could not be read. Do not skip checksum verification.' }
    $actual = (Get-FileHash -Algorithm SHA512 $zip).Hash
    if ($actual -ne $expected) { Remove-Item $zip; throw 'Maven checksum mismatch.' }
    Expand-Archive -Path $zip -DestinationPath $tools -Force
}
$env:Path = (Join-Path $mavenHome 'bin') + ';' + $env:Path
& (Join-Path $mavenHome 'bin/mvn.cmd') -v
if ($LASTEXITCODE -ne 0) { throw 'Maven failed to start. Verify JAVA_HOME points to JDK 25.' }
Write-Host 'Maven is available in this terminal. Generate the official wrapper next (instructions in README).'
