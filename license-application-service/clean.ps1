#!/usr/bin/env pwsh

$answer = Read-Host "Cleaning project. ALL NON-COMMITTED FILES WILL BE DELETED. Continue? (y/N)"

if ($answer -notin @("y", "Y")) {
    Write-Host "Aborted."
    exit 1
}

Write-Host "Cleaning up build artifacts and temporary files..."

# git clean
git clean -fd

# delete node_modules if present
if (Test-Path "node_modules") {
    Remove-Item "node_modules" -Recurse -Force
}

# npm install
npm install

# mvn clean
mvn clean

Write-Host "done!"
