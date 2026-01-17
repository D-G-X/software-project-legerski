# tests/e2e/test_register_api.py
import os
import uuid
import pytest

API_URL = os.getenv("API_URL", "http://localhost:8080").rstrip("/")


@pytest.fixture(scope="session")
def api_context(playwright):
    # Playwright-native HTTP client (kein requests nötig)
    ctx = playwright.request.new_context(
        base_url=API_URL,
        extra_http_headers={"Content-Type": "application/json"},
    )
    yield ctx
    ctx.dispose()


def _new_user_payload() -> dict:
    uid = uuid.uuid4().hex[:10]
    return {
        "firstname": "Test",
        "lastname": "User",
        "email": f"pw-{uid}@test.local",
        "password": "Test123!",
    }


def test_register_returns_201_and_resource(api_context):
    payload = _new_user_payload()

    r = api_context.post("/register", data=payload)
    assert r.status == 201, r.text()

    body = r.json()
    # Minimal-Checks (anpassen, wenn dein RegisterResource andere Felder hat)
    assert isinstance(body, dict)
    assert body.get("email") == payload["email"]
    assert body.get("firstname") == payload["firstname"]
    assert body.get("lastname") == payload["lastname"]
    # Optional: falls id existiert
    if "id" in body:
        assert body["id"]


def test_register_existing_user_returns_409(api_context):
    payload = _new_user_payload()

    r1 = api_context.post("/register", data=payload)
    assert r1.status == 201, r1.text()

    r2 = api_context.post("/register", data=payload)
    assert r2.status == 409, r2.text()


@pytest.mark.parametrize(
    "payload",
    [
        # missing email
        {"firstname": "Test", "lastname": "User", "password": "Test123!"},
        # invalid email
        {"firstname": "Test", "lastname": "User", "email": "not-an-email", "password": "Test123!"},
        # missing password
        {"firstname": "Test", "lastname": "User", "email": "x@test.local"},
        # empty fields
        {"firstname": "", "lastname": "", "email": "", "password": ""},
    ],
)
def test_register_invalid_payload_returns_400(api_context, payload):
    r = api_context.post("/register", data=payload)
    assert r.status == 400, r.text()