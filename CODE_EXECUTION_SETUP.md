# 代码执行环境配置指南

## 概述

本系统支持真实的代码执行功能，可以运行和评测以下编程语言：
- Java
- Python
- C
- C++
- JavaScript (Node.js)

## 环境要求

### Windows 系统

#### 1. Java
已安装（项目运行需要）

验证：
```bash
java -version
javac -version
```

#### 2. Python
下载并安装：https://www.python.org/downloads/

安装时勾选 "Add Python to PATH"

验证：
```bash
python --version
```

#### 3. GCC/G++ (C/C++)
安装 MinGW-w64：

1. 下载：https://sourceforge.net/projects/mingw-w64/
2. 安装到 `C:\mingw64`
3. 添加到系统环境变量 PATH：`C:\mingw64\bin`

验证：
```bash
gcc --version
g++ --version
```

#### 4. Node.js (JavaScript)
下载并安装：https://nodejs.org/

验证：
```bash
node --version
```

### Linux 系统

#### Ubuntu/Debian
```bash
# 更新包列表
sudo apt update

# 安装 Java
sudo apt install openjdk-17-jdk

# 安装 Python
sudo apt install python3

# 安装 GCC/G++
sudo apt install build-essential

# 安装 Node.js
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt install nodejs

# 验证安装
java -version
python3 --version
gcc --version
g++ --version
node --version
```

#### CentOS/RHEL
```bash
# 安装 Java
sudo yum install java-17-openjdk-devel

# 安装 Python
sudo yum install python3

# 安装 GCC/G++
sudo yum groupinstall "Development Tools"

# 安装 Node.js
curl -fsSL https://rpm.nodesource.com/setup_18.x | sudo bash -
sudo yum install nodejs
```

### macOS 系统

```bash
# 安装 Homebrew（如果未安装）
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# 安装 Java
brew install openjdk@17

# 安装 Python
brew install python

# 安装 GCC/G++（通过 Xcode Command Line Tools）
xcode-select --install

# 安装 Node.js
brew install node

# 验证安装
java -version
python3 --version
gcc --version
g++ --version
node --version
```

## 配置说明

### 临时文件目录

代码执行时会在系统临时目录创建工作目录：
- Windows: `%TEMP%\code-execution`
- Linux/macOS: `/tmp/code-execution`

确保应用有权限读写该目录。

### 安全配置

**重要提示**：当前实现适用于开发和测试环境。生产环境建议：

1. **使用 Docker 容器隔离**
   - 限制资源使用（CPU、内存、磁盘）
   - 隔离网络访问
   - 防止恶意代码破坏系统

2. **使用专门的代码评测服务**
   - Judge0: https://github.com/judge0/judge0
   - DMOJ: https://github.com/DMOJ/judge-server
   - 自建沙箱环境

3. **限制执行权限**
   - 使用低权限用户运行代码
   - 禁止访问敏感文件和目录
   - 限制系统调用

## 测试代码执行

### 测试 Java
```java
public class Main {
    public static void main(String[] args) {
        System.out.println("Hello World!");
    }
}
```

### 测试 Python
```python
print("Hello World!")
```

### 测试 C++
```cpp
#include <iostream>
using namespace std;

int main() {
    cout << "Hello World!" << endl;
    return 0;
}
```

### 测试 C
```c
#include <stdio.h>

int main() {
    printf("Hello World!\n");
    return 0;
}
```

### 测试 JavaScript
```javascript
console.log("Hello World!");
```

## 测试用例格式

在创建实验时，测试用例应该使用 JSON 格式：

```json
[
  {
    "input": "5\n3\n",
    "output": "8"
  },
  {
    "input": "10\n20\n",
    "output": "30"
  }
]
```

每个测试用例包含：
- `input`: 输入数据（通过标准输入传递）
- `output`: 期望的输出结果

## 故障排除

### 问题：找不到命令
**解决方案**：确保编译器/解释器已添加到系统 PATH 环境变量

### 问题：权限不足
**解决方案**：
- Windows: 以管理员身份运行应用
- Linux/macOS: 检查临时目录权限

### 问题：执行超时
**解决方案**：
- 检查代码是否有死循环
- 增加时间限制（在实验设置中）
- 优化代码性能

### 问题：内存不足
**解决方案**：
- 增加内存限制（在实验设置中）
- 优化代码内存使用

## 性能优化建议

1. **使用线程池**：已实现，最多同时执行 5 个代码任务
2. **定期清理临时文件**：系统会自动清理，但建议定期检查
3. **限制并发数**：避免系统资源耗尽
4. **监控资源使用**：使用系统监控工具观察 CPU、内存使用情况

## 生产环境部署建议

### 使用 Docker 容器（推荐）

创建 `docker-compose.yml`：

```yaml
version: '3.8'
services:
  judge0:
    image: judge0/judge0:latest
    ports:
      - "2358:2358"
    environment:
      - REDIS_HOST=redis
      - POSTGRES_HOST=postgres
    depends_on:
      - redis
      - postgres
  
  redis:
    image: redis:alpine
  
  postgres:
    image: postgres:alpine
    environment:
      - POSTGRES_PASSWORD=judge0
      - POSTGRES_DB=judge0
```

然后修改代码调用 Judge0 API 而不是本地执行。

## 支持的语言版本

- Java: 17+
- Python: 3.x
- GCC: 任意版本
- G++: 任意版本（支持 C++11/14/17）
- Node.js: 14+

## 更多信息

- 代码执行服务实现：`LocalCodeExecutionServiceImpl.java`
- 实验评测逻辑：`ExperimentServiceImpl.java`
- 测试用例格式：JSON 数组
