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
import { chatWithAi } from '@/api/ai'
import { getVisitorId } from '@/utils/visitor'

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
const currentSessionId = ref(null)
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

const analyzeQuestion = async () => {
  loading.value = true
  currentSessionId.value = null
  try {
    const prompt = `请分析以下题目，并给出：
1. 考察的知识点（用逗号分隔）
2. 解题思路
3. 详细解析

题目内容：
${props.questionContent}

请以JSON格式返回，格式如下：
{
  "tags": ["知识点1", "知识点2"],
  "thinking": "解题思路内容...",
  "analysis": "详细解析内容..."
}`

    const res = await chatWithAi({ 
      message: prompt,
      visitorId: getVisitorId(),
      systemPrompt: "你是一个专业的课程助教，擅长分析编程题目。请务必返回严格的JSON格式数据，不要包含Markdown代码块标记。" 
    })
    
    // API returns { response: "...", sessionId: ..., title: ... }
    const aiResponse = res.data
    currentSessionId.value = aiResponse.sessionId

    // 尝试解析返回的JSON
    let jsonStr = aiResponse.response
    // 清理可能的markdown标记
    jsonStr = jsonStr.replace(/```json/g, '').replace(/```/g, '').trim()
    
    try {
      const result = JSON.parse(jsonStr)
      analysisData.value = result
    } catch (e) {
      // 如果解析失败，直接显示原始内容
      analysisData.value = {
        tags: ['AI分析'],
        thinking: '解析格式异常，直接显示内容：',
        analysis: aiResponse.response
      }
    }
  } catch (error) {
    console.error(error)
    analysisData.value = {
      tags: ['错误'],
      thinking: '分析失败',
      analysis: '抱歉，AI 暂时无法分析该题目。'
    }
  } finally {
    loading.value = false
  }
}

const formatContent = (content) => {
  if (!content) return ''
  return content.replace(/\n/g, '<br/>')
}

const askMore = () => {
  visible.value = false
  if (currentSessionId.value) {
    router.push({ 
      path: '/ai/chat', 
      query: { 
        id: currentSessionId.value
      } 
    })
  } else {
    router.push({ 
      path: '/ai/chat', 
      query: { 
        initialMessage: `关于这道题：“${props.questionContent}”，我有更多疑问。` 
      } 
    })
  }
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
