# 自动评测问题调试指南

## 问题描述

提交代码后，评测状态一直显示"评测中"，无法完成评测。

## 已添加的改进

### 1. 增强的日志记录

在评测流程的关键节点添加了详细的日志：

- ✅ 异步任务开始和结束
- ✅ 测试用例解析
- ✅ 每个测试用例的执行结果
- ✅ 输出比较结果
- ✅ 最终评测结果
- ✅ 异常捕获和记录

### 2. 改进的异常处理

```java
CompletableFuture.runAsync(() -> {
    try {
        log.info("评测任务开始执行，提交ID: {}", submitId);
        evaluateSubmission(submitId, experiment, request.getCode(), request.getLanguage());
        log.info("评测任务执行完成，提交ID: {}", submitId);
    } catch (Exception e) {
        log.error("代码评测失败，提交ID: " + submitId, e);
        // 更新提交状态为运行错误
        try {
            ExperimentSubmit failedSubmit = experimentSubmitMapper.selectById(submitId);
            if (failedSubmit != null) {
                failedSubmit.setStatus(5);
                failedSubmit.setErrorMessage("评测失败: " + e.getMessage());
                experimentSubmitMapper.updateById(failedSubmit);
            }
        } catch (Exception updateEx) {
            log.error("更新提交状态失败，提交ID: " + submitId, updateEx);
        }
    }
}).exceptionally(ex -> {
    log.error("异步任务执行异常，提交ID: " + submitId, ex);
    return null;
});
```

## 调试步骤

### 步骤 1: 重启后端服务

修改了代码后，需要重启后端服务使更改生效。

### 步骤 2: 提交测试代码

提交一个简单的测试代码，例如：

**Python**:
```python
a = int(input())
b = int(input())
print(a + b)
```

**测试用例**:
```json
[
  {"input": "5\n3", "output": "8"}
]
```

### 步骤 3: 查看后端日志

提交后，立即查看后端控制台日志，应该能看到类似这样的输出：

```
INFO  - 开始异步评测，提交ID: 1, 实验ID: 1, 学生ID: 2
INFO  - 评测任务开始执行，提交ID: 1
INFO  - 开始评测代码，提交ID: 1, 语言: python
INFO  - 测试用例JSON: [{"input":"5\n3","output":"8"}]
INFO  - 成功解析测试用例，数量: 1, 提交ID: 1
INFO  - 执行测试用例 1/1, 提交ID: 1
INFO  - 找到可用的 Python 命令: python
INFO  - 测试用例 1 执行结果: success=true, output=8, error=null
INFO  - 测试用例 1 比较结果: passed=true, 期望输出=8, 实际输出=8
INFO  - 评测完成，提交ID: 1, 通过: 1/1, 得分: 100
INFO  - 已更新提交记录，提交ID: 1, 状态: 2
INFO  - 评测任务执行完成，提交ID: 1
```

### 步骤 4: 分析日志

根据日志输出，可以判断问题出在哪里：

#### 情况 1: 没有任何日志
**问题**: 异步任务没有启动
**可能原因**:
- 提交接口本身出错
- 数据库插入失败
- 事务回滚

**解决方案**: 检查提交接口的响应和数据库记录

#### 情况 2: 有开始日志，但没有后续日志
**问题**: 评测任务启动了但卡住了
**可能原因**:
- 测试用例解析失败
- 代码执行服务异常
- 数据库连接问题

**解决方案**: 查看完整的错误堆栈

#### 情况 3: 有执行日志，但没有更新日志
**问题**: 评测完成了但数据库更新失败
**可能原因**:
- 数据库事务问题
- 字段类型不匹配
- 权限问题

**解决方案**: 检查数据库连接和表结构

#### 情况 4: Python 命令找不到
**日志**: `Python 未安装或未添加到 PATH`
**解决方案**: 
- 确保 Python 已安装
- 确保 `python`、`python3` 或 `py` 命令可用
- 重启后端服务

## 常见问题排查

### 问题 1: 测试用例格式错误

