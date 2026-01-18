import base64
import json


def headers(access_token):
    return {"Authorization": f"Bearer {access_token}"} if access_token else {}


def decode_jwt(token: str) -> dict:
    payload = token.split(".")[1]
    payload += "=" * (-len(payload) % 4)
    return json.loads(base64.urlsafe_b64decode(payload))


def read_file(filepath: str, mode: str = "r"):
    with open(filepath, mode) as f:
        return f.read()