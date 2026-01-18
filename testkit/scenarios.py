from testkit.models import Scenario
from tests.e2e_tests.ui_runner import ui_create_ballot_period, ui_login, ui_logout, ui_register, ui_refresh_login, \
    ui_request_reset_password, ui_create_application_documents, ui_create_payment, ui_create_application_draft, \
    ui_skip_payment, ui_create_application, \
    ui_edit_application, ui_create_application_documents_edit, ui_create_application_documents_later, \
    ui_login_to_register, ui_change_language, ui_visit_legal, ui_visit_contact


def REGISTER_LOGIN_LOGOUT() -> Scenario:
    return Scenario(
        name="register_login_logout",
        steps=[
            lambda p, ctx: ui_register(p, ctx.user),
            lambda p, ctx: ui_login(p, ctx.user),
            lambda p, ctx: ui_logout(p, ctx.user),
        ],
    )


def REGISTER_VIA_LOGIN_LOGIN_LOGOUT() -> Scenario:
    return Scenario(
        name="register_login_logout",
        steps=[
            lambda p, ctx: ui_login_to_register(p),
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
        name="create_application_draft",
        steps=[
            lambda p, ctx: ui_register(p, ctx.user),
            lambda p, ctx: ui_login(p, ctx.user),
            lambda p, ctx: ui_create_application_draft(p, ctx.application, ctx.user),
            lambda p, ctx: ui_logout(p, ctx.user),
        ],
    )


def CREATE_APPLICATION_DOCUMENTS_LATER() -> Scenario:
    return Scenario(
        name="create_application_documents_later",
        steps=[
            lambda p, ctx: ui_register(p, ctx.user),
            lambda p, ctx: ui_login(p, ctx.user),
            lambda p, ctx: ui_create_application(p, ctx.application, ctx.user),
            lambda p, ctx: ui_create_application_documents_later(p),
            lambda p, ctx: ui_logout(p, ctx.user),
        ],
    )


def CREATE_APPLICATION_EDIT_FULL() -> Scenario:
    return Scenario(
        name="create_application_edit_full",
        steps=[
            lambda p, ctx: ui_register(p, ctx.user),
            lambda p, ctx: ui_login(p, ctx.user),
            lambda p, ctx: ui_create_application(p, ctx.application, ctx.user),
            lambda p, ctx: ui_create_application_documents_edit(p, ctx.application.id),
            lambda p, ctx: ui_edit_application(p, ctx.application, ctx.user),
            lambda p, ctx: ui_create_application_documents(p, ctx.application.id),
            lambda p, ctx: ui_create_payment(p, ctx.user, ctx.application, ctx.payment),
            lambda p, ctx: ui_logout(p, ctx.user),
        ],
    )


def CREATE_APPLICATION_PAY_LATER() -> Scenario:
    return Scenario(
        name="create_application_pay_later",
        steps=[
            lambda p, ctx: ui_register(p, ctx.user),
            lambda p, ctx: ui_login(p, ctx.user),
            lambda p, ctx: ui_create_application(p, ctx.application, ctx.user),
            lambda p, ctx: ui_create_application_documents(p, ctx.application.id),
            lambda p, ctx: ui_skip_payment(p, ctx.user, ctx.application, ctx.payment),
            lambda p, ctx: ui_logout(p, ctx.user),
        ],
    )


def CREATE_APPLICATION_FULL() -> Scenario:
    return Scenario(
        name="create_application_full",
        steps=[
            lambda p, ctx: ui_register(p, ctx.user),
            lambda p, ctx: ui_login(p, ctx.user),
            lambda p, ctx: ui_create_application(p, ctx.application, ctx.user),
            lambda p, ctx: ui_create_application_documents(p, ctx.application.id),
            lambda p, ctx: ui_create_payment(p, ctx.user, ctx.application, ctx.payment),
            lambda p, ctx: ui_logout(p, ctx.user),
        ],
    )


def ADMIN_LOGIN_CREATE_BALLOT() -> Scenario:
    return Scenario(
        name="admin_login_create_ballot",
        steps=[
            lambda p, ctx: ui_login(p, ctx.user),
            lambda p, ctx: ui_create_ballot_period(p, ctx.ballot.start_date, ctx.ballot.end_date),
            lambda p, ctx: ui_logout(p, ctx.user),
        ],
    )


def DASHBOARD_CHANGE_LANGUAGE() -> Scenario:
    return Scenario(
        name="dashboard_change_language",
        steps=[
            lambda p, ctx: ui_register(p, ctx.user),
            lambda p, ctx: ui_login(p, ctx.user),
            lambda p, ctx: ui_change_language(p, ctx.user),
            lambda p, ctx: ui_logout(p, ctx.user),
        ],
    )


def DASHBOARD_VISIT_CONTACT() -> Scenario:
    return Scenario(
        name="dashboard_visit_contact",
        steps=[
            lambda p, ctx: ui_visit_contact(p),
        ],
    )


def DASHBOARD_VISIT_LEGAL() -> Scenario:
    return Scenario(
        name="dashboard_visit_legal",
        steps=[
            lambda p, ctx: ui_visit_legal(p),
        ],
    )
