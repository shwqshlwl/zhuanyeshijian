# 真实代码执行功能使用指南

## 🎉 功能概述

系统现在支持真实的代码执行和自动评测功能！支持以下编程语言：
- ☕ Java
- 🐍 Python
- 🔧 C
- ⚙️ C++
- 📜 JavaScript (Node.js)

## 📋 快速开始

### 1. 安装必要的编译器和运行环境

#### Windows 用户

**Java**（已安装）
```bash
java -version
javac -version
```

**Python**
1. 下载：https://www.python.org/downloads/
2. 安装时勾选 "Add Python to PATH"
3. 验证：`python --version`

**GCC/G++ (C/C++)**
1. 下载 MinGW-w64：https://sourceforge.net/projects/mingw-w64/
2. 安装到 `C:\mingw64`
3. 添加到 PATH：`C:\mingw64\bin`
4. 验证：`gcc --version` 和 `g++ --version`

**Node.js (JavaScript)**
1. 下载：https://nodejs.org/
2. 安装
3. 验证：`node --version`

#### Linux 用户

```bash
# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-17-jdk python3 build-essential nodejs

# CentOS/RHEL
sudo yum install java-17-openjdk-devel python3 gcc gcc-c++ nodejs
```

### 2. 重启后端服务

修改了 Java 代码，需要重启 Spring Boot 应用。

### 3. 测试功能

#### 方式一：使用测试数据

执行 SQL 脚本插入测试实验：
```sql
-- 执行 experiment-test-data.sql
```

这会创建 5 个测试实验：
1. 两数相加 (Java)
2. Hello World (Python)
3. 数组求和 (C++)
4. 斐波那契数列 (JavaScript)
5. 判断奇偶数 (C)

#### 方式二：手动创建实验

1. 使用教师账号登录
2. 进入"实验管理"
3. 点击"创建实验"
4. 填写实验信息

## 📝 创建实验示例

### 基本信息
- **实验名称**：两数相加
- **所属课程**：选择课程
- **编程语言**：Java
- **时间限制**：5000 毫秒
- **内存限制**：256 MB
- **总分**：100

### 实验描述
```
编写一个程序，读取两个整数并输出它们的和。
```

### 实验要求
```
从标准输入读取两个整数（每行一个），计算它们的和并输出。
```

### 模板代码
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        // 读取第一个数
        int a = scanner.nextInt();
        // 读取第二个数
        int b = scanner.nextInt();
        // 输出和
        System.out.println(a + b);
        scanner.close();
    }
}
```

### 测试用例（JSON 格式）
```json
[
    {"input": "5\n3\n", "output": "8"},
    {"input": "10\n20\n", "output": "30"},
    {"input": "100\n200\n", "output": "300"},
    {"input": "-5\n5\n", "output": "0"},
    {"input": "0\n0\n", "output": "0"}
]
```

**注意**：
- `\n` 表示换行
- `input` 是通过标准输入传递给程序的数据
- `output` 是期望的输出结果（会自动去除行尾空格）

## 🧪 测试代码执行

### 学生端操作

1. **登录学生账号**
2. **进入实验详情页**
3. **编写代码**
   ```java
   import java.util.Scanner;
   
   public class Main {
       public static void main(String[] args) {
           Scanner scanner = new Scanner(System.in);
           int a = scanner.nextInt();
           int b = scanner.nextInt();
           System.out.println(a + b);
           scanner.close();
       }
   }
   ```

4. **点击"运行测试"**
   - 会快速执行代码并显示输出
   - 不会保存提交记录
   - 适合调试代码

5. **点击"提交代码"**
   - 会运行所有测试用例
   - 自动评分
   - 保存提交记录
   - 显示详细的测试结果

### 教师端操作

1. **查看学生提交列表**
   - 进入实验详情页
   - 查看所有学生的提交情况
   - 可以看到得分、通过率等信息

2. **查看学生代码**
   - 点击"查看代码"按钮
   - 查看学生提交的完整代码

## 🔍 评测流程

### 运行测试（快速测试）
1. 接收代码和语言
2. 创建临时工作目录
3. 编译代码（如需要）
4. 执行代码
5. 返回输出结果
6. 清理临时文件

### 提交代码（完整评测）
1. 保存提交记录（状态：评测中）
2. 异步执行评测：
   - 解析测试用例
   - 逐个运行测试用例
   - 比较输出结果
   - 计算得分
3. 更新提交记录（状态：通过/未通过）
4. 保存详细结果

### 评测状态
- **0** - 待评测
- **1** - 评测中
- **2** - 通过（所有测试用例通过）
- **3** - 未通过（部分测试用例失败）
- **4** - 编译错误
- **5** - 运行错误

## 📊 测试用例格式详解

### 基本格式
```json
[
    {
        "input": "输入数据",
        "output": "期望输出"
    }
]
```

### 多行输入
使用 `\n` 表示换行：
```json
{
    "input": "第一行\n第二行\n第三行\n",
    "output": "结果"
}
```

### 空输入
```json
{
    "input": "",
    "output": "Hello World!"
}
```

### 多个测试用例
```json
[
    {"input": "5\n3\n", "output": "8"},
    {"input": "10\n20\n", "output": "30"},
    {"input": "100\n200\n", "output": "300"}
]
```

## 🎯 各语言示例

### Java
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int n = scanner.nextInt();
        System.out.println(n * 2);
        scanner.close();
    }
}
```

