#!/bin/bash

CALLER_DIR="$(pwd)"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

TARGET_DIR="target"
NODE_MODULES_DIR="node_modules"
SERVICES_DIR="src/main/webapp/app/services"
TYPES_DIR="src/main/webapp/types"

cd "$SCRIPT_DIR" || exit 1

read -p "Cleaning project. ALL NON-COMMITTED FILES WILL BE DELETED. Continue? (y/N) " answer
if [[ "$answer" == "y" || "$answer" == "Y" ]]; then
    echo "Cleaning up build artifacts and temporary files..."
else
    echo "Aborted."
    cd "$CALLER_DIR" || exit 1
    exit 1
fi

git clean -fd

docker compose -f ../docker/docker-compose.yml down --volumes --remove-orphans

if [ -d TARGET_DIR ]; then
  rm -rf TARGET_DIR
fi

if [ -d $NODE_MODULES_DIR ]; then
  rm -rf $NODE_MODULES_DIR
fi

if [ -d $SERVICES_DIR ]; then
  rm -rf $SERVICES_DIR
fi

if [ -d $TYPES_DIR ]; then
  find "$TYPES_DIR" -maxdepth 1 -type f ! -name 'custom.d.ts' -delete
fi

mvn clean

echo "done!"

cd "$CALLER_DIR" || exit 1
