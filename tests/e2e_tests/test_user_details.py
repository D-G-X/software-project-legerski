from playwright.sync_api import Page

from testkit.factory import new_user, new_application
from testkit.models import RunContext
from testkit.scenarios import DASHBOARD_CHANGE_LANGUAGE, DASHBOARD_VISIT_LEGAL, DASHBOARD_VISIT_CONTACT, \
    DASHBOARD_VIEW_APPLICATION_DETAILS, DASHBOARD_VIEW_APPLICATION_DETAILS_OVERFLOW, DASHBOARD_VIEW_USER_DETAILS


def test_view_user_details(page: Page):
    ctx = RunContext(
        user=new_user(),
    )
    scenario = DASHBOARD_VIEW_USER_DETAILS()
    for step in scenario.steps:
        step(page, ctx)

def test_view_user_details(page: Page):
    ctx = RunContext(
        user=new_user(),
    )
    scenario = DASHBOARD_VIEW_USER_DETAILS()
    for step in scenario.steps:
        step(page, ctx)
