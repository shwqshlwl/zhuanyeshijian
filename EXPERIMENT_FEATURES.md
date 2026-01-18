# 实验功能完善说明

## 📋 功能概述

本次更新完善了实验和执行评测管理模块，实现了完整的实验创建、答题、执行、评测流程。

## 🎯 核心页面

### 1. 实验列表页 (`/experiments`)
**路径**: `frontend/src/views/experiment/index.vue`

**功能**:
- 教师：查看所有实验，创建、编辑、删除实验
- 学生：查看可参与的实验，开始答题
- 支持按课程筛选
- 分页展示

**操作**:
- 教师点击"创建实验"跳转到创建页
- 教师点击"编辑"跳转到编辑页
- 学生点击"开始答题"跳转到答题页
- 点击"详情"查看实验详情

---

### 2. 实验创建页 (`/experiments/create`)
**路径**: `frontend/src/views/experiment/create.vue`

**功能**:
- 完整的实验创建表单
- 支持设置基本信息（名称、课程、语言、时间等）
- 支持编写实验描述和要求
- 支持插入代码模板
- 支持添加多个测试用例
- 支持草稿和发布状态

**表单字段**:
- **基本信息**:
  - 实验名称（必填）
  - 所属课程（必填）
  - 编程语言（Java/Python/C++/C/JavaScript）
  - 开始时间、截止时间
  - 总分、时间限制、内存限制
  - 实验状态（草稿/已发布）

- **实验内容**:
  - 实验描述（必填）
  - 实验要求（必填）
  - 模板代码（可选）

- **测试用例**:
  - 输入数据
  - 期望输出
  - 支持添加多个测试用例

---

### 3. 实验答题页 (`/experiments/:id/answer`)
**路径**: `frontend/src/views/experiment/answer.vue`

**功能**:
- 左侧显示实验要求和信息
- 右侧代码编辑器
- 支持多种编程语言切换
- 代码行号显示
- Tab键自动缩进
- 运行测试功能
- 提交代码功能
- 保存草稿功能
- 查看提交历史
- 加载历史代码

**特色功能**:
- 全屏编辑模式
- 输入输出示例展示
- 实时保存草稿到本地存储
- 提交后自动跳转到评测结果页

---

### 4. 实验执行页 (`/experiments/:id/execute`)
**路径**: `frontend/src/views/experiment/execute.vue`

**功能**:
- 显示代码执行过程
- 实时执行日志
- 执行状态动画
- 进度条显示
- 标准输出展示
- 错误信息展示
- 代码预览

**执行流程**:
1. 显示"准备就绪"状态
2. 点击"开始执行"
3. 显示编译日志
4. 显示执行进度
5. 显示执行结果（成功/失败）
6. 展示输出或错误信息

---

### 5. 实验评测结果页 (`/experiments/:id/result`)
**路径**: `frontend/src/views/experiment/result.vue`

**功能**:
- 评测状态展示（通过/未通过/编译错误/运行错误）
- 得分统计
- 通过用例数量
- 执行时间和内存使用
- 测试用例详情
- 错误信息展示
- 提交的代码查看
- 代码复制功能

**评测详情**:
- 每个测试用例的通过情况
- 输入数据、期望输出、实际输出对比
- 错误信息详细展示
- 支持展开/折叠测试用例详情

---

### 6. 实验详情页 (`/experiments/:id`)
**路径**: `frontend/src/views/experiment/detail.vue`

**功能**:
- 查看实验完整信息
- 学生：代码编辑、运行、提交
- 教师：查看学生提交列表
- 提交历史查看

**更新内容**:
- 添加"开始答题"按钮（学生）
- 添加"执行页面"按钮
- 提交后自动跳转到评测结果页

---

## 🔄 页面跳转流程

### 学生流程
```
实验列表 → 开始答题 → 实验答题页
                    ↓
                运行测试 / 执行页面
                    ↓
                提交代码
                    ↓
                评测结果页
                    ↓
            继续答题 / 查看详情
```

### 教师流程
```
实验列表 → 创建实验 → 实验创建页
                    ↓
                保存实验
                    ↓
                返回列表
                    ↓
        查看详情 / 编辑 / 删除
```

---

## 🎨 UI/UX 特色

### 1. 代码编辑器
- 深色主题（VS Code风格）
- 行号显示
- 语法高亮（通过字体和颜色）
- Tab键自动缩进
- 滚动同步

### 2. 状态展示
- 渐变背景色区分不同状态
- 大图标展示（成功/失败/进行中）
- 动画效果（旋转加载、进度条）
- 颜色编码（绿色=成功，红色=失败，橙色=进行中）

### 3. 测试用例展示
- 卡片式布局
- 通过/未通过边框颜色区分
- 折叠面板展示详情
- 代码块格式化显示

### 4. 响应式设计
- 左右分栏布局（答题页）
- 网格布局（统计卡片）
- 自适应高度
- 滚动区域优化

---

## 🔧 技术实现

### 前端技术栈
- Vue 3 Composition API
- Element Plus UI组件库
- Vue Router 路由管理
- Axios HTTP请求
- LocalStorage 本地存储

