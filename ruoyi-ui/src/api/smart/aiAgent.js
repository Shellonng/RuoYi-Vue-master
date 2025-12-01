import { aiService } from '@/utils/smartRequest'

/**
 * AI Agent 智能组卷 API
 * 后端端口: 8001 (Python FastAPI)
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

// 发布组卷结果为正式作业/考试
export function publishAssembleResult(sessionId, data) {
  console.log('[API] 发布组卷结果, sessionId:', sessionId, 'data:', data)
  return aiService({
    url: `/sessions/${sessionId}/publish`,
    method: 'post',
    data: data
  })
}

// 创建 WebSocket 连接（用于实时组卷进度）
export function createWebSocket(onMessage, onError, onClose) {
  const wsUrl = 'ws://localhost:8001/ws/assemble'
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

// ========== 对话式智能组卷 API ==========

// 创建对话会话
export function createConversation(courseId) {
  return aiService({
    url: '/workflow/conversations',
    method: 'post',
    data: { course_id: courseId }
  })
}

// 发送对话消息
export function sendMessage(conversationId, message) {
  return aiService({
    url: `/workflow/conversations/${conversationId}/messages`,
    method: 'post',
    data: { message: message },
    timeout: 60000
  })
}

// 获取对话状态
export function getConversationStatus(conversationId) {
  return aiService({
    url: `/workflow/conversations/${conversationId}/status`,
    method: 'get'
  })
}

// 取消对话
export function cancelConversation(conversationId) {
  return aiService({
    url: `/workflow/conversations/${conversationId}/cancel`,
    method: 'post'
  })
}

// 重置对话
export function resetConversation(conversationId) {
  return aiService({
    url: `/workflow/conversations/${conversationId}/reset`,
    method: 'post'
  })
}

// ========== WebSocket 流式对话 ==========

/**
 * 对话 WebSocket 封装类
 * 用于实时流式输出对话内容
 */
export class ConversationWebSocket {
  constructor(conversationId) {
    this.conversationId = conversationId
    this.ws = null
    this.eventHandlers = {}
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 3
  }

  /**
   * 连接 WebSocket
   */
  connect() {
    return new Promise((resolve, reject) => {
      const wsUrl = `ws://localhost:8001/ws/workflow/conversations/${this.conversationId}/messages`
      
      try {
        this.ws = new WebSocket(wsUrl)
        
        this.ws.onopen = () => {
          console.log(`[WebSocket] 对话 ${this.conversationId} 已连接`)
          this.reconnectAttempts = 0
          this.emit('ready', { conversationId: this.conversationId })
          resolve()
        }
        
        this.ws.onmessage = (event) => {
          try {
            console.log('[WebSocket] 收到原始消息:', event.data)
            const data = JSON.parse(event.data)
            this.handleMessage(data)
          } catch (error) {
            console.error('[WebSocket] 消息解析失败:', error, event.data)
            // 如果是纯文本，当作 chunk 处理
            if (typeof event.data === 'string') {
              this.handleMessage({ chunk: event.data })
            }
          }
        }
        
        this.ws.onerror = (error) => {
          console.error('[WebSocket] 连接错误:', error)
          this.emit('error', error)
          reject(error)
        }
        
        this.ws.onclose = (event) => {
          console.log('[WebSocket] 连接关闭:', event.code, event.reason)
          this.emit('close', event)
          
          // 尝试重连（非正常关闭）
          if (event.code !== 1000 && this.reconnectAttempts < this.maxReconnectAttempts) {
            this.reconnectAttempts++
            console.log(`[WebSocket] 尝试重连 (${this.reconnectAttempts}/${this.maxReconnectAttempts})...`)
            setTimeout(() => this.connect(), 2000)
          }
        }
      } catch (error) {
        console.error('[WebSocket] 创建失败:', error)
        reject(error)
      }
    })
  }

  /**
   * 处理 WebSocket 消息
   * 完全按照 SmartCourseThird 的格式：{"event": "...", "data": {...}, "message": "..."}
   */
  handleMessage(data) {
    console.log('[WebSocket] 原始消息:', data)
    
    // SmartCourseThird 格式：{ event: 'chunk', data: {...}, message: '...' }
    const eventType = data.event
    const payload = data.data || data
    const message = data.message
    
    console.log('[WebSocket] 事件类型:', eventType, '| 数据:', payload, '| 消息:', message)
    
    switch (eventType) {
      case 'thinking':
        this.emit('thinking', { message })
        break
      case 'chunk':
        // chunk 事件：按照 SmartCourseThird 的格式提取
        // 优先使用 data.chunk，其次 message
        this.emit('chunk', {
          chunk: (payload && payload.chunk) || message || '',
          content: (payload && payload.content) || message || '',
          data: payload
        })
        break
      case 'action':
        this.emit('action', {
          action: payload.action,
          message: message || payload.message
        })
        break
      case 'result':
        this.emit('result', payload)
        break
      case 'published':
        this.emit('published', payload)
        break
      case 'done':
        this.emit('done', payload)
        break
      case 'error':
        this.emit('error', { error: payload, message })
        break
      case 'aborted':
        this.emit('aborted', payload)
        break
      default:
        console.warn('[WebSocket] 未知事件类型:', eventType, data)
    }
  }

  /**
   * 发送消息
   */
  sendMessage(message) {
    if (!this.ws || this.ws.readyState !== WebSocket.OPEN) {
      console.error('[WebSocket] 未连接，无法发送消息')
      return false
    }
    
    try {
      this.ws.send(JSON.stringify({ message }))
      return true
    } catch (error) {
      console.error('[WebSocket] 发送消息失败:', error)
      return false
    }
  }

  /**
   * 注册事件监听器
   */
  on(eventType, handler) {
    if (!this.eventHandlers[eventType]) {
      this.eventHandlers[eventType] = []
    }
    this.eventHandlers[eventType].push(handler)
  }

  /**
   * 触发事件
   */
  emit(eventType, data) {
    const handlers = this.eventHandlers[eventType]
    if (handlers) {
      handlers.forEach(handler => {
        try {
          handler(data)
        } catch (error) {
          console.error(`[WebSocket] 事件处理器错误 (${eventType}):`, error)
        }
      })
    }
  }

  /**
   * 关闭连接
   */
  close() {
    if (this.ws) {
      this.ws.close(1000, 'Client closed')
      this.ws = null
    }
  }

  /**
   * 检查连接状态
   */
  isConnected() {
    return this.ws && this.ws.readyState === WebSocket.OPEN
  }
}
