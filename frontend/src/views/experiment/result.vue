<template>
  <div class="experiment-result-container">
    <el-page-header @back="$router.back()" title="返回">
      <template #content>
        <span class="page-title">评测结果</span>
      </template>
    </el-page-header>

    <div class="result-content" v-loading="loading">
      <!-- 评测状态卡片 -->
      <el-card class="status-card">
        <div class="status-wrapper" :class="getStatusClass(result.status)">
          <div class="status-icon">
            <el-icon :size="80">
              <CircleCheckFilled v-if="result.status === 2" />
              <CircleCloseFilled v-else-if="result.status === 3 || result.status === 4 || result.status === 5" />
              <Loading v-else-if="result.status === 1" />
              <WarningFilled v-else />
            </el-icon>
          </div>
          <div class="status-text">{{ getStatusText(result.status) }}</div>
          <div class="status-desc">{{ getStatusDesc(result.status) }}</div>
        </div>

        <div class="stats-grid">
          <div class="stat-item">
            <div class="stat-icon score">
              <el-icon><TrophyBase /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-label">得分</div>
              <div class="stat-value">{{ result.score || 0 }}</div>
            </div>
          </div>

          <div class="stat-item">
            <div class="stat-icon pass">
              <el-icon><Select /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-label">通过用例</div>
              <div class="stat-value">{{ result.passCount || 0 }} / {{ result.totalCount || 0 }}</div>
            </div>
          </div>

          <div class="stat-item">
            <div class="stat-icon time">
              <el-icon><Timer /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-label">执行时间</div>
              <div class="stat-value">{{ result.executeTime || 0 }} ms</div>
            </div>
          </div>

          <div class="stat-item">
            <div class="stat-icon memory">
              <el-icon><Cpu /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-label">内存使用</div>
              <div class="stat-value">{{ formatMemory(result.memoryUsed) }}</div>
            </div>
          </div>
        </div>

        <div class="submit-info">
          <el-icon><Clock /></el-icon>
          <span>提交时间：{{ formatTime(result.submitTime) }}</span>
        </div>
      </el-card>

      <!-- 测试用例详情 -->
      <el-card class="test-cases-card" v-if="testCaseDetails.length > 0">
        <template #header>
          <div class="card-header">
            <el-icon><List /></el-icon>
            <span>测试用例详情</span>
          </div>
        </template>

        <div class="test-cases-list">
          <div 
            v-for="(tc, index) in testCaseDetails" 
            :key="index" 
            class="test-case-item"
            :class="{ passed: tc.passed, failed: !tc.passed }"
          >
            <div class="tc-header">
              <div class="tc-left">
                <el-icon class="tc-icon" :size="20">
                  <CircleCheckFilled v-if="tc.passed" />
                  <CircleCloseFilled v-else />
                </el-icon>
                <span class="tc-title">测试用例 {{ index + 1 }}</span>
              </div>
              <el-tag :type="tc.passed ? 'success' : 'danger'" size="small">
                {{ tc.passed ? '通过' : '未通过' }}
              </el-tag>
            </div>

            <div class="tc-body" v-if="!tc.passed || showAllDetails">
              <el-collapse v-model="activeCollapse[index]">
                <el-collapse-item name="details">
                  <template #title>
                    <span class="collapse-title">查看详情</span>
                  </template>
                  
                  <div class="tc-detail">
                    <div class="tc-row" v-if="tc.input !== undefined">
                      <div class="tc-label">
                        <el-icon><Download /></el-icon>
                        <span>输入数据</span>
                      </div>
                      <pre class="tc-content input">{{ tc.input || '(空)' }}</pre>
                    </div>

                    <div class="tc-row" v-if="tc.expected !== undefined">
                      <div class="tc-label">
                        <el-icon><Check /></el-icon>
                        <span>期望输出</span>
                      </div>
                      <pre class="tc-content expected">{{ tc.expected || '(空)' }}</pre>
                    </div>

                    <div class="tc-row" v-if="tc.actual !== undefined">
                      <div class="tc-label">
                        <el-icon><Upload /></el-icon>
                        <span>实际输出</span>
                      </div>
                      <pre class="tc-content actual" :class="{ error: !tc.passed }">{{ tc.actual || '(空)' }}</pre>
                    </div>

                    <div class="tc-row" v-if="tc.error">
                      <div class="tc-label error">
                        <el-icon><WarningFilled /></el-icon>
                        <span>错误信息</span>
                      </div>
                      <pre class="tc-content error">{{ tc.error }}</pre>
                    </div>
                  </div>
                </el-collapse-item>
              </el-collapse>
            </div>
          </div>
        </div>

        <div class="show-all-toggle">
          <el-button text @click="showAllDetails = !showAllDetails">
            {{ showAllDetails ? '只显示未通过用例' : '显示所有用例详情' }}
          </el-button>
        </div>
      </el-card>

      <!-- 错误信息 -->
      <el-card class="error-card" v-if="result.errorMessage">
        <template #header>
          <div class="card-header error">
            <el-icon><WarningFilled /></el-icon>
            <span>错误信息</span>
          </div>
        </template>
        <pre class="error-content">{{ result.errorMessage }}</pre>
      </el-card>

      <!-- 提交的代码 -->
      <el-card class="code-card">
        <template #header>
          <div class="card-header">
            <el-icon><Document /></el-icon>
            <span>提交的代码</span>
            <el-button size="small" @click="copyCode">
              <el-icon><CopyDocument /></el-icon>复制代码
            </el-button>
          </div>
        </template>
        <div class="code-viewer">
          <div class="line-numbers">
            <div v-for="n in codeLineCount" :key="n" class="line-number">{{ n }}</div>
          </div>
          <pre class="code-content">{{ result.code || '无代码' }}</pre>
        </div>
      </el-card>

      <!-- 操作按钮 -->
      <div class="action-buttons">
        <el-button type="primary" size="large" @click="goToAnswer">
          <el-icon><Edit /></el-icon>继续答题
        </el-button>
        <el-button size="large" @click="viewExperiment">
          <el-icon><View /></el-icon>查看实验详情
        </el-button>
        <el-button size="large" @click="viewHistory">
          <el-icon><Clock /></el-icon>查看提交历史
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()
const experimentId = route.params.id

