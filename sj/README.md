# 课程智慧教学平台 (Course Intelligent Teaching Platform)

## 项目概述

课程智慧教学平台是一个基于 Spring Boot 3.2 的后端项目，提供完整的教学管理功能，包括用户管理、课程管理、班级管理、作业管理、题库管理、考试管理和实验管理等模块。

## 技术栈

- **Java 17+**
- **Spring Boot 3.2.5**
- **Spring Security** - JWT 认证
- **MyBatis-Plus 3.5.5** - ORM 框架
- **MySQL 8.0** - 数据库
- **SpringDoc OpenAPI 2.3.0** - API 文档 (Swagger 3)
- **Hutool** - 工具类库
- **Lombok** - 简化代码

## 项目结构

```
com.example.citp
├── CitpApplication.java          # 启动类
├── common/                       # 通用类
│   └── Result.java              # 统一返回结果
├── config/                       # 配置类
│   ├── CorsConfig.java          # 跨域配置
│   ├── MyBatisPlusConfig.java   # MyBatis-Plus 配置
│   ├── SecurityConfig.java      # Spring Security 配置
│   └── SwaggerConfig.java       # Swagger 配置
├── controller/                   # 控制层
│   ├── SysUserController.java   # 用户管理
│   ├── CourseController.java    # 课程管理
│   ├── ClassController.java     # 班级管理
│   ├── HomeworkController.java  # 作业管理
│   ├── QuestionController.java  # 题库管理
│   ├── ExamController.java      # 考试管理
│   └── ExperimentController.java # 实验管理
├── service/                      # 服务层
│   ├── impl/                    # 服务实现
│   └── ...Service.java          # 服务接口
├── mapper/                       # 数据访问层
├── model/                        # 数据模型
│   ├── entity/                  # 数据库实体
│   ├── dto/                     # 数据传输对象
│   └── vo/                      # 视图对象
├── exception/                    # 异常处理
│   ├── BusinessException.java   # 业务异常
│   └── GlobalExceptionHandler.java # 全局异常处理
├── security/                     # 安全相关
│   ├── JwtAuthenticationFilter.java
│   └── JwtAuthenticationEntryPoint.java
└── util/                         # 工具类
    └── JwtUtil.java             # JWT 工具类
```

## 快速开始

### 1. 环境准备

- JDK 17+
- Maven 3.6+
- MySQL 8.0+

### 2. 数据库初始化

1. 创建数据库：
```sql
CREATE DATABASE IF NOT EXISTS course_teaching_platform DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 执行 SQL 脚本：
```bash
mysql -u root -p course_teaching_platform < src/main/resources/schema.sql
```

### 3. 修改配置

编辑 `src/main/resources/application.yml`，修改数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/course_teaching_platform?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: your_username
    password: your_password
```

### 4. 启动项目

```bash
# 使用 Maven 启动
mvn spring-boot:run

# 或者打包后启动
mvn clean package
java -jar target/course-intelligent-teaching-platform-1.0.0.jar
```

### 5. 访问接口文档

启动成功后，访问 Swagger UI：
- http://localhost:8080/api/swagger-ui.html

## API 接口说明

### 认证接口 (无需登录)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/auth/login | 用户登录 |
| POST | /api/auth/register/teacher | 教师注册 |

### 用户管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/user/info | 获取当前用户信息 |
| PUT | /api/user/password | 修改密码 |
| POST | /api/user/batch-import-students | 批量导入学生 |

### 课程管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/courses | 分页查询课程列表 |
| POST | /api/courses | 创建课程 |
| GET | /api/courses/{id} | 获取课程详情 |
| PUT | /api/courses/{id} | 更新课程 |
| DELETE | /api/courses/{id} | 删除课程 |

### 班级管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/classes | 分页查询班级列表 |
| POST | /api/classes | 创建班级 |
| GET | /api/classes/{id} | 获取班级详情 |
| PUT | /api/classes/{id} | 更新班级 |
| DELETE | /api/classes/{id} | 删除班级 |
| POST | /api/classes/{classId}/students/{studentId} | 添加学生到班级 |
| DELETE | /api/classes/{classId}/students/{studentId} | 从班级移除学生 |

### 作业管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/homeworks | 分页查询作业列表 |
| POST | /api/homeworks | 创建作业 |
| GET | /api/homeworks/{id} | 获取作业详情 |
| PUT | /api/homeworks/{id} | 更新作业 |
| DELETE | /api/homeworks/{id} | 删除作业 |
| POST | /api/homeworks/{id}/submit | 提交作业 |
| PUT | /api/homeworks/{id}/grade | 批改作业 |
| POST | /api/homeworks/{id}/ai-analyze | AI 解析题目 |

### 题库管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/questions/types | 获取所有题型 |
| GET | /api/questions | 分页查询题目列表 |
| POST | /api/questions | 创建题目 |
| GET | /api/questions/{id} | 获取题目详情 |
| PUT | /api/questions/{id} | 更新题目 |
| DELETE | /api/questions/{id} | 删除题目 |
| POST | /api/questions/batch-delete | 批量删除题目 |

### 考试管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/exams | 分页查询考试列表 |
| POST | /api/exams | 创建考试 |
| GET | /api/exams/{id} | 获取考试详情 |
| PUT | /api/exams/{id} | 更新考试 |
| DELETE | /api/exams/{id} | 删除考试 |
| POST | /api/exams/{id}/start | 开始考试 |
| POST | /api/exams/{id}/submit | 提交考试 |
| GET | /api/exams/{id}/analysis | 考试结果分析 |

### 实验管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/experiments | 分页查询实验列表 |
| POST | /api/experiments | 创建实验 |
| GET | /api/experiments/{id} | 获取实验详情 |
| PUT | /api/experiments/{id} | 更新实验 |
| DELETE | /api/experiments/{id} | 删除实验 |
| POST | /api/experiments/{id}/submit | 提交实验代码 |
| GET | /api/experiments/{id}/result | 获取实验结果 |

## 默认账号

系统初始化后会创建一个管理员账号：

- **用户名**: admin
- **密码**: admin123

## 测试接口

### 1. 登录获取 Token

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

### 2. 使用 Token 访问接口

```bash
curl -X GET http://localhost:8080/api/user/info \
  -H "Authorization: Bearer <your_token>"
```

## 注意事项

1. **JWT 密钥**: 生产环境请修改 `application.yml` 中的 `jwt.secret` 配置
2. **数据库密码**: 请修改默认的数据库密码
3. **跨域配置**: 根据实际前端地址修改 `CorsConfig.java`
4. **代码评测**: 实验模块的代码评测功能需要集成外部评测服务（如 Judge0）

## License

Apache 2.0
