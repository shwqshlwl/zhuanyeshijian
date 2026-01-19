import request from '@/utils/request'

// 获取学期列表
export function getSemesterList(params) {
  return request({
    url: '/semesters',
    method: 'get',
    params
  })
}

// 获取所有学期（用于下拉选择）
export function getAllSemesters() {
  return request({
    url: '/semesters/all',
    method: 'get'
  })
}

// 获取当前学期
export function getCurrentSemester() {
  return request({
    url: '/semesters/current',
    method: 'get'
  })
}

// 创建学期
export function createSemester(data) {
  return request({
    url: '/semesters',
    method: 'post',
    data
  })
}

// 更新学期
export function updateSemester(id, data) {
  return request({
    url: `/semesters/${id}`,
    method: 'put',
    data
  })
}

// 删除学期
export function deleteSemester(id) {
  return request({
    url: `/semesters/${id}`,
    method: 'delete'
  })
}

// 设置为当前学期
export function setCurrentSemester(id) {
  return request({
    url: `/semesters/${id}/set-current`,
    method: 'put'
  })
}

// 结束学期
export function endSemester(id) {
  return request({
    url: `/semesters/${id}/end`,
    method: 'put'
  })
}
