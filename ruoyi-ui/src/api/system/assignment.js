import request from '@/utils/request'

// 查询作业或考试列表
export function listAssignment(query) {
  return request({
    url: '/system/assignment/list',
    method: 'get',
    params: query
  })
}

// 查询作业或考试详细
export function getAssignment(id) {
  return request({
    url: '/system/assignment/' + id,
    method: 'get'
  })
}

// 新增作业或考试
export function addAssignment(data) {
  return request({
    url: '/system/assignment',
    method: 'post',
    data: data
  })
}

// 修改作业或考试
export function updateAssignment(data) {
  return request({
    url: '/system/assignment',
    method: 'put',
    data: data
  })
}

// 删除作业或考试
export function delAssignment(id) {
  return request({
    url: '/system/assignment/' + id,
    method: 'delete'
  })
}

// 根据知识点ID查询相关作业/考试/测验
export function getAssignmentsByKnowledgePoint(kpId) {
  return request({
    url: '/system/assignment/byKnowledgePoint/' + kpId,
    method: 'get'
  })
}
