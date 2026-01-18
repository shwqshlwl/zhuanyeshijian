<template>
  <el-dialog
    v-model="visible"
    title="AI 题目解析"
    width="600px"
    :before-close="handleClose"
    class="ai-analysis-dialog"
  >
    <div class="question-preview">
      <div class="label">题目内容：</div>
      <div class="content">{{ questionContent }}</div>
    </div>
    
    <div class="analysis-result">
      <div v-if="loading" class="loading-state">
        <el-skeleton :rows="5" animated />
        <div class="loading-text">AI 正在深入分析题目...</div>
      </div>
      
      <div v-else class="result-content">
        <div class="section">
          <div class="section-title">
            <el-icon><Reading /></el-icon> 考察知识点
          </div>
          <div class="section-body">
            <el-tag v-for="tag in analysisData.tags" :key="tag" size="small" style="margin-right: 5px;">{{ tag }}</el-tag>
          </div>
        </div>
        
        <div class="section">
          <div class="section-title">
            <el-icon><Aim /></el-icon> 解题思路
          </div>
          <div class="section-body" v-html="formatContent(analysisData.thinking)"></div>
        </div>
        
        <div class="section">
          <div class="section-title">
            <el-icon><Key /></el-icon> 参考解析
          </div>
          <div class="section-body" v-html="formatContent(analysisData.analysis)"></div>
        </div>
      </div>
    </div>
    
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">关闭</el-button>
        <el-button type="primary" @click="askMore">进一步提问</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  questionContent: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:modelValue'])
const router = useRouter()

const visible = ref(false)
const loading = ref(false)
const analysisData = ref({
  tags: [],
  thinking: '',
  analysis: ''
})

watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val && props.questionContent) {
    analyzeQuestion()
  }
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

const handleClose = () => {
  visible.value = false
}

const analyzeQuestion = () => {
  loading.value = true
  // 模拟AI分析过程
  setTimeout(() => {
    analysisData.value = {
      tags: ['循环结构', '数组操作', '算法复杂度'],
      thinking: '本题主要考察对数组遍历和条件判断的理解。需要注意边界条件的处理，以及如何优化算法的时间复杂度。',
      analysis: '1. 首先定义一个结果变量。<br>2. 遍历数组，对每个元素进行判断。<br>3. 如果满足条件，更新结果变量。<br>4. 循环结束后返回结果。<br><br>常见错误点：数组下标越界，或者循环终止条件设置错误。'
    }
    loading.value = false
  }, 2000)
}

const formatContent = (content) => {
  return content
}

const askMore = () => {
  visible.value = false
  router.push({ 
    path: '/ai/chat', 
    query: { 
      initialMessage: `关于这道题：“${props.questionContent}”，我有更多疑问。` 
    } 
  })
}
</script>

<style lang="scss" scoped>
.question-preview {
  background-color: #f5f7fa;
  padding: 10px 15px;
  border-radius: 4px;
  margin-bottom: 20px;
  border-left: 4px solid #409eff;
  
  .label {
    font-weight: bold;
    margin-bottom: 5px;
    font-size: 12px;
    color: #909399;
  }
  
  .content {
    font-size: 14px;
    color: #303133;
    line-height: 1.5;
    max-height: 100px;
    overflow-y: auto;
  }
}

.analysis-result {
  min-height: 200px;
  
  .loading-state {
    padding: 20px 0;
    text-align: center;
    
    .loading-text {
      margin-top: 15px;
      color: #909399;
      font-size: 14px;
    }
  }
  
  .section {
    margin-bottom: 20px;
    
    .section-title {
      font-weight: bold;
      font-size: 16px;
      color: #303133;
      margin-bottom: 10px;
      display: flex;
      align-items: center;
      
      .el-icon {
        margin-right: 5px;
        color: #409eff;
      }
    }
    
    .section-body {
      font-size: 14px;
      line-height: 1.6;
      color: #606266;
      background-color: #fff;
      padding: 0 5px;
    }
  }
}
</style>
