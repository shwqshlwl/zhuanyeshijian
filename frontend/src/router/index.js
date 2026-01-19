import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/login/register.vue'),
    meta: { title: '教师注册', requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/layout/index.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '首页', icon: 'HomeFilled', roles: ['teacher', 'admin'] }
      },
      {
        path: 'ai',
        name: 'AIChat',
        component: () => import('@/views/ai/chat.vue'),
        meta: { title: 'AI 助教', icon: 'ChatDotRound', requiresAuth: false }
      },
      {
        path: 'ai/history',
        name: 'AIHistory',
        component: () => import('@/views/ai/history.vue'),
        meta: { title: 'AI 会话历史', hidden: true, requiresAuth: false }
      },
      {
        path: 'courses',
        name: 'Courses',
        component: () => import('@/views/course/index.vue'),
        meta: { title: '课程管理', icon: 'Reading', roles: ['teacher', 'admin'] }
      },
      {
        path: 'course-templates',
        name: 'CourseTemplates',
        component: () => import('@/views/course/course-templates.vue'),
        meta: { title: '学期开课管理', icon: 'Reading', roles: ['teacher', 'admin'] }
      },
      {
        path: 'course-offerings/:id',
        name: 'CourseOfferingDetail',
        component: () => import('@/views/course/offering-detail.vue'),
        meta: { title: '开课详情', hidden: true, roles: ['teacher', 'admin'] }
      },
      {
        path: 'my-courses',
        name: 'MyCourses',
        component: () => import('@/views/course/my-courses.vue'),
        meta: { title: '我的课程', icon: 'Reading', roles: ['student'] }
      },
      {
        path: 'elective',
        name: 'Elective',
        component: () => import('@/views/elective/index.vue'),
        meta: { title: '选课中心', icon: 'ShoppingCart', roles: ['student'] }
      },
      {
        path: 'courses/:id',
        name: 'CourseDetail',
        component: () => import('@/views/course/detail.vue'),
        meta: { title: '课程详情', hidden: true, roles: ['student', 'teacher', 'admin'] }
      },
      {
        path: 'course-status',
        name: 'CourseStatus',
        component: () => import('@/views/course/status.vue'),
        meta: { title: '课程状态管理', icon: 'Switch', hidden: true, roles: ['teacher', 'admin'] }
      },
      {
        path: 'user-batch-import',
        name: 'UserBatchImport',
        component: () => import('@/views/user/batch-import.vue'),
        meta: { title: '批量导入学生', icon: 'Upload', roles: ['teacher', 'admin'] }
      },
      {
        path: 'admin/users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/users.vue'),
        meta: { title: '用户管理', icon: 'UserFilled', roles: ['admin'] }
      },
      {
        path: 'admin/modules',
        name: 'AdminModules',
        component: () => import('@/views/admin/modules.vue'),
        meta: { title: '模块管理', icon: 'Menu', roles: ['admin'] }
      },
      {
        path: 'admin/semesters',
        name: 'AdminSemesters',
        component: () => import('@/views/admin/semester.vue'),
        meta: { title: '学期管理', icon: 'Calendar', roles: ['admin'] }
      },
      {
        path: 'classes',
        name: 'Classes',
        component: () => import('@/views/class/index.vue'),
        meta: { title: '班级管理', icon: 'School', roles: ['teacher', 'admin'] }
      },
      {
        path: 'classes/:id',
        name: 'ClassDetail',
        component: () => import('@/views/class/detail.vue'),
        meta: { title: '班级详情', hidden: true, roles: ['teacher', 'admin'] }
      },
      {
        path: 'homeworks',
        name: 'Homeworks',
        component: () => import('@/views/homework/index.vue'),
        meta: { title: '作业管理', icon: 'EditPen', roles: ['teacher', 'admin'] }
      },
      {
        path: 'my-homeworks',
        name: 'myHomeworks',
        component: () => import('@/views/homework/index.vue'),
        meta: { title: '我的作业', icon: 'EditPen', roles: ['student'] }
      },
      {
        path: 'homeworks/:id',
        name: 'HomeworkDetail',
        component: () => import('@/views/homework/detail.vue'),
        meta: { title: '作业详情', hidden: true, roles: ['student', 'teacher', 'admin'] }
      },
      {
        path: 'exams',
        name: 'Exams',
        component: () => import('@/views/exam/index.vue'),
        meta: { title: '考试管理', icon: 'Document', roles: ['teacher', 'admin'] }
      },
      {
        path: 'my-exams',
        name: 'MyExams',
        component: () => import('@/views/exam/my-exams.vue'),
        meta: { title: '我的考试', icon: 'Document', roles: ['student'] }
      },
      {
        path: 'exams/:id',
        name: 'ExamDetail',
        component: () => import('@/views/exam/detail.vue'),
        meta: { title: '考试详情', hidden: true, roles: ['student', 'teacher', 'admin'] }
      },
      {
        path: 'exams/:id/take',
        name: 'TakeExam',
        component: () => import('@/views/exam/take.vue'),
        meta: { title: '参加考试', hidden: true, roles: ['student'] }
      },
      {
        path: 'questions',
        name: 'Questions',
        component: () => import('@/views/question/index.vue'),
        meta: { title: '题库管理', icon: 'Collection', roles: ['teacher', 'admin'] }
      },
      {
        path: 'experiments',
        name: 'Experiments',
        component: () => import('@/views/experiment/index.vue'),
        meta: { title: '实验管理', icon: 'Monitor', roles: ['teacher', 'admin'] }
      },
      {
        path: 'experiments/create',
        name: 'ExperimentCreate',
        component: () => import('@/views/experiment/create.vue'),
        meta: { title: '创建实验', hidden: true, roles: ['teacher', 'admin'] }
      },
      {
        path: 'experiments/:id/edit',
        name: 'ExperimentEdit',
        component: () => import('@/views/experiment/create.vue'),
        meta: { title: '编辑实验', hidden: true, roles: ['teacher', 'admin'] }
      },
      {
        path: 'experiments/:id',
        name: 'ExperimentDetail',
        component: () => import('@/views/experiment/detail.vue'),
        meta: { title: '实验详情', hidden: true, roles: ['student', 'teacher', 'admin'] }
      },
      {
        path: 'experiments/:id/answer',
        name: 'ExperimentAnswer',
        component: () => import('@/views/experiment/answer.vue'),
        meta: { title: '实验答题', hidden: true, roles: ['student'] }
      },
      {
        path: 'experiments/:id/execute',
        name: 'ExperimentExecute',
        component: () => import('@/views/experiment/execute.vue'),
        meta: { title: '代码执行', hidden: true, roles: ['student'] }
      },
      {
        path: 'experiments/:id/result',
        name: 'ExperimentResult',
        component: () => import('@/views/experiment/result.vue'),
        meta: { title: '评测结果', hidden: true, roles: ['student'] }
      },
      {
        path: 'my-experiments',
        name: 'MyExperiments',
        component: () => import('@/views/experiment/index.vue'),
        meta: { title: '我的实验', icon: 'Monitor', roles: ['student'] }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/profile/index.vue'),
        meta: { title: '个人中心', icon: 'User', hidden: true, roles: ['student', 'teacher', 'admin'] }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '404', requiresAuth: false }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  console.log('路由守卫 - 跳转到:', to.path, '来自:', from.path)

  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 课程智慧教学平台` : '课程智慧教学平台'

  const userStore = useUserStore()
  const requiresAuth = to.meta.requiresAuth !== false

  // 未登录检查
  if (requiresAuth && !userStore.token) {
    console.log('路由守卫 - 未登录，重定向到登录页')
    next({ name: 'Login', query: { redirect: to.fullPath } })
    return
  }

  // 已登录访问登录页，根据角色跳转
  if (to.name === 'Login' && userStore.token) {
    console.log('路由守卫 - 已登录访问登录页，根据角色重定向')
    const redirectPath = userStore.isStudent ? '/my-courses' : '/dashboard'
    next(redirectPath)
    return
  }

  // 角色权限检查
  if (to.meta.roles && to.meta.roles.length > 0) {
    const userRole = userStore.isStudent ? 'student' : userStore.isTeacher ? 'teacher' : 'admin'
    console.log('路由守卫 - 权限检查:', {
      path: to.path,
      requiredRoles: to.meta.roles,
      userRole,
      userType: userStore.userType,
      isStudent: userStore.isStudent,
      isTeacher: userStore.isTeacher,
      isAdmin: userStore.isAdmin
    })

    if (!to.meta.roles.includes(userRole)) {
      // 无权限，根据角色跳转到对应的默认页面
      console.log('路由守卫 - 权限不足，重定向到默认页面')
      const defaultPath = userRole === 'student' ? '/my-courses' : '/dashboard'
      next(defaultPath)
      return
    }
  }

  console.log('路由守卫 - 允许跳转')
  next()
})

export default router
