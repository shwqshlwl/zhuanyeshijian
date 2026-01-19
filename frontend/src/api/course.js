import request from '@/utils/request'

// 获取课程列表
export function getCourseList(params) {
  return request({
    url: '/courses',
    method: 'get',
    params
  })
}

// 创建课程
export function createCourse(data) {
  return request({
    url: '/courses',
    method: 'post',
    data
  })
}

// 获取课程详情
export function getCourseById(id) {
  return request({
    url: `/courses/${id}`,
    method: 'get'
  })
}

// 更新课程
export function updateCourse(id, data) {
  return request({
    url: `/courses/${id}`,
    method: 'put',
    data
  })
}

// 更新课程状态
export function updateCourseStatus(id, status) {
  return request({
    url: `/courses/${id}/status`,
    method: 'put',
    params: { status }
  })
}

// 删除课程
export function deleteCourse(id) {
  return request({
    url: `/courses/${id}`,
    method: 'delete'
  })
}

// 获取课程的选课学生列表
export function getCourseStudents(id) {
  return request({
    url: `/courses/${id}/students`,
    method: 'get'
  })
}

// 移除课程的选课学生
export function removeStudentFromCourse(courseId, studentId) {
  return request({
    url: `/courses/${courseId}/students/${studentId}`,
    method: 'delete'
  })
}
