from datetime import datetime
import re

import requests

from testkit.config import FRONTEND_URL, BACKEND_URL, ACCESS_TOKEN_KEY, REFRESH_TOKEN_KEY, ACCESS_EXP_KEY, \
    REFRESH_EXP_KEY, TOKEN_TYPE_KEY, ROLE_KEY
from testkit.models import UserContext, BallotPeriodContext
from testkit.runner.utils import decode_jwt, headers

TIMEOUT = 10_000

############################################################################
# Authentication functions
############################################################################

def register(page, user: UserContext) -> UserContext:
    page.goto(f"{FRONTEND_URL}/")
    page.locator("#registerLink").click()
    page.wait_for_url("/register")

    page.locator("#firstName").fill(user.firstname)
    page.locator("#lastName").fill(user.lastname)
    page.locator("#email").fill(user.email)
    page.locator("#password").fill(user.password)
    page.locator("#confirmPassword").fill(user.password)

    with page.expect_response(
            lambda res: res.request.method == "POST" and re.search(r"/register$", res.url)
    ) as res_info:
        page.locator("#registerButton").click()
    r = res_info.value
    assert r.status == 201, f"Registration failed: {r.status_text} {r.text}"
    assert r.json().get("user_id"), f"Registration returned no user id: {r.status_text} {r.text}"
    user.id = r.json().get("user_id")

    page.wait_for_url("/login")
    return user


# TODO: add keep logged in
def login(page, user: UserContext) -> UserContext:
    page.goto(f"{FRONTEND_URL}/")
    page.locator("#signInLink").click()
    page.wait_for_url("/login")

    page.locator("#email").fill(user.email)
    page.locator("#password").fill(user.password)

    with page.expect_response(
            lambda res: res.request.method == "POST" and re.search(r"/login$", res.url)
    ) as res_info:
        page.locator("#loginButton").click()
    r = res_info.value
    assert r.status == 200, f"Login failed: {r.status_text} {r.text}"
    assert "access_token" in r.json(), f"Login returned no access token: {r.status_text} {r.text}"

    user.access_token = r.json().get("access_token")
    user.refresh_token = r.json().get("refresh_token")
    user.expires_in = int(r.json().get("expires_in"))
    user.refresh_expires_in = int(r.json().get("refresh_expires_in"))
    user.token_type = r.json().get("token_type")
    user.is_admin = bool(r.json().get("is_admin"))

    assert user.id == decode_jwt(user.access_token)["sub"], f"Login returned invalid user id: {r.status_text} {r.text}"
    verify_local_storage_after_login(page, r.json())

    page.wait_for_url("/")
    return user


def refresh_login(page, user: UserContext) -> UserContext:
    access_token_old, refresh_token_old, expires_in_old, refresh_expires_in_old, token_type_old, is_admin_old = get_auth_details_from_local_storage(
        page)
    assert access_token_old and refresh_token_old and expires_in_old and refresh_expires_in_old and token_type_old and is_admin_old is not None, "No auth details in local storage"

    with page.expect_response(
            lambda r: r.url == f"{BACKEND_URL}/refresh-login" and r.status == 200,
            timeout=15000
    ):
        page.reload()

    # Verify that user is still logged in
    page.locator("#signOutBtn").wait_for(timeout=TIMEOUT)

    access_token_new, refresh_token_new, expires_in_new, refresh_expires_in_new, token_type_new, is_admin_new = get_auth_details_from_local_storage(
        page)
    assert access_token_new != access_token_old, "Access token was not refreshed"
    assert refresh_token_new != refresh_token_old, "Refresh token was not refreshed"
    assert expires_in_new != expires_in_old, "Expires in was not refreshed"
    assert refresh_expires_in_new != refresh_expires_in_old, "Refresh expires in was not refreshed"
    assert token_type_new == token_type_old, "Token type was changed"
    assert is_admin_new == is_admin_old, "Is admin was changed"

    user.access_token = access_token_new
    user.refresh_token = refresh_token_new
    user.expires_in = expires_in_new
    user.refresh_expires_in = refresh_expires_in_new

    return user


