<template>
  <div class="semester-container">
    <!-- 当前学期提示 -->
    <el-alert
      v-if="currentSemester"
      :title="`当前学期：${currentSemester.semesterName}`"
      type="success"
      :closable="false"
      style="margin-bottom: 20px"
    >
      <template #default>
        <div style="display: flex; align-items: center; gap: 20px">
          <span>当前学期：<strong>{{ currentSemester.semesterName }}</strong></span>
          <span>时间范围：{{ currentSemester.startDate }} ~ {{ currentSemester.endDate }}</span>
          <span>状态：
            <el-tag :type="getStatusType(currentSemester.status)" size="small">
              {{ getStatusText(currentSemester.status) }}
            </el-tag>
          </span>
        </div>
      </template>
    </el-alert>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索学期名称或编码"
        clearable
        style="width: 300px"
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-select
        v-model="filterStatus"
        placeholder="学期状态"
        clearable
        style="width: 150px"
        @change="handleSearch"
      >
        <el-option label="未开始" :value="0" />
        <el-option label="进行中" :value="1" />
        <el-option label="已归档" :value="2" />
      </el-select>
      <el-button type="primary" @click="handleSearch">
        <el-icon><Search /></el-icon>搜索
      </el-button>
      <div style="flex: 1"></div>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>新建学期
      </el-button>
    </div>

    <!-- 学期列表 -->
    <el-table :data="semesterList" stripe v-loading="loading" class="semester-table">
      <el-table-column label="学期信息" min-width="250">
        <template #default="{ row }">
          <div class="semester-info">
            <div class="semester-name">
              {{ row.semesterName }}
              <el-icon v-if="row.isCurrent === 1" color="#409EFF" :size="16" style="margin-left: 8px">
                <StarFilled />
              </el-icon>
            </div>
            <div class="semester-code">编码：{{ row.semesterCode }}</div>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="academicYear" label="学年" width="120" />
      <el-table-column label="学期类型" width="100">
        <template #default="{ row }">
          <el-tag size="small">
            {{ getTermName(row.termNumber) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="时间范围" width="220">
        <template #default="{ row }">
          <div style="font-size: 13px">
            <div>{{ row.startDate }}</div>
            <div style="color: #909399">{{ row.endDate }}</div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)" size="small">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="offeringCount" label="开课数" width="100" align="center">
        <template #default="{ row }">
          <el-tag type="info" size="small">{{ row.offeringCount || 0 }} 门</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="320" fixed="right">
        <template #default="{ row }">
          <!-- 未开始的学期 -->
          <template v-if="row.status === 0">
            <el-button
              v-if="row.isCurrent !== 1"
              type="primary"
              link
              size="small"
              @click="handleSetCurrent(row)"
            >
              设为当前
            </el-button>
            <el-button type="primary" link size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button
              type="danger"
              link
              size="small"
              @click="handleDelete(row)"
              :disabled="row.isCurrent === 1 || row.offeringCount > 0"
            >
              删除
            </el-button>
          </template>

          <!-- 进行中的学期 -->
          <template v-else-if="row.status === 1">
            <el-button
              v-if="row.isCurrent !== 1"
              type="primary"
              link
              size="small"
              @click="handleSetCurrent(row)"
            >
              设为当前
            </el-button>
            <el-button type="primary" link size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button type="warning" link size="small" @click="handleEnd(row)">
              结束学期
            </el-button>
          </template>

          <!-- 已归档的学期 -->
          <template v-else>
            <el-button type="info" link size="small" disabled>
              已归档
            </el-button>
            <el-button type="primary" link size="small" @click="handleView(row)">
              查看
            </el-button>
          </template>

          <el-tag v-if="row.isCurrent === 1" size="small" type="success" style="margin-left: 8px">
            当前学期
          </el-tag>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="!loading && semesterList.length === 0" description="暂无学期数据" />

    <!-- 分页 -->
    <div class="pagination-container" v-if="total > 0">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchSemesterList"
        @current-change="fetchSemesterList"
      />
    </div>

    <!-- 新建/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑学期' : '新建学期'"
      width="650px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="semesterForm"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="学期名称" prop="semesterName">
          <el-input
            v-model="semesterForm.semesterName"
            placeholder="如：2024-2025学年第1学期"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="学期编码" prop="semesterCode">
          <el-input
            v-model="semesterForm.semesterCode"
            placeholder="如：202401"
            maxlength="20"
          >
            <template #append>
              <el-tooltip content="建议格式：年份+学期序号，如：202401" placement="top">
                <el-icon><QuestionFilled /></el-icon>
              </el-tooltip>
            </template>
          </el-input>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="学年" prop="academicYear">
              <el-input v-model="semesterForm.academicYear" placeholder="如：2024-2025" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="学期类型" prop="termNumber">
              <el-select v-model="semesterForm.termNumber" placeholder="请选择" style="width: 100%">
                <el-option label="春季学期" :value="1" />
                <el-option label="夏季学期" :value="2" />
                <el-option label="秋季学期" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始日期" prop="startDate">
              <el-date-picker
                v-model="semesterForm.startDate"
                type="date"
                placeholder="选择开始日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束日期" prop="endDate">
              <el-date-picker
                v-model="semesterForm.endDate"
                type="date"
                placeholder="选择结束日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="学期描述">
          <el-input
            v-model="semesterForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入学期描述（可选）"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 查看对话框 -->
    <el-dialog v-model="viewDialogVisible" title="学期详情" width="600px">
      <el-descriptions :column="2" border v-if="viewSemester">
        <el-descriptions-item label="学期名称" :span="2">
          {{ viewSemester.semesterName }}
        </el-descriptions-item>
        <el-descriptions-item label="学期编码">
          {{ viewSemester.semesterCode }}
        </el-descriptions-item>
        <el-descriptions-item label="学年">
          {{ viewSemester.academicYear }}
        </el-descriptions-item>
        <el-descriptions-item label="学期类型">
          {{ getTermName(viewSemester.termNumber) }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(viewSemester.status)" size="small">
            {{ getStatusText(viewSemester.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="开始日期">
          {{ viewSemester.startDate }}
        </el-descriptions-item>
        <el-descriptions-item label="结束日期">
          {{ viewSemester.endDate }}
        </el-descriptions-item>
        <el-descriptions-item label="开课数量">
          {{ viewSemester.offeringCount || 0 }} 门
        </el-descriptions-item>
        <el-descriptions-item label="是否当前学期">
          <el-tag v-if="viewSemester.isCurrent === 1" type="success" size="small">是</el-tag>
          <el-tag v-else type="info" size="small">否</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="学期描述" :span="2">
          {{ viewSemester.description || '暂无描述' }}
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button type="primary" @click="viewDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import {
  getSemesterList,
  getCurrentSemester,
  createSemester,
  updateSemester,
  deleteSemester,
  setCurrentSemester,
  endSemester
} from '@/api/semester'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const semesterList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const searchKeyword = ref('')
const filterStatus = ref(null)
const currentSemester = ref(null)

const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref()
const currentSemesterId = ref(null)
const viewSemester = ref(null)

const semesterForm = reactive({
  semesterName: '',
  semesterCode: '',
  academicYear: '',
  termNumber: 1,
  startDate: '',
  endDate: '',
  description: ''
})

const formRules = {
  semesterName: [{ required: true, message: '请输入学期名称', trigger: 'blur' }],
  semesterCode: [
    { required: true, message: '请输入学期编码', trigger: 'blur' },
    { pattern: /^[A-Za-z0-9-]+$/, message: '学期编码只能包含字母、数字和连字符', trigger: 'blur' }
  ],
  academicYear: [{ required: true, message: '请输入学年', trigger: 'blur' }],
  termNumber: [{ required: true, message: '请选择学期类型', trigger: 'change' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  endDate: [
    { required: true, message: '请选择结束日期', trigger: 'change' },
    {
      validator: (rule, value, callback) => {
        if (value && semesterForm.startDate && value <= semesterForm.startDate) {
          callback(new Error('结束日期必须晚于开始日期'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ]
}

const getStatusType = (status) => {
  return status === 0 ? 'info' : status === 1 ? 'success' : 'warning'
}

const getStatusText = (status) => {
  return status === 0 ? '未开始' : status === 1 ? '进行中' : '已归档'
}

const getTermName = (termNumber) => {
  const termMap = { 1: '春季', 2: '夏季', 3: '秋季' }
  return termMap[termNumber] || '未知'
}

const fetchCurrentSemester = async () => {
  try {
    const res = await getCurrentSemester()
    currentSemester.value = res.data
  } catch (error) {
    console.error('获取当前学期失败:', error)
  }
}

const fetchSemesterList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: searchKeyword.value
    }
    if (filterStatus.value !== null && filterStatus.value !== undefined) {
      params.status = filterStatus.value
    }
    const res = await getSemesterList(params)
    semesterList.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('获取学期列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pageNum.value = 1
  fetchSemesterList()
}

const handleAdd = () => {
  isEdit.value = false
  currentSemesterId.value = null
  Object.assign(semesterForm, {
    semesterName: '',
    semesterCode: '',
    academicYear: '',
    termNumber: 1,
    startDate: '',
    endDate: '',
    description: ''
  })
  dialogVisible.value = true
}

const handleEdit = (semester) => {
  isEdit.value = true
  currentSemesterId.value = semester.id
  Object.assign(semesterForm, {
    semesterName: semester.semesterName,
    semesterCode: semester.semesterCode,
    academicYear: semester.academicYear,
    termNumber: semester.termNumber,
    startDate: semester.startDate,
    endDate: semester.endDate,
    description: semester.description || ''
  })
  dialogVisible.value = true
}

const handleView = (semester) => {
  viewSemester.value = semester
  viewDialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (isEdit.value) {
          await updateSemester(currentSemesterId.value, semesterForm)
          ElMessage.success('更新成功')
        } else {
          await createSemester(semesterForm)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        fetchSemesterList()
        fetchCurrentSemester()
      } catch (error) {
        console.error('提交失败:', error)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const handleSetCurrent = (semester) => {
  ElMessageBox.confirm(
    `确定要将"${semester.semesterName}"设置为当前学期吗？设置后全局生效，所有页面将显示该学期的数据。`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
      draggable: true
    }
  ).then(async () => {
    try {
      await setCurrentSemester(semester.id)
      ElMessage.success('设置成功，当前学期已切换')
      fetchSemesterList()
      fetchCurrentSemester()
    } catch (error) {
      console.error('设置失败:', error)
    }
  })
}

const handleEnd = (semester) => {
  ElMessageBox.confirm(
    `确定要结束"${semester.semesterName}"吗？结束后：
    1. 该学期状态将变为"已归档"
    2. 该学期的所有开课实例将变为"已结课"
    3. 已归档的学期数据仅可查看，不可修改`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
      draggable: true
    }
  ).then(async () => {
    try {
      await endSemester(semester.id)
      ElMessage.success('学期已结束并归档')
      fetchSemesterList()
      fetchCurrentSemester()
    } catch (error) {
      console.error('操作失败:', error)
    }
  })
}

const handleDelete = (semester) => {
  ElMessageBox.confirm(
    `确定要删除"${semester.semesterName}"吗？删除后无法恢复。`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
      draggable: true
    }
  ).then(async () => {
    try {
      await deleteSemester(semester.id)
      ElMessage.success('删除成功')
      fetchSemesterList()
    } catch (error) {
      console.error('删除失败:', error)
    }
  })
}

onMounted(() => {
  fetchCurrentSemester()
  fetchSemesterList()
})
</script>

<style lang="scss" scoped>
.semester-container {
  .search-bar {
    display: flex;
    gap: 12px;
    margin-bottom: 20px;
    padding: 20px;
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
    align-items: center;
  }

  .semester-table {
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
    padding: 20px;
  }

  .semester-info {
    .semester-name {
      font-size: 14px;
      font-weight: 600;
      color: #1e293b;
      margin-bottom: 4px;
      display: flex;
      align-items: center;
    }

    .semester-code {
      font-size: 12px;
      color: #64748b;
    }
  }

  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
