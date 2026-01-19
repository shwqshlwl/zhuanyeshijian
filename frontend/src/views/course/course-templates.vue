<template>
  <div class="course-template-container">
    <!-- 学期选择器 -->
    <div class="semester-bar">
      <SemesterSelector v-model="selectedSemesterId" @change="handleSemesterChange" />
      <div style="margin-left: 16px; color: #606266; font-size: 14px">
        说明：选择学期后，可以查看您的课程模板在该学期的开课情况，并可为新学期开课
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索课程名称或编码"
        clearable
        style="width: 300px"
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-button type="primary" @click="handleSearch">
        <el-icon><Search /></el-icon>搜索
      </el-button>
      <div style="flex: 1"></div>
      <el-button type="primary" @click="handleCreateTemplate">
        <el-icon><Plus /></el-icon>新建课程模板
      </el-button>
    </div>

    <!-- 课程模板列表 -->
    <el-table :data="templateList" stripe v-loading="loading" class="template-table">
      <el-table-column label="课程信息" min-width="300">
        <template #default="{ row }">
          <div class="course-info">
            <div class="course-name">
              {{ row.courseName }}
              <el-tag :type="row.courseType === 1 ? 'warning' : 'success'" size="small" style="margin-left: 8px">
                {{ row.courseType === 1 ? '选修' : '必修' }}
              </el-tag>
            </div>
            <div class="course-code">编码：{{ row.courseCode }}</div>
            <div class="course-meta">
              学分：{{ row.credit }} | 学时：{{ row.totalHours || '-' }} | 历史开课：{{ row.offeringCount || 0 }} 次
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="本学期开课状态" width="200" align="center">
        <template #default="{ row }">
          <div v-if="!selectedSemesterId" style="color: #909399">
            请先选择学期
          </div>
          <div v-else-if="row.isOfferedThisSemester">
            <el-tag type="success" size="large">已开课</el-tag>
            <div style="margin-top: 8px; font-size: 12px; color: #606266">
              授课教师：{{ row.offeringTeacherName }}
            </div>
            <div style="font-size: 12px; color: #909399">
              状态：{{ getOfferingStatusText(row.offeringStatus) }}
            </div>
          </div>
          <div v-else>
            <el-tag type="info" size="large">未开课</el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="300" fixed="right">
        <template #default="{ row }">
          <template v-if="!selectedSemesterId">
            <el-button type="primary" link size="small" disabled>
              请先选择学期
            </el-button>
          </template>
          <template v-else-if="row.isOfferedThisSemester">
            <el-button type="primary" link size="small" @click="goToOffering(row.offeringId)">
              进入课程
            </el-button>
            <el-button type="primary" link size="small" @click="handleEditOffering(row)">
              编辑开课
            </el-button>
          </template>
          <template v-else>
            <el-button type="success" link size="small" @click="handleCreateOffering(row)">
              <el-icon><Plus /></el-icon>新学期开课
            </el-button>
          </template>
          <el-button type="primary" link size="small" @click="handleEditTemplate(row)">
            编辑模板
          </el-button>
          <el-button type="danger" link size="small" @click="handleDeleteTemplate(row)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="!loading && templateList.length === 0" description="暂无课程模板，请先创建课程模板" />

    <!-- 分页 -->
    <div class="pagination-container" v-if="total > 0">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchTemplateList"
        @current-change="fetchTemplateList"
      />
    </div>

    <!-- 新学期开课对话框 -->
    <el-dialog
      v-model="offeringDialogVisible"
      title="新学期开课"
      width="500px"
      destroy-on-close
    >
      <el-form
        ref="offeringFormRef"
        :model="offeringForm"
        :rules="offeringRules"
        label-width="100px"
      >
        <el-form-item label="课程模板">
          <span>{{ currentTemplate?.courseName }}</span>
        </el-form-item>
        <el-form-item label="目标学期">
          <span>{{ currentSemesterName }}</span>
        </el-form-item>
        <el-form-item label="授课教师" prop="teacherId">
          <el-select v-model="offeringForm.teacherId" placeholder="选择授课教师" style="width: 100%">
            <el-option :label="userStore.username" :value="userStore.id" />
            <!-- TODO: 可以添加其他教师选项 -->
          </el-select>
        </el-form-item>
        <el-form-item label="上课地点">
          <el-input v-model="offeringForm.classLocation" placeholder="如：教学楼A-101" />
        </el-form-item>
        <el-form-item label="上课时间">
          <el-input
            v-model="offeringForm.classSchedule"
            type="textarea"
            :rows="3"
            placeholder="如：周一 1-2节、周三 3-4节"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="offeringForm.notes"
            type="textarea"
            :rows="2"
            placeholder="选填"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="offeringDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmitOffering">
          确定开课
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getTeacherCourseTemplates, createOffering, deleteOffering } from '@/api/courseOffering'
import { ElMessage, ElMessageBox } from 'element-plus'
import SemesterSelector from '@/components/SemesterSelector.vue'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const templateList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const searchKeyword = ref('')
const selectedSemesterId = ref(null)
const currentSemester = ref(null)

