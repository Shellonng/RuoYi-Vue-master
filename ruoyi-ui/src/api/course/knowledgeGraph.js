import request from '@/utils/request'

// 查询知识图谱列表
export function listKnowledgeGraph(query) {
  return request({
    url: '/knowledgeGraph/list',
    method: 'get',
    params: query
  })
}

// 查询知识图谱详细
export function getKnowledgeGraph(id) {
  return request({
    url: '/knowledgeGraph/' + id,
    method: 'get'
  })
}

// 根据课程ID查询知识图谱
export function getKnowledgeGraphByCourse(courseId, graphType) {
  return request({
    url: '/knowledgeGraph/byCourse/' + courseId,
    method: 'get',
    params: { graphType: graphType || 'COURSE' }
  })
}

// 新增知识图谱
export function addKnowledgeGraph(data) {
  return request({
    url: '/knowledgeGraph',
    method: 'post',
    data: data
  })
}

// 修改知识图谱
export function updateKnowledgeGraph(data) {
  return request({
    url: '/knowledgeGraph',
    method: 'put',
    data: data
  })
}

// 保存或更新知识图谱（智能判断）
export function saveKnowledgeGraph(data) {
  return request({
    url: '/knowledgeGraph/save',
    method: 'post',
    data: data
  })
}

// 删除知识图谱
export function delKnowledgeGraph(id) {
  return request({
    url: '/knowledgeGraph/' + id,
    method: 'delete'
  })
}