### 后端接口
- `POST /experiments` - 创建实验
- `PUT /experiments/:id` - 更新实验
- `GET /experiments/:id` - 获取实验详情
- `POST /experiments/:id/run` - 运行代码
- `POST /experiments/:id/submit` - 提交代码
- `GET /experiments/:id/result` - 获取评测结果
- `GET /experiments/:id/my-submissions` - 获取我的提交历史
- `GET /experiments/:id/submissions` - 获取学生提交列表（教师）

### 核心功能实现

#### 1. 代码执行
```javascript
// 调用后端API执行代码
const res = await request({
  url: `/experiments/${experimentId}/run`,
  method: 'post',
  data: { code: code.value, language: selectedLanguage.value }
})
```

#### 2. 自动评测
- 后端异步评测
- 前端轮询获取结果
- 测试用例逐个执行
- 输出对比（忽略行尾空格）

#### 3. 草稿保存
```javascript
// 保存到本地存储
localStorage.setItem(`experiment_${experimentId}_code`, code.value)
localStorage.setItem(`experiment_${experimentId}_language`, selectedLanguage.value)
```

#### 4. 历史代码加载
```javascript
// 从提交历史加载代码
const loadHistoryCode = (item) => {
  code.value = item.code || ''
  selectedLanguage.value = item.language || selectedLanguage.value
}
```

---

## 📝 数据结构

### 实验表单数据
```javascript
{
  experimentName: string,      // 实验名称
  courseId: number,            // 课程ID
  language: string,            // 编程语言
  startTime: datetime,         // 开始时间
  endTime: datetime,           // 截止时间
  totalScore: number,          // 总分
  timeLimit: number,           // 时间限制(ms)
  memoryLimit: number,         // 内存限制(MB)
  status: number,              // 状态(0草稿 1发布)
  description: string,         // 实验描述
  requirement: string,         // 实验要求
  templateCode: string,        // 模板代码
  testCases: string            // 测试用例JSON
}
```

### 测试用例数据
```javascript
[
  {
    input: string,    // 输入数据
    output: string    // 期望输出
  }
]
```

### 评测结果数据
```javascript
{
  submitId: number,           // 提交ID
  status: number,             // 状态(0待评测 1评测中 2通过 3未通过 4编译错误 5运行错误)
  score: number,              // 得分
  passCount: number,          // 通过用例数
  totalCount: number,         // 总用例数
  executeTime: number,        // 执行时间(ms)
  memoryUsed: number,         // 内存使用(KB)
  errorMessage: string,       // 错误信息
  resultDetail: string,       // 测试用例详情JSON
  submitTime: datetime,       // 提交时间
  code: string                // 提交的代码
}
```

---

## 🚀 使用指南

### 教师使用

1. **创建实验**
   - 进入"实验管理"页面
   - 点击"创建实验"按钮
   - 填写实验信息
   - 添加测试用例
   - 保存实验

2. **管理实验**
   - 编辑实验信息
   - 查看学生提交情况
   - 查看学生代码
   - 删除实验

### 学生使用

1. **参与实验**
   - 进入"我的实验"页面
   - 点击"开始答题"
   - 阅读实验要求

2. **编写代码**
   - 在代码编辑器中编写代码
   - 点击"运行测试"查看输出
   - 点击"执行页面"查看详细执行过程
   - 点击"保存草稿"保存当前代码

3. **提交代码**
   - 确认代码无误
   - 点击"提交代码"
   - 查看评测结果
   - 根据结果继续修改

---

## ✨ 亮点功能

1. **完整的实验流程** - 从创建到评测的完整闭环
2. **实时代码执行** - 支持5种编程语言的真实执行
3. **自动评测系统** - 基于测试用例的自动评分
4. **友好的代码编辑器** - 行号、缩进、全屏等功能
5. **详细的评测反馈** - 每个测试用例的详细对比
6. **草稿自动保存** - 防止代码丢失
7. **提交历史管理** - 查看和加载历史代码
8. **执行过程可视化** - 实时日志和进度展示

---

## 🔒 安全考虑

1. **代码执行隔离** - 后端使用独立进程执行
2. **超时控制** - 防止无限循环
3. **内存限制** - 防止内存溢出
4. **权限控制** - 学生只能查看自己的提交
5. **输入验证** - 前后端双重验证

---

## 📊 性能优化

1. **异步评测** - 不阻塞用户操作
2. **本地缓存** - 草稿保存到本地
3. **按需加载** - 路由懒加载
4. **分页查询** - 大数据量优化
5. **轮询优化** - 评测中才轮询结果

---

## 🎓 总结

本次更新实现了完整的实验管理和代码评测功能，涵盖了：
- ✅ 6个核心页面
- ✅ 完整的用户流程
- ✅ 真实的代码执行
- ✅ 自动化评测系统
- ✅ 友好的用户界面
- ✅ 详细的反馈信息

满足了教学平台对实验功能的所有需求，为师生提供了完善的编程实验环境。
