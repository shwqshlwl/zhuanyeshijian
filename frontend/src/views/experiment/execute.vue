<template>
  <div class="experiment-execute-container">
    <el-page-header @back="$router.back()" title="返回">
      <template #content>
        <span class="page-title">代码执行</span>
      </template>
    </el-page-header>

    <div class="execute-content">
      <el-card class="execute-card">
        <template #header>
          <div class="card-header">
            <el-icon><Monitor /></el-icon>
            <span>执行环境</span>
          </div>
        </template>

        <!-- 执行状态 -->
        <div class="execute-status" :class="statusClass">
          <div class="status-icon">
            <el-icon :size="60">
              <Loading v-if="executing" class="rotating" />
              <CircleCheckFilled v-else-if="executeResult.success" />
              <CircleCloseFilled v-else-if="executeResult.success === false" />
              <VideoPlay v-else />
            </el-icon>
          </div>
          <div class="status-text">{{ statusText }}</div>
          <div class="status-progress" v-if="executing">
            <el-progress :percentage="progress" :show-text="false" />
          </div>
        </div>

        <!-- 代码信息 -->
        <el-descriptions :column="3" border class="code-info">
          <el-descriptions-item label="编程语言">
            <el-tag>{{ languageMap[language] }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="代码行数">{{ codeLines }}</el-descriptions-item>
          <el-descriptions-item label="执行时间">
            {{ executeResult.executeTime || 0 }} ms
          </el-descriptions-item>
        </el-descriptions>

        <!-- 执行日志 -->
        <div class="execute-logs" v-if="logs.length > 0">
          <div class="logs-header">
            <el-icon><Document /></el-icon>
            <span>执行日志</span>
          </div>
          <div class="logs-content">
            <div v-for="(log, index) in logs" :key="index" class="log-item" :class="log.type">
              <span class="log-time">{{ log.time }}</span>
              <span class="log-message">{{ log.message }}</span>
            </div>
          </div>
        </div>

        <!-- 输出结果 -->
        <div class="output-section" v-if="executeResult.output !== undefined">
          <div class="section-header success">
            <el-icon><Check /></el-icon>
            <span>标准输出</span>
          </div>
          <pre class="output-content">{{ executeResult.output || '(无输出)' }}</pre>
        </div>

        <!-- 错误信息 -->
        <div class="error-section" v-if="executeResult.error">
          <div class="section-header error">
            <el-icon><WarningFilled /></el-icon>
            <span>错误信息</span>
          </div>
          <pre class="error-content">{{ executeResult.error }}</pre>
        </div>

        <!-- 操作按钮 -->
        <div class="action-buttons">
          <el-button 
            type="primary" 
            size="large" 
            @click="handleExecute" 
            :loading="executing"
            :disabled="!code"
          >
            <el-icon><VideoPlay /></el-icon>{{ executing ? '执行中...' : '开始执行' }}
          </el-button>
          <el-button size="large" @click="handleReset">
            <el-icon><RefreshRight /></el-icon>重置
          </el-button>
          <el-button size="large" @click="goBack">
            <el-icon><Back /></el-icon>返回答题
          </el-button>
        </div>
      </el-card>

      <!-- 代码预览 -->
      <el-card class="code-preview-card">
        <template #header>
          <div class="card-header">
            <el-icon><Document /></el-icon>
            <span>代码预览</span>
          </div>
        </template>
        <div class="code-viewer">
          <div class="line-numbers">
            <div v-for="n in codeLines" :key="n" class="line-number">{{ n }}</div>
          </div>
          <pre class="code-content">{{ code || '// 无代码' }}</pre>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()
const experimentId = route.params.id

const code = ref('')
const language = ref('java')
const executing = ref(false)
const executeResult = ref({})
const logs = ref([])
const progress = ref(0)

const languageMap = {
  java: 'Java',
  python: 'Python',
  cpp: 'C++',
  c: 'C',
  javascript: 'JavaScript'
}

const codeLines = computed(() => {
  return (code.value || '').split('\n').length
})

const statusClass = computed(() => {
  if (executing.value) return 'status-executing'
  if (executeResult.value.success) return 'status-success'
  if (executeResult.value.success === false) return 'status-error'
  return 'status-idle'
})

const statusText = computed(() => {
  if (executing.value) return '正在执行代码...'
  if (executeResult.value.success) return '执行成功'
  if (executeResult.value.success === false) return '执行失败'
  return '准备就绪'
})

const addLog = (message, type = 'info') => {
  const now = new Date()
  const time = `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}:${String(now.getSeconds()).padStart(2, '0')}`
  logs.value.push({ time, message, type })
}

const handleExecute = async () => {
  if (!code.value.trim()) {
    ElMessage.warning('请先编写代码')
    return
  }

  executing.value = true
  executeResult.value = {}
  logs.value = []
  progress.value = 0

  addLog('开始执行代码...', 'info')
  progress.value = 20

  try {
    addLog(`编译 ${languageMap[language.value]} 代码...`, 'info')
    progress.value = 40

    const res = await request({
      url: `/experiments/${experimentId}/run`,
      method: 'post',
      data: { code: code.value, language: language.value }
    })

    progress.value = 80
    addLog('代码执行完成', 'success')
    
    executeResult.value = res.data || {}
    
    if (executeResult.value.success) {
      addLog('✓ 执行成功', 'success')
    } else {
      addLog('✗ 执行失败', 'error')
    }
    
    progress.value = 100
  } catch (e) {
    addLog('✗ 执行出错: ' + (e.message || '未知错误'), 'error')
    executeResult.value = { success: false, error: e.message || '执行失败' }
    progress.value = 100
  } finally {
    executing.value = false
  }
}

const handleReset = () => {
  executeResult.value = {}
  logs.value = []
  progress.value = 0
  ElMessage.success('已重置')
}

const goBack = () => {
  router.push(`/experiments/${experimentId}/answer`)
}

onMounted(() => {
  // 从路由参数或本地存储获取代码
  const routeCode = route.query.code
  const routeLang = route.query.language
  
  if (routeCode) {
    code.value = decodeURIComponent(routeCode)
    language.value = routeLang || 'java'
  } else {
    const savedCode = localStorage.getItem(`experiment_${experimentId}_code`)
    const savedLang = localStorage.getItem(`experiment_${experimentId}_language`)
    
    if (savedCode) {
      code.value = savedCode
      language.value = savedLang || 'java'
    }
  }

  if (!code.value) {
    ElMessage.warning('未找到代码，请先编写代码')
  }
})
</script>

<style lang="scss" scoped>
.experiment-execute-container {
  .page-title {
    font-size: 20px;
    font-weight: 600;
  }

  .execute-content {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 20px;
    margin-top: 20px;
  }

  .card-header {
    display: flex;
    align-items: center;
    gap: 8px;
    font-weight: 600;
  }

  .execute-status {
    text-align: center;
    padding: 40px 20px;
    border-radius: 12px;
    margin-bottom: 24px;
    transition: all 0.3s;

    &.status-idle {
      background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
      
      .status-icon {
        color: #2196f3;
      }
    }

    &.status-executing {
      background: linear-gradient(135deg, #fff3e0 0%, #ffe0b2 100%);
      
      .status-icon {
        color: #ff9800;
      }
    }

    &.status-success {
      background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
      
      .status-icon {
        color: #4caf50;
      }
    }

    &.status-error {
      background: linear-gradient(135deg, #ffebee 0%, #ffcdd2 100%);
      
      .status-icon {
        color: #f44336;
      }
    }

    .status-icon {
      margin-bottom: 16px;

      .rotating {
        animation: rotate 2s linear infinite;
      }
    }

    .status-text {
      font-size: 20px;
      font-weight: 600;
      color: #303133;
      margin-bottom: 16px;
    }

    .status-progress {
      max-width: 300px;
      margin: 0 auto;
    }
  }

  @keyframes rotate {
    from { transform: rotate(0deg); }
    to { transform: rotate(360deg); }
  }

  .code-info {
    margin-bottom: 24px;
  }

  .execute-logs {
    margin-bottom: 24px;

    .logs-header {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 15px;
      font-weight: 600;
      color: #303133;
      margin-bottom: 12px;
    }

    .logs-content {
      max-height: 200px;
      overflow-y: auto;
      background: #1e1e1e;
      border-radius: 4px;
      padding: 12px;

      .log-item {
        display: flex;
        gap: 12px;
        padding: 4px 0;
        font-family: 'Consolas', 'Monaco', monospace;
        font-size: 13px;
        line-height: 1.6;

        .log-time {
          color: #858585;
          min-width: 70px;
        }

        .log-message {
          color: #d4d4d4;
        }

        &.success .log-message {
          color: #4caf50;
        }

        &.error .log-message {
          color: #f44336;
        }

        &.warning .log-message {
          color: #ff9800;
        }
      }
    }
  }

  .output-section, .error-section {
    margin-bottom: 24px;

    .section-header {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 15px;
      font-weight: 600;
      margin-bottom: 12px;

      &.success {
        color: #4caf50;
      }

      &.error {
        color: #f44336;
      }
    }

    .output-content, .error-content {
      margin: 0;
      padding: 16px;
      border-radius: 4px;
      font-family: 'Consolas', 'Monaco', monospace;
      font-size: 13px;
      line-height: 1.6;
      white-space: pre-wrap;
      max-height: 300px;
      overflow: auto;
    }

    .output-content {
      background: #f0f9ff;
      border: 1px solid #b3d8ff;
      color: #303133;
    }

    .error-content {
      background: #fef0f0;
      border: 1px solid #fbc4c4;
      color: #f56c6c;
    }
  }

  .action-buttons {
    display: flex;
    justify-content: center;
    gap: 12px;
    padding-top: 16px;
    border-top: 1px solid #ebeef5;
  }

  .code-viewer {
    display: flex;
    background: #1e1e1e;
    border-radius: 4px;
    overflow: auto;
    max-height: 600px;

    .line-numbers {
      padding: 16px 12px;
      background: #252526;
      color: #858585;
      font-family: 'Consolas', 'Monaco', monospace;
      font-size: 14px;
      line-height: 1.5;
      text-align: right;
      user-select: none;
      min-width: 50px;

      .line-number {
        height: 21px;
      }
    }

    .code-content {
      flex: 1;
      margin: 0;
      padding: 16px;
      color: #d4d4d4;
      font-family: 'Consolas', 'Monaco', monospace;
      font-size: 14px;
      line-height: 1.5;
      white-space: pre;
    }
  }
}
</style>
