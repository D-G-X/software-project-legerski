import time
from typing import Optional, List

from locust.clients import HttpSession, ResponseContextManager

from testkit.config import (
    BACKEND_URL,
    DOCUMENT_STATUSES,
    TEST_PDF_PATH,
    LICENSE_MAP,
    PAYMENT_STATUSES,
    APPLICATION_STATUSES,
    LICENSE_STATUSES,
)
from testkit.models import ApplicationContext, PaymentContext, UserContext, LicenseContext
from testkit.utils import headers, read_file, decode_jwt, safe_json


def _fail(r: ResponseContextManager, msg: str) -> None:
    r.failure(msg)


def api_register(client: HttpSession, user: UserContext) -> UserContext:
    with client.post(
            f"{BACKEND_URL}/register",
            json={
                "firstname": user.firstname,
                "lastname": user.lastname,
                "email": user.email,
                "password": user.password,
            },
            catch_response=True,
            name="POST /register",
    ) as r:
        if r.status_code != 201:
            _fail(r, f"Registration failed: {r.status_code} {r.text}")
            return user

        uid = safe_json(r).get("user_id")
        if not uid:
            _fail(r, f"Registration returned no user id: {r.status_code} {r.text}")
            return user

        user.id = uid
        r.success()
        return user


def api_login(client: HttpSession, user: UserContext) -> UserContext:
    with client.post(
            f"{BACKEND_URL}/login",
            json={"email": user.email, "password": user.password},
            catch_response=True,
            name="POST /login",
    ) as r:
        if r.status_code != 200:
            _fail(r, f"Login failed: {r.status_code} {r.text}")
            return user

        body = safe_json(r)
        if "access_token" not in body:
            _fail(r, f"Login returned no access token: {r.status_code} {r.text}")
            return user

        user.access_token = body.get("access_token")
        user.refresh_token = body.get("refresh_token")
        try:
            user.expires_in = int(body.get("expires_in"))
        except Exception:
            user.expires_in = 0
        try:
            user.refresh_expires_in = int(body.get("refresh_expires_in"))
        except Exception:
            user.refresh_expires_in = 0
        user.token_type = body.get("token_type")
        user.is_admin = bool(body.get("is_admin"))

        try:
            sub = decode_jwt(user.access_token).get("sub")
            if user.id and sub and user.id != sub:
                _fail(r, f"Login returned invalid user id: jwt.sub={sub} expected={user.id}")
                return user
        except Exception as e:
            _fail(r, f"Login jwt decode failed: {e}")
            return user

        if user.access_token:
            client.headers.update({"Authorization": f"Bearer {user.access_token}"})

        r.success()
        return user


def api_create_application(
        client: HttpSession,
        application: ApplicationContext,
        user_id: str,
        access_token: str,
) -> ApplicationContext:
    with client.post(
            f"{BACKEND_URL}/applications",
            headers=headers(access_token),
            json={
                "user_id": user_id,
                "license_type": application.license_type,
                "cadastral_reference": application.cadastral_reference,
                "remarks": application.remarks,
            },
            catch_response=True,
            name="POST /applications",
    ) as r:
        if r.status_code != 201:
            _fail(r, f"Application request failed: {r.status_code} {r.text}")
            return application

        body = safe_json(r)
        if body.get("user_id") != user_id:
            _fail(r, f"Application request returned invalid user id: {r.status_code} {r.text}")
            return application

        if body.get("application_status") not in APPLICATION_STATUSES:
            _fail(r, f"Application request returned invalid application status: {r.status_code} {r.text}")
            return application

        try:
            application.id = int(body.get("id"))
        except Exception:
            _fail(r, f"Application request returned invalid id: {r.status_code} {r.text}")
            return application

        application.applied_at = body.get("applied_at")
        application.changed_at = body.get("changed_at")
        application.application_status = body.get("application_status")

        r.success()
        return application


