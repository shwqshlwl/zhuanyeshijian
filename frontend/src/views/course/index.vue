<template>
  <div class="course-container">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索课程名称"
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
      <el-button type="primary" @click="handleAdd" v-if="userStore.isTeacher">
        <el-icon><Plus /></el-icon>新建课程
      </el-button>
    </div>

    <!-- 课程列表 -->
    <div class="course-list" v-loading="loading">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :lg="8" :xl="6" v-for="course in courseList" :key="course.id">
          <div class="course-card" @click="goToDetail(course.id)">
            <div class="course-cover" :style="{ background: getRandomGradient(course.id) }">
              <el-icon :size="48" color="rgba(255,255,255,0.8)"><Reading /></el-icon>
            </div>
            <div class="course-info">
              <h3 class="course-name">{{ course.name }}</h3>
              <p class="course-desc">{{ course.description || '暂无描述' }}</p>
              <div class="course-meta">
                <span class="meta-item">
                  <el-icon><User /></el-icon>
                  {{ course.teacherName || '未知教师' }}
                </span>
                <span class="meta-item">
                  <el-icon><Calendar /></el-icon>
                  {{ course.createTime?.substring(0, 10) }}
                </span>
              </div>
            </div>
            <div class="course-actions" @click.stop>
              <el-button type="primary" text size="small" @click="goToDetail(course.id)">
                查看详情
              </el-button>
              <template v-if="userStore.isTeacher">
                <el-button type="primary" text size="small" @click="handleEdit(course)">
                  编辑
                </el-button>
                <el-button type="danger" text size="small" @click="handleDelete(course)">
                  删除
                </el-button>
              </template>
            </div>
          </div>
        </el-col>
      </el-row>
      
      <el-empty v-if="!loading && courseList.length === 0" description="暂无课程数据" />
    </div>

    <!-- 分页 -->
    <div class="pagination-container" v-if="total > 0">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :page-sizes="[8, 12, 16, 24]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchCourseList"
        @current-change="fetchCourseList"
      />
    </div>

    <!-- 新建/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑课程' : '新建课程'"
      width="500px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="courseForm"
        :rules="formRules"
        label-width="80px"
      >
        <el-form-item label="课程名称" prop="courseName">
          <el-input v-model="courseForm.courseName" placeholder="请输入课程名称" />
        </el-form-item>
        <el-form-item label="课程编码" prop="courseCode">
          <el-input v-model="courseForm.courseCode" placeholder="请输入课程编码" />
        </el-form-item>
        <el-form-item label="学分" prop="credit">
          <el-input-number v-model="courseForm.credit" :min="0" :max="10" :precision="1" />
        </el-form-item>
        <el-form-item label="课程描述" prop="description">
          <el-input
            v-model="courseForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入课程描述"
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getCourseList, createCourse, updateCourse, deleteCourse } from '@/api/course'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const courseList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(8)
const searchKeyword = ref('')

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref()
const currentCourseId = ref(null)

const courseForm = reactive({
  courseName: '',
  courseCode: '',
  credit: 2,
  description: ''
})

const formRules = {
  courseName: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
  courseCode: [{ required: true, message: '请输入课程编码', trigger: 'blur' }]
}

const gradients = [
  'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
  'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
  'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
  'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
  'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
  'linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%)',
  'linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%)',
  'linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%)'
]

const getRandomGradient = (id) => {
  return gradients[id % gradients.length]
}

const fetchCourseList = async () => {
  loading.value = true
  try {
    const res = await getCourseList({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: searchKeyword.value
    })
    courseList.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('获取课程列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pageNum.value = 1
  fetchCourseList()
}

const goToDetail = (id) => {
  router.push(`/courses/${id}`)
}

const handleAdd = () => {
  isEdit.value = false
  currentCourseId.value = null
  Object.assign(courseForm, {
    courseName: '',
    courseCode: '',
    credit: 2,
    description: ''
  })
  dialogVisible.value = true
}

const handleEdit = (course) => {
  isEdit.value = true
  currentCourseId.value = course.id
  Object.assign(courseForm, {
    courseName: course.name,
    courseCode: course.code,
    credit: course.credit,
    description: course.description
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (isEdit.value) {
          await updateCourse(currentCourseId.value, courseForm)
          ElMessage.success('更新成功')
        } else {
          await createCourse(courseForm)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        fetchCourseList()
      } catch (error) {
        console.error('提交失败:', error)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const handleDelete = (course) => {
  ElMessageBox.confirm(`确定要删除课程"${course.name}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteCourse(course.id)
      ElMessage.success('删除成功')
      fetchCourseList()
    } catch (error) {
      console.error('删除失败:', error)
    }
  })
}

onMounted(() => {
  fetchCourseList()
})
</script>

<style lang="scss" scoped>
.course-container {
  .search-bar {
    display: flex;
    gap: 12px;
    margin-bottom: 20px;
    padding: 20px;
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  }
  
  .course-list {
    min-height: 400px;
  }
  
  .course-card {
    background: #fff;
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
    margin-bottom: 20px;
    cursor: pointer;
    transition: all 0.3s;
    
    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
    }
    
    .course-cover {
      height: 120px;
      display: flex;
      align-items: center;
      justify-content: center;
    }
    
    .course-info {
      padding: 16px;
      
      .course-name {
        font-size: 16px;
        font-weight: 600;
        color: #1e293b;
        margin: 0 0 8px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      
      .course-desc {
        font-size: 13px;
        color: #64748b;
        margin: 0 0 12px;
        height: 40px;
        overflow: hidden;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
      }
      
      .course-meta {
        display: flex;
        gap: 16px;
        
        .meta-item {
          display: flex;
          align-items: center;
          gap: 4px;
          font-size: 12px;
          color: #94a3b8;
        }
      }
    }
    
    .course-actions {
      padding: 12px 16px;
      border-top: 1px solid #f1f5f9;
      display: flex;
      justify-content: flex-end;
      gap: 8px;
    }
  }
}
</style>
