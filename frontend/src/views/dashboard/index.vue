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
      <el-col :xs="12" :sm="6">
        <div class="stat-card stat-card-blue">
          <div class="stat-icon">
            <el-icon :size="28"><Reading /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.courseCount }}</div>
            <div class="stat-label">课程数量</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card stat-card-green">
          <div class="stat-icon">
            <el-icon :size="28"><School /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.classCount }}</div>
            <div class="stat-label">班级数量</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card stat-card-orange">
          <div class="stat-icon">
            <el-icon :size="28"><EditPen /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.homeworkCount }}</div>
            <div class="stat-label">作业数量</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card stat-card-purple">
          <div class="stat-icon">
            <el-icon :size="28"><Document /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.examCount }}</div>
            <div class="stat-label">考试数量</div>
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

const userStore = useUserStore()

const stats = ref({
  courseCount: 0,
  classCount: 0,
  homeworkCount: 0,
  examCount: 0
})

const notices = ref([
  { title: '欢迎使用课程智慧教学平台', time: '2024-01-01' },
  { title: '系统已升级至最新版本', time: '2024-01-01' }
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

const fetchStats = async () => {
  try {
    console.log('开始获取首页统计数据...')
    const [courseRes, classRes, homeworkRes, examRes] = await Promise.all([
      getCourseList({ pageNum: 1, pageSize: 1 }),
      getClassList({ pageNum: 1, pageSize: 1 }),
      getHomeworkList({ pageNum: 1, pageSize: 1 }),
      getExamList({ pageNum: 1, pageSize: 1 })
    ])

    console.log('课程API响应:', courseRes)
    console.log('课程data详情:', courseRes.data)
    console.log('班级API响应:', classRes)
    console.log('班级data详情:', classRes.data)
    console.log('作业API响应:', homeworkRes)
    console.log('作业data详情:', homeworkRes.data)
    console.log('考试API响应:', examRes)
    console.log('考试data详情:', examRes.data)

    stats.value = {
      courseCount: courseRes.data?.total || 0,
      classCount: classRes.data?.total || 0,
      homeworkCount: homeworkRes.data?.total || 0,
      examCount: examRes.data?.total || 0
    }

    console.log('统计数据结果:', stats.value)
  } catch (error) {
    console.error('获取统计数据失败:', error)
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
