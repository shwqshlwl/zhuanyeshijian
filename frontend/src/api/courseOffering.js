import request from '@/utils/request'

// 查询教师的课程模板列表（含指定学期开课状态）
export function getTeacherCourseTemplates(params) {
  return request({
    url: '/course-offerings/teacher/templates',
    method: 'get',
    params
  })
}

// 创建开课实例
export function createOffering(data) {
  return request({
    url: '/course-offerings',
    method: 'post',
    data
  })
}

// 获取开课实例详情
export function getOfferingDetail(id) {
  return request({
    url: `/course-offerings/${id}`,
    method: 'get'
  })
}

// 更新开课实例
export function updateOffering(id, data) {
  return request({
    url: `/course-offerings/${id}`,
    method: 'put',
    data
  })
}

// 删除开课实例
export function deleteOffering(id) {
  return request({
    url: `/course-offerings/${id}`,
    method: 'delete'
  })
}

// 查询学期的开课实例列表
export function getOfferingsBySemester(semesterId, params) {
  return request({
    url: `/course-offerings/semester/${semesterId}`,
    method: 'get',
    params
  })
}

// 查询教师的开课实例列表
export function getTeacherOfferings(params) {
  return request({
    url: '/course-offerings/teacher/offerings',
    method: 'get',
    params
  })
}

// 检查课程在指定学期是否已开课
export function isOfferedInSemester(courseId, semesterId) {
  return request({
    url: '/course-offerings/check',
    method: 'get',
    params: { courseId, semesterId }
  })
}
