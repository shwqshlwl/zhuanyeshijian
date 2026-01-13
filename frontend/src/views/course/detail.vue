<template>
  <div class="course-detail">
    <el-page-header @back="$router.back()">
      <template #content>
        <span>{{ course.name || '课程详情' }}</span>
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
          <el-descriptions-item label="课程名称">{{ course.name }}</el-descriptions-item>
          <el-descriptions-item label="课程编码">{{ course.code }}</el-descriptions-item>
          <el-descriptions-item label="授课教师">{{ course.teacherName }}</el-descriptions-item>
          <el-descriptions-item label="学分">{{ course.credit }}</el-descriptions-item>
          <el-descriptions-item label="总学时">{{ course.totalHours || '-' }}</el-descriptions-item>
          <el-descriptions-item label="学期">{{ course.semester || '-' }}</el-descriptions-item>
          <el-descriptions-item label="课程描述" :span="3">{{ course.description || '暂无描述' }}</el-descriptions-item>
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

      <!-- 关联班级 -->
      <el-card class="class-card">
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
    <el-dialog v-model="showEditDialog" title="编辑课程" width="600px">
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="80px">
        <el-form-item label="课程名称" prop="name">
          <el-input v-model="editForm.name" placeholder="请输入课程名称" />
        </el-form-item>
        <el-form-item label="课程编码" prop="code">
          <el-input v-model="editForm.code" placeholder="请输入课程编码" />
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
          <el-input v-model="editForm.description" type="textarea" :rows="4" placeholder="请输入课程描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" :loading="editLoading" @click="handleSaveEdit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 编辑大纲对话框 -->
    <el-dialog v-model="showSyllabusDialog" title="编辑课程大纲" width="800px">
      <el-input
        v-model="syllabusContent"
        type="textarea"
        :rows="15"
        placeholder="请输入课程大纲内容，支持HTML格式"
      />
      <div class="syllabus-tips">
        <p>提示：您可以使用HTML标签来格式化内容，例如：</p>
        <code>&lt;h3&gt;第一章 绑论&lt;/h3&gt;&lt;ul&gt;&lt;li&gt;1.1 课程介绍&lt;/li&gt;&lt;/ul&gt;</code>
      </div>
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
import { getCourseById, updateCourse } from '@/api/course'
import { getHomeworkList } from '@/api/homework'
import { getExamList } from '@/api/exam'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const route = useRoute()
const userStore = useUserStore()
const courseId = route.params.id

const loading = ref(false)
const course = ref({})
const relatedClasses = ref([])
const homeworkList = ref([])
const examList = ref([])

// 状态计算
const statusType = computed(() => {
  const s = course.value.status
  return s === 0 ? 'info' : s === 1 ? 'success' : 'danger'
})
const statusText = computed(() => {
  const s = course.value.status
  return s === 0 ? '未开课' : s === 1 ? '进行中' : '已结束'
})

// 编辑课程
const showEditDialog = ref(false)
const editLoading = ref(false)
const editFormRef = ref()
const editForm = reactive({
  name: '', code: '', credit: 2, totalHours: 0, semester: '', status: 1, description: ''
})
const editRules = {
  name: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入课程编码', trigger: 'blur' }]
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

// 编辑课程
const handleEdit = () => {
  Object.assign(editForm, {
    name: course.value.name,
    code: course.value.code,
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