const loading = ref(false)
const result = ref({})
const testCaseDetails = ref([])
const showAllDetails = ref(false)
const activeCollapse = reactive({})

const codeLineCount = computed(() => {
  return (result.value.code || '').split('\n').length
})

const getStatusText = (status) => {
  const map = {
    0: '待评测',
    1: '评测中',
    2: '通过',
    3: '未通过',
    4: '编译错误',
    5: '运行错误'
  }
  return map[status] || '未知状态'
}

const getStatusDesc = (status) => {
  const map = {
    0: '您的代码正在等待评测',
    1: '正在执行测试用例，请稍候...',
    2: '恭喜！您的代码通过了所有测试用例',
    3: '您的代码未能通过所有测试用例，请检查逻辑',
    4: '代码编译失败，请检查语法错误',
    5: '代码运行时发生错误'
  }
  return map[status] || ''
}

const getStatusClass = (status) => {
  if (status === 2) return 'status-success'
  if (status === 3 || status === 4 || status === 5) return 'status-error'
  if (status === 1) return 'status-pending'
  return 'status-default'
}

const formatMemory = (kb) => {
  if (!kb) return '0 KB'
  if (kb < 1024) return kb + ' KB'
  return (kb / 1024).toFixed(2) + ' MB'
}

const formatTime = (time) => {
  return time ? dayjs(time).format('YYYY-MM-DD HH:mm:ss') : '-'
}

const fetchResult = async () => {
  loading.value = true
  try {
    const res = await request({
      url: `/experiments/${experimentId}/result`,
      method: 'get'
    })
    result.value = res.data || {}

    // 解析测试用例详情
    if (result.value.resultDetail) {
      try {
        testCaseDetails.value = JSON.parse(result.value.resultDetail)
      } catch (e) {
        console.error('解析测试用例详情失败', e)
      }
    }

    // 如果还在评测中，轮询结果
    if (result.value.status === 1) {
      setTimeout(fetchResult, 2000)
    }
  } catch (e) {
    ElMessage.error('获取评测结果失败')
  } finally {
    loading.value = false
  }
}

const copyCode = () => {
  if (!result.value.code) {
    ElMessage.warning('无代码可复制')
    return
  }
  
  navigator.clipboard.writeText(result.value.code).then(() => {
    ElMessage.success('代码已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败')
  })
}

const goToAnswer = () => {
  router.push(`/experiments/${experimentId}/answer`)
}

const viewExperiment = () => {
  router.push(`/experiments/${experimentId}`)
}

const viewHistory = () => {
  router.push(`/experiments/${experimentId}`)
}

onMounted(() => {
  fetchResult()
})
</script>

