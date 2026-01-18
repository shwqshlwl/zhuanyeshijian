<template>
  <div class="experiment-answer-container">
    <el-page-header @back="$router.back()" title="返回">
      <template #content>
        <span class="page-title">{{ experiment.experimentName || '实验答题' }}</span>
        <el-tag :type="statusType" style="margin-left: 12px">{{ statusText }}</el-tag>
      </template>
    </el-page-header>

    <div class="content-wrapper" v-loading="loading">
      <!-- 左侧：题目描述 -->
      <div class="left-panel">
        <el-card class="info-card">
          <template #header>
            <div class="card-header">
              <el-icon><Document /></el-icon>
              <span>实验要求</span>
            </div>
          </template>

          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="编程语言">
              <el-tag size="small">{{ languageMap[experiment.language] }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="总分">{{ experiment.totalScore || 100 }} 分</el-descriptions-item>
            <el-descriptions-item label="时间限制">{{ experiment.timeLimit || 5000 }} ms</el-descriptions-item>
            <el-descriptions-item label="内存限制">{{ experiment.memoryLimit || 256 }} MB</el-descriptions-item>
            <el-descriptions-item label="截止时间" :span="2">
              {{ formatTime(experiment.endTime) }}
            </el-descriptions-item>
          </el-descriptions>

          <div class="description-section">
            <div class="section-title">
              <el-icon><Reading /></el-icon>
              <span>实验描述</span>
            </div>
            <div class="section-content">{{ experiment.description || '暂无描述' }}</div>
          </div>

          <div class="description-section">
            <div class="section-title">
              <el-icon><List /></el-icon>
              <span>实验要求</span>
            </div>
            <div class="section-content">{{ experiment.requirement || '暂无要求' }}</div>
          </div>

          <!-- 示例（如果有） -->
          <div class="description-section" v-if="examples.length > 0">
            <div class="section-title">
              <el-icon><Memo /></el-icon>
              <span>输入输出示例</span>
            </div>
            <div v-for="(example, index) in examples" :key="index" class="example-item">
              <div class="example-title">示例 {{ index + 1 }}</div>
              <div class="example-row">
                <span class="example-label">输入：</span>
                <pre class="example-content">{{ example.input }}</pre>
              </div>
              <div class="example-row">
                <span class="example-label">输出：</span>
                <pre class="example-content">{{ example.output }}</pre>
              </div>
            </div>
          </div>

          <!-- 我的提交历史 -->
          <div class="description-section" v-if="mySubmissions.length > 0">
            <div class="section-title">
              <el-icon><Clock /></el-icon>
              <span>我的提交历史</span>
            </div>
            <el-timeline>
              <el-timeline-item 
                v-for="(item, index) in mySubmissions.slice(0, 5)" 
                :key="index"
                :type="getTimelineType(item.status)"
                :timestamp="formatTime(item.submitTime)"
                placement="top"
              >
                <div class="history-item">
                  <el-tag :type="getStatusTagType(item.status)" size="small">
                    {{ getStatusText(item.status) }}
                  </el-tag>
                  <span class="history-score">得分: {{ item.score || 0 }}</span>
                  <span class="history-pass">
                    通过: {{ item.passCount || 0 }}/{{ item.totalCount || 0 }}
                  </span>
                  <el-button type="primary" link size="small" @click="loadHistoryCode(item)">
                    加载代码
                  </el-button>
                </div>
              </el-timeline-item>
            </el-timeline>
          </div>
        </el-card>
      </div>

      <!-- 右侧：代码编辑器 -->
      <div class="right-panel">
        <el-card class="code-card">
          <template #header>
            <div class="card-header">
              <div class="header-left">
                <el-icon><Edit /></el-icon>
                <span>代码编辑器</span>
              </div>
              <div class="header-right">
                <el-select v-model="selectedLanguage" size="small" style="width: 120px; margin-right: 8px">
                  <el-option label="Java" value="java" />
                  <el-option label="Python" value="python" />
                  <el-option label="C++" value="cpp" />
                  <el-option label="C" value="c" />
                  <el-option label="JavaScript" value="javascript" />
                </el-select>
                <el-button size="small" @click="resetCode">
                  <el-icon><RefreshRight /></el-icon>重置
                </el-button>
                <el-button size="small" @click="fullscreen = !fullscreen">
                  <el-icon><FullScreen /></el-icon>{{ fullscreen ? '退出全屏' : '全屏' }}
                </el-button>
              </div>
            </div>
          </template>

          <div class="code-editor-wrapper" :class="{ fullscreen }">
            <div class="line-numbers">
              <div v-for="n in lineCount" :key="n" class="line-number">{{ n }}</div>
            </div>
            <textarea
              ref="codeTextarea"
              v-model="code"
              class="code-textarea"
              :placeholder="getPlaceholder()"
              spellcheck="false"
              @keydown="handleKeydown"
              @scroll="syncScroll"
            ></textarea>
          </div>

          <div class="code-actions">
            <el-button 
              type="primary" 
              size="large"
              :loading="runLoading" 
              @click="handleRun"
            >
              <el-icon><VideoPlay /></el-icon>运行测试
            </el-button>
            <el-button 
              type="success" 
              size="large"
              :loading="submitLoading" 
              @click="handleSubmit"
              :disabled="experiment.status === 2"
            >
              <el-icon><Upload /></el-icon>提交代码
            </el-button>
            <el-button size="large" @click="handleSaveDraft">
              <el-icon><Document /></el-icon>保存草稿
            </el-button>
            <span v-if="experiment.status === 2" class="deadline-tip">
              <el-icon><WarningFilled /></el-icon>实验已截止
            </span>
          </div>

          <!-- 运行结果 -->
          <div class="run-result" v-if="runResult">
            <div class="result-header">
              <el-icon><Monitor /></el-icon>
              <span>运行结果</span>
            </div>
            <el-alert 
              :type="runResult.success ? 'success' : 'error'" 
              :title="runResult.success ? '运行成功' : '运行失败'" 
              show-icon 
              :closable="false"
            />
            <div class="output-section" v-if="runResult.output">
              <div class="output-label">输出结果：</div>
              <pre class="output-content">{{ runResult.output }}</pre>
            </div>
            <div class="error-section" v-if="runResult.error">
              <div class="error-label">错误信息：</div>
              <pre class="error-content">{{ runResult.error }}</pre>
            </div>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getExperimentById } from '@/api/experiment'
