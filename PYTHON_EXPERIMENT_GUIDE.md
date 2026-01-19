# Python 实验功能使用指南

## 📋 概述

本系统现在支持两种方式运行 Python 代码：

### 1. 原有实验系统（临时运行）
- **路径**: `/experiments/{id}/run`
- **特点**: 代码临时存储，适合在线测试和作业提交
- **用途**: 学生提交实验作业，系统自动评测

### 2. 新增 Python 文件管理系统（持久化存储）✨
- **路径**: `/python-experiments/*`
- **特点**: 文件持久化存储，支持文件管理
- **用途**: 课程实验、项目开发、代码管理

---

## 🚀 快速开始

### 步骤 1: 确保 Python 已安装

系统会自动尝试以下命令：
- `python`
- `python3`
- `py`

**检查方法**：
```bash
# 在命令行中运行
python --version
# 或
python3 --version
# 或
py --version
```

如果都不可用，请：
1. 下载 Python: https://www.python.org/downloads/
2. 安装时**务必勾选** "Add Python to PATH"
3. 重启 IDE 和后端服务

### 步骤 2: 查看示例文件

系统已经创建了两个示例文件：
- `hello.py` - Hello World 示例
- `calculator.py` - 简单计算器示例

---

## 📡 API 接口说明

### 1. 获取文件列表

**请求**:
```http
GET /python-experiments/list?path=&pageNum=1&pageSize=10
```

**参数**:
- `path`: 相对路径（可选，默认为根目录）
- `pageNum`: 页码（可选，默认 1）
- `pageSize`: 每页数量（可选，默认 10）

**响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 2,
    "list": [
      {
        "name": "hello.py",
        "isDirectory": false,
        "size": 123,
        "fullPath": "hello.py",
        "lastModified": 1705654321000
      }
    ],
    "currentPath": ""
  }
}
```

### 2. 获取文件内容

**请求**:
```http
GET /python-experiments/content?path=hello.py
```

**响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": "print('Hello, World!')\n..."
}
```

### 3. 保存文件

**请求**:
```http
POST /python-experiments/save
Content-Type: application/json

{
  "path": "test.py",
  "content": "print('Hello')"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "保存成功"
}
```

### 4. 运行 Python 脚本 ⭐

**请求**:
```http
POST /python-experiments/run
Content-Type: application/json

{
  "path": "hello.py",
  "input": "张三"
}
```

**参数**:
- `path`: Python 文件路径（必填）
- `input`: 标准输入内容（可选）

**响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "success": true,
    "output": "Hello, World!\n你好, 张三!",
    "error": null,
    "exitCode": 0
  }
}
```

**错误响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "success": false,
    "output": "",
    "error": "Python 未安装或未添加到 PATH...",
    "exitCode": null
  }
}
```

### 5. 删除文件

**请求**:
```http
DELETE /python-experiments/delete?path=test.py
```

**响应**:
```json
{
  "code": 200,
  "message": "删除成功"
}
```

---

## 🔧 技术实现细节

### 1. 文件存储位置

所有 Python 文件存储在：
```
项目根目录/python-experiments/
```

### 2. 安全机制

- ✅ 路径验证：防止路径遍历攻击
- ✅ 文件类型限制：只能操作 `.py` 文件
- ✅ 超时控制：程序运行超过 5 秒自动终止
- ✅ 进程隔离：每次运行都在独立进程中

### 3. Python 命令检测

系统启动时会按顺序检测：
1. `python` 命令
2. `python3` 命令
3. `py` 命令

找到第一个可用的命令后使用。

### 4. 代码执行流程

```
1. 接收运行请求
   ↓
2. 验证文件路径和权限
   ↓
3. 检测可用的 Python 命令
   ↓
4. 创建进程并执行
   ↓
5. 写入标准输入（如果有）
   ↓
6. 等待执行完成（最多 5 秒）
   ↓
7. 读取标准输出和错误输出
   ↓
8. 返回执行结果
```

---

## 🎯 使用场景

### 场景 1: 课程实验

教师可以创建实验目录结构：
```
python-experiments/
├── lesson1/
│   ├── exercise1.py
│   └── exercise2.py
└── lesson2/
    └── ...
```

学生通过 API 获取、编辑、运行这些文件。

### 场景 2: 在线编程练习

前端提供代码编辑器，学生编写代码后：
1. 调用 `/save` 保存代码
2. 调用 `/run` 运行代码
3. 查看运行结果

### 场景 3: 作业提交与评测

结合原有的实验系统：
1. 学生在 Python 文件管理系统中开发和测试
2. 完成后通过实验系统提交
3. 系统自动运行测试用例并评分

---

## ⚠️ 注意事项

### 1. Python 环境问题

**问题**: 提示 "Python 未安装"

**解决方案**:
- 确保 Python 已安装
- 确保 Python 已添加到系统 PATH
- 重启 IDE 和后端服务
- 在命令行测试 `python --version`

### 2. Windows Store Python 问题

如果你的 `python` 命令指向 Windows Store 占位符：
```
C:\Users\xxx\AppData\Local\Microsoft\WindowsApps\python.exe
```

**解决方案**:
1. 从 python.org 下载并安装 Python
2. 安装时勾选 "Add Python to PATH"
3. 或者手动将 Python 安装目录添加到 PATH 环境变量

### 3. 超时问题

程序运行超过 5 秒会被终止。如果需要更长时间：
- 优化代码逻辑
- 或联系管理员调整超时设置

### 4. 文件权限

- 只能操作 `python-experiments` 目录下的文件
- 不能访问系统其他目录
- 不能删除目录，只能删除文件

---

## 🔄 与原有实验系统的对比

| 特性 | 原有实验系统 | Python 文件管理系统 |
|------|------------|-------------------|
| 文件存储 | 临时（运行后清理） | 持久化 |
| 文件管理 | 不支持 | 支持浏览、编辑、删除 |
| 测试用例 | 支持自动评测 | 需手动测试 |
| 适用场景 | 作业提交、自动评测 | 课程实验、项目开发 |
| API 路径 | `/experiments/*` | `/python-experiments/*` |

---

## 📝 更新日志

### 2026-01-19
- ✅ 创建 `PythonExperimentController` 控制器
- ✅ 实现文件列表、读取、保存、运行、删除功能
- ✅ 改进 `LocalCodeExecutionServiceImpl` 的 Python 检测逻辑
- ✅ 创建 `python-experiments` 目录
- ✅ 添加示例文件和文档

---

## 📞 技术支持

如有问题，请检查：
1. Python 是否正确安装
2. 后端日志中的错误信息
3. API 响应中的错误提示

---

**祝使用愉快！** 🎉
