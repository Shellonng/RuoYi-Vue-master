import request from '@/utils/request'

// 获取课程的知识点列表
export function getKnowledgePointsByCourse(courseId) {
  return request({
    url: `/knowledgePoint/listByCourse/${courseId}`,
    method: 'get'
  })
}

// 获取知识点列表（带分页）
export function listKnowledgePoint(query) {
  return request({
    url: '/knowledgePoint/list',
    method: 'get',
    params: query
  })
}

// 获取知识点详细信息
export function getKnowledgePoint(id) {
  return request({
    url: '/knowledgePoint/' + id,
    method: 'get'
  })
}

// 新增知识点
export function addKnowledgePoint(data) {
  return request({
    url: '/knowledgePoint',
    method: 'post',
    data: data
  })
}

// 修改知识点
export function updateKnowledgePoint(data) {
  return request({
    url: '/knowledgePoint',
    method: 'put',
    data: data
  })
}

// 删除知识点
export function delKnowledgePoint(id) {
  return request({
    url: '/knowledgePoint/' + id,
    method: 'delete'
  })
}
