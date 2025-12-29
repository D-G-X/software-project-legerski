import base64
import json
import os
import random
import string
import uuid
from locust import HttpUser, task, between, events, constant_throughput
from locust.exception import StopUser

MAX_RESPONSE_TIME_MS = 500  # Max avg response time according to requirements
MAX_ERROR_RATE = 0.01  # Max error rate according to requirements

LICENSE_TYPES = ["ETV", "ETVPL", "ETV60"]


def decode_jwt(token: str) -> dict:
  payload = token.split(".")[1]
  payload += "=" * (-len(payload) % 4)  # padding
  decoded = base64.urlsafe_b64decode(payload)
  return json.loads(decoded)


class WebsiteUser(HttpUser):
  wait_time = constant_throughput(10)  # 10 tasks per second

  def on_start(self):
    # 1 HOME
    self.client.get("/")

    # Generate unique user
    self.firstname = "Load"
    self.lastname = "Test"
    self.email = f"locust_{uuid.uuid4()}@test.local"
    self.password = "Locust123!"

    # 2 REGISTER
    r = self.client.post(
      "/register",
      json={
        "firstname": self.firstname,
        "lastname": self.lastname,
        "email": self.email,
        "password": self.password,
      },
      name="register",
    )
    if r.status_code != 201:
      raise StopUser()

    # 3 LOGIN
    r = self.client.post(
      "/login",
      json={
        "email": self.email,
        "password": self.password,
      },
      name="login",
    )
    if r.status_code != 200:
      raise StopUser()

    self.access_token = r.json()["access_token"]
    self.refresh_token = r.json()["refresh_token"]
    self.expires_in = r.json()["expires_in"]
    self.refresh_expires_in = r.json()["refresh_expires_in"]
    self.token_type = r.json()["token_type"]
    self.is_admin = r.json()["is_admin"]
    self.client.headers.update(
      {"Authorization": f"Bearer {self.access_token}"}
    )
    claims = decode_jwt(self.access_token)
    self.user_id = claims["sub"]
    self.username = claims["username"]

    if not self.user_id or not self.username or not (
        self.email == claims["email"]):
      raise StopUser()

    # 4 BACK TO HOME (authenticated)
    self.client.get("/", name="home_authenticated")

    # 5 REQUEST APPLICATION
    r = self.client.post(
      "/applications",
      json={
        "user_id": self.user_id,
        "license_type": LICENSE_TYPES[
          random.randint(0, len(LICENSE_TYPES) - 1)],
        "cadastral_reference": ''.join(random.choices(string.digits, k=20)),
        "remarks": random.random() < 0.5 and "This is a load test application." or "",
      },
      name="request_application",
    )
    if r.status_code != 201:
      raise StopUser()

    self.application_id = r.json()["id"]  # TODO: multi-application per user?

    # 6.1 DOCUMENT UPLOAD
    with open("sample.pdf", "rb") as f:
      r = self.client.post(
        "/process-document",
        data={
          "application_id": self.application_id,
        },
        files=[
          ("id_file", ("sample.pdf", f, "application/pdf")),
          ("proof_file", ("sample.pdf", f, "application/pdf")),
        ],
        name="upload_document",
      )

    if r.status_code != 200:
      raise StopUser()

    # 6.2 WAIT FOR VALIDATION RESULT
    validated = False
    while not validated:
      r = self.client.get(
        f"/applications/{self.application_id}/documents",
        name="check_document_validation_status",
      )
      if r.status_code != 200:
        raise StopUser()
      if r.json()["status"] == "VERIFIED":
        validated = True
      else:
        sleep(1)  # wait before retrying

    # 7 PAYMENT
    r = self.client.post(
      f"/applications/{self.application_id}/payments",
      json={
        "name": f"{self.firstname} {self.lastname}",
        "iban": "DE".join(random.choices(string.digits, k=20)),
        "BIC": "DEUTDEAAXXX",
      },
      name="payment_form",
    )
    if r.status_code != 200:
      raise StopUser()

    if not r.json()["id"] or r.json["status"] != "UNPAID":
      raise StopUser()
    self.payment_id = r.json()["id"]

    # 8 BACK TO HOME (authenticated)
    self.client.get("/", name="home_authenticated")

  @task
  def idle(self):
    # User story done -> keep alive
    pass


@events.quitting.add_listener
def _(environment, **kw):
  stats = environment.stats.total
  avg = stats.avg_response_time

  if stats.avg_response_time > MAX_RESPONSE_TIME_MS:
    raise SystemExit(f"❌ Avg response time too high: {avg} ms")

  if stats.fail_ratio > MAX_ERROR_RATE:
    raise SystemExit(f"❌ Error rate too high: {fail_ratio / 100:.2f}%")
