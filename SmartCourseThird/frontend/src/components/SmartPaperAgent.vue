<template>
  <div class="smart-paper-agent">
    <el-card class="chat-container" shadow="never">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <div class="ai-avatar">
              <el-icon :size="24"><MagicStick /></el-icon>
            </div>
            <div class="header-info">
              <h3>AI 智能组卷助手</h3>
              <p>基于课程知识点，智能生成高质量试卷</p>
            </div>
          </div>
          <div class="header-right">
            <el-tag :type="aiHealthy ? 'success' : 'danger'" effect="light" class="status-tag">
              <el-icon><Connection /></el-icon>
              {{ aiHealthy ? 'AI 服务在线' : 'AI 服务离线' }}
            </el-tag>
            <el-button 
              v-if="conversationId" 
              type="warning" 
              size="small" 
              @click="resetConversation"
              :icon="RefreshLeft"
              round
            >
              重新开始
            </el-button>
            <el-button 
              type="default" 
              size="small" 
              @click="emit('close')"
              :icon="Close"
              circle
            />
          </div>
        </div>
      </template>

      <!-- 对话区域 -->
      <div class="chat-messages" ref="messagesContainer">
        <!-- 欢迎消息 -->
        <div v-if="messages.length === 0 && !isStarting" class="welcome-section">
          <div class="welcome-icon">
            <el-icon :size="80" color="#667eea"><ChatDotRound /></el-icon>
          </div>
          <h2>欢迎使用 AI 智能组卷</h2>
          <p class="welcome-desc">
            当前课程：<el-tag type="primary" size="large">{{ courseName }}</el-tag>
          </p>
          <div class="feature-cards">
            <div class="feature-card">
              <el-icon :size="28" color="#67C23A"><TrendCharts /></el-icon>
              <span>智能分析知识点</span>
            </div>
            <div class="feature-card">
              <el-icon :size="28" color="#E6A23C"><Operation /></el-icon>
              <span>自动匹配难度</span>
            </div>
            <div class="feature-card">
              <el-icon :size="28" color="#F56C6C"><Document /></el-icon>
              <span>一键生成试卷</span>
            </div>
          </div>
          <el-button 
            type="primary" 
            size="large"
            @click="startConversation"
            :loading="isStarting"
            class="start-btn"
            round
          >
            <el-icon style="margin-right: 8px"><ChatDotRound /></el-icon>
            开始 AI 智能对话组卷
          </el-button>
        </div>

        <!-- 消息列表 -->
        <div v-else class="messages-list">
          <div
            v-for="(msg, index) in messages"
            :key="index"
            :class="['message-item', msg.role === 'user' ? 'user-message' : 'assistant-message']"
          >
            <div class="message-avatar">
              <el-avatar :size="42" :class="msg.role === 'user' ? 'user-avatar' : 'ai-avatar'">
                <template v-if="msg.role === 'user'">
                  <el-icon><User /></el-icon>
                </template>
                <template v-else>
                  <el-icon><MagicStick /></el-icon>
                </template>
              </el-avatar>
            </div>
            <div class="message-content">
              <div class="message-bubble">
                <div class="message-text" v-html="formatMessage(msg.content)"></div>
                
                <!-- 组卷结果展示 -->
                <div v-if="msg.result" class="result-card">
                  <div class="result-header">
                    <el-icon color="#67C23A" :size="20"><DocumentChecked /></el-icon>
                    <span>组卷结果预览</span>
                  </div>
                  
                  <el-row :gutter="15" class="result-stats">
                    <el-col :span="8">
                      <div class="stat-box">
                        <div class="stat-value">{{ msg.result.total_questions }}</div>
                        <div class="stat-label">题目总数</div>
                      </div>
                    </el-col>
                    <el-col :span="8">
                      <div class="stat-box">
                        <div class="stat-value">{{ msg.result.total_score }}</div>
                        <div class="stat-label">总分</div>
                      </div>
                    </el-col>
                    <el-col :span="8">
                      <div class="stat-box">
                        <el-rate 
                          :model-value="msg.result.average_difficulty" 
                          disabled 
                          size="small"
                        />
                        <div class="stat-label">平均难度</div>
                      </div>
                    </el-col>
                  </el-row>

                  <el-table 
                    :data="msg.result.questions" 
                    border 
                    size="small" 
                    max-height="280"
                    class="result-table"
                  >
                    <el-table-column prop="id" label="ID" width="70" align="center" />
                    <el-table-column prop="title" label="题目" show-overflow-tooltip />
                    <el-table-column prop="type" label="题型" width="90" align="center">
                      <template #default="{ row }">
                        <el-tag size="small" :type="getQuestionTypeTag(row.type)">
                          {{ getQuestionTypeLabel(row.type) }}
                        </el-tag>
                      </template>
                    </el-table-column>
                    <el-table-column prop="difficulty" label="难度" width="90" align="center">
                      <template #default="{ row }">
                        <el-rate :model-value="row.difficulty" disabled size="small" :max="5" />
                      </template>
                    </el-table-column>
                    <el-table-column prop="score" label="分值" width="70" align="center">
                      <template #default="{ row }">
                        <span class="score-text">{{ row.score }}</span>
                      </template>
                    </el-table-column>
                  </el-table>

                  <div class="result-actions">
                    <el-button 
                      type="primary" 
                      @click="showPublishDialog(msg)"
                      :icon="Promotion"
                    >
                      发布此试卷
                    </el-button>
                  </div>
                </div>

                <!-- 状态标签 -->
                <el-tag 
                  v-if="msg.stage && msg.role !== 'user'" 
                  size="small" 
                  :type="getStageType(msg.stage)"
                  class="stage-tag"
                >
                  {{ getStageText(msg.stage) }}
                </el-tag>
              </div>
            </div>
          </div>

          <!-- 流式输出和思考中动画 -->
          <div v-if="isThinking" class="message-item assistant-message">
            <div class="message-avatar">
              <el-avatar :size="42" class="ai-avatar thinking">
                <el-icon><MagicStick /></el-icon>
              </el-avatar>
            </div>
            <div class="message-content">
              <div class="message-bubble thinking-bubble">
                <!-- 流式输出内容 -->
                <div v-if="streamingContent" class="streaming-content" v-html="formatMessage(streamingContent)"></div>
                
                <!-- 思考状态提示 -->
                <div class="thinking-status">
                  <div class="typing-indicator">
                    <span></span>
                    <span></span>
                    <span></span>
                  </div>
                  <span class="thinking-text">{{ thinkingMessage || 'AI 正在思考...' }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 输入区域 -->
      <div v-if="conversationId" class="chat-input">
        <div class="input-container">
          <el-input
            v-model="userInput"
            placeholder="描述您的组卷需求，例如：我需要一份关于神经网络的测试，包含3道简答题，难度3..."
            @keyup.enter="sendMessage"
            :disabled="isThinking"
            clearable
            size="large"
            class="message-input"
          >
            <template #prefix>
              <el-icon><EditPen /></el-icon>
            </template>
          </el-input>
          <el-button
            type="primary"
            @click="sendMessage"
            :loading="isThinking"
            :disabled="!userInput.trim()"
            size="large"
            class="send-btn"
            :icon="Promotion"
          >
            发送
          </el-button>
        </div>

        <!-- 快捷提示 -->
        <div class="quick-actions">
          <span class="quick-label">快捷指令：</span>
          <el-tag 
            v-for="suggestion in quickSuggestions" 
            :key="suggestion"
            @click="quickSend(suggestion)"
            class="quick-tag"
            effect="plain"
            round
          >
            {{ suggestion }}
          </el-tag>
        </div>
      </div>
    </el-card>

    <!-- 发布对话框 -->
    <el-dialog 
      v-model="publishDialogVisible" 
      title="发布作业/考试" 
      width="600px" 
      class="publish-dialog"
      append-to-body
    >
      <el-form :model="publishForm" label-width="80px" class="publish-form">
        <el-form-item label="标题" required>
          <el-input v-model="publishForm.title" placeholder="请输入作业/考试标题" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="类型" required>
          <el-radio-group v-model="publishForm.assignment_type">
            <el-radio :label="'exam'">考试</el-radio>
            <el-radio :label="'homework'">作业</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-show="publishForm.assignment_type === 'exam'" label="时限(分钟)" required>
          <el-input-number v-model="publishForm.time_limit" :min="1" :max="600" style="width: 100%" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input 
            v-model="publishForm.description" 
            type="textarea" 
            :rows="3"
            placeholder="请输入描述（可选）"
          />
        </el-form-item>
        <el-form-item label="开始时间" required>
          <el-date-picker
            v-model="publishForm.start_time"
            type="datetime"
            placeholder="选择开始时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束时间" required>
          <el-date-picker
            v-model="publishForm.end_time"
            type="datetime"
            placeholder="选择结束时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="publishDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="publishPaper" :loading="isPublishing">
          确认发布
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, nextTick, onMounted, onUnmounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  MagicStick, Connection, RefreshLeft, Close, ChatDotRound, TrendCharts, 
  Operation, Document, User, DocumentChecked, Promotion, EditPen, Notification
} from '@element-plus/icons-vue'
import {
  checkAIHealth,
  createConversation,
  sendMessage as sendMessageAPI,
  publishAssembleResult,
  createConversationWebSocket
} from '../api/aiAgent'
import { getSession } from '../utils/session'

