@echo off
echo Cleaning up build artifacts and temporary files...

if exist node_modules (
    rmdir /s /q node_modules
)
call npm install
call mvn clean

echo done!
