import request from '@/utils/request'

// 查询知识点关系列表
export function listKpRelation(query) {
  return request({
    url: '/kpRelation/list',
    method: 'get',
    params: query
  })
}

// 根据课程ID查询所有知识点关系
export function listKpRelationByCourse(courseId) {
  return request({
    url: '/kpRelation/listByCourse/' + courseId,
    method: 'get'
  })
}

// 新增知识点关系
export function addKpRelation(data) {
  return request({
    url: '/kpRelation',
    method: 'post',
    data: data
  })
}

// 批量新增知识点关系
export function batchAddKpRelations(data) {
  return request({
    url: '/kpRelation/batch',
    method: 'post',
    data: data
  })
}

// 删除知识点关系
export function delKpRelation(id) {
  return request({
    url: '/kpRelation/' + id,
    method: 'delete'
  })
}

// 根据课程ID删除所有知识点关系
export function delKpRelationsByCourse(courseId) {
  return request({
    url: '/kpRelation/byCourse/' + courseId,
    method: 'delete'
  })
}

/**
 * AI生成课程知识点关系图谱
 * @param {Number} courseId - 课程ID
 */
export function generateKnowledgeGraph(courseId) {
  return request({
    url: '/kpRelation/generateGraph/' + courseId,
    method: 'post',
    timeout: 180000  // 3分钟超时，AI处理需要时间
  })
}
