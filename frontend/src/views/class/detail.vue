<template>
  <div class="class-detail">
    <el-page-header @back="$router.back()">
      <template #content>
        <span>{{ classInfo.className || '班级详情' }}</span>
      </template>
    </el-page-header>

    <div class="detail-content" v-loading="loading">
      <!-- 基本信息 -->
      <el-card class="info-card">
        <template #header>
          <div class="card-header">
            <span>基本信息</span>
            <el-button type="primary" size="small" @click="handleEdit" v-if="userStore.isTeacher">
              <el-icon><Edit /></el-icon>编辑
            </el-button>
          </div>
        </template>
        <el-descriptions :column="3" border>
          <el-descriptions-item label="班级名称">{{ classInfo.className }}</el-descriptions-item>
          <el-descriptions-item label="班级编码">{{ classInfo.code }}</el-descriptions-item>
          <el-descriptions-item label="年级">{{ classInfo.grade }}</el-descriptions-item>
          <el-descriptions-item label="专业">{{ classInfo.major }}</el-descriptions-item>
          <el-descriptions-item label="学生人数">{{ classInfo.studentCount || 0 }} 人</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ classInfo.createTime }}</el-descriptions-item>
          <el-descriptions-item label="班级描述" :span="3">{{ classInfo.description || '暂无描述' }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 关联课程 -->
      <el-card class="course-card">
        <template #header>
          <div class="card-header">
            <span>关联课程</span>
            <el-button type="primary" size="small" @click="handleOpenBindDialog" v-if="userStore.isTeacher">
              <el-icon><Plus /></el-icon>关联课程
            </el-button>
          </div>
        </template>
        <el-table :data="relatedCourses" stripe>
          <el-table-column prop="courseName" label="课程名称" />
          <el-table-column prop="courseCode" label="课程编码" width="150" />
          <el-table-column prop="teacherName" label="授课教师" width="120" />
          <el-table-column prop="credit" label="学分" width="80" />
          <el-table-column label="操作" width="100" v-if="userStore.isTeacher">
            <template #default="{ row }">
              <el-button type="danger" link size="small" @click="handleUnbindCourse(row)">取消关联</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="relatedCourses.length === 0" description="暂无关联课程" />
      </el-card>

      <!-- 学生名单 -->
      <el-card class="student-card">
        <template #header>
          <div class="card-header">
            <span>学生名单 ({{ studentTotal }} 人)</span>
            <div class="header-actions" v-if="userStore.isTeacher">
              <el-button type="primary" size="small" @click="showAddStudentDialog = true">
                <el-icon><Plus /></el-icon>添加学生
              </el-button>
              <el-button type="success" size="small" @click="showImportDialog = true">
                <el-icon><Upload /></el-icon>批量导入
              </el-button>
            </div>
          </div>
        </template>
        
        <el-table :data="studentList" stripe v-loading="studentLoading">
          <el-table-column prop="studentNo" label="学号" width="150" />
          <el-table-column prop="realName" label="姓名" width="120" />
          <el-table-column prop="username" label="用户名" width="150" />
          <el-table-column prop="email" label="邮箱" />
          <el-table-column prop="phone" label="手机号" width="130" />
          <el-table-column label="操作" width="100" fixed="right" v-if="userStore.isTeacher">
            <template #default="{ row }">
              <el-popconfirm title="确定要移除该学生吗？" @confirm="handleRemoveStudent(row)">
                <template #reference>
                  <el-button type="danger" link size="small">移除</el-button>
                </template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>
        
        <div class="pagination-container" v-if="studentTotal > 0">
          <el-pagination
            v-model:current-page="studentPageNum"
            v-model:page-size="studentPageSize"
            :total="studentTotal"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            @size-change="fetchStudents"
            @current-change="fetchStudents"
          />
        </div>
      </el-card>
    </div>

    <!-- 添加学生对话框 -->
    <el-dialog v-model="showAddStudentDialog" title="添加学生" width="400px">
      <el-form :model="addStudentForm" label-width="80px">
        <el-form-item label="学号">
          <el-input v-model="addStudentForm.studentNo" placeholder="请输入学生学号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddStudentDialog = false">取消</el-button>
        <el-button type="primary" :loading="addStudentLoading" @click="handleAddStudent">确定</el-button>
      </template>
    </el-dialog>

    <!-- 批量导入学生对话框 -->
    <el-dialog v-model="showImportDialog" title="批量导入学生" width="700px">
      <el-alert type="info" :closable="false" style="margin-bottom: 16px">
        <template #title>
          <div>请按以下格式填写学生信息，每行一个学生：</div>
          <div style="color: #909399; margin-top: 4px">学号,姓名,邮箱(可选),手机号(可选)</div>
        </template>
      </el-alert>
      <el-input
        v-model="importContent"
        type="textarea"
        :rows="10"
        placeholder="示例：
2024001,张三,zhangsan@example.com,13800138001
2024002,李四,lisi@example.com,13800138002
2024003,王五"
      />
      <template #footer>
        <el-button @click="showImportDialog = false">取消</el-button>
        <el-button type="primary" :loading="importLoading" @click="handleBatchImport">导入</el-button>
      </template>
    </el-dialog>

    <!-- 关联课程对话框 -->
    <el-dialog v-model="showBindCourseDialog" title="关联课程" width="500px">
      <el-form :model="bindCourseForm" label-width="80px">
        <el-form-item label="选择课程">
          <el-select
            v-model="bindCourseForm.courseIds"
            multiple
            collapse-tags
            collapse-tags-tooltip
            filterable
            placeholder="请选择课程（可多选）"
            style="width: 100%"
            clearable
          >
            <el-option v-for="c in availableCourses" :key="c.id" :label="c.courseName" :value="c.id">
              <span>{{ c.courseName }}</span>
              <span style="color: #909399; margin-left: 8px">{{ c.courseCode }}</span>
            </el-option>
          </el-select>
          <div style="font-size: 12px; color: #909399; margin-top: 4px">
            已选择 {{ bindCourseForm.courseIds?.length || 0 }} 门课程
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showBindCourseDialog = false">取消</el-button>
        <el-button type="primary" :loading="bindCourseLoading" @click="handleBindCourse">确定</el-button>
      </template>
    </el-dialog>

    <!-- 编辑班级对话框 -->
    <el-dialog v-model="showEditDialog" title="编辑班级" width="500px">
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="80px">
        <el-form-item label="班级名称" prop="className">
          <el-input v-model="editForm.className" placeholder="请输入班级名称" />
        </el-form-item>
        <el-form-item label="年级" prop="grade">
          <el-input v-model="editForm.grade" placeholder="如：2024" />
        </el-form-item>
        <el-form-item label="专业" prop="major">
          <el-input v-model="editForm.major" placeholder="请输入专业名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="editForm.description" type="textarea" :rows="3" placeholder="班级描述（选填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" :loading="editLoading" @click="handleSaveEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getClassById, updateClass, addStudentToClass, removeStudentFromClass, getClassCourses, setClassCourses } from '@/api/class'
