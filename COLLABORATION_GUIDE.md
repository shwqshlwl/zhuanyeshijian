# 项目协作指南

## 📦 克隆项目后的设置步骤

### 1. 克隆项目
```bash
git clone <your-repository-url>
cd zhuanyehsijian2
```

### 2. 前端设置

```bash
cd frontend

# 安装依赖
npm install

# 清理 Vite 缓存（重要！）
npm run clean
# 或手动删除
rm -rf node_modules/.vite
rm -rf .vite

# 启动开发服务器
npm run dev
```

### 3. 后端设置

```bash
cd sj

# 使用 Maven 安装依赖
mvn clean install

# 或使用 IDE（IntelliJ IDEA / Eclipse）
# 1. 导入项目为 Maven 项目
# 2. 等待依赖下载完成
# 3. 运行 CitpApplication.java
```

## 🔧 常见问题解决

### 问题 1：Vite 绝对路径错误

**错误信息**：
```
Failed to resolve import "D:/ZY]/zhuanyeshijian/frontend/node_modules/..."
```

**解决方案**：
```bash
cd frontend

# 方案 1：删除缓存并重新安装
rm -rf node_modules
rm -rf .vite
rm -rf dist
npm install

# 方案 2：清理 npm 缓存
npm cache clean --force
npm install

# 方案 3：删除 package-lock.json 重新安装
rm package-lock.json
rm -rf node_modules
npm install
```

### 问题 2：端口被占用

**前端端口冲突**：
```bash
# 修改 frontend/vite.config.js
server: {
  port: 5174  // 改为其他端口
}
```

**后端端口冲突**：
```bash
# 修改 sj/src/main/resources/application.yml
server:
  port: 8081  # 改为其他端口
```

### 问题 3：数据库连接失败

**检查配置**：
```yaml
# sj/src/main/resources/application.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/course_teaching_platform?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password  # 修改为你的密码
```

**初始化数据库**：
```bash
# 1. 创建数据库
mysql -u root -p
CREATE DATABASE course_teaching_platform DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 2. 执行 SQL 脚本
USE course_teaching_platform;
source sj/src/main/resources/create.sql;
source sj/src/main/resources/test-data.sql;
```

## 📝 开发规范

### Git 提交规范

```bash
# 功能开发
git commit -m "feat: 添加实验管理功能"

# Bug 修复
git commit -m "fix: 修复代码执行超时问题"

# 文档更新
git commit -m "docs: 更新 README"

# 代码重构
git commit -m "refactor: 重构代码执行服务"

# 样式调整
git commit -m "style: 调整页面布局"

# 性能优化
git commit -m "perf: 优化查询性能"
```

### 分支管理

```bash
# 主分支
main / master - 生产环境代码

# 开发分支
dev - 开发环境代码

# 功能分支
feature/实验管理
feature/代码执行

# 修复分支
fix/登录问题
fix/数据库连接
```

### 工作流程

```bash
# 1. 从 dev 分支创建功能分支
git checkout dev
git pull origin dev
git checkout -b feature/新功能

# 2. 开发并提交
git add .
git commit -m "feat: 实现新功能"

# 3. 推送到远程
git push origin feature/新功能

# 4. 创建 Pull Request
# 在 GitHub 上创建 PR，请求合并到 dev 分支

# 5. 代码审查通过后合并
# 合并后删除功能分支
git checkout dev
git pull origin dev
git branch -d feature/新功能
```

## 🚀 部署指南

### 前端部署

```bash
cd frontend

# 构建生产版本
npm run build

# 构建产物在 dist 目录
# 将 dist 目录部署到 Nginx 或其他静态服务器
```

### 后端部署

```bash
cd sj

# 打包
mvn clean package -DskipTests

# 运行
java -jar target/course-intelligent-teaching-platform-1.0.0.jar

# 或使用 Spring Boot Maven 插件
mvn spring-boot:run
```

## 🔐 环境变量配置

### 前端环境变量

创建 `frontend/.env.local`（不要提交到 Git）：
```env
VITE_API_BASE_URL=http://localhost:8080/api
```

创建 `frontend/.env.production`：
```env
VITE_API_BASE_URL=https://your-domain.com/api
```

### 后端环境变量

创建 `sj/src/main/resources/application-dev.yml`（不要提交到 Git）：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/course_teaching_platform
    username: root
    password: your_local_password
```

创建 `sj/src/main/resources/application-prod.yml`：
```yaml
spring:
  datasource:
    url: jdbc:mysql://your-server:3306/course_teaching_platform
    username: prod_user
    password: ${DB_PASSWORD}  # 使用环境变量
```

## 📋 提交前检查清单

- [ ] 代码已测试，功能正常
- [ ] 没有提交 `node_modules/`
- [ ] 没有提交 `.vite/` 缓存
- [ ] 没有提交 `target/` 目录
- [ ] 没有提交包含敏感信息的配置文件
- [ ] 代码格式化完成
- [ ] 提交信息清晰明确
- [ ] 已解决所有冲突

## 🛠️ 推荐工具

### IDE
- **前端**：VS Code + Volar 插件
- **后端**：IntelliJ IDEA / Eclipse

### VS Code 推荐插件
- Volar（Vue 3 支持）
- ESLint
- Prettier
- GitLens

### IntelliJ IDEA 推荐插件
- Lombok
- MyBatis
- GitToolBox

## 📞 遇到问题？

1. **查看文档**：先查看项目文档和 README
2. **搜索 Issues**：在 GitHub Issues 中搜索类似问题
3. **提问**：创建新的 Issue，详细描述问题
4. **联系团队**：联系项目维护者

## 🎯 快速命令参考

```bash
# 前端
cd frontend
npm install          # 安装依赖
npm run dev          # 开发模式
npm run build        # 构建生产版本
npm run clean        # 清理缓存

# 后端
cd sj
mvn clean install    # 安装依赖
mvn spring-boot:run  # 运行应用
mvn clean package    # 打包

# Git
git status           # 查看状态
git add .            # 添加所有更改
git commit -m "msg"  # 提交
git push             # 推送
git pull             # 拉取
git checkout -b xxx  # 创建并切换分支
```

## 📚 相关文档

- [README.md](README.md) - 项目介绍
- [CODE_EXECUTION_GUIDE.md](CODE_EXECUTION_GUIDE.md) - 代码执行功能指南
- [CODE_EXECUTION_SETUP.md](CODE_EXECUTION_SETUP.md) - 环境配置
- [FIXES.md](FIXES.md) - 问题修复记录

---

**注意**：首次克隆项目后，务必执行以下命令清理缓存：

```bash
cd frontend
rm -rf node_modules/.vite
rm -rf .vite
npm install
```

这样可以避免绝对路径问题！
