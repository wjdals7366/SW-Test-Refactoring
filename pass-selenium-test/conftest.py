"""공통 fixture: 브라우저, DB 연결, 테스트 계정 생성/정리"""
import os

import pymysql
import pytest
import requests
from selenium import webdriver
from selenium.webdriver.chrome.options import Options

from utils import BASE_URL, TEST_PASSWORD, join_payload, make_user_id


@pytest.fixture(scope="session")
def base_url():
    return BASE_URL


@pytest.fixture
def driver():
    opts = Options()
    if os.getenv("HEADLESS") == "1":
        opts.add_argument("--headless=new")
    opts.add_argument("--window-size=1280,900")
    drv = webdriver.Chrome(options=opts)  # 드라이버는 Selenium Manager가 자동 관리
    yield drv
    drv.quit()


@pytest.fixture
def db():
    password = os.getenv("DB_PASSWORD")
    if not password:
        pytest.fail("DB_PASSWORD 환경변수가 필요합니다. (cmd: set DB_PASSWORD=비밀번호)")
    conn = pymysql.connect(
        host=os.getenv("DB_HOST", "localhost"),
        port=int(os.getenv("DB_PORT", "3306")),
        user=os.getenv("DB_USER", "root"),
        password=password,
        database=os.getenv("DB_NAME", "firstproject_db"),
        charset="utf8mb4",
        autocommit=True,  # 오래된 스냅샷을 읽지 않도록 자동 커밋
    )
    yield conn
    conn.close()


@pytest.fixture
def user_cleanup(db):
    """테스트가 만든 계정을 끝나고 삭제한다. 정리할 아이디를 리스트에 추가해 두면 된다."""
    ids = []
    yield ids
    with db.cursor() as cur:
        for uid in ids:
            cur.execute("DELETE FROM user_group_mapping WHERE user_id = %s", (uid,))
            cur.execute("DELETE FROM user WHERE user_id = %s", (uid,))


@pytest.fixture
def new_user_id(user_cleanup):
    """아직 가입하지 않은 고유 아이디 (종료 시 정리 대상에 자동 등록)"""
    uid = make_user_id()
    user_cleanup.append(uid)
    return uid


@pytest.fixture
def registered_user(new_user_id, base_url):
    """사전 조건용: API로 빠르게 가입시킨 계정 (가입 UI 자체는 test_join에서 검증)"""
    resp = requests.post(f"{base_url}/join/action", data=join_payload(new_user_id), timeout=10)
    assert resp.status_code == 200, f"사전 가입 실패: {resp.status_code} {resp.text}"
    return {"user_id": new_user_id, "password": TEST_PASSWORD}
