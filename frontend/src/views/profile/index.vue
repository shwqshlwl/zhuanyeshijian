<template>
  <div class="profile-container">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="个人信息" name="info">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>个人信息</span>
              <el-button
                v-if="!editMode"
                type="primary"
                size="small"
                @click="handleEnterEdit"
              >
                <el-icon><Edit /></el-icon>编辑信息
              </el-button>
            </div>
          </template>

          <!-- 查看模式 -->
          <div v-if="!editMode" class="user-profile">
            <div class="avatar-section">
              <el-avatar :size="100" class="user-avatar">
                {{ userStore.realName?.charAt(0) || userStore.username?.charAt(0) || 'U' }}
              </el-avatar>
              <h2 class="user-name">{{ userStore.realName || userStore.username }}</h2>
              <el-tag :type="userStore.isAdmin ? 'danger' : userStore.isTeacher ? 'primary' : 'success'">
                {{ userStore.roleText }}
              </el-tag>
            </div>
            <el-descriptions :column="1" border class="info-section">
              <el-descriptions-item label="用户名">{{ userInfo.username }}</el-descriptions-item>
              <el-descriptions-item label="真实姓名">{{ userInfo.realName }}</el-descriptions-item>
              <el-descriptions-item label="用户类型">{{ userStore.roleText }}</el-descriptions-item>
              <el-descriptions-item label="学号" v-if="userInfo.studentNo">{{ userInfo.studentNo }}</el-descriptions-item>
              <el-descriptions-item label="工号" v-if="userInfo.teacherNo">{{ userInfo.teacherNo }}</el-descriptions-item>
              <el-descriptions-item label="邮箱">{{ userInfo.email || '未设置' }}</el-descriptions-item>
              <el-descriptions-item label="手机号">{{ userInfo.phone || '未设置' }}</el-descriptions-item>
              <el-descriptions-item label="注册时间">{{ userInfo.createTime }}</el-descriptions-item>
            </el-descriptions>
          </div>

          <!-- 编辑模式 -->
          <el-form
            v-else
            ref="editFormRef"
            :model="editForm"
            :rules="editRules"
            label-width="100px"
            style="max-width: 500px; margin-top: 20px"
          >
            <el-form-item label="真实姓名" prop="realName">
              <el-input v-model="editForm.realName" placeholder="请输入真实姓名" />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="editForm.email" placeholder="请输入邮箱" />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="editForm.phone" placeholder="请输入手机号" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="editLoading" @click="handleSaveEdit">保存</el-button>
              <el-button @click="handleCancelEdit">取消</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="修改密码" name="password">
        <el-card>
          <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="100px" style="max-width: 500px">
            <el-form-item label="原密码" prop="oldPassword">
              <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码" />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="loading" @click="handleChangePassword">确认修改</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getUserInfo, changePassword, updateUserInfo } from '@/api/user'
import { ElMessage } from 'element-plus'

const route = useRoute()
const userStore = useUserStore()
const activeTab = ref(route.query.tab || 'info')
const loading = ref(false)
const userInfo = ref({})

// 编辑个人信息相关
const editMode = ref(false)
const editLoading = ref(false)
const editFormRef = ref()
const editForm = reactive({
  realName: '',
  email: '',
  phone: ''
})

const editRules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

// 修改密码相关
const passwordFormRef = ref()
const passwordForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const fetchUserInfo = async () => {
  try {
    const res = await getUserInfo()
    userInfo.value = res.data || {}
  } catch (e) {}
}

const handleEnterEdit = () => {
  Object.assign(editForm, {
    realName: userInfo.value.realName || '',
    email: userInfo.value.email || '',
    phone: userInfo.value.phone || ''
  })
  editMode.value = true
}

const handleSaveEdit = async () => {
  await editFormRef.value?.validate(async (valid) => {
    if (valid) {
      editLoading.value = true
      try {
        await updateUserInfo(editForm)
        ElMessage.success('保存成功')
        await fetchUserInfo()
        await userStore.fetchUserInfo()
        editMode.value = false
      } finally {
        editLoading.value = false
      }
    }
  })
}

const handleCancelEdit = () => {
  editMode.value = false
}

const handleChangePassword = async () => {
  await passwordFormRef.value?.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await changePassword({
          oldPassword: passwordForm.oldPassword,
          newPassword: passwordForm.newPassword
        })
        ElMessage.success('密码修改成功')
        Object.assign(passwordForm, { oldPassword: '', newPassword: '', confirmPassword: '' })
      } finally {
        loading.value = false
      }
    }
  })
}

onMounted(() => fetchUserInfo())
</script>

<style lang="scss" scoped>
.profile-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 16px;
    font-weight: 500;
  }

  .user-profile {
    .avatar-section {
      text-align: center;
      padding: 30px 0;
      border-bottom: 1px solid #f0f0f0;
      margin-bottom: 20px;

      .user-avatar {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: #fff;
        font-size: 36px;
      }

      .user-name {
        margin: 16px 0 8px;
        font-size: 20px;
        color: #303133;
      }
    }
  }

  :deep(.el-descriptions) {
    .el-descriptions__label {
      width: 150px;
    }
  }

  :deep(.el-form) {
    max-width: 600px;
  }
}
</style>
