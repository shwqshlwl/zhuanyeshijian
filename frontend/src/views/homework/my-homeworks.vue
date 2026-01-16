<template>
  <div class="my-homeworks-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的作业</span>
        </div>
      </template>

      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="进行中" name="ongoing">
          <el-table :data="ongoingList" v-loading="loading" stripe>
            <el-table-column prop="homeworkTitle" label="作业标题" min-width="200">
              <template #default="{ row }">
                <el-link type="primary" @click="goToDetail(row.id)">{{ row.homeworkTitle }}</el-link>
              </template>
            </el-table-column>
            <el-table-column prop="courseName" label="课程" width="150" />
            <el-table-column prop="deadline" label="截止时间" width="180" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.submitted ? 'success' : 'warning'" size="small">
                  {{ row.submitted ? '已提交' : '未提交' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="goToDetail(row.id)">
                  {{ row.submitted ? '查看详情' : '去提交' }}
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!loading && ongoingList.length === 0" description="暂无进行中的作业" />
        </el-tab-pane>

        <el-tab-pane label="已完成" name="completed">
          <el-table :data="completedList" v-loading="loading" stripe>
            <el-table-column prop="homeworkTitle" label="作业标题" min-width="200">
              <template #default="{ row }">
                <el-link type="primary" @click="goToDetail(row.id)">{{ row.homeworkTitle }}</el-link>
              </template>
            </el-table-column>
            <el-table-column prop="courseName" label="课程" width="150" />
            <el-table-column prop="submitTime" label="提交时间" width="180" />
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
          <el-empty v-if="!loading && completedList.length === 0" description="暂无已完成的作业" />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'

const router = useRouter()
const loading = ref(false)
const activeTab = ref('ongoing')
const ongoingList = ref([])
const completedList = ref([])

const fetchMyHomeworks = async () => {
  loading.value = true
  try {
    // 获取学生的作业列表（根据后端API调整）
    const res = await request({ url: '/homeworks/student/my', method: 'get' })
    const homeworks = res.data || []

    // 分类：进行中（未提交或已提交但未批改）和已完成（已批改）
    const now = new Date()
    ongoingList.value = homeworks.filter(hw => {
      const deadline = new Date(hw.deadline)
      return deadline >= now && !hw.score
    })
    completedList.value = homeworks.filter(hw => {
      const deadline = new Date(hw.deadline)
      return deadline < now || hw.score !== null
    })
  } catch (error) {
    console.error('获取我的作业失败:', error)
  } finally {
    loading.value = false
  }
}

const handleTabChange = () => {
  // Tab切换时可以重新加载数据
}

const goToDetail = (id) => {
  router.push(`/homeworks/${id}`)
}

onMounted(() => {
  fetchMyHomeworks()
})
</script>

<style lang="scss" scoped>
.my-homeworks-container {
  .card-header {
    font-size: 18px;
    font-weight: 500;
  }
}
</style>
