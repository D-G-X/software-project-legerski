import random
import time

import requests
from playwright.sync_api import Page, FilePayload

from testkit.config import FRONTEND_URL, BACKEND_URL, ACCESS_TOKEN_KEY, REFRESH_TOKEN_KEY, ACCESS_EXP_KEY, \
    REFRESH_EXP_KEY, TOKEN_TYPE_KEY, ROLE_KEY, TEST_PDF_PATH, DOCUMENT_STATUSES, PAYMENT_STATUSES, LANGUAGE_KEY, \
    LICENSE_MAP
from testkit.models import UserContext, BallotPeriodContext, ApplicationContext, PaymentContext
from testkit.utils import decode_jwt, headers, read_file


############################################################################
# Authentication functions
############################################################################


def ui_register(page, user: UserContext) -> None:
    if not page.url == f"{FRONTEND_URL}/":
        page.goto(f"{FRONTEND_URL}/")
    page.locator("#registerLink").click()
    page.wait_for_url(f"{FRONTEND_URL}/register")

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
    assert r.status == 200, f"Registration failed: {r.status} {r.text()}"

    data = r.json() if r.headers.get("content-type", "").startswith("application/json") else {}
    user_id = data.get("user_id") or data.get("id") or data.get("userId")
    assert user_id, f"Registration returned no user id: {r.status} {r.text()}"
    user.id = user_id

    page.wait_for_url(f"{FRONTEND_URL}/login")


# TODO: add keep logged in
import re


def ui_login(page, user: UserContext) -> None:
    if not page.url == f"{FRONTEND_URL}/login":
        page.goto(f"{FRONTEND_URL}/")
        page.locator("#signInLink").click()
        page.wait_for_url(f"{FRONTEND_URL}/login")

    page.locator("#email").fill(user.email)
    page.locator("#password").fill(user.password)

    page.locator("#keepMeLoggedIn").set_checked(random.choice([True, False]))

    with page.expect_response(
            lambda res: res.request.method == "POST" and re.search(r"/login$", res.url)
    ) as res_info:
        page.locator("#loginButton").click()

    r = res_info.value
    assert r.status == 200, f"Login failed: {r.status} {r.text()}"

    data = r.json()
    assert "access_token" in data, f"Login returned no access token: {r.status} {r.text()}"

    user.access_token = data.get("access_token")
    user.refresh_token = data.get("refresh_token")
    user.expires_in = int(data.get("expires_in")) if data.get("expires_in") is not None else None
    user.refresh_expires_in = int(data.get("refresh_expires_in")) if data.get(
        "refresh_expires_in") is not None else None
    user.token_type = data.get("token_type")
    user.is_admin = bool(data.get("is_admin"))

    if not user.is_admin:
        assert user.id == decode_jwt(user.access_token)[
            "sub"], f"Login returned invalid user id: {r.status} {r.text()}"

    _verify_local_storage_after_login(page, data)

    page.wait_for_url(f"{FRONTEND_URL}/")


def ui_login_to_register(page) -> None:
    if not page.url == f"{FRONTEND_URL}/login":
        page.goto(f"{FRONTEND_URL}/")
        page.locator("#signInLink").click()
        page.wait_for_url(f"{FRONTEND_URL}/login")

    page.locator("#createAccountLink").click()

    page.wait_for_url(f"{FRONTEND_URL}/register")


def ui_request_reset_password(page, user_email: str) -> None:
    if not page.url == f"{FRONTEND_URL}/login":
        page.locator("#signInLink").click()
        page.wait_for_url("/login")

    page.locator("#forgotPasswortLink").click()

    page.wait_for_url("/forgot-password")
    page.locator("#email").fill(user_email)

    page.locator("#resetPasswordBtn").wait_for(state="visible")
    with page.expect_response(
            lambda res: res.request.method == "POST" and re.search(r"/reset-password$", res.url)
    ) as res_info:
        page.locator("#resetPasswordBtn").click()
    r = res_info.value
    assert r.status == 200, f"Password reset request failed: {r.status} {r.text()}"

    page.locator("#returnLinkRequestSent").wait_for(state="visible")
    page.locator("#returnLinkRequestSent").click()
    page.wait_for_url("/login")