def request_reset_password(page, user_email: str) -> None:
    page.goto(f"{FRONTEND_URL}/")
    page.locator("#signInLink").click()
    page.wait_for_url("/login")
    page.locator("#forgotPasswortLink").click()

    page.wait_for_url("/forgot-password")
    page.locator("#email").fill(user_email)

    #with page.expect_response(
    #        lambda res: res.request.method == "POST" and re.search(r"/reset-password$", res.url)
    #) as res_info:
    #    page.locator("#resetPasswordBtn").click()
    #r = res_info.value
    #assert r.status == 200, f"Password reset request failed: {r.status_code} {r.text}"
    # 1) Klick ausführen und auf die passende Response warten
    #with page.expect_response(lambda r: "password" in r.url.lower() and r.request.method == "POST", timeout=15_000) as resp_info:
    #    page.locator("#resetPasswordBtn").click()   # <-- sicherstellen: richtiger ID!
#
    #resp = resp_info.value
    #print("RESET RESPONSE:", resp.status, resp.url)
    #print("RESET BODY:", resp.text())
#
    ## 2) Wenn nicht 2xx -> du bist im catch-Zweig und Confirmation kommt NIE
    #assert resp.status in (200, 204), f"Reset request failed: {resp.status} {resp.text()}"
    page.locator("#resetPasswordBtn").click()
    page.locator("#resetPasswordBtn").wait_for(state="visible", timeout=TIMEOUT)
    page.locator("#resetPasswordBtn").click()
    page.wait_for_url("/login")


'''
def reset_password(user: UserContext, new_password: str) -> UserContext:
    r = requests.post(
        f"{FRONTEND_URL}/change-password",
        json={
            "token": "",  # TODO: get token from email
            "new_password": new_password,
        },
    )
    assert r.status == 200, f"Password reset failed: {r.status_code} {r.text}"
    user.password = new_password

    return user


# TODO: FIX API
def change_password(user: UserContext, new_password: str) -> UserContext:
    r = requests.patch(
        f"{FRONTEND_URL}/users/{user.id}/change-password",
        headers=_headers(user.access_token),
        params={"user_id": user.id},
        json={
            "old_password": user.password,
            "new_password": new_password,
        },
    )
    assert r.status == 201, f"Password change request failed: {r.status_code} {r.text}"
    assert r.json().get("description"), f"Password change request returned no description: {r.status_code} {r.text}"
    user.password = new_password

    return user


# TODO: FIX API
def change_name(user: UserContext, new_firstname: str, new_lastname: str) -> UserContext:
    r = requests.patch(
        f"{FRONTEND_URL}/users/{user.id}",
        headers=_headers(user.access_token),
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
    assert r.status == 204, f"User name change request failed: {r.status_code} {r.text}"
    assert r.json().get("description"), f"User name change request returned no description: {r.status_code} {r.text}"
    user.firstname = new_firstname
    user.lastname = new_lastname

    return user

'''


def logout(page, user_or_admin: UserContext) -> UserContext:
    page.goto(f"{FRONTEND_URL}/")
    page.locator("#signOutBtn").click()

    user_or_admin.access_token = ""
    user_or_admin.refresh_token = ""
    user_or_admin.expires_in = 0
    user_or_admin.refresh_expires_in = 0
    user_or_admin.token_type = ""
    user_or_admin.is_admin = False

    page.wait_for_url("/login")
    return user_or_admin


