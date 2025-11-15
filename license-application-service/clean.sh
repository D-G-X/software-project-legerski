#!/bin/sh

# Clean up build artifacts and temporary files
echo "Cleaning up build artifacts and temporary files..."

if [ -d "node_modules" ]; then
  rm -rf node_modules
fi
npm install
mvn clean

echo "done!"
