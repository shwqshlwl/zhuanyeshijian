<template>
  <div class="experiment-create-container">
    <el-page-header @back="$router.back()" title="返回">
      <template #content>
        <span class="page-title">{{ isEdit ? '编辑实验' : '创建实验' }}</span>
      </template>
    </el-page-header>

    <el-card class="form-card" v-loading="loading">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" size="large">
        <!-- 基本信息 -->
        <div class="form-section">
          <div class="section-title">基本信息</div>
          
          <el-form-item label="实验名称" prop="experimentName">
            <el-input 
              v-model="form.experimentName" 
              placeholder="请输入实验名称，如：两数相加" 
              maxlength="100"
              show-word-limit
            />
          </el-form-item>

          <el-form-item label="所属课程" prop="courseId">
            <el-select v-model="form.courseId" placeholder="请选择课程" style="width: 100%">
              <el-option 
                v-for="course in courseOptions" 
                :key="course.id" 
                :label="course.courseName" 
                :value="course.id" 
              />
            </el-select>
          </el-form-item>

          <el-form-item label="编程语言" prop="language">
            <el-select v-model="form.language" placeholder="请选择编程语言" style="width: 100%">
              <el-option label="Java" value="java">
                <span style="float: left">Java</span>
                <span style="float: right; color: #8492a6; font-size: 13px">适合算法题</span>
              </el-option>
              <el-option label="Python" value="python">
                <span style="float: left">Python</span>
                <span style="float: right; color: #8492a6; font-size: 13px">简洁易学</span>
              </el-option>
              <el-option label="C++" value="cpp">
                <span style="float: left">C++</span>
                <span style="float: right; color: #8492a6; font-size: 13px">高性能</span>
              </el-option>
              <el-option label="C" value="c">
                <span style="float: left">C</span>
                <span style="float: right; color: #8492a6; font-size: 13px">系统编程</span>
              </el-option>
              <el-option label="JavaScript" value="javascript">
                <span style="float: left">JavaScript</span>
                <span style="float: right; color: #8492a6; font-size: 13px">Web开发</span>
              </el-option>
            </el-select>
          </el-form-item>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="开始时间" prop="startTime">
                <el-date-picker 
                  v-model="form.startTime" 
                  type="datetime" 
                  placeholder="选择开始时间"
                  style="width: 100%"
                  :disabled-date="disabledStartDate"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="截止时间" prop="endTime">
                <el-date-picker 
                  v-model="form.endTime" 
                  type="datetime" 
                  placeholder="选择截止时间"
                  style="width: 100%"
                  :disabled-date="disabledEndDate"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="总分" prop="totalScore">
                <el-input-number v-model="form.totalScore" :min="1" :max="1000" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="时间限制" prop="timeLimit">
                <el-input-number v-model="form.timeLimit" :min="100" :max="60000" style="width: 100%" />
                <span class="form-tip">毫秒</span>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="内存限制" prop="memoryLimit">
                <el-input-number v-model="form.memoryLimit" :min="16" :max="1024" style="width: 100%" />
                <span class="form-tip">MB</span>
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="实验状态" prop="status">
            <el-radio-group v-model="form.status">
              <el-radio :label="0">草稿</el-radio>
              <el-radio :label="1">已发布</el-radio>
            </el-radio-group>
            <div class="form-tip">草稿状态学生不可见</div>
          </el-form-item>
        </div>

        <!-- 实验内容 -->
        <div class="form-section">
          <div class="section-title">实验内容</div>

          <el-form-item label="实验描述" prop="description">
            <el-input 
              v-model="form.description" 
              type="textarea" 
              :rows="4" 
              placeholder="请输入实验描述，说明实验的目的和背景"
              maxlength="1000"
              show-word-limit
            />
          </el-form-item>

          <el-form-item label="实验要求" prop="requirement">
            <el-input 
              v-model="form.requirement" 
              type="textarea" 
              :rows="6" 
              placeholder="请输入实验要求，详细说明需要实现的功能和注意事项"
              maxlength="2000"
              show-word-limit
            />
          </el-form-item>

          <el-form-item label="模板代码">
            <div class="code-editor">
              <div class="editor-header">
                <span>{{ languageMap[form.language] || '代码' }}</span>
                <el-button size="small" @click="insertTemplate">插入模板</el-button>
              </div>
              <textarea 
                v-model="form.templateCode" 
                class="code-textarea"
                placeholder="输入模板代码（可选），学生将基于此模板编写代码"
                spellcheck="false"
              ></textarea>
            </div>
          </el-form-item>
        </div>

        <!-- 测试用例 -->
        <div class="form-section">
          <div class="section-title">
            <span>测试用例</span>
            <el-button type="primary" size="small" @click="addTestCase">
              <el-icon><Plus /></el-icon>添加测试用例
            </el-button>
          </div>

          <div class="test-cases">
            <div v-for="(testCase, index) in testCases" :key="index" class="test-case-item">
              <div class="test-case-header">
                <span class="test-case-title">测试用例 {{ index + 1 }}</span>
                <el-button 
                  type="danger" 
                  size="small" 
                  text 
                  @click="removeTestCase(index)"
                  :disabled="testCases.length === 1"
                >
                  <el-icon><Delete /></el-icon>删除
                </el-button>
              </div>
              <el-row :gutter="20">
                <el-col :span="12">
                  <div class="test-case-label">输入</div>
                  <el-input 
                    v-model="testCase.input" 
                    type="textarea" 
                    :rows="4"
                    placeholder="输入数据（通过标准输入传递）&#10;多行输入用换行分隔"
                  />
                </el-col>
                <el-col :span="12">
                  <div class="test-case-label">期望输出</div>
                  <el-input 
                    v-model="testCase.output" 
                    type="textarea" 
                    :rows="4"
                    placeholder="期望的输出结果"
                  />
                </el-col>
              </el-row>
            </div>
          </div>

          <el-alert 
            title="测试用例说明" 
            type="info" 
            :closable="false"
            style="margin-top: 16px"
          >
            <ul style="margin: 0; padding-left: 20px">
              <li>输入数据通过标准输入（stdin）传递给程序</li>
              <li>输出结果通过标准输出（stdout）获取</li>
              <li>系统会自动去除行尾空格和末尾空行</li>
              <li>建议至少添加 3-5 个测试用例，覆盖正常、边界和异常情况</li>
            </ul>
          </el-alert>
        </div>

        <!-- 操作按钮 -->
        <el-form-item>
          <el-button type="primary" size="large" @click="handleSubmit" :loading="submitLoading">
            <el-icon><Check /></el-icon>{{ isEdit ? '保存修改' : '创建实验' }}
          </el-button>
          <el-button size="large" @click="handleCancel">
            <el-icon><Close /></el-icon>取消
          </el-button>
          <el-button size="large" @click="handlePreview" v-if="!isEdit">
            <el-icon><View /></el-icon>预览
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCourseList } from '@/api/course'
import { createExperiment, updateExperiment, getExperimentById } from '@/api/experiment'

