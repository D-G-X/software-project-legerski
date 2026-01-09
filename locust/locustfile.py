import base64
import json
import logging
import random
import string
import uuid
from datetime import datetime, timezone
from itertools import count
from math import floor
from queue import Queue

from gevent import sleep
from locust import HttpUser, task, events, constant_throughput
from locust.clients import HttpSession
from locust.exception import StopUser

MAX_RESPONSE_TIME_MS = 500  # Max avg response time according to requirements
MAX_ERROR_RATE = 0.01  # Max error rate according to requirements

LICENSE_TYPES = ["ETV", "ETVPL", "ETV60"]
MAX_TRY_SEC = 30
USER_COUNT = 1  # TODO: Add to environment variables
USER_COUNTER = count(1)
USER_QUEUE: Queue = Queue()

logger = logging.getLogger(__name__)


def decode_jwt(token: str) -> dict:
  payload = token.split(".")[1]
  payload += "=" * (-len(payload) % 4)  # padding
  return json.loads(base64.urlsafe_b64decode(payload))


@events.test_start.add_listener
def register_users(environment, **kwargs):
  logger.info("🔹 Pre-registering users...")

  client = HttpSession(
    base_url=environment.host,
    request_event=environment.events.request,
    user=None,
  )

  for _ in range(USER_COUNT):
    user_no = next(USER_COUNTER)
    user = {
      "id": "",
      "number": user_no,
      "email": f"user-{int(uuid.uuid4())}@locust.local",
      "password": "Locust123!",
      "firstname": "Load",
      "lastname": "Test",
      "access_token": "",
    }

    r = client.post(
      "/register",
      json={
        "firstname": user["firstname"],
        "lastname": user["lastname"],
        "email": user["email"],
        "password": user["password"],
      },
      name="register_preload",
    )

    if r.status_code != 201:
      raise RuntimeError(
        f"Registration failed for {user["email"]}: {r.status_code} {r.text}"
      )

    USER_QUEUE.put(user)

  logger.info("✅ User registration done")


class WebsiteUser(HttpUser):
  wait_time = constant_throughput(10)

  def on_start(self):
    logger.info("User started")

    if USER_QUEUE.empty():
      self.environment.runner.stats.log_request(
        request_type="USER",
        name="no_user_left",
        response_time=0,
        response_length=0,
      )
      raise StopUser()

    user = USER_QUEUE.get()

    # 1 HOME
    self.client.get("/", name="home")

    # 2 LOGIN
    r = self.client.post(
      "/login",
      json={
        "email": user["email"],
        "password": user["password"],
      },
      name="login",
    )
    if r.status_code != 200:
      raise RuntimeError(f"Login failed for {user["email"]}")

    data = r.json()
    user["access_token"] = data["access_token"]
    self.client.headers.update({
      "Authorization": f"Bearer {user["access_token"]}"
    })

    claims = decode_jwt(user["access_token"])
    user["id"] = claims["sub"]

    # 3 REQUEST APPLICATION
    r = self.client.post(
      "/applications",
      json={
        "user_id": user["id"],
        "license_type": random.choice(LICENSE_TYPES),
        "cadastral_reference": ''.join(random.choices(string.digits, k=20)),
        "remarks": "Load test application",
      },
      name="request_application",
    )
    if r.status_code != 201:
      raise RuntimeError(f"Application request failed: {r}")

    application_id = r.json()["id"]

    # 4 DOCUMENT UPLOAD
    with open("sample.pdf", "rb") as f:
      r = self.client.post(
        "/process-document",
        data={"application_id": application_id},
        files=[
          ("id_file", ("sample.pdf", f, "application/pdf")),
          ("proof_file", ("sample.pdf", f, "application/pdf")),
        ],
        name="upload_document",
      )
    if r.status_code != 200:
      raise RuntimeError("Document upload failed")

    # 5 WAIT FOR VALIDATION
    for _ in range(MAX_TRY_SEC):
      r = self.client.get(
        f"/applications/{application_id}/documents",
        name="check_document_validation_status",
      )
      if r.json()["status"] == "VERIFIED":
        break
      sleep(1)

    # 6 PAYMENT
    r = self.client.post(
      f"/applications/{application_id}/payments",
      json={
        "name": f"{user["firstname"]} {user["lastname"]}",
        "iban": "DE" + "".join(random.choices(string.digits, k=20)),
        "BIC": "DEUTDEAAXXX",
      },
      name="payment_form",
    )
    if r.status_code != 200:
      raise RuntimeError("Payment failed")

  @task
  def idle(self):
    pass

@events.quitting.add_listener
def _(environment, **kw):
  stats = environment.stats.total

  if stats.avg_response_time > MAX_RESPONSE_TIME_MS:
    raise SystemExit(
      f"❌ Avg response time too high: {stats.avg_response_time} ms"
    )

  if stats.fail_ratio > MAX_ERROR_RATE:
    raise SystemExit(
      f"❌ Error rate too high: {stats.fail_ratio * 100:.2f}%"
    )
  logger.info("✅ Test passed all requirements")
