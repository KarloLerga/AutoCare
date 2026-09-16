$ErrorActionPreference = 'Stop'
Write-Host 'Java runtime:'; & java -version
if ($LASTEXITCODE -ne 0) { throw 'Java is not available.' }
Write-Host 'Java compiler:'; & javac -version
if ($LASTEXITCODE -ne 0) { throw 'A full JDK is required.' }
Write-Host 'Maven and its selected Java home:'; & mvn -v
if ($LASTEXITCODE -ne 0) { throw 'Install Maven 3.9.16 or generate the standard Maven Wrapper first.' }
Write-Host 'Compare all outputs. Maven must use JDK 25; changing the VS Code runtime alone is insufficient.'
