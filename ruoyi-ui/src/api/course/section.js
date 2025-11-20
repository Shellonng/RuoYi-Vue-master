import request from '@/utils/request'

// 查询小节列表
export function listSection(query) {
  return request({
    url: '/section/list',
    method: 'get',
    params: query
  })
}

// 根据章节ID查询小节列表
export function listSectionByChapter(chapterId) {
  return request({
    url: '/section/listByChapter/' + chapterId,
    method: 'get'
  })
}

// 查询小节详细
export function getSection(id) {
  return request({
    url: '/section/' + id,
    method: 'get'
  })
}

// 新增小节
export function addSection(data) {
  return request({
    url: '/section',
    method: 'post',
    data: data
  })
}

// 修改小节
export function updateSection(data) {
  return request({
    url: '/section',
    method: 'put',
    data: data
  })
}

// 删除小节
export function delSection(id) {
  return request({
    url: '/section/' + id,
    method: 'delete'
  })
}

// 批量删除小节
export function delSections(ids) {
  return request({
    url: '/section/' + ids,
    method: 'delete'
  })
}
