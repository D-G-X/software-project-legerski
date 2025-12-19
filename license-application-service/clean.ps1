#!/usr/bin/env pwsh

$NODE_MODULES_DIR = "node_modules"
$SERVICES_DIR     = "src/main/webapp/app/services"
$TYPES_DIR        = "src/main/webapp/types"

$answer = Read-Host "Cleaning project. ALL NON-COMMITTED FILES WILL BE DELETED. Continue? (y/N)"
if ($answer -ne "y" -and $answer -ne "Y") {
    Write-Host "Aborted."
    exit 1
}

Write-Host "Cleaning up build artifacts and temporary files..."

git clean -fd

docker compose -f ./docker-compose.yml down --volumes --remove-orphans

if (Test-Path $NODE_MODULES_DIR) {
    Remove-Item $NODE_MODULES_DIR -Recurse -Force
}

if (Test-Path $SERVICES_DIR) {
    Remove-Item $SERVICES_DIR -Recurse -Force
}

if (Test-Path $TYPES_DIR) {
    Get-ChildItem $TYPES_DIR -File |
        Where-Object { $_.Name -ne "custom.d.ts" } |
        Remove-Item -Force
}

mvn clean

Write-Host "done!"
