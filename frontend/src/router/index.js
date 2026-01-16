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
        meta: { title: '首页', icon: 'HomeFilled', roles: ['student', 'teacher', 'admin'] }
      },
      {
        path: 'courses',
        name: 'Courses',
        component: () => import('@/views/course/index.vue'),
        meta: { title: '课程管理', icon: 'Reading', roles: ['teacher', 'admin'] }
      },
      {
        path: 'my-courses',
        name: 'MyCourses',
        component: () => import('@/views/course/my-courses.vue'),
        meta: { title: '我的课程', icon: 'Reading', roles: ['student'] }
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
        meta: { title: '批量导入学生', icon: 'UserFilled', roles: ['teacher', 'admin'] }
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
        name: 'MyHomeworks',
        component: () => import('@/views/homework/my-homeworks.vue'),
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
        path: 'experiments/:id',
        name: 'ExperimentDetail',
        component: () => import('@/views/experiment/detail.vue'),
        meta: { title: '实验详情', hidden: true, roles: ['student', 'teacher', 'admin'] }
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
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 课程智慧教学平台` : '课程智慧教学平台'

  const userStore = useUserStore()
  const requiresAuth = to.meta.requiresAuth !== false

  // 未登录检查
  if (requiresAuth && !userStore.token) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
    return
  }

  // 已登录访问登录页，跳转到首页
  if (to.name === 'Login' && userStore.token) {
    next({ name: 'Dashboard' })
    return
  }

  // 角色权限检查
  if (to.meta.roles && to.meta.roles.length > 0) {
    const userRole = userStore.isStudent ? 'student' : userStore.isTeacher ? 'teacher' : 'admin'

    if (!to.meta.roles.includes(userRole)) {
      // 无权限，跳转到首页
      next({ name: 'Dashboard' })
      return
    }
  }

  next()
})

export default router