'''
def ui_reset_password(user: UserContext, new_password: str) None:
    r = requests.post(
        f"{FRONTEND_URL}/change-password",
        json={
            "token": "",  # TODO: get token from email
            "new_password": new_password,
        },
    )
    assert r.status == 200, f"Password reset failed: {r.status} {r.text()}"
    user.password = new_password

    return user


# TODO: FIX API
def ui_change_password(user: UserContext, new_password: str) None:
    r = requests.patch(
        f"{FRONTEND_URL}/users/{user.id}/change-password",
        headers=_headers(user.access_token),
        params={"user_id": user.id},
        json={
            "old_password": user.password,
            "new_password": new_password,
        },
    )
    assert r.status == 201, f"Password change request failed: {r.status} {r.text()}"
    assert r.json().get("description"), f"Password change request returned no description: {r.status} {r.text()}"
    user.password = new_password

    return user


# TODO: FIX API
def ui_change_name(user: UserContext, new_firstname: str, new_lastname: str) None:
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
    assert r.status == 204, f"User name change request failed: {r.status} {r.text()}"
    assert r.json().get("description"), f"User name change request returned no description: {r.status} {r.text()}"
    user.firstname = new_firstname
    user.lastname = new_lastname

    return user

'''


def ui_logout(page, user_or_admin: UserContext) -> None:
    if not page.url == f"{FRONTEND_URL}/":
        page.goto(f"{FRONTEND_URL}/")
    page.locator("#signOutBtn").click()

    user_or_admin.access_token = ""
    user_or_admin.refresh_token = ""
    user_or_admin.expires_in = 0
    user_or_admin.refresh_expires_in = 0
    user_or_admin.token_type = ""
    user_or_admin.is_admin = False

    page.wait_for_url("/login")


'''

############################################################################
# User functions
############################################################################

def ui_get_users(user_name: str, email: str, first: int, max: int, access_token: str) -> list[UserContext]:
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
    assert r.status == 200, f"Fetching users failed: {r.status} {r.text()}"
    assert isinstance(r.json(), list), f"Fetching users returned invalid data structure: {r.status} {r.text()}"

    users: list[UserContext] = []
    for item in r.json():
        assert item.get("id"), f"Fetching users returned invalid user id: {r.status} {r.text()}"
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
'''


def ui_view_user_details(page: Page) -> None:
    if not page.url == f"{FRONTEND_URL}/":
        page.goto(f"{FRONTEND_URL}/")
    page.locator("#profileLink").click()
    page.wait_for_url(f"{FRONTEND_URL}/profile")
    page.locator("#deleteAccBtn").wait_for(state="visible")

    page.locator("#homeLink").click()
    page.wait_for_url(f"{FRONTEND_URL}/")


def ui_edit_user_details(page: Page) -> None:
    if not page.url == f"{FRONTEND_URL}/":
        page.goto(f"{FRONTEND_URL}/")
    page.locator("#profileLink").click()
    page.wait_for_url(f"{FRONTEND_URL}/profile")
    page.locator("#deleteAccBtn").wait_for(state="visible")

    page.locator("#homeLink").click()
    page.wait_for_url(f"{FRONTEND_URL}/")

