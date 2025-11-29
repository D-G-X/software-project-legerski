from locust import HttpUser, task, between

class WebsiteUser(HttpUser):
    wait_time = between(1, 3)

    @task
    def access_homepage(self):
        with self.client.get("/", catch_response=True) as response:
            if response.status_code == 401:
                response.success()  
            elif response.status_code == 200:
                response.success()
            else:
                response.failure(f"Status code: {response.status_code}")
