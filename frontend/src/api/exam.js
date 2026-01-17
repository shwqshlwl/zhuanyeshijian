import request from '@/utils/request'

// 获取考试列表
export function getExamList(params) {
  return request({
    url: '/exams',
    method: 'get',
    params
  })
}

// 创建考试
export function createExam(data) {
  return request({
    url: '/exams',
    method: 'post',
    data
  })
}

// 获取考试详情
export function getExamById(id) {
  return request({
    url: `/exams/${id}`,
    method: 'get'
  })
}

// 更新考试
export function updateExam(id, data) {
  return request({
    url: `/exams/${id}`,
    method: 'put',
    data
  })
}

// 删除考试
export function deleteExam(id) {
  return request({
    url: `/exams/${id}`,
    method: 'delete'
  })
}

// 开始考试
export function startExam(id) {
  return request({
    url: `/exams/${id}/start`,
    method: 'post'
  })
}

// 提交考试
export function submitExam(id, data) {
  return request({
    url: `/exams/${id}/submit`,
    method: 'post',
    data
  })
}

// 考试结果分析
export function getExamAnalysis(id) {
  return request({
    url: `/exams/${id}/analysis`,
    method: 'get'
  })
}

// 获取学生的考试列表
export function getStudentExams() {
  return request({
    url: '/exams/student/my',
    method: 'get'
  })
}

// 测试API
export function testApi() {
  return request({
    url: '/exams/test',
    method: 'get'
  })
}