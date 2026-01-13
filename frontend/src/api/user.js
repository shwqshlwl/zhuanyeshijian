import request from '@/utils/request'

// 用户登录
export function login(data) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

// 教师注册
export function registerTeacher(data) {
  return request({
    url: '/auth/register/teacher',
    method: 'post',
    data
  })
}

// 获取当前用户信息
export function getUserInfo() {
  return request({
    url: '/user/info',
    method: 'get'
  })
}

// 修改密码
export function changePassword(data) {
  return request({
    url: '/user/password',
    method: 'put',
    data
  })
}

// 批量导入学生
export function batchImportStudents(data) {
  return request({
    url: '/user/batch-import-students',
    method: 'post',
    data
  })
}

// 根据学号查询学生
export function getStudentByStudentNo(studentNo) {
  return request({
    url: `/user/student/${studentNo}`,
    method: 'get'
  })
}

// 更新用户信息
export function updateUserInfo(data) {
  return request({
    url: '/user/info',
    method: 'put',
    data
  })
}
