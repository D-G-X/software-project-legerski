import os
from decimal import Decimal

from testkit.models import LicenseType


def _env(name: str, default: str | None = None) -> str:
    val = os.getenv(name, default)
    if val is None or val == "":
        raise RuntimeError(f"Missing env var: {name}")
    return val


FRONTEND_URL = _env("FRONTEND_URL", "http://localhost:3000")
BACKEND_URL = _env("BACKEND_URL", "http://localhost:8080")
TEST_PDF_PATH = _env("TEST_PDF_PATH", "tests/sample.pdf")

ACCESS_TOKEN_KEY = "accessToken"
REFRESH_TOKEN_KEY = "refreshToken"
ACCESS_EXP_KEY = "accessTokenExpiry"
REFRESH_EXP_KEY = "refreshTokenExpiry"
TOKEN_TYPE_KEY = "tokenType"
ROLE_KEY = "role"

LICENSE_TYPES = [
    "ETV",
    "ETVPL",
    "ETV60",
]

LICENSE_MAP: dict[str, LicenseType] = {
    lt.name: lt for lt in
    (
        LicenseType("ETV", 3500.00),
        LicenseType("ETVPL", 875.00),
        LicenseType("ETV60", 290.00),
    )
}

APPLICATION_STATUSES = [
    "DRAFT",
    "DOCUMENTS_SUBMITTED",
    "VERIFICATION_PENDING",
    "AWAITING_PAYMENT",
    "PAYMENT_RECEIVED",
    "SUBMITTED",
    "IN_BALLOT",
    "SELECTED",
    "NOT_SELECTED",
    "UNDER_REVIEW",
    "APPROVED",
    "REJECTED",
    "EXPIRED",
    "CANCELLED",
]

DOCUMENT_STATUSES = [
    "PENDING",
    "VERIFIED",
    "REJECTED",
]

PAYMENT_STATUSES = [
    "UNPAID",
    "PAID",
]

LICENSE_STATUSES = [
    "ACTIVE",
    "SUSPENDED",
    "EXPIRED",
]

NOTIFICATION_WAYS = [
    "EMAIL",
    "SMS",
    "NONE",
]
