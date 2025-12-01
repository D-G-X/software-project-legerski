#!/bin/bash

read -p "Cleaning project. ALL NON-COMMITTED FILES WILL BE DELETED. Continue? (y/N) " answer
if [[ "$answer" == "y" || "$answer" == "Y" ]]; then
    echo "Cleaning up build artifacts and temporary files..."
else
    echo "Aborted."
    exit 1
fi

git clean -fd
if [ -d "node_modules" ]; then
  rm -rf node_modules
fi
npm install
mvn clean

echo "done!"
