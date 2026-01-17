from locust import HttpUser, task
from locust.exception import StopUser

from flows.application_flow import ApplicationFlow


class ApplicationUser(HttpUser):
    @task
    def run_flow(self):
        flow = ApplicationFlow(self.client)

        try:
            flow.register_user()
            flow.login()
            flow.create_application()
            flow.save_draft()
            flow.submit_application()
            flow.pay_fee()

            if flow.verify_status() != "SUBMITTED":
                raise Exception("Invalid status")

        except Exception as e:
            self.environment.events.test_stop.fire()
            raise StopUser()
