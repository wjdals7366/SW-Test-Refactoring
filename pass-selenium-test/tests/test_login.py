"""로그인 화면 테스트 (L-01 ~ L-07). 사전 계정은 API로 만들고, 로그인 동작은 UI로 검증한다."""
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import WebDriverWait

from pages.common import wait_alert_text
from pages.login_page import LoginPage
from utils import fetch_all

MSG_NO_USER = "존재하지 않는 ID입니다."
MSG_BAD_PW = "비밀번호가 일치하지 않습니다."


def _mapping_rows(db, user_id):
    return fetch_all(db, "SELECT user_group_id FROM user_group_mapping WHERE user_id = %s", (user_id,))


def test_l01_login_success_moves_to_passes_and_creates_session(driver, db, base_url, registered_user):
    """L-01 정상 로그인 → /passes 이동, 세션 쿠키 존재. DB: 로그인 후 HANBADA 그룹 매핑 1행(현재 동작 기준)."""
    assert _mapping_rows(db, registered_user["user_id"]) == (), "사전 조건: 로그인 전에는 그룹 매핑이 없어야 한다"

    page = LoginPage(driver, base_url).open()
    page.login(registered_user["user_id"], registered_user["password"])
    page.wait_until_passes()

    assert driver.get_cookie("JSESSIONID") is not None
    assert _mapping_rows(db, registered_user["user_id"]) == (("HANBADA",),)


def test_l02_unknown_user_id_shows_alert(driver, db, base_url, new_user_id):
    """L-02 존재하지 않는 ID → alert, /login 유지, 그룹 매핑 생성 안 됨."""
    page = LoginPage(driver, base_url).open()
    page.login(new_user_id, "Whatever1!")

    assert wait_alert_text(driver) == MSG_NO_USER
    assert "/login" in driver.current_url
    assert _mapping_rows(db, new_user_id) == ()


def test_l03_wrong_password_shows_alert(driver, db, base_url, registered_user):
    """L-03 올바른 ID + 틀린 비밀번호 → alert, 그룹 매핑 생성 안 됨."""
    page = LoginPage(driver, base_url).open()
    page.login(registered_user["user_id"], "Wrong1234!")

    assert wait_alert_text(driver) == MSG_BAD_PW
    assert "/login" in driver.current_url
    assert _mapping_rows(db, registered_user["user_id"]) == ()


def test_l04_empty_id_and_password_shows_alert(driver, base_url):
    """L-04 아이디·비밀번호 모두 빈 값 → '존재하지 않는 ID' 안내 (코드 해석 기반 예상값)."""
    page = LoginPage(driver, base_url).open()
    page.login("", "")

    assert wait_alert_text(driver) == MSG_NO_USER


def test_l05_empty_password_shows_alert(driver, base_url, registered_user):
    """L-05 아이디만 입력 → '비밀번호 불일치' 안내 (코드 해석 기반 예상값)."""
    page = LoginPage(driver, base_url).open()
    page.login(registered_user["user_id"], "")

    assert wait_alert_text(driver) == MSG_BAD_PW


def test_l06_login_with_enter_key(driver, base_url, registered_user):
    """L-06 비밀번호 입력 후 Enter로 제출해도 로그인된다."""
    page = LoginPage(driver, base_url).open()
    page.login_with_enter(registered_user["user_id"], registered_user["password"])
    page.wait_until_passes()


def test_l07_logout_returns_to_login(driver, base_url, registered_user):
    """L-07 로그인 후 /login/logout → /login 으로 돌아온다."""
    page = LoginPage(driver, base_url).open()
    page.login(registered_user["user_id"], registered_user["password"])
    page.wait_until_passes()

    driver.get(f"{base_url}/login/logout")
    WebDriverWait(driver, 5).until(EC.url_contains("/login"))
    assert driver.current_url.rstrip("/").endswith("/login")
