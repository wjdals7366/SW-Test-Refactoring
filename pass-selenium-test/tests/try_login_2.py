from selenium import webdriver
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from pages.login_page import LoginPage

driver = webdriver.Chrome()


page = LoginPage(driver)
page.open()
page.login("wjdals5798", "wrong1234!")

alert = WebDriverWait(driver, 5).until(EC.alert_is_present())
text = alert.text
alert.accept()
expected = "비밀번호가 일치하지 않습니다"
assert text == expected, f"기대: {expected} / 실제: {text}"
print("통과: ", text)

driver.quit()