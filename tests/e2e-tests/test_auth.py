from testkit.factory import new_user
from testkit.scenarios import REGISTER_LOGIN_LOGOUT
from testkit.runner.ui_runner import register, login, logout

UI_STEP_MAP = {
    "register": register,
    "login": login,
    "logout": logout,
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
