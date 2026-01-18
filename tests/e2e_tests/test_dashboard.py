from playwright.sync_api import Page

from testkit.factory import new_user
from testkit.models import RunContext
from testkit.scenarios import DASHBOARD_CHANGE_LANGUAGE, DASHBOARD_VISIT_LEGAL, DASHBOARD_VISIT_CONTACT


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
