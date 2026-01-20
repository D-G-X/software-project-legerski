import random
import time

from locust.clients import HttpSession, ResponseContextManager

from testkit.config import BACKEND_URL, DOCUMENT_STATUSES, TEST_PDF_PATH, LICENSE_MAP, PAYMENT_STATUSES, \
    APPLICATION_STATUSES, LICENSE_STATUSES
from testkit.models import ApplicationContext, PaymentContext, UserContext, \
    LicenseContext
from testkit.utils import headers, read_file, decode_jwt, safe_json


def api_register(client: HttpSession, user: UserContext) -> UserContext:
    r = client.post(
        f"{BACKEND_URL}/register",
        json={
            "firstname": user.firstname,
            "lastname": user.lastname,
            "email": user.email,
            "password": user.password,
        },
    )
    assert r.status_code == 201, f"Registration failed: {r.status_code} {r.text}"
    assert safe_json(r).get("user_id"), f"Registration returned no user id: {r.status_code} {r.text}"
    user.id = safe_json(r).get("user_id")
    return user


def api_login(client: HttpSession, user: UserContext) -> UserContext:
    r = client.post(
        f"{BACKEND_URL}/login",
        json={
            "email": user.email,
            "password": user.password,
        },
    )
    assert r.status_code == 200, f"Login failed: {r.status_code} {r.text}"
    assert "access_token" in safe_json(r), f"Login returned no access token: {r.status_code} {r.text}"

    user.access_token = safe_json(r).get("access_token")
    user.refresh_token = safe_json(r).get("refresh_token")
    user.expires_in = int(safe_json(r).get("expires_in"))
    user.refresh_expires_in = int(safe_json(r).get("refresh_expires_in"))
    user.token_type = safe_json(r).get("token_type")
    user.is_admin = bool(safe_json(r).get("is_admin"))

    assert user.id == decode_jwt(user.access_token)["sub"], f"Login returned invalid user id: {r.status_code} {r.text}"

    if user.access_token:
        client.headers.update({"Authorization": f"Bearer {user.access_token}"})

    return user


def api_create_application(client: HttpSession, application: ApplicationContext, user_id: str,
                           access_token: str) -> ApplicationContext:
    r = client.post(
        f"{BACKEND_URL}/applications",
        headers=headers(access_token),
        json={
            "user_id": user_id,
            "license_type": application.license_type,
            "cadastral_reference": application.cadastral_reference,
            "remarks": application.remarks,
        },
    )
    assert r.status_code == 201, f"Application request failed: {r.status_code} {r.text}"
    assert user_id == safe_json(r).get(
        "user_id"), f"Application request returned invalid user id: {r.status_code} {r.text}"
    assert safe_json(r).get(
        "application_status") in APPLICATION_STATUSES, f"Application request returned invalid application status: {r.status_code} {r.text}"

    application.id = int(safe_json(r).get("id"))
    application.applied_at = safe_json(r).get("applied_at")
    application.changed_at = safe_json(r).get("changed_at")
    application.application_status = safe_json(r).get("application_status")

    return application


def api_update_application(client: HttpSession, application: ApplicationContext,
                           new_cadastral_reference: str,
                           new_remarks: str, new_license_type: str, access_token: str) -> ApplicationContext:
    new_application_status = "DRAFT"  # Only DRAFT status can be set via API
    r = client.patch(
        f"{BACKEND_URL}/applications/{application.id}",
        headers=headers(access_token),
        params={"application_id": application.id},
        json={
            "application_status": new_application_status,
            "remarks": new_remarks,
            "cadastral_reference": new_cadastral_reference,
            "license_type": new_license_type,
        },
    )
    assert r.status_code == 200, f"Application update failed: {r.status_code} {r.text}"
    assert application.id == int(safe_json(r).get(
        "id")), f"Application update returned invalid application id: {r.status_code} {r.text}"
    assert isinstance(safe_json(r),
                      dict), f"Application update returned invalid data structure: {r.status_code} {r.text}"
    assert safe_json(r).get(
        "application_status") == new_application_status, f"Application update returned invalid application status: {r.status_code} {r.text}"
    assert safe_json(r).get(
        "cadastral_reference") == new_cadastral_reference, f"Application update returned invalid cadastral reference: {r.status_code} {r.text}"
    assert safe_json(r).get(
        "remarks") == new_remarks, f"Application update returned invalid remarks: {r.status_code} {r.text}"
    assert safe_json(r).get(
        "license_type") == new_license_type, f"Application update returned invalid license type: {r.status_code} {r.text}"
    assert safe_json(r).get("applied_at") == application.applied_at

    application.license_type = new_license_type
    application.cadastral_reference = new_cadastral_reference
    application.applied_at = safe_json(r).get("applied_at")
    application.changed_at = safe_json(r).get("changed_at")
    application.application_status = safe_json(r).get("application_status")
    application.remarks = new_remarks

    return application


