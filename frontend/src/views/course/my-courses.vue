<template>
  <div class="my-courses-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的课程</span>
        </div>
      </template>

      <div class="course-list" v-loading="loading">
        <el-row :gutter="20">
          <el-col :xs="24" :sm="12" :lg="8" :xl="6" v-for="course in courseList" :key="course.id">
            <div class="course-card" @click="goToDetail(course.id)">
              <div class="course-cover" :style="{ background: getRandomGradient(course.id) }">
                <el-icon :size="48" color="rgba(255,255,255,0.8)"><Reading /></el-icon>
              </div>
              <div class="course-info">
                <h3 class="course-name">{{ course.courseName }}</h3>
                <p class="course-desc">{{ stripHtmlTags(course.description, 60) || '暂无描述' }}</p>
                <div class="course-meta">
                  <span class="meta-item">
                    <el-icon><User /></el-icon>
                    {{ course.teacherName || '未知教师' }}
                  </span>
                  <span class="meta-item">
                    <el-icon><Star /></el-icon>
                    {{ course.credit || 0 }} 学分
                  </span>
                </div>
              </div>
              <div class="course-actions" @click.stop>
                <el-button type="primary" text size="small" @click="goToDetail(course.id)">
                  查看详情
                </el-button>
              </div>
            </div>
          </el-col>
        </el-row>

        <el-empty v-if="!loading && courseList.length === 0" description="暂无课程数据">
          <template #image>
            <el-icon :size="60" color="#909399"><Reading /></el-icon>
          </template>
          <div style="color: #909399; margin-top: 8px">您还没有加入任何课程</div>
        </el-empty>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import { stripHtmlTags } from '@/utils/format'

const router = useRouter()
const loading = ref(false)
const courseList = ref([])

const gradients = [
  'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
  'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
  'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
  'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
  'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
  'linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%)',
  'linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%)',
  'linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%)'
]

const getRandomGradient = (id) => {
  return gradients[id % gradients.length]
}

const fetchMyCourses = async () => {
  loading.value = true
  try {
    const res = await request({ url: '/courses/student/my', method: 'get' })
    courseList.value = res.data || []
  } catch (error) {
    console.error('获取我的课程失败:', error)
  } finally {
    loading.value = false
  }
}

const goToDetail = (id) => {
  router.push(`/courses/${id}`)
}

onMounted(() => {
  fetchMyCourses()
})
</script>

<style lang="scss" scoped>
.my-courses-container {
  .card-header {
    font-size: 18px;
    font-weight: 500;
  }

  .course-list {
    min-height: 400px;
  }

  .course-card {
    background: #fff;
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
    margin-bottom: 20px;
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
    }

    .course-cover {
      height: 120px;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .course-info {
      padding: 16px;

      .course-name {
        font-size: 16px;
        font-weight: 600;
        color: #1e293b;
        margin: 0 0 8px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .course-desc {
        font-size: 13px;
        color: #64748b;
        margin: 0 0 12px;
        height: 40px;
        overflow: hidden;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
      }

      .course-meta {
        display: flex;
        gap: 16px;

        .meta-item {
          display: flex;
          align-items: center;
          gap: 4px;
          font-size: 12px;
          color: #94a3b8;
        }
      }
    }

    .course-actions {
      padding: 12px 16px;
      border-top: 1px solid #f1f5f9;
      display: flex;
      justify-content: flex-end;
      gap: 8px;
    }
  }
}
</style>
