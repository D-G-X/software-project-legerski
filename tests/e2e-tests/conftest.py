from pathlib import Path

import pytest
from dotenv import load_dotenv
from playwright.sync_api import sync_playwright

from testkit.config import FRONTEND_URL

AUTH_DIR = Path(".auth")

load_dotenv()


def _browser_from_pytest(pytestconfig) -> str:
    b = pytestconfig.getoption("--browser") or "chromium"
    if isinstance(b, (list, tuple)):
        b = b[0] if b else "chromium"
    return b

@pytest.fixture
def page(pytestconfig):
    browser_name = _browser_from_pytest(pytestconfig)

    with sync_playwright() as p:
        browser_type = {
            "chromium": p.chromium,
            "firefox": p.firefox,
            "webkit": p.webkit,
        }[browser_name]

        browser = browser_type.launch(headless=False)
        context = browser.new_context(base_url=FRONTEND_URL)
        page = context.new_page()
        yield page
        context.close()
        browser.close()


'''
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
    # pytest-e2e-tests nutzt das automatisch beim Erstellen des Contexts
    return {"storage_state": str(ensure_storage_state)}

@pytest.fixture(scope="session")
def api_context(e2e-tests):
    ctx = e2e-tests.request.new_context(base_url=FRONTEND_URL)
    yield ctx
    ctx.dispose()


@pytest.fixture(scope="session")
def admin_ctx() -> UserContext:
    return UserContext()


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
def ballot_period_ctx() -> BallotPeriodContext:
    return BallotPeriodContext()
'''