'''

############################################################################
# User functions
############################################################################

def get_users(user_name: str, email: str, first: int, max: int, access_token: str) -> list[UserContext]:
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

    r = requests.get(
        f"{FRONTEND_URL}/users",
        headers=_headers(access_token),
        params={"username": "", "email": ""},
    )
    assert r.status == 200, f"Fetching users failed: {r.status_code} {r.text}"
    assert isinstance(r.json(), list), f"Fetching users returned invalid data structure: {r.status_code} {r.text}"

    users: list[UserContext] = []
    for item in r.json():
        assert item.get("id"), f"Fetching users returned invalid user id: {r.status_code} {r.text}"
        users.append(
            UserContext(
                id=item["id"],
                username=item["username"],
                email=item["email"],
                firstname=item["first_name"],
                lastname=item["last_name"],
                enabled=bool(item["enabled"]),
                email_verified=bool(item["email_verified"]),
                created_timestamp=item["created_timestamp"],
            )
        )
    return users


def get_user_by_id(user_id: str, access_token: str) -> UserContext:
    r = requests.get(
        f"{FRONTEND_URL}/users/{user_id}",
        headers=_headers(access_token),
        params={"user_id": user_id},
    )
    assert r.status == 200, f"Fetching user by id failed: {r.status_code} {r.text}"
    assert r.json().get("id") == user_id, f"Fetching user by id returned invalid user id: {r.status_code} {r.text}"

    return UserContext(
        id=r.json()["id"],
        username=r.json()["username"],
        email=r.json()["email"],
        firstname=r.json()["first_name"],
        lastname=r.json()["last_name"],
        enabled=bool(r.json()["enabled"]),
        email_verified=bool(r.json()["email_verified"]),
        created_timestamp=int(r.json()["created_timestamp"]),
    )


def create_user(user: UserContext, access_token: str) -> None:
    r = requests.post(
        f"{FRONTEND_URL}/users",
        headers=_headers(access_token),
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
    assert r.status == 201, f"User creation failed: {r.status_code} {r.text}"
    assert r.json().get("id") == user.id, f"User creation returned invalid user id: {r.status_code} {r.text}"


def update_user(user: UserContext, access_token: str) -> None:
    r = requests.put(
        f"{FRONTEND_URL}/users/{user.id}",
        headers=_headers(access_token),
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
    assert r.status == 204, f"User update failed: {r.status_code} {r.text}"
    assert r.text, f"User update returned no response: {r.status_code} {r.text}"


def delete_user(user_id: str, access_token: str) -> None:
    r = requests.delete(
        f"{FRONTEND_URL}/users/{user_id}",
        headers=_headers(access_token),
        params={"user_id": user_id},
    )
    assert r.status == 204, f"User deletion failed: {r.status_code} {r.text}"
    assert r.text, f"User deletion returned no response: {r.status_code} {r.text}"


def get_user_notifications(user_id: str, access_token: str) -> list[NotificationContext]:
    r = requests.get(
        f"{FRONTEND_URL}/users/{user_id}/notifications",
        headers=_headers(access_token),
        params={"user_id": user_id},
    )
    assert r.status == 200, f"Fetching notifications failed: {r.status_code} {r.text}"
    assert isinstance(r.json(),
                      list), f"Fetching notifications returned invalid data structure: {r.status_code} {r.text}"

    notifications: list[NotificationContext] = []
    for item in r.json():
        assert item.get(
            "user_id") == user_id, f"Fetching notifications returned invalid user id: {r.status_code} {r.text}"
        notifications.append(
            NotificationContext(
                id=item["id"],
                application_id=int(item["application_id"]),
                user_id=item["user_id"],
                date=item["date"],
                message=item["message"],
                is_read=bool(item["is_read"]),
            )
        )
    return notifications


# TODO: Here should the change password and name be, but the API is broken
def update_user_notification_as_read(notification_id: int, access_token: str) -> None:
    r = requests.patch(
        f"{FRONTEND_URL}/users/notifications/{notification_id}",
        headers=_headers(access_token),
        params={"id": notification_id},
    )
    assert r.status == 200, f"Updating notification as read failed: {r.status_code} {r.text}"
    assert r.text, f"Updating notification as read returned no response: {r.status_code} {r.text}"


def get_user_notification_preferences(user: UserContext, access_token: str) -> UserContext:
    r = requests.get(
        f"{FRONTEND_URL}/users/{user.id}/notifications/preferences",
        headers=_headers(access_token),
        params={"user_id": user.id},
    )
    assert r.status == 200, f"Fetching notification preferences failed: {r.status_code} {r.text}"
    assert r.json().get(
        "id") == user.id, f"Fetching notification preferences returned invalid user id: {r.status_code} {r.text}"
    user.notification_way = r.json().get("notification_way")
    user.application_updates_notification = bool(r.json().get("application_updates_notification"))
    user.license_renewal_notification = bool(r.json().get("license_renewal_notification"))

    return user


def update_user_notification_preferences(
        user: UserContext,
        new_notification_way: str,
        new_application_updates_notification: bool,
        new_license_renewal_notification: bool,
        access_token: str,
) -> UserContext:
    r = requests.patch(
        f"{FRONTEND_URL}/users/{user.id}/notifications/preferences",
        headers=_headers(access_token),
        params={"user_id": user.id},
        json={
            "notification_way": new_notification_way,
            "application_updates_notification": new_application_updates_notification,
            "license_renewal_notification": new_license_renewal_notification,
        },
    )
    assert r.status == 200, f"Updating notification preferences failed: {r.status_code} {r.text}"
    assert r.text, f"Updating notification preferences returned no response: {r.status_code} {r.text}"
    assert isinstance(r.json(),
                      dict), f"Updating notification preferences returned invalid data structure: {r.status_code} {r.text}"
    assert r.json().get(
        "notification_way") == new_notification_way, f"Updating notification preferences returned invalid notification way: {r.status_code} {r.text}"
    assert r.json().get(
        "application_updates_notification") == new_application_updates_notification, f"Updating notification preferences returned invalid application updates notification: {r.status_code} {r.text}"
    assert r.json().get(
        "license_renewal_notification") == new_license_renewal_notification, f"Updating notification preferences returned invalid license renewal notification: {r.status_code} {r.text}"

    user.notification_way = new_notification_way
    user.application_updates_notification = new_application_updates_notification
    user.license_renewal_notification = new_license_renewal_notification
    return user


############################################################################
# Application functions
############################################################################

def get_applications(user_id: str, access_token: str) -> list[ApplicationContext]:
    params: dict[str, str] = {}
    if user_id:
        params["user_id"] = user_id
    r = requests.get(
        f"{FRONTEND_URL}/applications",
        headers=_headers(access_token),
        params=params,
    )
    assert r.status == 200, f"Fetching applications failed: {r.status_code} {r.text}"
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
    return applications


def create_application(application: ApplicationContext, user_id: str, access_token: str) -> ApplicationContext:
    r = requests.post(
        f"{FRONTEND_URL}/applications",
        headers=_headers(access_token),
        json={
            "user_id": user_id,
            "license_type": application.license_type,
            "cadastral_reference": application.cadastral_reference,
            "remarks": application.remarks,
        },
    )
    assert r.status == 201, f"Application request failed: {r.status_code} {r.text}"
    assert user_id == r.json().get("user_id"), f"Application request returned invalid user id: {r.status_code} {r.text}"
    application.id = r.json().get("id")
    application.applied_at = r.json().get("applied_at")
    application.changed_at = r.json().get("changed_at")

    return application


def get_application_by_id(user_id: str, access_token: str) -> ApplicationContext:
    r = requests.get(
        f"{FRONTEND_URL}/applications",
        headers=_headers(access_token),
        params={"user_id": user_id}
    )
    assert r.status == 200, f"Fetching applications failed: {r.status_code} {r.text}"
    assert r.json().get("id") == user_id, f"Fetching application returned invalid user id: {r.status_code} {r.text}"
    assert r.json().get(
        "application_status") in APPLICATION_STATUSES, f"Fetching applications returned invalid application status: {r.status_code} {r.text}"

    return ApplicationContext(
        id=int(r.json().get("id")),
        user_id=r.json().get("user_id"),
        license_type=r.json().get("license_type"),
        cadastral_reference=r.json().get("cadastral_reference"),
        applied_at=r.json().get("applied_at"),
        changed_at=r.json().get("changed_at"),
        application_status=r.json().get("status"),
        remarks=r.json().get("remarks"),
    )


def update_application(application: ApplicationContext, new_application_status: str, new_cadastral_reference: str,
                       new_remarks: str, new_license_type: str, access_token: str) -> ApplicationContext:
    r = requests.put(
        f"{FRONTEND_URL}/applications/{application.id}",
        headers=_headers(access_token),
        json={
            "application_status": new_application_status,
            "cadastral_reference": new_cadastral_reference,
            "remarks": new_remarks,
            "license_type": new_license_type,
        },
    )
    assert r.status == 200, f"Application update failed: {r.status_code} {r.text}"
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

    return application


def delete_application(application_id: str, access_token: str) -> None:
    r = requests.delete(
        f"{FRONTEND_URL}/applications/{application_id}",
        headers=_headers(access_token),
        params={"application_id": application_id},
    )
    assert r.status == 204, f"Application deletion failed: {r.status_code} {r.text}"
    assert r.text, f"Application deletion returned no response: {r.status_code} {r.text}"


############################################################################
# License functions
############################################################################

def get_licenses(user_id: str, access_token: str) -> list[LicenseContext]:
    r = requests.get(
        f"{FRONTEND_URL}/licenses",
        headers=_headers(access_token),
        params={"user_id": user_id},
    )
    assert r.status == 200, f"Fetching licenses failed: {r.status_code} {r.text}"
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
    return licenses


def get_license_by_id(license_id: int, access_token: str) -> LicenseContext:
    r = requests.get(
        f"{FRONTEND_URL}/licenses/{license_id}",
        headers=_headers(access_token),
        params={"license_id": license_id},
    )
    assert r.status == 200, f"Fetching license by id failed: {r.status_code} {r.text}"
    assert r.json().get(
        "id") == license_id, f"Fetching license by id returned invalid license id: {r.status_code} {r.text}"
    assert r.json().get(
        "license_status") in LICENSE_STATUSES, f"Fetching license returned invalid license status: {r.status_code} {r.text}"

    return LicenseContext(
        id=int(r.json().get("id")),
        user_id=r.json().get("user_id"),
        application_id=int(r.json().get("application_id")),
        license_type=r.json().get("license_type"),
        license_status=r.json().get("status"),
        issued_at=r.json().get("issued_at"),
        expires_at=r.json().get("expiry_at"),
        cadastral_reference=r.json().get("cadastral_reference"),
    )


def update_license(license: LicenseContext, new_license_status: str, access_token: str) -> LicenseContext:
    r = requests.patch(
        f"{FRONTEND_URL}/licenses/{license.id}",
        headers=_headers(access_token),
        params={"license_id": license.id},
        json={
            "license_status": new_license_status,
        },
    )
    assert r.status == 200, f"License status update failed: {r.status_code} {r.text}"
    assert r.json().get(
        "id") == license.id, f"License status update returned invalid license id: {r.status_code} {r.text}"
    assert r.json().get(
        "status") == new_license_status, f"License status update returned invalid license status: {r.status_code} {r.text}"
    license.license_status = new_license_status

    return license


def delete_license(license_id: int, access_token: str) -> None:
    r = requests.delete(
        f"{FRONTEND_URL}/licenses/{license_id}",
        headers=_headers(access_token),
        params={"license_id": license_id},
    )
    assert r.status == 204, f"License deletion failed: {r.status_code} {r.text}"
    assert r.json().get("description"), f"License deletion returned no description: {r.status_code} {r.text}"


############################################################################
# Document verification functions
############################################################################

def get_document_verification_status(application: ApplicationContext, access_token: str) -> tuple[
    ApplicationContext, requests.Response]:
    r = requests.get(
        f"{FRONTEND_URL}/applications/{application.id}/documents",
        headers=_headers(access_token),
        params={"application_id": application.id},
    )
    assert r.status == 200, f"Document status fetch failed: {r.status_code} {r.text}"
    assert application.id == r.json().get(
        "application_id"), f"Document status fetch returned invalid application id: {r.status_code} {r.text}"
    assert r.json().get(
        "status") in DOCUMENT_STATUSES, f"Document status fetch returned invalid status: {r.status_code} {r.text}"
    application.status = r.json().get("status")
    application.rejection_reason = r.json().get("rejection_reason")

    return application, r


# TODO: FIX API (duplicate of get_document_verification_status)
def get_document_status(application: ApplicationContext, access_token: str) -> ApplicationContext:
    r = requests.get(
        f"{FRONTEND_URL}/applications/{application.id}/documents",
        headers=_headers(access_token),
        params={"application_id": application.id},
    )
    assert r.status == 200, f"Document status fetch failed: {r.status_code} {r.text}"
    assert application.id == r.json().get(
        "application_id"), f"Document status fetch returned invalid application id: {r.status_code} {r.text}"
    assert r.json().get(
        "status") in DOCUMENT_STATUSES, f"Document status fetch returned invalid status: {r.status_code} {r.text}"
    application.status = r.json().get("status")
    application.rejection_reason = r.json().get("rejection_reason")

    return application


############################################################################
# Payment functions
############################################################################

def get_payments(application_id: str, access_token: str) -> list[PaymentContext]:
    r = requests.get(
        f"{FRONTEND_URL}/applications/{application_id}/payments",
        headers=_headers(access_token),
        params={"application_id": application_id},
    )
    assert r.status == 200, f"Fetching payments failed: {r.status_code} {r.text}"
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
    return payments


def create_payment(application_id: int, payment: PaymentContext, access_token: str) -> PaymentContext:
    r = requests.post(
        f"{FRONTEND_URL}/applications/{application_id}/payments",
        headers=_headers(access_token),
        json={
            "application_id": application_id,
            "name": payment.name,
            "iban": payment.iban,
            "bic": payment.bic,
        },
    )
    assert r.status == 201, f"Payment creation failed: {r.status_code} {r.text}"
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

    return payment


def get_application_fee(application: ApplicationContext, access_token: str) -> float:
    r = requests.get(
        f"{FRONTEND_URL}/fees/{application.id}",
        headers=_headers(access_token),
        params={"application_id": application.id},
    )
    assert r.status == 200, f"Fetching application fees failed: {r.status_code} {r.text}"
    assert application.id == r.json().get(
        "application_id"), f"Fetching application fees returned invalid application id: {r.status_code} {r.text}"
    application_fee = r.json().get("application_fee")
    assert application_fee is LICENSE_MAP[
        application.license_type].price, f"Fetching application fees returned invalid amount: {application_fee}"

    return application_fee

'''
'''
############################################################################
# Consent functions
############################################################################

# TODO: FIX API (no pram)
def get_consents(user_id: str, access_token: str) -> list[ConsentContext]:
    r = requests.get(
        f"{FRONTEND_URL}/consents",
        headers=_headers(access_token),
        #params={"user_id": user_id},
    )
    assert r.status == 200, f"Fetching consents failed: {r.status_code} {r.text}"
    assert isinstance(r.json(), list), f"Fetching consents returned invalid data structure: {r.status_code} {r.text}"

    consents: list[ConsentContext] = []
    for item in r.json():
        #assert item.get("user_id") == user_id, f"Fetching consents returned invalid user id: {r.status_code} {r.text}"
        consents.append(
            ConsentContext(
                id=item["id"],
                user_id=item["user_id"],
                purpose=item["purpose"],
                granted=bool(item["granted"]),
                granted_at=item["granted_at"],
                withdrawn_at=item["withdrawn_at"],
            )
        )
    return consents

def create_consent(user_id: str, consent: ConsentContext, access_token: str) -> ConsentContext:
    r = requests.post(
        f"{FRONTEND_URL}/consents",
        headers=_headers(access_token),
        json={
            "user_id": user_id,
            "purpose": consent.purpose,
            "granted": consent.granted,
        },
    )
    assert r.status == 201, f"Consent creation failed: {r.status_code} {r.text}"
    assert user_id == r.json().get(
        "user_id"), f"Consent creation returned invalid user id: {r.status_code} {r.text}"
    assert consent.purpose == r.json().get(
        "purpose"), f"Consent creation returned invalid purpose: {r.status_code} {r.text}"
    assert consent.granted == r.json().get(
        "granted"), f"Consent creation returned invalid granted status: {r.status_code} {r.text}"
    consent.id = r.json().get("id")
    consent.user_id = user_id
    consent.granted_at = r.json().get("granted_at")
    consent.withdrawn_at = r.json().get("withdrawn_at")

    return consent
'''
'''

############################################################################
# Ballot period functions
############################################################################

def get_ballot_periods(access_token: str) -> list[BallotPeriodContext]:
    r = requests.get(
        f"{FRONTEND_URL}/ballot-periods",
        headers=_headers(access_token),
    )
    assert r.status == 200, f"Fetching ballot periods failed: {r.status_code} {r.text}"
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
    return ballot_periods

'''


