<template>
  <div class="users-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <h3>用户管理</h3>
          <span class="subtitle">管理系统中的所有用户</span>
        </div>
      </template>

      <!-- 搜索筛选 -->
      <div class="filter-section">
        <el-form :inline="true">
          <el-form-item label="用户类型">
            <el-select
              v-model="filters.userType"
              placeholder="全部"
              clearable
              style="width: 150px"
              @change="handleFilter"
            >
              <el-option label="全部" :value="null" />
              <el-option label="学生" :value="1" />
              <el-option label="教师" :value="2" />
              <el-option label="管理员" :value="3" />
            </el-select>
          </el-form-item>
          <el-form-item label="关键词">
            <el-input
              v-model="filters.keyword"
              placeholder="用户名/姓名/学号/工号"
              clearable
              style="width: 250px"
              @keyup.enter="handleFilter"
              @clear="handleFilter"
            >
              <template #append>
                <el-button :icon="Search" @click="handleFilter" />
              </template>
            </el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Refresh" @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 用户列表 -->
      <el-table
        v-loading="loading"
        :data="users"
        stripe
        style="width: 100%"
        class="users-table"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="realName" label="真实姓名" width="120" />
        <el-table-column label="用户类型" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.userType === 1" type="info">学生</el-tag>
            <el-tag v-else-if="row.userType === 2" type="success">教师</el-tag>
            <el-tag v-else-if="row.userType === 3" type="danger">管理员</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="学号/工号" width="150">
          <template #default="{ row }">
            {{ row.studentNo || row.teacherNo || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" width="200" show-overflow-tooltip />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 1" type="success">正常</el-tag>
            <el-tag v-else-if="row.status === 2" type="warning">待审核</el-tag>
            <el-tag v-else type="danger">禁用</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.userType === 2 && row.status === 2"
              type="primary"
              size="small"
              @click="handleAudit(row)"
            >
              审核
            </el-button>
            <el-button
              v-if="row.status === 1 && row.userType !== 3"
              type="warning"
              size="small"
              :icon="Lock"
              @click="handleToggleStatus(row, 0)"
            >
              禁用
            </el-button>
            <el-button
              v-if="row.status === 0"
              type="success"
              size="small"
              :icon="Unlock"
              @click="handleToggleStatus(row, 1)"
            >
              启用
            </el-button>
            <el-button
              type="primary"
              size="small"
              :icon="Key"
              @click="handleResetPassword(row)"
            >
              重置密码
            </el-button>
            <el-button
              v-if="row.userType !== 3"
              type="danger"
              size="small"
              :icon="Delete"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadUsers"
          @current-change="loadUsers"
        />
      </div>
    </el-card>

    <!-- 审核对话框 -->
    <el-dialog
      v-model="auditDialog.visible"
      title="审核教师注册"
      width="500px"
    >
      <div v-if="auditDialog.teacher">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="姓名">
            {{ auditDialog.teacher.realName }}
          </el-descriptions-item>
          <el-descriptions-item label="用户名">
            {{ auditDialog.teacher.username }}
          </el-descriptions-item>
          <el-descriptions-item label="工号">
            {{ auditDialog.teacher.teacherNo }}
          </el-descriptions-item>
          <el-descriptions-item label="邮箱">
            {{ auditDialog.teacher.email || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="手机号">
            {{ auditDialog.teacher.phone || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="注册时间">
            {{ auditDialog.teacher.createTime }}
          </el-descriptions-item>
        </el-descriptions>

        <el-form :model="auditDialog" style="margin-top: 20px">
          <el-form-item label="审核结果" required>
            <el-radio-group v-model="auditDialog.status">
              <el-radio :label="1">通过</el-radio>
              <el-radio :label="0">拒绝</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <el-button @click="auditDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitAudit" :loading="auditDialog.loading">
          确认
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Lock, Unlock, Key, Delete } from '@element-plus/icons-vue'
import request from '@/utils/request'

// 数据
const loading = ref(false)
const users = ref([])

const filters = reactive({
  userType: null,
  keyword: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 审核对话框
const auditDialog = reactive({
  visible: false,
  teacher: null,
  status: 1, // 默认通过
  loading: false
})

// 加载用户列表
const loadUsers = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.current,
      pageSize: pagination.size
    }
    if (filters.keyword) params.keyword = filters.keyword
    if (filters.userType !== null) params.userType = filters.userType

    const data = await request.get('/admin/users', { params })
    users.value = data.data.records
    pagination.total = data.data.total
  } catch (error) {
    console.error('加载用户列表失败:', error)
    // 错误已在拦截器中处理，这里不需要再次提示
  } finally {
    loading.value = false
  }
}

// 筛选
const handleFilter = () => {
  pagination.current = 1
  loadUsers()
}

// 重置
const handleReset = () => {
  filters.userType = null
  filters.keyword = ''
  handleFilter()
}

// 切换用户状态
const handleToggleStatus = async (user, status) => {
  try {
    const action = status === 1 ? '启用' : '禁用'
    await ElMessageBox.confirm(`确定要${action}用户"${user.realName}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const data = await request.put(`/admin/users/${user.id}/status`, null, {
      params: { status }
    })

    ElMessage.success(data.message || `${action}成功`)
    loadUsers()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('更新用户状态失败:', error)
      // 错误已在拦截器中处理
    }
  }
}

// 重置密码
const handleResetPassword = async (user) => {
  try {
    await ElMessageBox.confirm(
      `确定要重置用户"${user.realName}"的密码吗？密码将重置为 123456`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const data = await request.put(`/admin/users/${user.id}/reset-password`)
    ElMessage.success(data.message || '密码重置成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('重置密码失败:', error)
      // 错误已在拦截器中处理
    }
  }
}

// 删除用户
const handleDelete = async (user) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户"${user.realName}"吗？此操作不可恢复！`,
      '危险操作',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'error'
      }
    )

    const data = await request.delete(`/admin/users/${user.id}`)
    ElMessage.success(data.message || '删除成功')
    loadUsers()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除用户失败:', error)
      // 错误已在拦截器中处理
    }
  }
}

// 打开审核对话框
const handleAudit = (teacher) => {
  auditDialog.teacher = teacher
  auditDialog.status = 1 // 默认通过
  auditDialog.visible = true
}

// 提交审核
const submitAudit = async () => {
  try {
    auditDialog.loading = true

    const action = auditDialog.status === 1 ? '通过' : '拒绝'
    await request.put(`/admin/teachers/${auditDialog.teacher.id}/audit`, null, {
      params: { status: auditDialog.status }
    })

    ElMessage.success(`审核${action}`)
    auditDialog.visible = false
    loadUsers()
  } catch (error) {
    console.error('审核失败:', error)
    // 错误已在拦截器中处理
  } finally {
    auditDialog.loading = false
  }
}

onMounted(() => {
  loadUsers()
})
</script>

<style lang="scss" scoped>
.users-container {
  padding: 20px;
}

.card-header {
  h3 {
    margin: 0 0 4px 0;
    font-size: 18px;
    color: #303133;
  }

  .subtitle {
    font-size: 13px;
    color: #909399;
  }
}

.filter-section {
  margin-bottom: 20px;
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.users-table {
  margin-top: 16px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
