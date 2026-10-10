import os
import pymysql as ps


conn = ps.connect(
  host='localhost',
  port=3306,
  user='root',
  password=os.getenv("DB_PASSWORD"), # 보안을 위해 DB PASSWORD는 환경변수에 설정하여 넘겨줌
  database='firstproject_db',
  charset='utf8mb4',
  autocommit=True,
)

with conn.cursor() as cur:
  cur.execute(
    "SELECT user_id, LEFT(password, 7), CHAR_LENGTH(password) FROM user WHERE user_id = %s",("wjdals5798",),
  )
  # %s를 써서 SQL 인젝션 방어
  # 값이 2개인 튜플을 넘겨줘야 하기 때문에 ("wjdals5798",) 을 넘겨줌
  row = cur.fetchall()
  print(row)
  
conn.close()