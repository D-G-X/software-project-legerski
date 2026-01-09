# setup-venv.ps1

$NUM_USERS=1
$SPAWN_RATE=1 # users per second
$RUNTIME=10 # in minutes
$THREADS=1 # ~ number of CPU cores

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

locust -f locustfile.py \
  --users $NUM_USERS \
  --spawn-rate $SPAWN_RATE \
  --run-time ${RUNTIME}m \
  --processes $THREADS \
  -H http://localhost:3000

# Deactivate virtual environment
deactivate

Set-Location $CallerDir