<style lang="scss" scoped>
.experiment-result-container {
  .page-title {
    font-size: 20px;
    font-weight: 600;
  }

  .result-content {
    margin-top: 20px;
  }

  .status-card, .test-cases-card, .error-card, .code-card {
    margin-bottom: 20px;
  }

  .card-header {
    display: flex;
    align-items: center;
    gap: 8px;
    font-weight: 600;

    &.error {
      color: #f56c6c;
    }

    .el-button {
      margin-left: auto;
    }
  }

  // 状态卡片
  .status-wrapper {
    text-align: center;
    padding: 40px 20px;
    border-radius: 8px;
    margin-bottom: 30px;

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

    &.status-pending {
      background: linear-gradient(135deg, #fff3e0 0%, #ffe0b2 100%);
      
      .status-icon {
        color: #ff9800;
        animation: spin 2s linear infinite;
      }
    }

    &.status-default {
      background: linear-gradient(135deg, #f5f5f5 0%, #e0e0e0 100%);
      
      .status-icon {
        color: #9e9e9e;
      }
    }

    .status-icon {
      margin-bottom: 16px;
    }

    .status-text {
      font-size: 28px;
      font-weight: 700;
      margin-bottom: 8px;
      color: #303133;
    }

    .status-desc {
      font-size: 14px;
      color: #606266;
    }
  }

  @keyframes spin {
    from { transform: rotate(0deg); }
    to { transform: rotate(360deg); }
  }

  .stats-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20px;
    margin-bottom: 20px;

    .stat-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 16px;
      background: #f5f7fa;
      border-radius: 8px;

      .stat-icon {
        width: 48px;
        height: 48px;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: 8px;
        font-size: 24px;

        &.score {
          background: #fff3e0;
          color: #ff9800;
        }

        &.pass {
          background: #e8f5e9;
          color: #4caf50;
        }

        &.time {
          background: #e3f2fd;
          color: #2196f3;
        }

        &.memory {
          background: #f3e5f5;
          color: #9c27b0;
        }
      }

      .stat-content {
        flex: 1;

        .stat-label {
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

  .submit-info {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 12px;
    background: #f5f7fa;
    border-radius: 4px;
    font-size: 13px;
    color: #606266;
  }

  // 测试用例列表
  .test-cases-list {
    .test-case-item {
      margin-bottom: 12px;
      border: 2px solid #e4e7ed;
      border-radius: 8px;
      overflow: hidden;
      transition: all 0.3s;

      &.passed {
        border-color: #67c23a;
        background: #f0f9ff;
      }

      &.failed {
        border-color: #f56c6c;
        background: #fef0f0;
      }

      .tc-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 12px 16px;
        background: #fff;

        .tc-left {
          display: flex;
          align-items: center;
          gap: 8px;

          .tc-icon {
            &.passed {
              color: #67c23a;
            }

            &.failed {
              color: #f56c6c;
            }
          }

          .tc-title {
            font-weight: 600;
            color: #303133;
          }
        }
      }

      .tc-body {
        padding: 0 16px 12px;

        .collapse-title {
          font-size: 13px;
          color: #606266;
        }

        .tc-detail {
          .tc-row {
            margin-bottom: 12px;

            &:last-child {
              margin-bottom: 0;
            }

            .tc-label {
              display: flex;
              align-items: center;
              gap: 4px;
              font-size: 13px;
              font-weight: 600;
              color: #606266;
              margin-bottom: 6px;

              &.error {
                color: #f56c6c;
              }
            }

            .tc-content {
              margin: 0;
              padding: 12px;
              border-radius: 4px;
              font-family: 'Consolas', 'Monaco', monospace;
              font-size: 13px;
              line-height: 1.5;
              white-space: pre-wrap;
              word-break: break-all;

              &.input {
                background: #f5f7fa;
                border: 1px solid #e4e7ed;
              }

              &.expected {
                background: #f0f9ff;
                border: 1px solid #b3d8ff;
              }

              &.actual {
                background: #f0f9ff;
                border: 1px solid #b3d8ff;

                &.error {
                  background: #fef0f0;
                  border: 1px solid #fbc4c4;
                  color: #f56c6c;
                }
              }

              &.error {
                background: #fef0f0;
                border: 1px solid #fbc4c4;
                color: #f56c6c;
              }
            }
          }
        }
      }
    }
  }

  .show-all-toggle {
    text-align: center;
    margin-top: 16px;
  }

  // 错误信息
  .error-content {
    margin: 0;
    padding: 16px;
    background: #fef0f0;
    border-radius: 4px;
    color: #f56c6c;
    font-family: 'Consolas', 'Monaco', monospace;
    font-size: 13px;
    line-height: 1.6;
    white-space: pre-wrap;
  }

  // 代码查看器
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
      overflow-x: auto;
    }
  }

  // 操作按钮
  .action-buttons {
    display: flex;
    justify-content: center;
    gap: 16px;
    padding: 20px 0;
  }
}
</style>
