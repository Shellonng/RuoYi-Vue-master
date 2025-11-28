import request from '@/utils/request'

// 查询作业提交记录列表
export function listAssignmentSubmission(query) {
  return request({
    url: '/system/assignment/submission/list',
    method: 'get',
    params: query
  })
}

// 获取作业提交详情
export function getAssignmentSubmission(id) {
  return request({
    url: `/system/assignment/submission/${id}`,
    method: 'get'
  })
}

// 批改作业提交
export function gradeAssignmentSubmission(data) {
  return request({
    url: '/system/assignment/submission/grade',
    method: 'put',
    data
  })
}