'''
def ui_create_user(user: UserContext, access_token: str) -> None:
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
    assert r.status == 201, f"User creation failed: {r.status} {r.text()}"
    assert r.json().get("id") == user.id, f"User creation returned invalid user id: {r.status} {r.text()}"


def ui_update_user(user: UserContext, access_token: str) -> None:
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
    assert r.status == 204, f"User update failed: {r.status} {r.text()}"
    assert r.text(), f"User update returned no response: {r.status} {r.text()}"


def ui_delete_user(user_id: str, access_token: str) -> None:
    r = requests.delete(
        f"{FRONTEND_URL}/users/{user_id}",
        headers=_headers(access_token),
        params={"user_id": user_id},
    )
    assert r.status == 204, f"User deletion failed: {r.status} {r.text()}"
    assert r.text(), f"User deletion returned no response: {r.status} {r.text()}"


def ui_get_user_notifications(user_id: str, access_token: str) -> list[NotificationContext]:
    r = requests.get(
        f"{FRONTEND_URL}/users/{user_id}/notifications",
        headers=_headers(access_token),
        params={"user_id": user_id},
    )
    assert r.status == 200, f"Fetching notifications failed: {r.status} {r.text()}"
    assert isinstance(r.json(),
                      list), f"Fetching notifications returned invalid data structure: {r.status} {r.text()}"

    notifications: list[NotificationContext] = []
    for item in r.json():
        assert item.get(
            "user_id") == user_id, f"Fetching notifications returned invalid user id: {r.status} {r.text()}"
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
def ui_update_user_notification_as_read(notification_id: int, access_token: str) -> None:
    r = requests.patch(
        f"{FRONTEND_URL}/users/notifications/{notification_id}",
        headers=_headers(access_token),
        params={"id": notification_id},
    )
    assert r.status == 200, f"Updating notification as read failed: {r.status} {r.text()}"
    assert r.text(), f"Updating notification as read returned no response: {r.status} {r.text()}"


def ui_get_user_notification_preferences(user: UserContext, access_token: str) None:
    r = requests.get(
        f"{FRONTEND_URL}/users/{user.id}/notifications/preferences",
        headers=_headers(access_token),
        params={"user_id": user.id},
    )
    assert r.status == 200, f"Fetching notification preferences failed: {r.status} {r.text()}"
    assert r.json().get(
        "id") == user.id, f"Fetching notification preferences returned invalid user id: {r.status} {r.text()}"
    user.notification_way = r.json().get("notification_way")
    user.application_updates_notification = bool(r.json().get("application_updates_notification"))
    user.license_renewal_notification = bool(r.json().get("license_renewal_notification"))

    return user


def ui_update_user_notification_preferences(
        user: UserContext,
        new_notification_way: str,
        new_application_updates_notification: bool,
        new_license_renewal_notification: bool,
        access_token: str,
) None:
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
    assert r.status == 200, f"Updating notification preferences failed: {r.status} {r.text()}"
    assert r.text(), f"Updating notification preferences returned no response: {r.status} {r.text()}"
    assert isinstance(r.json(),
                      dict), f"Updating notification preferences returned invalid data structure: {r.status} {r.text()}"
    assert r.json().get(
        "notification_way") == new_notification_way, f"Updating notification preferences returned invalid notification way: {r.status} {r.text()}"
    assert r.json().get(
        "application_updates_notification") == new_application_updates_notification, f"Updating notification preferences returned invalid application updates notification: {r.status} {r.text()}"
    assert r.json().get(
        "license_renewal_notification") == new_license_renewal_notification, f"Updating notification preferences returned invalid license renewal notification: {r.status} {r.text()}"

    user.notification_way = new_notification_way
    user.application_updates_notification = new_application_updates_notification
    user.license_renewal_notification = new_license_renewal_notification
    return user


############################################################################
# Application functions
############################################################################

def ui_get_applications(user_id: str, access_token: str) -> list[ApplicationContext]:
    params: dict[str, str] = {}
    if user_id:
        params["user_id"] = user_id
    r = requests.get(
        f"{FRONTEND_URL}/applications",
        headers=_headers(access_token),
        params=params,
    )
    assert r.status == 200, f"Fetching applications failed: {r.status} {r.text()}"
    assert isinstance(r.json(),
                      list), f"Fetching applications returned invalid data structure: {r.status} {r.text()}"

    applications: list[ApplicationContext] = []
    for item in r.json():
        if user_id:
            assert item.get(
                "user_id") == user_id, f"Fetching applications returned invalid user id: {r.status} {r.text()}"
            assert item.get(
                "application_status") in APPLICATION_STATUSES, f"Fetching applications returned invalid application status: {r.status} {r.text()}"
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

'''


def ui_create_application_draft(page: Page, application: ApplicationContext, user: UserContext) -> None:
    if not page.url == f"{FRONTEND_URL}/":
        page.goto(f"{FRONTEND_URL}/")
    page.locator("#createNewApplicationBtn").click()
    page.wait_for_url(f"{FRONTEND_URL}/license-application-request")

    page.locator("#cadastral_number").fill(application.cadastral_reference)
    page.locator(f'#rental_license_type_{application.license_type}').check()
    page.locator("#additional_comments").fill(application.remarks)
    page.locator("#consent_legal_data").check()
    page.locator("#consent_personal_data").check()

    with page.expect_response(
            lambda res: res.request.method == "POST" and re.search(r"/applications$", res.url)
    ) as res_info:
        page.locator("#saveDraftBtn").click()
    r = res_info.value

    assert r.status == 201, f"Application request failed: {r.status} {r.text()}"
    assert user.id == r.json().get("user_id"), f"Application request returned invalid user id: {r.status} {r.text()}"
    application.id = r.json().get("id")
    application.applied_at = r.json().get("applied_at")
    application.changed_at = r.json().get("changed_at")

    page.wait_for_url(f"{FRONTEND_URL}/")


