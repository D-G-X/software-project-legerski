import random
import time

from locust.clients import HttpSession, ResponseContextManager

from testkit.config import BACKEND_URL, DOCUMENT_STATUSES, TEST_PDF_PATH, LICENSE_MAP, PAYMENT_STATUSES, \
    APPLICATION_STATUSES, LICENSE_STATUSES
from testkit.models import ApplicationContext, PaymentContext, UserContext, \
    LicenseContext, BallotPeriodContext
from testkit.utils import decode_jwt, headers, read_file


############################################################################
# Authentication functions
############################################################################

def api_register(client: HttpSession, user: UserContext) -> None:
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
    assert r.json().get("user_id"), f"Registration returned no user id: {r.status_code} {r.text}"

    user.id = r.json().get("user_id")


def api_login(client: HttpSession, user: UserContext) -> None:
    r = client.post(
        f"{BACKEND_URL}/login",
        json={
            "email": user.email,
            "password": user.password,
        },
    )
    assert r.status_code == 200, f"Login failed: {r.status_code} {r.text}"
    assert "access_token" in r.json(), f"Login returned no access token: {r.status_code} {r.text}"

    user.access_token = r.json().get("access_token")
    user.refresh_token = r.json().get("refresh_token")
    user.expires_in = int(r.json().get("expires_in"))
    user.refresh_expires_in = int(r.json().get("refresh_expires_in"))
    user.token_type = r.json().get("token_type")
    user.is_admin = bool(r.json().get("is_admin"))

    assert user.id == decode_jwt(user.access_token)["sub"], f"Login returned invalid user id: {r.status_code} {r.text}"

    if user.access_token:
        client.headers.update({"Authorization": f"Bearer {user.access_token}"})


def api_refresh_login(client: HttpSession, user: UserContext) -> None:
    r = client.post(
        f"{BACKEND_URL}/refresh-login",
        json={
            "refresh_token": user.refresh_token,
        },
    )
    assert r.status_code == 200, f"Refresh login failed: {r.status_code} {r.text}"
    assert "access_token" in r.json(), f"Refresh login returned no access token: {r.status_code} {r.text}"

    user.access_token = r.json().get("access_token")
    user.refresh_token = r.json().get("refresh_token")
    user.expires_in = int(r.json().get("expires_in"))
    user.refresh_expires_in = int(r.json().get("refresh_expires_in"))
    user.token_type = r.json().get("token_type")
    user.is_admin = bool(r.json().get("is_admin"))

    client.headers.update({"Authorization": f"Bearer {user.access_token}"})


def api_request_reset_password(client: HttpSession, user_email: str) -> None:
    r = client.post(
        f"{BACKEND_URL}/reset-password",
        json={
            "email": user_email,
        },
    )
    assert r.status_code == 200, f"Password reset request failed: {r.status_code} {r.text}"


def api_reset_password(client: HttpSession, user: UserContext, new_password: str) -> None:
    r = client.post(
        f"{BACKEND_URL}/change-password",
        json={
            "token": "",  # TODO: get token from email
            "new_password": new_password,
        },
    )
    assert r.status_code == 200, f"Password reset failed: {r.status_code} {r.text}"
    user.password = new_password


# TODO: FIX API
def api_change_password(client: HttpSession, user: UserContext, new_password: str) -> None:
    r = client.put(
        f"{BACKEND_URL}/users/{user.id}/change-password",
        headers=headers(user.access_token),
        params={"user_id": user.id},
        json={
            "old_password": user.password,
            "new_password": new_password,
        },
    )
    assert r.status_code == 201, f"Password change request failed: {r.status_code} {r.text}"
    assert r.json().get("description"), f"Password change request returned no description: {r.status_code} {r.text}"
    user.password = new_password


