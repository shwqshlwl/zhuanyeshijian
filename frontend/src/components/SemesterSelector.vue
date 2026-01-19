<template>
  <div class="semester-selector">
    <span class="label">学期：</span>
    <el-select
      v-model="selectedSemesterId"
      placeholder="选择学期"
      :loading="loading"
      filterable
      @change="handleChange"
      style="width: 240px"
    >
      <el-option
        v-for="semester in semesters"
        :key="semester.id"
        :label="semester.semesterName"
        :value="semester.id"
      >
        <div style="display: flex; align-items: center; justify-content: space-between">
          <span>{{ semester.semesterName }}</span>
          <div style="display: flex; gap: 4px; align-items: center">
            <el-tag v-if="semester.isCurrent === 1" size="small" type="primary">当前</el-tag>
            <el-tag :type="getStatusType(semester.status)" size="small">
              {{ getStatusText(semester.status) }}
            </el-tag>
          </div>
        </div>
      </el-option>
    </el-select>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { getAllSemesters, getCurrentSemester } from '@/api/semester'

const props = defineProps({
  modelValue: {
    type: Number,
    default: null
  },
  // 是否自动加载当前学期
  autoLoadCurrent: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

const loading = ref(false)
const semesters = ref([])
const selectedSemesterId = ref(props.modelValue)

const getStatusType = (status) => {
  return status === 0 ? 'info' : status === 1 ? 'success' : 'warning'
}

const getStatusText = (status) => {
  return status === 0 ? '未开始' : status === 1 ? '进行中' : '已归档'
}

const fetchSemesters = async () => {
  loading.value = true
  try {
    const res = await getAllSemesters()
    semesters.value = res.data || []

    // 如果需要自动加载当前学期且未选择学期
    if (props.autoLoadCurrent && !selectedSemesterId.value && semesters.value.length > 0) {
      // 查找当前学期
      const currentRes = await getCurrentSemester()
      if (currentRes.data) {
        selectedSemesterId.value = currentRes.data.id
        emit('update:modelValue', selectedSemesterId.value)
        const semester = semesters.value.find(s => s.id === selectedSemesterId.value)
        emit('change', selectedSemesterId.value, semester)
      }
    }
  } catch (error) {
    console.error('获取学期列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleChange = (value) => {
  emit('update:modelValue', value)
  const semester = semesters.value.find(s => s.id === value)
  emit('change', value, semester)
}

// 监听外部值变化
watch(() => props.modelValue, (newVal) => {
  selectedSemesterId.value = newVal
})

onMounted(() => {
  fetchSemesters()
})

// 暴露刷新方法
defineExpose({
  refresh: fetchSemesters
})
</script>

<style lang="scss" scoped>
.semester-selector {
  display: flex;
  align-items: center;
  gap: 8px;

  .label {
    font-size: 14px;
    color: #606266;
    white-space: nowrap;
  }
}
</style>
