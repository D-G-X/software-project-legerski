from __future__ import annotations

from dataclasses import dataclass
from typing import Callable
from typing import Optional

from playwright.sync_api import Page


@dataclass(frozen=True)
class Scenario:
    name: str
    steps: list[Step]


@dataclass
class RunContext:
    user: Optional["UserContext"] = None  # Admin is user if no user is needed
    admin: Optional["UserContext"] = None
    notification: Optional["NotificationContext"] = None
    application: Optional["ApplicationContext"] = None
    payment: Optional["PaymentContext"] = None
    license: Optional["LicenseContext"] = None
    ballot: Optional["BallotPeriodContext"] = None
    # consent: Optional["ConsentContext"] = None


Step = Callable[[Page, RunContext], None]


@dataclass
class UserContext:
    email: str
    password: str
    firstname: str
    lastname: str
    language: str = "en"

    id: Optional[str] = None
    username: Optional[str] = None
    enabled: bool = False
    email_verified: bool = False
    created_timestamp: Optional[str] = None
    access_token: Optional[str] = None
    refresh_token: Optional[str] = None
    expires_in: Optional[int] = None
    refresh_expires_in: Optional[int] = None
    token_type: Optional[str] = None
    is_admin: bool = False
    notification_way: Optional[str] = None
    application_updates_notification: Optional[str] = None
    license_renewal_notification: Optional[str] = None


@dataclass
class NotificationContext:
    id: str
    application_id: int
    user_id: str
    date: str
    message: str
    is_read: bool


@dataclass
class ApplicationContext:
    license_type: str
    cadastral_reference: str
    remarks: str

    id: Optional[int] = None
    user_id: Optional[str] = None
    applied_at: Optional[str] = None
    changed_at: Optional[str] = None
    application_status: Optional[str] = None
    document_status: Optional[str] = None
    rejection_reason: Optional[str] = None


@dataclass
class PaymentContext:
    name: str
    iban: str
    bic: str

    id: Optional[int] = None
    application_id: Optional[int] = None
    amount: Optional[float] = None
    payment_date: Optional[str] = None
    payment_status: Optional[str] = None


@dataclass
class LicenseContext:
    license_type: str
    cadastral_reference: str

    id: Optional[int] = None
    user_id: Optional[str] = None
    application_id: Optional[int] = None
    license_status: Optional[str] = None
    issued_at: Optional[str] = None
    expires_at: Optional[str] = None


@dataclass
class BallotPeriodContext:
    start_date: str
    end_date: str

    ballot_period_id: Optional[int] = None
    total_applications: Optional[int] = None


'''
@dataclass
class ConsentContext:
    id: str
    user_id: str
    purpose: str
    granted: bool
    granted_at: str
    withdrawn_at: str
'''


@dataclass(frozen=True)
class LicenseType:
    name: str
    price: float
