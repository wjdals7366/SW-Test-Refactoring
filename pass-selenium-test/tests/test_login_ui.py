import os
import uuid
import pymysql
import pytest
import requests
from selenium import webdriver
from selenium.common.exceptions import TimeoutException
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import WebDriverWait

from pages.login_page import LoginPage

BASE_URL = "http://localhost:8080"
PASSWORD = "asd123!!"
GREETING = "회원님 안녕하세요"
NO_ID_MSG = "존재하지 않는 ID입니다."
WRONG_PW_MSG = "비밀번호가 일치하지 않습니다."


@pytest.fixture
def driver():
    drv = webdriver.Chrome()
    yield drv
    drv.quit()

@pytest.fixture
def db():
    conn = pymysql.connect(
        host="localhost", port=3306, user="root",
        password=os.getenv("DB_PASSWORD"),
        database="firstproject_db", charset="utf8mb4", autocommit=True,
    )
    yield conn
    conn.close()
    
@pytest.fixture
def account(db):
    user_id = "t" + uuid.uuid4().hex[:8]
    resp = requests.post(
        BASE_URL + "/join/action",
        data={"userId": user_id, "password": PASSWORD, "userName": "테스터",
              "phone": "01012345678", "eMail": "t@example.com", "kaKaoId": "t_kakao"},
    )
    assert resp.status_code == 200, f"전체 실패 - 테스트 계정 가입: {resp.status_code} {resp.text}"
    
    yield user_id, PASSWORD
    
    with db.cursor() as cur:
        cur.execute("DELETE FROM user_group_mapping WHERE user_id = %s", (user_id,))
        cur.execute("DELETE FROM user WHERE user_id = %s", (user_id,))

def read_alert(driver, timeout=5):
    alert = WebDriverWait(driver, timeout).until(EC.alert_is_present())
    text = alert.text
    alert.accept()
    return text

    
def test_login_both_empty(driver):
    page = LoginPage(driver)
    page.open()
    page.login("", "")
    
    text = read_alert(driver)
    
    assert text == NO_ID_MSG, f"기대: {NO_ID_MSG} / 실제: {text}"
    assert driver.current_url.endswith("/login"), f"현재 주소: {driver.current_url}"
    
    
def test_login_empty_password(driver, account):
    user_id, _ = account            # _ 는 이 값을 쓰지 않는다는 의미
    page = LoginPage(driver)
    page.open()
    page.login(user_id, "")
    
    text = read_alert(driver)
    
    assert text == WRONG_PW_MSG, f"기대: {WRONG_PW_MSG} / 실제: {text}"


def test_login_with_enter_key(driver, account):
    page = LoginPage(driver)
    page.open()
    page.login_with_enter(*account)
    
    WebDriverWait(driver, 5).until(EC.url_contains("/passes"))
    WebDriverWait(driver, 5).until(lambda d: GREETING in d.page_source)
    
def test_logout_ends_session(driver, account):
    page = LoginPage(driver)
    page.open()
    page.login(*account)
    WebDriverWait(driver,5).until(lambda d: GREETING in d.page_source)
    
    driver.get(BASE_URL + "/login/logout")
    WebDriverWait(driver, 5).until(EC.url_matches("/login/?$"))
    
    driver.get(BASE_URL + "/passes")
    assert GREETING not in driver.page_source, "로그아웃 후에도 로그인 상태 표시됨(세션유지)"
    
    
def test_login_sql_injection_id(driver):
    page = LoginPage(driver)
    page.open()
    page.login("' OR '1'='1", "anything1!")
    
    text = read_alert(driver)
    
    assert text == NO_ID_MSG, f"기대: {NO_ID_MSG} / 실제: {text}"
    assert "/passes" not in driver.current_url, "SQL 구문으로 로그인이 우회됨"
    
    
def test_login_uppercase_id(driver, account):
    user_id, password = account
    page = LoginPage(driver)
    page.open()
    page.login(user_id.upper(), password)
    
    WebDriverWait(driver, 5).until(EC.url_contains("/passes"))
    




# DB 기본 설정 정책으로 대소문자 구분 안함을 확인했음 -> test_login_uppercase_id로 테스트케이스 변경
# def test_login_uppercase_id_observe(driver, account):
#     user_id, password = account
#     page = LoginPage(driver)
#     page.open()
#     page.login(user_id.upper(), password)
    
#     try:
#         text = read_alert(driver, timeout=3)
#         print(f"\n[L-09] 입력 {user_id.upper()} → 로그인 실패, alert: {text}")
#     except TimeoutException:
#         print(f"\n[L-09] 입력 {user_id.upper()} → 로그인 성공, 이동한 주소: {driver.current_url}")