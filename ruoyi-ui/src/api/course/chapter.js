import request from '@/utils/request'

// 查询章节列表
export function listChapter(query) {
  return request({
    url: '/chapter/list',
    method: 'get',
    params: query
  })
}

// 根据课程ID查询章节列表
export function listChapterByCourse(courseId) {
  return request({
    url: '/chapter/listByCourse/' + courseId,
    method: 'get'
  })
}

// 查询章节详细
export function getChapter(id) {
  return request({
    url: '/chapter/' + id,
    method: 'get'
  })
}

// 新增章节
export function addChapter(data) {
  return request({
    url: '/chapter',
    method: 'post',
    data: data
  })
}

// 修改章节
export function updateChapter(data) {
  return request({
    url: '/chapter',
    method: 'put',
    data: data
  })
}

// 删除章节
export function delChapter(id) {
  return request({
    url: '/chapter/' + id,
    method: 'delete'
  })
}

// 批量删除章节
export function delChapters(ids) {
  return request({
    url: '/chapter/' + ids,
    method: 'delete'
  })
}
