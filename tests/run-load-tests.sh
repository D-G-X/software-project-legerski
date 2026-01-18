#!/bin/bash

set -a
source .env
set +a

CALLER_DIR="$(pwd)"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TEST_DIR="$SCRIPT_DIR/load-tests"
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

locust -f ${TEST_DIR}/locustfile.py \
  --users ${NUM_USERS} \
  --spawn-rate ${SPAWN_RATE} \
  --run-time ${RUNTIME}m \
  --processes ${THREADS} \
  --loglevel DEBUG \
  -H ${BACKEND_URL} \

  deactivate

cd "$CALLER_DIR" || exit 1
