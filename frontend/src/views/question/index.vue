<template>
  <div class="question-container">
    <div class="search-bar">
      <el-select v-model="searchForm.courseId" placeholder="选择课程" clearable style="width: 200px">
        <el-option v-for="c in courseOptions" :key="c.id" :label="c.name" :value="c.id" />
      </el-select>
      <el-select v-model="searchForm.questionTypeId" placeholder="题型" clearable style="width: 150px">
        <el-option v-for="t in typeOptions" :key="t.id" :label="t.name" :value="t.id" />
      </el-select>
      <el-select v-model="searchForm.difficulty" placeholder="难度" clearable style="width: 120px">
        <el-option label="简单" :value="1" />
        <el-option label="中等" :value="2" />
        <el-option label="困难" :value="3" />
      </el-select>
      <el-button type="primary" @click="handleSearch"><el-icon><Search /></el-icon>搜索</el-button>
      <el-button type="primary" @click="handleAdd" v-if="userStore.isTeacher">
        <el-icon><Plus /></el-icon>添加题目
      </el-button>
      <el-button type="warning" @click="showTypeDialog = true" v-if="userStore.isTeacher">
        <el-icon><Setting /></el-icon>题型管理
      </el-button>
      <el-button type="danger" @click="handleBatchDelete" :disabled="!selectedIds.length" v-if="userStore.isTeacher">
        批量删除
      </el-button>
    </div>

    <el-table :data="questionList" v-loading="loading" stripe @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" v-if="userStore.isTeacher" />
      <el-table-column prop="content" label="题目内容" min-width="300" show-overflow-tooltip />
      <el-table-column prop="typeName" label="题型" width="100" />
      <el-table-column prop="courseName" label="所属课程" width="150" />
      <el-table-column prop="difficulty" label="难度" width="80">
        <template #default="{ row }">
          <el-tag :type="row.difficulty === 1 ? 'success' : row.difficulty === 2 ? 'warning' : 'danger'" size="small">
            {{ row.difficulty === 1 ? '简单' : row.difficulty === 2 ? '中等' : '困难' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right" v-if="userStore.isTeacher">
        <template #default="{ row }">
          <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
          <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container">
      <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
        layout="total, sizes, prev, pager, next" @change="fetchList" />
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑题目' : '添加题目'" width="700px">
      <el-form ref="formRef" :model="questionForm" :rules="formRules" label-width="100px">
        <el-form-item label="题型" prop="questionTypeId">
          <el-select v-model="questionForm.questionTypeId" placeholder="请选择题型" style="width: 100%">
            <el-option v-for="t in typeOptions" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属课程" prop="courseId">
          <el-select v-model="questionForm.courseId" placeholder="请选择课程" style="width: 100%">
            <el-option v-for="c in courseOptions" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="题目内容" prop="content">
          <el-input v-model="questionForm.content" type="textarea" :rows="4" placeholder="请输入题目内容" />
        </el-form-item>
        <el-form-item label="选项" v-if="questionForm.questionTypeId && isChoiceType">
          <div v-for="(opt, idx) in questionForm.options" :key="idx" class="option-item">
            <span class="option-label">{{ String.fromCharCode(65 + idx) }}.</span>
            <el-input v-model="questionForm.options[idx]" placeholder="请输入选项内容" />
            <el-button type="danger" link @click="questionForm.options.splice(idx, 1)" v-if="questionForm.options.length > 2">
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
          <el-button type="primary" link @click="questionForm.options.push('')" v-if="questionForm.options.length < 6">
            <el-icon><Plus /></el-icon>添加选项
          </el-button>
        </el-form-item>
        <el-form-item label="正确答案" prop="answer">
          <el-input v-model="questionForm.answer" :placeholder="isChoiceType ? '如：A 或 A,B,C' : '请输入正确答案'" />
        </el-form-item>
        <el-form-item label="难度" prop="difficulty">
          <el-radio-group v-model="questionForm.difficulty">
            <el-radio :label="1">简单</el-radio>
            <el-radio :label="2">中等</el-radio>
            <el-radio :label="3">困难</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="解析">
          <el-input v-model="questionForm.analysis" type="textarea" :rows="3" placeholder="请输入题目解析（选填）" />
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
import { ref, reactive, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getQuestionList, getAllQuestionTypes, createQuestion, updateQuestion, deleteQuestion, batchDeleteQuestions } from '@/api/question'
import { getCourseList } from '@/api/course'
import { ElMessage, ElMessageBox } from 'element-plus'

const userStore = useUserStore()
const loading = ref(false)
const questionList = ref([])
const courseOptions = ref([])
const typeOptions = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const searchForm = reactive({ courseId: '', questionTypeId: '' })
const selectedIds = ref([])

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref()
const currentId = ref(null)
const questionForm = reactive({
  questionTypeId: '', courseId: '', content: '', options: ['', '', '', ''], answer: '', difficulty: 1, analysis: ''
})
const formRules = {
  questionTypeId: [{ required: true, message: '请选择题型', trigger: 'change' }],
  courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
  content: [{ required: true, message: '请输入题目内容', trigger: 'blur' }],
  answer: [{ required: true, message: '请输入正确答案', trigger: 'blur' }]
}

const isChoiceType = computed(() => {
  const type = typeOptions.value.find(t => t.id === questionForm.questionTypeId)
  return type && (type.code === 'SINGLE' || type.code === 'MULTIPLE')
})

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getQuestionList({ pageNum: pageNum.value, pageSize: pageSize.value, ...searchForm })
    questionList.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const fetchOptions = async () => {
  const [courseRes, typeRes] = await Promise.all([
    getCourseList({ pageNum: 1, pageSize: 100 }),
    getAllQuestionTypes()
  ])
  courseOptions.value = courseRes.data?.records || []
  typeOptions.value = typeRes.data || []
}

const handleSearch = () => { pageNum.value = 1; fetchList() }
const handleSelectionChange = (rows) => { selectedIds.value = rows.map(r => r.id) }

const handleAdd = () => {
  isEdit.value = false
  currentId.value = null
  Object.assign(questionForm, {
    questionTypeId: '', courseId: '', content: '', options: ['', '', '', ''], answer: '', difficulty: 1, analysis: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  currentId.value = row.id
  Object.assign(questionForm, {
    questionTypeId: row.questionTypeId, courseId: row.courseId, content: row.content,
    options: row.options || ['', '', '', ''], answer: row.answer, difficulty: row.difficulty, analysis: row.analysis
  })
  dialogVisible.value = true
}

const handleFormSubmit = async () => {
  await formRef.value?.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        const data = { ...questionForm }
        if (!isChoiceType.value) delete data.options
        if (isEdit.value) {
          await updateQuestion(currentId.value, data)
          ElMessage.success('更新成功')
        } else {
          await createQuestion(data)
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
  ElMessageBox.confirm('确定删除该题目吗？', '提示', { type: 'warning' }).then(async () => {
    await deleteQuestion(row.id)
    ElMessage.success('删除成功')
    fetchList()
  })
}

const handleBatchDelete = () => {
  ElMessageBox.confirm(`确定删除选中的 ${selectedIds.value.length} 道题目吗？`, '提示', { type: 'warning' }).then(async () => {
    await batchDeleteQuestions(selectedIds.value)
    ElMessage.success('删除成功')
    fetchList()
  })
}

onMounted(() => { fetchList(); fetchOptions() })
</script>

<style lang="scss" scoped>
.question-container {
  .search-bar {
    display: flex; gap: 12px; margin-bottom: 20px; padding: 20px; background: #fff; border-radius: 12px;
  }
  .option-item {
    display: flex; align-items: center; gap: 8px; margin-bottom: 10px;
    .option-label { width: 24px; font-weight: 600; }
    .el-input { flex: 1; }
  }
}
</style>
