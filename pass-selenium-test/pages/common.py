from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import WebDriverWait


def wait_alert_text(driver, timeout: int = 5) -> str:
    """브라우저 alert이 뜰 때까지 기다렸다가 문구를 읽고 닫는다."""
    alert = WebDriverWait(driver, timeout).until(EC.alert_is_present())
    text = alert.text
    alert.accept()
    return text