# TODO: FIX API
def api_change_name(client: HttpSession, user: UserContext, new_firstname: str, new_lastname: str) -> None:
    r = client.put(
        f"{BACKEND_URL}/users/{user.id}",
        headers=headers(user.access_token),
        json={
            "firstName": new_firstname,
            "lastName": new_lastname,
            "email": user.email,
            "enabled": True,
            "credentials": [
                {
                    "type": "password",
                    "value": user.password,
                    "temporary": False,
                },
            ]
        },
    )
    assert r.status_code == 204, f"User name change request failed: {r.status_code} {r.text}"
    assert r.json().get("description"), f"User name change request returned no description: {r.status_code} {r.text}"
    user.firstname = new_firstname
    user.lastname = new_lastname


def api_logout(client: HttpSession, user_or_admin: UserContext) -> None:
    user_or_admin.access_token = ""
    user_or_admin.refresh_token = ""
    user_or_admin.expires_in = 0
    user_or_admin.refresh_expires_in = 0
    user_or_admin.token_type = ""
    user_or_admin.is_admin = False

    client.headers.pop("Authorization", None)


############################################################################
# User functions
############################################################################

def api_get_users(client: HttpSession, user_name: str, email: str, first: int, max: int, access_token: str) -> None:
    params: dict[str, str] = {}
    # TODO: FIX API
    if user_name:
        params["username"] = user_name
    if email:
        params["email"] = email
    if first:
        params["first"] = str(first)
    if max:
        params["max"] = str(max)

    r = client.get(
        f"{BACKEND_URL}/users",
        headers=headers(access_token),
        params={"username": "", "email": ""},
    )
    assert r.status_code == 200, f"Fetching users failed: {r.status_code} {r.text}"
    assert isinstance(r.json(), list), f"Fetching users returned invalid data structure: {r.status_code} {r.text}"

    users: list[UserContext] = []
    for item in r.json():
        assert item.get("id"), f"Fetching users returned invalid user id: {r.status_code} {r.text}"
        users.append(
            UserContext(
                id=item["id"],
                username=item["username"],
                email=item["email"],
                password=item["password"],
                firstname=item["first_name"],
                lastname=item["last_name"],
                enabled=bool(item["enabled"]),
                email_verified=bool(item["email_verified"]),
                created_timestamp=item["created_timestamp"],
            )
        )


def api_get_user_by_id(client: HttpSession, user_id: str, access_token: str) -> None:
    r = client.get(
        f"{BACKEND_URL}/users/{user_id}",
        headers=headers(access_token),
        params={"user_id": user_id},
    )
    assert r.status_code == 200, f"Fetching user by id failed: {r.status_code} {r.text}"
    assert r.json().get("id") == user_id, f"Fetching user by id returned invalid user id: {r.status_code} {r.text}"


def api_create_user(client: HttpSession, user: UserContext, access_token: str) -> None:
    r = client.post(
        f"{BACKEND_URL}/users",
        headers=headers(access_token),
        json={
            "firstName": user.firstname,
            "lastName": user.lastname,
            "email": user.email,
            "enabled": True,
            "credentials": [
                {
                    "type": "password",
                    "temporary": False,
                    "value": user.password,
                },
            ]
        },
    )
    assert r.status_code == 201, f"User creation failed: {r.status_code} {r.text}"
    assert r.json().get("id") == user.id, f"User creation returned invalid user id: {r.status_code} {r.text}"


def api_update_user(client: HttpSession, user: UserContext, access_token: str) -> None:
    r = client.put(
        f"{BACKEND_URL}/users/{user.id}",
        headers=headers(access_token),
        json={
            "firstName": user.firstname,
            "lastName": user.lastname,
            "email": user.email,
            "enabled": True,
            "credentials": [
                {
                    "type": "password",
                    "temporary": False,
                    "value": user.password,
                },
            ]
        },
    )
    assert r.status_code == 204, f"User update failed: {r.status_code} {r.text}"
    assert r.text, f"User update returned no response: {r.status_code} {r.text}"


def api_delete_user(client: HttpSession, user_id: str, access_token: str) -> None:
    r = client.delete(
        f"{BACKEND_URL}/users/{user_id}",
        headers=headers(access_token),
        params={"user_id": user_id},
    )
    assert r.status_code == 204, f"User deletion failed: {r.status_code} {r.text}"
    assert r.text, f"User deletion returned no response: {r.status_code} {r.text}"


############################################################################
# Application functions
############################################################################

