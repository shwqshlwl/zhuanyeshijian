import request from '@/utils/request'

// 获取班级列表
export function getClassList(params) {
  return request({
    url: '/classes',
    method: 'get',
    params
  })
}

// 创建班级
export function createClass(data) {
  return request({
    url: '/classes',
    method: 'post',
    data
  })
}

// 获取班级详情
export function getClassById(id) {
  return request({
    url: `/classes/${id}`,
    method: 'get'
  })
}

// 更新班级
export function updateClass(id, data) {
  return request({
    url: `/classes/${id}`,
    method: 'put',
    data
  })
}

// 删除班级
export function deleteClass(id) {
  return request({
    url: `/classes/${id}`,
    method: 'delete'
  })
}

// 添加学生到班级
export function addStudentToClass(classId, studentId) {
  return request({
    url: `/classes/${classId}/students/${studentId}`,
    method: 'post'
  })
}

// 从班级移除学生
export function removeStudentFromClass(classId, studentId) {
  return request({
    url: `/classes/${classId}/students/${studentId}`,
    method: 'delete'
  })
}

// 获取班级关联的课程
export function getClassCourses(classId) {
  return request({
    url: `/classes/${classId}/courses`,
    method: 'get'
  })
}

// 设置班级关联的课程
export function setClassCourses(classId, courseIds) {
  return request({
    url: `/classes/${classId}/courses`,
    method: 'put',
    data: courseIds
  })
}