import request from '@/utils/request'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()
const experimentId = route.params.id

const loading = ref(false)
const experiment = ref({})
const code = ref('')
const selectedLanguage = ref('java')
const codeTextarea = ref(null)
const fullscreen = ref(false)

const runLoading = ref(false)
const submitLoading = ref(false)
const runResult = ref(null)
const mySubmissions = ref([])
const examples = ref([])

const languageMap = {
  java: 'Java',
  python: 'Python',
  cpp: 'C++',
  c: 'C',
  javascript: 'JavaScript'
}

const statusType = computed(() => {
  const s = experiment.value.status
  return s === 0 ? 'info' : s === 1 ? 'success' : 'danger'
})

const statusText = computed(() => {
  const s = experiment.value.status
  return s === 0 ? '未开始' : s === 1 ? '进行中' : '已截止'
})

const lineCount = computed(() => {
  return Math.max(code.value.split('\n').length, 30)
})

const getPlaceholder = () => {
  const templates = {
    java: 'public class Main {\n    public static void main(String[] args) {\n        // 在此编写代码\n    }\n}',
    python: '# 在此编写代码\n',
    cpp: '#include <iostream>\nusing namespace std;\n\nint main() {\n    // 在此编写代码\n    return 0;\n}',
    c: '#include <stdio.h>\n\nint main() {\n    // 在此编写代码\n    return 0;\n}',
    javascript: '// 在此编写代码\n'
  }
  return templates[selectedLanguage.value] || '// 在此编写代码'
}

const formatTime = (time) => {
  return time ? dayjs(time).format('YYYY-MM-DD HH:mm:ss') : '-'
}

const getStatusText = (status) => {
  const map = { 0: '待评测', 1: '评测中', 2: '通过', 3: '未通过', 4: '编译错误', 5: '运行错误' }
  return map[status] || '未知'
}

const getStatusTagType = (status) => {
  if (status === 2) return 'success'
  if (status === 3 || status === 4 || status === 5) return 'danger'
  if (status === 1) return 'warning'
  return 'info'
}

const getTimelineType = (status) => {
  if (status === 2) return 'success'
  if (status === 3 || status === 4 || status === 5) return 'danger'
  return 'primary'
}

