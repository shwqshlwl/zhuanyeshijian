<template>
  <div class="experiment-container">
    <div class="search-bar">
      <el-select v-model="searchForm.courseId" placeholder="选择课程" clearable style="width: 200px">
        <el-option v-for="c in courseOptions" :key="c.id" :label="c.name" :value="c.id" />
      </el-select>
      <el-button type="primary" @click="handleSearch"><el-icon><Search /></el-icon>搜索</el-button>
      <el-button type="primary" @click="handleAdd" v-if="userStore.isTeacher">
        <el-icon><Plus /></el-icon>创建实验
      </el-button>
    </div>

    <el-table :data="experimentList" v-loading="loading" stripe>
      <el-table-column prop="title" label="实验名称" min-width="200" />
      <el-table-column prop="courseName" label="所属课程" width="150" />
      <el-table-column prop="deadline" label="截止时间" width="180" />
      <el-table-column prop="language" label="编程语言" width="100" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="$router.push(`/experiments/${row.id}`)">详情</el-button>
          <template v-if="userStore.isTeacher">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container">
      <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
        layout="total, sizes, prev, pager, next" @change="fetchList" />
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑实验' : '创建实验'" width="600px">
      <el-form ref="formRef" :model="experimentForm" :rules="formRules" label-width="100px">
        <el-form-item label="实验名称" prop="title">
          <el-input v-model="experimentForm.title" placeholder="请输入实验名称" />
        </el-form-item>
        <el-form-item label="所属课程" prop="courseId">
          <el-select v-model="experimentForm.courseId" placeholder="请选择课程" style="width: 100%">
            <el-option v-for="c in courseOptions" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="编程语言" prop="language">
          <el-select v-model="experimentForm.language" placeholder="请选择编程语言" style="width: 100%">
            <el-option label="Java" value="java" />
            <el-option label="Python" value="python" />
            <el-option label="C++" value="cpp" />
            <el-option label="JavaScript" value="javascript" />
          </el-select>
        </el-form-item>
        <el-form-item label="截止时间" prop="deadline">
          <el-date-picker v-model="experimentForm.deadline" type="datetime" placeholder="选择截止时间" style="width: 100%" />
        </el-form-item>
        <el-form-item label="实验描述" prop="description">
          <el-input v-model="experimentForm.description" type="textarea" :rows="4" placeholder="请输入实验描述" />
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

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref()
const currentId = ref(null)
const experimentForm = reactive({ title: '', courseId: '', language: 'java', deadline: '', description: '' })
const formRules = {
  title: [{ required: true, message: '请输入实验名称', trigger: 'blur' }],
  courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
  language: [{ required: true, message: '请选择编程语言', trigger: 'change' }]
}

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
  courseOptions.value = res.data?.records || []
}

const handleSearch = () => { pageNum.value = 1; fetchList() }

const handleAdd = () => {
  isEdit.value = false
  currentId.value = null
  Object.assign(experimentForm, { title: '', courseId: '', language: 'java', deadline: '', description: '' })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  currentId.value = row.id
  Object.assign(experimentForm, {
    title: row.title, courseId: row.courseId, language: row.language, deadline: row.deadline, description: row.description
  })
  dialogVisible.value = true
}

const handleFormSubmit = async () => {
  await formRef.value?.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (isEdit.value) {
          await updateExperiment(currentId.value, experimentForm)
          ElMessage.success('更新成功')
        } else {
          await createExperiment(experimentForm)
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
  ElMessageBox.confirm(`确定删除实验"${row.title}"吗？`, '提示', { type: 'warning' }).then(async () => {
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