**日志**:
```
ERROR - 解析测试用例失败，提交ID: 1
```

**原因**: 测试用例不是有效的 JSON 格式

**解决方案**: 确保测试用例格式正确
```json
[
  {"input": "5\n3", "output": "8"},
  {"input": "10\n20", "output": "30"}
]
```

### 问题 2: 代码执行超时

**日志**:
```
INFO  - 测试用例 1 执行结果: success=false, error=执行超时...
```

**原因**: 代码运行时间超过限制

**解决方案**: 
- 检查代码是否有死循环
- 增加时间限制
- 优化代码逻辑

### 问题 3: 输出不匹配

**日志**:
```
INFO  - 测试用例 1 比较结果: passed=false, 期望输出=8, 实际输出=8 
```

**原因**: 输出有多余的空格或换行

**解决方案**: 
- 检查代码输出格式
- 系统会自动去除行尾空格和末尾空行

### 问题 4: 数据库更新失败

**日志**:
```
ERROR - 更新提交状态失败，提交ID: 1
```

**原因**: 数据库连接或权限问题

**解决方案**:
- 检查数据库连接
- 检查表结构
- 检查字段类型

## 手动测试评测功能

### 1. 直接调用评测方法

创建一个测试接口：

```java
@GetMapping("/test-evaluate/{submitId}")
public Result<Void> testEvaluate(@PathVariable Long submitId) {
    ExperimentSubmit submit = experimentSubmitMapper.selectById(submitId);
    Experiment experiment = experimentMapper.selectById(submit.getExperimentId());
    
    evaluateSubmission(submitId, experiment, submit.getCode(), submit.getLanguage());
    
    return Result.successMsg("评测完成");
}
```

### 2. 检查数据库状态

提交后，直接查询数据库：

```sql
SELECT id, status, score, pass_count, total_count, error_message, submit_time
FROM experiment_submit
WHERE id = ?
ORDER BY submit_time DESC;
```

状态码：
- `1` - 评测中
- `2` - 通过
- `3` - 未通过
- `4` - 编译错误
- `5` - 运行错误

### 3. 模拟异步任务

在本地测试时，可以临时改为同步执行：

```java
// 临时改为同步执行（仅用于调试）
try {
    evaluateSubmission(submitId, experiment, request.getCode(), request.getLanguage());
} catch (Exception e) {
    log.error("代码评测失败", e);
}
```

## 性能优化建议

### 1. 使用自定义线程池

```java
private final ExecutorService evaluationExecutor = Executors.newFixedThreadPool(5);

// 使用自定义线程池
CompletableFuture.runAsync(() -> {
    // ...
}, evaluationExecutor);
```

### 2. 添加超时控制

```java
CompletableFuture.runAsync(() -> {
    // ...
}).orTimeout(30, TimeUnit.SECONDS)
  .exceptionally(ex -> {
      log.error("评测超时", ex);
      return null;
  });
```

### 3. 批量更新

如果有多个测试用例，可以考虑批量更新结果。

## 前端轮询优化

如果评测时间较长，前端可以添加轮询机制：

```javascript
const pollResult = async () => {
  const maxAttempts = 30 // 最多轮询30次
  let attempts = 0
  
  const poll = async () => {
    if (attempts >= maxAttempts) {
      ElMessage.error('评测超时，请刷新页面查看结果')
      return
    }
    
    const res = await getExperimentResult(experimentId)
    
    if (res.data.status === 1) {
      // 还在评测中，继续轮询
      attempts++
      setTimeout(poll, 2000) // 2秒后再次查询
    } else {
      // 评测完成
      showResult(res.data)
    }
  }
  
  poll()
}
```

## 总结

✅ 已添加详细的日志记录
✅ 已改进异常处理机制
✅ 已添加测试用例解析错误处理
✅ 已添加数据库更新失败处理

**下一步**：
1. 重启后端服务
2. 提交测试代码
3. 查看后端日志
4. 根据日志定位问题

如果问题仍然存在，请提供后端日志的完整输出，我会帮你进一步分析。