const handleKeydown = (e) => {
  if (e.key === 'Tab') {
    e.preventDefault()
    const start = e.target.selectionStart
    const end = e.target.selectionEnd
    code.value = code.value.substring(0, start) + '    ' + code.value.substring(end)
    setTimeout(() => {
      e.target.selectionStart = e.target.selectionEnd = start + 4
    }, 0)
  }
}

const syncScroll = (e) => {
  const lineNumbers = document.querySelector('.line-numbers')
  if (lineNumbers) {
    lineNumbers.scrollTop = e.target.scrollTop
  }
}

const resetCode = () => {
  ElMessageBox.confirm('确定要重置代码吗？当前代码将被清空', '提示', {
    type: 'warning'
  }).then(() => {
    code.value = experiment.value.templateCode || ''
    ElMessage.success('已重置代码')
  }).catch(() => {})
}

const fetchExperiment = async () => {
  loading.value = true
  try {
    const res = await getExperimentById(experimentId)
    experiment.value = res.data || {}
    selectedLanguage.value = experiment.value.language || 'java'
    
    if (experiment.value.templateCode) {
      code.value = experiment.value.templateCode
    }

    // 解析示例（从测试用例中取前2个作为示例）
    if (experiment.value.testCases) {
      try {
        const testCases = JSON.parse(experiment.value.testCases)
        examples.value = testCases.slice(0, 2)
      } catch (e) {
        console.error('解析测试用例失败', e)
      }
    }
  } finally {
    loading.value = false
  }
}

const fetchMySubmissions = async () => {
  try {
    const res = await request({ url: `/experiments/${experimentId}/my-submissions`, method: 'get' })
    mySubmissions.value = res.data || []
    
    // 如果有提交记录，加载最新的代码
    if (mySubmissions.value.length > 0 && mySubmissions.value[0].code) {
      code.value = mySubmissions.value[0].code
    }
  } catch (e) {
    console.error(e)
  }
}

const handleRun = async () => {
  if (!code.value.trim()) {
    ElMessage.warning('请输入代码')
    return
  }
  
  runLoading.value = true
  runResult.value = null
  
  try {
    const res = await request({
      url: `/experiments/${experimentId}/run`,
      method: 'post',
      data: { code: code.value, language: selectedLanguage.value }
    })
    runResult.value = res.data
  } catch (e) {
    runResult.value = { success: false, error: e.message || '运行失败' }
  } finally {
    runLoading.value = false
  }
}

const handleSubmit = async () => {
  if (!code.value.trim()) {
    ElMessage.warning('请输入代码')
    return
  }

  ElMessageBox.confirm('确定要提交代码吗？提交后将进行自动评测', '提示', {
    type: 'warning',
    confirmButtonText: '确定提交',
    cancelButtonText: '取消'
  }).then(async () => {
    submitLoading.value = true
    try {
      await request({
        url: `/experiments/${experimentId}/submit`,
        method: 'post',
        data: { code: code.value, language: selectedLanguage.value }
      })
      ElMessage.success('提交成功，正在评测中...')
      
      // 跳转到评测结果页
      setTimeout(() => {
        router.push(`/experiments/${experimentId}/result`)
      }, 1500)
    } catch (e) {
      ElMessage.error(e.message || '提交失败')
    } finally {
      submitLoading.value = false
    }
  }).catch(() => {})
}

const handleSaveDraft = () => {
  localStorage.setItem(`experiment_${experimentId}_code`, code.value)
  localStorage.setItem(`experiment_${experimentId}_language`, selectedLanguage.value)
  ElMessage.success('草稿已保存')
}

const loadHistoryCode = (item) => {
  code.value = item.code || ''
  selectedLanguage.value = item.language || selectedLanguage.value
  ElMessage.success('已加载历史代码')
}

// 加载草稿
const loadDraft = () => {
  const draftCode = localStorage.getItem(`experiment_${experimentId}_code`)
  const draftLanguage = localStorage.getItem(`experiment_${experimentId}_language`)
  
  if (draftCode && !code.value) {
    ElMessageBox.confirm('检测到未提交的草稿，是否加载？', '提示', {
      type: 'info'
    }).then(() => {
      code.value = draftCode
      if (draftLanguage) selectedLanguage.value = draftLanguage
      ElMessage.success('已加载草稿')
    }).catch(() => {})
  }
}

