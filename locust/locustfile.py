from locust import HttpUser, task, between, events, constant_throughput


MAX_RESPONSE_TIME_MS = 500  # Max avg response time according to requirements
MAX_ERROR_RATE = 0.01    # Max error rate according to requirements

class WebsiteUser(HttpUser):
    wait_time = constant_throughput(10)  # 10 tasks per second

    @task(1)
    def access_homepage(self):
        with self.client.get("/", catch_response=True) as response:
            if response.status_code in [200, 401]:
                response.success()
            else:
                response.failure(f"Homepage failed with status: {response.status_code}")
'''
    @task(2)
    def login(self):
        payload = {
            "email": "test@locust.com",
            "password": "locust123!"
        }
        
        headers = {"Content-Type": "application/json"}

        with self.client.post("/login", json=payload, headers=headers, catch_response=True) as response:
            if response.status_code == 200:
                if "access_token" in response.text:
                    response.success()
                else:
                    response.failure("200 OK but no token found!")
            else:
                response.failure(f"Login failed: {response.status_code} - {response.text}")
'''
@events.quitting.add_listener
def _(environment, **kw):
    stats = environment.stats.total
    avg = stats.avg_response_time

    if stats.avg_response_time > MAX_RESPONSE_TIME_MS:
        raise SystemExit(f"❌ Avg response time too high: {avg} ms")

    if stats.fail_ratio > MAX_ERROR_RATE:
        raise SystemExit(f"❌ Error rate too high: {fail_ratio / 100:.2f}%")
