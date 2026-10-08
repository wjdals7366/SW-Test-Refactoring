from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import WebDriverWait


class JoinPage:
    SUBMIT = (By.CSS_SELECTOR, "input[type='button'][value='회원가입']")

    def __init__(self, driver, base_url: str, timeout: int = 5):
        self.driver = driver
        self.base_url = base_url
        self.wait = WebDriverWait(driver, timeout)

    def open(self):
        self.driver.get(f"{self.base_url}/join")
        self.wait.until(EC.visibility_of_element_located((By.CSS_SELECTOR, "input[name='userId']")))
        return self

    def fill(self, **values):
        """name 속성 기준으로 입력 (userId, password, userName, phone, eMail, kaKaoId). 빈 문자열이면 비워 둔다."""
        for name, value in values.items():
            el = self.driver.find_element(By.CSS_SELECTOR, f"input[name='{name}']")
            el.clear()
            if value:
                el.send_keys(value)

    def submit(self):
        self.driver.find_element(*self.SUBMIT).click()

    def field_error(self, field: str) -> str:
        """입력칸 바로 아래 오류 문구 (Ajax 응답 후 채워질 때까지 대기)"""
        locator = (By.CSS_SELECTOR, f"input[name='{field}'] + p")
        self.wait.until(lambda d: d.find_element(*locator).text.strip() != "")
        return self.driver.find_element(*locator).text.strip()
