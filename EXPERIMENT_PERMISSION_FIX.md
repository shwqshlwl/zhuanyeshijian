# 实验权限控制修复说明

## 🔒 问题描述

**原问题**: 学生可以访问和提交任何课程的实验，即使该学生没有选修该课程。

**安全风险**: 
- 学生可以看到不属于自己课程的实验
- 学生可以提交不属于自己课程的实验代码
- 违反了基于课程的访问控制原则

---

## ✅ 解决方案

### 1. 权限控制逻辑

学生访问实验的权限链：
```
学生 → student_class表 → 班级 → 课程 → 实验
```

**规则**:
- 学生只能访问自己所选课程下的实验
- 教师和管理员不受限制，可以访问所有实验

### 2. 修改的文件

**文件**: `ExperimentServiceImpl.java`

**修改内容**:

#### 2.1 添加依赖注入
```java
private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;
private final com.example.citp.mapper.ClassMapper classMapper;
```

#### 2.2 新增辅助方法

**方法1: 获取学生所选课程ID列表**
```java
private java.util.List<Long> getStudentCourseIds(Long studentId) {
    // 1. 查询学生所在的班级
    java.util.List<Long> classIds = jdbcTemplate.queryForList(
            "SELECT class_id FROM student_class WHERE student_id = ?",
            Long.class, studentId);

    if (classIds.isEmpty()) {
        return new java.util.ArrayList<>();
    }

    // 2. 查询班级关联的课程
    java.util.List<ClassEntity> classes = classMapper.selectBatchIds(classIds);
    return classes.stream()
            .filter(c -> c.getCourseId() != null)
            .map(ClassEntity::getCourseId)
            .distinct()
            .collect(java.util.stream.Collectors.toList());
}
```

**方法2: 检查学生课程访问权限**
```java
private void checkStudentCourseAccess(Long studentId, Long courseId) {
    java.util.List<Long> studentCourseIds = getStudentCourseIds(studentId);
    if (!studentCourseIds.contains(courseId)) {
        throw new BusinessException("您没有权限访问该课程的实验");
    }
}
```

#### 2.3 修改的接口方法

**1. getExperimentList() - 实验列表查询**
```java
// 学生只能查看自己所选课程的实验
if (currentUser.getUserType() == 1) { // 学生
    java.util.List<Long> studentCourseIds = getStudentCourseIds(currentUser.getId());
    if (studentCourseIds.isEmpty()) {
        // 学生没有选课，返回空列表
        Page<ExperimentVO> emptyPage = new Page<>(pageNum, pageSize, 0);
        emptyPage.setRecords(new java.util.ArrayList<>());
        return emptyPage;
    }
    wrapper.in(Experiment::getCourseId, studentCourseIds);
}
```

**2. getExperimentById() - 实验详情查询**
```java
// 学生权限检查：只能查看自己所选课程的实验
SysUser currentUser = getCurrentUser();
if (currentUser.getUserType() == 1) { // 学生
    checkStudentCourseAccess(currentUser.getId(), experiment.getCourseId());
}
```

**3. submitExperiment() - 提交实验代码**
```java
// 学生权限检查：只能提交自己所选课程的实验
checkStudentCourseAccess(currentUser.getId(), experiment.getCourseId());
```

**4. runCode() - 运行测试代码**
```java
// 学生权限检查：只能运行自己所选课程的实验代码
SysUser currentUser = getCurrentUser();
if (currentUser.getUserType() == 1) { // 学生
    checkStudentCourseAccess(currentUser.getId(), experiment.getCourseId());
}
```

**5. getExperimentResult() - 获取评测结果**
```java
// 学生权限检查：只能查看自己所选课程的实验结果
if (currentUser.getUserType() == 1) { // 学生
    checkStudentCourseAccess(currentUser.getId(), experiment.getCourseId());
}
```

**6. getMySubmissions() - 获取提交历史**
```java
// 学生权限检查：只能查看自己所选课程的实验提交历史
if (currentUser.getUserType() == 1) { // 学生
    checkStudentCourseAccess(currentUser.getId(), experiment.getCourseId());
}
```

---

## 🎯 权限控制覆盖的接口

| 接口 | 路径 | 权限控制 |
|------|------|----------|
| 实验列表 | GET /experiments | ✅ 学生只能看到自己课程的实验 |
| 实验详情 | GET /experiments/:id | ✅ 学生只能查看自己课程的实验 |
| 运行代码 | POST /experiments/:id/run | ✅ 学生只能运行自己课程的实验 |
| 提交代码 | POST /experiments/:id/submit | ✅ 学生只能提交自己课程的实验 |
| 评测结果 | GET /experiments/:id/result | ✅ 学生只能查看自己课程的实验结果 |
| 提交历史 | GET /experiments/:id/my-submissions | ✅ 学生只能查看自己课程的实验历史 |

---

## 🧪 测试场景

### 场景1: 学生查看实验列表
**前提**: 
- 学生A选修了课程1和课程2
- 课程1有实验A、实验B
- 课程2有实验C
- 课程3有实验D（学生A未选修）

