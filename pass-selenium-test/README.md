# pass-selenium-test

Sport Pass 웹 서비스(로그인·가입)의 Selenium + DB 검증 자동화 테스트.

## 준비 (Windows cmd)
```
cd pass-selenium-test
python -m venv .venv
.venv\Scripts\activate
pip install -r requirements.txt
set DB_PASSWORD=MySQL_root_비밀번호
```
- 웹 서버(8080)와 MySQL이 켜져 있어야 한다. 비밀번호는 코드나 파일에 쓰지 않고 환경변수로만 넘긴다.
- 크롬이 설치되어 있으면 드라이버는 Selenium이 자동으로 받는다.

## 실행
```
pytest                                   # 전체
pytest tests\test_login.py               # 로그인만
pytest -k j01                            # 특정 케이스만
set HEADLESS=1 && pytest                 # 브라우저 창 없이
```

## 환경변수
| 이름 | 기본값 |
|---|---|
| BASE_URL | http://localhost:8080 |
| DB_HOST / DB_PORT | localhost / 3306 |
| DB_USER / DB_NAME | root / firstproject_db |
| DB_PASSWORD | (필수) |
| HEADLESS | 1이면 창 없이 실행 |

## 결과 표시 해석
- `xfailed`: 알려진/예상한 결함이 실제로 재현됨 (스위트는 통과로 처리)
- `xpassed`: 결함으로 예상했지만 재현되지 않음 → 예상이 틀렸거나 이미 수정됨
- `DEF-001`은 strict라서, 결함을 고치면 `FAILED [XPASS(strict)]`가 뜬다 → 마커를 지우면 회귀 테스트가 된다.
