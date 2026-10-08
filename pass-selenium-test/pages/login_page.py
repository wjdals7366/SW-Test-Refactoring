from selenium.webdriver.common.by import By
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import WebDriverWait


class LoginPage:
    USER_ID = (By.ID, "logEmail")
    PASSWORD = (By.ID, "logPass")
    SUBMIT = (By.CSS_SELECTOR, "input[type='button'][value='submit']")

    def __init__(self, driver, base_url: str, timeout: int = 5):
        self.driver = driver
        self.base_url = base_url
        self.wait = WebDriverWait(driver, timeout)

    def open(self):
        self.driver.get(f"{self.base_url}/login")
        self.wait.until(EC.visibility_of_element_located(self.USER_ID))
        return self

    def _fill(self, user_id: str, password: str):
        self.driver.find_element(*self.USER_ID).send_keys(user_id)
        self.driver.find_element(*self.PASSWORD).send_keys(password)

    def login(self, user_id: str, password: str):
        self._fill(user_id, password)
        self.driver.find_element(*self.SUBMIT).click()

    def login_with_enter(self, user_id: str, password: str):
        self._fill(user_id, password)
        self.driver.find_element(*self.PASSWORD).send_keys(Keys.ENTER)

    def wait_until_passes(self):
        self.wait.until(EC.url_contains("/passes"))
