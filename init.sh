#!/bin/bash

CALLER_DIR="$(pwd)"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DOCKER_DIR="${SCRIPT_DIR}/docker"

cd "$SCRIPT_DIR" || exit 1

git lfs install

cp -n "${DOCKER_DIR}/.env.example" "${DOCKER_DIR}/.env"

cd "$CALLER_DIR" || exit 1
