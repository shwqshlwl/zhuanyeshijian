# 侧边栏菜单点击无反应问题 - 诊断指南

## 问题描述
合并代码后，班级管理、作业管理、考试管理、实验管理菜单项点击没有反应，无法跳转。

## 已完成的检查

### ✅ 1. 路由配置检查
所有路由配置正确：
- `/classes` - 班级管理
- `/homeworks` - 作业管理
- `/exams` - 考试管理
- `/experiments` - 实验管理

### ✅ 2. 视图文件检查
所有必要的 Vue 文件都存在：
- `frontend/src/views/class/index.vue`
- `frontend/src/views/homework/index.vue`
- `frontend/src/views/exam/index.vue`
- `frontend/src/views/experiment/index.vue`

### ✅ 3. 角色权限逻辑检查
`stores/user.js` 中的角色判断逻辑正确：
- `isStudent: userType === 1`
- `isTeacher: userType === 2`
- `isAdmin: userType === 3`

## 诊断步骤

### 第一步：检查浏览器控制台

1. 打开浏览器开发者工具（按 F12）
2. 切换到 **Console** 标签页
3. 刷新页面
4. 查看以下调试信息：

**预期看到的信息：**
```
当前用户角色: teacher （或 admin）
userStore.userType: 2 （或 3）
过滤后的菜单路由: [...]
```

**点击菜单时预期看到：**
```
路由守卫 - 跳转到: /classes 来自: /dashboard
路由守卫 - 权限检查: {...}
路由守卫 - 允许跳转
```

### 第二步：根据控制台输出诊断问题

#### 情况A：看到 "权限不足，重定向到首页"
**问题原因：** 当前登录用户角色与菜单权限不匹配
**解决方案：**
1. 确认当前登录的用户角色（查看控制台 `userStore.userType`）
2. 如果是学生账号（userType=1），需要使用教师或管理员账号登录
3. 教师账号：username=hxw, password=123456
4. 管理员账号：username=admin, password=123456

#### 情况B：看到 "未登录，重定向到登录页"
**问题原因：** token 失效或被清除
**解决方案：** 重新登录

#### 情况C：userType 显示错误值或 undefined
**问题原因：** localStorage 中的 userInfo 数据过期
**解决方案：**
1. 在控制台执行以下命令清除缓存：
   ```javascript
   localStorage.clear()
   sessionStorage.clear()
   location.reload(true)
   ```
2. 重新登录系统

#### 情况D：没有看到任何调试信息
**问题原因：** 前端代码未更新或浏览器缓存问题
**解决方案：**
1. 硬性刷新浏览器：
   - Windows/Linux: **Ctrl + Shift + R**
   - Mac: **Cmd + Shift + R**
2. 或者清除网站数据：
   - 打开开发者工具 > Application 标签
   - 左侧找到 'Storage' > 'Clear site data'
   - 勾选所有选项并点击 'Clear site data'
   - 刷新页面重新登录

### 第三步：验证修复

修复后，点击菜单项应该：
1. 在控制台看到 "路由守卫 - 允许跳转"
2. 页面成功跳转到对应的管理页面
3. 面包屑导航正确显示

## 常见问题解答

### Q1: 为什么合并代码后会出现这个问题？
A: 合并代码引入了新的角色权限系统，旧的 localStorage 缓存中的用户信息可能与新系统不兼容。

### Q2: 清除缓存会丢失数据吗？
A: 只会清除浏览器本地缓存（token、userInfo），服务器端的数据不受影响。重新登录即可恢复。

### Q3: 学生账号应该看到哪些菜单？
A: 学生账号只能看到：首页、我的课程、我的作业、我的考试

### Q4: 教师账号应该看到哪些菜单？
A: 教师账号可以看到：首页、课程管理、批量导入学生、班级管理、作业管理、考试管理、题库管理、实验管理

## 移除调试代码

问题修复后，可以移除添加的 console.log 调试代码：

1. 编辑 `frontend/src/layout/index.vue`，移除第 116-119 行的 console.log
2. 编辑 `frontend/src/router/index.js`，移除所有 console.log 语句（第 153, 163, 170, 178-186, 190, 196 行）

## 联系支持

如果按照上述步骤仍无法解决问题，请提供：
1. 浏览器控制台的完整输出截图
2. 当前登录用户的 userType 值
3. 点击菜单时的控制台输出
