<template>
  <div class="ai-history-container">
    <div class="page-header">
      <h2>AI 会话历史</h2>
      <el-button type="primary" icon="ChatDotRound" @click="$router.push('/ai/chat')">开始新会话</el-button>
    </div>
    
    <div class="history-content">
      <el-table :data="historyList" style="width: 100%" v-loading="loading">
        <el-table-column prop="title" label="会话主题" min-width="300">
          <template #default="{ row }">
            <span class="history-title" @click="goToChat(row.id)">{{ row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="messageCount" label="消息数" width="100" align="center" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column prop="lastActiveTime" label="最后活跃" width="180" />
        <el-table-column label="操作" width="150" align="center">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="goToChat(row.id)">查看</el-button>
            <el-button link type="danger" size="small" @click="deleteHistory(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination">
        <el-pagination
          background
          layout="prev, pager, next"
          :total="100"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const historyList = ref([])

onMounted(() => {
  fetchHistory()
})

const fetchHistory = () => {
  loading.value = true
  // 模拟数据
  setTimeout(() => {
    historyList.value = [
      { id: 1, title: '关于Java多线程的问题', messageCount: 5, createTime: '2023-05-20 10:30', lastActiveTime: '2023-05-20 10:45' },
      { id: 2, title: 'Spring MVC原理解析', messageCount: 12, createTime: '2023-05-19 15:45', lastActiveTime: '2023-05-19 16:20' },
      { id: 3, title: 'Vue3组合式API使用', messageCount: 8, createTime: '2023-05-18 09:20', lastActiveTime: '2023-05-18 09:50' },
      { id: 4, title: '数据库索引优化策略', messageCount: 20, createTime: '2023-05-15 14:10', lastActiveTime: '2023-05-15 15:30' },
    ]
    loading.value = false
  }, 500)
}

const goToChat = (id) => {
  router.push({ path: '/ai/chat', query: { id } })
}

const deleteHistory = (id) => {
  ElMessageBox.confirm('确定要删除这条会话记录吗？', '提示', {
    type: 'warning'
  }).then(() => {
    historyList.value = historyList.value.filter(item => item.id !== id)
    ElMessage.success('删除成功')
  })
}
</script>

<style lang="scss" scoped>
.ai-history-container {
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  margin: 20px;
  min-height: calc(100vh - 100px);
  
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding-bottom: 15px;
    border-bottom: 1px solid #ebeef5;
    
    h2 {
      margin: 0;
      font-size: 20px;
      color: #303133;
    }
  }
  
  .history-content {
    .history-title {
      cursor: pointer;
      color: #409eff;
      font-weight: 500;
      
      &:hover {
        text-decoration: underline;
      }
    }
    
    .pagination {
      margin-top: 20px;
      display: flex;
      justify-content: flex-end;
    }
  }
}
</style>
