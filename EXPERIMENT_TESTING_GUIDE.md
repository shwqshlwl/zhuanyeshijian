# 测试实验评测功能

## 测试步骤

### 1. 创建一个简单的 Python 实验

**实验要求**：编写一个程序，读取两个整数并输出它们的和

**测试用例**：
```json
[
  {"input": "5\n3", "output": "8"},
  {"input": "10\n20", "output": "30"},
  {"input": "0\n0", "output": "0"}
]
```

**正确答案**：
```python
a = int(input())
b = int(input())
print(a + b)
```

### 2. 测试提交

#### 测试 1：正确答案
```http
POST /experiments/{id}/submit
Content-Type: application/json

{
  "code": "a = int(input())\nb = int(input())\nprint(a + b)",
  "language": "python"
}
```

**期望结果**：
- 状态：通过
- 通过测试：3/3
- 得分：满分

#### 测试 2：错误答案
```http
POST /experiments/{id}/submit
Content-Type: application/json

{
  "code": "a = int(input())\nb = int(input())\nprint(a - b)",
  "language": "python"
}
```

**期望结果**：
- 状态：未通过
- 通过测试：0/3
- 得分：0

#### 测试 3：部分正确
```http
POST /experiments/{id}/submit
Content-Type: application/json

{
  "code": "a = int(input())\nb = int(input())\nif a == 5:\n    print(8)\nelse:\n    print(a + b)",
  "language": "python"
}
```

**期望结果**：
- 状态：未通过
- 通过测试：2/3
- 得分：部分分数

### 3. 查看结果

```http
GET /experiments/{id}/result
```

**响应示例**：
```json
{
  "code": 200,
  "data": {
    "submitId": 1,
    "status": 2,
    "score": 100,
    "passCount": 3,
    "totalCount": 3,
    "executeTime": 150,
    "memoryUsed": 10,
    "errorMessage": null,
    "resultDetail": "[{\"testCase\":1,\"passed\":true,\"input\":\"5\\n3\",\"expected\":\"8\",\"actual\":\"8\",\"error\":null}...]",
    "submitTime": "2026-01-19T12:00:00"
  }
}
```

## 状态码说明

- `1` - 评测中
- `2` - 通过
- `3` - 未通过（部分测试用例失败）
- `4` - 编译错误
- `5` - 运行错误

## 常见问题

### Q1: 提交后一直显示"评测中"
**原因**：
- Python 环境问题
- 代码执行超时
- 测试用例格式错误

**解决**：
- 检查后端日志
- 查看错误信息
- 确认 Python 可用

### Q2: 所有测试用例都失败
**原因**：
- 输出格式不匹配
- 多余的空格或换行
- 输入读取方式错误

**解决**：
- 使用 `/experiments/{id}/run` 先测试
- 检查输出格式
- 确认输入读取正确

### Q3: Python 代码无法运行
**原因**：
- Python 未安装
- PATH 配置错误
- 使用了不支持的 Python 版本

**解决**：
- 确保 `python`、`python3` 或 `py` 命令可用
- 重启后端服务
- 查看错误提示

## 与 Python 文件管理系统的区别

| 特性 | 实验评测系统 | Python 文件管理 |
|------|------------|----------------|
| 用途 | 作业提交和自动评测 | 代码开发和测试 |
| 文件存储 | 临时（数据库） | 持久化（文件系统） |
| 测试用例 | 自动运行 | 手动测试 |
| 评分 | 自动评分 | 无评分 |
| 适用场景 | 课程作业、考试 | 实验练习、项目开发 |

## 建议的使用流程

1. **开发阶段**：使用 Python 文件管理系统
   - 创建和编辑代码
   - 反复测试和调试
   - 保存多个版本

2. **测试阶段**：使用实验系统的运行功能
   - 在线运行代码
   - 查看输出结果
   - 验证逻辑正确性

3. **提交阶段**：使用实验系统的提交功能
   - 提交最终代码
   - 自动评测
   - 获得成绩

## 总结

✅ 实验评测功能**完全可用**
✅ Python 执行已优化，更可靠
✅ 支持自动评测和评分
✅ 提供详细的错误信息

如有问题，请查看后端日志或联系技术支持。
