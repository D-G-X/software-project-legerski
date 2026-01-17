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
