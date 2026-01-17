from __future__ import annotations

from dataclasses import dataclass
from decimal import Decimal


@dataclass
class UserContext:
    id: str
    username: str
    email: str
    password: str
    firstname: str
    lastname: str
    enabled: bool
    email_verified: bool
    created_timestamp: int
    access_token: str
    refresh_token: str
    expires_in: int
    refresh_expires_in: int
    token_type: str
    is_admin: bool
    notification_way: str
    application_updates_notification: bool
    license_renewal_notification: bool


@dataclass
class NotificationContext:
    id: str
    application_id: int
    user_id: str
    date: str
    message: str
    is_read: bool


@dataclass
class AdminContext:
    id: str
    email: str
    password: str
    firstname: str
    lastname: str
    access_token: str
    refresh_token: str
    expires_in: int
    refresh_expires_in: int
    token_type: str
    is_admin: bool


@dataclass
class ApplicationContext:
    id: int
    user_id: str
    license_type: str
    cadastral_reference: str
    applied_at: str
    changed_at: str
    application_status: str
    remarks: str
    document_status: str
    rejection_reason: str


@dataclass
class PaymentContext:
    id: int
    application_id: int
    amount: float
    name: str
    iban: str
    bic: str
    payment_date: str
    payment_status: str


@dataclass
class LicenseContext:
    id: int
    user_id: str
    application_id: int
    license_type: str
    license_status: str
    issued_at: str
    expires_at: str
    cadastral_reference: str


@dataclass
class BallotPeriodContext:
    ballot_period_id: int
    start_date: str
    end_date: str
    total_applications: int


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
    price: Decimal
