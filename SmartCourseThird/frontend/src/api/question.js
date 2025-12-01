import { request, BASE_URL } from '../utils/api'
import { getSession } from '../utils/session'

/**
 * 题目管理 API
 */

// 创建题目
export const createQuestion = async (data) => {
  const session = getSession()
  const res = await fetch(`${BASE_URL}/api/questions`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'userId': session?.user?.id || '20001'
    },
    body: JSON.stringify(data)
  })
  return res.json()
}

// 更新题目
export const updateQuestion = async (id, data) => {
  const session = getSession()
  const res = await fetch(`${BASE_URL}/api/questions/${id}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      'userId': session?.user?.id || '20001'
    },
    body: JSON.stringify(data)
  })
  return res.json()
}

// 删除题目
export const deleteQuestion = async (id) => {
  const session = getSession()
  const res = await fetch(`${BASE_URL}/api/questions/${id}`, {
    method: 'DELETE',
    headers: {
      'Content-Type': 'application/json',
      'userId': session?.user?.id || '20001'
    }
  })
  return res.json()
}

// 批量删除题目
export const batchDeleteQuestions = async (ids) => {
  const session = getSession()
  const res = await fetch(`${BASE_URL}/api/questions/batch`, {
    method: 'DELETE',
    headers: {
      'Content-Type': 'application/json',
      'userId': session?.user?.id || '20001'
    },
    body: JSON.stringify(ids)
  })
  return res.json()
}

// 获取题目详情
export const getQuestionDetail = async (id) => {
  const res = await fetch(`${BASE_URL}/api/questions/${id}`)
  return res.json()
}

// 分页查询题目
export const getQuestions = async (params) => {
  const search = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      search.set(key, value)
    }
  })
  const query = search.toString()
  const res = await fetch(`${BASE_URL}/api/questions${query ? `?${query}` : ''}`)
  return res.json()
}

// 按题型统计
export const getQuestionTypeStatistics = async (courseId) => {
  const res = await fetch(`${BASE_URL}/api/questions/statistics/question-type?courseId=${courseId}`)
  return res.json()
}

// 按难度统计
export const getDifficultyStatistics = async (courseId) => {
  const res = await fetch(`${BASE_URL}/api/questions/statistics/difficulty?courseId=${courseId}`)
  return res.json()
}

// 按知识点统计
export const getKnowledgePointStatistics = async (courseId) => {
  const res = await fetch(`${BASE_URL}/api/questions/statistics/knowledge-point?courseId=${courseId}`)
  return res.json()
}

// 批量导入题目
export const importQuestions = async (data) => {
  const session = getSession()
  const res = await fetch(`${BASE_URL}/api/questions/import`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'userId': session?.user?.id || '20001'
    },
    body: JSON.stringify(data)
  })
  return res.json()
}

// 获取知识点列表
export const getKnowledgePoints = async (courseId) => {
  const res = await fetch(`${BASE_URL}/api/knowledge-points/list?courseId=${courseId}`)
  return res.json()
}