def api_create_application_documents(client: HttpSession, application_id: int, access_token: str) -> None:
    file = read_file(filepath=TEST_PDF_PATH)
    r = client.post(
        f"{BACKEND_URL}/process-document",
        headers=headers(access_token),
        params={"application_id": application_id},
        files=[
            ("id_file", ("sample_id.pdf", file, "application/pdf")),
            ("proof_file", ("sample_proof.pdf", file, "application/pdf")),
        ],
    )
    assert r.status_code == 200, f"Document upload failed: {r.status_code} {r.text}"
    assert application_id == safe_json(r).get(
        "application_id"), f"Document upload returned invalid application id: {r.status_code} {r.text}"


def api_get_document_verification_status(client: HttpSession, application: ApplicationContext,
                                         access_token: str) -> ResponseContextManager:
    r = client.get(
        f"{BACKEND_URL}/applications/{application.id}/documents",
        headers=headers(access_token),
        params={"application_id": application.id},
    )
    assert r.status_code == 200, f"Document status fetch failed: {r.status_code} {r.text}"

    return r


def api_wait_for_document_verification(
        client: HttpSession,
        application: ApplicationContext,
        access_token: str,
        timeout_seconds: int = 120,
        initial_interval: float = 0.2,
        max_interval: float = 2.0,
) -> ApplicationContext:

    deadline = time.monotonic() + timeout_seconds
    interval = initial_interval
    r = None
    msg = None

    while time.monotonic() < deadline:
        r = api_get_document_verification_status(client, application, access_token)

        if safe_json(r).get("status") in ["VERIFIED", "REJECTED"]:
            assert application.id == safe_json(r).get(
                "application_id"), f"Document status fetch returned invalid application id: {r.status_code} {r.text}"
            assert safe_json(r).get(
                "status") in DOCUMENT_STATUSES, f"Document status fetch returned invalid status: {r.status_code} {r.text}"
            application.status = safe_json(r).get("status")
            application.rejection_reason = safe_json(r).get("rejection_reason")
            return application

        if safe_json(r).get("status") == ["PENDING"]:
            # backoff + jitter (to avoid thundering herd)
            jitter = random.uniform(-0.2, 0.2) * interval
            time.sleep(max(0.0, interval + jitter))
            interval = min(max_interval, interval * 1.5)

            msg = f"Document verification returned invalid status {application.id}: {safe_json(r).get('status')}"

    # Timeout
    if msg is None:
        msg = f"Document verification timed out for application {application.id}"

    raise RuntimeError(msg + f": {r.status_code} {r.text}")


def api_get_application_fee(client: HttpSession, application: ApplicationContext, payment: PaymentContext,
                            access_token: str) -> PaymentContext:
    r = client.get(
        f"{BACKEND_URL}/fees/{application.id}",
        headers=headers(access_token),
        params={"application_id": application.id},
    )
    assert r.status_code == 200, f"Fetching application fees failed: {r.status_code} {r.text}"
    assert application.id == safe_json(r).get(
        "application_id"), f"Fetching application fees returned invalid application id: {r.status_code} {r.text}"
    assert float(safe_json(r).get("fee_amount")) == LICENSE_MAP[
        application.license_type].price, f"Fetching application fees returned invalid amount: {safe_json(r).get('fee_amount')}"
    payment.amount = float(safe_json(r).get("fee_amount"))

    return payment


