import time
import uuid

from clients.api_client import ApiClient


class ApplicationFlow:
    def __init__(self, api: ApiClient):
        self.api = api
        self.user = {
            "id": "",
            "email": f"user-{uuid.uuid4().hex}@test.local",
            "password": "Test123!",
            "firstname": "Test",
            "lastname": "User",
            "access_token": "",
        }
        self.application = {
            "id": "",
            "license_type": "",
            "status": "",
            "document_status": "",
            "payment_status": "",
        }
        self.ballot_period = {
            "start_date": "",
            "end_date": "",
        }

    def run(self):
        self.user = self.api.register(self.user)
        self.user = self.api.login(self.user)
        self.ballot_period = self.api.get_current_ballot_period(self.ballot_period)
        self.application = self.api.create_license_request(self.application, self.user)
        while True:
            self.application = self.api.upload_license_request_documents(self.application, self.user)
            if self.application["document_status"] == "VERIFIED":
                break
            time.sleep(1)
        self.application = self.api.payment_license_request(self.application, self.user)

    def status(self):
        return self.api.update_application_status(self.application)
