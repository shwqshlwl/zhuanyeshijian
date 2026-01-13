<template>
  <div class="class-container">
    <div class="search-bar">
      <el-input v-model="searchForm.grade" placeholder="年级" clearable style="width: 150px" />
      <el-input v-model="searchForm.major" placeholder="专业" clearable style="width: 150px" />
      <el-button type="primary" @click="handleSearch">
        <el-icon><Search /></el-icon>搜索
      </el-button>
      <el-button @click="toggleGroupView">
        <el-icon><Grid /></el-icon>{{ showGroupView ? '列表视图' : '分组视图' }}
      </el-button>
      <el-button type="primary" @click="handleAdd" v-if="userStore.isTeacher">
        <el-icon><Plus /></el-icon>新建班级
      </el-button>
    </div>

    <!-- 分组视图 -->
    <div v-if="showGroupView" class="group-view" v-loading="loading">
      <div v-for="(classes, grade) in groupedClasses" :key="grade" class="grade-group">
        <h3 class="grade-title">{{ grade }} 级</h3>
        <el-row :gutter="16">
          <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="cls in classes" :key="cls.id">
            <div class="class-card" @click="$router.push(`/classes/${cls.id}`)">
              <div class="class-icon">
                <el-icon :size="32"><School /></el-icon>
              </div>
              <div class="class-info">
                <h4>{{ cls.name }}</h4>
                <p>{{ cls.major }}</p>
                <div class="class-meta">
                  <span><el-icon><User /></el-icon> {{ cls.studentCount || 0 }} 人</span>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>
      <el-empty v-if="Object.keys(groupedClasses).length === 0" description="暂无班级数据" />
    </div>

    <!-- 列表视图 -->
    <div v-else>
      <el-table :data="classList" v-loading="loading" stripe>
        <el-table-column prop="name" label="班级名称" min-width="150">
          <template #default="{ row }">
            <el-link type="primary" @click="$router.push(`/classes/${row.id}`)">{{ row.name }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="grade" label="年级" width="100" />
        <el-table-column prop="major" label="专业" min-width="150" />
        <el-table-column prop="studentCount" label="学生人数" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="$router.push(`/classes/${row.id}`)">详情</el-button>
            <template v-if="userStore.isTeacher">
              <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
              <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="fetchList"
          @current-change="fetchList"
        />
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑班级' : '新建班级'" width="500px">
      <el-form ref="formRef" :model="classForm" :rules="formRules" label-width="80px">
        <el-form-item label="班级名称" prop="className">
          <el-input v-model="classForm.className" placeholder="请输入班级名称" />
        </el-form-item>
        <el-form-item label="班级编码" prop="classCode">
          <el-input v-model="classForm.classCode" placeholder="请输入班级编码，如：CS2024-01" />
        </el-form-item>
        <el-form-item label="年级" prop="grade">
          <el-input v-model="classForm.grade" placeholder="如：2024" />
        </el-form-item>
        <el-form-item label="专业" prop="major">
          <el-input v-model="classForm.major" placeholder="请输入专业名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="classForm.description" type="textarea" :rows="3" placeholder="班级描述（选填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getClassList, createClass, updateClass, deleteClass } from '@/api/class'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const userStore = useUserStore()
const loading = ref(false)
const classList = ref([])
const groupedClasses = ref({})
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const searchForm = reactive({ grade: '', major: '' })
const showGroupView = ref(false)

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref()
const currentId = ref(null)
const classForm = reactive({ className: '', classCode: '', grade: '', major: '', description: '' })
const formRules = { 
  className: [{ required: true, message: '请输入班级名称', trigger: 'blur' }],
  classCode: [{ required: true, message: '请输入班级编码', trigger: 'blur' }]
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getClassList({ pageNum: pageNum.value, pageSize: pageSize.value, ...searchForm })
    classList.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const fetchGroupedList = async () => {
  loading.value = true
  try {
    const res = await request({ url: '/classes/group-by-grade', method: 'get' })
    groupedClasses.value = res.data || {}
  } finally {
    loading.value = false
  }
}

const toggleGroupView = () => {
  showGroupView.value = !showGroupView.value
  if (showGroupView.value) {
    fetchGroupedList()
  } else {
    fetchList()
  }
}

const handleSearch = () => { pageNum.value = 1; fetchList() }

const handleAdd = () => {
  isEdit.value = false
  currentId.value = null
  Object.assign(classForm, { className: '', classCode: '', grade: '', major: '', description: '' })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  currentId.value = row.id
  Object.assign(classForm, { 
    className: row.name, 
    classCode: row.code, 
    grade: row.grade, 
    major: row.major, 
    description: row.description 
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value?.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (isEdit.value) {
          await updateClass(currentId.value, classForm)
          ElMessage.success('更新成功')
        } else {
          await createClass(classForm)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        fetchList()
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除班级"${row.name}"吗？`, '提示', { type: 'warning' }).then(async () => {
    await deleteClass(row.id)
    ElMessage.success('删除成功')
    fetchList()
  })
}

onMounted(() => fetchList())
</script>

<style lang="scss" scoped>
.class-container {
  .search-bar {
    display: flex;
    gap: 12px;
    margin-bottom: 20px;
    padding: 20px;
    background: #fff;
    border-radius: 12px;
  }
  
  .group-view {
    .grade-group {
      margin-bottom: 24px;
      
      .grade-title {
        font-size: 18px;
        font-weight: 600;
        color: #303133;
        margin-bottom: 16px;
        padding-left: 12px;
        border-left: 4px solid #409eff;
      }
    }
    
    .class-card {
      background: #fff;
      border-radius: 12px;
      padding: 20px;
      margin-bottom: 16px;
      cursor: pointer;
      transition: all 0.3s;
      display: flex;
      align-items: center;
      gap: 16px;
      
      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
      }
      
      .class-icon {
        width: 60px;
        height: 60px;
        border-radius: 12px;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        display: flex;
        align-items: center;
        justify-content: center;
        color: #fff;
      }
      
      .class-info {
        flex: 1;
        
        h4 {
          margin: 0 0 4px;
          font-size: 16px;
          color: #303133;
        }
        
        p {
          margin: 0 0 8px;
          font-size: 13px;
          color: #909399;
        }
        
        .class-meta {
          font-size: 12px;
          color: #606266;
          display: flex;
          align-items: center;
          gap: 4px;
        }
      }
    }
  }
  
  .pagination-container {
    margin-top: 16px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