def api_update_application(
        client: HttpSession,
        application: ApplicationContext,
        new_cadastral_reference: str,
        new_remarks: str,
        new_license_type: str,
        access_token: str,
) -> ApplicationContext:
    new_application_status = "DRAFT"

    with client.patch(
            f"{BACKEND_URL}/applications/{application.id}",
            headers=headers(access_token),
            params={"application_id": application.id},
            json={
                "application_status": new_application_status,
                "remarks": new_remarks,
                "cadastral_reference": new_cadastral_reference,
                "license_type": new_license_type,
            },
            catch_response=True,
            name="PATCH /applications/:id",
    ) as r:
        if r.status_code != 200:
            _fail(r, f"Application update failed: {r.status_code} {r.text}")
            return application

        body = safe_json(r)
        if not isinstance(body, dict):
            _fail(r, f"Application update returned invalid data structure: {r.status_code} {r.text}")
            return application

        try:
            rid = int(body.get("id"))
        except Exception:
            _fail(r, f"Application update returned invalid application id: {r.status_code} {r.text}")
            return application

        if rid != application.id:
            _fail(r, f"Application update returned mismatching id: got={rid} expected={application.id}")
            return application

        if body.get("application_status") != new_application_status:
            _fail(r, f"Application update returned invalid application status: {r.status_code} {r.text}")
            return application

        if body.get("cadastral_reference") != new_cadastral_reference:
            _fail(r, f"Application update returned invalid cadastral reference: {r.status_code} {r.text}")
            return application

        if body.get("remarks") != new_remarks:
            _fail(r, f"Application update returned invalid remarks: {r.status_code} {r.text}")
            return application

        if body.get("license_type") != new_license_type:
            _fail(r, f"Application update returned invalid license type: {r.status_code} {r.text}")
            return application

        if body.get("applied_at") != application.applied_at:
            _fail(r, f"Application update changed applied_at unexpectedly: {r.status_code} {r.text}")
            return application

        application.license_type = new_license_type
        application.cadastral_reference = new_cadastral_reference
        application.applied_at = body.get("applied_at")
        application.changed_at = body.get("changed_at")
        application.application_status = body.get("application_status")
        application.remarks = new_remarks

        r.success()
        return application


def api_create_application_documents(client: HttpSession, application_id: int, access_token: str) -> None:
    file = read_file(filepath=TEST_PDF_PATH)
    with client.post(
            f"{BACKEND_URL}/process-document",
            headers=headers(access_token),
            params={"application_id": application_id},
            files=[
                ("id_file", ("sample_id.pdf", file, "application/pdf")),
                ("proof_file", ("sample_proof.pdf", file, "application/pdf")),
            ],
            catch_response=True,
            name="POST /process-document",
    ) as r:
        if r.status_code != 200:
            _fail(r, f"Document upload failed: {r.status_code} {r.text}")
            return

        if safe_json(r).get("application_id") != application_id:
            _fail(r, f"Document upload returned invalid application id: {r.status_code} {r.text}")
            return

        r.success()


def api_get_document_verification_status(
        client: HttpSession,
        application: ApplicationContext,
        access_token: str,
) -> ResponseContextManager:
    with client.get(
            f"{BACKEND_URL}/applications/{application.id}/documents",
            headers=headers(access_token),
            params={"application_id": application.id},
            catch_response=True,
            name="GET /applications/:id/documents",
    ) as r:
        if r.status_code != 200:
            _fail(r, f"Document status fetch failed: {r.status_code} {r.text}")
            return r
        r.success()
        return r


def api_wait_for_document_verification(
        client: HttpSession,
        application: ApplicationContext,
        access_token: str,
        timeout_seconds: int = 120,
) -> ApplicationContext:
    deadline = time.monotonic() + timeout_seconds
    last_r: Optional[ResponseContextManager] = None

    while time.monotonic() < deadline:
        last_r = api_get_document_verification_status(client, application, access_token)
        status = safe_json(last_r).get("status")

        if status in ("VERIFIED", "REJECTED"):
            app_id = safe_json(last_r).get("application_id")
            if app_id != application.id:
                last_r.failure(
                    f"Document status fetch returned invalid application id: expected={application.id} got={app_id}"
                )
                return application

            if status not in DOCUMENT_STATUSES:
                last_r.failure(f"Document status fetch returned invalid status: {status}")
                return application

            application.status = status
            application.rejection_reason = safe_json(last_r).get("rejection_reason")
            return application

        if status == "PENDING":
            time.sleep(0.5)
            continue

        if last_r is not None:
            last_r.failure(f"Document verification returned invalid status {application.id}: {status}")
        return application

    if last_r is not None:
        last_r.failure(f"Document verification timed out for application {application.id}")
    return application


def api_get_application_fee(
        client: HttpSession,
        application: ApplicationContext,
        payment: PaymentContext,
        access_token: str,
) -> PaymentContext:
    with client.get(
            f"{BACKEND_URL}/fees/{application.id}",
            headers=headers(access_token),
            params={"application_id": application.id},
            catch_response=True,
            name="GET /fees/:application_id",
    ) as r:
        if r.status_code != 200:
            _fail(r, f"Fetching application fees failed: {r.status_code} {r.text}")
            return payment

        body = safe_json(r)
        if body.get("application_id") != application.id:
            _fail(r, f"Fetching application fees returned invalid application id: {r.status_code} {r.text}")
            return payment

        try:
            fee_amount = float(body.get("fee_amount"))
        except Exception:
            _fail(r, f"Fetching application fees returned invalid amount: {body.get('fee_amount')}")
            return payment

        expected = LICENSE_MAP[application.license_type].price
        if fee_amount != expected:
            _fail(r, f"Fetching application fees returned invalid amount: got={fee_amount} expected={expected}")
            return payment

        payment.amount = fee_amount
        r.success()
        return payment


