<template>
  <div class="offering-detail-container" v-loading="loading">
    <template v-if="offering">
      <!-- 基本信息卡片 -->
      <el-card class="info-card" shadow="never">
        <template #header>
          <div class="card-header">
            <div>
              <h2>{{ offering.courseName }}</h2>
              <el-tag :type="offering.courseType === 1 ? 'warning' : 'success'" size="large" style="margin-left: 12px">
                {{ offering.courseType === 1 ? '选修课' : '必修课' }}
              </el-tag>
              <el-tag :type="getStatusType(offering.status)" size="large" style="margin-left: 8px">
                {{ getStatusText(offering.status) }}
              </el-tag>
            </div>
            <div class="header-actions">
              <el-button type="primary" @click="goBack">
                <el-icon><Back /></el-icon>返回
              </el-button>
              <el-button type="primary" v-if="offering.canEdit" @click="handleEdit">
                <el-icon><Edit /></el-icon>编辑
              </el-button>
            </div>
          </div>
        </template>

        <el-descriptions :column="3" border>
          <el-descriptions-item label="课程编码">
            {{ offering.courseCode }}
          </el-descriptions-item>
          <el-descriptions-item label="学分">
            {{ offering.credit }}
          </el-descriptions-item>
          <el-descriptions-item label="学期">
            {{ offering.semesterName }}（{{ offering.academicYear }}）
          </el-descriptions-item>
          <el-descriptions-item label="授课教师">
            {{ offering.teacherName }}
          </el-descriptions-item>
          <el-descriptions-item label="选课人数" v-if="offering.courseType === 1">
            {{ offering.currentStudents || 0 }}/{{ offering.maxStudents === 0 ? '不限' : offering.maxStudents }}
          </el-descriptions-item>
          <el-descriptions-item label="关联班级数" v-else>
            {{ offering.classCount || 0 }} 个
          </el-descriptions-item>
          <el-descriptions-item label="上课地点" :span="2">
            {{ offering.classLocation || '未设置' }}
          </el-descriptions-item>
          <el-descriptions-item label="上课时间" :span="3">
            {{ offering.classSchedule || '未设置' }}
          </el-descriptions-item>
          <el-descriptions-item label="备注" :span="3">
            {{ offering.notes || '无' }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="3">
            {{ offering.createTime ? offering.createTime.substring(0, 16).replace('T', ' ') : '-' }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 关联班级列表 -->
      <el-card class="info-card" shadow="never" v-if="offering.courseType === 0">
        <template #header>
          <div class="card-header">
            <span>关联班级</span>
            <el-button type="primary" size="small" @click="handleManageClasses">
              <el-icon><Setting /></el-icon>管理班级
            </el-button>
          </div>
        </template>
        <div class="class-list">
          <el-tag v-for="cls in classList" :key="cls.id" size="large" style="margin: 4px">
            {{ cls.className }}
          </el-tag>
          <el-empty v-if="classList.length === 0" description="暂无关联班级" />
        </div>
      </el-card>

      <!-- 作业列表 -->
      <el-card class="info-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span>作业列表</span>
            <el-button type="primary" size="small" @click="handleCreateHomework">
              <el-icon><Plus /></el-icon>布置作业
            </el-button>
          </div>
        </template>
        <el-table :data="homeworkList" stripe>
          <el-table-column prop="title" label="作业标题" min-width="200" />
          <el-table-column prop="deadline" label="截止时间" width="180" />
          <el-table-column label="提交情况" width="150">
            <template #default="{ row }">
              {{ row.submittedCount || 0 }}/{{ row.totalCount || 0 }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="goToHomework(row.id)">
                查看详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="homeworkList.length === 0" description="暂无作业" />
      </el-card>

      <!-- 考试列表 -->
      <el-card class="info-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span>考试列表</span>
            <el-button type="primary" size="small" @click="handleCreateExam">
              <el-icon><Plus /></el-icon>创建考试
            </el-button>
          </div>
        </template>
        <el-table :data="examList" stripe>
          <el-table-column prop="title" label="考试标题" min-width="200" />
          <el-table-column prop="startTime" label="开始时间" width="180" />
          <el-table-column prop="duration" label="考试时长" width="120">
            <template #default="{ row }">
              {{ row.duration }} 分钟
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="goToExam(row.id)">
                查看详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="examList.length === 0" description="暂无考试" />
      </el-card>

      <!-- 实验列表 -->
      <el-card class="info-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span>实验列表</span>
            <el-button type="primary" size="small" @click="handleCreateExperiment">
              <el-icon><Plus /></el-icon>创建实验
            </el-button>
          </div>
        </template>
        <el-table :data="experimentList" stripe>
          <el-table-column prop="title" label="实验标题" min-width="200" />
          <el-table-column prop="deadline" label="截止时间" width="180" />
          <el-table-column label="提交情况" width="150">
            <template #default="{ row }">
              {{ row.submittedCount || 0 }}/{{ row.totalCount || 0 }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="goToExperiment(row.id)">
                查看详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="experimentList.length === 0" description="暂无实验" />
      </el-card>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getOfferingDetail } from '@/api/courseOffering'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const offering = ref(null)
const classList = ref([])
const homeworkList = ref([])
const examList = ref([])
const experimentList = ref([])

const getStatusType = (status) => {
  return status === 0 ? 'info' : status === 1 ? 'success' : 'warning'
}

const getStatusText = (status) => {
  return status === 0 ? '未开课' : status === 1 ? '进行中' : '已结课'
}

const fetchOfferingDetail = async () => {
  loading.value = true
  try {
    const res = await getOfferingDetail(route.params.id)
    offering.value = res.data

    // TODO: 获取关联的班级、作业、考试、实验列表
    // 这些需要根据 offering_id 来查询，需要修改后端API
  } catch (error) {
    console.error('获取开课详情失败:', error)
    ElMessage.error('获取开课详情失败')
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.back()
}

const handleEdit = () => {
  // TODO: 实现编辑功能
  ElMessage.info('编辑功能开发中')
}

const handleManageClasses = () => {
  // 跳转到班级管理页面
  router.push({ name: 'Classes' })
}

const handleCreateHomework = () => {
  router.push({ name: 'Homeworks' })
}

const handleCreateExam = () => {
  router.push({ name: 'Exams' })
}

const handleCreateExperiment = () => {
  router.push({ name: 'ExperimentCreate' })
}

const goToHomework = (id) => {
  router.push({ name: 'HomeworkDetail', params: { id } })
}

const goToExam = (id) => {
  router.push({ name: 'ExamDetail', params: { id } })
}

const goToExperiment = (id) => {
  router.push({ name: 'ExperimentDetail', params: { id } })
}

onMounted(() => {
  fetchOfferingDetail()
})
</script>

<style lang="scss" scoped>
.offering-detail-container {
  .info-card {
    margin-bottom: 20px;
    border-radius: 12px;

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      h2 {
        margin: 0;
        display: inline-block;
      }

      .header-actions {
        display: flex;
        gap: 8px;
      }
    }

    .class-list {
      padding: 12px;
    }
  }
}
</style>
