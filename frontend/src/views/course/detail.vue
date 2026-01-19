<template>
  <div class="course-detail">
    <el-page-header @back="$router.back()">
      <template #content>
        <span>{{ course.courseName || '课程详情' }}</span>
        <el-tag :type="statusType" style="margin-left: 12px">{{ statusText }}</el-tag>
      </template>
    </el-page-header>

    <div class="detail-content" v-loading="loading">
      <!-- 基本信息 -->
      <el-card class="info-card">
        <template #header>
          <div class="card-header">
            <span>基本信息</span>
            <el-button type="primary" size="small" @click="handleEdit" v-if="userStore.isTeacher">
              <el-icon><Edit /></el-icon>编辑
            </el-button>
          </div>
        </template>
        <el-descriptions :column="3" border>
          <el-descriptions-item label="课程名称">{{ course.courseName }}</el-descriptions-item>
          <el-descriptions-item label="课程编码">{{ course.courseCode }}</el-descriptions-item>
          <el-descriptions-item label="授课教师">{{ course.teacherName }}</el-descriptions-item>
          <el-descriptions-item label="学分">{{ course.credit }}</el-descriptions-item>
          <el-descriptions-item label="总学时">{{ course.totalHours || '-' }}</el-descriptions-item>
          <el-descriptions-item label="学期">{{ course.semester || '-' }}</el-descriptions-item>
          <el-descriptions-item label="课程类型">
            <el-tag :type="course.courseType === 1 ? 'warning' : 'success'" size="small">
              {{ course.courseType === 1 ? '选修课' : '必修课' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="选课人数" v-if="course.courseType === 1">
            {{ course.currentStudents || 0 }} / {{ course.maxStudents === 0 ? '不限' : course.maxStudents }}
          </el-descriptions-item>
          <el-descriptions-item label="选课时间" v-if="course.courseType === 1" :span="course.courseType === 1 ? 1 : 0">
            <el-tag :type="selectionTimeStatus.type" size="small">
              {{ selectionTimeStatus.text }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="课程描述" :span="3">
            <div v-if="course.description" v-html="course.description" class="course-description"></div>
            <span v-else>暂无描述</span>
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 课程大纲 -->
      <el-card class="syllabus-card">
        <template #header>
          <div class="card-header">
            <span>课程大纲</span>
            <el-button type="primary" size="small" @click="showSyllabusDialog = true" v-if="userStore.isTeacher">
              <el-icon><Edit /></el-icon>编辑大纲
            </el-button>
          </div>
        </template>
        <div class="syllabus-content" v-if="course.syllabus" v-html="course.syllabus"></div>
        <el-empty v-else description="暂无课程大纲" />
      </el-card>

      <!-- 关联班级（仅必修课） -->
      <el-card class="class-card" v-if="course.courseType === 0">
        <template #header>
          <div class="card-header">
            <span>关联班级</span>
          </div>
        </template>
        <el-table :data="relatedClasses" stripe>
          <el-table-column prop="name" label="班级名称">
            <template #default="{ row }">
              <el-link type="primary" @click="$router.push(`/classes/${row.id}`)">{{ row.name }}</el-link>
            </template>
          </el-table-column>
          <el-table-column prop="grade" label="年级" width="100" />
          <el-table-column prop="major" label="专业" />
          <el-table-column prop="studentCount" label="学生人数" width="100" />
        </el-table>
        <el-empty v-if="relatedClasses.length === 0" description="暂无关联班级" />
      </el-card>

      <!-- 选课学生（仅选修课） -->
      <el-card class="students-card" v-if="course.courseType === 1">
        <template #header>
          <div class="card-header">
            <span>选课学生</span>
            <div>
              <el-tag type="info" size="small">已选人数: {{ courseStudents.length }}</el-tag>
              <el-tag type="warning" size="small" style="margin-left: 8px">
                容量: {{ course.maxStudents === 0 ? '不限' : course.maxStudents }}
              </el-tag>
            </div>
          </div>
        </template>
        <el-table :data="courseStudents" stripe v-loading="studentsLoading">
          <el-table-column type="index" label="序号" width="60" />
          <el-table-column prop="studentNumber" label="学号" width="150" />
          <el-table-column prop="studentName" label="姓名" width="120" />
          <el-table-column prop="selectionTime" label="选课时间" width="180">
            <template #default="{ row }">
              {{ row.selectionTime ? row.selectionTime.replace('T', ' ').substring(0, 19) : '-' }}
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
                {{ row.status === 1 ? '正常' : '已退课' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" v-if="userStore.isTeacher">
            <template #default="{ row }">
              <el-button
                type="danger"
                link
                size="small"
                @click="handleRemoveStudent(row)"
                :disabled="row.status === 0"
              >
                移除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!studentsLoading && courseStudents.length === 0" description="暂无选课学生" />
      </el-card>

      <!-- 历史作业 -->
      <el-card class="homework-card">
        <template #header>
          <div class="card-header">
            <span>历史作业</span>
            <el-button type="primary" size="small" @click="$router.push('/homeworks')" v-if="userStore.isTeacher">
              查看全部
            </el-button>
          </div>
        </template>
        <el-table :data="homeworkList" stripe>
          <el-table-column prop="title" label="作业标题">
            <template #default="{ row }">
              <el-link type="primary" @click="$router.push(`/homeworks/${row.id}`)">{{ row.title }}</el-link>
            </template>
          </el-table-column>
          <el-table-column prop="deadline" label="截止时间" width="180" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 0 ? 'info' : row.status === 1 ? 'success' : 'danger'" size="small">
                {{ row.status === 0 ? '未开始' : row.status === 1 ? '进行中' : '已结束' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="提交情况" width="120">
            <template #default="{ row }">
              {{ row.submitCount || 0 }} / {{ row.totalCount || 0 }}
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="homeworkList.length === 0" description="暂无作业记录" />
      </el-card>

      <!-- 历史考试 -->
      <el-card class="exam-card">
        <template #header>
          <div class="card-header">
            <span>历史考试</span>
            <el-button type="primary" size="small" @click="$router.push('/exams')" v-if="userStore.isTeacher">
              查看全部
            </el-button>
          </div>
        </template>
        <el-table :data="examList" stripe>
          <el-table-column prop="title" label="考试名称">
            <template #default="{ row }">
              <el-link type="primary" @click="$router.push(`/exams/${row.id}`)">{{ row.title }}</el-link>
            </template>
          </el-table-column>
          <el-table-column prop="startTime" label="开始时间" width="180" />
          <el-table-column prop="duration" label="时长(分钟)" width="100" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 0 ? 'info' : row.status === 1 ? 'success' : 'danger'" size="small">
                {{ row.status === 0 ? '未开始' : row.status === 1 ? '进行中' : '已结束' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="平均分" width="100">
            <template #default="{ row }">
              {{ row.avgScore ? row.avgScore.toFixed(1) : '-' }}
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="examList.length === 0" description="暂无考试记录" />
      </el-card>
    </div>

    <!-- 编辑课程对话框 -->
    <el-dialog v-model="showEditDialog" title="编辑课程" width="900px">
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="80px">
        <el-form-item label="课程名称" prop="courseName">
          <el-input v-model="editForm.courseName" placeholder="请输入课程名称" />
        </el-form-item>
        <el-form-item label="课程编码" prop="courseCode">
          <el-input v-model="editForm.courseCode" placeholder="请输入课程编码" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="学分" prop="credit">
              <el-input-number v-model="editForm.credit" :min="0" :max="10" :precision="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="总学时">
              <el-input-number v-model="editForm.totalHours" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="学期">
              <el-input v-model="editForm.semester" placeholder="如：2024春" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="课程状态">
          <el-radio-group v-model="editForm.status">
            <el-radio :label="0">未开课</el-radio>
            <el-radio :label="1">进行中</el-radio>
            <el-radio :label="2">已结束</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="课程描述">
          <WangEditor v-model="editForm.description" placeholder="请输入课程描述" height="400px" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" :loading="editLoading" @click="handleSaveEdit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 编辑大纲对话框 -->
    <el-dialog v-model="showSyllabusDialog" title="编辑课程大纲" width="900px">
      <WangEditor v-model="syllabusContent" placeholder="请输入课程大纲内容" height="500px" />
      <template #footer>
        <el-button @click="showSyllabusDialog = false">取消</el-button>
        <el-button type="primary" :loading="syllabusLoading" @click="handleSaveSyllabus">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getCourseById, updateCourse, getCourseStudents, removeStudentFromCourse } from '@/api/course'
import { getHomeworkList } from '@/api/homework'
import { getExamList } from '@/api/exam'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const userStore = useUserStore()
const courseId = route.params.id

const loading = ref(false)
const course = ref({})
const relatedClasses = ref([])
const homeworkList = ref([])
const examList = ref([])
const courseStudents = ref([])
const studentsLoading = ref(false)

// 状态计算
const statusType = computed(() => {
  const s = course.value.status
  return s === 0 ? 'info' : s === 1 ? 'success' : 'danger'
})
const statusText = computed(() => {
  const s = course.value.status
  return s === 0 ? '未开课' : s === 1 ? '进行中' : '已结束'
})

// 选课时间状态
const selectionTimeStatus = computed(() => {
  if (!course.value.selectionStartTime || !course.value.selectionEndTime) {
    return { type: 'info', text: '未设置' }
  }
  const now = new Date()
  const startTime = new Date(course.value.selectionStartTime)
  const endTime = new Date(course.value.selectionEndTime)

  if (now < startTime) {
    return { type: 'info', text: '未开始' }
  } else if (now > endTime) {
    return { type: 'danger', text: '已结束' }
  } else {
    return { type: 'success', text: '进行中' }
  }
})

// 编辑课程
const showEditDialog = ref(false)
const editLoading = ref(false)
const editFormRef = ref()
const editForm = reactive({
  courseName: '', courseCode: '', credit: 2, totalHours: 0, semester: '', status: 1, description: ''
})
const editRules = {
  courseName: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
  courseCode: [{ required: true, message: '请输入课程编码', trigger: 'blur' }]
}

// 编辑大纲
const showSyllabusDialog = ref(false)
const syllabusLoading = ref(false)
const syllabusContent = ref('')

// 获取课程详情
const fetchCourseDetail = async () => {
  loading.value = true
  try {
    const res = await getCourseById(courseId)
    course.value = res.data || {}
    syllabusContent.value = course.value.syllabus || ''

    // 如果是选修课，获取选课学生列表
    if (course.value.courseType === 1) {
      fetchCourseStudents()
    }
  } finally {
    loading.value = false
  }
}

// 获取关联班级
const fetchRelatedClasses = async () => {
  try {
    const res = await request({ url: `/classes/course/${courseId}`, method: 'get' })
    relatedClasses.value = res.data || []
  } catch (e) {
    console.error(e)
  }
}

// 获取历史作业
const fetchHomeworks = async () => {
  try {
    const res = await getHomeworkList({ courseId, pageNum: 1, pageSize: 5 })
    homeworkList.value = res.data?.records || []
  } catch (e) {
    console.error(e)
  }
}

// 获取历史考试
const fetchExams = async () => {
  try {
    const res = await getExamList({ courseId, pageNum: 1, pageSize: 5 })
    examList.value = res.data?.records || []
  } catch (e) {
    console.error(e)
  }
}

// 获取选课学生列表（仅选修课）
const fetchCourseStudents = async () => {
  if (course.value.courseType !== 1) return

  studentsLoading.value = true
  try {
    const res = await getCourseStudents(courseId)
    courseStudents.value = res.data || []
  } catch (e) {
    console.error('获取选课学生失败:', e)
  } finally {
    studentsLoading.value = false
  }
}

// 移除学生
const handleRemoveStudent = (student) => {
  ElMessageBox.confirm(
    `确定要移除学生"${student.studentName}"（${student.studentNumber}）吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await removeStudentFromCourse(courseId, student.studentId)
      ElMessage.success('移除成功')
      fetchCourseStudents()
      fetchCourseDetail() // 刷新课程信息以更新选课人数
    } catch (error) {
      console.error('移除学生失败:', error)
    }
  }).catch(() => {
    // 用户取消操作
  })
}

// 编辑课程
const handleEdit = () => {
  Object.assign(editForm, {
    courseName: course.value.courseName,
    courseCode: course.value.courseCode,
    credit: course.value.credit,
    totalHours: course.value.totalHours,
    semester: course.value.semester,
    status: course.value.status,
    description: course.value.description
  })
  showEditDialog.value = true
}

const handleSaveEdit = async () => {
  await editFormRef.value?.validate(async (valid) => {
    if (valid) {
      editLoading.value = true
      try {
        await updateCourse(courseId, editForm)
        ElMessage.success('保存成功')
        showEditDialog.value = false
        fetchCourseDetail()
      } finally {
        editLoading.value = false
      }
    }
  })
}

// 保存大纲
const handleSaveSyllabus = async () => {
  syllabusLoading.value = true
  try {
    await updateCourse(courseId, { ...course.value, syllabus: syllabusContent.value })
    ElMessage.success('大纲保存成功')
    showSyllabusDialog.value = false
    fetchCourseDetail()
  } finally {
    syllabusLoading.value = false
  }
}

onMounted(() => {
  fetchCourseDetail()
  fetchRelatedClasses()
  fetchHomeworks()
  fetchExams()
})
</script>

<style lang="scss" scoped>
.course-detail {
  .detail-content {
    margin-top: 20px;
  }
  
  .info-card, .syllabus-card, .class-card, .homework-card, .exam-card {
    margin-bottom: 20px;
  }
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .course-description {
    line-height: 1.8;
    color: #606266;

    :deep(p) {
      margin: 8px 0;
    }

    :deep(ul), :deep(ol) {
      padding-left: 20px;
      margin: 8px 0;
    }

    :deep(li) {
      margin: 4px 0;
    }
  }

  .syllabus-content {
    line-height: 1.8;
    color: #606266;
    
    :deep(h3) {
      margin: 16px 0 8px;
      color: #303133;
    }
    
    :deep(ul) {
      padding-left: 20px;
      margin: 8px 0;
    }
    
    :deep(li) {
      margin: 4px 0;
    }
  }
  
  .syllabus-tips {
    margin-top: 12px;
    padding: 12px;
    background: #f5f7fa;
    border-radius: 4px;
    font-size: 13px;
    color: #909399;
    
    code {
      display: block;
      margin-top: 8px;
      padding: 8px;
      background: #fff;
      border-radius: 4px;
      font-size: 12px;
    }
  }
}
</style>
