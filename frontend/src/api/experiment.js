import request from '@/utils/request'

// 获取实验列表
export function getExperimentList(params) {
  return request({
    url: '/experiments',
    method: 'get',
    params
  })
}

// 创建实验
export function createExperiment(data) {
  return request({
    url: '/experiments',
    method: 'post',
    data
  })
}

// 获取实验详情
export function getExperimentById(id) {
  return request({
    url: `/experiments/${id}`,
    method: 'get'
  })
}

// 更新实验
export function updateExperiment(id, data) {
  return request({
    url: `/experiments/${id}`,
    method: 'put',
    data
  })
}

// 删除实验
export function deleteExperiment(id) {
  return request({
    url: `/experiments/${id}`,
    method: 'delete'
  })
}

// 提交实验代码
export function submitExperiment(id, data) {
  return request({
    url: `/experiments/${id}/submit`,
    method: 'post',
    data
  })
}

// 获取实验结果
export function getExperimentResult(id) {
  return request({
    url: `/experiments/${id}/result`,
    method: 'get'
  })
}
