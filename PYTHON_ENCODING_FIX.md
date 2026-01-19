# Python 中文乱码问题修复

## 问题描述

Python 代码中的中文输出出现乱码，例如：
```
������������
```

## 问题原因

Python 在 Windows 上默认使用系统编码（GBK），而代码文件是 UTF-8 编码，导致中文字符无法正确显示。

## 解决方案

### 1. 设置 Python 环境变量

在运行 Python 时设置 `PYTHONIOENCODING=utf-8` 环境变量。

### 2. 使用 `-u` 参数

使用 `python -u` 参数强制 Python 使用无缓冲模式，确保输出立即显示。

## 修改内容

**文件**：`LocalCodeExecutionServiceImpl.java`

**修改前**：
```java
// 运行
ProcessBuilder pb = new ProcessBuilder(pythonCmd, sourceFile);
pb.directory(new File(workDir));
Process runProcess = pb.start();
```

**修改后**：
```java
// 运行（设置 UTF-8 环境）
ProcessBuilder pb = new ProcessBuilder(pythonCmd, "-u", sourceFile);
pb.directory(new File(workDir));
// 设置环境变量，确保 Python 使用 UTF-8
pb.environment().put("PYTHONIOENCODING", "utf-8");
Process runProcess = pb.start();
```

## 技术细节

### 1. PYTHONIOENCODING 环境变量

这个环境变量告诉 Python 使用 UTF-8 编码进行输入输出：
```java
pb.environment().put("PYTHONIOENCODING", "utf-8");
```

### 2. -u 参数（无缓冲模式）

`-u` 参数强制 Python 使用无缓冲模式：
- 标准输出和标准错误立即刷新
- 避免输出延迟
- 确保编码设置生效

```java
new ProcessBuilder(pythonCmd, "-u", sourceFile)
```

### 3. UTF-8 文件写入

确保源文件以 UTF-8 编码写入：
```java
Files.writeString(Paths.get(sourceFile), code, StandardCharsets.UTF_8);
```

## 测试验证

### 测试代码 1：简单输出

```python
print("你好，世界！")
print("Hello, World!")
```

**期望输出**：
```
你好，世界！
Hello, World!
```

### 测试代码 2：输入输出

```python
name = input("请输入你的名字：")
print(f"你好，{name}！")
```

**输入**：
```
张三
```

**期望输出**：
```
请输入你的名字：你好，张三！
```

### 测试代码 3：中文注释和字符串

```python
# 这是中文注释
name = "李四"  # 中文变量值
age = 20

print(f"姓名：{name}")
print(f"年龄：{age}")
print("欢迎使用Python！")
```

**期望输出**：
```
姓名：李四
年龄：20
欢迎使用Python！
```

## 与 Java 编码处理的对比

| 语言 | 编码设置方式 | 说明 |
|------|------------|------|
| Java | `-Dfile.encoding=UTF-8` | JVM 参数 |
| Python | `PYTHONIOENCODING=utf-8` | 环境变量 |
| C/C++ | 通常不需要 | 直接使用 UTF-8 |
| JavaScript | 不需要 | Node.js 默认 UTF-8 |

## 完整的编码处理流程

```
1. 源代码（UTF-8）
   ↓
2. 写入文件（UTF-8）
   Files.writeString(..., StandardCharsets.UTF_8)
   ↓
3. 设置环境变量
   PYTHONIOENCODING=utf-8
   ↓
4. 运行 Python
   python -u main.py
   ↓
5. 读取输出（UTF-8）
   InputStreamReader(..., StandardCharsets.UTF_8)
   ↓
6. 正确的中文输出
```

## 常见问题

### Q1: 为什么需要 -u 参数？

**A**: `-u` 参数确保：
- 输出立即刷新，不会缓冲
- 编码设置立即生效
- 避免输出顺序混乱

### Q2: 只设置环境变量够吗？

**A**: 通常够了，但加上 `-u` 参数更保险，可以避免缓冲问题。

### Q3: 会影响性能吗？

**A**: 
- 环境变量设置：无性能影响
- `-u` 参数：可能有轻微影响（因为禁用了缓冲），但对于短时间运行的程序可以忽略

### Q4: 其他语言需要类似处理吗？

**A**: 
- ✅ Java：需要（已处理）
- ✅ Python：需要（已处理）
- ❌ C/C++：通常不需要
- ❌ JavaScript：不需要（Node.js 默认 UTF-8）

## 环境变量说明

### PYTHONIOENCODING

这个环境变量控制 Python 的输入输出编码：

```bash
# Windows
set PYTHONIOENCODING=utf-8

# Linux/Mac
export PYTHONIOENCODING=utf-8

# Java ProcessBuilder
pb.environment().put("PYTHONIOENCODING", "utf-8");
```

### 其他相关环境变量

- `PYTHONLEGACYWINDOWSSTDIO`：Windows 控制台兼容性
- `PYTHONUTF8`：Python 3.7+ 的 UTF-8 模式

我们使用 `PYTHONIOENCODING` 因为它兼容所有 Python 版本。

## 总结

✅ 设置 `PYTHONIOENCODING=utf-8` 环境变量
✅ 使用 `python -u` 参数
✅ 文件以 UTF-8 编码写入
✅ 输出以 UTF-8 编码读取

**现在 Python 代码中的中文应该能正确显示了！**

## 测试建议

重启后端服务后，测试以下场景：

1. **纯中文输出**
```python
print("你好世界")
```

2. **中英文混合**
```python
print("Hello 世界")
```

3. **中文输入输出**
```python
name = input("姓名：")
print(f"你好，{name}！")
```

4. **中文注释**
```python
# 这是注释
print("测试")  # 行尾注释
```

所有这些场景现在都应该正常工作了！🎉
