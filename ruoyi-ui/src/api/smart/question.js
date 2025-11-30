import smartRequest from '@/utils/smartRequest'

/**
 * 题库管理 API
 * 后端端口: 8083
 */

// 创建题目
export function createQuestion(data) {
  return smartRequest({
    url: '/api/questions',
    method: 'post',
    data: data
  })
}

// 更新题目
export function updateQuestion(id, data) {
  return smartRequest({
    url: `/api/questions/${id}`,
    method: 'put',
    data: data
  })
}

// 删除题目
export function deleteQuestion(id) {
  return smartRequest({
    url: `/api/questions/${id}`,
    method: 'delete'
  })
}

// 批量删除题目
export function batchDeleteQuestions(ids) {
  return smartRequest({
    url: '/api/questions/batch',
    method: 'delete',
    data: { ids }
  })
}

// 获取题目详情
export function getQuestionDetail(id) {
  return smartRequest({
    url: `/api/questions/${id}`,
    method: 'get'
  })
}

// 获取题目列表（分页查询）
export function getQuestionList(params) {
  return smartRequest({
    url: '/api/questions',
    method: 'get',
    params: params
  })
}

// 按课程获取题目列表
export function getQuestionsByCourse(courseId, params) {
  return smartRequest({
    url: `/api/questions/course/${courseId}`,
    method: 'get',
    params: params
  })
}

// 按题型获取题目
export function getQuestionsByType(type, params) {
  return smartRequest({
    url: `/api/questions/type/${type}`,
    method: 'get',
    params: params
  })
}

// 按知识点获取题目
export function getQuestionsByKnowledge(knowledgePointId, params) {
  return smartRequest({
    url: `/api/questions/knowledge/${knowledgePointId}`,
    method: 'get',
    params: params
  })
}

// 批量导入题目
export function importQuestions(file, courseId) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('courseId', courseId)
  
  return smartRequest({
    url: '/api/questions/import',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 导出题目
export function exportQuestions(params) {
  return smartRequest({
    url: '/api/questions/export',
    method: 'get',
    params: params,
    responseType: 'blob'
  })
}

// 获取题型统计
export function getQuestionTypeStats(courseId) {
  return smartRequest({
    url: '/api/questions/statistics/question-type',
    method: 'get',
    params: { courseId }
  })
}

// 获取难度分布统计
export function getQuestionDifficultyStats(courseId) {
  return smartRequest({
    url: '/api/questions/statistics/difficulty',
    method: 'get',
    params: { courseId }
  })
}

// 搜索题目
export function searchQuestions(keyword, params) {
  return smartRequest({
    url: '/api/questions/search',
    method: 'get',
    params: {
      keyword,
      ...params
    }
  })
}
