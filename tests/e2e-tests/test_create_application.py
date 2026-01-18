from playwright.sync_api import Page

from testkit.factory import new_user, new_application, new_payment
from testkit.models import RunContext
from testkit.scenarios import CREATE_APPLICATION_DOCUMENT_PAYMENT


def test_create_application_document_payment(page: Page):
    ctx = RunContext(
        user=new_user(),
        application=new_application(),
        payment=new_payment()
    )
    scenario = CREATE_APPLICATION_DOCUMENT_PAYMENT()
    for step in scenario.steps:
        step(page, ctx)
