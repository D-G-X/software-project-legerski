from pathlib import Path

import pytest
from dotenv import load_dotenv
from playwright.sync_api import sync_playwright

from testkit.config import FRONTEND_URL

AUTH_DIR = Path(".auth")
TIMEOUT = 10_000
SLOW_MO = 1000

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

        browser = browser_type.launch(headless=False, slow_mo=SLOW_MO)
        context = browser.new_context(base_url=FRONTEND_URL)
        page = context.new_page()
        page.set_default_timeout(TIMEOUT)
        page.set_default_navigation_timeout(TIMEOUT)
        yield page
        context.close()
        browser.close()
