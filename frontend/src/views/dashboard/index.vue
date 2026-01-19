<template>
  <div class="dashboard-container">
    <!-- 欢迎卡片 -->
    <div class="welcome-card">
      <div class="welcome-content">
        <h2 class="welcome-title">
          {{ getGreeting() }}，{{ userStore.realName || userStore.username }}！
        </h2>
        <p class="welcome-desc">欢迎使用课程智慧教学平台，祝您工作愉快！</p>
      </div>
      <div class="welcome-illustration">
        <el-icon :size="80" color="rgba(255,255,255,0.3)"><Reading /></el-icon>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-row">
      <!-- 管理员：总用户数 -->
      <el-col v-if="userStore.isAdmin" :xs="12" :sm="6">
        <div class="stat-card stat-card-red">
          <div class="stat-icon">
            <el-icon :size="28"><User /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalUsers || 0 }}</div>
            <div class="stat-label">总用户数</div>
            <div v-if="stats.totalTeachers" class="stat-detail">
              教师 {{ stats.totalTeachers }} · 学生 {{ stats.totalStudents }}
            </div>
          </div>
        </div>
      </el-col>
      <!-- 教师：我的课程数 -->
      <el-col v-if="!userStore.isAdmin" :xs="12" :sm="6">
        <div class="stat-card stat-card-blue">
          <div class="stat-icon">
            <el-icon :size="28"><Reading /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.courseCount || 0 }}</div>
            <div class="stat-label">我的课程</div>
          </div>
        </div>
      </el-col>
      <!-- 课程数 -->
      <el-col :xs="12" :sm="6">
        <div class="stat-card stat-card-blue">
          <div class="stat-icon">
            <el-icon :size="28"><Reading /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ userStore.isAdmin ? (stats.totalCourses || 0) : (stats.courseCount || 0) }}</div>
            <div class="stat-label">{{ userStore.isAdmin ? '总课程数' : '我的课程' }}</div>
          </div>
        </div>
      </el-col>
      <!-- 班级数 -->
      <el-col :xs="12" :sm="6">
        <div class="stat-card stat-card-green">
          <div class="stat-icon">
            <el-icon :size="28"><School /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ userStore.isAdmin ? (stats.totalClasses || 0) : (stats.classCount || 0) }}</div>
            <div class="stat-label">{{ userStore.isAdmin ? '总班级数' : '我的班级' }}</div>
          </div>
        </div>
      </el-col>
      <!-- 作业数 -->
      <el-col :xs="12" :sm="6">
        <div class="stat-card stat-card-orange">
          <div class="stat-icon">
            <el-icon :size="28"><EditPen /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ userStore.isAdmin ? (stats.totalHomeworks || 0) : (stats.homeworkCount || 0) }}</div>
            <div class="stat-label">{{ userStore.isAdmin ? '总作业数' : '我的作业' }}</div>
          </div>
        </div>
      </el-col>
      <!-- 考试数 -->
      <el-col :xs="12" :sm="6">
        <div class="stat-card stat-card-purple">
          <div class="stat-icon">
            <el-icon :size="28"><Document /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ userStore.isAdmin ? (stats.totalExams || 0) : (stats.examCount || 0) }}</div>
            <div class="stat-label">{{ userStore.isAdmin ? '总考试数' : '我的考试' }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 快捷操作 -->
    <el-row :gutter="20">
      <el-col :xs="24" :lg="16">
        <div class="panel">
          <div class="panel-header">
            <h3 class="panel-title">快捷操作</h3>
          </div>
          <div class="quick-actions">
            <div class="action-item" @click="$router.push('/courses')">
              <div class="action-icon" style="background: linear-gradient(135deg, #3b82f6, #1d4ed8)">
                <el-icon :size="24"><Reading /></el-icon>
              </div>
              <span class="action-text">课程管理</span>
            </div>
            <div class="action-item" @click="$router.push('/classes')">
              <div class="action-icon" style="background: linear-gradient(135deg, #10b981, #059669)">
                <el-icon :size="24"><School /></el-icon>
              </div>
              <span class="action-text">班级管理</span>
            </div>
            <div class="action-item" @click="$router.push('/homeworks')">
              <div class="action-icon" style="background: linear-gradient(135deg, #f59e0b, #d97706)">
                <el-icon :size="24"><EditPen /></el-icon>
              </div>
              <span class="action-text">布置作业</span>
            </div>
            <div class="action-item" @click="$router.push('/exams')">
              <div class="action-icon" style="background: linear-gradient(135deg, #8b5cf6, #7c3aed)">
                <el-icon :size="24"><Document /></el-icon>
              </div>
              <span class="action-text">创建考试</span>
            </div>
            <div class="action-item" @click="$router.push('/questions')">
              <div class="action-icon" style="background: linear-gradient(135deg, #ec4899, #db2777)">
                <el-icon :size="24"><Collection /></el-icon>
              </div>
              <span class="action-text">题库管理</span>
            </div>
            <div class="action-item" @click="$router.push('/experiments')">
              <div class="action-icon" style="background: linear-gradient(135deg, #06b6d4, #0891b2)">
                <el-icon :size="24"><Monitor /></el-icon>
              </div>
              <span class="action-text">实验管理</span>
            </div>
          </div>
        </div>
      </el-col>
      
      <el-col :xs="24" :lg="8">
        <div class="panel">
          <div class="panel-header">
            <h3 class="panel-title">系统公告</h3>
          </div>
          <div class="notice-list">
            <div class="notice-item" v-for="(notice, index) in notices" :key="index">
              <el-icon class="notice-icon"><Bell /></el-icon>
              <div class="notice-content">
                <div class="notice-title">{{ notice.title }}</div>
                <div class="notice-time">{{ notice.time }}</div>
              </div>
            </div>
            <el-empty v-if="notices.length === 0" description="暂无公告" :image-size="60" />
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getCourseList } from '@/api/course'
import { getClassList } from '@/api/class'
import { getHomeworkList } from '@/api/homework'
import { getExamList } from '@/api/exam'
import request from '@/utils/request'

