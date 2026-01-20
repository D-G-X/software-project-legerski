import logging
import os
from gevent import sleep

from locust import HttpUser, SequentialTaskSet, task, between

from api_runner import (
    api_register,
    api_login,
    api_logout,
    api_create_application,
    api_update_application,
    api_create_application_documents,
    api_create_payment,
    api_get_application_fee,
    api_list_applications,
    api_list_applications_by_id,
    api_get_user_by_id,
    api_list_payments,
    api_wait_for_document_verification,
)

from testkit.factory import new_user, new_application, new_payment, new_license
from testkit.models import UserContext, ApplicationContext, PaymentContext, LicenseContext

logging.getLogger("urllib3").setLevel(logging.ERROR)

os.environ.setdefault("BACKEND_URL", os.environ.get("BACKEND_URL", ""))


class RegisterLoginLogout(SequentialTaskSet):
    def on_start(self):
        self.finished = False

        self.test_user: UserContext = new_user()
        self.test_application: ApplicationContext = new_application()
        self.test_payment: PaymentContext = new_payment()
        self.test_license: LicenseContext = new_license()

    @task
    def register(self):
        if self.finished:
            sleep(10**9)
        self.test_user = api_register(self.client, self.test_user)

        if not getattr(self.test_user, "id", None):
            self.finished = True
            sleep(10**9)

    @task
    def login(self):
        if self.finished:
            sleep(10**9)
        self.test_user = api_login(self.client, self.test_user)

        if not getattr(self.test_user, "access_token", None) or not getattr(self.test_user, "id", None):
            self.finished = True
            sleep(10**9)

    @task
    def create_application(self):
        if self.finished:
            sleep(10**9)
        self.test_application = api_create_application(
            self.client,
            self.test_application,
            self.test_user.id,
            self.test_user.access_token,
        )

        if not getattr(self.test_application, "id", None):
            self.finished = True
            sleep(10**9)

    @task
    def edit_application(self):
        if self.finished:
            sleep(10**9)
        self.test_application = api_update_application(
            self.client,
            self.test_application,
            self.test_application.cadastral_reference,
            self.test_application.remarks,
            self.test_application.license_type,
            self.test_user.access_token,
        )

    @task
    def upload_documents(self):
        if self.finished:
            sleep(10**9)
        api_create_application_documents(self.client, self.test_application.id, self.test_user.access_token)
        self.test_application = api_wait_for_document_verification(
            self.client, self.test_application, self.test_user.access_token
        )

    @task
    def payment(self):
        if self.finished:
            sleep(10**9)
        self.test_payment = api_get_application_fee(
            self.client, self.test_application, self.test_payment, self.test_user.access_token
        )
        self.test_payment = api_create_payment(
            self.client, self.test_application.id, self.test_payment, self.test_user.access_token
        )

    @task
    def show_applications_dashboard(self):
        if self.finished:
            sleep(10**9)
        api_list_applications(self.client, self.test_user.id, self.test_user.access_token)

    @task
    def show_application_details(self):
        if self.finished:
            sleep(10**9)
        self.test_application = api_list_applications_by_id(
            self.client, self.test_application, self.test_user.id, self.test_user.access_token
        )
        self.test_user = api_get_user_by_id(self.client, self.test_user, self.test_user.access_token)

        self.test_application = api_wait_for_document_verification(
            self.client, self.test_application, self.test_user.access_token
        )

        self.test_payment = api_list_payments(self.client, self.test_application.id, self.test_user.access_token)

    @task
    def logout(self):
        if self.finished:
            sleep(10**9)
        self.test_user = api_logout(self.client, self.test_user)

        self.finished = True
        sleep(10**9)


class WebsiteUser(HttpUser):
    tasks = [RegisterLoginLogout]
    wait_time = between(5, 10)