const offeringDialogVisible = ref(false)
const submitLoading = ref(false)
const offeringFormRef = ref()
const currentTemplate = ref(null)

const offeringForm = reactive({
  courseId: null,
  semesterId: null,
  teacherId: null,
  classLocation: '',
  classSchedule: '',
  notes: ''
})

const offeringRules = {
  teacherId: [{ required: true, message: '请选择授课教师', trigger: 'change' }]
}

const currentSemesterName = computed(() => {
  return currentSemester.value?.semesterName || ''
})

const getOfferingStatusText = (status) => {
  return status === 0 ? '未开课' : status === 1 ? '进行中' : '已结课'
}

const handleSemesterChange = (semesterId, semester) => {
  selectedSemesterId.value = semesterId
  currentSemester.value = semester
  pageNum.value = 1
  fetchTemplateList()
}

const fetchTemplateList = async () => {
  loading.value = true
  try {
    const params = {
      semesterId: selectedSemesterId.value,
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: searchKeyword.value
    }
    const res = await getTeacherCourseTemplates(params)
    templateList.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('获取课程模板列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pageNum.value = 1
  fetchTemplateList()
}

const handleCreateTemplate = () => {
  // 跳转到创建课程页面（使用现有的创建课程功能）
  router.push({ name: 'Courses' })
}

const handleEditTemplate = (template) => {
  // 跳转到编辑课程模板页面
  router.push({ name: 'Courses' })
}

const handleDeleteTemplate = (template) => {
  ElMessageBox.confirm(
    `确定要删除课程模板"${template.courseName}"吗？删除后，该课程的所有历史开课记录仍会保留。`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      // TODO: 实现删除课程模板的API
      ElMessage.success('删除成功')
      fetchTemplateList()
    } catch (error) {
      console.error('删除失败:', error)
    }
  })
}

const handleCreateOffering = (template) => {
  if (!selectedSemesterId.value) {
    ElMessage.warning('请先选择学期')
    return
  }

  currentTemplate.value = template
  Object.assign(offeringForm, {
    courseId: template.id,
    semesterId: selectedSemesterId.value,
    teacherId: userStore.id, // 默认为当前用户
    classLocation: '',
    classSchedule: '',
    notes: ''
  })
  offeringDialogVisible.value = true
}

const handleSubmitOffering = async () => {
  if (!offeringFormRef.value) return
  await offeringFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        await createOffering(offeringForm)
        ElMessage.success('开课成功')
        offeringDialogVisible.value = false
        fetchTemplateList()
      } catch (error) {
        console.error('开课失败:', error)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const handleEditOffering = (row) => {
  // 跳转到开课实例编辑页面
  router.push({ name: 'CourseOfferingDetail', params: { id: row.offeringId } })
}

const goToOffering = (offeringId) => {
  // 跳转到开课实例详情页
  router.push({ name: 'CourseOfferingDetail', params: { id: offeringId } })
}

onMounted(() => {
  // 等待学期选择器加载完成后再获取数据
  // fetchTemplateList 会在 handleSemesterChange 中被调用
})
</script>

<style lang="scss" scoped>
.course-template-container {
  .semester-bar {
    display: flex;
    align-items: center;
    margin-bottom: 20px;
    padding: 16px 20px;
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  }

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

  .template-table {
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
    padding: 20px;

    .course-info {
      .course-name {
        font-size: 16px;
        font-weight: 600;
        color: #1e293b;
        margin-bottom: 6px;
        display: flex;
        align-items: center;
      }

      .course-code {
        font-size: 13px;
        color: #64748b;
        margin-bottom: 4px;
      }

      .course-meta {
        font-size: 12px;
        color: #94a3b8;
      }
    }
  }

  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
