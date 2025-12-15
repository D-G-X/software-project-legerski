from locust import HttpUser, task, between

class WebsiteUser(HttpUser):
    wait_time = between(1, 3)

    @task(1)
    def access_homepage(self):
        with self.client.get("/", catch_response=True) as response:
            if response.status_code in [200, 401]:
                response.success()
            else:
                response.failure(f"Homepage failed with status: {response.status_code}")

    @task(2)
    def login(self):
        payload = {
            "email": "xyz@gmail.com", 
            "password": "xyz@1234"
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