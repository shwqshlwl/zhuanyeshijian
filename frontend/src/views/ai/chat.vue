<template>
  <div class="ai-chat-container">
    <div class="chat-sidebar">
      <div class="sidebar-header">
        <h3>会话历史</h3>
        <el-button type="primary" size="small" icon="Plus" @click="createNewChat">新建会话</el-button>
      </div>
      <div class="history-list">
        <div 
          v-for="item in historyList" 
          :key="item.id" 
          class="history-item"
          :class="{ active: currentChatId === item.id }"
          @click="selectChat(item)"
        >
          <span class="title">{{ item.title }}</span>
          <span class="time">{{ item.time }}</span>
        </div>
      </div>
    </div>
    
    <div class="chat-main">
      <div class="chat-header">
        <h2>{{ currentChatTitle || 'AI 助教' }}</h2>
        <div class="actions">
          <el-button v-if="currentChatId" type="danger" link icon="Delete" @click="deleteCurrentChat">删除会话</el-button>
        </div>
      </div>
      
      <div class="messages-container" ref="messagesContainer">
        <div v-if="messages.length === 0" class="empty-state">
          <el-empty description="开始向 AI 助教提问吧" />
          <div class="suggestions">
            <el-tag 
              v-for="sug in suggestions" 
              :key="sug" 
              class="suggestion-tag" 
              effect="plain"
              @click="sendMessage(sug)"
            >
              {{ sug }}
            </el-tag>
          </div>
        </div>
        
        <div 
          v-for="(msg, index) in messages" 
          :key="index" 
          class="message-item"
          :class="msg.role"
        >
          <div class="avatar">
            <el-avatar :size="36" :icon="msg.role === 'user' ? 'User' : 'Service'" :class="msg.role" />
          </div>
          <div class="content">
            <div class="bubble">
              <div v-html="formatMessage(msg.content)"></div>
            </div>
            <div class="meta">{{ msg.time }}</div>
          </div>
        </div>
        
        <div v-if="loading" class="message-item assistant">
          <div class="avatar">
            <el-avatar :size="36" icon="Service" class="assistant" />
          </div>
          <div class="content">
            <div class="bubble typing">
              <span></span><span></span><span></span>
            </div>
          </div>
        </div>
      </div>
      
      <div class="input-area">
        <el-input
          v-model="inputMessage"
          type="textarea"
          :rows="3"
          placeholder="请输入您的问题..."
          resize="none"
          @keydown.enter.prevent="handleEnter"
        />
        <div class="input-actions">
          <el-button type="primary" :loading="loading" @click="handleSend" :disabled="!inputMessage.trim()">发送</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { chatWithAi, getAiSessionList, getAiSessionMessages, deleteAiSession } from '@/api/ai'
import { getVisitorId } from '@/utils/visitor'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const inputMessage = ref('')
const loading = ref(false)
const messagesContainer = ref(null)
const currentChatId = ref(null)
const currentChatTitle = ref('')
const messages = ref([])
const historyList = ref([])

const suggestions = [
  '如何学习Java编程？',
  '解释一下面向对象的三大特性',
  '帮我分析这道题目',
  '什么是Spring Boot？'
]

onMounted(async () => {
  await fetchHistoryList()
  
  // Check for session ID from router (from history page)
  if (route.query.id) {
    const session = historyList.value.find(h => h.id == route.query.id)
    if (session) {
      selectChat(session)
    } else {
      // If not in list (maybe new or not loaded), try to fetch directly
      loadSessionMessages(route.query.id)
    }
  }

  // Check for initial message from router (e.g. from homework analysis)
  if (route.query.initialMessage) {
    // If we are already in a session, maybe start a new one? 
    // Usually initialMessage implies starting a new analysis.
    createNewChat()
    inputMessage.value = route.query.initialMessage
    handleSend()
  }
})

const fetchHistoryList = async () => {
  try {
    const res = await getAiSessionList({ visitorId: getVisitorId() })
    historyList.value = res.data.map(item => ({
      id: item.id,
      title: item.title,
      time: formatTime(item.updateTime)
    }))
  } catch (error) {
    console.error('Failed to fetch history:', error)
  }
}

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return date.toLocaleString()
}

const createNewChat = () => {
  currentChatId.value = null
  currentChatTitle.value = '新会话'
  messages.value = []
  inputMessage.value = ''
  // Clear query param
  router.replace({ query: {} })
}

const selectChat = (chat) => {
  currentChatId.value = chat.id
  currentChatTitle.value = chat.title
  loadSessionMessages(chat.id)
}

const loadSessionMessages = async (sessionId) => {
  currentChatId.value = sessionId
  loading.value = true
  try {
    const res = await getAiSessionMessages(sessionId)
    messages.value = res.data.map(msg => ({
      role: msg.role,
      content: msg.content,
      time: formatTime(msg.createTime)
    }))
    scrollToBottom()
  } catch (error) {
    ElMessage.error('加载消息失败')
  } finally {
    loading.value = false
  }
}

