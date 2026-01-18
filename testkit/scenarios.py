from playwright.sync_api import Page

from testkit.models import Scenario, RunContext
from testkit.runner.api_runner import api_register, api_login
from testkit.runner.ui_runner import ui_create_ballot_period, ui_login, ui_logout, ui_register, ui_refresh_login, \
    ui_request_reset_password, ui_create_application_documents, ui_create_payment, \
    ui_create_application_full, ui_create_application_draft


def REGISTER_LOGIN_LOGOUT() -> Scenario:
    return Scenario(
        name="register_login_logout",
        steps=[
            lambda p, ctx: ui_register(p, ctx.user),
            lambda p, ctx: ui_login(p, ctx.user),
            lambda p, ctx: ui_logout(p, ctx.user),
        ],
    )


def REGISTER_FORGOT_PASSWORD() -> Scenario:
    return Scenario(
        name="register_forgot_password",
        steps=[
            lambda p, ctx: ui_register(p, ctx.user),
            lambda p, ctx: ui_request_reset_password(p, ctx.user.email),
        ],
    )


def REGISTER_LOGIN_REFRESH_TOKEN() -> Scenario:
    return Scenario(
        name="register_login_refresh_token",
        steps=[
            lambda p, ctx: ui_register(p, ctx.user),
            lambda p, ctx: ui_login(p, ctx.user),
            lambda p, ctx: ui_refresh_login(p, ctx.user),
            lambda p, ctx: ui_logout(p, ctx.user),
        ],
    )


def CREATE_APPLICATION_DRAFT() -> Scenario:
    return Scenario(
        name="create_application_document_payment",
        steps=[
            lambda p, ctx: ui_register(p, ctx.user),
            lambda p, ctx: ui_login(p, ctx.user),
            lambda p, ctx: ui_create_application_draft(p, ctx.application, ctx.user),
        ],
    )


def CREATE_APPLICATION_FULL() -> Scenario:
    return Scenario(
        name="create_application_document_payment",
        steps=[
            lambda p, ctx: ui_register(p, ctx.user),
            lambda p, ctx: ui_login(p, ctx.user),
            lambda p, ctx: ui_create_application_full(p, ctx.application, ctx.user),
            lambda p, ctx: ui_create_application_documents(p, ctx.application.id),
            lambda p, ctx: ui_create_payment(p, ctx.user, ctx.application, ctx.payment),
        ],
    )


def ADMIN_LOGIN_CREATE_BALLOT() -> Scenario:
    return Scenario(
        name="admin_login_create_ballot",
        steps=[
            lambda p, ctx: ui_login(p, ctx.user),
            lambda p, ctx: ui_create_ballot_period(p, ctx.ballot.start_date, ctx.ballot.end_date),
        ],
    )
