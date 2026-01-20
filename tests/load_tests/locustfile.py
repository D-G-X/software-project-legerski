import logging
import os

from locust import HttpUser, SequentialTaskSet, task, between

from api_runner import api_register, api_login, api_logout, api_create_application, api_update_application, \
    api_create_application_documents, api_create_payment, api_get_application_fee, api_list_applications, \
    api_list_applications_by_id, api_get_user_by_id, \
    api_list_payments, api_wait_for_document_verification, api_list_licenses
from testkit.factory import new_user, new_application, new_payment, new_license
from testkit.models import UserContext, ApplicationContext, PaymentContext, LicenseContext
from tests.load_tests.api_runner import api_get_license_by_id

logging.getLogger("urllib3").setLevel(logging.WARNING)
os.environ.setdefault("BACKEND_URL", "")
BACKEND_URL = os.environ["BACKEND_URL"]


class RegisterLoginLogout(SequentialTaskSet):

    def on_start(self):
        self.test_user: UserContext = new_user()
        self.test_application: ApplicationContext = new_application()
        self.test_payment: PaymentContext = new_payment()
        self.test_license: LicenseContext = new_license()

    @task
    def register(self):
        self.test_user = api_register(self.client, self.test_user)

    @task
    def login(self):
        self.test_user = api_login(self.client, self.test_user)

    @task
    def create_application(self):
        self.test_application = api_create_application(self.client, self.test_application, self.test_user.id,
                                                       self.test_user.access_token)

    @task
    def edit_application(self):
        self.test_application = api_update_application(self.client, self.test_application,
                                                       self.test_application.cadastral_reference,
                                                       self.test_application.remarks,
                                                       self.test_application.license_type, self.test_user.access_token)

    @task
    def upload_documents(self):
        api_create_application_documents(self.client, self.test_application.id, self.test_user.access_token)
        self.test_application = api_wait_for_document_verification(self.client, self.test_application,
                                                                   self.test_user.access_token)

    @task
    def payment(self):
        self.test_payment = api_get_application_fee(self.client, self.test_application, self.test_payment,
                                                    self.test_user.access_token)
        self.test_payment = api_create_payment(self.client, self.test_application.id, self.test_payment,
                                               self.test_user.access_token)

    @task
    def show_applications_dashboard(self):
        api_list_applications(self.client, self.test_user.id, self.test_user.access_token)

    @task
    def show_application_details(self):
        self.test_application = api_list_applications_by_id(self.client, self.test_application, self.test_user.id,
                                                            self.test_user.access_token)
        self.test_user = api_get_user_by_id(self.client, self.test_user, self.test_user.access_token)
        self.test_license = api_list_licenses(self.client, self.test_user.id, self.test_user.access_token)
        self.test_license = api_get_license_by_id(self.client, self.test_license, self.test_user.access_token)
        self.test_application = api_wait_for_document_verification(self.client, self.test_application,
                                                                   self.test_user.access_token)
        self.test_payment = api_list_payments(self.client, self.test_application.id, self.test_user.access_token)

    @task
    def logout(self):
        self.test_user = api_logout(self.client, self.test_user)

        self.interrupt(reschedule=False)


class WebsiteUser(HttpUser):
    tasks = [RegisterLoginLogout]
    wait_time = between(30, 300)  # seconds
