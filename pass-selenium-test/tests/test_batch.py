import os
import time
from datetime import datetime, timedelta
import pymysql
import pytest
import requests

BASE_URL = "http://localhost:8080"
BATCH_URL = "http://localhost:8081/job/launcher"
ADMIN = ("admin", "admin123")

@pytest.fixture
def db():
    conn = pymysql.connect(
        host="localhost", port=3306, user="root",
        password=os.getenv("DB_PASSWORD"),
        database="firstproject_db", charset="utf8mb4", autocommit=True,
    )
    yield conn
    conn.close()
    
def scalar(db, sql, args=None):
    with db.cursor() as cur:
        cur.execute(sql, args)
        return cur.fetchone()[0]
    
@pytest.fixture
def cleanup_marks(db):
    marks = {
        "bulk": scalar(db, "SELECT COALESCE(MAX(bulk_pass_seq), 0) FROM bulk_pass"),
        "pass": scalar(db, "SELECT COALESCE(MAX(pass_seq), 0) FROM pass"),
    }
    yield marks
    with db.cursor() as cur:
        cur.execute("DELETE FROM booking WHERE pass_seq > %s", (marks["pass"],))
        cur.execute("DELETE FROM pass WHERE pass_seq > %s", (marks["pass"],))
        cur.execute("DELETE FROM bulk_pass WHERE bulk_pass_seq > %s", (marks["bulk"],))
        
def add_bulk_pass(started_at):
    return requests.post(
        BASE_URL + "/admin/bulk-pass",
        auth=ADMIN,
        data={"packageSeq": 1, "userId": "A1000000",
              "startedAt": started_at.strftime("%Y-%m-%d %H:%M")},
        allow_redirects=False,
    )
    
def run_add_passes_job():
    return requests.post(
        BATCH_URL,
        json={"name": "addPassesJob", "jobParameters": {"run.id": str(time.time_ns())}},
        timeout=300,
    )
    

def test_every_booking_has_pass(db):
    orphans = scalar(
        db,
        "SELECT COUNT(*) FROM booking b "
        "  LEFT JOIN pass p ON b.pass_seq = p.pass_seq "
        " WHERE b.pass_seq IS NOT NULL AND p.pass_seq IS NULL",
    )
    
    assert orphans == 0, f"존재하지 않는 pass를 가리키는 booking {orphans}건"
    
def test_batch_start_time_boundary(db, cleanup_marks):
    ready = scalar(db, "SELECT COUNT(*) FROM bulk_pass WHERE status = 'READY'")
    
    if ready:
        pytest.skip(f"READY 상태의 bulk_pass {ready}건이 있어 정리 후 다시 실행")
        
    threshold = datetime.now() - timedelta(days=1)
    inside = add_bulk_pass(threshold + timedelta(minutes=5))   #등록시간이 분 단위라서 경계에 5분 여유를 둠
    outside = add_bulk_pass(threshold - timedelta(minutes=5))
    
    assert inside.status_code == 302 and outside.status_code == 302, \
        f"전체 실패 - 등록: {inside.status_code}, {outside.status_code}"
    
    with db.cursor() as cur:
        cur.execute(
            "SELECT bulk_pass_seq FROM bulk_pass WHERE bulk_pass_seq > %s ORDER BY bulk_pass_seq",
            (cleanup_marks["bulk"],),
        )
        inside_seq, outside_seq = [row[0] for row in cur.fetchall()]
        
    resp = run_add_passes_job()
    assert resp.status_code == 200 and "COMPLETE" in resp.text, f"배치 실패: {resp.status_code}, {resp.text}"
    
    with db.cursor() as cur:
        cur.execute(
            "SELECT bulk_pass_seq, status FROM bulk_pass WHERE bulk_pass_seq IN (%s, %s)",
            (inside_seq, outside_seq),
        )
        status = dict(cur.fetchall())
        
    assert status[inside_seq] == "COMPLETED", f"경계 안쪽 건이 처리되지 않음: {status[inside_seq]}"
    assert status[outside_seq] == "READY", f"경계 바깥쪽 건이 처리됨: {status[outside_seq]}"
    
    