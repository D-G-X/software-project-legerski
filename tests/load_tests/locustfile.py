import logging
import os

from locust import HttpUser, SequentialTaskSet, task, between

from api_runner import api_register, api_login, api_logout, api_create_application, api_update_application, \
    api_create_application_documents, api_create_payment, api_get_application_fee, api_get_document_verification_status, \
    api_get_applications, api_get_application_by_id, api_get_user_by_id, api_get_licenses, api_get_license_by_id, \
    api_get_payments, api_wait_for_document_verification
from testkit.factory import new_user, new_application, new_payment
from testkit.models import RunContext

logging.getLogger("urllib3").setLevel(logging.WARNING)
os.environ.setdefault("BACKEND_URL", "")
BACKEND_URL = os.environ["BACKEND_URL"]


class RegisterLoginLogout(SequentialTaskSet):
    ctx: RunContext = None

    def on_start(self):
        self.ctx = RunContext(
            user=new_user(),
            application=new_application(),
            payment=new_payment()
        )

    @task
    def register(self):
        api_register(self.client, self.ctx.user)

    @task
    def login(self):
        api_login(self.client, self.ctx.user)

    @task
    def create_application(self):
        api_create_application(self.client, self.ctx.application, self.ctx.user.id, self.ctx.user.access_token)

    @task
    def edit_application(self):
        api_update_application(self.client, self.ctx.application, self.ctx.application.application_status,
                               self.ctx.application.cadastral_reference, self.ctx.application.remarks,
                               self.ctx.application.license_type, self.ctx.user.access_token)

    @task
    def upload_documents(self):
        api_create_application_documents(self.client, self.ctx.application.id, self.ctx.user.access_token)
        api_wait_for_document_verification(self.client, self.ctx.application, self.ctx.user.access_token)

    @task
    def get_fee(self):
        api_get_application_fee(self.client, self.ctx.application, self.ctx.user.access_token)

    @task
    def payment(self):
        api_create_payment(self.client, self.ctx.application.id, self.ctx.payment, self.ctx.user.access_token)

    @task
    def show_applications_dashboard(self):
        api_get_applications(self.client, self.ctx.user.id, self.ctx.user.access_token)

    @task
    def show_application_details(self):
        api_get_application_by_id(self.client, self.ctx.user.id, self.ctx.user.access_token)
        api_get_user_by_id(self.client, self.ctx.user.id, self.ctx.user.access_token)
        api_get_licenses(self.client, self.ctx.user.id, self.ctx.user.access_token)
        api_get_document_verification_status(self.client, self.ctx.application, self.ctx.user.access_token)
        api_get_payments(self.client, self.ctx.application.id, self.ctx.user.access_token)

    @task
    def logout(self):
        api_logout(self.client, self.ctx.user)

        self.interrupt(reschedule=False)


class WebsiteUser(HttpUser):
    tasks = [RegisterLoginLogout]
    wait_time = between(0.5, 1.5)
