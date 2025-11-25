import request from '@/utils/request'

/**
 * 任务3-资源智能打标 API
 */

// 查询课程资源列表
export function listResourceRenwu3(query) {
  return request({
    url: '/system/resource/renwu3/list',
    method: 'get',
    params: query
  })
}

// 查询课程资源详细
export function getResourceRenwu3(id) {
  return request({
    url: '/system/resource/renwu3/' + id,
    method: 'get'
  })
}

// 新增课程资源
export function addResourceRenwu3(data) {
  return request({
    url: '/system/resource/renwu3',
    method: 'post',
    data: data
  })
}

// 修改课程资源
export function updateResourceRenwu3(data) {
  return request({
    url: '/system/resource/renwu3',
    method: 'put',
    data: data
  })
}

// 删除课程资源
export function delResourceRenwu3(ids) {
  return request({
    url: '/system/resource/renwu3/' + ids,
    method: 'delete'
  })
}

/**
 * 核心功能：上传资源并智能分析
 * 调用DeepSeek API提取知识点
 * 视频文件需要较长处理时间（语音识别）
 */
export function uploadAndAnalyzeRenwu3(data) {
  return request({
    url: '/system/resource/renwu3/upload',
    method: 'post',
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    timeout: 600000, // 5分钟超时（视频转录可能需要较长时间）
    data: data
  })
}

/**
 * 确认资源关联的知识点
 * 教师确认AI推荐后保存关联
 */
export function confirmKnowledgePointsRenwu3(data) {
  return request({
    url: '/system/resource/renwu3/confirmKnowledgePoints',
    method: 'post',
    params: data
  })
}

/**
 * 获取资源关联的知识点
 */
export function getResourceKnowledgePointsRenwu3(resourceId) {
  return request({
    url: '/system/resource/renwu3/knowledgePoints/' + resourceId,
    method: 'get'
  })
}

/**
 * 与AI助手对话（任务3评价系统）
 */
export function chatWithAIRenwu3(data) {
  return request({
    url: '/system/resource/renwu3/chat',
    method: 'post',
    params: data,
    timeout: 60000 // AI对话可能需要较长时间，设置60秒超时
  })
}
