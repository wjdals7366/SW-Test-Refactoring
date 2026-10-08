from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
# import time

user_id = "wjdals5798"
user_pw = "dkskaksk123!!"

driver = webdriver.Chrome()         # Chrome 드라이버 입력
driver.get("http://localhost:8080/login")       # 로그인 화면으로 이동


# 1)아이디 입력칸을 찾아서 아이디를 입력
driver.find_element(By.ID, "logEmail").send_keys(user_id)

# 2) 비밀번호 입력칸을 찾아서 비밀번호를 입력 
driver.find_element(By.ID, "logPass").send_keys(user_pw)

# 3) 로그인 버튼을 찾아서 클릭
driver.find_element(By.CSS_SELECTOR, "input[type='button'][value='submit']").click()

alert = WebDriverWait(driver, 5).until(EC.alert_is_present())
print(alert.text)
text = alert.text
alert.accept()
assert text == "비밀번호가 일치하지 않습니다"
print("통과:", text)


# time.sleep(2) WebDriverWait 로 고도화
#WebDriverWait(driver, 5).until(EC.url_contains("/passes"))
#print(driver.current_url)
driver.quit()