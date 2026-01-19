import logging
import os

from locust import HttpUser, SequentialTaskSet, task, between
from testkit.runner.api_runner import api_register, api_login, api_logout

from testkit.factory import new_user, new_application, new_payment
from testkit.models import RunContext

logging.getLogger("urllib3").setLevel(logging.WARNING)
# Trick: damit deine Funktionen f"{BACKEND_URL}/..." zu "/..." werden
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
    def logout(self):
        api_logout(self.client, self.ctx.user)

        # Scenario ist fertig -> TaskSet beenden (sonst würde er nochmal register versuchen)
        self.interrupt(reschedule=False)


class WebsiteUser(HttpUser):
    tasks = [RegisterLoginLogout]
    wait_time = between(0.5, 1.5)
