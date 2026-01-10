from dotenv import load_dotenv

from clients.api_client import ApiClient
from flows.application_flow import ApplicationFlow
from utils.utils import env

load_dotenv()


def test_application_e2e():
    api = ApiClient(env("BACKEND_URL"))

    flow = ApplicationFlow(api)

    flow.run()

    assert flow.status() == "SUBMITTED"

    # with sync_playwright() as p:
    #  browser = p.webkit.launch(headless=False)
    #  page = browser.new_page()
    #  page.goto(env("PLAYWRIGHT_URL"))
