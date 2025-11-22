import request from '@/utils/request'

// 查询小节评论列表
export function listComment(query) {
  return request({
    url: '/system/comment/list',
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

// 获取当前登录的业务用户信息
export function getCurrentBusinessUser() {
  return request({
    url: '/system/comment/currentUser',
    method: 'get'
  })
}

// 查询小节评论详细
export function getComment(id) {
  return request({
    url: '/system/comment/' + id,
    method: 'get'
  })
}

// 新增小节评论
export function addComment(data) {
  return request({
    url: '/system/comment',
    method: 'post',
    data: data
  })
}

// 修改小节评论
export function updateComment(data) {
  return request({
    url: '/system/comment',
    method: 'put',
    data: data
  })
}

// 删除小节评论
export function delComment(ids) {
  return request({
    url: '/system/comment/' + ids,
    method: 'delete'
  })
}
