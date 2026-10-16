import os
import uuid
import pymysql
import pytest
import requests

BASE_URL = "http://localhost:8080"
TEST_PW = "asd123!!"


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
    user_id = "t" + uuid.uuid4().hex[:8]
    yield user_id
    with db.cursor() as cur:
        cur.execute("DELETE FROM user WHERE user_id = %s", (user_id,))


def post_join_json(user_id, password=TEST_PW, name="테스터",
              phone="01012345678", email="t@example.com", kakao="t_kakao"):
    return requests.post(
        BASE_URL + "/join/action",
        json={"userId": user_id, "password": password, "userName": name,
              "phone": phone, "eMail": email, "kaKaoId": kakao},
    )
    

@pytest.mark.xfail(strict=True, reason="DEF-004: JSON 본문을 읽지 못해 예외가 나지만 catch에서 삼켜 저장 없이 200 응답")
def test_join_json_not_reported_as_success(db, new_user_id):
    resp = post_join_json(new_user_id)
    
    with db.cursor() as cur:
        cur.execute("SELECT COUNT(*) FROM user WHERE user_id = %s", (new_user_id,))
        count = cur.fetchone()[0]
        
    assert count == 1 or resp.status_code != 200, \
        f"저장 없이 성공 응답: 상태 코드 {resp.status_code}, 본문 {resp.text}, 행 수 {count}"

    
    
    # 해당 테스트는 test_join_api.py로 이동함
# def test_join_ok(new_user_id):
#   resp = post_join_json(new_user_id)
#   print(resp.status_code, resp.text)
#   assert resp.status_code == 200, f"상태 코드 : {resp.status_code}"
  
  
    # 해당 테스트는 test_join_api.py로 이동함
# def test_join_empty_name_observe(db, new_user_id):
#     resp = post_join_json(new_user_id, name="")
#     print(resp.status_code, resp.text)

#     with db.cursor() as cur:
#         cur.execute("SELECT COUNT(*) FROM user WHERE user_id = %s", (new_user_id,))
#         count = cur.fetchone()[0]
#     print("행 수:", count)

