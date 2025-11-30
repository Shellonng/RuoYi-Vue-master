import smartRequest from '@/utils/smartRequest'

/**
 * 作业管理 API
 * 后端端口: 8083
 */

// 创建作业
export function createAssignment(data) {
  return smartRequest({
    url: '/api/assignments/create',
    method: 'post',
    data: data
  })
}

// 更新作业
export function updateAssignment(id, data) {
  return smartRequest({
    url: `/api/assignments/${id}`,
    method: 'put',
    data: data
  })
}

// 删除作业
export function deleteAssignment(id) {
  return smartRequest({
    url: `/api/assignments/${id}`,
    method: 'delete'
  })
}

// 发布作业
export function publishAssignment(data) {
  return smartRequest({
    url: '/api/assignments/publish',
    method: 'post',
    data: data
  })
}

// 撤销发布
export function unpublishAssignment(id) {
  return smartRequest({
    url: `/api/assignments/${id}/unpublish`,
    method: 'post'
  })
}

// 获取作业详情
export function getAssignmentDetail(id) {
  return smartRequest({
    url: `/api/assignments/${id}`,
    method: 'get'
  })
}

// 获取作业列表
export function getAssignmentList(params) {
  return smartRequest({
    url: '/api/assignments/list',
    method: 'get',
    params: params
  })
}

// 添加题目到作业
export function addQuestionsToAssignment(id, questionIds) {
  return smartRequest({
    url: `/api/assignments/${id}/questions/add`,
    method: 'post',
    data: { questionIds }
  })
}

// 从作业中移除题目
export function removeQuestionsFromAssignment(id, questionIds) {
  return smartRequest({
    url: `/api/assignments/${id}/questions/remove`,
    method: 'post',
    data: { questionIds }
  })
}

// 获取作业的题目列表
export function getAssignmentQuestions(id) {
  return smartRequest({
    url: `/api/assignments/${id}/questions`,
    method: 'get'
  })
}

// 批量获取已发布作业
export function getPublishedAssignments(params) {
  return smartRequest({
    url: '/api/assignments/published',
    method: 'get',
    params: params
  })
}

// 获取作业提交列表
export function getSubmissionList(params) {
  return smartRequest({
    url: '/api/submissions/list',
    method: 'get',
    params: params
  })
}

// 获取提交详情
export function getSubmissionDetail(submissionId) {
  return smartRequest({
    url: `/api/submissions/${submissionId}`,
    method: 'get'
  })
}

// 批改作业（手动批改）
export function gradeSubmission(submissionId, data) {
  return smartRequest({
    url: `/api/submissions/${submissionId}/grade`,
    method: 'post',
    data: data
  })
}

// 获取待批改作业列表
export function getPendingGradingList() {
  return smartRequest({
    url: '/api/ai-grading/pending',
    method: 'get'
  })
}

// 批量AI批改
export function batchAIGrading(submissionIds, llmModel) {
  return smartRequest({
    url: '/api/ai-grading/grading/batch',
    method: 'post',
    data: {
      submissionIds,
      llmModel
    }
  })
}

// 获取AI批改结果
export function getAIGradingResult(submissionId) {
  return smartRequest({
    url: `/api/ai-grading/result/${submissionId}`,
    method: 'get'
  })
}

// 获取作业统计数据
export function getAssignmentStats(courseId) {
  return smartRequest({
    url: `/api/assignments/stats/${courseId}`,
    method: 'get'
  })
}