def ui_create_application(page: Page, application: ApplicationContext, user: UserContext) -> None:
    if not page.url == f"{FRONTEND_URL}/":
        page.goto(f"{FRONTEND_URL}/")
    page.locator("#createNewApplicationBtn").click()
    page.wait_for_url(f"{FRONTEND_URL}/license-application-request")

    page.locator("#cadastral_number").fill(application.cadastral_reference)
    page.locator(f'#rental_license_type_{application.license_type}').check()
    page.locator("#additional_comments").fill(application.remarks)
    page.locator("#consent_legal_data").check()
    page.locator("#consent_personal_data").check()

    with page.expect_response(
            lambda res: res.request.method == "POST" and re.search(r"/applications$", res.url)
    ) as res_info:
        page.locator("#submitApplicationBtn").click()
    r = res_info.value

    assert r.status == 201, f"Application request failed: {r.status} {r.text()}"
    assert user.id == r.json().get("user_id"), f"Application request returned invalid user id: {r.status} {r.text()}"
    application.id = r.json().get("id")
    application.applied_at = r.json().get("applied_at")
    application.changed_at = r.json().get("changed_at")

    page.wait_for_url(f"{FRONTEND_URL}/license-document-upload/{application.id}")


def ui_edit_application(page: Page, application: ApplicationContext, user: UserContext) -> None:
    if not page.url == f"/license-application-request/edit/{application.id}":
        page.goto(f"/license-application-request/edit/{application.id}")
    page.locator("#cadastral_number").fill(application.cadastral_reference)
    page.locator(f'#rental_license_type_{application.license_type}').check()
    page.locator("#additional_comments").fill(application.remarks)
    page.locator("#consent_legal_data").check()
    page.locator("#consent_personal_data").check()

    with page.expect_response(
            lambda res: res.request.method == "PATCH" and re.search(rf"/applications/{application.id}$", res.url)
    ) as res_info:
        page.locator("#submitApplicationBtn").click()
    r = res_info.value

    assert r.status == 200, f"Application request failed: {r.status} {r.text()}"
    assert user.id == r.json().get("user_id"), f"Application request returned invalid user id: {r.status} {r.text()}"
    application.id = r.json().get("id")
    application.applied_at = r.json().get("applied_at")
    application.changed_at = r.json().get("changed_at")

    page.wait_for_url(f"{FRONTEND_URL}/license-document-upload/{application.id}")


'''
def ui_get_application_by_id(user_id: str, access_token: str) -> ApplicationContext:
    r = requests.get(
        f"{FRONTEND_URL}/applications",
        headers=_headers(access_token),
        params={"user_id": user_id}
    )
    assert r.status == 200, f"Fetching applications failed: {r.status} {r.text()}"
    assert r.json().get("id") == user_id, f"Fetching application returned invalid user id: {r.status} {r.text()}"
    assert r.json().get(
        "application_status") in APPLICATION_STATUSES, f"Fetching applications returned invalid application status: {r.status} {r.text()}"

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


def ui_update_application(application: ApplicationContext, new_application_status: str, new_cadastral_reference: str,
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
    assert r.status == 200, f"Application update failed: {r.status} {r.text()}"
    assert application.id == r.json().get(
        "id"), f"Application update returned invalid application id: {r.status} {r.text()}"
    assert isinstance(r.json(), dict), f"Application update returned invalid data structure: {r.status} {r.text()}"
    assert r.json().get(
        "application_status") == new_application_status, f"Application update returned invalid application status: {r.status} {r.text()}"
    assert r.json().get(
        "cadastral_reference") == new_cadastral_reference, f"Application update returned invalid cadastral reference: {r.status} {r.text()}"
    assert r.json().get(
        "remarks") == new_remarks, f"Application update returned invalid remarks: {r.status} {r.text()}"
    assert r.json().get(
        "license_type") == new_license_type, f"Application update returned invalid license type: {r.status} {r.text()}"
    assert r.json().get("applied_at") == application.applied_at

    application.application_status = new_application_status
    application.cadastral_reference = new_cadastral_reference
    application.remarks = new_remarks
    application.license_type = new_license_type
    application.changed_at = r.json().get("changed_at")

    return application


def ui_delete_application(application_id: str, access_token: str) -> None:
    r = requests.delete(
        f"{FRONTEND_URL}/applications/{application_id}",
        headers=_headers(access_token),
        params={"application_id": application_id},
    )
    assert r.status == 204, f"Application deletion failed: {r.status} {r.text()}"
    assert r.text(), f"Application deletion returned no response: {r.status} {r.text()}"


'''


