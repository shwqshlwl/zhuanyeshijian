<template>
  <div class="take-exam">
    <div class="exam-header">
      <h2>{{ exam.title }}</h2>
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
          <div class="question-options" v-if="q.type === 'SINGLE' || q.type === 'MULTIPLE'">
            <el-radio-group v-if="q.type === 'SINGLE'" v-model="answers[q.id]">
              <el-radio v-for="opt in q.options" :key="opt.key" :label="opt.key">{{ opt.key }}. {{ opt.value }}</el-radio>
            </el-radio-group>
            <el-checkbox-group v-else v-model="answers[q.id]">
              <el-checkbox v-for="opt in q.options" :key="opt.key" :label="opt.key">{{ opt.key }}. {{ opt.value }}</el-checkbox>
            </el-checkbox-group>
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
import { startExam, submitExam } from '@/api/exam'
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
      await submitExam(route.params.id, { answers })
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
    const res = await startExam(route.params.id)
    exam.value = res.data?.exam || {}
    questions.value = res.data?.questions || []
    remainingTime.value = (res.data?.remainingTime || exam.value.duration * 60)
    questions.value.forEach(q => { answers[q.id] = q.type === 'MULTIPLE' ? [] : '' })
    startTimer()
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
