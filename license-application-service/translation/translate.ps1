# setup-venv.ps1

$CallerDir = Get-Location
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path

Set-Location $ScriptDir

if (-not (Test-Path ".venv")) {
    Write-Host "→ .venv not found. Creating new virtual environment..."
    python3 -m venv .venv
}

# Activate virtual environment
& .\.venv\Scripts\Activate.ps1

python -m pip install --upgrade pip

if (-not (Test-Path "requirements.txt")) {
    Write-Host "❌ requirements.txt missing"
    Set-Location $CallerDir
    exit 1
}

pip install -r requirements.txt

python3 .\translator.py

# Deactivate virtual environment
deactivate

Set-Location $CallerDir