def ui_view_application_details(page: Page, application_id: str):
    if not page.url == f"{FRONTEND_URL}/":
        page.goto(f"{FRONTEND_URL}/")
    if page.locator("#next-page-button").is_enabled():
        page.locator("#next-page-button").click()

    page.locator(f"#viewDetailsBtn-{application_id}").click()

    page.locator("#closeButton").wait_for(state="visible")
    page.locator(f"#closeButton").click()

    page.locator(f"#viewDetailsBtn-{application_id}").wait_for(state="visible")
    if page.locator("#previous-page-button").is_enabled():
        page.locator("#previous-page-button").click()


def ui_view_application_details_edit(page: Page, application_id: str):
    if not page.url == f"{FRONTEND_URL}/":
        page.goto(f"{FRONTEND_URL}/")
    if page.locator("#next-page-button").is_enabled():
        page.locator("#next-page-button").click()

    page.locator(f"#viewDetailsBtn-{application_id}").click()

    page.locator("#editBtn").wait_for(state="visible")
    page.locator(f"#editBtn").click()
    page.wait_for_url(f"{FRONTEND_URL}/license-application-request/edit/{application_id}")


'''
############################################################################
# License functions
############################################################################

def ui_get_licenses(user_id: str, access_token: str) -> list[LicenseContext]:
    r = requests.get(
        f"{FRONTEND_URL}/licenses",
        headers=_headers(access_token),
        params={"user_id": user_id},
    )
    assert r.status == 200, f"Fetching licenses failed: {r.status} {r.text()}"
    assert isinstance(r.json(), list), f"Fetching licenses returned invalid data structure: {r.status} {r.text()}"

    licenses: list[LicenseContext] = []
    for item in r.json():
        assert item.get("user_id") == user_id, f"Fetching licenses returned invalid user id: {r.status} {r.text()}"
        assert item.get(
            "license_status") in LICENSE_STATUSES, f"Fetching license returned invalid license status: {r.status} {r.text()}"
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


def ui_get_license_by_id(license_id: int, access_token: str) -> LicenseContext:
    r = requests.get(
        f"{FRONTEND_URL}/licenses/{license_id}",
        headers=_headers(access_token),
        params={"license_id": license_id},
    )
    assert r.status == 200, f"Fetching license by id failed: {r.status} {r.text()}"
    assert r.json().get(
        "id") == license_id, f"Fetching license by id returned invalid license id: {r.status} {r.text()}"
    assert r.json().get(
        "license_status") in LICENSE_STATUSES, f"Fetching license returned invalid license status: {r.status} {r.text()}"

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


def ui_update_license(license: LicenseContext, new_license_status: str, access_token: str) -> LicenseContext:
    r = requests.patch(
        f"{FRONTEND_URL}/licenses/{license.id}",
        headers=_headers(access_token),
        params={"license_id": license.id},
        json={
            "license_status": new_license_status,
        },
    )
    assert r.status == 200, f"License status update failed: {r.status} {r.text()}"
    assert r.json().get(
        "id") == license.id, f"License status update returned invalid license id: {r.status} {r.text()}"
    assert r.json().get(
        "status") == new_license_status, f"License status update returned invalid license status: {r.status} {r.text()}"
    license.license_status = new_license_status

    return license


def ui_delete_license(license_id: int, access_token: str) -> None:
    r = requests.delete(
        f"{FRONTEND_URL}/licenses/{license_id}",
        headers=_headers(access_token),
        params={"license_id": license_id},
    )
    assert r.status == 204, f"License deletion failed: {r.status} {r.text()}"
    assert r.json().get("description"), f"License deletion returned no description: {r.status} {r.text()}"


############################################################################
# Document verification functions
############################################################################

def ui_get_document_verification_status(application: ApplicationContext, access_token: str) -> tuple[
    ApplicationContext, requests.Response]:
    r = requests.get(
        f"{FRONTEND_URL}/applications/{application.id}/documents",
        headers=_headers(access_token),
        params={"application_id": application.id},
    )
    assert r.status == 200, f"Document status fetch failed: {r.status} {r.text()}"
    assert application.id == r.json().get(
        "application_id"), f"Document status fetch returned invalid application id: {r.status} {r.text()}"
    assert r.json().get(
        "status") in DOCUMENT_STATUSES, f"Document status fetch returned invalid status: {r.status} {r.text()}"
    application.status = r.json().get("status")
    application.rejection_reason = r.json().get("rejection_reason")

    return application, r


# TODO: FIX API (duplicate of get_document_verification_status)
def ui_get_document_status(application: ApplicationContext, access_token: str) -> ApplicationContext:
    r = requests.get(
        f"{FRONTEND_URL}/applications/{application.id}/documents",
        headers=_headers(access_token),
        params={"application_id": application.id},
    )
    assert r.status == 200, f"Document status fetch failed: {r.status} {r.text()}"
    assert application.id == r.json().get(
        "application_id"), f"Document status fetch returned invalid application id: {r.status} {r.text()}"
    assert r.json().get(
        "status") in DOCUMENT_STATUSES, f"Document status fetch returned invalid status: {r.status} {r.text()}"
    application.status = r.json().get("status")
    application.rejection_reason = r.json().get("rejection_reason")

    return application


############################################################################
# Payment functions
############################################################################

def ui_get_payments(application_id: str, access_token: str) -> list[PaymentContext]:
    r = requests.get(
        f"{FRONTEND_URL}/applications/{application_id}/payments",
        headers=_headers(access_token),
        params={"application_id": application_id},
    )
    assert r.status == 200, f"Fetching payments failed: {r.status} {r.text()}"
    assert isinstance(r.json(),
                      list), f"Fetching payments returned invalid data structure: {r.status} {r.text()}"

    payments: list[PaymentContext] = []
    for item in r.json():
        assert item.get(
            "application_id") == application_id, f"Fetching payments returned invalid application id: {r.status} {r.text()}"
        assert item.get(
            "payment_status") in PAYMENT_STATUSES, f"Fetching payment returned invalid payment status: {r.status} {r.text()}"
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
'''


