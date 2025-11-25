import request from '@/utils/request'

// 查询选课列表
export function listEnrollment(query) {
  return request({
    url: '/system/enrollment/list',
    method: 'get',
    params: query
  })
}

// 查询选课详细
export function getEnrollment(id) {
  return request({
    url: '/system/enrollment/' + id,
    method: 'get'
  })
}

// 新增选课
export function addEnrollment(data) {
  return request({
    url: '/system/enrollment',
    method: 'post',
    data: data
  })
}

// 修改选课
export function updateEnrollment(data) {
  return request({
    url: '/system/enrollment',
    method: 'put',
    data: data
  })
}

// 删除选课
export function delEnrollment(id) {
  return request({
    url: '/system/enrollment/' + id,
    method: 'delete'
  })
}

// 批量通过选课申请
export function batchApproveEnrollment(data) {
  return request({
    url: '/system/enrollment/batchApprove',
    method: 'post',
    data: data
  })
}

// 批量拒绝选课申请
export function batchRejectEnrollment(data) {
  return request({
    url: '/system/enrollment/batchReject',
    method: 'post',
    data: data
  })
}

// 导出选课
export function exportEnrollment(query) {
  return request({
    url: '/system/enrollment/export',
    method: 'get',
    params: query
  })
}