const route = useRoute()
const router = useRouter()

const isEdit = computed(() => !!route.params.id)
const loading = ref(false)
const submitLoading = ref(false)
const formRef = ref()
const courseOptions = ref([])

const languageMap = {
  java: 'Java',
  python: 'Python',
  cpp: 'C++',
  c: 'C',
  javascript: 'JavaScript'
}

const form = reactive({
  experimentName: '',
  courseId: null,
  language: 'java',
  startTime: null,
  endTime: null,
  totalScore: 100,
  timeLimit: 5000,
  memoryLimit: 256,
  status: 1,
  description: '',
  requirement: '',
  templateCode: '',
  testCases: ''
})

const testCases = ref([
  { input: '', output: '' }
])

const rules = {
  experimentName: [
    { required: true, message: '请输入实验名称', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  courseId: [
    { required: true, message: '请选择课程', trigger: 'change' }
  ],
  language: [
    { required: true, message: '请选择编程语言', trigger: 'change' }
  ],
  endTime: [
    { required: true, message: '请选择截止时间', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请输入实验描述', trigger: 'blur' }
  ],
  requirement: [
    { required: true, message: '请输入实验要求', trigger: 'blur' }
  ]
}

const codeTemplates = {
  java: `import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        // 在此编写代码
        
        scanner.close();
    }
}`,
  python: `# 在此编写代码
`,
  cpp: `#include <iostream>
using namespace std;

int main() {
    // 在此编写代码
    
    return 0;
}`,
  c: `#include <stdio.h>

int main() {
    // 在此编写代码
    
    return 0;
}`,
  javascript: `// 在此编写代码
`
}

const disabledStartDate = (time) => {
  return time.getTime() < Date.now() - 8.64e7
}

const disabledEndDate = (time) => {
  if (form.startTime) {
    return time.getTime() < new Date(form.startTime).getTime()
  }
  return time.getTime() < Date.now() - 8.64e7
}

const fetchCourses = async () => {
  try {
    const res = await getCourseList({ pageNum: 1, pageSize: 100 })
    courseOptions.value = res.data?.records || []
  } catch (e) {
    ElMessage.error('获取课程列表失败')
  }
}

const fetchExperiment = async () => {
  if (!isEdit.value) return
  
  loading.value = true
  try {
    const res = await getExperimentById(route.params.id)
    const data = res.data
    
    Object.assign(form, {
      experimentName: data.experimentName,
      courseId: data.courseId,
      language: data.language,
      startTime: data.startTime,
      endTime: data.endTime,
      totalScore: data.totalScore,
      timeLimit: data.timeLimit,
      memoryLimit: data.memoryLimit,
      status: data.status,
      description: data.description,
      requirement: data.requirement,
      templateCode: data.templateCode || ''
    })
    
    // 解析测试用例
    if (data.testCases) {
      try {
        testCases.value = JSON.parse(data.testCases)
      } catch (e) {
        console.error('解析测试用例失败', e)
      }
    }
  } catch (e) {
    ElMessage.error('获取实验信息失败')
    router.back()
  } finally {
    loading.value = false
  }
}

const insertTemplate = () => {
  form.templateCode = codeTemplates[form.language] || ''
  ElMessage.success('已插入模板代码')
}

const addTestCase = () => {
  testCases.value.push({ input: '', output: '' })
}

const removeTestCase = (index) => {
  testCases.value.splice(index, 1)
}

const handleSubmit = async () => {
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    // 验证测试用例
    const validTestCases = testCases.value.filter(tc => tc.input || tc.output)
    if (validTestCases.length === 0) {
      ElMessage.warning('请至少添加一个测试用例')
      return
    }
    
    submitLoading.value = true
    try {
      const data = {
        ...form,
        testCases: JSON.stringify(validTestCases)
      }
      
      if (isEdit.value) {
        await updateExperiment(route.params.id, data)
        ElMessage.success('更新成功')
      } else {
        await createExperiment(data)
        ElMessage.success('创建成功')
      }
      
      router.push('/experiments')
    } catch (e) {
      ElMessage.error(e.message || '操作失败')
    } finally {
      submitLoading.value = false
    }
  })
}

const handleCancel = () => {
  ElMessageBox.confirm('确定要取消吗？未保存的内容将丢失', '提示', {
    type: 'warning'
  }).then(() => {
    router.back()
  }).catch(() => {})
}

const handlePreview = () => {
  ElMessage.info('预览功能开发中')
}

onMounted(() => {
  fetchCourses()
  fetchExperiment()
})
</script>

<style lang="scss" scoped>
.experiment-create-container {
  .page-title {
    font-size: 20px;
    font-weight: 600;
  }

  .form-card {
    margin-top: 20px;
  }

  .form-section {
    margin-bottom: 40px;
    padding-bottom: 30px;
    border-bottom: 1px solid #ebeef5;

    &:last-child {
      border-bottom: none;
    }

    .section-title {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
      margin-bottom: 20px;
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }

  .form-tip {
    margin-left: 8px;
    font-size: 12px;
    color: #909399;
  }

  .code-editor {
    width: 100%;
    border: 1px solid #dcdfe6;
    border-radius: 4px;
    overflow: hidden;

    .editor-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 8px 12px;
      background: #f5f7fa;
      border-bottom: 1px solid #dcdfe6;
      font-size: 14px;
      color: #606266;
    }

    .code-textarea {
      width: 100%;
      min-height: 300px;
      padding: 12px;
      border: none;
      outline: none;
      resize: vertical;
      font-family: 'Consolas', 'Monaco', monospace;
      font-size: 14px;
      line-height: 1.5;
      background: #1e1e1e;
      color: #d4d4d4;

      &::placeholder {
        color: #6a6a6a;
      }
    }
  }

  .test-cases {
    .test-case-item {
      margin-bottom: 20px;
      padding: 16px;
      background: #f5f7fa;
      border-radius: 8px;

      .test-case-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 12px;

        .test-case-title {
          font-size: 14px;
          font-weight: 600;
          color: #303133;
        }
      }

      .test-case-label {
        font-size: 13px;
        color: #606266;
        margin-bottom: 8px;
      }
    }
  }
}
</style>
