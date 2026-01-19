from playwright.sync_api import Page

from testkit.factory import new_user, new_application
from testkit.models import RunContext
from testkit.scenarios import DASHBOARD_CHANGE_LANGUAGE, DASHBOARD_VISIT_LEGAL, DASHBOARD_VISIT_CONTACT, \
    DASHBOARD_VIEW_APPLICATION_DETAILS, DASHBOARD_VIEW_APPLICATION_DETAILS_OVERFLOW


def test_view_application_details(page: Page):
    ctx = RunContext(
        user=new_user(),
        application=new_application()
    )
    scenario = DASHBOARD_VIEW_APPLICATION_DETAILS()
    for step in scenario.steps:
        step(page, ctx)


def test_view_application_details_overflow(page: Page):
    ctx = RunContext(
        user=new_user(),
        application=new_application()
    )
    scenario = DASHBOARD_VIEW_APPLICATION_DETAILS_OVERFLOW()
    for step in scenario.steps:
        if step == scenario.steps[2]:
            ctx.application = new_application()
        step(page, ctx)


def test_dashboard_change_language(page: Page):
    ctx = RunContext(
        user=new_user()
    )
    scenario = DASHBOARD_CHANGE_LANGUAGE()
    for step in scenario.steps:
        step(page, ctx)


def test_dashboard_visit_contact(page: Page):
    ctx = RunContext()
    scenario = DASHBOARD_VISIT_CONTACT()
    for step in scenario.steps:
        step(page, ctx)


def test_dashboard_visit_legal(page: Page):
    ctx = RunContext()
    scenario = DASHBOARD_VISIT_LEGAL()
    for step in scenario.steps:
        step(page, ctx)
