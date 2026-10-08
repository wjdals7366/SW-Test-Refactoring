"""가입 화면 테스트 (J-01 ~ J-05) + ET1: 비밀번호 BCrypt 해시 저장 확인"""
import bcrypt
import pytest
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import WebDriverWait

from pages.common import wait_alert_text
from pages.join_page import JoinPage
from utils import TEST_PASSWORD, fetch_all, join_payload, poll


def test_j01_join_success_and_et1_password_is_bcrypt_hash(driver, db, base_url, new_user_id):
    """J-01 정상 가입 → /login 이동. ET1 저장된 비밀번호가 BCrypt 해시인지 확인."""
    page = JoinPage(driver, base_url).open()
    page.fill(**join_payload(new_user_id))
    page.submit()

    WebDriverWait(driver, 5).until(EC.url_contains("/login"))

    rows = fetch_all(db, "SELECT password FROM user WHERE user_id = %s", (new_user_id,))
    assert len(rows) == 1, "가입한 계정이 user 테이블에 정확히 1행 있어야 한다"

    stored = rows[0][0]
    assert stored.startswith("$2"), f"BCrypt 접두($2)가 아님: {stored[:7]}"
    assert len(stored) == 60, f"BCrypt 해시 길이는 60이어야 한다: {len(stored)}"
    assert stored != TEST_PASSWORD, "원문 그대로 저장되면 안 된다"
    assert bcrypt.checkpw(TEST_PASSWORD.encode(), stored.encode()), "저장된 해시가 입력 비밀번호와 대응해야 한다"


def test_j02_user_id_too_short_shows_error(driver, db, base_url, new_user_id, user_cleanup):
    """J-02 아이디 4자 → 오류 문구 표시, 가입되지 않음."""
    short_id = new_user_id[:4]
    user_cleanup.append(short_id)

    page = JoinPage(driver, base_url).open()
    page.fill(**join_payload(short_id))
    page.submit()

    assert page.field_error("userId") == "아이디는 5~20 자리로 입력해주세요."
    assert "/join" in driver.current_url
    assert fetch_all(db, "SELECT 1 FROM user WHERE user_id = %s", (short_id,)) == ()


def test_j03_weak_password_shows_error(driver, db, base_url, new_user_id):
    """J-03 숫자만 있는 비밀번호 → 오류 문구 표시, 가입되지 않음."""
    page = JoinPage(driver, base_url).open()
    page.fill(**join_payload(new_user_id, password="12345678"))
    page.submit()

    assert page.field_error("password") == "비밀번호는 영문과 특수문자, 숫자를 포함하며 8자 이상이어야 합니다."
    assert fetch_all(db, "SELECT 1 FROM user WHERE user_id = %s", (new_user_id,)) == ()


def test_j04_duplicate_user_id_is_rejected(driver, db, base_url, registered_user):
    """J-04 이미 있는 아이디로 가입 → alert, 행 수 그대로."""
    page = JoinPage(driver, base_url).open()
    page.fill(**join_payload(registered_user["user_id"]))
    page.submit()

    assert wait_alert_text(driver) == "이미 존재하는 ID입니다."
    rows = fetch_all(db, "SELECT 1 FROM user WHERE user_id = %s", (registered_user["user_id"],))
    assert len(rows) == 1


@pytest.mark.xfail(
    reason="DEF-002 후보(미확인): LoginService.create()가 문자열을 == 로 비교해 빈 이름/이메일 검사가 동작하지 않을 것으로 예상",
    strict=False,
)
def test_j05_empty_name_and_email_should_be_rejected(driver, db, base_url, new_user_id):
    """J-05 이름·이메일을 비우고 가입 → 의도대로라면 거부되어야 한다. (결함이 있으면 xfail, 없으면 xpass)"""
    page = JoinPage(driver, base_url).open()
    page.fill(**join_payload(new_user_id, userName="", eMail=""))
    page.submit()

    created = poll(lambda: fetch_all(db, "SELECT 1 FROM user WHERE user_id = %s", (new_user_id,)), timeout=3)
    assert not created, "필수값(이름·이메일)이 비어 있는데 가입이 완료되었다"
