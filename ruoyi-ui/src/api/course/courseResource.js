import request from '@/utils/request'

// 查询课程资源列表
export function listCourseResource(query) {
  return request({
    url: '/courseResource/list',
    method: 'get',
    params: query
  })
}

// 查询课程资源详细
export function getCourseResource(id) {
  return request({
    url: '/courseResource/' + id,
    method: 'get'
  })
}

// 根据知识点ID查询关联的课程资源
export function getCourseResourcesByKnowledgePoint(kpId) {
  return request({
    url: '/courseResource/byKnowledgePoint/' + kpId,
    method: 'get'
  })
}

// 新增课程资源
export function addCourseResource(data) {
  return request({
    url: '/courseResource',
    method: 'post',
    data: data
  })
}

// 修改课程资源
export function updateCourseResource(data) {
  return request({
    url: '/courseResource',
    method: 'put',
    data: data
  })
}

// 删除课程资源
export function delCourseResource(id) {
  return request({
    url: '/courseResource/' + id,
    method: 'delete'
  })
}

// 批量删除课程资源
export function delCourseResources(ids) {
  return request({
    url: '/courseResource/' + ids,
    method: 'delete'
  })
}

// 导出课程资源
export function exportCourseResource(query) {
  return request({
    url: '/courseResource/export',
    method: 'post',
    params: query
  })
}
