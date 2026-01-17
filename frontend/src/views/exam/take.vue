<template>
  <div class="take-exam">
    <div class="exam-header">
      <h2>{{ exam.examName }}</h2>
      <div class="timer">
        <el-icon><Clock /></el-icon>
        剩余时间：{{ formatTime(remainingTime) }}
      </div>
    </div>
    <div class="exam-content" v-loading="loading">
      <div class="question-list">
        <div v-for="(q, index) in questions" :key="q.id" class="question-item">
          <div class="question-title">
            <span class="question-num">{{ index + 1 }}.</span>
            <span class="question-type">[{{ q.typeName }}]</span>
            {{ q.content }}
            <span class="question-score">({{ q.score }}分)</span>
          </div>
          <div class="question-options" v-if="q.type === 'SINGLE' || q.type === 'MULTIPLE' || q.type === 'TRUE_FALSE'">
            <el-radio-group v-if="q.type === 'SINGLE'" v-model="answers[q.id]">
              <el-radio label="A">A</el-radio>
              <el-radio label="B">B</el-radio>
              <el-radio label="C">C</el-radio><
              <el-radio label="D">D</el-radio>
            </el-radio-group>
            <el-checkbox-group v-else-if="q.type === 'MULTIPLE'" v-model="answers[q.id]">
              <el-checkbox label="A">A</el-checkbox>
              <el-checkbox label="B">B</el-checkbox>
              <el-checkbox label="C">C</el-checkbox>
              <el-checkbox label="D">D</el-checkbox>
            </el-checkbox-group>
            <el-radio-group v-else-if="q.type === 'TRUE_FALSE'" v-model="answers[q.id]">
              <el-radio label="A">A. 正确</el-radio>
              <el-radio label="B">B. 错误</el-radio>
            </el-radio-group>
          </div>
          <div class="question-answer" v-else>
            <el-input v-model="answers[q.id]" type="textarea" :rows="4" placeholder="请输入答案" />
          </div>
        </div>
      </div>
      <el-empty v-if="!loading && questions.length === 0" description="暂无题目" />
    </div>
    <div class="exam-footer">
      <el-button type="primary" size="large" @click="handleSubmit" :loading="submitLoading">提交试卷</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { startExam, submitExam, testApi } from '@/api/exam'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const submitLoading = ref(false)
const exam = ref({})
const questions = ref([])
const answers = reactive({})
const remainingTime = ref(0)
let timer = null

const formatTime = (seconds) => {
  const h = Math.floor(seconds / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  const s = seconds % 60
  return `${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`
}

const startTimer = () => {
  timer = setInterval(() => {
    if (remainingTime.value > 0) {
      remainingTime.value--
    } else {
      clearInterval(timer)
      handleSubmit()
    }
  }, 1000)
}

const handleSubmit = () => {
  ElMessageBox.confirm('确定要提交试卷吗？', '提示', { type: 'warning' }).then(async () => {
    submitLoading.value = true
    try {
      // 格式化答案数据
      const formattedAnswers = {}
      for (const [questionId, answer] of Object.entries(answers)) {
        if (Array.isArray(answer)) {
          // 多选题答案用逗号连接
          formattedAnswers[questionId] = answer.sort().join(',')
        } else {
          formattedAnswers[questionId] = answer || ''
        }
      }

      await submitExam(route.params.id, { answers: formattedAnswers })
      ElMessage.success('提交成功')
      router.push(`/exams/${route.params.id}`)
    } finally {
      submitLoading.value = false
    }
  })
}

onMounted(async () => {
  loading.value = true
  try {
    // 首先测试基本API连接
    console.log('测试API连接...')
    const testRes = await testApi()
    console.log('API测试成功:', testRes)

    console.log('开始调用 startExam API, 考试ID:', route.params.id)
    const res = await startExam(route.params.id)
    console.log('API 响应成功，完整响应:', res)
    console.log('响应数据类型:', typeof res)
    console.log('响应数据结构:', Object.keys(res))

    if (!res || typeof res !== 'object') {
      console.error('响应数据异常')
      return
    }

    console.log('res.data 存在:', !!res.data)
    if (res.data) {
      console.log('res.data 类型:', typeof res.data)
      console.log('res.data 结构:', Object.keys(res.data))
    }

    exam.value = res.data?.exam || {}
    questions.value = res.data?.questions || []

    console.log('设置的考试信息:', exam.value)
    console.log('设置的题目列表长度:', questions.value.length)

    remainingTime.value = (res.data?.remainingTime || exam.value.duration * 60)
    console.log('设置的剩余时间:', remainingTime.value)

    questions.value.forEach((q, index) => {
      console.log(`题目 ${index + 1}:`, {
        id: q.id,
        type: q.type,
        content: q.content?.substring(0, 50) + '...'
      })

      if (q.type === 'MULTIPLE') {
        answers[q.id] = []
      } else {
        answers[q.id] = ''
      }
    })

    console.log('答案初始化完成，答案数量:', Object.keys(answers).length)
    startTimer()
    console.log('考试页面初始化完成')
  } catch (error) {
    console.error('考试页面初始化失败:', error)
    console.error('错误详情:', {
      message: error.message,
      stack: error.stack
    })
    ElMessage.error('加载考试失败: ' + error.message)
  } finally {
    loading.value = false
  }
})

onUnmounted(() => { if (timer) clearInterval(timer) })
</script>

<style lang="scss" scoped>
.take-exam {
  .exam-header {
    display: flex; justify-content: space-between; align-items: center;
    padding: 20px; background: #fff; border-radius: 12px; margin-bottom: 20px;
    h2 { margin: 0; font-size: 20px; }
    .timer { font-size: 18px; color: #f56c6c; font-weight: 600; display: flex; align-items: center; gap: 8px; }
  }
  .exam-content { background: #fff; border-radius: 12px; padding: 20px; min-height: 400px; }
  .question-item {
    padding: 20px 0; border-bottom: 1px solid #f0f0f0;
    &:last-child { border-bottom: none; }
    .question-title { font-size: 15px; margin-bottom: 16px; line-height: 1.6; }
    .question-num { font-weight: 600; margin-right: 4px; }
    .question-type { color: #409eff; margin-right: 8px; }
    .question-score { color: #999; margin-left: 8px; }
    .question-options { padding-left: 20px; }
    .el-radio, .el-checkbox { display: block; margin: 10px 0; }
  }
  .exam-footer { margin-top: 20px; text-align: center; }
}
</style>