def create_ballot_period(page, start_date: str, end_date: str) -> BallotPeriodContext:
    page.goto(f"{FRONTEND_URL}/")
    page.locator("#handleCreateBallotClick").click()
    page.wait_for_url("/ballot-config")
    page.locator("#ballot_start_date").fill(start_date)
    page.locator("#ballot_end_date").fill(end_date)

    with page.expect_response(
            lambda res: res.request.method == "POST" and re.search(r"/ballot-config$", res.url)
    ) as res_info:
        page.locator("#createBallotPeriodBtn").click()
    r = res_info.value
    assert r.status == 201, f"Ballot period creation failed: {r.status_code} {r.text}"
    assert r.json().get("id"), f"Ballot period creation returned no id: {r.status_code} {r.text}"

    page.wait_for_url("/login")
    return BallotPeriodContext(
        ballot_period_id=int(r.json().get("id")),
        start_date=r.json().get("start_date"),
        end_date=r.json().get("end_date"),
        total_applications=int(r.json().get("total_applications")),
    )


'''

def get_current_ballot_period(access_token: str) -> BallotPeriodContext:
    r = requests.get(
        f"{FRONTEND_URL}/ballot-periods/current",
        headers=_headers(access_token),
    )
    assert r.status == 200, f"Fetching current ballot period failed: {r.status_code} {r.text}"

    return BallotPeriodContext(
        start_date=r.json().get("start_date"),
        end_date=r.json().get("end_date"),
    )


def get_ballot_period(ballot_period_id: int, access_token: str) -> BallotPeriodContext:
    r = requests.get(
        f"{FRONTEND_URL}/ballot-periods/{ballot_period_id}",
        headers=_headers(access_token),
        params={"ballot_period_id": ballot_period_id},
    )
    assert r.status == 200, f"Fetching ballot period by id failed: {r.status_code} {r.text}"

    return BallotPeriodContext(
        ballot_period_id=int(r.json().get("id")),
        start_date=r.json().get("start_date"),
        end_date=r.json().get("end_date"),
        total_applications=int(r.json().get("total_applications")),
    )


def get_ballot_period_entries(ballot_period_id: int, access_token: str) -> list[ApplicationContext]:
    r = requests.get(
        f"{FRONTEND_URL}/ballot-periods/{ballot_period_id}/entries",
        headers=_headers(access_token),
        params={"period_id": ballot_period_id},
    )
    assert r.status == 200, f"Fetching ballot period entries failed: {r.status_code} {r.text}"
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
    return applications


def create_lottery(ballot_period_id: int, license_count: int, license_type: str, access_token: str) -> tuple[
    list[ApplicationContext], list[ApplicationContext]]:
    json: dict[str, str] = {}
    if license_type and license_count > 0:
        json["license_type"] = license_type
    r = requests.post(
        f"{FRONTEND_URL}/ballot-periods/{ballot_period_id}/draw",
        headers=_headers(access_token),
        params={"period_id": ballot_period_id},
        json=json
    )
    assert r.status == 200, f"Lottery creation failed: {r.status_code} {r.text}"
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

    return selected_applications, rejected_applications


def create_application_documents(application_id: int, access_token: str) -> None:
    file = _read_file(filepath=TEST_PDF_PATH)
    r = requests.post(
        f"{FRONTEND_URL}/process-document",
        headers=_headers(access_token),
        params={"application_id": application_id},
        files=[
            ("id_file", ("sample_id.pdf", file, "application/pdf")),
            ("proof_file", ("sample_proof.pdf", file, "application/pdf")),
        ],
    )
    assert r.status == 200, f"Document upload failed: {r.status_code} {r.text}"
    assert application_id == r.json().get(
        "application_id"), f"Document upload returned invalid application id: {r.status_code} {r.text}"


############################################################################
# Synthetic functions
############################################################################

def wait_for_document_verification(
        application: ApplicationContext,
        access_token: str,
        timeout_seconds: int = 120,
        initial_interval: float = 0.2,
        max_interval: float = 2.0,
) -> ApplicationContext:
    deadline = time.monotonic() + timeout_seconds
    interval = initial_interval

    r = None
    while time.monotonic() < deadline:
        application, r = get_document_verification_status(application, access_token)

        if application.application_status != DOCUMENT_STATUSES.PENDING:
            return application

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
# Language functions (incl localStorage)
############################################################################

############################################################################
# Helper functions
############################################################################

def update_application_status(application: ApplicationContext, access_token: str) -> ApplicationContext:
    r = requests.get(
        f"{FRONTEND_URL}/applications/{application.id}",
        headers=_headers(access_token),
        data={"application_id": application.id},
    )
    assert r.status == 200, f"Fetching application status failed: {r.status_code} {r.text}"
    assert application.id == r.json().get(
        "application_id"), f"Fetching application status returned invalid application id: {r.status_code} {r.text}"
    application.status = r.json().get("status")
    return application
'''


