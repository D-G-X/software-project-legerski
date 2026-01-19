from playwright.sync_api import Page

from testkit.factory import new_user, new_application, new_payment
from testkit.models import RunContext
from testkit.scenarios import CREATE_APPLICATION_FULL, CREATE_APPLICATION_DRAFT, CREATE_APPLICATION_DOCUMENTS_LATER, \
    CREATE_APPLICATION_EDIT_FULL, CREATE_APPLICATION_PAY_LATER


def test_create_application_draft(page: Page):
    ctx = RunContext(
        user=new_user(),
        application=new_application()
    )
    scenario = CREATE_APPLICATION_DRAFT()
    for step in scenario.steps:
        step(page, ctx)


def test_create_application_documents_later(page: Page):
    ctx = RunContext(
        user=new_user(),
        application=new_application()
    )
    scenario = CREATE_APPLICATION_DOCUMENTS_LATER()
    for step in scenario.steps:
        step(page, ctx)


def test_create_application_edit_full(page: Page):
    ctx = RunContext(
        user=new_user(),
        application=new_application(),
        payment=new_payment()
    )
    scenario = CREATE_APPLICATION_EDIT_FULL()
    for step in scenario.steps:
        step(page, ctx)


def test_create_application_pay_later(page: Page):
    ctx = RunContext(
        user=new_user(),
        application=new_application()
    )
    scenario = CREATE_APPLICATION_PAY_LATER()
    for step in scenario.steps:
        step(page, ctx)


def test_create_application_full(page: Page):
    ctx = RunContext(
        user=new_user(),
        application=new_application(),
        payment=new_payment()
    )
    scenario = CREATE_APPLICATION_FULL()
    for step in scenario.steps:
        step(page, ctx)
