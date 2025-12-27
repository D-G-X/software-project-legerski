#!/bin/bash

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
  exit 1
fi

pip install -r requirements.txt

python3 ./translator.py

deactivate
