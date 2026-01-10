#!/bin/bash

CALLER_DIR="$(pwd)"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TEST_DIR="$SCRIPT_DIR/e2e-tests"

cd "$SCRIPT_DIR" || exit 1

if [ ! -d ".venv" ]; then
  echo "→ .venv not found. Run init.sh first..."
  cd "$CALLER_DIR" || exit 1
  exit 1
fi

pytest ${TEST_DIR}/playwright_tests

cd "$CALLER_DIR" || exit 1
