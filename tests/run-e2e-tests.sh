#!/bin/bash

CALLER_DIR="$(pwd)"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TEST_DIR="$SCRIPT_DIR/e2e-tests"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

export PYTHONPATH="${REPO_ROOT}"

cd "$SCRIPT_DIR" || exit 1

if [ ! -d ".venv" ]; then
  echo "→ .venv not found. Run init.sh first..."
  cd "$CALLER_DIR" || exit 1
  exit 1
fi

source .venv/bin/activate

if [ ! -d "${TEST_DIR}" ]; then
  echo "❌ ${TEST_DIR} missing"
  deactivate
  cd "$CALLER_DIR" || exit 1
  exit 1
fi

cd ${TEST_DIR}

#pytest ${TEST_SCRIPT} --browser webkit
pytest ${TEST_SCRIPT} --browser chromium
#pytest ${TEST_SCRIPT} --browser firefox

deactivate

cd "$CALLER_DIR" || exit 1
