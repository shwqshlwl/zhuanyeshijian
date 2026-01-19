<template>
  <div class="elective-container">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索课程名称或编码"
        clearable
        style="width: 300px"
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-button type="primary" @click="handleSearch">
        <el-icon><Search /></el-icon>搜索
      </el-button>
    </div>

    <!-- 选课说明 -->
    <el-alert
      title="选课说明"
      type="info"
      :closable="false"
      style="margin-bottom: 20px"
    >
      <template #default>
        <div>1. 只能在选课时间内进行选课和退课</div>
        <div>2. 课程满员后无法再选</div>
        <div>3. 已选课程可以在选课时间内退课</div>
      </template>
    </el-alert>

    <!-- 课程列表 -->
    <div class="course-list" v-loading="loading">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :lg="8" :xl="6" v-for="course in courseList" :key="course.id">
          <div class="course-card">
            <div class="course-cover" :style="{ background: getRandomGradient(course.id) }">
              <el-icon :size="48" color="rgba(255,255,255,0.8)"><Reading /></el-icon>
            </div>
            <div class="course-info">
              <h3 class="course-name">{{ course.courseName }}</h3>
              <p class="course-code">课程编码：{{ course.courseCode }}</p>
              <p class="course-teacher">授课教师：{{ course.teacherName || '未知' }}</p>
              <div class="course-meta">
                <el-tag size="small" type="warning">
                  {{ course.credit || 0 }} 学分
                </el-tag>
                <el-tag size="small" type="info">
                  {{ course.totalHours || 0 }} 学时
                </el-tag>
              </div>

              <!-- 选课人数信息 -->
              <div class="selection-info">
                <el-progress
                  :percentage="getSelectionPercentage(course)"
                  :status="course.currentStudents >= course.maxStudents ? 'exception' : ''"
                  :format="() => `${course.currentStudents}/${course.maxStudents || '不限'}`"
                />
              </div>

              <!-- 选课时间 -->
              <div class="time-info">
                <div class="time-item">
                  <el-icon><Clock /></el-icon>
                  <span>开始：{{ formatTime(course.selectionStartTime) }}</span>
                </div>
                <div class="time-item">
                  <el-icon><Clock /></el-icon>
                  <span>结束：{{ formatTime(course.selectionEndTime) }}</span>
                </div>
              </div>
            </div>

            <!-- 操作按钮 -->
            <div class="course-actions">
              <el-button
                v-if="course.isSelected"
                type="warning"
                :disabled="!course.canDrop"
                @click="handleDrop(course)"
              >
                退课
              </el-button>
              <el-button
                v-else
                type="primary"
                :disabled="!course.canSelect"
                @click="handleSelect(course)"
              >
                选课
              </el-button>
              <el-tag v-if="course.isSelected" type="success" size="small">已选</el-tag>
              <el-tooltip v-if="course.unavailableReason" :content="course.unavailableReason" placement="top">
                <el-tag type="info" size="small">{{ course.unavailableReason }}</el-tag>
              </el-tooltip>
            </div>
          </div>
        </el-col>
      </el-row>

      <el-empty v-if="!loading && courseList.length === 0" description="暂无选修课" />
    </div>

    <!-- 分页 -->
    <div class="pagination-container" v-if="total > 0">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :page-sizes="[8, 12, 16, 24]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchCourseList"
        @current-change="fetchCourseList"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getElectiveCourseList, selectCourse, dropCourse } from '@/api/elective'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const courseList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(8)
const searchKeyword = ref('')

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

const getSelectionPercentage = (course) => {
  if (!course.maxStudents || course.maxStudents === 0) {
    return 0
  }
  return Math.round((course.currentStudents / course.maxStudents) * 100)
}

const formatTime = (time) => {
  if (!time) return '未设置'
  return time.substring(0, 16).replace('T', ' ')
}

const fetchCourseList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: searchKeyword.value
    }

    const res = await getElectiveCourseList(params)
    courseList.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('获取选课列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pageNum.value = 1
  fetchCourseList()
}

const handleSelect = (course) => {
  ElMessageBox.confirm(
    `确定要选择课程"${course.courseName}"吗？`,
    '选课确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    }
  ).then(async () => {
    try {
      await selectCourse(course.id)
      ElMessage.success('选课成功')
      fetchCourseList()
    } catch (error) {
      console.error('选课失败:', error)
    }
  })
}

const handleDrop = (course) => {
  ElMessageBox.confirm(
    `确定要退选课程"${course.courseName}"吗？`,
    '退课确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await dropCourse(course.id)
      ElMessage.success('退课成功')
      fetchCourseList()
    } catch (error) {
      console.error('退课失败:', error)
    }
  })
}

onMounted(() => {
  fetchCourseList()
})
</script>

<style lang="scss" scoped>
.elective-container {
  .search-bar {
    display: flex;
    gap: 12px;
    margin-bottom: 20px;
    padding: 20px;
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
    align-items: center;
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

      .course-code,
      .course-teacher {
        font-size: 13px;
        color: #64748b;
        margin: 4px 0;
      }

      .course-meta {
        display: flex;
        gap: 8px;
        margin: 12px 0;
      }

      .selection-info {
        margin: 12px 0;
      }

      .time-info {
        margin-top: 12px;

        .time-item {
          display: flex;
          align-items: center;
          gap: 4px;
          font-size: 12px;
          color: #64748b;
          margin: 4px 0;
        }
      }
    }

    .course-actions {
      padding: 12px 16px;
      border-top: 1px solid #f1f5f9;
      display: flex;
      align-items: center;
      gap: 8px;
    }
  }

  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
