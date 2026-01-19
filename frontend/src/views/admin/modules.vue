<template>
  <div class="module-management">
    <div class="page-header">
      <h2>模块权限管理</h2>
      <p class="description">管理不同角色用户可见的侧边栏菜单模块</p>
    </div>

    <el-row :gutter="24" class="role-cards" v-loading="loading">
      <!-- 学生卡片 -->
      <el-col :xs="24" :sm="12" :lg="8">
        <el-card class="role-card" shadow="hover">
          <div class="role-card-content">
            <div class="role-icon student-icon">
              <el-icon :size="64"><User /></el-icon>
            </div>
            <h3 class="role-name">学生</h3>
            <div class="role-stats">
              <span class="stats-number">{{ getEnabledCount(1) }}</span>
              <span class="stats-label">个模块已开放</span>
            </div>
            <el-button type="primary" @click="openConfigDialog(1)" class="config-btn">
              <el-icon><Setting /></el-icon>
              配置权限
            </el-button>
          </div>
        </el-card>
      </el-col>

      <!-- 教师卡片 -->
      <el-col :xs="24" :sm="12" :lg="8">
        <el-card class="role-card" shadow="hover">
          <div class="role-card-content">
            <div class="role-icon teacher-icon">
              <el-icon :size="64"><Reading /></el-icon>
            </div>
            <h3 class="role-name">教师</h3>
            <div class="role-stats">
              <span class="stats-number">{{ getEnabledCount(2) }}</span>
              <span class="stats-label">个模块已开放</span>
            </div>
            <el-button type="success" @click="openConfigDialog(2)" class="config-btn">
              <el-icon><Setting /></el-icon>
              配置权限
            </el-button>
          </div>
        </el-card>
      </el-col>

      <!-- 管理员卡片 -->
      <el-col :xs="24" :sm="12" :lg="8">
        <el-card class="role-card" shadow="hover">
          <div class="role-card-content">
            <div class="role-icon admin-icon">
              <el-icon :size="64"><UserFilled /></el-icon>
            </div>
            <h3 class="role-name">管理员</h3>
            <div class="role-stats">
              <span class="stats-number">{{ getEnabledCount(3) }}</span>
              <span class="stats-label">个模块已开放</span>
            </div>
            <el-button type="danger" @click="openConfigDialog(3)" class="config-btn">
              <el-icon><Setting /></el-icon>
              配置权限
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 配置权限对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="`配置【${getRoleName(currentRoleType)}】模块权限`"
      width="500px"
      :close-on-click-modal="false"
    >
      <div class="permission-config" v-loading="dialogLoading">
        <el-checkbox-group v-model="selectedModules" class="module-checkbox-group">
          <div
            v-for="module in modules"
            :key="module.id"
            class="module-checkbox-item"
          >
            <el-checkbox
              :label="module.id"
              :disabled="isModuleDisabled(module)"
            >
              <div class="module-info">
                <div class="module-main">
                  <el-icon class="module-icon">
                    <component :is="module.icon || 'Document'" />
                  </el-icon>
                  <span class="module-name">{{ module.moduleName }}</span>
                </div>
                <span class="module-path">{{ module.path }}</span>
              </div>
            </el-checkbox>
          </div>
        </el-checkbox-group>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="savePermissions" :loading="saving">
            保存
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const modules = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const saving = ref(false)
const currentRoleType = ref(null)
const selectedModules = ref([])

const roleNames = {
  1: '学生',
  2: '教师',
  3: '管理员'
}

const loadModules = async () => {
  loading.value = true
  try {
    const data = await request.get('/admin/modules')
    modules.value = data.data
  } catch (error) {
    ElMessage.error('加载模块列表失败')
  } finally {
    loading.value = false
  }
}

const getEnabledCount = (roleType) => {
  return modules.value.filter(m => m.rolePermissions[roleType] === 1).length
}

const getRoleName = (roleType) => {
  return roleNames[roleType] || ''
}

const isModuleDisabled = (module) => {
  // 首页对所有角色都必须开放
  if (module.moduleKey === 'dashboard') {
    return true
  }
  // 管理员的模块管理权限不能禁用
  if (currentRoleType.value === 3 && module.moduleKey === 'admin-modules') {
    return true
  }
  return false
}

const openConfigDialog = (roleType) => {
  currentRoleType.value = roleType
  // 获取该角色已启用的模块ID列表
  selectedModules.value = modules.value
    .filter(m => m.rolePermissions[roleType] === 1)
    .map(m => m.id)
  dialogVisible.value = true
}

