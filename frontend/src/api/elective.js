import request from '@/utils/request'

/**
 * 获取选课中心课程列表
 */
export function getElectiveCourseList(params) {
  return request({
    url: '/student-courses/elective',
    method: 'get',
    params
  })
}

/**
 * 选课
 */
export function selectCourse(courseId) {
  return request({
    url: `/student-courses/select/${courseId}`,
    method: 'post'
  })
}

/**
 * 退课
 */
export function dropCourse(courseId) {
  return request({
    url: `/student-courses/drop/${courseId}`,
    method: 'delete'
  })
}
