<template>
  <div class="batch-import-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>批量导入学生</span>
        </div>
      </template>

      <el-alert type="info" :closable="false" style="margin-bottom: 20px">
        <template #title>
          <div>功能说明</div>
        </template>
        <div style="line-height: 1.8">
          <p>1. 选择目标班级（必选）</p>
          <p style="color: #E6A23C; font-weight: 500">
            ⚠️ 重要：请确保所选班级已关联课程，否则学生登录后将看不到课程
          </p>
          <p>2. 按照格式填写学生信息：学号,姓名,邮箱(可选),手机号(可选)</p>
          <p>3. 系统将自动创建学生账号（用户名=学号，默认密码=123456）</p>
          <p>4. 学生将自动关联到所选班级</p>
        </div>
      </el-alert>

      <el-form :model="importForm" label-width="100px" style="max-width: 800px">
        <el-form-item label="选择班级" required>
          <el-select
            v-model="importForm.classId"
            placeholder="请选择班级"
            clearable
            filterable
            style="width: 100%"
            @change="handleClassChange"
          >
            <el-option
              v-for="cls in classList"
              :key="cls.id"
              :label="`${cls.className} (${cls.grade} - ${cls.major})`"
              :value="cls.id"
            >
              <div style="display: flex; justify-content: space-between; align-items: center">
                <span>{{ cls.className }}</span>
                <div style="display: flex; align-items: center; gap: 8px">
                  <span style="color: #909399; font-size: 12px">{{ cls.grade }} - {{ cls.major }}</span>
                  <el-tag v-if="cls.courseId" type="success" size="small">已关联课程</el-tag>
                  <el-tag v-else type="warning" size="small">未关联课程</el-tag>
                </div>
              </div>
            </el-option>
          </el-select>
          <div v-if="importForm.classId && selectedClass && !selectedClass.courseId" style="color: #E6A23C; font-size: 13px; margin-top: 4px">
            ⚠️ 该班级尚未关联课程，学生导入后将看不到课程。建议先在"班级管理"中关联课程。
          </div>
          <div v-if="importForm.classId && selectedClass && selectedClass.courseName" style="color: #67C23A; font-size: 13px; margin-top: 4px">
            ✓ 该班级已关联课程：{{ selectedClass.courseName }}
          </div>
        </el-form-item>

        <el-form-item label="学生信息" required>
          <el-input
            v-model="importForm.content"
            type="textarea"
            :rows="12"
            placeholder="请按照以下格式填写，每行一个学生：
学号,姓名,邮箱(可选),手机号(可选)

示例：
2024001,张三,zhangsan@example.com,13800138001
2024002,李四,lisi@example.com,13800138002
2024003,王五"
            style="font-family: 'Courier New', monospace"
          />
          <div style="margin-top: 8px; color: #909399; font-size: 13px">
            <el-icon><InfoFilled /></el-icon>
            支持CSV格式，邮箱和手机号为可选项
          </div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="importing" @click="handleImport" size="large">
            <el-icon><Upload /></el-icon>开始导入
          </el-button>
          <el-button @click="handleClear" size="large">清空</el-button>
        </el-form-item>
      </el-form>

      <!-- 导入结果 -->
      <el-card v-if="importResult" class="result-card" shadow="never">
        <template #header>
          <div class="result-header">
            <el-icon :size="20" :color="importResult.failCount > 0 ? '#E6A23C' : '#67C23A'">
              <CircleCheck v-if="importResult.failCount === 0" />
              <Warning v-else />
            </el-icon>
            <span style="margin-left: 8px">导入结果</span>
          </div>
        </template>

        <el-descriptions :column="3" border>
          <el-descriptions-item label="成功数量">
            <el-tag type="success">{{ importResult.successCount }} 人</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="失败数量">
            <el-tag :type="importResult.failCount > 0 ? 'danger' : 'info'">{{ importResult.failCount }} 人</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="总计">
            {{ importResult.successCount + importResult.failCount }} 人
          </el-descriptions-item>
        </el-descriptions>

        <div v-if="importResult.failCount > 0 && importResult.failDetails" class="fail-details">
          <div class="fail-title">失败详情：</div>
          <el-alert type="warning" :closable="false">
            <div style="white-space: pre-line">{{ importResult.failDetails }}</div>
          </el-alert>
        </div>
      </el-card>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { getClassList } from '@/api/class'
import { batchImportStudents } from '@/api/user'
import { ElMessage } from 'element-plus'

const importing = ref(false)
const classList = ref([])
const importResult = ref(null)

const importForm = reactive({
  classId: null,
  content: ''
})

// 选中的班级
const selectedClass = computed(() => {
  return classList.value.find(c => c.id === importForm.classId)
})

// 获取班级列表
const fetchClassList = async () => {
  try {
    const res = await getClassList({ pageNum: 1, pageSize: 100 })
    classList.value = res.data?.records || []
  } catch (error) {
    console.error('获取班级列表失败:', error)
  }
}

// 班级变更
const handleClassChange = () => {
  // 班级变更时可以做一些处理
}

// 清空表单
const handleClear = () => {
  importForm.classId = null
  importForm.content = ''
  importResult.value = null
}

// 导入学生
const handleImport = async () => {
  // 验证
  if (!importForm.classId) {
    ElMessage.warning('请选择班级')
    return
  }

  if (!importForm.content.trim()) {
    ElMessage.warning('请输入学生信息')
    return
  }

  // 解析学生数据
  const lines = importForm.content.trim().split('\n')
  const students = []

  for (const line of lines) {
    const parts = line.split(',').map(s => s.trim())
    if (parts.length >= 2) {
      students.push({
        studentNo: parts[0],
        realName: parts[1],
        email: parts[2] || '',
        phone: parts[3] || ''
      })
    }
  }

  if (students.length === 0) {
    ElMessage.warning('未解析到有效的学生信息，请检查格式')
    return
  }

  importing.value = true
  try {
    const res = await batchImportStudents({
      classId: Number(importForm.classId),
      students
    })

    importResult.value = res.data

    if (res.data.failCount === 0) {
      ElMessage.success(`导入成功！共导入 ${res.data.successCount} 名学生`)
      // 清空输入框
      importForm.content = ''
    } else {
      ElMessage.warning(`导入完成：成功 ${res.data.successCount} 人，失败 ${res.data.failCount} 人`)
    }
  } catch (error) {
    console.error('导入失败:', error)
    ElMessage.error('导入失败，请检查数据格式或联系管理员')
  } finally {
    importing.value = false
  }
}

onMounted(() => {
  fetchClassList()
})
</script>

<style lang="scss" scoped>
.batch-import-container {
  max-width: 1200px;
  margin: 0 auto;

  .card-header {
    font-size: 18px;
    font-weight: 500;
  }

  .result-card {
    margin-top: 24px;
    background: #f5f7fa;

    .result-header {
      display: flex;
      align-items: center;
      font-size: 16px;
      font-weight: 500;
    }

    .fail-details {
      margin-top: 16px;

      .fail-title {
        font-size: 14px;
        font-weight: 500;
        color: #606266;
        margin-bottom: 8px;
      }
    }
  }
}
</style>
