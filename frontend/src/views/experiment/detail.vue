<template>
  <div class="experiment-detail">
    <el-page-header @back="$router.back()">
      <template #content>
        <span>{{ experiment.experimentName || '实验详情' }}</span>
        <el-tag :type="statusType" style="margin-left: 12px">{{ statusText }}</el-tag>
      </template>
    </el-page-header>

    <div class="detail-content" v-loading="loading">
      <!-- 基本信息 -->
      <el-card class="info-card">
        <template #header>
          <div class="card-header">
            <span>实验信息</span>
            <el-button type="primary" size="small" @click="handleEdit" v-if="userStore.isTeacher">
              <el-icon><Edit /></el-icon>编辑
            </el-button>
          </div>
        </template>
        <el-descriptions :column="3" border>
          <el-descriptions-item label="实验名称">{{ experiment.experimentName }}</el-descriptions-item>
          <el-descriptions-item label="所属课程">{{ experiment.courseName }}</el-descriptions-item>
          <el-descriptions-item label="编程语言">
            <el-tag>{{ languageMap[experiment.language] || experiment.language }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="开始时间">{{ experiment.startTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="截止时间">{{ experiment.endTime }}</el-descriptions-item>
          <el-descriptions-item label="总分">{{ experiment.totalScore || 100 }} 分</el-descriptions-item>
          <el-descriptions-item label="时间限制">{{ experiment.timeLimit || 1000 }} ms</el-descriptions-item>
          <el-descriptions-item label="内存限制">{{ experiment.memoryLimit || 128 }} MB</el-descriptions-item>
          <el-descriptions-item label="提交情况" v-if="userStore.isTeacher">
            {{ experiment.submitCount || 0 }} / {{ experiment.totalCount || 0 }}
          </el-descriptions-item>
          <el-descriptions-item label="实验描述" :span="3">
            <div class="experiment-desc">{{ experiment.description || '暂无描述' }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="实验要求" :span="3">
            <div class="experiment-requirement">{{ experiment.requirement || '暂无要求' }}</div>
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 学生代码编辑区域 -->
      <el-card class="code-card" v-if="userStore.isStudent">
        <template #header>
          <div class="card-header">
            <span>代码编辑器</span>
            <div class="header-actions">
              <el-select v-model="selectedLanguage" size="small" style="width: 120px">
                <el-option label="Java" value="java" />
                <el-option label="Python" value="python" />
                <el-option label="C++" value="cpp" />
                <el-option label="C" value="c" />
                <el-option label="JavaScript" value="javascript" />
              </el-select>
              <el-button size="small" @click="resetCode">
                <el-icon><RefreshRight /></el-icon>重置
              </el-button>
            </div>
          </div>
        </template>
        
        <div class="code-editor-wrapper">
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
          <el-button type="primary" :loading="runLoading" @click="handleRun">
            <el-icon><VideoPlay /></el-icon>运行测试
          </el-button>
          <el-button type="success" :loading="submitLoading" @click="handleSubmit" 
            :disabled="experiment.status === 2 && !experiment.allowLate">
            <el-icon><Upload /></el-icon>提交代码
          </el-button>
          <span v-if="experiment.status === 2" class="deadline-tip">实验已截止</span>
        </div>
      </el-card>

      <!-- 运行/评测结果 -->
      <el-card class="result-card" v-if="(runResult || submitResult) && userStore.isStudent">
        <template #header>
          <span>{{ showSubmitResult ? '评测结果' : '运行结果' }}</span>
        </template>
        
        <div v-if="showSubmitResult && submitResult" class="submit-result">
          <div class="result-summary">
            <div class="result-status" :class="getStatusClass(submitResult.status)">
              <el-icon :size="48">
                <CircleCheckFilled v-if="submitResult.status === 2" />
                <CircleCloseFilled v-else-if="submitResult.status === 3" />
                <WarningFilled v-else />
              </el-icon>
              <span class="status-text">{{ getStatusText(submitResult.status) }}</span>
            </div>
            <div class="result-stats">
              <div class="stat-item">
                <span class="stat-label">得分</span>
                <span class="stat-value">{{ submitResult.score || 0 }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">通过用例</span>
                <span class="stat-value">{{ submitResult.passCount || 0 }} / {{ submitResult.totalCount || 0 }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">执行时间</span>
                <span class="stat-value">{{ submitResult.executeTime || 0 }} ms</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">内存使用</span>
                <span class="stat-value">{{ formatMemory(submitResult.memoryUsed) }}</span>
              </div>
            </div>
          </div>
          
          <div class="test-cases" v-if="submitResult.resultDetail">
            <h4>测试用例详情</h4>
            <div v-for="(tc, index) in submitResult.resultDetail" :key="index" class="test-case-item">
              <div class="tc-header">
                <span class="tc-index">用例 {{ index + 1 }}</span>
                <el-tag :type="tc.passed ? 'success' : 'danger'" size="small">
                  {{ tc.passed ? '通过' : '未通过' }}
                </el-tag>
              </div>
              <div class="tc-detail" v-if="!tc.passed">
                <div class="tc-row" v-if="tc.input">
                  <span class="tc-label">输入：</span>
                  <pre class="tc-content">{{ tc.input }}</pre>
                </div>
                <div class="tc-row" v-if="tc.expected">
                  <span class="tc-label">期望输出：</span>
                  <pre class="tc-content">{{ tc.expected }}</pre>
                </div>
                <div class="tc-row" v-if="tc.actual">
                  <span class="tc-label">实际输出：</span>
                  <pre class="tc-content error">{{ tc.actual }}</pre>
                </div>
              </div>
            </div>
          </div>
          
          <div class="error-message" v-if="submitResult.errorMessage">
            <h4>错误信息</h4>
            <pre class="error-content">{{ submitResult.errorMessage }}</pre>
          </div>
        </div>
        
        <div v-else-if="runResult" class="run-result">
          <el-alert :type="runResult.success ? 'success' : 'error'" 
            :title="runResult.success ? '运行成功' : '运行失败'" show-icon :closable="false" />
          <div class="output-section">
            <h4>输出结果</h4>
            <pre class="output-content">{{ runResult.output || '无输出' }}</pre>
          </div>
          <div class="error-section" v-if="runResult.error">
            <h4>错误信息</h4>
            <pre class="error-content">{{ runResult.error }}</pre>
          </div>
        </div>
      </el-card>

      <!-- 教师查看提交列表 -->
      <el-card class="submissions-card" v-if="userStore.isTeacher">
        <template #header>
          <div class="card-header">
            <span>学生提交列表</span>
            <el-button type="success" size="small" @click="exportSubmissions">
              <el-icon><Download /></el-icon>导出
            </el-button>
          </div>
        </template>
        
        <el-table :data="submissions" stripe v-loading="submissionsLoading">
          <el-table-column prop="studentNo" label="学号" width="120" />
          <el-table-column prop="studentName" label="姓名" width="100" />
          <el-table-column prop="submitTime" label="提交时间" width="180" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusTagType(row.status)" size="small">
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="score" label="得分" width="80">
            <template #default="{ row }">{{ row.score !== null ? row.score : '-' }}</template>
          </el-table-column>
          <el-table-column label="通过率" width="100">
            <template #default="{ row }">
              {{ row.passCount || 0 }} / {{ row.totalCount || 0 }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="viewStudentCode(row)">查看代码</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- 我的提交历史（学生） -->
      <el-card class="history-card" v-if="userStore.isStudent && mySubmissions.length > 0">
        <template #header>
          <span>我的提交历史</span>
        </template>
        <el-timeline>
          <el-timeline-item v-for="(item, index) in mySubmissions" :key="index"
            :type="item.status === 2 ? 'success' : item.status === 3 ? 'danger' : 'warning'"
            :timestamp="item.submitTime" placement="top">
            <div class="history-item">
              <el-tag :type="getStatusTagType(item.status)" size="small">{{ getStatusText(item.status) }}</el-tag>
              <span class="history-score">得分: {{ item.score || 0 }}</span>
              <span class="history-pass">通过: {{ item.passCount || 0 }}/{{ item.totalCount || 0 }}</span>
              <el-button type="primary" link size="small" @click="loadHistoryCode(item)">加载代码</el-button>
            </div>
          </el-timeline-item>
        </el-timeline>
      </el-card>
    </div>

    <!-- 查看学生代码对话框 -->
    <el-dialog v-model="showCodeDialog" title="学生代码" width="800px">
      <el-descriptions :column="3" border style="margin-bottom: 16px">
        <el-descriptions-item label="学生">{{ currentStudent.studentName }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ currentStudent.studentNo }}</el-descriptions-item>
        <el-descriptions-item label="得分">{{ currentStudent.score }}</el-descriptions-item>
      </el-descriptions>
      <div class="student-code-view">
        <pre class="code-content">{{ currentStudent.code }}</pre>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getExperimentById, submitExperiment } from '@/api/experiment'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const route = useRoute()
const userStore = useUserStore()
const experimentId = route.params.id

const loading = ref(false)
const experiment = ref({})
const code = ref('')
const selectedLanguage = ref('java')
const codeTextarea = ref(null)

const runLoading = ref(false)
const submitLoading = ref(false)
const runResult = ref(null)
const submitResult = ref(null)
const showSubmitResult = ref(false)

const submissionsLoading = ref(false)
const submissions = ref([])
const mySubmissions = ref([])

const showCodeDialog = ref(false)
const currentStudent = ref({})

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
  return Math.max(code.value.split('\n').length, 20)
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

const getStatusText = (status) => {
  const map = { 0: '待评测', 1: '评测中', 2: '通过', 3: '未通过', 4: '编译错误', 5: '运行错误' }
  return map[status] || '未知'
}

const getStatusClass = (status) => {
  if (status === 2) return 'status-pass'
  if (status === 3 || status === 4 || status === 5) return 'status-fail'
  return 'status-pending'
}

const getStatusTagType = (status) => {
  if (status === 2) return 'success'
  if (status === 3 || status === 4 || status === 5) return 'danger'
  if (status === 1) return 'warning'
  return 'info'
}

const formatMemory = (kb) => {
  if (!kb) return '0 KB'
  if (kb < 1024) return kb + ' KB'
  return (kb / 1024).toFixed(2) + ' MB'
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
  code.value = experiment.value.templateCode || ''
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
  } finally {
    loading.value = false
  }
}

const fetchMySubmissions = async () => {
  try {
    const res = await request({ url: `/experiments/${experimentId}/my-submissions`, method: 'get' })
    mySubmissions.value = res.data || []
    if (mySubmissions.value.length > 0) {
      const latest = mySubmissions.value[0]
      if (latest.code) code.value = latest.code
      submitResult.value = latest
      showSubmitResult.value = true
    }
  } catch (e) {
    console.error(e)
  }
}

const fetchSubmissions = async () => {
  submissionsLoading.value = true
  try {
    const res = await request({ url: `/experiments/${experimentId}/submissions`, method: 'get' })
    submissions.value = res.data?.records || []
  } finally {
    submissionsLoading.value = false
  }
}

const handleRun = async () => {
  if (!code.value.trim()) {
    ElMessage.warning('请输入代码')
    return
  }
  runLoading.value = true
  showSubmitResult.value = false
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
  submitLoading.value = true
  try {
    const res = await submitExperiment(experimentId, { 
      code: code.value, 
      language: selectedLanguage.value 
    })
    ElMessage.success('提交成功')
    submitResult.value = res.data
    showSubmitResult.value = true
    fetchMySubmissions()
  } finally {
    submitLoading.value = false
  }
}

const loadHistoryCode = (item) => {
  code.value = item.code || ''
  ElMessage.success('已加载历史代码')
}

const viewStudentCode = async (row) => {
  try {
    const res = await request({ url: `/experiments/${experimentId}/submissions/${row.studentId}`, method: 'get' })
    currentStudent.value = { ...row, code: res.data?.code || '' }
    showCodeDialog.value = true
  } catch (e) {
    console.error(e)
  }
}

const exportSubmissions = () => {
  ElMessage.info('导出功能开发中')
}

onMounted(() => {
  fetchExperiment()
  if (userStore.isStudent) {
    fetchMySubmissions()
  } else if (userStore.isTeacher) {
    fetchSubmissions()
  }
})
</script>

<style lang="scss" scoped>
.experiment-detail {
  .detail-content {
    margin-top: 20px;
  }

  .info-card, .code-card, .result-card, .submissions-card, .history-card {
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

  .experiment-desc, .experiment-requirement {
    white-space: pre-wrap;
    line-height: 1.6;
  }

  // 代码编辑器
  .code-editor-wrapper {
    display: flex;
    border: 1px solid #dcdfe6;
    border-radius: 4px;
    overflow: hidden;
    background: #1e1e1e;

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
      min-height: 400px;
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
      color: #f56c6c;
      font-size: 13px;
    }
  }

  // 评测结果
  .submit-result {
    .result-summary {
      display: flex;
      align-items: center;
      padding: 20px;
      background: #f5f7fa;
      border-radius: 8px;
      margin-bottom: 20px;

      .result-status {
        display: flex;
        flex-direction: column;
        align-items: center;
        padding-right: 30px;
        border-right: 1px solid #e4e7ed;
        margin-right: 30px;

        &.status-pass { color: #67c23a; }
        &.status-fail { color: #f56c6c; }
        &.status-pending { color: #e6a23c; }

        .status-text {
          margin-top: 8px;
          font-size: 16px;
          font-weight: 600;
        }
      }

      .result-stats {
        display: flex;
        gap: 40px;

        .stat-item {
          text-align: center;

          .stat-label {
            display: block;
            font-size: 12px;
            color: #909399;
            margin-bottom: 4px;
          }

          .stat-value {
            font-size: 20px;
            font-weight: 600;
            color: #303133;
          }
        }
      }
    }

    .test-cases {
      h4 {
        margin: 0 0 12px;
        font-size: 15px;
        color: #303133;
      }

      .test-case-item {
        padding: 12px;
        background: #f5f7fa;
        border-radius: 4px;
        margin-bottom: 8px;

        .tc-header {
          display: flex;
          align-items: center;
          gap: 8px;
          margin-bottom: 8px;

          .tc-index {
            font-weight: 500;
          }
        }

        .tc-detail {
          .tc-row {
            margin-bottom: 8px;

            .tc-label {
              display: block;
              font-size: 12px;
              color: #909399;
              margin-bottom: 4px;
            }

            .tc-content {
              margin: 0;
              padding: 8px;
              background: #fff;
              border-radius: 4px;
              font-family: monospace;
              font-size: 13px;
              white-space: pre-wrap;

              &.error {
                background: #fef0f0;
                color: #f56c6c;
              }
            }
          }
        }
      }
    }
  }

  .error-message, .error-section {
    margin-top: 16px;

    h4 {
      margin: 0 0 8px;
      font-size: 15px;
      color: #f56c6c;
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

  .output-section {
    margin-top: 16px;

    h4 {
      margin: 0 0 8px;
      font-size: 15px;
      color: #303133;
    }

    .output-content {
      margin: 0;
      padding: 12px;
      background: #f5f7fa;
      border-radius: 4px;
      font-family: monospace;
      font-size: 13px;
      white-space: pre-wrap;
    }
  }

  // 提交历史
  .history-item {
    display: flex;
    align-items: center;
    gap: 12px;

    .history-score, .history-pass {
      font-size: 13px;
      color: #606266;
    }
  }

  // 学生代码查看
  .student-code-view {
    .code-content {
      margin: 0;
      padding: 16px;
      background: #1e1e1e;
      color: #d4d4d4;
      border-radius: 4px;
      font-family: 'Consolas', 'Monaco', monospace;
      font-size: 14px;
      white-space: pre-wrap;
      max-height: 500px;
      overflow: auto;
    }
  }
}
</style>
