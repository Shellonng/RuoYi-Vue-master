import { aiService } from '@/utils/smartRequest'

/**
 * AI Agent 智能组卷 API
 * 后端端口: 8000 (Python FastAPI)
 */

// 健康检查
export function checkAIHealth() {
  return aiService({
    url: '/health',
    method: 'get'
  }).catch(error => {
    console.error('AI 健康检查失败:', error)
    return { status: 'error', message: 'AI服务未启动或连接失败' }
  })
}

// 获取课程知识点
export function getCourseKnowledgePoints(courseId) {
  return aiService({
    url: `/courses/${courseId}/knowledge-points`,
    method: 'get'
  })
}

// 获取题型库存
export function getQuestionTypes(courseId) {
  return aiService({
    url: `/courses/${courseId}/question-types`,
    method: 'get'
  })
}

// 智能组卷
export function assembleTest(data) {
  return aiService({
    url: '/assemble',
    method: 'post',
    data: data,
    timeout: 60000 // 组卷可能需要较长时间
  })
}

// 获取组卷历史
export function getAssembleHistory(params) {
  return aiService({
    url: '/assemble/history',
    method: 'get',
    params: params
  })
}

// 评估试卷质量
export function evaluateTestPaper(data) {
  return aiService({
    url: '/assemble/evaluate',
    method: 'post',
    data: data
  })
}

// 优化试卷
export function optimizeTestPaper(data) {
  return aiService({
    url: '/assemble/optimize',
    method: 'post',
    data: data
  })
}

// 创建 WebSocket 连接（用于实时组卷进度）
export function createWebSocket(onMessage, onError, onClose) {
  const wsUrl = 'ws://localhost:8000/ws/assemble'
  const ws = new WebSocket(wsUrl)
  
  ws.onopen = () => {
    console.log('AI Agent WebSocket 连接已建立')
  }
  
  ws.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data)
      if (onMessage) {
        onMessage(data)
      }
    } catch (error) {
      console.error('WebSocket 消息解析失败:', error)
    }
  }
  
  ws.onerror = (error) => {
    console.error('WebSocket 错误:', error)
    if (onError) {
      onError(error)
    }
  }
  
  ws.onclose = () => {
    console.log('AI Agent WebSocket 连接已关闭')
    if (onClose) {
      onClose()
    }
  }
  
  return ws
}

// 发送组卷请求（通过 WebSocket）
export function sendAssembleRequest(ws, data) {
  if (ws && ws.readyState === WebSocket.OPEN) {
    ws.send(JSON.stringify(data))
  } else {
    console.error('WebSocket 未连接')
  }
}
