import pytest
import os
import pymysql
import uuid
import time
from selenium import webdriver
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from login_page import LoginPage
from join_page import JoinPage

PASSWORD = "asd123!!"

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
def new_user_id(db):
    user_id = 't' + uuid.uuid4().hex[:8]
    yield user_id 
    with db.cursor() as cur:
      cur.execute('DELETE FROM user WHERE user_id = %s',(user_id,))
    

# alert 관련 코드가 반복되므로 함수로 구현함
def read_alert(driver):
  alert = WebDriverWait(driver, 5).until(EC.alert_is_present())
  text = alert.text
  alert.accept()
  return text

def test_wrong_password_alert(driver):
  page = LoginPage(driver)
  page.open()
  page.login("wjdals5798","wrong1234!")
  
  text = read_alert(driver)
  
  expected = "비밀번호가 일치하지 않습니다."
  assert text == expected, f"기대: {expected} / 실제: {text}"
  
  
def test_unknown_id_alert(driver):
  page = LoginPage(driver)
  page.open()
  page.login("wrong5798","wrong1234!!")

  text = read_alert(driver)

  expected = "존재하지 않는 ID입니다."
  assert text == expected, f"기대: {expected} / 실제: {text}"
  
def test_stored_password_is_bcrypt_hash(db):
  with db.cursor() as cur:
    cur.execute("SELECT password FROM user WHERE user_id = %s", ("wjdals5798",))
    rows = cur.fetchall()
    
    assert len(rows) == 1, f"행 수가 1이어야 함: {len(rows)}"
    stored = rows[0][0]
    assert stored.startswith("$2a$10$"), f"BCrypt 접두가 아님: {stored[:7]}" # $2a$10$은 Bcrypt 접두어임
    assert len(stored) == 60, f"해시 길이가 60이어야 함: {len(stored)}"
    

def test_join_stores_hashed_password(driver, db, new_user_id):
  page = JoinPage(driver)
  page.open()
  page.join(new_user_id, PASSWORD, "테스터", "01012345678", "user@naver.com", "user@kakao.com")
  
  WebDriverWait(driver,5).until(EC.url_contains("/login"))  

  with db.cursor() as cur:
    cur.execute("SELECT password FROM user WHERE user_id = %s", (new_user_id,))
    rows = cur.fetchall()
    

  assert len(rows) == 1, f"가입 후 행 수: {len(rows)}"
  assert rows[0][0].startswith('$2'), 'BCrypt 해시가 아님'
  assert rows[0][0] != PASSWORD, '원문이 그대로 저장됨'