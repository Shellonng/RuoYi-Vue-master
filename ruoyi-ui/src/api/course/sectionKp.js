import request from '@/utils/request'

// 查询小节知识点关联列表
export function listSectionKp(query) {
  return request({
    url: '/sectionKp/list',
    method: 'get',
    params: query
  })
}

// 根据小节ID查询关联的知识点列表（含知识点详情）
export function listSectionKpBySection(sectionId) {
  return request({
    url: '/sectionKp/listBySection/' + sectionId,
    method: 'get'
  })
}

// 根据知识点ID查询关联的小节列表
export function listSectionKpByKp(kpId) {
  return request({
    url: '/sectionKp/listByKp/' + kpId,
    method: 'get'
  })
}

// 查询小节知识点关联详细
export function getSectionKp(id) {
  return request({
    url: '/sectionKp/' + id,
    method: 'get'
  })
}

// 新增小节知识点关联
export function addSectionKp(data) {
  return request({
    url: '/sectionKp',
    method: 'post',
    data: data
  })
}

// 删除小节知识点关联
export function delSectionKp(id) {
  return request({
    url: '/sectionKp/' + id,
    method: 'delete'
  })
}

// 删除小节的所有知识点关联
export function delSectionKpBySection(sectionId) {
  return request({
    url: '/sectionKp/section/' + sectionId,
    method: 'delete'
  })
}

// 批量新增小节知识点关联（用于AI生成课程结构）
export function batchAddSectionKp(data) {
  return request({
    url: '/sectionKp/batch',
    method: 'post',
    data: data
  })
}

// 为小节批量设置知识点（先删除旧关联，再创建新关联）
export function setSectionKnowledgePoints(sectionId, kpIds) {
  return request({
    url: '/sectionKp/setKnowledgePoints/' + sectionId,
    method: 'put',
    data: kpIds
  })
}