// Props
const props = defineProps({
  courseId: {
    type: [Number, String],
    required: true
  },
  courseName: {
    type: String,
    default: '当前课程'
  }
})

// Emits
const emit = defineEmits(['close', 'published'])

// State
const aiHealthy = ref(false)
const conversationId = ref(null)
const userInput = ref('')
const messages = ref([])
const isThinking = ref(false)
const isStarting = ref(false)
const messagesContainer = ref(null)
const currentSessionId = ref(null)

// WebSocket 相关状态
const wsConnection = ref(null)
const streamingContent = ref('') // 当前正在流式输出的内容（解析后的 reply）
const thinkingMessage = ref('') // 当前思考状态提示
const rawStreamingBuffer = ref('') // 原始 LLM 响应缓冲区

// 从部分 JSON 中提取 reply 字段的值
const extractReplyFromPartialJson = (partialJson) => {
  if (!partialJson) return ''
  
  // 尝试匹配 "reply": "..." 或 "reply":"..."
  // 支持未闭合的字符串
  const replyMatch = partialJson.match(/"reply"\s*:\s*"([^"]*(?:")?)/s)
  if (replyMatch) {
    let reply = replyMatch[1]
    // 移除末尾未闭合的引号
    if (reply.endsWith('"')) {
      reply = reply.slice(0, -1)
    }
    // 处理转义字符
    try {
      reply = reply.replace(/\\n/g, '\n').replace(/\\"/g, '"').replace(/\\\\/g, '\\')
    } catch (e) {
      // 忽略解析错误
    }
    return reply
  }
  return ''
}

const publishDialogVisible = ref(false)
const isPublishing = ref(false)
const publishForm = reactive({
  title: 'AI 智能组卷',
  assignment_type: 'exam',
  start_time: '',
  end_time: '',
  time_limit: 60,
  description: 'AI 智能助手自动生成'
})

const quickSuggestions = ref([
  '我需要关于神经网络的测试',
  '难度设为3',
  '2道简答题',
  '开始组卷',
  '发布试卷'
])

// Methods
const checkHealth = async () => {
  try {
    const res = await checkAIHealth()
    aiHealthy.value = res.status === 'ok'
  } catch (error) {
    aiHealthy.value = false
  }
}

const startConversation = async () => {
  isStarting.value = true
  try {
    // 首先通过 HTTP 创建对话获取 conversation_id
    const res = await createConversation(props.courseId)
    conversationId.value = res.conversation_id
    
    messages.value.push({
      role: 'assistant',
      content: res.message,
      stage: res.stage,
      spec: res.spec,
      missing_fields: res.missing_fields
    })
    
    // 创建 WebSocket 连接
    await setupWebSocket()
    
    ElMessage.success('对话已开始，请描述您的组卷需求')
    scrollToBottom()
  } catch (error) {
    console.error('启动对话失败:', error)
    ElMessage.error('启动对话失败，请检查 AI 服务是否正常运行')
  } finally {
    isStarting.value = false
  }
}

// 设置 WebSocket 连接
const setupWebSocket = async () => {
  console.log('开始设置 WebSocket 连接, conversationId:', conversationId.value)
  
  if (wsConnection.value) {
    console.log('关闭旧的 WebSocket 连接')
    wsConnection.value.close()
  }
  
  wsConnection.value = createConversationWebSocket(conversationId.value)
  
  // 注册事件处理器
  wsConnection.value
    .on('ready', (data) => {
      console.log('WebSocket 连接就绪:', data)
    })
    .on('thinking', (data) => {
      console.log('收到 thinking 事件:', data)
      thinkingMessage.value = data.message || 'AI 正在思考...'
      streamingContent.value = '' // 清空流式内容
      scrollToBottom()
    })
    .on('chunk', (data) => {
      console.log('收到 chunk 事件:', data)
      // LLM 返回的是 JSON 格式，尝试实时提取 reply 字段进行显示
      const chunk = data.data || ''
      rawStreamingBuffer.value += chunk
      
      // 尝试从累积的内容中提取 reply 字段的值
      const extractedReply = extractReplyFromPartialJson(rawStreamingBuffer.value)
      if (extractedReply) {
        streamingContent.value = extractedReply
        console.log('提取的 reply:', extractedReply)
      }
      scrollToBottom()
    })
    .on('action', (data) => {
      console.log('收到 action 事件:', data)
      thinkingMessage.value = data.message || '正在执行操作...'
      rawStreamingBuffer.value = '' // 清空缓冲区
      streamingContent.value = ''
      scrollToBottom()
    })
    .on('result', (data) => {
      console.log('收到 result 事件:', data)
      // 收到组卷结果
      thinkingMessage.value = data.message || '组卷完成！'
      // 结果会在 done 事件中一起处理
      scrollToBottom()
    })
    .on('published', (data) => {
      console.log('收到 published 事件:', data)
      // 发布成功
      if (data.data && data.data.assignment_id) {
        ElMessage.success(`试卷已成功发布！作业 ID: ${data.data.assignment_id}`)
        emit('published', data.data.assignment_id)
      }
    })
    .on('done', (data) => {
      console.log('收到 done 事件:', data)
      // 处理完成，添加完整的 AI 回复消息
      isThinking.value = false
      thinkingMessage.value = ''
      streamingContent.value = ''
      rawStreamingBuffer.value = '' // 清空缓冲区
      
      if (data.data) {
        const replyData = data.data
        messages.value.push({
          role: 'assistant',
          content: replyData.message || '',
          stage: replyData.stage,
          action: replyData.action,
          spec: replyData.spec,
          missing_fields: replyData.missing_fields,
          result: replyData.result,
          session_id: replyData.session_id,
          assignment_id: replyData.assignment_id
        })
        
        if (replyData.session_id) {
          currentSessionId.value = replyData.session_id
        }
        
        if (replyData.assignment_id) {
          quickSuggestions.value = ['重新开始', '查看作业']
        }
      }
      scrollToBottom()
    })
    .on('error', (data) => {
      isThinking.value = false
      thinkingMessage.value = ''
      streamingContent.value = ''
      rawStreamingBuffer.value = ''
      console.error('WebSocket 错误:', data)
      ElMessage.error(data.message || '处理失败')
    })
    .on('aborted', (data) => {
      isThinking.value = false
      thinkingMessage.value = ''
      streamingContent.value = ''
      rawStreamingBuffer.value = ''
      ElMessage.warning(data.message || '对话已中断')
    })
    .on('close', () => {
      console.log('WebSocket 连接已关闭')
    })
  
  // 连接 WebSocket
  console.log('正在连接 WebSocket...')
  try {
    await wsConnection.value.connect()
    console.log('WebSocket 连接成功!')
  } catch (err) {
    console.error('WebSocket 连接失败:', err)
    throw err
  }
}

const sendMessage = async () => {
  if (!userInput.value.trim()) return
  
  const message = userInput.value.trim()
  console.log('发送消息:', message, 'WebSocket状态:', wsConnection.value?.connected)
  
  messages.value.push({
    role: 'user',
    content: message
  })
  
  userInput.value = ''
  isThinking.value = true
  thinkingMessage.value = 'AI 正在思考...'
  streamingContent.value = ''
  rawStreamingBuffer.value = ''
  scrollToBottom()
  
  try {
    // 检查 WebSocket 连接状态
    console.log('检查 WebSocket 状态:', wsConnection.value?.connected)
    if (!wsConnection.value || !wsConnection.value.connected) {
      console.log('WebSocket 未连接，尝试重新连接...')
      // 如果 WebSocket 未连接，尝试重新连接
      await setupWebSocket()
    }
    
    // 通过 WebSocket 发送消息
    console.log('通过 WebSocket 发送消息...')
    wsConnection.value.sendMessage(message)
    console.log('消息已发送')
    
  } catch (error) {
    console.error('发送消息失败:', error)
    isThinking.value = false
    thinkingMessage.value = ''
    streamingContent.value = ''
    rawStreamingBuffer.value = ''
    ElMessage.error('发送消息失败，请检查网络连接')
  }
}

const quickSend = (text) => {
  userInput.value = text
  sendMessage()
}

const showPublishDialog = (msg) => {
  if (!msg.session_id) {
    ElMessage.warning('缺少会话信息，无法发布')
    return
  }
  currentSessionId.value = msg.session_id
  publishForm.title = 'AI 智能组卷 - ' + new Date().toLocaleDateString()
  publishDialogVisible.value = true
}

const publishPaper = async () => {
  if (!publishForm.title.trim()) {
    ElMessage.warning('请输入标题')
    return
  }
  
  if (!publishForm.start_time || !publishForm.end_time) {
    ElMessage.warning('请选择开始时间和结束时间')
    return
  }
  
  if (publishForm.assignment_type === 'exam' && (!publishForm.time_limit || publishForm.time_limit < 1)) {
    ElMessage.warning('考试类型请设置答题时限')
    return
  }
  
  if (!currentSessionId.value) {
    ElMessage.warning('缺少会话信息，无法发布')
    return
  }
  
  isPublishing.value = true

  try {
    const session = getSession()
    const publishData = {
      title: publishForm.title,
      publisher_user_id: session?.user?.id || 20001,
      assignment_type: publishForm.assignment_type,
      description: publishForm.description || 'AI 智能助手自动生成',
      mode: 'question',
      start_time: publishForm.start_time,
      end_time: publishForm.end_time
    }
    
    // 如果是考试类型，添加时间限制
    if (publishForm.assignment_type === 'exam' && publishForm.time_limit) {
      publishData.time_limit = publishForm.time_limit
    }
    
    const res = await publishAssembleResult(currentSessionId.value, publishData)
    
    publishDialogVisible.value = false
    ElMessage.success(`试卷发布成功！作业 ID: ${res.assignment_id}`)
    
    messages.value.push({
      role: 'assistant',
      content: `✅ 试卷已成功发布！\n\n📋 **作业 ID**: ${res.assignment_id}\n📝 **标题**: ${publishForm.title}\n📁 **类型**: ${publishForm.assignment_type === 'homework' ? '作业' : '考试'}`,
      stage: 'published',
      assignment_id: res.assignment_id
    })
    
    emit('published', res.assignment_id)
    scrollToBottom()
  } catch (error) {
    console.error('发布失败:', error)
    ElMessage.error('发布失败，请重试')
  } finally {
    isPublishing.value = false
  }
}

const resetConversation = async () => {
  try {
    await ElMessageBox.confirm('确定要重新开始对话吗？当前对话内容将丢失。', '提示', {
      type: 'warning'
    })
    
    // 关闭 WebSocket 连接
    if (wsConnection.value) {
      wsConnection.value.close()
      wsConnection.value = null
    }
    
    conversationId.value = null
    messages.value = []
    currentSessionId.value = null
    streamingContent.value = ''
    thinkingMessage.value = ''
    rawStreamingBuffer.value = ''
    quickSuggestions.value = [
      '我需要关于神经网络的测试',
      '难度设为3',
      '2道简答题',
      '开始组卷',
      '发布试卷'
    ]
    
    ElMessage.info('已重置，可以重新开始')
  } catch (error) {
    // 用户取消
  }
}

const formatMessage = (text) => {
  if (!text) return ''
  
  let formatted = text.replace(/\n/g, '<br>')
  
  // Markdown 加粗
  formatted = formatted.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
  
  // 高亮关键词
  const keywords = ['知识点', '难度', '题型', '总分', '组卷', '发布', '题目']
  keywords.forEach(keyword => {
    const regex = new RegExp(`(?<!<[^>]*)${keyword}(?![^<]*>)`, 'g')
    formatted = formatted.replace(regex, `<span class="keyword">${keyword}</span>`)
  })
  
  return formatted
}

const getStageText = (stage) => {
  const map = {
    collecting: '📝 收集信息中',
    assembled: '✅ 组卷完成',
    published: '🎉 已发布',
    completed: '🏁 对话结束'
  }
  return map[stage] || stage
}

const getStageType = (stage) => {
  const map = {
    collecting: 'info',
    assembled: 'success',
    published: 'success',
    completed: ''
  }
  return map[stage] || 'info'
}

const getQuestionTypeLabel = (type) => {
  const map = {
    'single': '单选题',
    'multiple': '多选题',
    'true_false': '判断题',
    'blank': '填空题',
    'short': '简答题',
    'code': '编程题'
  }
  return map[type] || type
}

const getQuestionTypeTag = (type) => {
  const map = {
    'single': 'success',
    'multiple': 'warning',
    'true_false': 'info',
    'blank': 'primary',
    'short': 'danger',
    'code': 'danger'
  }
  return map[type] || ''
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

onMounted(() => {
  checkHealth()
})

// 组件卸载时关闭 WebSocket 连接
onUnmounted(() => {
  if (wsConnection.value) {
    wsConnection.value.close()
    wsConnection.value = null
  }
})
</script>

<style scoped>
.smart-paper-agent {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.chat-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  border-radius: 16px;
  overflow: hidden;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8eb 100%);
}

.chat-container :deep(.el-card__header) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 15px 20px;
  border-bottom: none;
}

.chat-container :deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.ai-avatar {
  width: 48px;
  height: 48px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.header-info h3 {
  margin: 0;
  color: white;
  font-size: 18px;
  font-weight: 600;
}

.header-info p {
  margin: 4px 0 0 0;
  color: rgba(255, 255, 255, 0.8);
  font-size: 13px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.status-tag {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* Chat messages */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

/* Welcome section */
.welcome-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  text-align: center;
  padding: 40px;
}

.welcome-icon {
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.welcome-section h2 {
  margin: 20px 0 10px;
  color: #303133;
  font-size: 24px;
}

.welcome-desc {
  color: #606266;
  margin-bottom: 30px;
}

.feature-cards {
  display: flex;
  gap: 20px;
  margin-bottom: 30px;
}

.feature-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 20px 30px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transition: transform 0.3s;
}

.feature-card:hover {
  transform: translateY(-5px);
}

.start-btn {
  padding: 15px 40px;
  font-size: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

.start-btn:hover {
  background: linear-gradient(135deg, #764ba2 0%, #667eea 100%);
}

/* Messages list */
.messages-list {
  padding: 10px 0;
}

.message-item {
  display: flex;
  margin-bottom: 20px;
  animation: slideIn 0.3s ease-out;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.user-message {
  flex-direction: row-reverse;
}

.message-avatar {
  flex-shrink: 0;
  margin: 0 12px;
}

.user-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.ai-avatar {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
}

.ai-avatar.thinking {
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.6; }
}

.message-content {
  max-width: 75%;
}

.message-bubble {
  padding: 14px 18px;
  border-radius: 16px;
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  position: relative;
}

.user-message .message-bubble {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-bottom-right-radius: 4px;
}

.assistant-message .message-bubble {
  border-bottom-left-radius: 4px;
}

.message-text {
  line-height: 1.6;
  word-wrap: break-word;
}

.message-text :deep(.keyword) {
  color: #409EFF;
  font-weight: 500;
}

.user-message .message-text :deep(.keyword) {
  color: #fff;
}

/* Result card */
.result-card {
  margin-top: 15px;
  padding: 15px;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border-radius: 12px;
  border: 1px solid #bae6fd;
}

.result-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #0369a1;
  margin-bottom: 15px;
}

.result-stats {
  margin-bottom: 15px;
}

.stat-box {
  text-align: center;
  padding: 12px;
  background: white;
  border-radius: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #0369a1;
}

.stat-label {
  font-size: 12px;
  color: #64748b;
  margin-top: 4px;
}

.result-table {
  border-radius: 8px;
  overflow: hidden;
}

.score-text {
  font-weight: 600;
  color: #0369a1;
}

.result-actions {
  margin-top: 15px;
  text-align: right;
}

.stage-tag {
  margin-top: 10px;
}

/* Thinking animation */
.thinking-bubble {
  display: flex;
  flex-direction: column;
  gap: 10px;
  background: #f5f7fa;
  min-width: 200px;
}

/* 流式输出内容样式 */
.streaming-content {
  color: #303133;
  font-size: 14px;
  line-height: 1.6;
  padding-bottom: 8px;
  border-bottom: 1px dashed #dcdfe6;
  margin-bottom: 8px;
}

/* 思考状态提示样式 */
.thinking-status {
  display: flex;
  align-items: center;
  gap: 10px;
}

.typing-indicator {
  display: flex;
  gap: 4px;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #667eea;
  animation: bounce 1.4s infinite;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes bounce {
  0%, 60%, 100% {
    transform: translateY(0);
  }
  30% {
    transform: translateY(-8px);
  }
}

.thinking-text {
  color: #909399;
  font-size: 13px;
}

/* Input area */
.chat-input {
  padding: 15px 20px;
  background: white;
  border-top: 1px solid #e4e7ed;
}

.input-container {
  display: flex;
  gap: 10px;
}

.message-input {
  flex: 1;
}

.message-input :deep(.el-input__wrapper) {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.send-btn {
  border-radius: 12px;
  padding: 0 25px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

.send-btn:hover {
  background: linear-gradient(135deg, #764ba2 0%, #667eea 100%);
}

.quick-actions {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
}

.quick-label {
  font-size: 12px;
  color: #909399;
}

.quick-tag {
  cursor: pointer;
  transition: all 0.3s;
}

.quick-tag:hover {
  background: #667eea;
  color: white;
  border-color: #667eea;
}

/* Publish dialog */
.publish-form :deep(.el-radio-button__inner) {
  display: flex;
  align-items: center;
  gap: 6px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>
