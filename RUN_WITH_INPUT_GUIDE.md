# 实验运行测试功能 - 支持键盘输入

## ✅ 已修复

现在 `/experiments/{id}/run` 接口已经支持键盘输入了！

## 📡 API 使用方法

### 运行代码（带输入）

**请求**:
```http
POST /experiments/{id}/run
Content-Type: application/json

{
  "code": "a = int(input())\nb = int(input())\nprint(a + b)",
  "language": "python",
  "input": "5\n3"
}
```

**参数说明**:
- `code`: 代码内容（必填）
- `language`: 编程语言（必填）
  - 支持：`python`, `java`, `cpp`, `c`, `javascript`
- `input`: 标准输入内容（可选）
  - 多行输入使用 `\n` 分隔
  - 如果不需要输入，可以省略此参数或传空字符串

**响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "success": true,
    "output": "8",
    "error": null,
    "exitCode": 0
  }
}
```

## 🎯 使用示例

### 示例 1: Python - 两数相加

**代码**:
```python
a = int(input())
b = int(input())
print(a + b)
```

**请求**:
```json
{
  "code": "a = int(input())\nb = int(input())\nprint(a + b)",
  "language": "python",
  "input": "5\n3"
}
```

**输出**:
```
8
```

### 示例 2: Python - 带提示的输入

**代码**:
```python
name = input("请输入你的名字: ")
age = input("请输入你的年龄: ")
print(f"你好, {name}! 你今年 {age} 岁。")
```

**请求**:
```json
{
  "code": "name = input('请输入你的名字: ')\nage = input('请输入你的年龄: ')\nprint(f'你好, {name}! 你今年 {age} 岁。')",
  "language": "python",
  "input": "张三\n20"
}
```

**输出**:
```
请输入你的名字: 请输入你的年龄: 你好, 张三! 你今年 20 岁。
```

### 示例 3: Python - 循环输入

**代码**:
```python
n = int(input())
total = 0
for i in range(n):
    num = int(input())
    total += num
print(total)
```

**请求**:
```json
{
  "code": "n = int(input())\ntotal = 0\nfor i in range(n):\n    num = int(input())\n    total += num\nprint(total)",
  "language": "python",
  "input": "3\n10\n20\n30"
}
```

**输出**:
```
60
```

### 示例 4: Java - 两数相加

**代码**:
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int a = sc.nextInt();
        int b = sc.nextInt();
        System.out.println(a + b);
    }
}
```

**请求**:
```json
{
  "code": "import java.util.Scanner;\n\npublic class Main {\n    public static void main(String[] args) {\n        Scanner sc = new Scanner(System.in);\n        int a = sc.nextInt();\n        int b = sc.nextInt();\n        System.out.println(a + b);\n    }\n}",
  "language": "java",
  "input": "5 3"
}
```

**输出**:
```
8
```

### 示例 5: C++ - 两数相加

**代码**:
```cpp
#include <iostream>
using namespace std;

int main() {
    int a, b;
    cin >> a >> b;
    cout << a + b << endl;
    return 0;
}
```

**请求**:
```json
{
  "code": "#include <iostream>\nusing namespace std;\n\nint main() {\n    int a, b;\n    cin >> a >> b;\n    cout << a + b << endl;\n    return 0;\n}",
  "language": "cpp",
  "input": "5 3"
}
```

**输出**:
```
8
```

### 示例 6: 无输入的程序

**代码**:
```python
print("Hello, World!")
```

**请求**:
```json
{
  "code": "print('Hello, World!')",
  "language": "python"
}
```

或者：
```json
{
  "code": "print('Hello, World!')",
  "language": "python",
  "input": ""
}
```

**输出**:
```
Hello, World!
```

## 🔧 输入格式说明

### 单行输入
```json
{
  "input": "5"
}
```

### 多行输入
使用 `\n` 分隔每一行：
```json
{
  "input": "5\n3\n8"
}
```

### 空格分隔的输入
```json
{
  "input": "5 3 8"
}
```

### 混合输入
```json
{
  "input": "3\n10 20 30\nHello"
}
```

## ⚠️ 注意事项

### 1. 输入格式要匹配代码

**错误示例**：
```python
# 代码期望两行输入
a = int(input())
b = int(input())
```

```json
// 但只提供了一行
{
  "input": "5 3"  // ❌ 错误
}
```

**正确示例**：
```json
{
  "input": "5\n3"  // ✅ 正确
}
```

### 2. 超时限制

- 默认超时：5 秒
- 可在实验设置中调整
- 超时会返回错误信息

### 3. 输入提示不会显示

当使用 `input("提示信息")` 时，提示信息会出现在输出中，但不会等待用户输入。

**示例**：
```python
name = input("请输入名字: ")
print(f"你好, {name}")
```

**输出**：
```
请输入名字: 你好, 张三
```

### 4. 输入结束符

- Python: 使用 `input()` 读取一行
- Java: 使用 `Scanner.nextLine()` 或 `Scanner.nextInt()`
- C++: 使用 `cin` 或 `getline()`

## 🎓 前端集成示例

### Vue 3 示例

```vue
<template>
  <div class="code-runner">
    <el-form>
      <el-form-item label="代码">
        <el-input
          v-model="code"
          type="textarea"
          :rows="10"
          placeholder="请输入代码"
        />
      </el-form-item>
      
      <el-form-item label="输入">
        <el-input
          v-model="input"
          type="textarea"
          :rows="5"
          placeholder="请输入测试数据（多行用回车分隔）"
        />
      </el-form-item>
      
      <el-form-item>
        <el-button type="primary" @click="runCode">运行</el-button>
      </el-form-item>
      
      <el-form-item label="输出">
        <el-input
          v-model="output"
          type="textarea"
          :rows="10"
          readonly
          placeholder="运行结果将显示在这里"
        />
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { runExperimentCode } from '@/api/experiment'
import { ElMessage } from 'element-plus'

const code = ref('')
const input = ref('')
const output = ref('')

const runCode = async () => {
  try {
    const res = await runExperimentCode(experimentId, {
      code: code.value,
      language: 'python',
      input: input.value
    })
    
    if (res.success) {
      output.value = res.output
      ElMessage.success('运行成功')
    } else {
      output.value = res.error || '运行失败'
      ElMessage.error('运行失败')
    }
  } catch (error) {
    ElMessage.error('运行异常: ' + error.message)
  }
}
</script>
```

### API 封装

```javascript
// api/experiment.js
import request from '@/utils/request'

export function runExperimentCode(experimentId, data) {
  return request({
    url: `/experiments/${experimentId}/run`,
    method: 'post',
    data
  })
}
```

## 📊 与提交评测的区别

| 特性 | 运行测试 (`/run`) | 提交评测 (`/submit`) |
|------|------------------|---------------------|
| 输入方式 | 手动提供 | 使用测试用例 |
| 保存记录 | 不保存 | 保存到数据库 |
| 评分 | 无 | 自动评分 |
| 测试用例 | 单次测试 | 多个测试用例 |
| 用途 | 调试和测试 | 正式提交 |

## 🔄 推荐工作流程

1. **编写代码** - 在编辑器中编写
2. **运行测试** - 使用 `/run` 接口，手动输入测试数据
3. **调试修改** - 根据输出结果调整代码
4. **重复测试** - 多次运行，验证各种情况
5. **正式提交** - 使用 `/submit` 接口提交评测

## ✅ 总结

- ✅ 支持键盘输入
- ✅ 支持多行输入
- ✅ 支持所有编程语言
- ✅ 实时返回结果
- ✅ 不保存记录，可反复测试

现在你可以在运行测试时提供输入数据了！🎉