def ui_skip_payment(page: Page, user: UserContext, application: ApplicationContext, payment: PaymentContext) -> None:
    if not page.url == f"{FRONTEND_URL}/payment/{application.id}":
        page.goto(f"{FRONTEND_URL}/payment/{application.id}")

    page.locator("#payLaterBtn").click()

    page.wait_for_url(f"{FRONTEND_URL}/")


def ui_create_payment(page: Page, user: UserContext, application: ApplicationContext, payment: PaymentContext) -> None:
    if not page.url == f"{FRONTEND_URL}/payment/{application.id}":
        page.goto(f"{FRONTEND_URL}/payment/{application.id}")

    payment.amount = _get_application_fee(application, user.access_token)

    page.locator("#name").fill(payment.name)
    page.locator("#iban").fill(payment.iban)
    page.locator("#bic").fill(payment.bic)

    page.locator("#showSepaMandateBtn").click()
    page.locator("#acceptButton").click()

    with page.expect_response(
            lambda res: res.request.method == "POST" and re.search(rf"/applications/{application.id}/payments$",
                                                                   res.url)
    ) as res_info:
        page.locator("#payBtn").click()
    r = res_info.value

    assert r.status == 201, f"Payment creation failed: {r.status} {r.text()}"
    assert application.id == r.json().get(
        "application_id"), f"Payment creation returned invalid application id: {r.status} {r.text()}"
    assert payment.amount == r.json().get(
        "amount"), f"Payment creation returned invalid amount: {r.status} {r.text()}"
    assert r.json().get(
        "payment_status") in PAYMENT_STATUSES, f"Payment create returned invalid status: {r.status} {r.text()}"
    payment.id = r.json().get("id")
    payment.application_id = application.id
    payment.payment_date = r.json().get("payment_date")
    payment.payment_status = r.json().get("status")

    page.wait_for_url(f"{FRONTEND_URL}/payment/{application.id}/done")
    page.locator("#returnToDashboardBtn").click()
    page.wait_for_url(f"{FRONTEND_URL}/")