def api_create_payment(client: HttpSession, application_id: int, payment: PaymentContext,
                       access_token: str) -> PaymentContext:
    r = client.post(
        f"{BACKEND_URL}/applications/{application_id}/payments",
        headers=headers(access_token),
        params={"application_id": application_id},
        json={
            "application_id": application_id,
            "name": payment.name,
            "iban": payment.iban,
            "bic": payment.bic,
        },
    )
    assert r.status_code == 201, f"Payment creation failed: {r.status_code} {r.text}"
    assert application_id == safe_json(r).get(
        "application_id"), f"Payment creation returned invalid application id: {r.status_code} {r.text}"
    assert payment.amount == float(safe_json(r).get(
        "amount")), f"Payment creation returned invalid amount: {r.status_code} {r.text}"
    assert safe_json(r).get(
        "payment_status") in PAYMENT_STATUSES, f"Payment create returned invalid status: {r.status_code} {r.text}"
    payment.id = int(safe_json(r).get("id"))
    payment.application_id = int(safe_json(r).get("application_id"))
    payment.amount = float(safe_json(r).get("amount"))
    payment.payment_date = safe_json(r).get("payment_date")
    payment.payment_status = safe_json(r).get("payment_status")

    return payment


def api_list_applications(client: HttpSession, user_id: str, access_token: str) -> list[ApplicationContext]:
    r = client.get(
        f"{BACKEND_URL}/applications",
        headers=headers(access_token),
        params={"user_id": user_id},
    )
    assert r.status_code == 200, f"Fetching applications failed: {r.status_code} {r.text}"
    assert isinstance(safe_json(r),
                      list), f"Fetching applications returned invalid data structure: {r.status_code} {r.text}"

    applications: list[ApplicationContext] = []
    for item in safe_json(r):
        assert item.get(
            "user_id") == user_id, f"Fetching applications returned invalid user id: {r.status_code} {r.text}"
        assert item.get(
            "application_status") in APPLICATION_STATUSES, f"Fetching applications returned invalid application status: {r.status_code} {r.text}"
        applications.append(
            ApplicationContext(
                id=int(item["id"]),
                user_id=item["user_id"],
                license_type=item["license_type"],
                cadastral_reference=item["cadastral_reference"],
                applied_at=item["applied_at"],
                changed_at=item["changed_at"],
                application_status=item["application_status"],
                remarks=item["remarks"],
            )
        )
    return applications if applications else None


def api_list_applications_by_id(client: HttpSession, application: ApplicationContext, user_id: str,
                                access_token: str) -> ApplicationContext:
    r = client.get(
        f"{BACKEND_URL}/applications/{application.id}",
        headers=headers(access_token),
        params={"user_id": user_id}
    )
    assert r.status_code == 200, f"Fetching applications failed: {r.status_code} {r.text}"
    assert safe_json(r).get(
        "id") == application.id, f"Fetching application returned invalid user id: {r.status_code} {r.text}"
    assert safe_json(r).get(
        "application_status") in APPLICATION_STATUSES, f"Fetching applications returned invalid application status: {r.status_code} {r.text}"

    application.id = int(safe_json(r).get("id"))
    application.user_id = safe_json(r).get("user_id")
    application.license_type = safe_json(r).get("license_type")
    application.cadastral_reference = safe_json(r).get("cadastral_reference")
    application.applied_at = safe_json(r).get("applied_at")
    application.changed_at = safe_json(r).get("changed_at")
    application.application_status = safe_json(r).get("application_status")
    application.remarks = safe_json(r).get("remarks")

    return application


def api_get_user_by_id(client: HttpSession, user: UserContext, access_token: str) -> UserContext:
    r = client.get(
        f"{BACKEND_URL}/users/{user.id}",
        headers=headers(access_token),
        params={"user_id": user.id},
    )
    assert r.status_code == 200, f"Fetching user by id failed: {r.status_code} {r.text}"
    assert safe_json(r).get("id") == user.id, f"Fetching user by id returned invalid user id: {r.status_code} {r.text}"

    user.id = safe_json(r).get("id")
    user.username = safe_json(r).get("username")
    user.email = safe_json(r).get("email")
    user.firstName = safe_json(r).get("firstName")
    user.lastName = safe_json(r).get("lastName")
    user.enabled = bool(safe_json(r).get("enabled"))
    user.email_verified = bool(safe_json(r).get("emailVerified"))
    user.created_timestamp = safe_json(r).get("createdTimestamp")

    return user