import { getCourseList } from '@/api/course'
import { batchImportStudents, getStudentByStudentNo } from '@/api/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const route = useRoute()
const userStore = useUserStore()
const classId = route.params.id

const loading = ref(false)
const classInfo = ref({})
const relatedCourses = ref([])

// 学生列表
const studentLoading = ref(false)
const studentList = ref([])
const studentTotal = ref(0)
const studentPageNum = ref(1)
const studentPageSize = ref(10)

// 添加学生
const showAddStudentDialog = ref(false)
const addStudentLoading = ref(false)
const addStudentForm = reactive({ studentNo: '' })

// 批量导入
const showImportDialog = ref(false)
const importLoading = ref(false)
const importContent = ref('')

// 关联课程
const showBindCourseDialog = ref(false)
const bindCourseLoading = ref(false)
const bindCourseForm = reactive({ courseIds: [] })
const availableCourses = ref([])

// 编辑班级
const showEditDialog = ref(false)
const editLoading = ref(false)
const editFormRef = ref()
const editForm = reactive({ className: '', grade: '', major: '', description: '' })
const editRules = { className: [{ required: true, message: '请输入班级名称', trigger: 'blur' }] }

// 获取班级详情
const fetchClassDetail = async () => {
  loading.value = true
  try {
    const res = await getClassById(classId)
    classInfo.value = res.data || {}
    relatedCourses.value = res.data?.courses || []
    console.log('✅ 班级详情加载成功')
    console.log('📚 关联课程数量:', relatedCourses.value.length)
    console.log('📋 关联课程列表:', relatedCourses.value)
  } finally {
    loading.value = false
  }
}

// 获取学生列表
const fetchStudents = async () => {
  studentLoading.value = true
  try {
    const res = await request({
      url: `/classes/${classId}/students`,
      method: 'get',
      params: { pageNum: studentPageNum.value, pageSize: studentPageSize.value }
    })
    studentList.value = res.data?.records || []
    studentTotal.value = res.data?.total || 0
  } finally {
    studentLoading.value = false
  }
}

// 获取可用课程列表（排除已关联的课程）
const fetchAvailableCourses = async () => {
  const res = await getCourseList({ pageNum: 1, pageSize: 100 })
  const allCourses = res.data?.records || []

  // 过滤掉已关联的课程
  const linkedCourseIds = relatedCourses.value.map(c => c.id)
  availableCourses.value = allCourses.filter(c => !linkedCourseIds.includes(c.id))
}