def api_get_applications(client: HttpSession, user_id: str, access_token: str) -> None:
    params: dict[str, str] = {}
    if user_id:
        params["user_id"] = user_id
    r = client.get(
        f"{BACKEND_URL}/applications",
        headers=headers(access_token),
        params=params,
    )
    assert r.status_code == 200, f"Fetching applications failed: {r.status_code} {r.text}"
    assert isinstance(r.json(),
                      list), f"Fetching applications returned invalid data structure: {r.status_code} {r.text}"

    applications: list[ApplicationContext] = []
    for item in r.json():
        if user_id:
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
                application_status=item["status"],
                remarks=item["remarks"],
            )
        )


def api_create_application(client: HttpSession, application: ApplicationContext, user_id: str,
                           access_token: str) -> None:
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
    assert user_id == r.json().get("user_id"), f"Application request returned invalid user id: {r.status_code} {r.text}"
    application.id = r.json().get("id")
    application.applied_at = r.json().get("applied_at")
    application.changed_at = r.json().get("changed_at")


def api_get_application_by_id(client: HttpSession, user_id: str, access_token: str) -> None:
    r = client.get(
        f"{BACKEND_URL}/applications",
        headers=headers(access_token),
        params={"user_id": user_id}
    )
    assert r.status_code == 200, f"Fetching applications failed: {r.status_code} {r.text}"
    assert r.json().get("id") == user_id, f"Fetching application returned invalid user id: {r.status_code} {r.text}"
    assert r.json().get(
        "application_status") in APPLICATION_STATUSES, f"Fetching applications returned invalid application status: {r.status_code} {r.text}"


def api_update_application(client: HttpSession, application: ApplicationContext, new_application_status: str,
                           new_cadastral_reference: str,
                           new_remarks: str, new_license_type: str, access_token: str) -> None:
    r = client.put(
        f"{BACKEND_URL}/applications/{application.id}",
        headers=headers(access_token),
        json={
            "application_status": new_application_status,
            "cadastral_reference": new_cadastral_reference,
            "remarks": new_remarks,
            "license_type": new_license_type,
        },
    )
    assert r.status_code == 200, f"Application update failed: {r.status_code} {r.text}"
    assert application.id == r.json().get(
        "id"), f"Application update returned invalid application id: {r.status_code} {r.text}"
    assert isinstance(r.json(), dict), f"Application update returned invalid data structure: {r.status_code} {r.text}"
    assert r.json().get(
        "application_status") == new_application_status, f"Application update returned invalid application status: {r.status_code} {r.text}"
    assert r.json().get(
        "cadastral_reference") == new_cadastral_reference, f"Application update returned invalid cadastral reference: {r.status_code} {r.text}"
    assert r.json().get(
        "remarks") == new_remarks, f"Application update returned invalid remarks: {r.status_code} {r.text}"
    assert r.json().get(
        "license_type") == new_license_type, f"Application update returned invalid license type: {r.status_code} {r.text}"
    assert r.json().get("applied_at") == application.applied_at

    application.application_status = new_application_status
    application.cadastral_reference = new_cadastral_reference
    application.remarks = new_remarks
    application.license_type = new_license_type
    application.changed_at = r.json().get("changed_at")


def api_delete_application(client: HttpSession, application_id: str, access_token: str) -> None:
    r = client.delete(
        f"{BACKEND_URL}/applications/{application_id}",
        headers=headers(access_token),
        params={"application_id": application_id},
    )
    assert r.status_code == 204, f"Application deletion failed: {r.status_code} {r.text}"
    assert r.text, f"Application deletion returned no response: {r.status_code} {r.text}"


############################################################################
# License functions
############################################################################

