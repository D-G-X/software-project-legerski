import random
import string
import uuid
from datetime import datetime, timedelta
from datetime import timezone

from testkit.config import LICENSE_TYPES
from testkit.models import AdminContext, UserContext, ApplicationContext, PaymentContext, BallotPeriodContext


def _unique_email(prefix: str = "pw") -> str:
    return f"{prefix}-{uuid.uuid4().hex}@test.local"


def _date_str(days_offset: int = 0, hours_offset: int = 0, minutes_offset: int = 0, seconds_offset: int = 0, ) -> str:
    target_date = datetime.now(timezone.utc) + timedelta(days=days_offset,
                                                         hours=hours_offset,
                                                         minutes=minutes_offset,
                                                         seconds=seconds_offset)
    return target_date.strftime("%Y-%m-%dT%H:%M:%SZ")


def new_user() -> UserContext:
    return UserContext(
        email=_unique_email(prefix="user"),
        password="Test123!",
        firstname="Test",
        lastname="User",
    )


def new_admin() -> AdminContext:
    return AdminContext(
        email="admin@xx.xx",
        password="admin",
        firstname="admin",
        lastname="admin",
    )


def new_application() -> ApplicationContext:
    return ApplicationContext(
        license_type=random.choice(LICENSE_TYPES)["name"],
        cadastral_reference=''.join(random.choices(string.digits, k=20)),
        remarks="Test license application"
    )


def new_payment() -> PaymentContext:
    return PaymentContext(
        name="Test User",
        iban="DE" + ''.join(random.choices(string.digits, k=20)),
        bic=(''.join(random.choices(string.ascii_uppercase, k=4)) + "DE"
             + ''.join(random.choices(string.digits + string.ascii_uppercase, k=2))),
    )


def new_ballot_period_running() -> BallotPeriodContext:
    return BallotPeriodContext(
        start_date=_date_str(days_offset=-30),
        end_date=_date_str(days_offset=30)
    )


def new_ballot_period_upcoming() -> BallotPeriodContext:
    return BallotPeriodContext(
        start_date=_date_str(days_offset=1),
        end_date=_date_str(days_offset=61)
    )


def new_ballot_period_ending_now() -> BallotPeriodContext:
    return BallotPeriodContext(
        start_date=_date_str(days_offset=-60),
        end_date=_date_str(seconds_offset=10)
    )


def new_ballot_period_ended() -> BallotPeriodContext:
    return BallotPeriodContext(
        start_date=_date_str(days_offset=-61),
        end_date=_date_str(days_offset=-1)
    )
