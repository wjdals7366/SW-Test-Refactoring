import requests
import os

BASE_URL = "http://localhost:8080"
USER_ID = "wjdals5798"
PASSWORD = "asd123!!"

def post_login(user_id, password):
  return requests.post(
    BASE_URL + "/login/action",
    json = {"userId" : user_id, "password": password},
  )

def test_login_success_status():
  resp = post_login(USER_ID,PASSWORD)
  assert resp.status_code == 200, f"상태 코드: {resp.status_code}"
  
def test_login_response_has_no_password():
  resp = post_login(USER_ID,PASSWORD)
  assert PASSWORD not in resp.text, "응답에 비밀번호 원문이 포함됨"
  
def test_login_wrong_password():
  resp = post_login(USER_ID, "wrong1234!!")
  assert resp.status_code == 400, f"상태코드 : {resp.status_code}"
  assert resp.json()["msg"] == "비밀번호가 일치하지 않습니다.", f"본문 {resp.text}"
  
  
def test_login_unknown_id():
  resp = post_login("no_such_user_9999", "wrong1234!!")
  assert resp.status_code == 400, f"상태코드 : {resp.status_code}"
  assert resp.json()["msg"] == "존재하지 않는 ID입니다.", f"본문 {resp.text}"