def api_get_licenses(client: HttpSession, user_id: str, access_token: str) -> None:
    r = client.get(
        f"{BACKEND_URL}/licenses",
        headers=headers(access_token),
        params={"user_id": user_id},
    )
    assert r.status_code == 200, f"Fetching licenses failed: {r.status_code} {r.text}"
    assert isinstance(r.json(), list), f"Fetching licenses returned invalid data structure: {r.status_code} {r.text}"

    licenses: list[LicenseContext] = []
    for item in r.json():
        assert item.get("user_id") == user_id, f"Fetching licenses returned invalid user id: {r.status_code} {r.text}"
        assert item.get(
            "license_status") in LICENSE_STATUSES, f"Fetching license returned invalid license status: {r.status_code} {r.text}"
        licenses.append(
            LicenseContext(
                id=int(item["id"]),
                user_id=item["user_id"],
                application_id=int(item["application_id"]),
                license_type=item["license_type"],
                license_status=item["status"],
                issued_at=item["issued_at"],
                expires_at=item["expiry_at"],
                cadastral_reference=item["cadastral_reference"],
            )
        )


def api_get_license_by_id(client: HttpSession, license_id: int, access_token: str) -> None:
    r = client.get(
        f"{BACKEND_URL}/licenses/{license_id}",
        headers=headers(access_token),
        params={"license_id": license_id},
    )
    assert r.status_code == 200, f"Fetching license by id failed: {r.status_code} {r.text}"
    assert r.json().get(
        "id") == license_id, f"Fetching license by id returned invalid license id: {r.status_code} {r.text}"
    assert r.json().get(
        "license_status") in LICENSE_STATUSES, f"Fetching license returned invalid license status: {r.status_code} {r.text}"


def api_update_license(client: HttpSession, license: LicenseContext, new_license_status: str,
                       access_token: str) -> None:
    r = client.put(
        f"{BACKEND_URL}/licenses/{license.id}",
        headers=headers(access_token),
        params={"license_id": license.id},
        json={
            "license_status": new_license_status,
        },
    )
    assert r.status_code == 200, f"License status update failed: {r.status_code} {r.text}"
    assert r.json().get(
        "id") == license.id, f"License status update returned invalid license id: {r.status_code} {r.text}"
    assert r.json().get(
        "status") == new_license_status, f"License status update returned invalid license status: {r.status_code} {r.text}"
    license.license_status = new_license_status


def api_delete_license(client: HttpSession, license_id: int, access_token: str) -> None:
    r = client.delete(
        f"{BACKEND_URL}/licenses/{license_id}",
        headers=headers(access_token),
        params={"license_id": license_id},
    )
    assert r.status_code == 204, f"License deletion failed: {r.status_code} {r.text}"
    assert r.json().get("description"), f"License deletion returned no description: {r.status_code} {r.text}"


############################################################################
# Document verification functions
############################################################################

def api_get_document_verification_status(client: HttpSession, application: ApplicationContext,
                                         access_token: str) -> ResponseContextManager:
    r = client.get(
        f"{BACKEND_URL}/applications/{application.id}/documents",
        headers=headers(access_token),
        params={"application_id": application.id},
    )
    assert r.status_code == 200, f"Document status fetch failed: {r.status_code} {r.text}"
    assert application.id == r.json().get(
        "application_id"), f"Document status fetch returned invalid application id: {r.status_code} {r.text}"
    assert r.json().get(
        "status") in DOCUMENT_STATUSES, f"Document status fetch returned invalid status: {r.status_code} {r.text}"
    application.status = r.json().get("status")
    application.rejection_reason = r.json().get("rejection_reason")

    return r


############################################################################
# Payment functions
############################################################################

def api_get_payments(client: HttpSession, application_id: int, access_token: str) -> None:
    r = client.get(
        f"{BACKEND_URL}/applications/{application_id}/payments",
        headers=headers(access_token),
        params={"application_id": application_id},
    )
    assert r.status_code == 200, f"Fetching payments failed: {r.status_code} {r.text}"
    assert isinstance(r.json(),
                      list), f"Fetching payments returned invalid data structure: {r.status_code} {r.text}"

    payments: list[PaymentContext] = []
    for item in r.json():
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
                payment_status=item["status"],
            )
        )


