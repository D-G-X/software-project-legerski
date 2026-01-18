from dataclasses import dataclass
from typing import List


@dataclass(frozen=True)
class Scenario:
    name: str
    steps: List[str]


REGISTER_LOGIN_LOGOUT = Scenario(
    name="register_login_logout",
    steps=["register", "login", "logout"]
)

REGISTER_FORGOT_PASSWORD = Scenario(
    name="register_forgot_password",
    steps=["register", "request_reset_password"]  # TODO: add "reset_password" step implementation
)

REGISTER_LOGIN_REFRESH_TOKEN = Scenario(
    name="register_login_refresh_token",
    steps=["register", "login", "refresh_login", "logout"]
)

ADMIN_LOGIN_CREATE_BALLOT = Scenario(
    name="admin_login_create_ballot",
    steps=["admin_login", "create_ballot"]
)
