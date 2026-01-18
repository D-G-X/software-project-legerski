from testkit.factory import new_user, new_admin, new_ballot_period_running
from testkit.runner.ui_runner import register, login, logout, create_ballot_period, request_reset_password
from testkit.scenarios import REGISTER_LOGIN_LOGOUT, ADMIN_LOGIN_CREATE_BALLOT, REGISTER_LOGIN_REFRESH_TOKEN, \
    REGISTER_FORGOT_PASSWORD

UI_STEP_MAP = {
    "register": register,
    "login": login,
    "logout": logout,
    "request_reset_password": request_reset_password,
    "refresh_login": login,
    "admin_login": login,
    "create_ballot_period": create_ballot_period,
}


def test_register_login_logout(page):
    user = new_user()

    for step in REGISTER_LOGIN_LOGOUT.steps:
        if step == "register":
            UI_STEP_MAP[step](page, user)
        elif step == "login":
            UI_STEP_MAP[step](page, user)
        elif step == "logout":
            UI_STEP_MAP[step](page, user)


def test_register_forgot_password(page):
    user = new_user()

    for step in REGISTER_FORGOT_PASSWORD.steps:
        if step == "register":
            UI_STEP_MAP[step](page, user)
        elif step == "request_reset_password":
            UI_STEP_MAP[step](page, user.email)


def test_register_login_refresh_login(page):
    user = new_user()

    for step in REGISTER_LOGIN_REFRESH_TOKEN.steps:
        if step == "register":
            UI_STEP_MAP[step](page, user)
        elif step == "login":
            UI_STEP_MAP[step](page, user)
        elif step == "refresh_login":
            UI_STEP_MAP[step](page, user)
        elif step == "logout":
            UI_STEP_MAP[step](page, user)


def test_admin_login_create_ballot(page):
    admin = new_admin()
    ballot_period = new_ballot_period_running()

    for step in ADMIN_LOGIN_CREATE_BALLOT.steps:
        if step == "admin_login":
            UI_STEP_MAP[step](page, admin)
        elif step == "create_ballot_period":
            UI_STEP_MAP[step](page, create_ballot_period(page,
                                                         ballot_period.start_date,
                                                         ballot_period.end_date))
