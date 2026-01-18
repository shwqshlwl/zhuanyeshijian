<template>
  <div class="homework-detail">
    <el-page-header @back="$router.back()">
      <template #content>
        <span>{{ homework.title || '作业详情' }}</span>
        <el-tag :type="statusType" style="margin-left: 12px">{{ statusText }}</el-tag>
      </template>
    </el-page-header>

    <div class="detail-content" v-loading="loading">
      <!-- 基本信息 -->
      <el-card class="info-card">
        <template #header>
          <div class="card-header">
            <span>作业信息</span>
            <el-button type="primary" size="small" @click="handleEdit" v-if="userStore.isTeacher">
              <el-icon><Edit /></el-icon>编辑
            </el-button>
          </div>
        </template>
        <el-descriptions :column="3" border>
          <el-descriptions-item label="作业标题">{{ homework.homeworkTitle }}</el-descriptions-item>
          <el-descriptions-item label="所属课程">{{ homework.courseName }}</el-descriptions-item>
          <el-descriptions-item label="所属班级">{{ homework.className || (homework.classId ? '班级信息缺失' : '全部班级') }}</el-descriptions-item>
          <el-descriptions-item label="开始时间">{{ homework.startTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="截止时间">{{ homework.endTime }}</el-descriptions-item>
          <el-descriptions-item label="总分">{{ homework.totalScore || 100 }} 分</el-descriptions-item>
          <el-descriptions-item label="提交情况">
            <span class="submit-stats">
              <el-progress :percentage="submitPercentage" :stroke-width="10" style="width: 150px" />
              <span style="margin-left: 8px">{{ homework.submittedCount || 0 }} / {{ homework.totalCount || 0 }}</span>
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="允许迟交">
            <el-tag :type="homework.allowLate ? 'success' : 'info'" size="small">
              {{ homework.allowLate ? '是' : '否' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="作业内容" :span="3">
            <div class="homework-content">{{ homework.content || '暂无内容' }}</div>
            <el-button 
              type="primary" 
              link 
              icon="ChatDotRound" 
              @click="handleAiAnalysis(homework.content)"
              style="margin-top: 10px;"
            >
              AI 题目解析
            </el-button>
          </el-descriptions-item>
          <el-descriptions-item label="附件" :span="3" v-if="homework.attachment">
            <el-link type="primary" :href="homework.attachment" target="_blank">
              <el-icon><Download /></el-icon> 下载附件
            </el-link>
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 学生提交区域 -->
      <el-card class="submit-card" v-if="userStore.isStudent">
        <template #header>
          <div class="card-header">
            <span>我的提交</span>
            <el-tag v-if="mySubmission.status === 2" type="success">已批改</el-tag>
            <el-tag v-else-if="mySubmission.status === 1" type="warning">已提交</el-tag>
            <el-tag v-else type="info">未提交</el-tag>
          </div>
        </template>
        
        <!-- 已批改显示结果 -->
        <div v-if="mySubmission.status === 2" class="graded-result">
          <el-result icon="success" title="作业已批改">
            <template #extra>
              <div class="score-display">
                <span class="score-label">得分</span>
                <span class="score-value">{{ mySubmission.score }}</span>
                <span class="score-total">/ {{ homework.totalScore || 100 }}</span>
              </div>
            </template>
          </el-result>
          <el-descriptions :column="1" border style="margin-top: 20px">
            <el-descriptions-item label="提交时间">{{ mySubmission.submitTime }}</el-descriptions-item>
            <el-descriptions-item label="批改时间">{{ mySubmission.gradeTime }}</el-descriptions-item>
            <el-descriptions-item label="我的答案">
              <div class="my-answer">{{ mySubmission.content }}</div>
            </el-descriptions-item>
            <el-descriptions-item label="教师评语">
              <div class="teacher-comment">{{ mySubmission.comment || '暂无评语' }}</div>
            </el-descriptions-item>
          </el-descriptions>
        </div>
        
        <!-- 未批改可编辑 -->
        <div v-else class="submit-form">
          <el-form label-position="top">
            <el-form-item label="作业内容">
              <el-input
                v-model="submitForm.content"
                type="textarea"
                :rows="8"
                placeholder="请输入作业内容..."
                :disabled="homework.status === 2 && !homework.allowLate"
              />
            </el-form-item>
<!--            <el-form-item label="附件上传">-->
<!--              <el-upload-->
<!--                class="upload-area"-->
<!--                drag-->
<!--                action="#"-->
<!--                :auto-upload="false"-->
<!--                :on-change="handleFileChange"-->
<!--              >-->
<!--                <el-icon class="el-icon&#45;&#45;upload"><Upload /></el-icon>-->
<!--                <div class="el-upload__text">拖拽文件到此处或 <em>点击上传</em></div>-->
<!--              </el-upload>-->
<!--            </el-form-item>-->
            <el-form-item>
              <el-button
                type="primary"
                size="large"
                :loading="submitLoading"
                :disabled="homework.status === 0 || (homework.status === 2 && !homework.allowLate)"
                @click="handleSubmit"
              >
                {{ mySubmission.status === 1 ? '重新提交' : '提交作业' }}
              </el-button>
              <span v-if="homework.status === 0 || (homework.status === 2 && !homework.allowLate)" class="deadline-tip">
                作业没开始或者已截止且不允许迟交
              </span>
            </el-form-item>
          </el-form>
        </div>
      </el-card>

      <!-- 教师批改区域 -->
      <el-card class="grade-card" v-if="userStore.isTeacher">
        <template #header>
          <div class="card-header">
            <span>学生提交列表</span>
            <div class="header-actions">
              <el-select v-model="filterStatus" placeholder="筛选状态" clearable style="width: 120px">
                <el-option label="未提交" :value="0" />
                <el-option label="已提交" :value="1" />
                <el-option label="已批改" :value="2" />
              </el-select>
              <el-button type="success" size="small" @click="exportSubmissions">
                <el-icon><Download /></el-icon>导出
              </el-button>
            </div>
          </div>
        </template>
        
        <el-table :data="filteredSubmissions" stripe v-loading="submissionsLoading">
          <el-table-column prop="studentNo" label="学号" width="120" />
          <el-table-column prop="studentName" label="姓名" width="100" />
          <el-table-column prop="submitTime" label="提交时间" width="180">
            <template #default="{ row }">
              <span>{{ row.submitTime || '-' }}</span>
              <el-tag v-if="row.isLate" type="warning" size="small" style="margin-left: 4px">迟交</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 0 ? 'info' : row.status === 1 ? 'warning' : 'success'" size="small">
                {{ row.status === 0 ? '未提交' : row.status === 1 ? '待批改' : '已批改' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="score" label="得分" width="80">
            <template #default="{ row }">
              {{ row.score !== null && row.score !== undefined ? row.score : '-' }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link @click="openGradeDialog(row)" :disabled="row.status === 0">
                {{ row.status === 2 ? '查看/修改' : '批改' }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="submissionPageNum"
            v-model:page-size="submissionPageSize"
            :total="submissionTotal"
            layout="total, prev, pager, next"
            @current-change="fetchSubmissions"
          />
        </div>
      </el-card>
    </div>

    <!-- 批改对话框 -->
    <el-dialog v-model="gradeDialogVisible" title="批改作业" width="700px">
      <div class="grade-dialog-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="学生">{{ currentSubmission.studentName }} ({{ currentSubmission.studentNo }})</el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ currentSubmission.submitTime }}</el-descriptions-item>
        </el-descriptions>
        
        <div class="student-answer">
          <h4>学生答案</h4>
          <div class="answer-content">{{ currentSubmission.content || '未提交内容' }}</div>
          <el-link v-if="currentSubmission.attachment" type="primary" :href="currentSubmission.attachment" target="_blank">
            <el-icon><Download /></el-icon> 下载附件
          </el-link>
        </div>
        
        <el-form :model="gradeForm" label-width="80px" style="margin-top: 20px">
          <el-form-item label="评分">
            <el-input-number v-model="gradeForm.score" :min="0" :max="homework.totalScore || 100" />
            <span style="margin-left: 8px; color: #909399">/ {{ homework.totalScore || 100 }} 分</span>
          </el-form-item>
          <el-form-item label="评语">
            <el-input v-model="gradeForm.comment" type="textarea" :rows="4" placeholder="请输入评语（选填）" />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="gradeDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="gradeLoading" @click="handleGrade">确认批改</el-button>
      </template>
    </el-dialog>

    <!-- 编辑作业对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑作业" width="600px">
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="80px">
        <el-form-item label="作业标题" prop="title">
          <el-input v-model="editForm.title" placeholder="请输入作业标题" />
        </el-form-item>
        <el-form-item label="截止时间" prop="deadline">
          <el-date-picker v-model="editForm.deadline" type="datetime" placeholder="选择截止时间" style="width: 100%" />
        </el-form-item>
        <el-form-item label="总分">
          <el-input-number v-model="editForm.totalScore" :min="1" :max="200" />
        </el-form-item>
        <el-form-item label="允许迟交">
          <el-switch v-model="editForm.allowLate" />
        </el-form-item>
        <el-form-item label="作业内容" prop="content">
          <el-input v-model="editForm.content" type="textarea" :rows="5" placeholder="请输入作业内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="editLoading" @click="handleSaveEdit">保存</el-button>
      </template>
    </el-dialog>

    <AiAnalysisPopup v-model="aiAnalysisVisible" :question-content="currentAnalysisContent" />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import AiAnalysisPopup from '@/views/ai/components/AiAnalysisPopup.vue'
import { useUserStore } from '@/stores/user'
import { getHomeworkById, updateHomework } from '@/api/homework'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const route = useRoute()
const userStore = useUserStore()
const homeworkId = route.params.id

const loading = ref(false)
const homework = ref({})

const aiAnalysisVisible = ref(false)
const currentAnalysisContent = ref('')

const handleAiAnalysis = (content) => {
  if (!content) return
  currentAnalysisContent.value = content
  aiAnalysisVisible.value = true
}

// 状态计算
const statusType = computed(() => {
  const s = homework.value.status
  return s === 0 ? 'info' : s === 1 ? 'success' : 'danger'
})
const statusText = computed(() => {
  const s = homework.value.status
  return s === 0 ? '未开始' : s === 1 ? '进行中' : '已截止'
})
const submitPercentage = computed(() => {
  const total = homework.value.totalCount || 0
  const submit = homework.value.submittedCount || 0
  return total > 0 ? Math.round((submit / total) * 100) : 0
})

// 学生提交
const mySubmission = ref({})
const submitForm = reactive({ content: '', attachment: '' })
const submitLoading = ref(false)
// 教师批改
const submissionsLoading = ref(false)
const submissions = ref([])
const submissionPageNum = ref(1)
const submissionPageSize = ref(10)
const submissionTotal = ref(0)
const filterStatus = ref('')

const filteredSubmissions = computed(() => {
  if (filterStatus.value === '') return submissions.value
  return submissions.value.filter(s => s.status === filterStatus.value)
})

const gradeDialogVisible = ref(false)
const gradeLoading = ref(false)
const currentSubmission = ref({})
const gradeForm = reactive({ score: 0, comment: '' })

// 编辑作业
const editDialogVisible = ref(false)
const editLoading = ref(false)
const editFormRef = ref()
const editForm = reactive({ title: '', deadline: '', totalScore: 100, allowLate: false, content: '' })
const editRules = {
  title: [{ required: true, message: '请输入作业标题', trigger: 'blur' }],
  deadline: [{ required: true, message: '请选择截止时间', trigger: 'change' }]
}

// 获取作业详情
const fetchHomeworkDetail = async () => {
  loading.value = true
  try {
    const res = await getHomeworkById(homeworkId)
    homework.value = res.data || {}
  } finally {
    loading.value = false
  }
}

// 获取我的提交（学生）
const fetchMySubmission = async () => {
  try {
    const res = await request({
      url: `/homeworks/${homeworkId}/submissions/${userStore.userId}`,
      method: 'get'
    })
    mySubmission.value = res.data || {}
    if (mySubmission.value.content) {
      submitForm.content = mySubmission.value.content
    }
  } catch (e) {
    console.error(e)
  }
}

// 获取提交列表（教师）
const fetchSubmissions = async () => {
  submissionsLoading.value = true
  try {
    const res = await request({
      url: `/homeworks/${homeworkId}/submissions`,
      method: 'get',
      params: { pageNum: submissionPageNum.value, pageSize: submissionPageSize.value }
    })
    submissions.value = res.data?.records || []
    submissionTotal.value = res.data?.total || 0
  } finally {
    submissionsLoading.value = false
  }
}

// 学生提交作业
const handleSubmit = async () => {
  if (!submitForm.content.trim()) {
    ElMessage.warning('请输入作业内容')
    return
  }
  submitLoading.value = true
  try {
    await request({
      url: `/homeworks/${homeworkId}/submit`,
      method: 'post',
      data: submitForm
    })
    ElMessage.success('提交成功')
    fetchMySubmission()
  } finally {
    submitLoading.value = false
  }
}

// 文件上传处理
const handleFileChange = (file) => {
  // 这里可以实现文件上传逻辑
  console.log('File selected:', file)
}

// 打开批改对话框
const openGradeDialog = (row) => {
  currentSubmission.value = row
  gradeForm.score = row.score || 0
  gradeForm.comment = row.comment || ''
  gradeDialogVisible.value = true
}

// 批改作业
const handleGrade = async () => {
  gradeLoading.value = true
  try {
    await request({
      url: `/homeworks/${homeworkId}/grade`,
      method: 'put',
      params: { studentId: currentSubmission.value.studentId },
      data: gradeForm
    })
    ElMessage.success('批改成功')
    gradeDialogVisible.value = false
    fetchSubmissions()
  } finally {
    gradeLoading.value = false
  }
}

// 编辑作业
const handleEdit = () => {
  Object.assign(editForm, {
    title: homework.value.title,
    deadline: homework.value.deadline,
    totalScore: homework.value.totalScore || 100,
    allowLate: homework.value.allowLate || false,
    content: homework.value.content
  })
  editDialogVisible.value = true
}

const handleSaveEdit = async () => {
  await editFormRef.value?.validate(async (valid) => {
    if (valid) {
      editLoading.value = true
      try {
        await updateHomework(homeworkId, editForm)
        ElMessage.success('保存成功')
        editDialogVisible.value = false
        fetchHomeworkDetail()
      } finally {
        editLoading.value = false
      }
    }
  })
}

// 导出提交
const exportSubmissions = () => {
  ElMessage.info('导出功能开发中')
}

onMounted(() => {
  fetchHomeworkDetail()
  if (userStore.isStudent) {
    fetchMySubmission()
  } else if (userStore.isTeacher) {
    fetchSubmissions()
  }
})
</script>

<style lang="scss" scoped>
.homework-detail {
  .detail-content {
    margin-top: 20px;
  }
  
  .info-card, .submit-card, .grade-card {
    margin-bottom: 20px;
  }
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .header-actions {
      display: flex;
      gap: 12px;
    }
  }
  
  .homework-content {
    white-space: pre-wrap;
    line-height: 1.8;
  }
  
  .submit-stats {
    display: flex;
    align-items: center;
  }
  
  // 学生提交区域
  .graded-result {
    .score-display {
      text-align: center;
      
      .score-label {
        display: block;
        font-size: 14px;
        color: #909399;
        margin-bottom: 8px;
      }
      
      .score-value {
        font-size: 48px;
        font-weight: 600;
        color: #67c23a;
      }
      
      .score-total {
        font-size: 24px;
        color: #909399;
      }
    }
    
    .my-answer, .teacher-comment {
      white-space: pre-wrap;
      line-height: 1.6;
    }
  }
  
  .submit-form {
    .upload-area {
      width: 100%;
    }
    
    .deadline-tip {
      margin-left: 12px;
      color: #f56c6c;
      font-size: 13px;
    }
  }
  
  // 批改对话框
  .grade-dialog-content {
    .student-answer {
      margin-top: 20px;
      
      h4 {
        margin: 0 0 12px;
        font-size: 14px;
        color: #606266;
      }
      
      .answer-content {
        padding: 16px;
        background: #f5f7fa;
        border-radius: 8px;
        white-space: pre-wrap;
        line-height: 1.6;
        max-height: 200px;
        overflow-y: auto;
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
