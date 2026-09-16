$ErrorActionPreference = 'Stop'
New-Item -ItemType Directory -Force docs/evidence | Out-Null
& git log --graph --all --decorate --date=iso-strict --pretty=format:'%h %ad %d %s' | Out-File -Encoding utf8 docs/evidence/git-dag.txt
if ($LASTEXITCODE -ne 0) { throw 'Git history could not be exported.' }
& git rev-parse HEAD | Out-File -Encoding utf8 docs/evidence/git-head.txt
Write-Host 'Evidence represents the actual history at the recorded HEAD; do not fabricate earlier dates.'
