# 课程智慧教学平台

一个基于 Spring Boot + Vue 3 的在线教学平台，支持课程管理、作业管理、考试管理、实验管理（含真实代码执行）等功能。

## ✨ 主要功能

- 👥 **用户管理**：学生、教师、管理员三种角色
- 📚 **课程管理**：课程创建、编辑、学生选课
- 📝 **作业管理**：作业发布、提交、批改
- 📊 **考试管理**：在线考试、自动评分
- 🧪 **实验管理**：编程实验、代码执行、自动评测
- 💬 **题库管理**：题目创建、分类、复用
- 👨‍🏫 **班级管理**：班级创建、学生管理

## 🚀 技术栈

### 后端
- Spring Boot 3.2.5
- Spring Security（JWT 认证）
- MyBatis Plus
- MySQL 8.0
- Maven

### 前端
- Vue 3
- Vite
- Element Plus
- Pinia（状态管理）
- Vue Router
- Axios

## 📦 快速开始

### 前置要求

- JDK 17+
- Node.js 16+
- MySQL 8.0+
- Maven 3.6+

### 克隆项目

```bash
git clone <your-repository-url>
cd zhuanyehsijian2
```

### 后端设置

1. **创建数据库**
```sql
CREATE DATABASE course_teaching_platform DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. **配置数据库连接**
```bash
# 编辑 sj/src/main/resources/application.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/course_teaching_platform
    username: root
    password: your_password  # 修改为你的密码
```

3. **执行 SQL 脚本**
```bash
mysql -u root -p course_teaching_platform < sj/src/main/resources/create.sql
mysql -u root -p course_teaching_platform < sj/src/main/resources/test-data.sql
```

4. **启动后端**
```bash
cd sj
mvn clean install
mvn spring-boot:run
```

后端将在 `http://localhost:8080` 启动

### 前端设置

1. **安装依赖**
```bash
cd frontend
npm install
```

2. **启动开发服务器**
```bash
npm run dev
```

前端将在 `http://localhost:5173` 启动

### 默认账号

- **管理员**：admin / admin123
- **教师**：teacher1 / 123456
- **学生**：student1 / 123456

## 🧪 代码执行功能

系统支持真实的代码执行和自动评测，支持以下语言：

- ☕ Java
- 🐍 Python
- 🔧 C
- ⚙️ C++
- 📜 JavaScript (Node.js)

### 环境配置

**Windows**：
```bash
# Python
下载：https://www.python.org/downloads/
安装时勾选 "Add Python to PATH"

# GCC/G++ (MinGW-w64)
下载：https://sourceforge.net/projects/mingw-w64/
安装到 C:\mingw64
添加 C:\mingw64\bin 到系统 PATH

# Node.js
下载：https://nodejs.org/
```

**Linux**：
```bash
sudo apt update
sudo apt install openjdk-17-jdk python3 build-essential nodejs
```

详细配置请查看 [CODE_EXECUTION_SETUP.md](CODE_EXECUTION_SETUP.md)

## 📚 文档

- [协作指南](COLLABORATION_GUIDE.md) - 团队协作和开发规范
- [代码执行指南](CODE_EXECUTION_GUIDE.md) - 代码执行功能使用说明
- [环境配置](CODE_EXECUTION_SETUP.md) - 编译器和运行环境配置
- [问题修复记录](FIXES.md) - 已知问题和解决方案

## 🔧 常见问题

### 1. Vite 绝对路径错误

**错误**：`Failed to resolve import "D:/..."`

**解决**：
```bash
cd frontend
npm run clean
npm install
```

### 2. 数据库连接失败

检查：
- MySQL 服务是否启动
- 数据库名称是否正确
- 用户名密码是否正确
- 端口是否为 3306

### 3. 代码执行失败

检查：
- 编译器/解释器是否安装
- 是否添加到系统 PATH
- 重启后端服务

更多问题请查看 [COLLABORATION_GUIDE.md](COLLABORATION_GUIDE.md)

## 📁 项目结构

```
zhuanyehsijian2/
├── frontend/                 # 前端项目
│   ├── src/
│   │   ├── api/             # API 接口
│   │   ├── components/      # 公共组件
│   │   ├── views/           # 页面组件
│   │   ├── router/          # 路由配置
│   │   ├── stores/          # 状态管理
│   │   └── utils/           # 工具函数
│   ├── public/              # 静态资源
│   └── package.json
│
├── sj/                      # 后端项目
│   ├── src/main/java/
│   │   └── com/example/citp/
│   │       ├── controller/  # 控制器
│   │       ├── service/     # 服务层
│   │       ├── mapper/      # 数据访问层
│   │       ├── model/       # 数据模型
│   │       ├── config/      # 配置类
│   │       ├── security/    # 安全配置
│   │       └── exception/   # 异常处理
│   ├── src/main/resources/
│   │   ├── application.yml  # 配置文件
│   │   ├── create.sql       # 数据库脚本
│   │   └── test-data.sql    # 测试数据
│   └── pom.xml
│
└── docs/                    # 文档
```

## 🤝 贡献指南

1. Fork 本仓库
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'feat: Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

提交规范请参考 [COLLABORATION_GUIDE.md](COLLABORATION_GUIDE.md)

## 📄 许可证

本项目采用 MIT 许可证

## 👥 团队

- 开发团队：[你的团队名称]
- 联系方式：[联系邮箱]

## 🙏 致谢

感谢所有为本项目做出贡献的开发者！

---

**注意**：首次克隆项目后，请务必阅读 [COLLABORATION_GUIDE.md](COLLABORATION_GUIDE.md) 避免常见问题！