const savePermissions = async () => {
  saving.value = true
  try {
    // 计算需要更新的权限
    const updates = []

    for (const module of modules.value) {
      const isSelected = selectedModules.value.includes(module.id)
      const currentEnabled = module.rolePermissions[currentRoleType.value]
      const newEnabled = isSelected ? 1 : 0

      // 只更新状态变化的权限
      if (currentEnabled !== newEnabled) {
        updates.push({
          roleType: currentRoleType.value,
          moduleId: module.id,
          enabled: newEnabled
        })
      }
    }

    // 批量调用API更新权限
    if (updates.length > 0) {
      await Promise.all(
        updates.map(update =>
          request.put('/admin/modules/role-permission', update)
        )
      )
      ElMessage.success(`成功更新 ${updates.length} 个模块权限`)
    } else {
      ElMessage.info('没有权限变更')
    }

    // 刷新模块列表
    await loadModules()
    dialogVisible.value = false
  } catch (error) {
    ElMessage.error(error.message || '保存权限失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadModules()
})
</script>

<style lang="scss" scoped>
.module-management {
  .page-header {
    margin-bottom: 32px;

    h2 {
      font-size: 24px;
      font-weight: 600;
      color: #1e293b;
      margin: 0 0 8px;
    }

    .description {
      font-size: 14px;
      color: #64748b;
      margin: 0;
    }
  }

  .role-cards {
    margin-bottom: 20px;
  }

  .role-card {
    margin-bottom: 20px;
    border-radius: 12px;
    transition: all 0.3s ease;

    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 12px 24px rgba(0, 0, 0, 0.15);
    }

    .role-card-content {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 24px 16px;

      .role-icon {
        width: 120px;
        height: 120px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-bottom: 20px;
        color: #fff;
        transition: transform 0.3s ease;

        &:hover {
          transform: scale(1.1);
        }

        &.student-icon {
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        }

        &.teacher-icon {
          background: linear-gradient(135deg, #10b981 0%, #059669 100%);
        }

        &.admin-icon {
          background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
        }
      }

      .role-name {
        font-size: 22px;
        font-weight: 600;
        color: #1e293b;
        margin: 0 0 16px;
      }

      .role-stats {
        display: flex;
        flex-direction: column;
        align-items: center;
        margin-bottom: 24px;

        .stats-number {
          font-size: 36px;
          font-weight: 700;
          color: #3b82f6;
          line-height: 1;
        }

        .stats-label {
          font-size: 13px;
          color: #64748b;
          margin-top: 8px;
        }
      }

      .config-btn {
        width: 100%;
        height: 44px;
        font-size: 15px;
        font-weight: 500;
        border-radius: 8px;
      }
    }
  }

  .permission-config {
    max-height: 500px;
    overflow-y: auto;
    padding: 8px 0;

    .module-checkbox-group {
      display: flex;
      flex-direction: column;
      width: 100%;

      .module-checkbox-item {
        padding: 12px 0;
        border-bottom: 1px solid #f1f5f9;

        &:last-child {
          border-bottom: none;
        }

        :deep(.el-checkbox) {
          width: 100%;
          height: auto;
          display: flex;
          align-items: center;

          .el-checkbox__label {
            width: 100%;
            padding-left: 12px;
          }
        }

        .module-info {
          display: flex;
          justify-content: space-between;
          align-items: center;
          width: 100%;

          .module-main {
            display: flex;
            align-items: center;
            flex: 1;

            .module-icon {
              margin-right: 8px;
              color: #3b82f6;
              font-size: 18px;
            }

            .module-name {
              font-size: 14px;
              font-weight: 500;
              color: #1e293b;
            }
          }

          .module-path {
            font-size: 12px;
            color: #94a3b8;
            font-family: monospace;
            margin-left: 12px;
            white-space: nowrap;
          }
        }
      }
    }
  }

  .dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
  }
}

// 滚动条样式
.permission-config::-webkit-scrollbar {
  width: 6px;
}

.permission-config::-webkit-scrollbar-track {
  background: #f1f5f9;
  border-radius: 3px;
}

.permission-config::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 3px;

  &:hover {
    background: #94a3b8;
  }
}

@media (max-width: 768px) {
  .role-cards {
    :deep(.el-col) {
      margin-bottom: 0;
    }
  }
}
</style>
