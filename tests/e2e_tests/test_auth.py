from playwright.sync_api import Page

from testkit.factory import new_user, new_admin, new_ballot_period_running
from testkit.models import RunContext
from testkit.scenarios import REGISTER_LOGIN_LOGOUT, REGISTER_FORGOT_PASSWORD, \
    ADMIN_LOGIN_CREATE_BALLOT, REGISTER_VIA_LOGIN_LOGIN_LOGOUT


def test_register_login_logout(page: Page):
    ctx = RunContext(user=new_user())
    scenario = REGISTER_LOGIN_LOGOUT()
    for step in scenario.steps:
        step(page, ctx)


def test_register_via_login_login_logout(page: Page):
    ctx = RunContext(user=new_user())
    scenario = REGISTER_VIA_LOGIN_LOGIN_LOGOUT()
    for step in scenario.steps:
        step(page, ctx)


def test_register_forgot_password(page: Page):
    ctx = RunContext(user=new_user())
    scenario = REGISTER_FORGOT_PASSWORD()
    for step in scenario.steps:
        step(page, ctx)


# TODO: fix admin test
def test_admin_login_create_ballot(page: Page):
    ctx = RunContext(user=new_admin(), ballot=new_ballot_period_running())
    scenario = ADMIN_LOGIN_CREATE_BALLOT()
    for step in scenario.steps:
        step(page, ctx)
