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
        meta: { title: '首页', icon: 'HomeFilled' }
      },
      {
        path: 'courses',
        name: 'Courses',
        component: () => import('@/views/course/index.vue'),
        meta: { title: '课程管理', icon: 'Reading' }
      },
      {
        path: 'courses/:id',
        name: 'CourseDetail',
        component: () => import('@/views/course/detail.vue'),
        meta: { title: '课程详情', hidden: true }
      },
      {
        path: 'classes',
        name: 'Classes',
        component: () => import('@/views/class/index.vue'),
        meta: { title: '班级管理', icon: 'School' }
      },
      {
        path: 'classes/:id',
        name: 'ClassDetail',
        component: () => import('@/views/class/detail.vue'),
        meta: { title: '班级详情', hidden: true }
      },
      {
        path: 'homeworks',
        name: 'Homeworks',
        component: () => import('@/views/homework/index.vue'),
        meta: { title: '作业管理', icon: 'EditPen' }
      },
      {
        path: 'homeworks/:id',
        name: 'HomeworkDetail',
        component: () => import('@/views/homework/detail.vue'),
        meta: { title: '作业详情', hidden: true }
      },
      {
        path: 'exams',
        name: 'Exams',
        component: () => import('@/views/exam/index.vue'),
        meta: { title: '考试管理', icon: 'Document' }
      },
      {
        path: 'exams/:id',
        name: 'ExamDetail',
        component: () => import('@/views/exam/detail.vue'),
        meta: { title: '考试详情', hidden: true }
      },
      {
        path: 'exams/:id/take',
        name: 'TakeExam',
        component: () => import('@/views/exam/take.vue'),
        meta: { title: '参加考试', hidden: true }
      },
      {
        path: 'questions',
        name: 'Questions',
        component: () => import('@/views/question/index.vue'),
        meta: { title: '题库管理', icon: 'Collection' }
      },
      {
        path: 'experiments',
        name: 'Experiments',
        component: () => import('@/views/experiment/index.vue'),
        meta: { title: '实验管理', icon: 'Monitor' }
      },
      {
        path: 'experiments/:id',
        name: 'ExperimentDetail',
        component: () => import('@/views/experiment/detail.vue'),
        meta: { title: '实验详情', hidden: true }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/profile/index.vue'),
        meta: { title: '个人中心', icon: 'User', hidden: true }
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
  
  if (requiresAuth && !userStore.token) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
  } else if (to.name === 'Login' && userStore.token) {
    next({ name: 'Dashboard' })
  } else {
    next()
  }
})

export default router
