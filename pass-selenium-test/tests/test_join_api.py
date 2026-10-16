import os
import uuid
import pymysql
import pytest
import requests

BASE_URL = "http://localhost:8080"
VALID_PW = "asd123!!"   # 영문 + 숫자 + 특수문자 8자 → 비밀번호 규칙 충족


# 서버가 돌려주는 문구 (UserDto, LoginService 기준)
ID_ERROR = "아이디는 5~20 자리로 입력해주세요."
PW_ERROR = "비밀번호는 영문과 특수문자, 숫자를 포함하며 8자 이상이어야 합니다."
EMPTY_MSG = "입력이 안된 사항이 있습니다."
DUP_MSG = "이미 존재하는 ID입니다."

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
def created_ids(db):
    ids = []
    yield ids
    with db.cursor() as cur:
        for user_id in ids:         # 테스트로 생성한 ID가 DB에 남지 않게 지워주는 작업
            cur.execute("DELETE FROM user_group_mapping WHERE user_id = %s", (user_id,))
            cur.execute("DELETE FROM user WHERE user_id = %s", (user_id,))
            

def make_id(length):
    return ("t" + uuid.uuid4().hex)[:length]


def post_join(user_id, password=VALID_PW, name="테스터", phone="01012345678",
              email="test@example.com", kakao="t_kakao"):
    return requests.post(
        BASE_URL + "/join/action",
        data={"userId": user_id, "password": password, "userName": name,
              "phone": phone, "eMail": email, "kaKaoId": kakao}
    )
    
def post_login(user_id, password):
    return requests.post(BASE_URL + "/login/action", json={"userId": user_id, "password": password})

def count_user(db, user_id):
    with db.cursor() as cur:
        cur.execute("SELECT COUNT(*) FROM user WHERE user_id = %s", (user_id,))
        return cur.fetchone()[0]
    

def test_join_from_ok(db, created_ids):
    user_id = make_id(9)
    created_ids.append(user_id)
    
    resp = post_join(user_id)
    
    assert resp.status_code == 200, f"상태 코드 : {resp.status_code}, 본문: {resp.text}"
    assert count_user(db, user_id) == 1, f"성공 응답이지만 DB에 행이 없음"
    
    

@pytest.mark.parametrize("field", [
    pytest.param("name", id="JN-04-empty-name"),
    pytest.param("email", id="JN-05-empty-email"),
])
def test_join_empty_field(db, created_ids, field):
    user_id = make_id(9)
    created_ids.append(user_id)
    
    resp = post_join(user_id, **{field: ""}) # 해당값 빈칸 입력
    count = count_user(db, user_id)
    
    assert count == 0, f"빈칸 {field}인데 저장됨 (상태코드 {resp.status_code}, 행 수 {count})"    
    assert resp.status_code == 400, f"상태 코드: {resp.status_code}"
    assert resp.json().get("msg") == EMPTY_MSG, f"본문: {resp.text}"
    
  
def test_join_rejects_duplicate_id(db, created_ids):
    user_id = make_id(9)
    created_ids.append(user_id)
    
    first = post_join(user_id)
    assert first.status_code == 200, f"전제 실패 - 첫 가입: {first.status_code} {first.text}"
    
    second = post_join(user_id)
    
    assert second.status_code == 400, f"상태 코드: {second.status_code}"
    assert second.json().get("msg") == DUP_MSG, f"본문: {second.text}"
    assert count_user(db, user_id) == 1, "중복 가입으로 행이 늘어남"
    

@pytest.mark.parametrize("length", [
    pytest.param(5, id="JN-08-id-5"),
    pytest.param(20, id="JN-09-id-20"),
])
def test_join_accepts_id_length(db, created_ids, length):
    user_id = make_id(length)
    created_ids.append(user_id)
    assert len(user_id) == length
    
    resp = post_join(user_id)
    
    assert resp.status_code == 200, f"{length}자 거부됨: {resp.status_code} {resp.text}"
    assert count_user(db, user_id) == 1
    
    
@pytest.mark.parametrize("length", [
    pytest.param(4, id="JN-07-id-4"),
    pytest.param(21, id="JN-10-id-21"),
])
def test_join_rejects_id_length(db, created_ids, length):
    user_id = make_id(length)
    created_ids.append(user_id)
    assert len(user_id) == length
    
    resp = post_join(user_id)
    
    assert resp.status_code == 400, f"{length}자 허용됨: {resp.status_code}"
    assert resp.json().get("id_error") == ID_ERROR, f"본문: {resp.text}"
    assert count_user(db, user_id) == 0
    

def test_join_accpepts_password_8_chars(db, created_ids):
    user_id = make_id(9)
    created_ids.append(user_id)
    
    resp = post_join(user_id, password="Abcdef1!")
    
    assert resp.status_code == 200, f"8자 비밀번호 거부됨: {resp.status_code} {resp.text}"
    assert count_user(db, user_id) == 1
    
@pytest.mark.parametrize("password", [
    pytest.param("Abc12!x", id="JN-11-pw-7-chars"),
    pytest.param("Abcd123456", id="JN-13-pw-no-special"),
])
def test_join_rejects_password(db, created_ids, password):
    user_id = make_id(9)
    created_ids.append(user_id)
    
    resp = post_join(user_id, password=password)
    
    assert resp.status_code == 400, f"규칙 위반 비밀번호가 허용됨: {resp.status_code}"
    assert resp.json().get("pw_error") == PW_ERROR, f"본문: {resp.text}"
    assert count_user(db, user_id) == 0
    

@pytest.mark.xfail(strict=True, reason="DEF-003: Bcrypt는 72바이트까지만 체크하고 이후 바이트는 무시해 뒤 8자가 다른 비밀번호로도 로그인된다.")
def test_password_bytes_after_72_are_checked(created_ids):
    user_id = make_id(9)
    created_ids.append(user_id)
    
    head = "Aa1!" + "x" * 68 
    password = head + "TAIL1234"
    other = head + "DIFF5678"
    assert len(head.encode("utf-8")) == 72
    
    join = post_join(user_id, password=password)
    assert join.status_code == 200, f"전제 실패 - 가입: {join.status_code} {join.text}"
    ok = post_login(user_id, password)
    assert ok.status_code == 200, f"전제 실패 - 가입한 비밀번호로 로그인 안됨: {ok.text}"
    
    resp = post_login(user_id, other)
    
    # 대조군: 앞 72자 안쪽(첫 글자)이 다르면 거부되어야 한다
    inner = "B" + password[1:]
    control = post_login(user_id, inner)
    assert control.status_code == 400, f"전제 실패 - 앞부분이 달라도 로그인됨: {control.status_code}"
    
    assert resp.status_code == 400, f"뒤 8자가 다른 비밀번호로 로그인됨 (상태코드 {resp.status_code})"
    
    
    
def test_join_rejects_case_only_duplicate(db, created_ids):
    user_id = make_id(9)
    created_ids.append(user_id)
    
    first = post_join(user_id)
    assert first.status_code == 200, f"전제 실패 - 첫 가입: {first.status_code} {first.text}"
    
    second = post_join(user_id.upper())
    
    assert second.status_code == 400, f"대소문자만 다른 아이디로 가입됨: {second.status_code} {second.text}"
    assert second.json().get("msg") == DUP_MSG, f"본문 : {second.text}"