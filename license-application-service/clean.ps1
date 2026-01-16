# clean.ps1

$CallerDir = Get-Location
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path

$TargetDir      = "target"
$NodeModulesDir = "node_modules"
$ServicesDir    = "src/main/webapp/app/services"
$TypesDir       = "src/main/webapp/types"

Set-Location $ScriptDir

$answer = Read-Host "Cleaning project. ALL NON-COMMITTED FILES WILL BE DELETED. Continue? (y/N)"
if ($answer -ne "y" -and $answer -ne "Y") {
    Write-Host "Aborted."
    Set-Location $CallerDir
    exit 1
}

Write-Host "Cleaning up build artifacts and temporary files..."

git clean -fd

docker compose -f ../docker/docker-compose.yml down --volumes --remove-orphans

if (Test-Path $TargetDir) {
    Remove-Item -Recurse -Force $TargetDir
}

if (Test-Path $NodeModulesDir) {
    Remove-Item -Recurse -Force $NodeModulesDir
}

if (Test-Path $ServicesDir) {
    Remove-Item -Recurse -Force $ServicesDir
}

if (Test-Path $TypesDir) {
    Get-ChildItem $TypesDir -File |
        Where-Object { $_.Name -ne "custom.d.ts" } |
        Remove-Item -Force
}

mvn clean

Write-Host "done!"

Set-Location $CallerDir