**预期结果**:
- ✅ 学生A可以看到实验A、B、C
- ❌ 学生A看不到实验D

### 场景2: 学生访问实验详情
**前提**:
- 学生A选修了课程1
- 实验X属于课程1
- 实验Y属于课程2（学生A未选修）

**预期结果**:
- ✅ 学生A可以访问实验X的详情
- ❌ 学生A访问实验Y时返回错误："您没有权限访问该课程的实验"

### 场景3: 学生提交代码
**前提**:
- 学生A选修了课程1
- 实验X属于课程1
- 实验Y属于课程2（学生A未选修）

**预期结果**:
- ✅ 学生A可以提交实验X的代码
- ❌ 学生A提交实验Y的代码时返回错误："您没有权限访问该课程的实验"

### 场景4: 学生没有选课
**前提**:
- 学生B没有选修任何课程

**预期结果**:
- ✅ 实验列表为空
- ❌ 无法访问任何实验

### 场景5: 教师访问
**前提**:
- 用户C是教师

**预期结果**:
- ✅ 可以查看所有实验
- ✅ 可以创建、编辑、删除实验
- ✅ 可以查看所有学生的提交

---

## 🔍 测试步骤

### 1. 准备测试数据
```sql
-- 创建测试课程
INSERT INTO course (course_name, course_code, teacher_id) VALUES 
('Java程序设计', 'CS101', 1),
('Python基础', 'CS102', 1);

-- 创建测试班级
INSERT INTO class (class_name, course_id, teacher_id) VALUES 
('Java班级1', 1, 1),
('Python班级1', 2, 1);

-- 学生选课（学生ID=2选修Java课程）
INSERT INTO student_class (student_id, class_id) VALUES (2, 1);

-- 创建实验
INSERT INTO experiment (experiment_name, course_id, teacher_id, language) VALUES 
('Java实验1', 1, 1, 'java'),
('Python实验1', 2, 1, 'python');
```

### 2. 测试学生访问
```bash
# 学生登录
POST /login
{
  "username": "student1",
  "password": "123456"
}

# 查看实验列表（应该只看到Java实验1）
GET /experiments

# 访问Java实验1详情（应该成功）
GET /experiments/1

# 访问Python实验1详情（应该失败）
GET /experiments/2
# 预期返回: {"code": 500, "message": "您没有权限访问该课程的实验"}
```

### 3. 测试教师访问
```bash
# 教师登录
POST /login
{
  "username": "teacher1",
  "password": "123456"
}

# 查看实验列表（应该看到所有实验）
GET /experiments

# 访问任意实验详情（应该都成功）
GET /experiments/1
GET /experiments/2
```

---

## 📊 影响范围

### 前端影响
- ✅ 无需修改前端代码
- ✅ 学生自动只能看到自己课程的实验
- ✅ 如果学生尝试访问无权限的实验，会收到错误提示

### 后端影响
- ✅ 增强了数据安全性
- ✅ 符合基于角色的访问控制（RBAC）原则
- ✅ 防止了越权访问

### 性能影响
- ⚠️ 每次查询需要额外查询学生的课程列表
- ✅ 使用了批量查询，性能影响较小
- ✅ 可以考虑添加缓存优化

---

## 🚀 部署说明

### 1. 代码更新
```bash
# 拉取最新代码
git pull

# 重新编译
cd sj
mvn clean package

# 重启服务
java -jar target/citp-0.0.1-SNAPSHOT.jar
```

### 2. 验证部署
- 使用学生账号登录
- 查看实验列表
- 尝试访问不同课程的实验
- 确认权限控制生效

---

## 🔧 后续优化建议

### 1. 性能优化
```java
// 添加缓存，避免重复查询
@Cacheable(value = "studentCourses", key = "#studentId")
private List<Long> getStudentCourseIds(Long studentId) {
    // ...
}
```

### 2. 日志记录
```java
// 记录权限检查失败的日志
private void checkStudentCourseAccess(Long studentId, Long courseId) {
    List<Long> studentCourseIds = getStudentCourseIds(studentId);
    if (!studentCourseIds.contains(courseId)) {
        log.warn("学生{}尝试访问无权限的课程{}实验", studentId, courseId);
        throw new BusinessException("您没有权限访问该课程的实验");
    }
}
```

### 3. 更细粒度的权限控制
- 考虑实验的发布状态（草稿/已发布）
- 考虑实验的时间限制（开始时间/截止时间）
- 考虑学生的退课情况

---

## ✅ 总结

本次修改成功实现了实验模块的课程级权限控制：

1. **安全性提升** - 学生只能访问自己所选课程的实验
2. **代码质量** - 统一的权限检查方法，易于维护
3. **用户体验** - 清晰的错误提示，帮助用户理解权限限制
4. **向后兼容** - 不影响教师和管理员的使用

**修改日期**: 2026年1月18日  
**修改人**: AI Assistant  
**版本**: v1.1
