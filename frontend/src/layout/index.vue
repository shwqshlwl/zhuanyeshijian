<template>
  <div class="layout-container">
    <!-- 侧边栏 -->
    <aside class="sidebar" :class="{ 'is-collapse': isCollapse }">
      <div class="logo">
        <img src="@/assets/logo.svg" alt="Logo" class="logo-img" />
        <span v-show="!isCollapse" class="logo-text">智慧教学平台</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :collapse-transition="false"
        router
        background-color="#1e293b"
        text-color="#94a3b8"
        active-text-color="#ffffff"
        class="sidebar-menu"
      >
        <template v-for="route in menuRoutes" :key="route.path">
          <el-menu-item :index="'/' + route.path">
            <el-icon>
              <component :is="route.meta?.icon || 'Document'" />
            </el-icon>
            <template #title>{{ route.meta?.title }}</template>
          </el-menu-item>
        </template>
      </el-menu>
    </aside>

    <!-- 主内容区 -->
    <div class="main-container">
      <!-- 顶部导航 -->
      <header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="toggleCollapse">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentRoute.meta?.title !== '首页'">
              {{ currentRoute.meta?.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <div v-if="!userStore.token" class="guest-actions">
             <el-button type="primary" link @click="router.push('/login')">登录</el-button>
             <el-divider direction="vertical" />
             <el-button type="primary" link @click="router.push('/register')">注册</el-button>
          </div>
          <el-dropdown v-else trigger="click" @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="32" class="user-avatar">
                {{ userStore.realName?.charAt(0) || userStore.username?.charAt(0) || 'U' }}
              </el-avatar>
              <span class="user-name">{{ userStore.realName || userStore.username }}</span>
              <el-tag :type="getRoleTagType()" size="small" class="role-tag">{{ userStore.roleText }}</el-tag>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item command="password">
                  <el-icon><Lock /></el-icon>修改密码
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <!-- 内容区 -->
      <main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isCollapse = ref(false)
const userModules = ref([])

const currentRoute = computed(() => route)
const activeMenu = computed(() => route.path)

// 加载用户可见模块
const loadUserModules = async () => {
  if (!userStore.token) {
    userModules.value = []
    return
  }

  try {
    // 从localStorage获取缓存
    const cached = localStorage.getItem('userModules')
    if (cached) {
      userModules.value = JSON.parse(cached)
    }

    // 从API获取最新数据
    const data = await request.get('/user/modules')
    userModules.value = data.data || []

    // 缓存到localStorage
    localStorage.setItem('userModules', JSON.stringify(userModules.value))
  } catch (error) {
    console.error('加载用户模块失败:', error)
    // 如果API调用失败，使用缓存数据
    const cached = localStorage.getItem('userModules')
    if (cached) {
      userModules.value = JSON.parse(cached)
    }
  }
}

// 获取菜单路由
const menuRoutes = computed(() => {
  // 如果未登录，显示AI助教
  if (!userStore.token) {
    const mainRoute = router.options.routes.find(r => r.path === '/')
    const allRoutes = mainRoute?.children?.filter(r => !r.meta?.hidden) || []
    return allRoutes.filter(route => route.meta?.requiresAuth === false)
  }

  // 使用API返回的模块列表构建菜单
  return userModules.value.map(module => ({
    path: module.path.startsWith('/') ? module.path.substring(1) : module.path,
    meta: {
      title: module.moduleName,
      icon: module.icon
    }
  }))
})

// 监听登录状态变化
watch(() => userStore.token, (newToken) => {
  if (newToken) {
    loadUserModules()
  } else {
    userModules.value = []
    localStorage.removeItem('userModules')
  }
})

// 组件挂载时加载模块
onMounted(() => {
  if (userStore.token) {
    loadUserModules()
  }
})

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

const getRoleTagType = () => {
  if (userStore.isStudent) return 'info'
  if (userStore.isTeacher) return 'success'
  if (userStore.isAdmin) return 'danger'
  return ''
}

const handleCommand = (command) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'password':
      router.push('/profile?tab=password')
      break
    case 'logout':
      ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        userStore.logout()
        router.push('/login')
      })
      break
  }
}
</script>

<style lang="scss" scoped>
.layout-container {
  display: flex;
  height: 100vh;
  background-color: #f0f2f5;
}

.sidebar {
  width: 220px;
  background-color: #1e293b;
  transition: width 0.3s;
  display: flex;
  flex-direction: column;
  
  &.is-collapse {
    width: 64px;
  }
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 16px;
  background-color: #0f172a;
  
  .logo-img {
    width: 32px;
    height: 32px;
  }
  
  .logo-text {
    margin-left: 10px;
    font-size: 16px;
    font-weight: 600;
    color: #fff;
    white-space: nowrap;
  }
}

.sidebar-menu {
  flex: 1;
  border-right: none;
  overflow-y: auto;
  
  &:not(.el-menu--collapse) {
    width: 220px;
  }
  
  :deep(.el-menu-item) {
    &:hover {
      background-color: #334155 !important;
    }
    
    &.is-active {
      background-color: #3b82f6 !important;
    }
  }
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.header {
  height: 60px;
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  z-index: 10;
}

.header-left {
  display: flex;
  align-items: center;
  
  .collapse-btn {
    font-size: 20px;
    cursor: pointer;
    margin-right: 16px;
    color: #606266;
    
    &:hover {
      color: #3b82f6;
    }
  }
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  
  &:hover {
    background-color: #f5f7fa;
  }
  
  .user-avatar {
    background-color: #3b82f6;
    color: #fff;
  }
  
  .user-name {
    margin: 0 8px;
    font-size: 14px;
    color: #303133;
  }

  .role-tag {
    margin-right: 8px;
  }
}

.main-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

// 过渡动画
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
