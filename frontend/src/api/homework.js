import request from '@/utils/request'

// 获取作业列表
export function getHomeworkList(params) {
  return request({
    url: '/homeworks',
    method: 'get',
    params
  })
}

// 创建作业
export function createHomework(data) {
  return request({
    url: '/homeworks',
    method: 'post',
    data
  })
}

// 获取作业详情
export function getHomeworkById(id) {
  return request({
    url: `/homeworks/${id}`,
    method: 'get'
  })
}

// 更新作业
export function updateHomework(id, data) {
  return request({
    url: `/homeworks/${id}`,
    method: 'put',
    data
  })
}

// 删除作业
export function deleteHomework(id) {
  return request({
    url: `/homeworks/${id}`,
    method: 'delete'
  })
}

// 提交作业
export function submitHomework(id, data) {
  return request({
    url: `/homeworks/${id}/submit`,
    method: 'post',
    data
  })
}

// 批改作业
export function gradeHomework(id, studentId, data) {
  return request({
    url: `/homeworks/${id}/grade`,
    method: 'put',
    params: { studentId },
    data
  })
}

// AI 解析题目
export function aiAnalyzeHomework(id) {
  return request({
    url: `/homeworks/${id}/ai-analyze`,
    method: 'post'
  })
}
