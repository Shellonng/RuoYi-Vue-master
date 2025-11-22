import request from '@/utils/request'

// 查询知识点列表
export function listKnowledgePoint(query) {
  return request({
    url: '/knowledgePoint/list',
    method: 'get',
    params: query
  })
}

// 根据课程ID查询知识点列表
export function listKnowledgePointByCourse(courseId) {
  return request({
    url: '/knowledgePoint/listByCourse/' + courseId,
    method: 'get'
  })
}

// 根据课程ID和难度级别查询知识点列表
export function listKnowledgePointByCourseAndLevel(courseId, level) {
  return request({
    url: '/knowledgePoint/listByCourse/' + courseId + '/level/' + level,
    method: 'get'
  })
}

// 根据小节ID查询关联的知识点列表
export function listKnowledgePointBySection(sectionId) {
  return request({
    url: '/sectionKp/listBySection/' + sectionId,
    method: 'get'
  })
}

// 查询知识点详细
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

// 批量新增知识点（用于AI生成课程结构）
export function batchAddKnowledgePoints(data) {
  return request({
    url: '/knowledgePoint/batch',
    method: 'post',
    data: data
  })
}

// 设置小节的知识点（先删除旧关联，再创建新关联）
export function setSectionKnowledgePoints(sectionId, kpIds) {
  return request({
    url: '/sectionKp/setKnowledgePoints/' + sectionId,
    method: 'put',
    data: kpIds
  })
}
