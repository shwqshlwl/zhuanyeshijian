<template>
  <div class="course-status-container">
    <el-card class="filter-card">
      <el-form inline>
        <el-form-item label="课程状态">
          <el-select v-model="filterStatus" placeholder="全部状态" clearable style="width: 150px" @change="handleFilter">
            <el-option label="未开课" :value="0" />
            <el-option label="进行中" :value="1" />
            <el-option label="已结束" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="搜索">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索课程名称"
            clearable
            style="width: 250px"
            @keyup.enter="handleFilter"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleFilter">
            <el-icon><Search /></el-icon>搜索
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>课程状态管理</span>
          <div class="status-stats">
            <el-tag type="info" size="small">未开课: {{ stats.notStarted }}</el-tag>
            <el-tag type="success" size="small">进行中: {{ stats.ongoing }}</el-tag>
            <el-tag type="danger" size="small">已结束: {{ stats.finished }}</el-tag>
          </div>
        </div>
      </template>

      <el-table :data="courseList" stripe v-loading="loading">
        <el-table-column prop="courseName" label="课程名称" min-width="200" />
        <el-table-column prop="courseCode" label="课程编码" width="150" />
        <el-table-column prop="teacherName" label="授课教师" width="120" />
        <el-table-column prop="credit" label="学分" width="80" />
        <el-table-column label="当前状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="更新状态" width="180">
          <template #default="{ row }">
            <el-select
              v-model="row.status"
              size="small"
              @change="handleStatusChange(row)"
              style="width: 130px"
            >
              <el-option label="未开课" :value="0" />
              <el-option label="进行中" :value="1" />
              <el-option label="已结束" :value="2" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="更新时间" width="180">
          <template #default="{ row }">
            {{ row.updateTime ? row.updateTime.substring(0, 16).replace('T', ' ') : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="goToCourseDetail(row.id)">
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container" v-if="total > 0">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchCourseList"
          @current-change="fetchCourseList"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getCourseList, updateCourseStatus } from '@/api/course'
import { ElMessage } from 'element-plus'

const router = useRouter()

const loading = ref(false)
const courseList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(20)
const filterStatus = ref(null)
const searchKeyword = ref('')

// 统计各状态课程数量
const stats = reactive({
  notStarted: 0,
  ongoing: 0,
  finished: 0
})

const getStatusType = (status) => {
  return status === 0 ? 'info' : status === 1 ? 'success' : 'danger'
}

const getStatusText = (status) => {
  return status === 0 ? '未开课' : status === 1 ? '进行中' : '已结束'
}

// 获取课程列表
const fetchCourseList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }

    if (filterStatus.value !== null && filterStatus.value !== undefined) {
      params.status = filterStatus.value
    }

    if (searchKeyword.value) {
      params.keyword = searchKeyword.value
    }

    const res = await getCourseList(params)
    courseList.value = res.data?.records || []
    total.value = res.data?.total || 0

    // 更新统计数据
    await updateStats()
  } catch (error) {
    console.error('获取课程列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 更新统计数据
const updateStats = async () => {
  try {
    // 获取各状态的课程数量
    const [notStartedRes, ongoingRes, finishedRes] = await Promise.all([
      getCourseList({ pageNum: 1, pageSize: 1, status: 0 }),
      getCourseList({ pageNum: 1, pageSize: 1, status: 1 }),
      getCourseList({ pageNum: 1, pageSize: 1, status: 2 })
    ])

    stats.notStarted = notStartedRes.data?.total || 0
    stats.ongoing = ongoingRes.data?.total || 0
    stats.finished = finishedRes.data?.total || 0
  } catch (error) {
    console.error('更新统计数据失败:', error)
  }
}

// 筛选
const handleFilter = () => {
  pageNum.value = 1
  fetchCourseList()
}

// 状态变更
const handleStatusChange = async (row) => {
  try {
    await updateCourseStatus(row.id, row.status)
    ElMessage.success('状态更新成功')
    await updateStats()
  } catch (error) {
    console.error('状态更新失败:', error)
    // 失败后恢复原状态
    fetchCourseList()
  }
}

// 跳转课程详情
const goToCourseDetail = (id) => {
  router.push(`/courses/${id}`)
}

onMounted(() => {
  fetchCourseList()
})
</script>

<style lang="scss" scoped>
.course-status-container {
  .filter-card {
    margin-bottom: 20px;
  }

  .table-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .status-stats {
        display: flex;
        gap: 12px;
      }
    }
  }

  .pagination-container {
    margin-top: 16px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
