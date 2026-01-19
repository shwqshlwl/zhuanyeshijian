# 示例：简单计算器
def add(a, b):
    return a + b

def subtract(a, b):
    return a - b

def multiply(a, b):
    return a * b

def divide(a, b):
    if b == 0:
        return "错误：除数不能为0"
    return a / b

# 测试
print("简单计算器")
print("5 + 3 =", add(5, 3))
print("5 - 3 =", subtract(5, 3))
print("5 * 3 =", multiply(5, 3))
print("5 / 3 =", divide(5, 3))
