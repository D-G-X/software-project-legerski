$CallerDir = Get-Location
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition

Set-Location $ScriptDir

git lfs install

if (-not (Test-Path ".env")) {
    Copy-Item ".env.example" ".env"
}

Set-Location $CallerDir