def api_list_licenses(client: HttpSession, user_id: str, access_token: str) -> LicenseContext | None:
    r = client.get(
        f"{BACKEND_URL}/licenses",
        headers=headers(access_token),
        params={"user_id": user_id},
    )
    assert r.status_code == 200 or r.status_code == 404, f"Fetching licenses failed: {r.status_code} {r.text}"
    if r.status_code == 404:
        print("INFO: Fetching licenses returned no matches:", user_id)
        return None
    assert isinstance(safe_json(r),
                      list), f"Fetching licenses returned invalid data structure: {r.status_code} {r.text}"

    licenses: list[LicenseContext] = []
    for item in safe_json(r):
        assert item.get("user_id") == user_id, f"Fetching licenses returned invalid user id: {r.status_code} {r.text}"
        assert item.get(
            "license_status") in LICENSE_STATUSES, f"Fetching license returned invalid license status: {r.status_code} {r.text}"
        licenses.append(
            LicenseContext(
                id=int(item["id"]),
                user_id=item["user_id"],
                application_id=int(item["application_id"]),
                license_type=item["license_type"],
                license_status=item["license_status"],
                issued_at=item["issued_at"],
                expires_at=item["expires_at"],
                cadastral_reference=item["cadastral_reference"],
            )
        )
    return licenses[len(licenses) - 1] if licenses else None


def api_get_license_by_id(client: HttpSession, license: LicenseContext, access_token: str) -> LicenseContext:
    if license is None:
        return None

    r = client.get(
        f"{BACKEND_URL}/licenses/{license.id}",
        headers=headers(access_token),
        params={"license_id": license.id},
    )
    assert r.status_code == 200, f"Fetching license by id failed: {r.status_code} {r.text}"
    assert safe_json(r).get(
        "id") == license.id, f"Fetching license by id returned invalid license id: {r.status_code} {r.text}"
    assert safe_json(r).get(
        "license_status") in LICENSE_STATUSES, f"Fetching license returned invalid license status: {r.status_code} {r.text}"

    license.user_id = safe_json(r).get("user_id")
    license.application_id = int(safe_json(r).get("application_id"))
    license.license_type = safe_json(r).get("license_type")
    license.license_status = safe_json(r).get("license_status")
    license.issued_at = safe_json(r).get("issued_at")
    license.expires_at = safe_json(r).get("expires_at")
    license.cadastral_reference = safe_json(r).get("cadastral_reference")

    return license


def api_list_payments(client: HttpSession, application_id: int, access_token: str) -> PaymentContext:
    r = client.get(
        f"{BACKEND_URL}/applications/{application_id}/payments",
        headers=headers(access_token),
        params={"application_id": application_id},
    )
    assert r.status_code == 200, f"Fetching payments failed: {r.status_code} {r.text}"
    assert isinstance(safe_json(r),
                      list), f"Fetching payments returned invalid data structure: {r.status_code} {r.text}"

    payments: list[PaymentContext] = []
    for item in safe_json(r):
        assert item.get(
            "application_id") == application_id, f"Fetching payments returned invalid application id: {r.status_code} {r.text}"
        assert item.get(
            "payment_status") in PAYMENT_STATUSES, f"Fetching payment returned invalid payment status: {r.status_code} {r.text}"
        payments.append(
            PaymentContext(
                id=int(item["id"]),
                application_id=int(item["application_id"]),
                amount=float(item["amount"]),
                name=item["name"],
                iban=item["iban"],
                bic=item["bic"],
                payment_date=item["payment_date"],
                payment_status=item["payment_status"],
            )
        )
    return payments[len(payments) - 1] if payments else None


def api_logout(client: HttpSession, user_or_admin: UserContext) -> UserContext:
    user_or_admin.access_token = ""
    user_or_admin.refresh_token = ""
    user_or_admin.expires_in = 0
    user_or_admin.refresh_expires_in = 0
    user_or_admin.token_type = ""
    user_or_admin.is_admin = False

    client.headers.pop("Authorization", None)

    return user_or_admin