onMounted(() => {
  fetchExperiment()
  fetchMySubmissions()
  setTimeout(loadDraft, 500)
})
</script>

<style lang="scss" scoped>
.experiment-answer-container {
  .page-title {
    font-size: 20px;
    font-weight: 600;
  }

  .content-wrapper {
    display: flex;
    gap: 20px;
    margin-top: 20px;
    height: calc(100vh - 140px);

    .left-panel {
      width: 400px;
      overflow-y: auto;

      .info-card {
        height: 100%;
      }
    }

    .right-panel {
      flex: 1;
      overflow-y: auto;

      .code-card {
        height: 100%;
      }
    }
  }

  .card-header {
    display: flex;
    align-items: center;
    gap: 8px;
    font-weight: 600;

    .header-left {
      display: flex;
      align-items: center;
      gap: 8px;
      flex: 1;
    }

    .header-right {
      display: flex;
      align-items: center;
      gap: 8px;
    }
  }

  .description-section {
    margin-top: 20px;
    padding-top: 20px;
    border-top: 1px solid #ebeef5;

    &:first-child {
      margin-top: 16px;
      padding-top: 16px;
    }

    .section-title {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 15px;
      font-weight: 600;
      color: #303133;
      margin-bottom: 12px;
    }

    .section-content {
      white-space: pre-wrap;
      line-height: 1.8;
      color: #606266;
    }
  }

  .example-item {
    margin-bottom: 16px;
    padding: 12px;
    background: #f5f7fa;
    border-radius: 4px;

    .example-title {
      font-weight: 600;
      margin-bottom: 8px;
      color: #303133;
    }

    .example-row {
      margin-bottom: 8px;

      .example-label {
        font-size: 13px;
        color: #909399;
      }

      .example-content {
        margin: 4px 0 0 0;
        padding: 8px;
        background: #fff;
        border-radius: 4px;
        font-family: monospace;
        font-size: 13px;
      }
    }
  }

  .history-item {
    display: flex;
    align-items: center;
    gap: 12px;

    .history-score, .history-pass {
      font-size: 13px;
      color: #606266;
    }
  }

  .code-editor-wrapper {
    display: flex;
    border: 1px solid #dcdfe6;
    border-radius: 4px;
    overflow: hidden;
    background: #1e1e1e;
    min-height: 500px;

    &.fullscreen {
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      z-index: 9999;
      border-radius: 0;
      min-height: 100vh;
    }

    .line-numbers {
      width: 50px;
      padding: 12px 8px;
      background: #252526;
      color: #858585;
      font-family: 'Consolas', 'Monaco', monospace;
      font-size: 14px;
      line-height: 1.5;
      text-align: right;
      user-select: none;
      overflow: hidden;

      .line-number {
        height: 21px;
      }
    }

    .code-textarea {
      flex: 1;
      padding: 12px;
      border: none;
      outline: none;
      resize: none;
      background: #1e1e1e;
      color: #d4d4d4;
      font-family: 'Consolas', 'Monaco', monospace;
      font-size: 14px;
      line-height: 1.5;
      tab-size: 4;

      &::placeholder {
        color: #6a6a6a;
      }
    }
  }

  .code-actions {
    margin-top: 16px;
    display: flex;
    align-items: center;
    gap: 12px;

    .deadline-tip {
      display: flex;
      align-items: center;
      gap: 4px;
      color: #f56c6c;
      font-size: 13px;
    }
  }

  .run-result {
    margin-top: 20px;
    padding: 16px;
    background: #f5f7fa;
    border-radius: 8px;

    .result-header {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 15px;
      font-weight: 600;
      margin-bottom: 12px;
    }

    .output-section, .error-section {
      margin-top: 12px;

      .output-label, .error-label {
        font-size: 13px;
        color: #606266;
        margin-bottom: 8px;
      }

      .output-content {
        margin: 0;
        padding: 12px;
        background: #fff;
        border-radius: 4px;
        font-family: monospace;
        font-size: 13px;
        white-space: pre-wrap;
      }

      .error-content {
        margin: 0;
        padding: 12px;
        background: #fef0f0;
        border-radius: 4px;
        color: #f56c6c;
        font-family: monospace;
        font-size: 13px;
        white-space: pre-wrap;
      }
    }
  }
}
</style>