### Python
```python
n = int(input())
print(n * 2)
```

### C++
```cpp
#include <iostream>
using namespace std;

int main() {
    int n;
    cin >> n;
    cout << n * 2 << endl;
    return 0;
}
```

### C
```c
#include <stdio.h>

int main() {
    int n;
    scanf("%d", &n);
    printf("%d\n", n * 2);
    return 0;
}
```

### JavaScript
```javascript
const readline = require('readline');

const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout
});

rl.on('line', (line) => {
    const n = parseInt(line);
    console.log(n * 2);
    rl.close();
});
```

## ⚠️ 注意事项

### 安全性
- 当前实现适用于**开发和测试环境**
- 生产环境建议使用 Docker 容器隔离
- 或使用专门的代码评测服务（如 Judge0）

### 性能
- 默认时间限制：5000 毫秒
- 默认内存限制：256 MB
- 最多同时执行 5 个代码任务

### 输出比较
- 自动去除行尾空格
- 自动去除末尾空行
- 大小写敏感
- 空格敏感（行内）

### 常见问题

**Q: 代码执行超时怎么办？**
A: 检查代码是否有死循环，或增加时间限制

**Q: 编译错误怎么办？**
A: 检查代码语法，确保类名正确（Java）

**Q: 找不到命令（javac/python/gcc等）？**
A: 确保已安装并添加到系统 PATH 环境变量

**Q: 输出不匹配但看起来一样？**
A: 检查是否有多余的空格或换行

## 🚀 高级功能

### 自定义评测逻辑
修改 `ExperimentServiceImpl.evaluateSubmission()` 方法

### 添加新语言支持
在 `LocalCodeExecutionServiceImpl` 中添加新的执行方法

### 集成 Docker
参考 `CODE_EXECUTION_SETUP.md` 中的 Docker 配置

## 📚 相关文件

- **代码执行服务**：`LocalCodeExecutionServiceImpl.java`
- **实验评测逻辑**：`ExperimentServiceImpl.java`
- **环境配置指南**：`CODE_EXECUTION_SETUP.md`
- **测试数据**：`experiment-test-data.sql`

## 🎓 教学建议

1. **从简单开始**：先让学生完成 Hello World
2. **逐步增加难度**：输入输出 → 条件判断 → 循环 → 数组 → 算法
3. **提供模板代码**：帮助学生快速上手
4. **设置合理的测试用例**：覆盖边界情况
5. **及时反馈**：查看学生提交情况，提供指导

## 💡 最佳实践

1. **测试用例设计**
   - 包含正常情况
   - 包含边界情况（0、负数、最大值等）
   - 包含特殊情况

2. **时间限制设置**
   - 简单题目：1-3 秒
   - 中等题目：3-5 秒
   - 复杂题目：5-10 秒

3. **内存限制设置**
   - 一般题目：128-256 MB
   - 大数据题目：512 MB - 1 GB

4. **代码规范**
   - 提供清晰的注释
   - 使用有意义的变量名
   - 保持代码整洁

祝你使用愉快！🎉
