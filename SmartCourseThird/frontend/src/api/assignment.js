import { BASE_URL } from '../utils/api'
import { getSession } from '../utils/session'

/**
 * 作业管理 API
 */

// 创建作业
export const createAssignment = async (data) => {
  const session = getSession()
  const res = await fetch(`${BASE_URL}/api/assignments/create`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'userId': session?.user?.id || '20001'
    },
    body: JSON.stringify(data)
  })
  return res.json()
}

// 更新作业
export const updateAssignment = async (id, data) => {
  const session = getSession()
  const res = await fetch(`${BASE_URL}/api/assignments/${id}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      'userId': session?.user?.id || '20001'
    },
    body: JSON.stringify(data)
  })
  return res.json()
}

// 删除作业
export const deleteAssignment = async (id) => {
  const session = getSession()
  const res = await fetch(`${BASE_URL}/api/assignments/${id}`, {
    method: 'DELETE',
    headers: {
      'Content-Type': 'application/json',
      'userId': session?.user?.id || '20001'
    }
  })
  return res.json()
}

// 发布作业
export const publishAssignment = async (data) => {
  const session = getSession()
  const res = await fetch(`${BASE_URL}/api/assignments/publish`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'userId': session?.user?.id || '20001'
    },
    body: JSON.stringify(data)
  })
  return res.json()
}

// 撤销发布
export const unpublishAssignment = async (id) => {
  const session = getSession()
  const res = await fetch(`${BASE_URL}/api/assignments/${id}/unpublish`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'userId': session?.user?.id || '20001'
    }
  })
  return res.json()
}

// 获取作业详情
export const getAssignmentDetail = async (id) => {
  const res = await fetch(`${BASE_URL}/api/assignments/${id}`)
  return res.json()
}

// 获取作业列表
export const getAssignmentList = async (params) => {
  const search = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      search.set(key, value)
    }
  })
  const query = search.toString()
  const res = await fetch(`${BASE_URL}/api/assignments/list${query ? `?${query}` : ''}`)
  return res.json()
}

// 添加题目到作业
export const addQuestionsToAssignment = async (id, questionIds) => {
  const session = getSession()
  const res = await fetch(`${BASE_URL}/api/assignments/${id}/questions/add`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'userId': session?.user?.id || '20001'
    },
    body: JSON.stringify(questionIds)
  })
  return res.json()
}

// 从作业移除题目
export const removeQuestionsFromAssignment = async (id, questionIds) => {
  const session = getSession()
  const res = await fetch(`${BASE_URL}/api/assignments/${id}/questions/remove`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'userId': session?.user?.id || '20001'
    },
    body: JSON.stringify(questionIds)
  })
  return res.json()
}
