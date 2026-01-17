from pathlib import Path
from time import sleep

from clients.api_client import ApiClient
from dotenv import load_dotenv
from flows.application_flow import ApplicationCreationFlow
from testkit.models import UserContext, AdminContext, ApplicationContext, PaymentContext, LicenseContext
import pytest

from playwright.context import BallotPeriodUserContext
from utils.utils import env

AUTH_DIR = Path(".auth")

load_dotenv()


def _browser_from_pytest(pytestconfig) -> str:
    # pytest-playwright: --browser chromium|firefox|webkit
    return pytestconfig.getoption("--browser") or "chromium"


@pytest.fixture(scope="session")
def storage_state_file(pytestconfig) -> Path:
    AUTH_DIR.mkdir(exist_ok=True)
    b = _browser_from_pytest(pytestconfig)
    return AUTH_DIR / f"state-{b}.json"


@pytest.fixture(scope="session")
def ensure_storage_state(browser, storage_state_file: Path):
    context = browser.new_context()
    page = context.new_page()

    page.goto("/")

    sleep(30)

    context.storage_state(path=str(storage_state_file))
    context.close()

    return storage_state_file


@pytest.fixture
def browser_context_args(ensure_storage_state: Path):
    # pytest-playwright nutzt das automatisch beim Erstellen des Contexts
    return {"storage_state": str(ensure_storage_state)}


@pytest.fixture
def page():
    with sync_playwright() as p:
        browser = p.firefox.launch(headless=True)
        context = browser.new_context(base_url=env("FRONTEND_URL"))
        page = context.new_page()
        yield page
        context.close()
        browser.close()


@pytest.fixture(scope="session")
def api_context(playwright):
    ctx = playwright.request.new_context(base_url=env("BACKEND_URL"))
    yield ctx
    ctx.dispose()


@pytest.fixture(scope="session")
def admin_ctx() -> AdminContext:
    return AdminContext()


@pytest.fixture(scope="function")
def user_ctx() -> UserContext:
    return UserContext()


@pytest.fixture(scope="function")
def application_ctx() -> ApplicationContext:
    return ApplicationContext()


@pytest.fixture(scope="function")
def payment_ctx() -> PaymentContext:
    return PaymentContext()


@pytest.fixture(scope="function")
def license_ctx() -> LicenseContext:
    return LicenseContext()


@pytest.fixture(scope="function")
def ballot_period_ctx() -> BallotPeriodUserContext:
    return BallotPeriodUserContext()
