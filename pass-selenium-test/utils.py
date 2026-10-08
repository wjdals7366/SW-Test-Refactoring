"""테스트 공통 상수와 보조 함수"""
import os
import time
import uuid

BASE_URL = os.getenv("BASE_URL", "http://localhost:8080")

# 가입 규칙(영문+숫자+특수문자, 8자 이상)을 만족하고 가입 화면 입력 제한(20자) 안에 들어오는 테스트 전용 값
TEST_PASSWORD = "Test1234!"


def make_user_id() -> str:
    """5~20자 규칙을 만족하는, 실행마다 고유한 테스트 아이디 (총 12자)"""
    return "at" + uuid.uuid4().hex[:10]


def join_payload(user_id: str, **overrides) -> dict:
    """가입 폼과 같은 필드 구성. 길이 제한(20자)을 넘지 않도록 값을 만든다."""
    data = {
        "userId": user_id,
        "password": TEST_PASSWORD,
        "userName": "자동화테스트",
        "phone": "010-1234-5678",
        "eMail": f"{user_id}@t.com",
        "kaKaoId": f"k_{user_id}",
    }
    data.update(overrides)
    return data


def poll(fn, timeout: float = 3.0, interval: float = 0.3):
    """fn()이 참 값을 돌려줄 때까지 기다린다. 시간이 지나면 마지막 값을 반환한다."""
    end = time.time() + timeout
    value = fn()
    while not value and time.time() < end:
        time.sleep(interval)
        value = fn()
    return value


def fetch_all(db, sql: str, params=()):
    with db.cursor() as cur:
        cur.execute(sql, params)
        return cur.fetchall()