// 打开关联课程对话框
const handleOpenBindDialog = async () => {
  bindCourseForm.courseIds = []
  await fetchAvailableCourses()
  showBindCourseDialog.value = true
}

// 添加学生
const handleAddStudent = async () => {
  if (!addStudentForm.studentNo.trim()) {
    ElMessage.warning('请输入学号')
    return
  }
  addStudentLoading.value = true
  try {
    // 先查询学生是否存在
    const studentRes = await getStudentByStudentNo(addStudentForm.studentNo)
    if (!studentRes.data) {
      ElMessage.error('学号不存在，请先创建学生账号')
      return
    }
    // 添加到班级
    await addStudentToClass(classId, studentRes.data.id)
    ElMessage.success('添加成功')
    showAddStudentDialog.value = false
    addStudentForm.studentNo = ''
    fetchStudents()
    fetchClassDetail()
  } finally {
    addStudentLoading.value = false
  }
}

// 移除学生
const handleRemoveStudent = async (row) => {
  try {
    await removeStudentFromClass(classId, row.id)
    ElMessage.success('移除成功')
    fetchStudents()
    fetchClassDetail()
  } catch (e) {
    console.error(e)
  }
}

// 批量导入学生
const handleBatchImport = async () => {
  if (!importContent.value.trim()) {
    ElMessage.warning('请输入学生信息')
    return
  }
  
  const lines = importContent.value.trim().split('\n')
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
    ElMessage.warning('未解析到有效的学生信息')
    return
  }
  
  importLoading.value = true
  try {
    const res = await batchImportStudents({ classId: Number(classId), students })
    const result = res.data
    ElMessage.success(`导入完成：成功 ${result.successCount} 人，失败 ${result.failCount} 人`)
    if (result.failCount > 0 && result.failDetails) {
      ElMessageBox.alert(result.failDetails, '导入失败详情', { type: 'warning' })
    }
    showImportDialog.value = false
    importContent.value = ''
    fetchStudents()
    fetchClassDetail()
  } finally {
    importLoading.value = false
  }
}

// 关联课程（添加多个课程）
const handleBindCourse = async () => {
  if (!bindCourseForm.courseIds || bindCourseForm.courseIds.length === 0) {
    ElMessage.warning('请至少选择一门课程')
    return
  }
  bindCourseLoading.value = true
  try {
    // 合并现有的课程ID和新选择的课程ID
    const existingCourseIds = relatedCourses.value.map(c => c.id)
    const allCourseIds = [...new Set([...existingCourseIds, ...bindCourseForm.courseIds])]

    // 使用新的批量设置API
    await setClassCourses(classId, allCourseIds)
    ElMessage.success('关联成功')
    showBindCourseDialog.value = false
    bindCourseForm.courseIds = []
    await fetchClassDetail()
    await fetchAvailableCourses()
  } finally {
    bindCourseLoading.value = false
  }
}

// 取消关联课程（移除单个课程）
const handleUnbindCourse = (row) => {
  ElMessageBox.confirm(`确定要取消关联课程"${row.courseName}"吗？`, '提示', { type: 'warning' }).then(async () => {
    try {
      // 从现有课程ID列表中移除该课程
      const existingCourseIds = relatedCourses.value.map(c => c.id)
      const updatedCourseIds = existingCourseIds.filter(id => id !== row.id)

      // 使用新的批量设置API
      await setClassCourses(classId, updatedCourseIds)
      ElMessage.success('取消关联成功')
      await fetchClassDetail()
      await fetchAvailableCourses()
    } catch (e) {
      console.error(e)
    }
  })
}

// 编辑班级
const handleEdit = () => {
  Object.assign(editForm, {
    className: classInfo.value.className,
    grade: classInfo.value.grade,
    major: classInfo.value.major,
    description: classInfo.value.description
  })
  showEditDialog.value = true
}

const handleSaveEdit = async () => {
  await editFormRef.value?.validate(async (valid) => {
    if (valid) {
      editLoading.value = true
      try {
        await updateClass(classId, editForm)
        ElMessage.success('保存成功')
        showEditDialog.value = false
        fetchClassDetail()
      } finally {
        editLoading.value = false
      }
    }
  })
}

onMounted(() => {
  fetchClassDetail()
  fetchStudents()
})
</script>

<style lang="scss" scoped>
.class-detail {
  .detail-content {
    margin-top: 20px;
  }
  
  .info-card, .course-card, .student-card {
    margin-bottom: 20px;
  }
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .header-actions {
      display: flex;
      gap: 8px;
    }
  }
  
  .pagination-container {
    margin-top: 16px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