def api_create_payment(
        client: HttpSession,
        application_id: int,
        payment: PaymentContext,
        access_token: str,
) -> PaymentContext:
    with client.post(
            f"{BACKEND_URL}/applications/{application_id}/payments",
            headers=headers(access_token),
            params={"application_id": application_id},
            json={
                "application_id": application_id,
                "name": payment.name,
                "iban": payment.iban,
                "bic": payment.bic,
            },
            catch_response=True,
            name="POST /applications/:id/payments",
    ) as r:
        if r.status_code != 201:
            _fail(r, f"Payment creation failed: {r.status_code} {r.text}")
            return payment

        body = safe_json(r)
        if body.get("application_id") != application_id:
            _fail(r, f"Payment creation returned invalid application id: {r.status_code} {r.text}")
            return payment

        try:
            amount = float(body.get("amount"))
        except Exception:
            _fail(r, f"Payment creation returned invalid amount: {r.status_code} {r.text}")
            return payment

        if payment.amount != amount:
            _fail(r, f"Payment creation returned invalid amount: got={amount} expected={payment.amount}")
            return payment

        if body.get("payment_status") not in PAYMENT_STATUSES:
            _fail(r, f"Payment create returned invalid status: {r.status_code} {r.text}")
            return payment

        try:
            payment.id = int(body.get("id"))
            payment.application_id = int(body.get("application_id"))
        except Exception:
            _fail(r, f"Payment creation returned invalid ids: {r.status_code} {r.text}")
            return payment

        payment.amount = amount
        payment.payment_date = body.get("payment_date")
        payment.payment_status = body.get("payment_status")

        r.success()
        return payment


def api_list_applications(client: HttpSession, user_id: str, access_token: str) -> Optional[List[ApplicationContext]]:
    with client.get(
            f"{BACKEND_URL}/applications",
            headers=headers(access_token),
            params={"user_id": user_id},
            catch_response=True,
            name="GET /applications",
    ) as r:
        if r.status_code != 200:
            _fail(r, f"Fetching applications failed: {r.status_code} {r.text}")
            return None

        body = safe_json(r)
        if not isinstance(body, list):
            _fail(r, f"Fetching applications returned invalid data structure: {r.status_code} {r.text}")
            return None

        applications: List[ApplicationContext] = []
        for item in body:
            if item.get("user_id") != user_id:
                _fail(r, f"Fetching applications returned invalid user id: {r.status_code} {r.text}")
                return None
            if item.get("application_status") not in APPLICATION_STATUSES:
                _fail(r, f"Fetching applications returned invalid application status: {r.status_code} {r.text}")
                return None

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

        r.success()
        return applications if applications else None


def api_list_applications_by_id(
        client: HttpSession,
        application: ApplicationContext,
        user_id: str,
        access_token: str,
) -> ApplicationContext:
    with client.get(
            f"{BACKEND_URL}/applications/{application.id}",
            headers=headers(access_token),
            params={"user_id": user_id},
            catch_response=True,
            name="GET /applications/:id",
    ) as r:
        if r.status_code != 200:
            _fail(r, f"Fetching application failed: {r.status_code} {r.text}")
            return application

        body = safe_json(r)
        if body.get("id") != application.id:
            _fail(r, f"Fetching application returned invalid id: {r.status_code} {r.text}")
            return application

        if body.get("application_status") not in APPLICATION_STATUSES:
            _fail(r, f"Fetching application returned invalid application status: {r.status_code} {r.text}")
            return application

        application.id = int(body.get("id"))
        application.user_id = body.get("user_id")
        application.license_type = body.get("license_type")
        application.cadastral_reference = body.get("cadastral_reference")
        application.applied_at = body.get("applied_at")
        application.changed_at = body.get("changed_at")
        application.application_status = body.get("application_status")
        application.remarks = body.get("remarks")

        r.success()
        return application