const deleteCurrentChat = () => {
  if (!currentChatId.value) return
  
  ElMessageBox.confirm('确定要删除当前会话吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await deleteAiSession(currentChatId.value)
      ElMessage.success('删除成功')
      await fetchHistoryList()
      createNewChat()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

const handleEnter = (e) => {
  if (!e.shiftKey) {
    handleSend()
  }
}

const handleSend = async () => {
  const content = inputMessage.value.trim()
  if (!content) return
  
  sendMessage(content)
}

const sendMessage = async (content) => {
  // Optimistic UI update
  messages.value.push({
    role: 'user',
    content: content,
    time: new Date().toLocaleTimeString()
  })
  
  inputMessage.value = ''
  loading.value = true
  scrollToBottom()
  
  try {
    const params = {
      message: content,
      sessionId: currentChatId.value,
      visitorId: getVisitorId()
    }
    
    const res = await chatWithAi(params)
    const aiResponse = res.data
    
    // Update session info if it was new
    if (!currentChatId.value) {
      currentChatId.value = aiResponse.sessionId
      currentChatTitle.value = aiResponse.title
      // Add to history list
      fetchHistoryList()
    }
    
    messages.value.push({
      role: 'assistant',
      content: aiResponse.response,
      time: new Date().toLocaleTimeString()
    })
    
    scrollToBottom()
  } catch (error) {
    console.error(error)
    messages.value.push({
      role: 'assistant',
      content: '抱歉，AI 服务暂时不可用，请稍后再试。',
      time: new Date().toLocaleTimeString()
    })
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

const formatMessage = (content) => {
  // Simple format, replace newlines with <br>
  // In a real app, use a Markdown renderer
  if (!content) return ''
  return content.replace(/\n/g, '<br>')
}
</script>

<style lang="scss" scoped>
.ai-chat-container {
  display: flex;
  height: calc(100vh - 100px); /* 减去顶部导航高度 */
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  overflow: hidden;
  margin: 20px;
}

.chat-sidebar {
  width: 260px;
  background-color: #f5f7fa;
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
  
  .sidebar-header {
    padding: 15px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-bottom: 1px solid #e4e7ed;
    
    h3 {
      margin: 0;
      font-size: 16px;
      color: #303133;
    }
  }
  
  .history-list {
    flex: 1;
    overflow-y: auto;
    padding: 10px;
    
    .history-item {
      padding: 10px;
      border-radius: 4px;
      cursor: pointer;
      margin-bottom: 5px;
      transition: background-color 0.2s;
      
      &:hover {
        background-color: #e6e8eb;
      }
      
      &.active {
        background-color: #e1f3d8;
        color: #67c23a;
      }
      
      .title {
        display: block;
        font-size: 14px;
        font-weight: 500;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        margin-bottom: 4px;
      }
      
      .time {
        display: block;
        font-size: 12px;
        color: #909399;
      }
    }
  }
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background-color: #fff;
  
  .chat-header {
    padding: 15px 20px;
    border-bottom: 1px solid #ebeef5;
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    h2 {
      margin: 0;
      font-size: 18px;
      color: #303133;
    }
  }
  
  .messages-container {
    flex: 1;
    overflow-y: auto;
    padding: 20px;
    background-color: #f9fafc;
    
    .empty-state {
      margin-top: 100px;
      text-align: center;
      
      .suggestions {
        margin-top: 20px;
        display: flex;
        flex-wrap: wrap;
        justify-content: center;
        gap: 10px;
        
        .suggestion-tag {
          cursor: pointer;
        }
      }
    }
    
    .message-item {
      display: flex;
      margin-bottom: 20px;
      
      &.user {
        flex-direction: row-reverse;
        
        .content {
          align-items: flex-end;
          
          .bubble {
            background-color: #95d475;
            color: #303133;
            border-bottom-right-radius: 4px;
            border-bottom-left-radius: 12px;
          }
        }
        
        .avatar {
          margin-left: 10px;
          margin-right: 0;
        }
      }
      
      &.assistant {
        .content {
          align-items: flex-start;
          
          .bubble {
            background-color: #fff;
            color: #303133;
            border: 1px solid #e4e7ed;
            border-bottom-left-radius: 4px;
            border-bottom-right-radius: 12px;
          }
        }
        
        .avatar {
          margin-right: 10px;
          margin-left: 0;
          
          .el-avatar {
            background-color: #409eff;
          }
        }
      }
      
      .content {
        display: flex;
        flex-direction: column;
        max-width: 70%;
        
        .bubble {
          padding: 10px 15px;
          border-radius: 12px;
          border-top-left-radius: 12px;
          border-top-right-radius: 12px;
          font-size: 14px;
          line-height: 1.6;
          box-shadow: 0 1px 2px rgba(0,0,0,0.05);
          word-break: break-word;
        }
        
        .meta {
          font-size: 12px;
          color: #909399;
          margin-top: 4px;
        }
      }
    }
  }
  
  .input-area {
    padding: 20px;
    border-top: 1px solid #ebeef5;
    background-color: #fff;
    
    .input-actions {
      display: flex;
      justify-content: flex-end;
      margin-top: 10px;
    }
  }
}

.typing {
  display: flex;
  align-items: center;
  height: 24px;
  
  span {
    display: inline-block;
    width: 6px;
    height: 6px;
    background-color: #909399;
    border-radius: 50%;
    margin: 0 2px;
    animation: typing 1.4s infinite ease-in-out both;
    
    &:nth-child(1) { animation-delay: -0.32s; }
    &:nth-child(2) { animation-delay: -0.16s; }
  }
}

@keyframes typing {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}
</style>
