from selenium.webdriver.common.by import By

class JoinPage:
  URL = "http://localhost:8080/join"
  USER_ID = (By.NAME,"userId")
  PASSWORD = (By.NAME,"password")
  NAME = (By.NAME, "userName")
  PHONE = (By.NAME, "phone")
  EMAIL = (By.NAME, "eMail")
  KAKAO = (By.NAME, "kaKaoId")
  SUBMIT = (By.CSS_SELECTOR, "input[type='button'][value='회원가입']")
  
  def __init__(self, driver):
    self.driver = driver
    
    
  def open(self):
    self.driver.get(self.URL)
    
  def join(self, user_id, password, username, phone, email, kakao):
    self.driver.find_element(*self.USER_ID).send_keys(user_id)
    self.driver.find_element(*self.PASSWORD).send_keys(password)
    self.driver.find_element(*self.NAME).send_keys(username)
    self.driver.find_element(*self.PHONE).send_keys(phone)
    self.driver.find_element(*self.EMAIL).send_keys(email)
    self.driver.find_element(*self.KAKAO).send_keys(kakao)
    self.driver.find_element(*self.SUBMIT).click()