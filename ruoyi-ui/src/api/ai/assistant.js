import request from '@/utils/request'

/**
 * 调用AI教学助手
 */
export function callAIAssistant(data) {
  return request({
    url: '/ai/assistant/chat',
    method: 'post',
    data: data
  })
}
