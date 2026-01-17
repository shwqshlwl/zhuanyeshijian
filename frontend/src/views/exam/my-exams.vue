<template>
  <div class="my-exams-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的考试</span>
        </div>
      </template>

      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="待参加" name="upcoming">
          <el-table :data="upcomingList" v-loading="loading" stripe>
            <el-table-column prop="examName" label="考试名称" min-width="200" />
            <el-table-column prop="courseName" label="课程" width="150" />
            <el-table-column prop="startTime" label="开始时间" width="180" />
            <el-table-column prop="duration" label="时长(分钟)" width="100" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row)" size="small">
                  {{ getStatusText(row) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button
                  v-if="canTakeExam(row)"
                  type="primary"
                  link
                  size="small"
                  @click="goToTakeExam(row.id)"
                >
                  开始考试
                </el-button>
                <el-button v-else type="info" link size="small" disabled>
                  未开始
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!loading && upcomingList.length === 0" description="暂无待参加的考试" />
        </el-tab-pane>

        <el-tab-pane label="已完成" name="completed">
          <el-table :data="completedList" v-loading="loading" stripe>
            <el-table-column prop="examName" label="考试名称" min-width="200">
              <template #default="{ row }">
                <el-link type="primary" @click="goToDetail(row.id)">{{ row.examName }}</el-link>
              </template>
            </el-table-column>
            <el-table-column prop="courseName" label="课程" width="150" />
            <el-table-column prop="submitTime" label="完成时间" width="180" />
            <el-table-column prop="score" label="成绩" width="100">
              <template #default="{ row }">
                <span v-if="row.score !== null">{{ row.score }} 分</span>
                <el-tag v-else type="info" size="small">待批改</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="goToDetail(row.id)">
                  查看详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!loading && completedList.length === 0" description="暂无已完成的考试" />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getStudentExams } from '@/api/exam'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const activeTab = ref('upcoming')
const upcomingList = ref([])
const completedList = ref([])

const fetchMyExams = async () => {
  loading.value = true
  try {
    const res = await getStudentExams()
    const exams = res.data || []

    // 分类：待参加和已完成
    const now = new Date()
    upcomingList.value = exams.filter(exam => {
      const endTime = new Date(exam.endTime)
      return !exam.submitted && endTime >= now
    })
    completedList.value = exams.filter(exam => {
      return exam.submitted
    })
  } catch (error) {
    console.error('获取我的考试失败:', error)
    ElMessage.error('获取我的考试失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const getStatusType = (exam) => {
  const now = new Date()
  const startTime = new Date(exam.startTime)
  const endTime = new Date(exam.endTime)

  if (now < startTime) return 'info'
  if (now >= startTime && now <= endTime) return 'success'
  return 'danger'
}

const getStatusText = (exam) => {
  const now = new Date()
  const startTime = new Date(exam.startTime)
  const endTime = new Date(exam.endTime)

  if (now < startTime) return '未开始'
  if (now >= startTime && now <= endTime) return '进行中'
  return '已结束'
}

const canTakeExam = (exam) => {
  const now = new Date()
  const startTime = new Date(exam.startTime)
  const endTime = new Date(exam.endTime)
  return now >= startTime && now <= endTime
}

const handleTabChange = () => {
  // Tab切换时重新加载数据
  fetchMyExams()
}

const goToTakeExam = (id) => {
  router.push(`/exams/${id}/take`)
}

const goToDetail = (id) => {
  router.push(`/exams/${id}`)
}

onMounted(() => {
  fetchMyExams()
})
</script>

<style lang="scss" scoped>
.my-exams-container {
  .card-header {
    font-size: 18px;
    font-weight: 500;
  }
}
</style>