'''
############################################################################
# Consent functions
############################################################################

# TODO: FIX API (no pram)
def ui_get_consents(user_id: str, access_token: str) -> list[ConsentContext]:
    r = requests.get(
        f"{FRONTEND_URL}/consents",
        headers=_headers(access_token),
        #params={"user_id": user_id},
    )
    assert r.status == 200, f"Fetching consents failed: {r.status} {r.text()}"
    assert isinstance(r.json(), list), f"Fetching consents returned invalid data structure: {r.status} {r.text()}"

    consents: list[ConsentContext] = []
    for item in r.json():
        #assert item.get("user_id") == user_id, f"Fetching consents returned invalid user id: {r.status} {r.text()}"
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

def ui_create_consent(user_id: str, consent: ConsentContext, access_token: str) -> ConsentContext:
    r = requests.post(
        f"{FRONTEND_URL}/consents",
        headers=_headers(access_token),
        json={
            "user_id": user_id,
            "purpose": consent.purpose,
            "granted": consent.granted,
        },
    )
    assert r.status == 201, f"Consent creation failed: {r.status} {r.text()}"
    assert user_id == r.json().get(
        "user_id"), f"Consent creation returned invalid user id: {r.status} {r.text()}"
    assert consent.purpose == r.json().get(
        "purpose"), f"Consent creation returned invalid purpose: {r.status} {r.text()}"
    assert consent.granted == r.json().get(
        "granted"), f"Consent creation returned invalid granted status: {r.status} {r.text()}"
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

def ui_get_ballot_periods(access_token: str) -> list[BallotPeriodContext]:
    r = requests.get(
        f"{FRONTEND_URL}/ballot-periods",
        headers=_headers(access_token),
    )
    assert r.status == 200, f"Fetching ballot periods failed: {r.status} {r.text()}"
    assert isinstance(r.json(),
                      list), f"Fetching ballot periods returned invalid data structure: {r.status} {r.text()}"

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


def ui_create_ballot_period(page, start_date: str, end_date: str) -> BallotPeriodContext:
    if not page.url == f"{FRONTEND_URL}/":
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
    assert r.status == 201, f"Ballot period creation failed: {r.status} {r.text()}"
    assert r.json().get("id"), f"Ballot period creation returned no id: {r.status} {r.text()}"

    page.locator("#handleCreateBallotClick").wait_for(state="visible")
    return BallotPeriodContext(
        ballot_period_id=int(r.json().get("id")),
        start_date=r.json().get("start_date"),
        end_date=r.json().get("end_date"),
        total_applications=int(r.json().get("total_applications")),
    )


'''

def ui_get_current_ballot_period(access_token: str) -> BallotPeriodContext:
    r = requests.get(
        f"{FRONTEND_URL}/ballot-periods/current",
        headers=_headers(access_token),
    )
    assert r.status == 200, f"Fetching current ballot period failed: {r.status} {r.text()}"

    return BallotPeriodContext(
        start_date=r.json().get("start_date"),
        end_date=r.json().get("end_date"),
    )


def ui_get_ballot_period(ballot_period_id: int, access_token: str) -> BallotPeriodContext:
    r = requests.get(
        f"{FRONTEND_URL}/ballot-periods/{ballot_period_id}",
        headers=_headers(access_token),
        params={"ballot_period_id": ballot_period_id},
    )
    assert r.status == 200, f"Fetching ballot period by id failed: {r.status} {r.text()}"

    return BallotPeriodContext(
        ballot_period_id=int(r.json().get("id")),
        start_date=r.json().get("start_date"),
        end_date=r.json().get("end_date"),
        total_applications=int(r.json().get("total_applications")),
    )


def ui_get_ballot_period_entries(ballot_period_id: int, access_token: str) -> list[ApplicationContext]:
    r = requests.get(
        f"{FRONTEND_URL}/ballot-periods/{ballot_period_id}/entries",
        headers=_headers(access_token),
        params={"period_id": ballot_period_id},
    )
    assert r.status == 200, f"Fetching ballot period entries failed: {r.status} {r.text()}"
    assert isinstance(r.json(),
                      list), f"Fetching ballot period entries returned invalid data structure: {r.status} {r.text()}"

    applications: list[ApplicationContext] = []
    for item in r.json():
        assert item.get(
            "id") == ballot_period_id, f"Fetching ballot period entries returned invalid ballot period id: {r.status} {r.text()}"
        assert r.json().get(
            "application_status") in APPLICATION_STATUSES, f"Fetching ballot period entries returned invalid application status: {r.status} {r.text()}"
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


