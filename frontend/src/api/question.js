import request from '@/utils/request'

// 获取所有题型
export function getAllQuestionTypes() {
  return request({
    url: '/questions/types',
    method: 'get'
  })
}

// 获取题目列表
export function getQuestionList(params) {
  return request({
    url: '/questions',
    method: 'get',
    params
  })
}

// 创建题目
export function createQuestion(data) {
  return request({
    url: '/questions',
    method: 'post',
    data
  })
}

// 获取题目详情
export function getQuestionById(id) {
  return request({
    url: `/questions/${id}`,
    method: 'get'
  })
}

// 更新题目
export function updateQuestion(id, data) {
  return request({
    url: `/questions/${id}`,
    method: 'put',
    data
  })
}

// 删除题目
export function deleteQuestion(id) {
  return request({
    url: `/questions/${id}`,
    method: 'delete'
  })
}

// 批量删除题目
export function batchDeleteQuestions(ids) {
  return request({
    url: '/questions/batch-delete',
    method: 'post',
    data: ids
  })
}
