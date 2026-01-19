# Python 实验目录

这个目录用于存放 Python 实验文件。

## 使用说明

### 1. 通过 API 管理 Python 文件

系统提供了以下 API 接口来管理和运行 Python 文件：

#### 获取文件列表
```
GET /python-experiments/list?path=&pageNum=1&pageSize=10
```

#### 获取文件内容
```
GET /python-experiments/content?path=hello.py
```

#### 保存文件
```
POST /python-experiments/save
Body: {
  "path": "test.py",
  "content": "print('Hello')"
}
```

#### 运行 Python 脚本
```
POST /python-experiments/run
Body: {
  "path": "hello.py",
  "input": "张三"  // 可选，作为标准输入
}
```

#### 删除文件
```
DELETE /python-experiments/delete?path=test.py
```

### 2. Python 环境要求

系统会自动尝试以下 Python 命令：
- `python`
- `python3`
- `py`

请确保至少有一个命令可用。

### 3. 示例文件

- `hello.py` - Hello World 示例，演示输入输出
- `calculator.py` - 简单计算器示例

### 4. 注意事项

- 所有 Python 文件必须以 `.py` 结尾
- 程序运行超时时间为 5 秒
- 支持标准输入输出
- 文件路径会自动验证，防止路径遍历攻击

## 与实验系统集成

原有的实验系统（`/experiments`）仍然可以运行 Python 代码，但是：
- 代码是临时存储的，运行后会被清理
- 适合用于在线编程测试和作业提交

这个新的 Python 实验目录（`/python-experiments`）的优势：
- 文件持久化存储
- 可以管理多个 Python 文件
- 适合用于课程实验和项目开发
- 支持文件浏览和管理

## 目录结构建议

```
python-experiments/
├── README.md           # 本文件
├── hello.py           # 示例文件
├── calculator.py      # 示例文件
├── lesson1/           # 第一课实验
│   ├── exercise1.py
│   └── exercise2.py
├── lesson2/           # 第二课实验
│   └── ...
└── projects/          # 学生项目
    └── ...
```
