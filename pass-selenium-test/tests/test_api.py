"""API 직접 호출 테스트: 화면에 드러나지 않는 응답 내용과 UI로 넣을 수 없는 입력(21자 이상 비밀번호)을 검증한다."""
import pytest
import requests

from utils import TEST_PASSWORD, join_payload


@pytest.mark.xfail(
    reason="DEF-001(확인됨): 로그인 성공 응답 본문에 비밀번호 원문이 포함된다. 수정 후 이 표시를 지우고 통과를 확인할 것",
    strict=True,
)
def test_def001_login_response_must_not_contain_password(base_url, registered_user):
    """로그인 성공 응답에 password 키가 없어야 한다."""
    resp = requests.post(
        f"{base_url}/login/action",
        json={"userId": registered_user["user_id"], "password": registered_user["password"]},
        timeout=10,
    )
    assert resp.status_code == 200
    body = resp.json()
    assert "password" not in body, f"응답에 비밀번호가 포함됨: keys={list(body.keys())}"


def test_l08_sql_injection_string_is_not_accepted(base_url):
    """L-08 아이디에 SQL 인젝션 문자열 → 로그인 실패(400)."""
    resp = requests.post(
        f"{base_url}/login/action",
        json={"userId": "' OR '1'='1", "password": "x"},
        timeout=10,
    )
    assert resp.status_code == 400
    assert resp.json().get("msg") == "존재하지 않는 ID입니다."


@pytest.mark.xfail(
    reason="DEF-003 후보(미확인): BCrypt는 앞 72바이트만 사용하므로 긴 비밀번호의 뒷부분이 무시될 수 있음",
    strict=False,
)
def test_bcrypt_72byte_tail_must_not_be_ignored(base_url, new_user_id):
    """73바이트 이상 비밀번호로 가입한 뒤, 앞 72바이트만으로는 로그인되면 안 된다."""
    long_pw = "Aa1!" + "x" * 68 + "TAIL1234"  # 앞 72자 + 뒤 8자
    truncated = long_pw[:72]

    join = requests.post(f"{base_url}/join/action", data=join_payload(new_user_id, password=long_pw), timeout=10)
    assert join.status_code == 200, f"사전 가입 실패: {join.status_code} {join.text}"

    resp = requests.post(
        f"{base_url}/login/action",
        json={"userId": new_user_id, "password": truncated},
        timeout=10,
    )
    assert resp.status_code == 400, "비밀번호 뒷부분이 다른데도 로그인되었다 (72바이트 초과분 무시)"