const userStore = useUserStore()

const stats = ref({
  // 教师统计
  courseCount: 0,
  classCount: 0,
  homeworkCount: 0,
  examCount: 0,
  // 管理员统计
  totalUsers: 0,
  totalTeachers: 0,
  totalStudents: 0,
  totalCourses: 0,
  totalClasses: 0,
  totalHomeworks: 0,
  totalExams: 0
})

const notices = ref([
  { title: '欢迎使用课程智慧教学平台', time: '2026-01-01' },
  { title: '系统已升级至最新版本', time: '2026-01-01' }
])

const getGreeting = () => {
  const hour = new Date().getHours()
  if (hour < 6) return '凌晨好'
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 17) return '下午好'
  if (hour < 19) return '傍晚好'
  return '晚上好'
}

// 获取管理员统计数据
const fetchAdminStats = async () => {
  try {
    console.log('获取管理员统计数据...')
    const data = await request.get('/admin/statistics')
    stats.value = {
      ...stats.value,
      totalUsers: data.data.totalUsers,
      totalTeachers: data.data.totalTeachers,
      totalStudents: data.data.totalStudents,
      totalCourses: data.data.totalCourses,
      totalClasses: data.data.totalClasses,
      totalHomeworks: data.data.totalHomeworks,
      totalExams: data.data.totalExams
    }
    console.log('管理员统计数据:', stats.value)
  } catch (error) {
    console.error('获取管理员统计数据失败:', error)
  }
}

