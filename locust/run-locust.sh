#!/bin/bash

NUM_USERS=1
SPAWN_RATE=1 # users per second
RUNTIME=10 # in minutes
THREADS=1 # ~ number of CPU cores

CALLER_DIR="$(pwd)"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

cd "$SCRIPT_DIR" || exit 1

if [ ! -d ".venv" ]; then
  echo "→ .venv not found. Creating new virtual environment..."
  python3 -m venv .venv
fi

source .venv/bin/activate

python -m pip install --upgrade pip

if [ ! -f "requirements.txt" ]; then
  echo "❌ requirements.txt missing"
  cd "$CALLER_DIR" || exit 1
  exit 1
fi

pip install -r requirements.txt

locust -f locustfile.py \
  --users $NUM_USERS \
  --spawn-rate $SPAWN_RATE \
  --run-time ${RUNTIME}m \
  --processes $THREADS \
  --loglevel DEBUG \
  -H http://localhost:8080 \

deactivate

cd "$CALLER_DIR" || exit 1
