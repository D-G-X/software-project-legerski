import base64
import binascii
import json
from pathlib import Path
from typing import Any


def headers(access_token):
    return {"Authorization": f"Bearer {access_token}"} if access_token else {}


def safe_json(r):
    ct = (r.headers.get("Content-Type") or "").lower()
    if "application/json" not in ct:
        raise AssertionError(
            f"Expected JSON but got Content-Type={ct!r}, status={r.status_code}, body={r.text[:500]!r}"
        )
    try:
        return r.json()
    except Exception as e:
        raise AssertionError(
            f"Invalid JSON: status={r.status_code}, body={r.text[:500]!r}"
        ) from e


def decode_jwt(token: str) -> dict[str, Any]:
    if not token:
        raise ValueError("Empty token")

    token = token.strip()
    if token.lower().startswith("bearer "):
        token = token[7:].strip()

    parts = token.split(".")
    if len(parts) != 3:
        raise ValueError(f"Invalid JWT format (expected 3 parts, got {len(parts)})")

    payload_b64 = parts[1]

    # base64url padding
    payload_b64 += "=" * (-len(payload_b64) % 4)

    try:
        payload_bytes = base64.urlsafe_b64decode(payload_b64.encode("ascii"))
    except (binascii.Error, UnicodeEncodeError) as e:
        raise ValueError(f"Invalid base64 payload: {e}") from e

    try:
        return json.loads(payload_bytes.decode("utf-8"))
    except (UnicodeDecodeError, json.JSONDecodeError) as e:
        raise ValueError(f"Invalid JSON payload: {e}") from e


def read_file(filepath: str, mode: str = "rb"):
    p = Path(filepath)
    if not p.is_absolute():
        p = Path(__file__).resolve().parent / p # relative to this file
    return p.read_bytes() if "b" in mode else p.read_text(encoding="utf-8")
