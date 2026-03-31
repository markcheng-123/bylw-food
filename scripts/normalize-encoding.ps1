param(
  [string]$Root = ".",
  [switch]$WhatIf
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$textExt = @(
  ".js",".ts",".tsx",".jsx",".vue",".json",".css",".scss",".less",".html",".md",
  ".yml",".yaml",".xml",".properties",".sql",".java",".kt",".txt",".env",".sh",
  ".ps1",".bat",".cmd"
)

$excludeDirs = @(
  ".git",
  "node_modules",
  "dist",
  "target",
  ".vite",
  "uploads",
  ".idea",
  ".vscode"
)

function Convert-ToUtf8NoBom([string]$path) {
  $bytes = [System.IO.File]::ReadAllBytes($path)
  if ($bytes.Length -eq 0) { return $false }

  $utf8Strict = New-Object System.Text.UTF8Encoding($false, $true)
  $content = $null
  $decodedBy = ""

  try {
    $content = $utf8Strict.GetString($bytes)
    $decodedBy = "utf8"
  } catch {
    $gb18030 = [System.Text.Encoding]::GetEncoding("GB18030")
    $content = $gb18030.GetString($bytes)
    $decodedBy = "gb18030"
  }

  # Normalize line endings to LF and guarantee trailing newline.
  $content = $content -replace "`r`n", "`n"
  $content = $content -replace "`r", "`n"
  if (-not $content.EndsWith("`n")) {
    $content += "`n"
  }

  if (-not $WhatIf) {
    $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
    [System.IO.File]::WriteAllText($path, $content, $utf8NoBom)
  }

  return $decodedBy -ne "utf8"
}

$changed = 0
$recoveredFromGb = 0
$recoveredFiles = New-Object System.Collections.Generic.List[string]

Get-ChildItem -Path $Root -Recurse -File |
  Where-Object {
    $parts = $_.FullName.Split([System.IO.Path]::DirectorySeparatorChar)
    -not ($parts | Where-Object { $excludeDirs -contains $_ })
  } |
  Where-Object { $textExt -contains $_.Extension.ToLowerInvariant() } |
  ForEach-Object {
    $usedGb = Convert-ToUtf8NoBom $_.FullName
    $changed++
    if ($usedGb) {
      $recoveredFromGb++
      $recoveredFiles.Add($_.FullName) | Out-Null
    }
  }

Write-Host "Normalized files: $changed"
Write-Host "Recovered from GB18030 decode: $recoveredFromGb"
if ($recoveredFiles.Count -gt 0) {
  Write-Host "Recovered files:"
  $recoveredFiles | ForEach-Object { Write-Host " - $_" }
}
