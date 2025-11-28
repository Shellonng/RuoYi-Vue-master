import request from '@/utils/request'

// 获取当前登录教师的信息
export function getTeacherProfile() {
  return request({
    url: '/system/teacher/profile',
    method: 'get'
  })
}

// 查询教师详细信息
export function getTeacher(id) {
  return request({
    url: '/system/teacher/' + id,
    method: 'get'
  })
}

// 根据用户ID查询教师信息
export function getTeacherByUserId(userId) {
  return request({
    url: '/system/teacher/byUserId/' + userId,
    method: 'get'
  })
}

// 新增教师信息
export function addTeacher(data) {
  return request({
    url: '/system/teacher',
    method: 'post',
    data: data
  })
}

// 修改教师信息
export function updateTeacher(data) {
  return request({
    url: '/system/teacher',
    method: 'put',
    data: data
  })
}

// 删除教师信息
export function delTeacher(id) {
  return request({
    url: '/system/teacher/' + id,
    method: 'delete'
  })
}
