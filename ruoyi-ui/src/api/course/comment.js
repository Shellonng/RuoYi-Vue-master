import request from '@/utils/request'

// 查询小节评论列表
export function listComment(query) {
  return request({
    url: '/system/comment/list',
    method: 'get',
    params: query
  })
}

// 根据课程ID查询评论列表
export function listCommentByCourse(courseId, query) {
  return request({
    url: '/system/comment/listByCourse/' + courseId,
    method: 'get',
    params: query
  })
}

// 根据小节ID查询评论树形列表
export function getCommentTree(sectionId) {
  return request({
    url: '/system/comment/tree/' + sectionId,
    method: 'get'
  })
}

// 查询评论详细
export function getComment(id) {
  return request({
    url: '/system/comment/' + id,
    method: 'get'
  })
}

// 新增评论
export function addComment(data) {
  return request({
    url: '/system/comment',
    method: 'post',
    data: data
  })
}

// 修改评论
export function updateComment(data) {
  return request({
    url: '/system/comment',
    method: 'put',
    data: data
  })
}

// 删除评论
export function delComment(id) {
  return request({
    url: '/system/comment/' + id,
    method: 'delete'
  })
}

// 批量删除评论
export function delComments(ids) {
  return request({
    url: '/system/comment/' + ids,
    method: 'delete'
  })
}

// 获取当前登录用户信息
export function getCurrentUser() {
  return request({
    url: '/system/comment/currentUser',
    method: 'get'
  })
}

