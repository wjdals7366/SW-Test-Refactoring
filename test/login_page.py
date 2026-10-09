from selenium import webdriver
from selenium.webdriver.common.by import By


class LoginPage:
  USER_ID = (By.ID, "logEmail")
  PASSWORD = (By.ID, "logPass")
  SUBMIT = (By.CSS_SELECTOR, "input[type='button'][value='submit']")
  
  
  def __init__(self, driver):
    self.driver = driver
    
  
  def open(self):
    self.driver.get("http://localhost:8080/login")
    
  def login(self, user_id, password):
    self.driver.find_element(*self.USER_ID).send_keys(user_id)
    self.driver.find_element(*self.PASSWORD).send_keys(password)
    self.driver.find_element(*self.SUBMIT).click()
