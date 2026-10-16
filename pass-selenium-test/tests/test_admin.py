import json
import os
import re

import pymysql
import pytest
import requests
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import Select, WebDriverWait

BASE_URL = "http://localhost:8080"
ADMIN = ("admin", "admin123")
DATE_ALERT = "올바른 날짜 형식을 입력해주세요. (예: 2022-09-01 00:00)"

@pytest.fixture
def db():
    conn =pymysql.connect(
        host="localhost", port = 3306, user = "root",
        password=os.getenv("DB_PASSWORD"),
        database="firstproject_db", charset="utf8mb4", autocommit=True,
    )
    yield conn
    conn.close()
    
@pytest.fixture
def driver():
    drv = webdriver.Chrome()
    yield drv
    drv.quit()
    
def get_dashboard(to=None):
    params = {"to": to} if to else None
    return requests.get(BASE_URL + "/admin", params=params, auth=ADMIN)

def extract_chart(html):
    match = re.search(r"let chartData = (\{.*?\});", html, re.S)
    assert match, "대시보드에서 chartData를 찾지 못함"
    return json.loads(match.group(1))

def count_bulk_pass(db):
    with db.cursor() as cur:
        cur.execute("SELECT COUNT(*) FROM bulk_pass")
        return cur.fetchone()[0]
    

def test_dashboard_without_dbte():
    resp = get_dashboard()
    
    assert resp.status_code == 200, f"상태코드: {resp.status_code}"
    assert "Whitelabel" not in resp.text, "오류 페이지가 표시됨"
    
def test_dashboard_with_date():
    resp = get_dashboard("2022-09-10")
    
    assert resp.status_code == 200, f"상태 코드: {resp.status_code}"
    chart = extract_chart(resp.text)
    assert "09-01" in chart["labels"], f"기준일 범위의 통계가 없음: {chart['labels']}"
    

def test_bulk_pass_rejects_bad_date(driver, db):
    before = count_bulk_pass(db)
    
    driver.get("http://admin:admin123@localhost:8080/admin/bulk-pass")
    Select(driver.find_element(By.ID, "packageSeq")).select_by_value("1")
    driver.find_element(By.ID, "userId").send_keys("A1000000")
    driver.find_element(By.ID, "startedAtInput").send_keys("2026/12/31 10시")
    driver.find_element(By.CSS_SELECTOR, "input[type='submit']").click()
    
    alert = WebDriverWait(driver, 5).until(EC.alert_is_present())
    text = alert.text
    alert.accept()
    
    assert text == DATE_ALERT, f"기대: {DATE_ALERT} / 실제: {text}"
    assert count_bulk_pass(db) == before, "형식이 틀린 일시로 등록됨"
    
    
def test_chart_matches_db(db):
    chart = extract_chart(get_dashboard("2022-09-10").text)
    chart_map = {
        label: (attended, cancelled)
        for label, attended, cancelled in zip(
            chart["labels"], chart["attendedCounts"], chart["cancelledCounts"])
    }
    
    with db.cursor() as cur:
        cur.execute(
            "SELECT DATE_FORMAT(statistics_at, '%%m-%%d'), "
            "       SUM(attended_count), SUM(cancelled_count) "
            "  FROM statistics "
            " WHERE statistics_at BETWEEN %s AND %s "
            " GROUP BY statistics_at",
            ("2022-08-31 00:00:00", "2022-09-10 00:00:00"),
        )
        db_map = {label: (int(a), int(c)) for label, a, c in cur.fetchall()}
        
    assert chart_map == db_map, f"\n그래프: {chart_map}\nDB     : {db_map}"