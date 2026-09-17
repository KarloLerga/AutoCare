param()

$ErrorActionPreference = 'Stop'
$runtimeRoot = Join-Path $PSScriptRoot '..\src\main\java'

$forbiddenPatterns = [ordered]@{
  Lambda = '->'
  MethodReference = '::'
  Stream = '\.\s*stream\s*\('
  FunctionalPackage = 'java\.util\.function'
  FunctionalType = '\b(Function|Consumer|BiFunction|LongConsumer|Callable)\b'
  Optional = '\bOptional\b'
  ComputeIfAbsent = 'computeIfAbsent'
  Record = '\brecord\b'
  Var = '\bvar\b'
  TransactionRunner = 'TransactionRunner'
  UiTasks = 'UiTasks'
  GenericDataTable = 'DataTable\s*<'
  ScheduleKind = '\bScheduleKind\b|\bscheduleKind\b'
  RequestKey = '\bRequestKey\b|\brequestKey\b'
  VersionAnnotation = '@Version'
  Clock = '\bClock\b'
  DefensiveEventCopy = 'List\.copyOf'
  EntityManagerShortName = '\bEntityManager\s+em\b'
  EntityManagerFactoryShortName = '\bEntityManagerFactory\s+emf\b'
  TransactionShortName = '\bEntityTransaction\s+tx\b'
}

function Remove-CommentsAndStrings([string]$source) {
  $withoutBlockComments = [regex]::Replace(
      $source,
      '/\*[\s\S]*?\*/',
      { param($match) ($match.Value -replace '[^\r\n]', ' ') })
  $withoutLineComments = [regex]::Replace(
      $withoutBlockComments,
      '//[^\r\n]*',
      { param($match) ($match.Value -replace '[^\r\n]', ' ') })
  return [regex]::Replace(
      $withoutLineComments,
      '"(?:\\.|[^"\\])*"',
      { param($match) ($match.Value -replace '[^\r\n]', ' ') })
}

$violations = @()
$javaFiles = Get-ChildItem -LiteralPath $runtimeRoot -Filter '*.java' -Recurse -File
foreach ($javaFile in $javaFiles) {
  $source = Remove-CommentsAndStrings (Get-Content -LiteralPath $javaFile.FullName -Raw)
  foreach ($patternName in $forbiddenPatterns.Keys) {
    $match = [regex]::Match($source, $forbiddenPatterns[$patternName])
    if ($match.Success) {
      $lineNumber = ($source.Substring(0, $match.Index) -split "`r?`n").Count
      $relativePath = $javaFile.FullName.Substring((Resolve-Path $runtimeRoot).Path.Length + 1)
      $violations += "${relativePath}:${lineNumber}: $patternName"
    }
  }
}

if ($violations.Count -gt 0) {
  $violations | ForEach-Object { Write-Error $_ }
  throw 'Runtime style check failed.'
}

Write-Host ("Runtime style check passed: {0} Java files." -f $javaFiles.Count)
