import { getSession } from '../utils/session'

const AI_BASE_URL = 'http://localhost:8001' // Python FastAPI 服务地址
const AI_WS_URL = 'ws://localhost:8001' // WebSocket 地址

/**
 * AI Agent 智能组卷 API
 */

// 健康检查
export const checkAIHealth = async () => {
  try {
    const res = await fetch(`${AI_BASE_URL}/health`, {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' }
    })
    return res.json()
  } catch (error) {
    console.error('AI 健康检查失败:', error)
    return { status: 'error' }
  }
}

// 获取课程知识点
export const getCourseKnowledgePoints = async (courseId) => {
  const res = await fetch(`${AI_BASE_URL}/courses/${courseId}/knowledge-points`, {
    method: 'GET',
    headers: { 'Content-Type': 'application/json' }
  })
  return res.json()
}

// 获取题型库存
export const getQuestionTypes = async (courseId) => {
  const res = await fetch(`${AI_BASE_URL}/courses/${courseId}/question-types`, {
    method: 'GET',
    headers: { 'Content-Type': 'application/json' }
  })
  return res.json()
}

// 智能组卷
export const assembleTest = async (data) => {
  const res = await fetch(`${AI_BASE_URL}/assemble`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  })
  return res.json()
}

// 查询组卷会话
export const getAssembleSession = async (sessionId) => {
  const res = await fetch(`${AI_BASE_URL}/sessions/${sessionId}`, {
    method: 'GET',
    headers: { 'Content-Type': 'application/json' }
  })
  return res.json()
}

// 发布组卷结果
export const publishAssembleResult = async (sessionId, data) => {
  console.log("类别:", data.assignment_type)
  const res = await fetch(`${AI_BASE_URL}/sessions/${sessionId}/publish`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  })
  return res.json()
}

/**
 * 对话式智能组卷 API (Workflow)
 */

// 创建对话会话
export const createConversation = async (courseId) => {
  const res = await fetch(`${AI_BASE_URL}/workflow/conversations`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ course_id: courseId })
  })
  return res.json()
}

// 发送消息到对话
export const sendMessage = async (conversationId, message) => {
  const res = await fetch(`${AI_BASE_URL}/workflow/conversations/${conversationId}/messages`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ message })
  })
  return res.json()
}

// 获取对话快照
export const getConversation = async (conversationId) => {
  const res = await fetch(`${AI_BASE_URL}/workflow/conversations/${conversationId}`, {
    method: 'GET',
    headers: { 'Content-Type': 'application/json' }
  })
  return res.json()
}

// 中断对话
export const abortConversation = async (conversationId) => {
  const res = await fetch(`${AI_BASE_URL}/workflow/conversations/${conversationId}/abort`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' }
  })
  return res.json()
}

/**
 * WebSocket 流式对话 API
 */

// WebSocket 连接管理类
export class ConversationWebSocket {
  constructor(conversationId) {
    this.conversationId = conversationId
    this.ws = null
    this.isConnected = false
    this.eventHandlers = {
      ready: [],
      thinking: [],
      chunk: [],
      action: [],
      result: [],
      published: [],
      done: [],
      error: [],
      aborted: [],
      close: [],
      open: []
    }
  }

  // 连接 WebSocket
  connect() {
    return new Promise((resolve, reject) => {
      if (this.ws && this.ws.readyState === WebSocket.OPEN) {
        resolve()
        return
      }

      this.ws = new WebSocket(`${AI_WS_URL}/ws/workflow/conversations/${this.conversationId}/messages`)

      this.ws.onopen = () => {
        this.isConnected = true
        this._emit('open')
        // 等待 ready 事件
      }

      this.ws.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data)
          const eventType = data.event
          
          if (eventType === 'ready') {
            resolve(data)
          }
          
          this._emit(eventType, data)
        } catch (e) {
          console.error('解析 WebSocket 消息失败:', e)
        }
      }

      this.ws.onerror = (error) => {
        console.error('WebSocket 错误:', error)
        this._emit('error', { message: 'WebSocket 连接错误' })
        reject(error)
      }

      this.ws.onclose = (event) => {
        this.isConnected = false
        this._emit('close', event)
      }

      // 设置超时
      setTimeout(() => {
        if (!this.isConnected) {
          reject(new Error('WebSocket 连接超时'))
        }
      }, 10000)
    })
  }

  // 发送消息
  sendMessage(message) {
    if (!this.ws || this.ws.readyState !== WebSocket.OPEN) {
      throw new Error('WebSocket 未连接')
    }
    this.ws.send(JSON.stringify({ message }))
  }

  // 注册事件处理器
  on(eventType, handler) {
    if (this.eventHandlers[eventType]) {
      this.eventHandlers[eventType].push(handler)
    }
    return this
  }

  // 移除事件处理器
  off(eventType, handler) {
    if (this.eventHandlers[eventType]) {
      const index = this.eventHandlers[eventType].indexOf(handler)
      if (index > -1) {
        this.eventHandlers[eventType].splice(index, 1)
      }
    }
    return this
  }

  // 触发事件
  _emit(eventType, data) {
    if (this.eventHandlers[eventType]) {
      this.eventHandlers[eventType].forEach(handler => {
        try {
          handler(data)
        } catch (e) {
          console.error(`事件处理器错误 (${eventType}):`, e)
        }
      })
    }
  }

  // 关闭连接
  close() {
    if (this.ws) {
      this.ws.close()
      this.ws = null
      this.isConnected = false
    }
  }

  // 检查连接状态
  get connected() {
    return this.ws && this.ws.readyState === WebSocket.OPEN
  }
}

// 创建 WebSocket 连接的便捷函数
export const createConversationWebSocket = (conversationId) => {
  return new ConversationWebSocket(conversationId)
}
