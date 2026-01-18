<template>
  <div class="homework-container">
    <div class="search-bar">
      <el-select v-model="searchForm.courseId" placeholder="选择课程" clearable style="width: 200px">
        <el-option v-for="c in courseOptions" :key="c.id" :label="c.courseName" :value="c.id" />
      </el-select>
      <el-select v-model="searchForm.classId" placeholder="选择班级" clearable style="width: 150px">
        <el-option v-for="cls in classOptions" :key="cls.id" :label="cls.className" :value="cls.id" />
      </el-select>
      <el-select v-model="searchForm.status" placeholder="状态" clearable style="width: 120px">
        <el-option label="未开始" :value="0" />
        <el-option label="进行中" :value="1" />
        <el-option label="已截止" :value="2" />
      </el-select>
      <el-button type="primary" @click="handleSearch"><el-icon><Search /></el-icon>搜索</el-button>
      <el-button type="primary" @click="handleAdd" v-if="userStore.isTeacher">
        <el-icon><Plus /></el-icon>布置作业
      </el-button>
    </div>

    <el-table :data="homeworkList" v-loading="loading" stripe>
      <el-table-column prop="title" label="作业标题" min-width="200">
        <template #default="{ row }">
          <el-link type="primary" @click="$router.push(`/homeworks/${row.id}`)">{{ row.homeworkTitle }}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="courseName" label="所属课程" width="150" />
      <el-table-column prop="className" label="所属班级" width="120">
        <template #default="{ row }">{{ row.className || '全部' }}</template>
      </el-table-column>
      <el-table-column prop="endTime" label="截止时间" width="180" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'info' : row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 0 ? '未开始' : row.status === 1 ? '进行中' : '已截止' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="提交情况" width="120" v-if="userStore.isTeacher">
        <template #default="{ row }">
          <el-progress :percentage="getSubmitPercentage(row)" :stroke-width="6" style="width: 80px" />
          <span style="font-size: 12px; color: #909399; margin-left: 4px">
            {{ row.submittedCount || 0 }}/{{ row.totalCount || 0 }}
          </span>
        </template>
      </el-table-column>
      <!--      <el-table-column label="我的状态" width="100" v-if="userStore.isStudent">-->
      <!--        <template #default="{ row }">-->
      <!--          <el-tag :type="row.myStatus === 2 ? 'success' : row.myStatus === 1 ? 'warning' : 'info'" size="small">-->
      <!--            {{ row.myStatus === 2 ? '已批改' : row.myStatus === 1 ? '已提交' : '未提交' }}-->
      <!--          </el-tag>-->
      <!--        </template>-->
      <!--      </el-table-column>-->
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="$router.push(`/homeworks/${row.id}`)">详情</el-button>
          <template v-if="userStore.isTeacher">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
          <el-button v-if="userStore.isStudent && row.status === 1 && row.myStatus !== 2" type="success" link
                     @click="$router.push(`/homeworks/${row.id}`)">
            {{ row.myStatus === 1 ? '修改' : '提交' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container">
      <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
                     :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next" @change="fetchList" />
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑作业' : '布置作业'" width="650px">
      <el-form ref="formRef" :model="homeworkForm" :rules="formRules" label-width="100px">
        <el-form-item label="作业标题" prop="title">
          <el-input v-model="homeworkForm.homeworkTitle" placeholder="请输入作业标题" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="所属课程" prop="courseId">
              <el-select v-model="homeworkForm.courseId" placeholder="请选择课程" style="width: 100%">
                <el-option v-for="c in courseOptions" :key="c.id" :label="c.courseName" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属班级">
              <el-select v-model="homeworkForm.classId" placeholder="全部班级" clearable style="width: 100%">
                <el-option v-for="cls in classOptions" :key="cls.id" :label="cls.className" :value="cls.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始时间">
              <el-date-picker v-model="homeworkForm.startTime" type="datetime" placeholder="选择开始时间" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="截止时间" prop="endTime">
              <el-date-picker v-model="homeworkForm.endTime" type="datetime" placeholder="选择截止时间" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="总分">
              <el-input-number v-model="homeworkForm.totalScore" :min="1" :max="200" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="允许迟交">
              <el-switch v-model="homeworkForm.allowLate" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="作业内容" prop="content">
          <el-input v-model="homeworkForm.content" type="textarea" :rows="5" placeholder="请输入作业要求和内容" />
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
import {getHomeworkList, createHomework, updateHomework, deleteHomework, refreshStatus} from '@/api/homework'
import { getCourseList } from '@/api/course'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import {getClassList} from "@/api/class.js";

const userStore = useUserStore()
const loading = ref(false)
const homeworkList = ref([])
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
const homeworkForm = reactive({
  homeworkTitle: '', courseId: '', classId: '', startTime: '', endTime: '',
  content: '', totalScore: 100, allowLate: false
})
const formRules = {
  homeworkTitle: [{ required: true, message: '请输入作业标题', trigger: 'blur' }],
  courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择截止时间', trigger: 'change' }]
}

const getSubmitPercentage = (row) => {
  const total = row.totalCount || 0
  const submit = row.submittedCount || 0
  return total > 0 ? Math.round((submit / total) * 100) : 0
}

const fetchList = async () => {
  loading.value = true
  try {
    const url = userStore.isStudent ? '/homeworks/student/my' : '/homeworks/listDetailSubmit'
    console.log(searchForm)
    const res = await request({ url, method: 'get', params: { pageNum: pageNum.value, pageSize: pageSize.value, ...searchForm } })
    homeworkList.value = res.data?.records || []
    console.log(homeworkList.value)
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}
const fetchStatus = async () => {
  loading.value = true
  try {
    await refreshStatus()
  } finally {
    loading.value = false
  }
}

const fetchCourses = async () => {
  const res = await getCourseList({ pageNum: 1, pageSize: 100 })
  courseOptions.value = res.data?.records || []
}
const fetchClasses = async () => {
  const res = await getClassList({ pageNum: 1, pageSize: 100 })
  classOptions.value = res.data?.records || []
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
  homeworkForm.classId = ''
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
  Object.assign(homeworkForm, {
    title: '', courseId: '', classId: '', startTime: '', endTime: '',
    content: '', totalScore: 100, allowLate: false
  })
  formClassOptions.value = []
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  currentId.value = row.id
  Object.assign(homeworkForm, {
    title: row.title, courseId: row.courseId, classId: row.classId,
    startTime: row.startTime, endTime: row.endTime,
    content: row.content, totalScore: row.totalScore || 100, allowLate: row.allowLate || false
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
        if (isEdit.value) {
          await updateHomework(currentId.value, homeworkForm)
          ElMessage.success('更新成功')
        } else {
          await createHomework(homeworkForm)
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
  ElMessageBox.confirm(`确定删除作业"${row.title}"吗？`, '提示', { type: 'warning' }).then(async () => {
    await deleteHomework(row.id)
    ElMessage.success('删除成功')
    fetchList()
  })
}

onMounted(() => { fetchStatus();fetchList(); fetchCourses();fetchClasses() })
</script>

<style lang="scss" scoped>
.homework-container {
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