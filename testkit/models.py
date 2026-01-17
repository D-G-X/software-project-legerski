from __future__ import annotations

from dataclasses import dataclass
from decimal import Decimal
from typing import Optional


@dataclass
class UserContext:
    email: str
    password: str
    firstname: str
    lastname: str

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
    price: Decimal
