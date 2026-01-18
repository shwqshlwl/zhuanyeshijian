<template>
  <div class="experiment-container">
    <div class="search-bar">
      <el-select v-model="searchForm.courseId" placeholder="选择课程" clearable style="width: 200px">
        <el-option v-for="c in courseOptions" :key="c.id" :label="c.name" :value="c.id" />
      </el-select>
      <el-button type="primary" @click="handleSearch"><el-icon><Search /></el-icon>搜索</el-button>
      <el-button type="primary" @click="$router.push('/experiments/create')" v-if="userStore.isTeacher">
        <el-icon><Plus /></el-icon>创建实验
      </el-button>
    </div>

    <el-table :data="experimentList" v-loading="loading" stripe>
      <el-table-column prop="experimentName" label="实验名称" min-width="200" />
      <el-table-column prop="courseName" label="所属课程" width="150" />
      <el-table-column prop="endTime" label="截止时间" width="180" />
      <el-table-column prop="language" label="编程语言" width="100" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="$router.push(`/experiments/${row.id}`)">详情</el-button>
          <el-button type="success" link @click="$router.push(`/experiments/${row.id}/answer`)" v-if="userStore.isStudent">开始答题</el-button>
          <template v-if="userStore.isTeacher">
            <el-button type="primary" link @click="$router.push(`/experiments/${row.id}/edit`)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container">
      <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
        layout="total, sizes, prev, pager, next" @change="fetchList" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getExperimentList, createExperiment, updateExperiment, deleteExperiment } from '@/api/experiment'
import { getCourseList } from '@/api/course'
import { ElMessage, ElMessageBox } from 'element-plus'

const userStore = useUserStore()
const loading = ref(false)
const experimentList = ref([])
const courseOptions = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const searchForm = reactive({ courseId: '' })

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getExperimentList({ pageNum: pageNum.value, pageSize: pageSize.value, ...searchForm })
    experimentList.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const fetchCourses = async () => {
  const res = await getCourseList({ pageNum: 1, pageSize: 100 })
  const records = res.data?.records || []
  courseOptions.value = records.map(c => ({ id: c.id, name: c.courseName || c.name }))
}

const handleSearch = () => { pageNum.value = 1; fetchList() }

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除实验"${row.experimentName}"吗？`, '提示', { type: 'warning' }).then(async () => {
    await deleteExperiment(row.id)
    ElMessage.success('删除成功')
    fetchList()
  })
}

onMounted(() => { fetchList(); fetchCourses() })
</script>

<style lang="scss" scoped>
.experiment-container {
  .search-bar {
    display: flex; gap: 12px; margin-bottom: 20px; padding: 20px; background: #fff; border-radius: 12px;
  }
}
</style>
