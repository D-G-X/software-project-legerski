import logging
import random
import string
import uuid
from time import sleep

import requests
from dotenv import load_dotenv

from utils.utils import decode_jwt, read_file, env

LICENSE_TYPES = [
    {
        "name": "ETV",
        "cost": 3500.0
    },
    {
        "name": "ETVPL",
        "cost": 875.0
    },
    {
        "name": "ETV60",
        "cost": 290.0
    },
]

load_dotenv()
logger = logging.getLogger(__name__)


class ApiClient:
    def __init__(self, base_url: str):
        self.base_url = base_url
        self.access_token = None
        self.refresh_token = None
        self.expires_in = None
        self.refresh_expires_in = None
        self.token_type = None
        self.is_admin = None

    def _headers(self):
        return {"Authorization": f"Bearer {self.access_token}"} if self.access_token else {}

    def register(self, user: dict[str, str]) -> dict[str, str]:
        logger.info("Registering user %s", user["email"])

        r = requests.post(
            f"{self.base_url}/register",
            json={
                "firstname": user["firstname"],
                "lastname": user["lastname"],
                "email": user["email"],
                "password": user["password"],
            },
        )
        if r.status_code != 201 or not r.json().get("user_id"):
            raise RuntimeError(f"Registration failed for {user['email']}: {r.status_code} {r.text}")
        user["id"] = r.json().get("user_id")

        logger.info("  -> User registered successfully")
        return user

    def login(self, user: dict[str, str]) -> dict[str, str]:
        logger.info("Logging in user %s", user["email"])

        r = requests.post(
            f"{self.base_url}/login",
            json={
                "email": user["email"],
                "password": user["password"],
            },
        )
        if r.status_code != 200 or "access_token" not in r.json():
            raise RuntimeError(f"Login failed for {user['email']}: {r.status_code} {r.text}")

        self.access_token = r.json().get("access_token")
        self.refresh_token = r.json().get("refresh_token")
        self.expires_in = r.json().get("expires_in")
        self.refresh_expires_in = r.json().get("refresh_expires_in")
        self.token_type = int(r.json().get("token_type"))
        self.is_admin = bool(r.json().get("is_admin"))

        user["access_token"] = self.access_token
        if user["id"] != decode_jwt(self.access_token)["sub"]:
            raise RuntimeError(f"Login returned invalid user id for {user['email']}: {r.status_code} {r.text}")

        logger.info("  -> User logged in successfully")
        return user

    def get_current_ballot_period(self, ballot_period: dict[str, str]) -> dict[str, str]:
        logger.info("Fetching current ballot period")

        r = requests.get(
            f"{self.base_url}/ballot-periods/current",
            headers=self._headers(),
        )
        if r.status_code != 200:
            raise RuntimeError(f"Fetching current ballot period failed: {r.status_code} {r.text}")
        ballot_period["start_date"] = r.json().get("start_date")
        ballot_period["end_date"] = r.json().get("end_date")

        logger.info("  -> Current ballot period fetched successfully")
        return ballot_period

    def create_license_request(self, application: dict[str, str], user: dict[str, str]) -> dict[str, str]:
        logger.info("Creating license application for user %s", user["email"])

        application["license_type"] = random.choice(LICENSE_TYPES)["name"]
        application["cadastral_reference"] = ''.join(random.choices(string.digits, k=20))
        r = requests.post(
            f"{self.base_url}/applications",
            headers=self._headers(),
            json={
                "user_id": user["id"],
                "license_type": application["license_type"],
                "cadastral_reference": application["cadastral_reference"],
                "remarks": "Test license application",
            },
        )
        if r.status_code != 201 or user["id"] != r.json().get("user_id"):
            raise RuntimeError(f"Application request failed for {user['email']}: {r.status_code} {r.text}")
        application["id"] = r.json().get("id")

        logger.info("  -> License application created successfully")
        return self.update_application_status(application)

    def upload_license_request_documents(self, application: dict[str, str], user: dict[str, str]) -> dict[str, str]:
        logger.info("Uploading documents for application %s of user %s", application["id"], user["email"])

        file = read_file(filepath=env("TEST_PDF_PATH"))
        r = requests.post(
            f"{self.base_url}/process-document",
            headers=self._headers(),
            data={"application_id": application["id"]},
            files=[
                ("id_file", ("sample.pdf", file, "application/pdf")),
                ("proof_file", ("sample.pdf", file, "application/pdf")),
            ],
        )
        if r.status_code != 200 or application["id"] != r.json().get("application_id"):
            raise RuntimeError(f"Document upload failed for {user['email']}: {r.status_code} {r.text}")

        for _ in range(30):
            r = requests.get(
                f"{self.base_url}/applications/{application["id"]}/documents",
                headers=self._headers(),
                data={"application_id": application["id"]},
            )
            if r.status_code != 200 or application["id"] != r.json().get("application_id"):
                raise RuntimeError(f"Document status cannot be fetched for {user['email']}: {r.status_code} {r.text}")
            status, application["document_status"] = r.json().get("status")

            if status != "PENDING":
                logger.info(f"  -> Documents {status.lower()} successfully")
                return self.update_application_status(application)

            sleep(1)
        raise RuntimeError(f"Document verification timed out for {user['email']}: {r.status_code} {r.text}")

    def payment_license_request(self, application: dict[str, str], user: dict[str, str]) -> dict[str, str]:
        logger.info("Processing payment for application %s", application["id"])

        r = requests.post(
            f"{self.base_url}/applications/{application["id"]}/payments",
            headers=self._headers(),
            json={
                "name": f"{user["firstname"]} {user["lastname"]}",
                "iban": "DE" + ''.join(random.choices(string.digits, k=20)),
                "BIC": ''.join(random.choices(string.ascii_uppercase, k=4)) +
                       "DE" +
                       ''.join(random.choices(string.digits, string.ascii_uppercase, k=2)),
            },
        )
        if r.status_code != 200 or application["id"] != r.json().get("application_id"):
            raise RuntimeError(f"Payment failed for {user['email']}: {r.status_code} {r.text}")
        application["payment_status"] = r.json().get("status")

        logger.info("  -> Payment processed successfully")
        return self.update_application_status(application)

    def get_license_fee(self, application: dict[str, str]) -> float:
        logger.info("Fetching license fees")
        expected_cost: float = next(lt["cost"] for lt in LICENSE_TYPES if lt["name"] == application["license_type"])
        r = requests.get(
            f"{self.base_url}/fees/{application["id"]}",
            headers=self._headers(),
        )
        if r.status_code != 200 or application["id"] != r.json().get("application_id"):
            raise RuntimeError(f"Fetching license fees failed: {r.status_code} {r.text}")
        if expected_cost != float(r.json().get("fee_amount")):
            raise RuntimeError(f"Fetched license fees do not match expected cost: {r.status_code} {r.text}")

        logger.info("  -> License fees fetched successfully")
        return r.json().get("license_fees", [])

    def refresh_login(self, user: dict[str, str]) -> dict[str, str]:
        logger.info("Refreshing access token")

        r = requests.post(
            f"{self.base_url}/refresh-login",
            json={
                "refresh_token": self.refresh_token,
            },
        )
        if r.status_code != 200 or "access_token" not in r.json():
            raise RuntimeError(f"Token refresh failed: {r.status_code} {r.text}")

        self.access_token = r.json().get("access_token")
        self.refresh_token = r.json().get("refresh_token")
        self.expires_in = r.json().get("expires_in")
        self.refresh_expires_in = r.json().get("refresh_expires_in")
        self.token_type = int(r.json().get("token_type"))
        self.is_admin = bool(r.json().get("is_admin"))
        user["access_token"] = self.access_token

        logger.info("  -> Access token refreshed successfully")
        return user

    def request_reset_password(self, user: dict[str, str]) -> None:
        logger.info("Requesting to reset password for user %s", user["email"])

        r = requests.post(
            f"{self.base_url}/reset-password",
            json={
                "email": user["email"],
            },
        )
        if r.status_code != 200:
            raise RuntimeError(f"Password reset request failed for {user['email']}: {r.status_code} {r.text}")

        logger.info("  -> Password reset request sent successfully")

    def reset_password(self, user: dict[str, str]) -> dict[str, str]:
        logger.info("Resetting password for user %s", user["email"])

        new_password = "Reset123!"
        r = requests.post(
            f"{self.base_url}/change-password",
            json={
                "token": "",  # TODO: get token from email
                "new_password": new_password,
            },
        )
        if r.status_code != 200:
            raise RuntimeError(f"Password reset failed for {user['email']}: {r.status_code} {r.text}")
        user["password"] = new_password

        logger.info("  -> Password reset successfully")
        return user

    def change_password(self, user: dict[str, str]) -> dict[str, str]:
        logger.info("Changing password for user %s", user["email"])

        new_password = "Change123!"
        r = requests.patch(
            f"{self.base_url}/users/{user["id"]}",
            headers=self._headers(),
            json={
                "firstName": user["firstname"],
                "lastName": user["lastname"],
                "email": user["email"],
                "enabled": True,
                "credentials": [
                    {
                        "type": "password",
                        "value": new_password,
                        "temporary": False,
                    },
                ]
            },
        )
        if r.status_code != 204 or r.json().get("description") == "" or r.json().get("description") is None:
            raise RuntimeError(f"Password change failed for {user['email']}: {r.status_code} {r.text}")
        user["password"] = new_password

        logger.info("  -> Password changed successfully")
        return user

    def change_name(self, user: dict[str, str]) -> dict[str, str]:
        logger.info("Changing name for user %s", user["email"])

        new_firstname, new_lastname = "New", "Name"
        r = requests.patch(
            f"{self.base_url}/users/{user["id"]}",
            headers=self._headers(),
            json={
                "firstName": new_firstname,
                "lastName": new_lastname,
                "email": user["email"],
                "enabled": True,
                "credentials": [
                    {
                        "type": "password",
                        "value": user["password"],
                        "temporary": False,
                    },
                ]
            },
        )
        if r.status_code != 204 or r.json().get("description") == "" or r.json().get("description") is None:
            raise RuntimeError(f"Name change failed for {user['email']}: {r.status_code} {r.text}")
        user["firstname"], user["lastname"] = new_firstname, new_lastname

        logger.info("  -> Name changed successfully")
        return user

    def change_email(self, user: dict[str, str]) -> dict[str, str]:
        logger.info("Changing e-mail for user %s", user["email"])

        new_email = f"user-{uuid.uuid4().hex}@test.local"
        r = requests.patch(
            f"{self.base_url}/users/{user["id"]}",
            headers=self._headers(),
            json={
                "firstName": user["firstname"],
                "lastName": user["lastname"],
                "email": new_email,
                "enabled": True,
                "credentials": [
                    {
                        "type": "password",
                        "value": user["password"],
                        "temporary": False,
                    },
                ]
            },
        )
        if r.status_code != 204 or r.json().get("description") == "" or r.json().get("description") is None:
            raise RuntimeError(f"E-Mail change failed for {user['email']}: {r.status_code} {r.text}")
        user["email"] = new_email

        logger.info("  -> E-Mail changed successfully")
        return user

    def get_notifications(self, user: dict[str, str]) -> list[dict[str, str]]:
        r = requests.get(
            f"{self.base_url}/users/{user["id"]}/notifications",
            headers=self._headers(),
            data={"user_id": user["id"]},
        )
        if r.status_code != 200 or user["id"] != r.json().get("user_id"):
            raise RuntimeError(f"Fetching notifications failed for {user['email']}: {r.status_code} {r.text}")
        return r.json().get("notifications", [])

    def update_application_status(self, application: dict[str, str]) -> dict[str, str]:
        r = requests.get(
            f"{self.base_url}/applications/{application["id"]}",
            headers=self._headers(),
            data={"application_id": application["id"]},
        )
        if r.status_code != 200 or application["id"] != r.json().get("application_id"):
            raise RuntimeError(f"Fetching application status failed: {r.status_code} {r.text}")
        application["status"] = r.json().get("status")
        return application