def verify_local_storage_after_login(page, json_response: dict) -> None:
    access_token, refresh_token, expires_in, refresh_expires_in, token_type, is_admin = get_auth_details_from_local_storage(
        page)
    assert access_token == json_response.get(
        "access_token"), f"Access token in local storage does not match login response"
    assert refresh_token == json_response.get(
        "refresh_token"), f"Refresh token in local storage does not match login response"
    assert expires_in == int(
        json_response.get("expires_in")), f"Expires in in local storage does not match login response"
    assert refresh_expires_in == int(
        json_response.get("refresh_expires_in")), f"Refresh expires in in local storage does not match login response"
    assert token_type == json_response.get("token_type"), f"Token type in local storage does not match login response"
    assert is_admin == bool(json_response.get("is_admin")), f"Is admin in local storage does not match login response"


def get_auth_details_from_local_storage(page) -> tuple[
    str | None, str | None, int | None, int | None, str | None, bool | None
]:
    access_token = page.evaluate("(k) => window.localStorage.getItem(k)", ACCESS_TOKEN_KEY)
    refresh_token = page.evaluate("(k) => window.localStorage.getItem(k)", REFRESH_TOKEN_KEY)
    access_exp = page.evaluate(
        "(k) => { const v = window.localStorage.getItem(k); const n = parseInt(v ?? '', 10); return Number.isFinite(n) ? n : null; }",
        ACCESS_EXP_KEY, )
    refresh_exp = page.evaluate(
        "(k) => { const v = window.localStorage.getItem(k); const n = parseInt(v ?? '', 10); return Number.isFinite(n) ? n : null; }",
        REFRESH_EXP_KEY, )
    token_type = page.evaluate("(k) => window.localStorage.getItem(k)", TOKEN_TYPE_KEY)
    is_admin = page.evaluate("(k) => window.localStorage.getItem(k) === 'admin'", ROLE_KEY, )

    return access_token, refresh_token, access_exp, refresh_exp, token_type, is_admin
