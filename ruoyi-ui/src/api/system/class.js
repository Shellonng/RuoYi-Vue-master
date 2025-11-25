import request from '@/utils/request'

// 查询班级列表
export function listClass(query) {
  return request({
    url: '/system/class/list',
    method: 'get',
    params: query
  })
}

// 查询班级详细
export function getClass(id) {
  return request({
    url: '/system/class/' + id,
    method: 'get'
  })
}

// 修改班级
export function updateClass(data) {
  return request({
    url: '/system/class',
    method: 'put',
    data: data
  })
}

// 查询班级学生列表
export function listClassStudent(courseId, query) {
  return request({
    url: '/system/class/student/list',
    method: 'get',
    params: { courseId, ...query }
  })
}

// 移除班级学生
export function removeClassStudent(ids) {
  return request({
    url: '/system/class/student/' + ids,
    method: 'delete'
  })
}