def api_create_payment(client: HttpSession, application_id: int, payment: PaymentContext,
                       access_token: str) -> None:
    r = client.post(
        f"{BACKEND_URL}/applications/{application_id}/payments",
        headers=headers(access_token),
        json={
            "application_id": application_id,
            "name": payment.name,
            "iban": payment.iban,
            "bic": payment.bic,
        },
    )
    assert r.status_code == 201, f"Payment creation failed: {r.status_code} {r.text}"
    assert application_id == r.json().get(
        "application_id"), f"Payment creation returned invalid application id: {r.status_code} {r.text}"
    assert payment.amount == r.json().get(
        "amount"), f"Payment creation returned invalid amount: {r.status_code} {r.text}"
    assert r.json().get(
        "payment_status") in PAYMENT_STATUSES, f"Payment create returned invalid status: {r.status_code} {r.text}"
    payment.id = r.json().get("id")
    payment.application_id = application_id
    payment.payment_date = r.json().get("payment_date")
    payment.payment_status = r.json().get("status")


def api_get_application_fee(client: HttpSession, application: ApplicationContext, access_token: str) -> None:
    r = client.get(
        f"{BACKEND_URL}/fees/{application.id}",
        headers=headers(access_token),
        params={"application_id": application.id},
    )
    assert r.status_code == 200, f"Fetching application fees failed: {r.status_code} {r.text}"
    assert application.id == r.json().get(
        "application_id"), f"Fetching application fees returned invalid application id: {r.status_code} {r.text}"
    assert r.json().get("fee_amount") == LICENSE_MAP[
        application.license_type].price, f"Fetching application fees returned invalid amount: {r.json().get('fee_amount')}"
    application.amount = r.json().get("fee_amount")


############################################################################
# Ballot period functions
############################################################################

def api_get_ballot_periods(client: HttpSession, access_token: str) -> None:
    r = client.get(
        f"{BACKEND_URL}/ballot-periods",
        headers=headers(access_token),
    )
    assert r.status_code == 200, f"Fetching ballot periods failed: {r.status_code} {r.text}"
    assert isinstance(r.json(),
                      list), f"Fetching ballot periods returned invalid data structure: {r.status_code} {r.text}"

    ballot_periods: list[BallotPeriodContext] = []
    for item in r.json():
        ballot_periods.append(
            BallotPeriodContext(
                ballot_period_id=int(item["id"]),
                start_date=item["start_date"],
                end_date=item["end_date"],
                total_applications=int(item["total_applications"]),
            )
        )


def api_create_ballot_period(client: HttpSession, start_date: str, end_date: str,
                             access_token: str) -> None:
    r = client.post(
        f"{BACKEND_URL}/ballot-periods",
        headers=headers(access_token),
        json={
            "start_date": start_date,
            "end_date": end_date,
        },
    )
    assert r.status_code == 201, f"Ballot period creation failed: {r.status_code} {r.text}"
    assert r.json().get("id"), f"Ballot period creation returned no id: {r.status_code} {r.text}"


def api_get_current_ballot_period(client: HttpSession, access_token: str) -> None:
    r = client.get(
        f"{BACKEND_URL}/ballot-periods/current",
        headers=headers(access_token),
    )
    assert r.status_code == 200, f"Fetching current ballot period failed: {r.status_code} {r.text}"


def api_get_ballot_period(client: HttpSession, ballot_period_id: int, access_token: str) -> None:
    r = client.get(
        f"{BACKEND_URL}/ballot-periods/{ballot_period_id}",
        headers=headers(access_token),
        params={"ballot_period_id": ballot_period_id},
    )
    assert r.status_code == 200, f"Fetching ballot period by id failed: {r.status_code} {r.text}"


def api_get_ballot_period_entries(client: HttpSession, ballot_period_id: int, access_token: str) -> None:
    r = client.get(
        f"{BACKEND_URL}/ballot-periods/{ballot_period_id}/entries",
        headers=headers(access_token),
        params={"period_id": ballot_period_id},
    )
    assert r.status_code == 200, f"Fetching ballot period entries failed: {r.status_code} {r.text}"
    assert isinstance(r.json(),
                      list), f"Fetching ballot period entries returned invalid data structure: {r.status_code} {r.text}"

    applications: list[ApplicationContext] = []
    for item in r.json():
        assert item.get(
            "id") == ballot_period_id, f"Fetching ballot period entries returned invalid ballot period id: {r.status_code} {r.text}"
        assert r.json().get(
            "application_status") in APPLICATION_STATUSES, f"Fetching ballot period entries returned invalid application status: {r.status_code} {r.text}"
        applications.append(
            ApplicationContext(
                id=int(item["id"]),
                user_id=item["user_id"],
                license_type=item["license_type"],
                cadastral_reference=item["cadastral_reference"],
                applied_at=item["applied_at"],
                changed_at=item["changed_at"],
                application_status=item["status"],
                remarks=item["remarks"],
            )
        )