// 获取教师统计数据
const fetchTeacherStats = async () => {
  try {
    console.log('获取教师统计数据...')
    const [courseRes, classRes, homeworkRes, examRes] = await Promise.all([
      getCourseList({ pageNum: 1, pageSize: 1 }),
      getClassList({ pageNum: 1, pageSize: 1 }),
      getHomeworkList({ pageNum: 1, pageSize: 1 }),
      getExamList({ pageNum: 1, pageSize: 1 })
    ])

    stats.value = {
      ...stats.value,
      courseCount: courseRes.data?.total || 0,
      classCount: classRes.data?.total || 0,
      homeworkCount: homeworkRes.data?.total || 0,
      examCount: examRes.data?.total || 0
    }

    console.log('教师统计数据:', stats.value)
  } catch (error) {
    console.error('获取教师统计数据失败:', error)
  }
}

const fetchStats = async () => {
  if (userStore.isAdmin) {
    await fetchAdminStats()
  } else {
    await fetchTeacherStats()
  }
}

onMounted(() => {
  fetchStats()
})
</script>

<style lang="scss" scoped>
.dashboard-container {
  .welcome-card {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 12px;
    padding: 32px;
    margin-bottom: 20px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    color: #fff;
    
    .welcome-title {
      font-size: 24px;
      font-weight: 600;
      margin: 0 0 8px;
    }
    
    .welcome-desc {
      font-size: 14px;
      opacity: 0.9;
      margin: 0;
    }
  }
  
  .stat-row {
    margin-bottom: 20px;
  }
  
  .stat-card {
    background: #fff;
    border-radius: 12px;
    padding: 20px;
    display: flex;
    align-items: center;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
    transition: transform 0.3s, box-shadow 0.3s;
    
    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
    }
    
    .stat-icon {
      width: 56px;
      height: 56px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 16px;
      color: #fff;
    }
    
    .stat-value {
      font-size: 28px;
      font-weight: 700;
      color: #1e293b;
    }

    .stat-label {
      font-size: 13px;
      color: #64748b;
      margin-top: 4px;
    }

    .stat-detail {
      font-size: 11px;
      color: #94a3b8;
      margin-top: 4px;
    }

    &.stat-card-red .stat-icon {
      background: linear-gradient(135deg, #ef4444, #dc2626);
    }

    &.stat-card-blue .stat-icon {
      background: linear-gradient(135deg, #3b82f6, #1d4ed8);
    }

    &.stat-card-green .stat-icon {
      background: linear-gradient(135deg, #10b981, #059669);
    }

    &.stat-card-orange .stat-icon {
      background: linear-gradient(135deg, #f59e0b, #d97706);
    }

    &.stat-card-purple .stat-icon {
      background: linear-gradient(135deg, #8b5cf6, #7c3aed);
    }
  }
  
  .panel {
    background: #fff;
    border-radius: 12px;
    padding: 20px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
    margin-bottom: 20px;
    
    .panel-header {
      margin-bottom: 20px;
      
      .panel-title {
        font-size: 16px;
        font-weight: 600;
        color: #1e293b;
        margin: 0;
      }
    }
  }
  
  .quick-actions {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 16px;
    
    .action-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 20px;
      border-radius: 12px;
      cursor: pointer;
      transition: all 0.3s;
      background: #f8fafc;
      
      &:hover {
        background: #f1f5f9;
        transform: translateY(-2px);
      }
      
      .action-icon {
        width: 56px;
        height: 56px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #fff;
        margin-bottom: 12px;
      }
      
      .action-text {
        font-size: 14px;
        color: #475569;
        font-weight: 500;
      }
    }
  }
  
  .notice-list {
    .notice-item {
      display: flex;
      align-items: flex-start;
      padding: 12px 0;
      border-bottom: 1px solid #f1f5f9;
      
      &:last-child {
        border-bottom: none;
      }
      
      .notice-icon {
        color: #f59e0b;
        margin-right: 12px;
        margin-top: 2px;
      }
      
      .notice-title {
        font-size: 14px;
        color: #334155;
        margin-bottom: 4px;
      }
      
      .notice-time {
        font-size: 12px;
        color: #94a3b8;
      }
    }
  }
}

@media (max-width: 768px) {
  .quick-actions {
    grid-template-columns: repeat(2, 1fr) !important;
  }
}
</style>
