<template>
  <div class="exam-container">
    <div class="search-bar">
      <el-select v-model="searchForm.courseId" placeholder="选择课程" clearable style="width: 200px" @change="handleCourseChange">
        <el-option v-for="c in courseOptions" :key="c.id" :label="c.name" :value="c.id" />
      </el-select>
      <el-select v-model="searchForm.classId" placeholder="选择班级" clearable style="width: 150px">
        <el-option v-for="cls in classOptions" :key="cls.id" :label="cls.name" :value="cls.id" />
      </el-select>
      <el-select v-model="searchForm.status" placeholder="状态" clearable style="width: 120px">
        <el-option label="未开始" :value="0" />
        <el-option label="进行中" :value="1" />
        <el-option label="已结束" :value="2" />
      </el-select>
      <el-button type="primary" @click="handleSearch"><el-icon><Search /></el-icon>搜索</el-button>
      <el-button type="primary" @click="handleAdd" v-if="userStore.isTeacher">
        <el-icon><Plus /></el-icon>创建考试
      </el-button>
    </div>

    <el-table :data="examList" v-loading="loading" stripe>
      <el-table-column prop="title" label="考试名称" min-width="200">
        <template #default="{ row }">
          <el-link type="primary" @click="$router.push(`/exams/${row.id}`)">{{ row.title }}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="courseName" label="所属课程" width="150" />
      <el-table-column prop="className" label="关联班级" width="120">
        <template #default="{ row }">{{ row.className || '全部' }}</template>
      </el-table-column>
      <el-table-column prop="startTime" label="开始时间" width="180" />
      <el-table-column prop="duration" label="时长(分钟)" width="100" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'info' : row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 0 ? '未开始' : row.status === 1 ? '进行中' : '已结束' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="参考情况" width="100" v-if="userStore.isTeacher">
        <template #default="{ row }">
          {{ row.submitCount || 0 }}/{{ row.totalCount || 0 }}
        </template>
      </el-table-column>
      <el-table-column label="我的状态" width="100" v-if="userStore.isStudent">
        <template #default="{ row }">
          <el-tag :type="row.myStatus === 2 ? 'success' : row.myStatus === 1 ? 'warning' : 'info'" size="small">
            {{ row.myStatus === 2 ? '已完成' : row.myStatus === 1 ? '答题中' : '未参加' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="$router.push(`/exams/${row.id}`)">详情</el-button>
          <template v-if="userStore.isTeacher">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
          <el-button v-if="userStore.isStudent && row.status === 1 && row.myStatus !== 2" type="success" link 
            @click="$router.push(`/exams/${row.id}/take`)">
            {{ row.myStatus === 1 ? '继续' : '参加' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container">
      <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
        :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next" @change="fetchList" />
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑考试' : '创建考试'" width="650px">
      <el-form ref="formRef" :model="examForm" :rules="formRules" label-width="100px">
        <el-form-item label="考试名称" prop="title">
          <el-input v-model="examForm.title" placeholder="请输入考试名称" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="所属课程" prop="courseId">
              <el-select v-model="examForm.courseId" placeholder="请选择课程" style="width: 100%" @change="onFormCourseChange">
                <el-option v-for="c in courseOptions" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="关联班级">
              <el-select v-model="examForm.classId" placeholder="全部班级" clearable style="width: 100%">
                <el-option v-for="cls in formClassOptions" :key="cls.id" :label="cls.name" :value="cls.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="考试时间" prop="timeRange">
          <el-date-picker v-model="examForm.timeRange" type="datetimerange" start-placeholder="开始时间"
            end-placeholder="结束时间" style="width: 100%" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="考试时长" prop="duration">
              <el-input-number v-model="examForm.duration" :min="10" :max="300" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="总分">
              <el-input-number v-model="examForm.totalScore" :min="1" :max="200" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="及格分">
              <el-input-number v-model="examForm.passScore" :min="0" :max="200" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="考试说明">
          <el-input v-model="examForm.description" type="textarea" :rows="3" placeholder="考试说明（选填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleFormSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getExamList, createExam, updateExam, deleteExam } from '@/api/exam'
import { getCourseList } from '@/api/course'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const userStore = useUserStore()
const loading = ref(false)
const examList = ref([])
const courseOptions = ref([])
const classOptions = ref([])
const formClassOptions = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const searchForm = reactive({ courseId: '', classId: '', status: '' })

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref()
const currentId = ref(null)
const examForm = reactive({ 
  title: '', courseId: '', classId: '', timeRange: [], 
  duration: 90, totalScore: 100, passScore: 60, description: '' 
})
const formRules = {
  title: [{ required: true, message: '请输入考试名称', trigger: 'blur' }],
  courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
  timeRange: [{ required: true, message: '请选择考试时间', trigger: 'change' }]
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getExamList({ pageNum: pageNum.value, pageSize: pageSize.value, ...searchForm })
    examList.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const fetchCourses = async () => {
  const res = await getCourseList({ pageNum: 1, pageSize: 100 })
  courseOptions.value = res.data?.records || []
}

const fetchClassesByCourse = async (courseId) => {
  if (!courseId) {
    classOptions.value = []
    return
  }
  try {
    const res = await request({ url: `/classes/course/${courseId}`, method: 'get' })
    classOptions.value = res.data || []
  } catch (e) {
    classOptions.value = []
  }
}

const handleCourseChange = (courseId) => {
  searchForm.classId = ''
  fetchClassesByCourse(courseId)
}

const onFormCourseChange = (courseId) => {
  examForm.classId = ''
  if (courseId) {
    request({ url: `/classes/course/${courseId}`, method: 'get' }).then(res => {
      formClassOptions.value = res.data || []
    })
  } else {
    formClassOptions.value = []
  }
}

const handleSearch = () => { pageNum.value = 1; fetchList() }

const handleAdd = () => {
  isEdit.value = false
  currentId.value = null
  Object.assign(examForm, { 
    title: '', courseId: '', classId: '', timeRange: [], 
    duration: 90, totalScore: 100, passScore: 60, description: '' 
  })
  formClassOptions.value = []
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  currentId.value = row.id
  Object.assign(examForm, {
    title: row.title, courseId: row.courseId, classId: row.classId,
    timeRange: [row.startTime, row.endTime], duration: row.duration, 
    totalScore: row.totalScore, passScore: row.passScore || 60, description: row.description
  })
  if (row.courseId) {
    onFormCourseChange(row.courseId)
  }
  dialogVisible.value = true
}

const handleFormSubmit = async () => {
  await formRef.value?.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        const data = {
          ...examForm,
          startTime: examForm.timeRange[0],
          endTime: examForm.timeRange[1]
        }
        delete data.timeRange
        if (isEdit.value) {
          await updateExam(currentId.value, data)
          ElMessage.success('更新成功')
        } else {
          await createExam(data)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        fetchList()
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除考试"${row.title}"吗？`, '提示', { type: 'warning' }).then(async () => {
    await deleteExam(row.id)
    ElMessage.success('删除成功')
    fetchList()
  })
}

onMounted(() => { fetchList(); fetchCourses() })
</script>

<style lang="scss" scoped>
.exam-container {
  .search-bar {
    display: flex;
    gap: 12px;
    margin-bottom: 20px;
    padding: 20px;
    background: #fff;
    border-radius: 12px;
  }
  
  .pagination-container {
    margin-top: 16px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
