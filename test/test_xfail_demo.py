import pytest


def add(a,b):
  return a + b

@pytest.mark.xfail(strict=True, reason="DEF-DEMO: add()가 뺄셈을 함") # strict=True 는 XPASS를 FAIL로 처리하여 결함 추적을 가능하게 해줌
def test_add_known_bug():
  assert add(2,3) == 5
  
def test_add_normal ():
  assert add(5,3) == 8