def api_get_user_by_id(client: HttpSession, user: UserContext, access_token: str) -> UserContext:
    with client.get(
            f"{BACKEND_URL}/users/{user.id}",
            headers=headers(access_token),
            params={"user_id": user.id},
            catch_response=True,
            name="GET /users/:id",
    ) as r:
        if r.status_code != 200:
            _fail(r, f"Fetching user by id failed: {r.status_code} {r.text}")
            return user

        body = safe_json(r)
        if body.get("id") != user.id:
            _fail(r, f"Fetching user by id returned invalid user id: {r.status_code} {r.text}")
            return user

        user.id = body.get("id")
        user.username = body.get("username")
        user.email = body.get("email")
        user.firstName = body.get("firstName")
        user.lastName = body.get("lastName")
        user.enabled = bool(body.get("enabled"))
        user.email_verified = bool(body.get("emailVerified"))
        user.created_timestamp = body.get("createdTimestamp")

        r.success()
        return user


def api_list_licenses(client: HttpSession, user_id: str, access_token: str) -> Optional[LicenseContext]:
    with client.get(
            f"{BACKEND_URL}/licenses",
            headers=headers(access_token),
            params={"user_id": user_id},
            catch_response=True,
            name="GET /licenses",
    ) as r:
        if r.status_code not in (200, 404):
            _fail(r, f"Fetching licenses failed: {r.status_code} {r.text}")
            return None

        if r.status_code == 404:
            r.success()
            return None

        body = safe_json(r)
        if not isinstance(body, list):
            _fail(r, f"Fetching licenses returned invalid data structure: {r.status_code} {r.text}")
            return None

        licenses: List[LicenseContext] = []
        for item in body:
            if item.get("user_id") != user_id:
                _fail(r, f"Fetching licenses returned invalid user id: {r.status_code} {r.text}")
                return None
            if item.get("license_status") not in LICENSE_STATUSES:
                _fail(r, f"Fetching license returned invalid license status: {r.status_code} {r.text}")
                return None

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

        r.success()
        return licenses[-1] if licenses else None


def api_get_license_by_id(client: HttpSession, license: Optional[LicenseContext], access_token: str) -> Optional[
    LicenseContext]:
    if license is None:
        return None

    with client.get(
            f"{BACKEND_URL}/licenses/{license.id}",
            headers=headers(access_token),
            params={"license_id": license.id},
            catch_response=True,
            name="GET /licenses/:id",
    ) as r:
        if r.status_code != 200:
            _fail(r, f"Fetching license by id failed: {r.status_code} {r.text}")
            return license

        body = safe_json(r)
        if body.get("id") != license.id:
            _fail(r, f"Fetching license by id returned invalid license id: {r.status_code} {r.text}")
            return license

        if body.get("license_status") not in LICENSE_STATUSES:
            _fail(r, f"Fetching license returned invalid license status: {r.status_code} {r.text}")
            return license

        license.user_id = body.get("user_id")
        try:
            license.application_id = int(body.get("application_id"))
        except Exception:
            _fail(r, f"Fetching license by id returned invalid application_id: {r.status_code} {r.text}")
            return license
        license.license_type = body.get("license_type")
        license.license_status = body.get("license_status")
        license.issued_at = body.get("issued_at")
        license.expires_at = body.get("expires_at")
        license.cadastral_reference = body.get("cadastral_reference")

        r.success()
        return license


def api_list_payments(client: HttpSession, application_id: int, access_token: str) -> Optional[PaymentContext]:
    with client.get(
            f"{BACKEND_URL}/applications/{application_id}/payments",
            headers=headers(access_token),
            params={"application_id": application_id},
            catch_response=True,
            name="GET /applications/:id/payments",
    ) as r:
        if r.status_code != 200:
            _fail(r, f"Fetching payments failed: {r.status_code} {r.text}")
            return None

        body = safe_json(r)
        if not isinstance(body, list):
            _fail(r, f"Fetching payments returned invalid data structure: {r.status_code} {r.text}")
            return None

        payments: List[PaymentContext] = []
        for item in body:
            if item.get("application_id") != application_id:
                _fail(r, f"Fetching payments returned invalid application id: {r.status_code} {r.text}")
                return None
            if item.get("payment_status") not in PAYMENT_STATUSES:
                _fail(r, f"Fetching payment returned invalid payment status: {r.status_code} {r.text}")
                return None

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

        r.success()
        return payments[-1] if payments else None


def api_logout(client: HttpSession, user_or_admin: UserContext) -> UserContext:
    user_or_admin.access_token = ""
    user_or_admin.refresh_token = ""
    user_or_admin.expires_in = 0
    user_or_admin.refresh_expires_in = 0
    user_or_admin.token_type = ""
    user_or_admin.is_admin = False

    client.headers.pop("Authorization", None)
    return user_or_admin
