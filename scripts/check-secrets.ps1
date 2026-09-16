# Review staged content without printing any matched secrets.
$ErrorActionPreference='Stop'
$files=@(& git diff --cached --name-only)
if($LASTEXITCODE -ne 0){throw 'Not in a Git repository.'}
foreach($file in $files){
 if($file -match '(^|/)(private|source-materials|history)/|\.local\.json$|local-env\.ps1$|\.zip$') { throw 'Staged private/archive file; unstage and inspect.' }
}
$diff=(& git diff --cached --no-ext-diff | Out-String)
if($diff -match '(?im)^[+]\s*(password|AUTOCARE_DB_PASSWORD)\s*[:=]\s*["'']?[^$\s]' -or $diff -match 'gh[pousr]_[A-Za-z0-9]{20,}|sk-proj-[A-Za-z0-9_-]{20,}') { throw 'Potential credential in staged diff. Review privately.' }
if($env:AUTOCARE_DB_PASSWORD -and $diff.Contains($env:AUTOCARE_DB_PASSWORD)){throw 'Actual local SQL credential is present in staged content.'}
Write-Host 'Basic staged secret check passed. Still review the diff manually; this is not a complete secret scanner.'
