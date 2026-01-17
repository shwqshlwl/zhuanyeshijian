<template>
  <div class="exam-detail">
    <el-page-header @back="$router.back()">
      <template #content>
        <span>{{ exam.examName || '考试详情' }}</span>
        <el-tag :type="statusType" style="margin-left: 12px">{{ statusText }}</el-tag>
      </template>
    </el-page-header>

    <div class="detail-content" v-loading="loading">
      <!-- 基本信息 -->
      <el-card class="info-card">
        <template #header>
          <div class="card-header">
            <span>考试信息</span>
            <div class="header-actions" v-if="userStore.isTeacher">
              <el-button type="primary" size="small" @click="handleEdit">
                <el-icon><Edit /></el-icon>编辑
              </el-button>
              <el-button type="success" size="small" @click="showQuestionDialog = true">
                <el-icon><Plus /></el-icon>管理题目
              </el-button>
            </div>
          </div>
        </template>
        <el-descriptions :column="3" border>
          <el-descriptions-item label="考试名称">{{ exam.examName }}</el-descriptions-item>
          <el-descriptions-item label="所属课程">{{ exam.courseName }}</el-descriptions-item>
          <el-descriptions-item label="关联班级">{{ exam.className || (exam.classId ? '班级信息缺失' : '全部班级') }}</el-descriptions-item>
          <el-descriptions-item label="开始时间">{{ exam.startTime }}</el-descriptions-item>
          <el-descriptions-item label="结束时间">{{ exam.endTime }}</el-descriptions-item>
          <el-descriptions-item label="考试时长">{{ exam.duration }} 分钟</el-descriptions-item>
          <el-descriptions-item label="总分"><span class="score-number">{{ exam.totalScore }} 分</span></el-descriptions-item>
          <el-descriptions-item label="及格分"><span class="score-number">{{ exam.passScore }} 分</span></el-descriptions-item>
          <el-descriptions-item label="题目数量">{{ examQuestions.length }} 题</el-descriptions-item>
          <el-descriptions-item label="考试说明" :span="3">{{ exam.description || '暂无说明' }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 题目列表 -->
      <el-card class="question-card">
        <template #header>
          <div class="card-header">
            <span>考试题目 ({{ examQuestions.length }} 题，共 {{ totalQuestionScore }} 分)</span>
          </div>
        </template>
        <el-table :data="examQuestions" stripe>
          <el-table-column type="index" label="序号" width="60" />
          <el-table-column prop="content" label="题目内容" min-width="300" show-overflow-tooltip />
          <el-table-column prop="typeName" label="题型" width="100" />
          <el-table-column prop="difficulty" label="难度" width="80">
            <template #default="{ row }">
              <el-tag :type="row.difficulty === 1 ? 'success' : row.difficulty === 2 ? 'warning' : 'danger'" size="small">
                {{ row.difficulty === 1 ? '简单' : row.difficulty === 2 ? '中等' : '困难' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="score" label="分值" width="80" />
          <el-table-column label="操作" width="100" v-if="userStore.isTeacher">
            <template #default="{ row }">
              <el-button type="danger" link size="small" @click="removeQuestion(row)">移除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="examQuestions.length === 0" description="暂未添加题目" />
      </el-card>

      <!-- 学生成绩统计（教师视图） -->
      <el-card class="result-card" v-show="userStore.isTeacher && (exam.status === 1 || exam.status === 2)">
        <template #header>
          <div class="card-header">
            <span>{{ exam.status === 1 ? '实时答题统计' : '成绩统计分析' }}</span>
            <el-button type="success" size="small" @click="exportResults" :disabled="exam.status === 1">
              <el-icon><Download /></el-icon>导出成绩
            </el-button>
          </div>
        </template>
        
        <!-- 统计概览 -->
        <div class="stats-overview">
          <div class="stat-item">
            <div class="stat-value">{{ statistics.totalCount || 0 }}</div>
            <div class="stat-label">应考人数</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ statistics.submitCount || 0 }}</div>
            <div class="stat-label">实考人数</div>
          </div>
          <div class="stat-item">
            <div class="stat-value highlight">{{ statistics.avgScore?.toFixed(1) || '-' }}</div>
            <div class="stat-label">平均分</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ statistics.maxScore || '-' }}</div>
            <div class="stat-label">最高分</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ statistics.minScore || '-' }}</div>
            <div class="stat-label">最低分</div>
          </div>
          <div class="stat-item">
            <div class="stat-value" :class="{ 'text-success': statistics.passRate >= 60 }">
              {{ statistics.passRate?.toFixed(1) || 0 }}%
            </div>
            <div class="stat-label">及格率</div>
          </div>
        </div>

        <!-- 分数分布图 -->
        <div class="score-distribution">
          <h4>分数分布</h4>
          <div class="distribution-chart">
            <div v-for="(count, range) in scoreDistribution" :key="range" class="distribution-bar">
              <div class="bar-label">{{ range }}</div>
              <div class="bar-container">
                <div class="bar-fill" :style="{ width: getBarWidth(count) + '%' }"></div>
              </div>
              <div class="bar-count">{{ count }}人</div>
            </div>
          </div>
        </div>

        <!-- 学生成绩列表 -->
        <div class="student-results">
          <h4>学生成绩明细</h4>
          <el-table :data="examRecords" stripe :row-key="row => row.studentId || row.recordId">
            <el-table-column prop="studentNo" label="学号" min-width="140" />
            <el-table-column prop="studentName" label="姓名" min-width="160" show-overflow-tooltip />
            <el-table-column prop="submitTime" label="提交时间" min-width="200" show-overflow-tooltip />
            <el-table-column prop="score" label="得分" min-width="100">
              <template #default="{ row }">
                <span :class="{ 'text-danger': row.score < exam.passScore, 'text-success': row.score >= exam.passScore }">
                  {{ row.score !== null ? row.score : '-' }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="状态" min-width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 0 ? 'info' : row.status === 1 ? 'warning' : 'success'" size="small">
                  {{ row.status === 0 ? '未参加' : row.status === 1 ? '答题中' : '已完成' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" min-width="100">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="viewStudentAnswer(row)" :disabled="row.status === 0">
                  查看答卷
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-card>

      <!-- 学生答题进度（学生视图） -->
      <el-card class="my-progress-card" v-if="userStore.isStudent && exam.status === 1 && myRecord.status === 1">
        <template #header>
          <span>答题进度</span>
        </template>
        <el-result icon="info" title="正在答题中">
          <template #extra>
            <div class="progress-info">
              <span>已用时：{{ myRecord.duration || 0 }} 分钟</span>
              <span>考试总时长：{{ exam.duration }} 分钟</span>
            </div>
            <el-button type="primary" @click="$router.push(`/exams/${examId}/take`)">
              继续答题
            </el-button>
          </template>
        </el-result>
      </el-card>

      <!-- 学生考试结果（学生视图） -->
      <el-card class="my-result-card" v-if="userStore.isStudent && myRecord.status === 2">
        <template #header>
          <span>我的成绩</span>
        </template>
        <el-result :icon="myRecord.score >= exam.passScore ? 'success' : 'warning'" 
          :title="myRecord.score >= exam.passScore ? '恭喜通过考试！' : '未达到及格线'">
          <template #extra>
            <div class="my-score">
              <span class="score-value">{{ myRecord.score }}</span>
              <span class="score-total">/ {{ exam.totalScore }}</span>
            </div>
            <div class="score-info">
              <span>提交时间：{{ myRecord.submitTime }}</span>
              <span>用时：{{ myRecord.duration || '-' }} 分钟</span>
            </div>
          </template>
        </el-result>
      </el-card>

      <!-- 学生入口 -->
      <el-card class="action-card" v-if="userStore.isStudent && exam.status === 1 && myRecord.status !== 2">
        <div class="exam-action">
          <el-icon :size="48" color="#409eff"><Document /></el-icon>
          <h3>{{ myRecord.status === 1 ? '继续考试' : '开始考试' }}</h3>
          <p>考试时长：{{ exam.duration }} 分钟</p>
          <el-button type="primary" size="large" @click="$router.push(`/exams/${examId}/take`)">
            {{ myRecord.status === 1 ? '继续答题' : '进入考试' }}
          </el-button>
        </div>
      </el-card>
    </div>

    <!-- 管理题目对话框 -->
    <el-dialog v-model="showQuestionDialog" title="管理考试题目" width="900px">
      <div class="question-manager">
        <div class="question-filter">
          <el-select v-model="questionFilter.typeId" placeholder="题型" clearable style="width: 120px">
            <el-option v-for="t in questionTypes" :key="t.id" :label="t.typeName" :value="t.id" />
          </el-select>
          <el-select v-model="questionFilter.difficulty" placeholder="难度" clearable style="width: 100px">
            <el-option label="简单" :value="1" />
            <el-option label="中等" :value="2" />
            <el-option label="困难" :value="3" />
          </el-select>
          <el-button type="primary" @click="fetchAvailableQuestions">搜索</el-button>
        </div>
        
        <el-table :data="availableQuestions" stripe max-height="400" @selection-change="handleQuestionSelect">
          <el-table-column type="selection" width="50" />
          <el-table-column prop="questionContent" label="题目内容" min-width="250" show-overflow-tooltip />
          <el-table-column prop="typeName" label="题型" width="80" />
          <el-table-column prop="difficulty" label="难度" width="70">
            <template #default="{ row }">
              {{ row.difficulty === 1 ? '简单' : row.difficulty === 2 ? '中等' : '困难' }}
            </template>
          </el-table-column>
          <el-table-column label="分值" width="100">
            <template #default="{ row }">
              <el-input-number v-model="row.score" :min="1" :max="100" size="small" style="width: 80px" />
            </template>
          </el-table-column>
        </el-table>
        
        <div class="selected-info">
          已选择 {{ selectedQuestions.length }} 题，共 {{ selectedTotalScore }} 分
        </div>
      </div>
      <template #footer>
        <el-button @click="showQuestionDialog = false">取消</el-button>
        <el-button type="primary" :loading="addQuestionLoading" @click="handleAddQuestions">添加到考试</el-button>
      </template>
    </el-dialog>

    <!-- 编辑考试对话框 -->
    <el-dialog v-model="showEditDialog" title="编辑考试" width="600px">
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="80px">
        <el-form-item label="考试名称" prop="examName">
          <el-input v-model="editForm.examName" placeholder="请输入考试名称" />
        </el-form-item>
        <el-form-item label="考试时间" prop="timeRange">
          <el-date-picker v-model="editForm.timeRange" type="datetimerange" start-placeholder="开始时间"
            end-placeholder="结束时间" style="width: 100%" />
        </el-form-item>
        <el-row :gutter="8">
          <el-col :span="8">
            <el-form-item label="考试时长" prop="duration">
              <el-input-number v-model="editForm.duration" :min="10" :max="300" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="总分">
              <el-input-number v-model="editForm.totalScore" :min="1" :max="200" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="及格分">
              <el-input-number v-model="editForm.passScore" :min="0" :max="200" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="考试说明">
          <el-input v-model="editForm.description" type="textarea" :rows="3" placeholder="考试说明（选填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" :loading="editLoading" @click="handleSaveEdit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 查看学生答卷对话框 -->
    <el-dialog v-model="showAnswerDialog" title="学生答卷" width="800px">
      <div class="student-answer-view">
        <el-descriptions :column="3" border style="margin-bottom: 20px">
          <el-descriptions-item label="学生">{{ currentStudent.studentName }}</el-descriptions-item>
          <el-descriptions-item label="学号">{{ currentStudent.studentNo }}</el-descriptions-item>
          <el-descriptions-item label="得分">{{ currentStudent.score }}</el-descriptions-item>
        </el-descriptions>
        <div v-for="(item, index) in currentStudent.answers" :key="index" class="answer-item">
          <div class="question-info">
            <span class="q-num">{{ index + 1 }}.</span>
            <span class="q-content">{{ item.questionContent }}</span>
            <span class="q-score">({{ item.questionScore }}分)</span>
          </div>
          <div class="answer-info">
            <div class="student-ans">
              <span class="label">学生答案：</span>
              <span :class="{ 'correct': item.isCorrect, 'wrong': !item.isCorrect }">{{ item.studentAnswer || '未作答' }}</span>
            </div>
            <div class="correct-ans">
              <span class="label">正确答案：</span>
              <span>{{ item.correctAnswer }}</span>
            </div>
            <div class="get-score">
              <span class="label">得分：</span>
              <span>{{ item.getScore }}</span>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getExamById, updateExam } from '@/api/exam'
import { getQuestionList, getAllQuestionTypes } from '@/api/question'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const route = useRoute()
const userStore = useUserStore()
const examId = route.params.id

const loading = ref(false)
const exam = ref({})
const examQuestions = ref([])
const examRecords = ref([])
const myRecord = ref({})
const statistics = ref({})

// 状态计算
const statusType = computed(() => {
  const s = exam.value.status
  return s === 0 ? 'info' : s === 1 ? 'success' : 'danger'
})
const statusText = computed(() => {
  const s = exam.value.status
  return s === 0 ? '未开始' : s === 1 ? '进行中' : '已结束'
})
const totalQuestionScore = computed(() => {
  return examQuestions.value.reduce((sum, q) => sum + (q.score || 0), 0)
})

// 分数分布
const scoreDistribution = computed(() => {
  const dist = { '0-59': 0, '60-69': 0, '70-79': 0, '80-89': 0, '90-100': 0 }
  examRecords.value.forEach(r => {
    if (r.score === null || r.score === undefined) return
    if (r.score < 60) dist['0-59']++
    else if (r.score < 70) dist['60-69']++
    else if (r.score < 80) dist['70-79']++
    else if (r.score < 90) dist['80-89']++
    else dist['90-100']++
  })
  return dist
})

const getBarWidth = (count) => {
  const max = Math.max(...Object.values(scoreDistribution.value), 1)
  return (count / max) * 100
}

// 题目管理
const showQuestionDialog = ref(false)
const questionTypes = ref([])
const availableQuestions = ref([])
const selectedQuestions = ref([])
const questionFilter = reactive({ typeId: '', difficulty: '' })
const addQuestionLoading = ref(false)

const selectedTotalScore = computed(() => {
  return selectedQuestions.value.reduce((sum, q) => sum + (q.score || 5), 0)
})

// 编辑考试
const showEditDialog = ref(false)
const editLoading = ref(false)
const editFormRef = ref()
const editForm = reactive({ examName: '', timeRange: [], duration: 90, totalScore: 100, passScore: 60, description: '' })
const editRules = {
  examName: [{ required: true, message: '请输入考试名称', trigger: 'blur' }],
  timeRange: [{ required: true, message: '请选择考试时间', trigger: 'change' }]
}

// 查看答卷
const showAnswerDialog = ref(false)
const currentStudent = ref({})

// 获取考试详情
const fetchExamDetail = async () => {
  // validate examId
  const id = Number(examId)
  if (!id || Number.isNaN(id)) {
    ElMessage.error('考试 ID 无效，无法获取详情')
    return
  }

  loading.value = true
  try {
    const res = await getExamById(id)
    exam.value = res.data || {}
  } catch (e) {
    console.error('fetchExamDetail error:', e)
    // show friendlier message; the request util may already show server message
    ElMessage.error(e.message || '获取考试详情失败，请联系管理员')
  } finally {
    loading.value = false
  }
}

// 获取考试题目
const fetchExamQuestions = async () => {
  try {
    const res = await request({ url: `/exams/${examId}/questions`, method: 'get' })
    examQuestions.value = res.data || []
  } catch (e) {
    console.error(e)
  }
}

// 获取考试记录（教师）
const fetchExamRecords = async () => {
  try {
    const res = await request({ url: `/exams/${examId}/records`, method: 'get' })
    // normalize records to expected field names for the table
    const rawRecords = res.data?.records || []
    examRecords.value = rawRecords.map(r => ({
      // try multiple possible backend field shapes
      studentId: r.studentId ?? r.student_id ?? r.studentId,
      studentNo: r.studentNo ?? r.student_no ?? r.studentNo ?? r.studentNo ?? '-',
      studentName: r.studentName ?? r.student_name ?? r.realName ?? r.studentName ?? '-',
      submitTime: r.submitTime ?? r.submit_time ?? r.submitTime ?? '-',
      score: r.score ?? null,
      status: typeof r.status !== 'undefined' ? r.status : (r.status ?? 0),
      // keep original record for any extra use
      __raw: r
    }))

    // normalize statistics fields returned by backend to frontend expected names
    const s = res.data?.statistics || {}
    const total = s.total ?? s.totalCount ?? 0
    const submitted = s.submitted ?? s.submitCount ?? 0
    statistics.value = {
      totalCount: total,
      submitCount: submitted,
      avgScore: s.avgScore ?? s.avg_score ?? null,
      maxScore: s.maxScore ?? s.max_score ?? null,
      minScore: s.minScore ?? s.min_score ?? null,
      passRate: s.passRate ? s.passRate: 0
    }
  } catch (e) {
    console.error(e)
  }
}

// 实时轮询教师端的考试记录（进行中时）
let recordsTimer = null
watch(() => exam.value.status, (s) => {
  // 如果考试进行中且是教师视图，开启轮询
  if (userStore.isTeacher && s === 1) {
    fetchExamRecords()
    if (!recordsTimer) {
      recordsTimer = setInterval(fetchExamRecords, 10000)
    }
  } else {
    if (recordsTimer) {
      clearInterval(recordsTimer)
      recordsTimer = null
    }
  }
})

onUnmounted(() => {
  if (recordsTimer) {
    clearInterval(recordsTimer)
    recordsTimer = null
  }
})

// 获取我的考试记录（学生）
const fetchMyRecord = async () => {
  try {
    const res = await request({ url: `/exams/${examId}/my-record`, method: 'get' })
    myRecord.value = res.data || {}
  } catch (e) {
    console.error(e)
  }
}

// 获取题型列表
const fetchQuestionTypes = async () => {
  try {
    const res = await getAllQuestionTypes()
    questionTypes.value = res.data || []
  } catch (e) {
    console.error(e)
  }
}

// 获取可用题目
const fetchAvailableQuestions = async () => {
  try {
    const res = await getQuestionList({ 
      courseId: exam.value.courseId, 
      questionTypeId: questionFilter.typeId,
      difficulty: questionFilter.difficulty,
      pageNum: 1, 
      pageSize: 100 
    })
    availableQuestions.value = (res.data?.records || []).map(q => ({ ...q, score: q.score || 5 }))
  } catch (e) {
    console.error(e)
  }
}

// 当管理题目对话框打开时自动加载可用题目（确保有 courseId）
watch(showQuestionDialog, (visible) => {
  if (visible) {
    if (!exam.value.courseId) {
      // 如果考试详情尚未加载，先获取后再加载题目
      fetchExamDetail().then(() => {
        fetchAvailableQuestions()
      })
    } else {
      fetchAvailableQuestions()
    }
  }
})

// 选择题目
const handleQuestionSelect = (rows) => {
  selectedQuestions.value = rows
}

// 添加题目到考试
const handleAddQuestions = async () => {
  if (selectedQuestions.value.length === 0) {
    ElMessage.warning('请选择题目')
    return
  }
  addQuestionLoading.value = true
  try {
    await request({
      url: `/exams/${examId}/questions`,
      method: 'post',
      data: selectedQuestions.value.map(q => ({ questionId: q.id, score: q.score }))
    })
    ElMessage.success('添加成功')
    showQuestionDialog.value = false
    // refresh questions list and exam info (total score / pass score)
    fetchExamQuestions()
    fetchExamDetail()
  } finally {
    addQuestionLoading.value = false
  }
}

// 移除题目
const removeQuestion = async (row) => {
  try {
    await request({ url: `/exams/${examId}/questions/${row.id}`, method: 'delete' })
    ElMessage.success('移除成功')
    // refresh questions list and exam info (total score / pass score)
    fetchExamQuestions()
    fetchExamDetail()
  } catch (e) {
    console.error(e)
  }
}

// 编辑考试
const handleEdit = () => {
  Object.assign(editForm, {
    examName: exam.value.examName,
    timeRange: [exam.value.startTime, exam.value.endTime],
    duration: exam.value.duration,
    totalScore: exam.value.totalScore,
    passScore: exam.value.passScore,
    description: exam.value.description
  })
  showEditDialog.value = true
}

const handleSaveEdit = async () => {
  await editFormRef.value?.validate(async (valid) => {
    if (valid) {
      editLoading.value = true
      try {
        await updateExam(examId, {
          ...editForm,
          startTime: editForm.timeRange[0],
          endTime: editForm.timeRange[1]
        })
        ElMessage.success('保存成功')
        showEditDialog.value = false
        fetchExamDetail()
      } finally {
        editLoading.value = false
      }
    }
  })
}

// 查看学生答卷
const viewStudentAnswer = async (row) => {
  try {
    const res = await request({ url: `/exams/${examId}/records/${row.studentId}`, method: 'get' })
    currentStudent.value = { ...row, answers: res.data?.answers || [] }
    showAnswerDialog.value = true
  } catch (e) {
    console.error(e)
  }
}

// 导出成绩
const exportResults = () => {
  ElMessage.info('导出功能开发中')
}

onMounted(() => {
  fetchExamDetail()
  fetchExamQuestions()
  fetchQuestionTypes()
  if (userStore.isTeacher) {
    fetchExamRecords()
  }
})
</script>

<style lang="scss" scoped>
.exam-detail {
  .detail-content {
    margin-top: 20px;
  }
  
  .question-card, .result-card, .my-result-card, .my-progress-card, .action-card {
    margin-bottom: 20px;
  }
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .header-actions {
      display: flex;
      gap: 8px;
    }
  }
  
  .info-card {
      .el-descriptions {
      /* responsive grid: each item min 140px, expand as space allows */
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
      gap: 8px 12px;
      font-size: 16px;

      .el-descriptions__item {
        padding: 8px 12px;
        box-sizing: border-box;
      }

      .el-descriptions__label {
        font-size: 14px;
        color: #606266;
        padding-right: 8px;
        display: inline-block;
        vertical-align: middle;
      }

      .el-descriptions__content {
        /* allow wrapping inside grid cell; align numbers to right */
        white-space: normal;
        overflow: hidden;
        text-overflow: ellipsis;
        text-align: right;
        font-size: 15px;
      }

      .score-number {
        display: inline-block;
        min-width: 40px;
        max-width: 100%;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        text-align: right;
        font-weight: 700;
        font-size: 16px;
        color: #409eff;
      }
    }
  }
  
  // 统计概览
  .stats-overview {
    display: flex;
    justify-content: space-around;
    padding: 20px 0;
    border-bottom: 1px solid #f0f0f0;
    margin-bottom: 20px;
    
    .stat-item {
      text-align: center;
      
      .stat-value {
        font-size: 32px;
        font-weight: 600;
        color: #303133;
        
        &.highlight {
          color: #409eff;
        }
        
        &.text-success {
          color: #67c23a;
        }
      }
      
      .stat-label {
        font-size: 14px;
        color: #909399;
        margin-top: 4px;
      }
    }
  }
  
  // 分数分布
  .score-distribution {
    margin-bottom: 20px;
    
    h4 {
      margin: 0 0 16px;
      font-size: 15px;
      color: #303133;
    }
    
    .distribution-bar {
      display: flex;
      align-items: center;
      margin-bottom: 8px;
      
      .bar-label {
        width: 60px;
        font-size: 13px;
        color: #606266;
      }
      
      .bar-container {
        flex: 1;
        height: 20px;
        background: #f5f7fa;
        border-radius: 4px;
        overflow: hidden;
        
        .bar-fill {
          height: 100%;
          background: linear-gradient(90deg, #409eff, #67c23a);
          border-radius: 4px;
          transition: width 0.3s;
        }
      }
      
      .bar-count {
        width: 50px;
        text-align: right;
        font-size: 13px;
        color: #909399;
      }
    }
  }
  
  .student-results h4 {
    margin: 0 0 16px;
    font-size: 15px;
    color: #303133;
  }
  
  .text-danger { color: #f56c6c; }
  .text-success { color: #67c23a; }
  
  // 学生成绩
  .my-score {
    text-align: center;
    margin-bottom: 16px;
    
    .score-value {
      font-size: 56px;
      font-weight: 600;
      color: #409eff;
    }
    
    .score-total {
      font-size: 24px;
      color: #909399;
    }
  }
  
  .score-info {
    display: flex;
    justify-content: center;
    gap: 24px;
    color: #909399;
    font-size: 14px;
  }

  .progress-info {
    display: flex;
    justify-content: center;
    gap: 24px;
    color: #909399;
    font-size: 14px;
    margin-bottom: 16px;
  }
  
  // 考试入口
  .exam-action {
    text-align: center;
    padding: 40px;
    
    h3 {
      margin: 16px 0 8px;
      font-size: 20px;
      color: #303133;
    }
    
    p {
      margin: 0 0 20px;
      color: #909399;
    }
  }
  
  // 题目管理
  .question-manager {
    .question-filter {
      display: flex;
      gap: 12px;
      margin-bottom: 16px;
    }
    
    .selected-info {
      margin-top: 12px;
      text-align: right;
      color: #409eff;
      font-weight: 500;
    }
  }
  
  // 答卷查看
  .student-answer-view {
    .answer-item {
      padding: 16px;
      background: #f5f7fa;
      border-radius: 8px;
      margin-bottom: 12px;
      
      .question-info {
        margin-bottom: 12px;
        
        .q-num {
          font-weight: 600;
          margin-right: 4px;
        }
        
        .q-score {
          color: #909399;
          margin-left: 8px;
        }
      }
      
      .answer-info {
        padding-left: 20px;
        font-size: 14px;
        
        > div {
          margin-bottom: 4px;
        }
        
        .label {
          color: #909399;
        }
        
        .correct {
          color: #67c23a;
        }
        
        .wrong {
          color: #f56c6c;
        }
      }
    }
  
  // ensure number inputs don't overflow in edit dialog
  .el-input-number {
    width: 100%;
    min-width: 0;
    box-sizing: border-box;
  }
  }
}
</style>
