import axios from 'axios'
import { Message } from 'element-ui'
import store from '@/store'
import { getToken } from '@/utils/auth'
import request from '@/utils/request'

// 业务用户ID缓存（避免重复请求）
let businessUserIdCache = null
let businessUserIdPromise = null

/**
 * 获取当前用户的业务ID（user.id）
 * 从若依后端获取：sys_user.user_id → user.id
 */
async function getBusinessUserId() {
  // 如果已缓存，直接返回
  if (businessUserIdCache !== null) {
    console.log('[Smart API] 使用缓存的业务用户ID:', businessUserIdCache)
    return businessUserIdCache
  }
  
  // 如果正在请求中，返回同一个 Promise（避免并发请求）
  if (businessUserIdPromise !== null) {
    console.log('[Smart API] 等待业务用户ID请求完成...')
    return businessUserIdPromise
  }
  
  // 发起请求
  console.log('[Smart API] 开始获取业务用户ID...')
  businessUserIdPromise = request({
    url: '/system/businessUser/currentId',
    method: 'get'
  }).then(response => {
    console.log('[Smart API] 获取业务用户ID成功:', response)
    businessUserIdCache = response.data
    businessUserIdPromise = null
    return businessUserIdCache
  }).catch(error => {
    console.error('[Smart API] 获取业务用户ID失败:', error)
    businessUserIdPromise = null
    // 返回默认值
    console.warn('[Smart API] 使用默认业务用户ID: 20001')
    return '20001'
  })
  
  return businessUserIdPromise
}

/**
 * 清除业务用户ID缓存（用户登出时调用）
 */
export function clearBusinessUserIdCache() {
  businessUserIdCache = null
  businessUserIdPromise = null
}

// 智能功能模块专用的请求工具（调用8083端口后端）
// 开发环境使用代理，生产环境直接指向后端
const smartService = axios.create({
  baseURL: process.env.NODE_ENV === 'production' ? 'http://localhost:8083' : '/smart-api',
  timeout: 30000,
  withCredentials: true, // 重要：携带 cookie，用于 session 认证
  headers: {
    'Content-Type': 'application/json;charset=utf-8'
  }
})

// 请求拦截器
smartService.interceptors.request.use(
  async config => {
    // 添加若依的 token（用于后续扩展）
    const token = getToken()
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
    }
    
    // 关键：添加 userId header（8083 后端通过此 header 识别用户）
    // 从若依后端获取业务用户ID（user.id）
    try {
      const businessUserId = await getBusinessUserId()
      // 重要：直接设置到 config.headers 对象上，而不是嵌套对象
      if (!config.headers) {
        config.headers = {}
      }
      config.headers['userId'] = String(businessUserId)
      config.headers['userid'] = String(businessUserId) // 添加小写版本以防大小写问题
      
      console.log('[Smart API] 请求配置:', {
        baseURL: config.baseURL,
        url: config.url,
        fullURL: config.baseURL + config.url,
        method: config.method,
        businessUserId: businessUserId,
        'headers.userId': config.headers['userId'],
        'headers.userid': config.headers['userid'],
        hasToken: !!token
      })
    } catch (error) {
      console.error('[Smart API] 获取业务用户ID失败，使用默认值:', error)
      if (!config.headers) {
        config.headers = {}
      }
      config.headers['userId'] = '37'
      config.headers['userid'] = '37'
    }
    
    return config
  },
  error => {
    console.error('智能功能请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
smartService.interceptors.response.use(
  response => {
    const res = response.data
    
    // 如果响应有 code 字段且不是 200，显示错误
    if (res.code !== undefined && res.code !== 200) {
      Message({
        message: res.message || res.msg || '请求失败',
        type: 'error',
        duration: 5000
      })
      return Promise.reject(new Error(res.message || res.msg || '请求失败'))
    }
    
    // 返回完整的响应数据（包含 code, data, message 等）
    return res
  },
  error => {
    console.error('智能功能响应错误:', error)
    let message = '系统错误'
    
    if (error.response) {
      const status = error.response.status
      const data = error.response.data
      
      if (status === 401) {
        message = '未授权，请重新登录'
      } else if (status === 403) {
        message = '拒绝访问'
      } else if (status === 404) {
        message = '请求的资源不存在'
      } else if (status === 500) {
        message = data.message || data.msg || '服务器内部错误'
      } else {
        message = data.message || data.msg || '请求失败'
      }
    } else if (error.message) {
      if (error.message.includes('timeout')) {
        message = '请求超时'
      } else if (error.message.includes('Network Error')) {
        message = '网络错误，请检查网络连接'
      } else {
        message = error.message
      }
    }
    
    Message({
      message: message,
      type: 'error',
      duration: 5000
    })
    
    return Promise.reject(error)
  }
)

// AI Agent 服务专用（调用8000端口）
const aiService = axios.create({
  baseURL: process.env.NODE_ENV === 'production' ? 'http://localhost:8000' : '/ai-api',
  timeout: 60000, // AI 请求可能较慢，设置更长的超时时间
  headers: {
    'Content-Type': 'application/json;charset=utf-8'
  }
})

// AI 服务请求拦截器
aiService.interceptors.request.use(
  config => {
    return config
  },
  error => {
    console.error('AI服务请求错误:', error)
    return Promise.reject(error)
  }
)

// AI 服务响应拦截器
aiService.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    console.error('AI服务响应错误:', error)
    
    let message = 'AI服务错误'
    if (error.response) {
      message = error.response.data.message || error.response.data.detail || 'AI服务请求失败'
    } else if (error.message) {
      if (error.message.includes('timeout')) {
        message = 'AI服务请求超时'
      } else if (error.message.includes('Network Error')) {
        message = 'AI服务连接失败，请确保服务已启动'
      } else {
        message = error.message
      }
    }
    
    Message({
      message: message,
      type: 'error',
      duration: 5000
    })
    
    return Promise.reject(error)
  }
)

export { smartService, aiService }
export default smartService
