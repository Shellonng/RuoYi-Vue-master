import request from '@/utils/request'

// 查询任务关联的知识点列表
export function listAssignmentKp(assignmentId) {
  return request({
    url: '/system/assignmentKp/list/' + assignmentId,
    method: 'get'
  })
}

// 批量添加任务知识点关联
export function batchAddAssignmentKp(data) {
  return request({
    url: '/system/assignmentKp/batch',
    method: 'post',
    data: data
  })
}

// 删除任务知识点关联
export function delAssignmentKp(id) {
  return request({
    url: '/system/assignmentKp/' + id,
    method: 'delete'
  })
}

// 设置任务的知识点（替换所有）
export function setAssignmentKnowledgePoints(assignmentId, kpIds) {
  return request({
    url: '/system/assignmentKp/set/' + assignmentId,
    method: 'post',
    data: kpIds
  })
}