def api_create_lottery(client: HttpSession, ballot_period_id: int, license_count: int, license_type: str,
                       access_token: str) -> None:
    json: dict[str, str] = {}
    if license_type and license_count > 0:
        json["license_type"] = license_type
    r = client.post(
        f"{BACKEND_URL}/ballot-periods/{ballot_period_id}/draw",
        headers=headers(access_token),
        params={"period_id": ballot_period_id},
        json=json
    )
    assert r.status_code == 200, f"Lottery creation failed: {r.status_code} {r.text}"
    assert r.json().get(
        "selected_applications"), f"Lottery creation returned no selected applications: {r.status_code} {r.text}"

    selected_applications: list[ApplicationContext] = []
    rejected_applications: list[ApplicationContext] = []
    for item in r.json().get("selected_applications"):
        assert r.json().get(
            "application_status") in APPLICATION_STATUSES, f"Fetching ballot period entries returned invalid application status: {r.status_code} {r.text}"
        selected_applications.append(
            ApplicationContext(
                id=int(item["id"]),
                user_id=item["user_id"],
                license_type=item["license_type"],
                cadastral_reference=item["cadastral_reference"],
                applied_at=item["applied_at"],
                changed_at=item["changed_at"],
                application_status=item["status"],
                remarks=item["remarks"],
            )
        )
    for item in r.json().get("rejected_applications"):
        assert r.json().get(
            "application_status") in APPLICATION_STATUSES, f"Fetching ballot period entries returned invalid application status: {r.status_code} {r.text}"
        rejected_applications.append(
            ApplicationContext(
                id=int(item["id"]),
                user_id=item["user_id"],
                license_type=item["license_type"],
                cadastral_reference=item["cadastral_reference"],
                applied_at=item["applied_at"],
                changed_at=item["changed_at"],
                application_status=item["status"],
                remarks=item["remarks"],
            )
        )


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
    assert application_id == r.json().get(
        "application_id"), f"Document upload returned invalid application id: {r.status_code} {r.text}"


############################################################################
# Synthetic functions
############################################################################

def api_wait_for_document_verification(
        client: HttpSession,
        application: ApplicationContext,
        access_token: str,
        timeout_seconds: int = 120,
        initial_interval: float = 0.2,
        max_interval: float = 2.0,
) -> None:
    deadline = time.monotonic() + timeout_seconds
    interval = initial_interval

    r = None
    while time.monotonic() < deadline:
        r = api_get_document_verification_status(client, application, access_token)

        if application.application_status != DOCUMENT_STATUSES.PENDING:
            return

        # backoff + jitter (to avoid thundering herd)
        jitter = random.uniform(-0.2, 0.2) * interval
        time.sleep(max(0.0, interval + jitter))
        interval = min(max_interval, interval * 1.5)

    # Timeout
    msg = f"Document verification timed out for application {application.id}"
    if r is None:
        raise RuntimeError(msg + ": no response")
    raise RuntimeError(msg + f": {r.status_code} {r.text}")


############################################################################
# Helper functions
############################################################################

def api_update_application_status(client: HttpSession, application: ApplicationContext,
                                  access_token: str) -> None:
    r = client.get(
        f"{BACKEND_URL}/applications/{application.id}",
        headers=headers(access_token),
        data={"application_id": application.id},
    )
    assert r.status_code == 200, f"Fetching application status failed: {r.status_code} {r.text}"
    assert application.id == r.json().get(
        "application_id"), f"Fetching application status returned invalid application id: {r.status_code} {r.text}"
    application.status = r.json().get("status")
