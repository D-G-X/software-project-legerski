import base64
import json
import os


def env(name: str, default: str | None = None) -> str:
    val = os.getenv(name, default)
    if val is None or val == "":
        raise RuntimeError(f"Missing env var: {name}")
    return val


def decode_jwt(token: str) -> dict:
    payload = token.split(".")[1]
    payload += "=" * (-len(payload) % 4)  # padding
    return json.loads(base64.urlsafe_b64decode(payload))


def read_file(filepath: str, mode: str = "r"):
    with open(filepath, mode) as f:
        return f.read()
