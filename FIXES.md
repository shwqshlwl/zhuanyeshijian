# 实验功能修复说明

## 问题描述

1. **创建实验时显示"实验名称不能为空"** - 前后端字段名不匹配
2. **进入实验详情页显示"系统异常，请联系管理员"** - 缺少后端接口实现
3. **不能运行测试** - 缺少运行测试接口

## 修复内容

### 1. 前端字段名修改

#### 文件：`frontend/src/views/experiment/index.vue`
- ✅ 将 `title` 字段改为 `experimentName`
- ✅ 将 `deadline` 字段改为 `endTime`
- ✅ 更新表单验证规则
- ✅ 更新表格列显示
- ✅ 更新创建/编辑/删除功能

#### 文件：`frontend/src/views/experiment/detail.vue`
- ✅ 将 `title` 字段改为 `experimentName`
- ✅ 将 `deadline` 字段改为 `endTime`

### 2. 后端接口补充

#### 文件：`ExperimentService.java`
新增接口方法：
- `runCode()` - 运行测试代码
- `getMySubmissions()` - 获取我的提交历史
- `getSubmissions()` - 获取学生提交列表（教师）
- `getStudentSubmission()` - 获取学生提交详情（教师）

#### 文件：`ExperimentServiceImpl.java`
实现新增的接口方法：
- ✅ `runCode()` - 返回模拟运行结果（TODO: 需要集成真实的代码执行服务）
- ✅ `getMySubmissions()` - 查询当前学生的所有提交记录
- ✅ `getSubmissions()` - 分页查询所有学生的提交记录
- ✅ `getStudentSubmission()` - 查询指定学生的最新提交
- ✅ 修复空指针异常问题（添加 null 检查）

#### 文件：`ExperimentController.java`
新增接口端点：
- `POST /experiments/{id}/run` - 运行测试代码
- `GET /experiments/{id}/my-submissions` - 获取我的提交历史
- `GET /experiments/{id}/submissions` - 获取学生提交列表
- `GET /experiments/{experimentId}/submissions/{studentId}` - 获取学生提交详情

#### 文件：`ExperimentResultVO.java`
新增字段：
- `studentId` - 学生ID
- `studentName` - 学生姓名
- `studentNo` - 学号
- `code` - 代码内容

### 3. 测试数据

创建了 `test-data.sql` 文件，包含：
- 测试教师账号（username: teacher1, password: 123456）
- 测试学生账号（username: student1/student2, password: 123456）
- 测试课程数据
- 测试班级数据
- 测试实验数据

## 使用说明

### 1. 数据库初始化

如果数据库还没有初始化，请执行：

```sql
-- 1. 执行 create.sql 创建表结构
-- 2. 执行 test-data.sql 插入测试数据
```

### 2. 重启后端服务

修改了 Java 代码后，需要重启 Spring Boot 应用。

### 3. 刷新前端页面

修改了 Vue 代码后，前端会自动热重载。如果没有自动刷新，请手动刷新浏览器。

### 4. 测试功能

1. **创建实验**
   - 使用教师账号登录
   - 进入实验管理页面
   - 点击"创建实验"按钮
   - 填写实验信息（实验名称、课程、编程语言等）
   - 点击"确定"提交

2. **查看实验详情**
   - 点击实验列表中的"详情"按钮
   - 应该能正常显示实验信息

3. **运行测试**
   - 使用学生账号登录
   - 进入实验详情页
   - 在代码编辑器中输入代码
   - 点击"运行测试"按钮
   - 应该能看到运行结果（目前是模拟数据）

4. **提交代码**
   - 在代码编辑器中输入代码
   - 点击"提交代码"按钮
   - 应该能成功提交

## 待完成功能

### 代码执行服务

目前 `runCode()` 方法返回的是模拟数据。实际项目中需要集成真实的代码执行服务，例如：

1. **Judge0 API** - 开源的代码执行引擎
2. **Docker 容器** - 使用 Docker 隔离执行代码
3. **自建沙箱** - 使用 Java SecurityManager 或其他沙箱技术

示例集成 Judge0：

```java
@Override
public Map<String, Object> runCode(Long experimentId, String code, String language) {
    // 调用 Judge0 API
    String judge0Url = "https://judge0-ce.p.rapidapi.com/submissions";
    
    // 构建请求
    Map<String, Object> request = new HashMap<>();
    request.put("source_code", code);
    request.put("language_id", getLanguageId(language));
    
    // 发送请求并获取结果
    // ...
    
    return result;
}
```

## 注意事项

1. 确保数据库中有测试数据（课程、教师、学生等）
2. 确保用户已登录且有正确的权限
3. 代码执行功能目前返回模拟数据，需要后续集成真实的代码执行服务
4. 建议在生产环境中添加更多的错误处理和日志记录