def ui_create_lottery(ballot_period_id: int, license_count: int, license_type: str, access_token: str) -> tuple[
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
    assert r.status == 200, f"Lottery creation failed: {r.status} {r.text()}"
    assert r.json().get(
        "selected_applications"), f"Lottery creation returned no selected applications: {r.status} {r.text()}"

    selected_applications: list[ApplicationContext] = []
    rejected_applications: list[ApplicationContext] = []
    for item in r.json().get("selected_applications"):
        assert r.json().get(
            "application_status") in APPLICATION_STATUSES, f"Fetching ballot period entries returned invalid application status: {r.status} {r.text()}"
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
            "application_status") in APPLICATION_STATUSES, f"Fetching ballot period entries returned invalid application status: {r.status} {r.text()}"
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
'''


def ui_create_application_documents(page: Page, application_id: int) -> None:
    if not page.url == f"{FRONTEND_URL}/license-document-upload/{application_id}":
        page.goto(f"{FRONTEND_URL}/license-document-upload/{application_id}")

    file = read_file(filepath=TEST_PDF_PATH, mode="rb")

    payload: FilePayload = {
        "name": TEST_PDF_PATH,
        "mimeType": "application/pdf",
        "buffer": file,
    }

    page.locator("#id_proof").set_input_files(payload)
    page.locator("#address_proof").set_input_files(payload)

    with page.expect_response(
            lambda res: res.request.method == "POST" and re.search(r"/process-document$", res.url)
    ) as res_info:
        page.locator("#submitDocumentsBtn").click()
    r = res_info.value

    assert r.status == 200, f"Document upload failed: {r.status} {r.text()}"
    assert application_id == r.json().get(
        "application_id"), f"Document upload returned invalid application id: {r.status} {r.text()}"

    page.wait_for_url(f"{FRONTEND_URL}/payment/{application_id}")


def ui_create_application_documents_later(page: Page, application_id) -> None:
    if not page.url == f"{FRONTEND_URL}/license-document-upload/{application_id}":
        page.goto(f"{FRONTEND_URL}/license-document-upload/{application_id}")

    page.locator("#uploadLaterBtn").click()

    page.wait_for_url(f"{FRONTEND_URL}/")


def ui_create_application_documents_edit(page: Page, application_id: int) -> None:
    if not page.url == f"{FRONTEND_URL}/license-document-upload/{application_id}":
        page.goto(f"{FRONTEND_URL}/license-document-upload/{application_id}")

    page.locator("#editApplicationBtn").click()

    page.wait_for_url(f"{FRONTEND_URL}/license-application-request/edit/{application_id}")


############################################################################
# Dashboard functions
############################################################################

def ui_change_language(page, user: UserContext) -> None:
    language = "de"
    if not page.url == f"{FRONTEND_URL}/":
        page.goto(f"{FRONTEND_URL}/")
    page.locator("#languageOptionBtn").click()
    page.locator("ul.absolute").wait_for(state="visible")
    page.locator(f"#{language}").click()
    page.locator("ul.absolute").wait_for(state="hidden")

    new_language = _get_language_from_local_storage(page)
    assert new_language == language, f"Language change in local storage failed: expected '{language}', got '{new_language}'"
    user.language = new_language


def ui_visit_contact(page) -> None:
    if not page.url == f"{FRONTEND_URL}/":
        page.goto(f"{FRONTEND_URL}/")
    page.locator("#contactLink").click()
    page.wait_for_url(f"{FRONTEND_URL}/contact")


def ui_visit_legal(page) -> None:
    if not page.url == f"{FRONTEND_URL}/":
        page.goto(f"{FRONTEND_URL}/")
    page.locator("#legalLink").click()
    page.wait_for_url(f"{FRONTEND_URL}/legal")


############################################################################
# Synthetic functions
############################################################################

def ui_wait_for_document_verification(
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
        application, r = _get_document_verification_status(application, access_token)

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
# Helper functions
############################################################################


def _verify_local_storage_after_login(page, json_response: dict) -> None:
    access_token, refresh_token, expires_in, refresh_expires_in, token_type, is_admin = _get_auth_details_from_local_storage(
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


def _get_auth_details_from_local_storage(page) -> tuple[
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


def _get_language_from_local_storage(page) -> str | None:
    return page.evaluate("(k) => window.localStorage.getItem(k)", LANGUAGE_KEY)


def _get_unformatted_amount(formatted: str) -> float:
    s = formatted.strip()

    s = re.sub(r"[^\d,.\-]", "", s)

    # Determine the decimal and thousands separators
    if "." in s and "," in s:
        last_dot = s.rfind(".")
        last_comma = s.rfind(",")
        if last_comma > last_dot:
            s = s.replace(".", "").replace(",", ".")
        else:
            s = s.replace(",", "")
    elif "," in s:
        s = s.replace(".", "")
        s = s.replace(",", ".")
    else:
        s = s.replace(",", "")

    return float(s)


def _get_document_verification_status(application: ApplicationContext, access_token: str) -> \
        tuple[
            ApplicationContext, requests.Response]:
    r = requests.get(
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

    return application, r


def _get_application_fee(application: ApplicationContext, access_token: str) -> float:
    r = requests.get(
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

    return float(r.json().get("fee_amount"))
