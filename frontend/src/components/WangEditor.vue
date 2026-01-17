<template>
  <div class="wang-editor-container">
    <Toolbar
      :editor="editorRef"
      :defaultConfig="toolbarConfig"
      :mode="mode"
      class="toolbar"
    />
    <Editor
      v-model="valueHtml"
      :defaultConfig="editorConfig"
      :mode="mode"
      @onCreated="handleCreated"
      class="editor"
    />
  </div>
</template>

<script setup>
import { ref, shallowRef, onBeforeUnmount, watch } from 'vue'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import '@wangeditor/editor/dist/css/style.css'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  mode: {
    type: String,
    default: 'default' // 'default' 或 'simple'
  },
  placeholder: {
    type: String,
    default: '请输入内容...'
  },
  height: {
    type: String,
    default: '400px'
  }
})

const emit = defineEmits(['update:modelValue'])

const editorRef = shallowRef()
const valueHtml = ref(props.modelValue || '')

// 工具栏配置
const toolbarConfig = {}

// 编辑器配置
const editorConfig = {
  placeholder: props.placeholder,
  MENU_CONF: {
    // 上传图片配置（如果需要）
    uploadImage: {
      // 这里可以配置图片上传服务器
      // server: '/api/upload/image',
      // 暂时禁用上传，使用base64
      maxFileSize: 1 * 1024 * 1024, // 1M
    }
  }
}

const handleCreated = (editor) => {
  editorRef.value = editor
}

// 监听内容变化
watch(valueHtml, (val) => {
  emit('update:modelValue', val)
})

// 监听外部值变化
watch(() => props.modelValue, (val) => {
  if (val !== valueHtml.value) {
    valueHtml.value = val || ''
  }
})

// 组件销毁时销毁编辑器
onBeforeUnmount(() => {
  const editor = editorRef.value
  if (editor) {
    editor.destroy()
  }
})
</script>

<style lang="scss" scoped>
.wang-editor-container {
  border: 1px solid #ccc;
  border-radius: 4px;
  overflow: hidden;

  .toolbar {
    border-bottom: 1px solid #ccc;
  }

  .editor {
    height: v-bind(height);
    overflow-y: auto;

    :deep(.w-e-text-container) {
      background-color: #fff;
    }

    :deep(.w-e-text-placeholder) {
      color: #999;
      font-style: normal;
    }
  }
}
</style>
