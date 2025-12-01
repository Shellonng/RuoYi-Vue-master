<template>
  <div class="smart-paper-page">
    <!-- 顶部导航 -->
    <div class="page-header">
      <el-button @click="goBack" :icon="ArrowLeft" class="back-btn">
        返回题库管理
      </el-button>
      <div class="header-title">
        <el-icon :size="28" color="#667eea"><MagicStick /></el-icon>
        <h2>AI 智能组卷助手</h2>
      </div>
      <div class="header-actions">
        <el-tag :type="aiHealthy ? 'success' : 'danger'" effect="light" class="status-tag">
          <el-icon><Connection /></el-icon>
          {{ aiHealthy ? 'AI 服务在线' : 'AI 服务离线' }}
        </el-tag>
        <el-button type="warning" @click="resetConversation" :icon="RefreshLeft">
          重新开始
        </el-button>
      </div>
    </div>

    <!-- 聊天区域 -->
    <div class="chat-container">
      <div class="messages-area" ref="messagesContainer">
        <div v-for="(msg, index) in messages" :key="index" :class="['message', msg.role]">
          <div class="message-avatar">
            <el-icon v-if="msg.role === 'assistant'" :size="20"><MagicStick /></el-icon>
            <el-icon v-else :size="20"><User /></el-icon>
          </div>
          <div class="message-content">
            <div class="message-bubble" v-html="formatMessage(msg.content)"></div>
            <div v-if="msg.streamLog" class="stream-log">
              <el-collapse>
                <el-collapse-item title="finished working" name="stream">
                  <div class="stream-log-content" v-html="formatMessage(msg.streamLog)"></div>
                </el-collapse-item>
              </el-collapse>
            </div>
            <div v-if="msg.role === 'assistant' && msg.result" class="result-preview">
              <el-card class="result-card" shadow="hover">
                <template #header>
                  <div class="result-header">
                    <el-icon><Document /></el-icon>
                    <span>组卷结果预览</span>
                  </div>
                </template>
                <div class="result-stats">
                  <div class="stat-item">
                    <span class="label">题目总数</span>
                    <span class="value">{{ getTotalQuestions(msg.result) }}</span>
                  </div>
                  <div class="stat-item">
                    <span class="label">总分值</span>
                    <span class="value">{{ msg.result.total_score || 100 }}分</span>
                  </div>
                  <div class="stat-item">
                    <span class="label">平均难度</span>
                    <span class="value">{{ msg.result.average_difficulty?.toFixed(1) || msg.result.actual_difficulty?.toFixed(1) || '--' }}</span>
                  </div>
                </div>
                <el-collapse v-if="getTotalQuestions(msg.result) > 0">
                  <el-collapse-item v-for="(questions, type) in getQuestionsByType(msg.result)" :key="type" :title="`${getQuestionTypeText(type)} (${questions.length}题)`">
                    <div class="question-list">
                      <div v-for="(q, i) in questions" :key="q.id" class="question-item">
                        <span class="q-index">{{ i + 1 }}.</span>
                        <span class="q-title">{{ q.title }}</span>
                        <el-tag size="small" type="info">{{ q.score }}分</el-tag>
                      </div>
                    </div>
                  </el-collapse-item>
                </el-collapse>
                <div class="result-actions" v-if="msg.sessionId">
                  <el-button type="primary" @click="showPublishDialog(msg.sessionId)" :icon="Promotion">
                    发布为作业/考试
                  </el-button>
                </div>
              </el-card>
            </div>
          </div>
        </div>
        
        <!-- 正在输入指示器 -->
        <div v-if="isTyping" class="message assistant">
          <div class="message-avatar">
            <el-icon :size="20"><MagicStick /></el-icon>
          </div>
          <div class="message-content">
            <div 
              v-if="streamingContent" 
              class="message-bubble" 
              v-html="formatMessage(streamingContent)"
            ></div>
            <div v-else class="message-bubble thinking-bubble">
              <div class="typing-indicator">
                <span></span><span></span><span></span>
              </div>
              <div class="thinking-text">{{ thinkingMessage || 'AI 正在思考...' }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="input-area">
        <el-button 
          class="quick-config-btn" 
          type="success" 
          :icon="Setting" 
          @click="showConfigDialog"
          circle
          title="快速配置组卷参数"
        />
        <el-input
          v-model="inputMessage"
          type="textarea"
          :rows="2"
          :placeholder="inputPlaceholder"
          @keydown.enter.ctrl="sendMessage"
          :disabled="isTyping || !conversationId"
          class="message-input"
        />
        <el-button 
          type="primary" 
          :icon="Promotion" 
          @click="sendMessage" 
          :loading="isTyping"
          :disabled="!inputMessage.trim() || !conversationId"
          class="send-btn"
        >
          发送
        </el-button>
        <el-button 
          v-if="isTyping"
          type="danger" 
          :icon="CircleClose" 
          @click="abortMessage"
          class="abort-btn"
        >
          中断
        </el-button>
      </div>
    </div>

    <!-- 快速配置组卷参数弹框 -->
    <el-dialog v-model="configDialogVisible" title="快速配置组卷参数" width="650px" class="config-dialog">
      <el-form :model="configForm" label-width="100px" class="config-form">
        <el-form-item label="知识点">
          <div class="knowledge-points-selector">
            <el-select 
              v-model="selectedKp" 
              placeholder="选择知识点添加" 
              filterable
              clearable
              @change="addKnowledgePoint"
              style="width: 300px"
            >
              <el-option 
                v-for="kp in availableKnowledgePoints" 
                :key="kp.id" 
                :label="kp.title" 
                :value="kp"
              />
            </el-select>
            <div class="selected-kps">
              <el-tag
                v-for="kp in configForm.knowledgePoints"
                :key="kp.id"
                closable
                @close="removeKnowledgePoint(kp)"
                class="kp-tag"
                type="success"
              >
                {{ kp.title }}
              </el-tag>
              <span v-if="configForm.knowledgePoints.length === 0" class="empty-tip">
                请选择至少一个知识点
              </span>
            </div>
          </div>
        </el-form-item>
        
        <el-form-item label="目标难度">
          <el-slider 
            v-model="configForm.difficulty" 
            :min="1" 
            :max="5" 
            :step="0.5"
            show-stops
            :marks="difficultyMarks"
            style="width: 400px"
          />
        </el-form-item>
        
        <el-form-item label="题目数量">
          <el-row :gutter="15" class="question-counts">
            <el-col :span="8">
              <div class="count-item">
                <span class="label">单选题</span>
                <el-input-number v-model="configForm.singleCount" :min="0" :max="50" size="small" />
              </div>
            </el-col>
            <el-col :span="8">
              <div class="count-item">
                <span class="label">多选题</span>
                <el-input-number v-model="configForm.multipleCount" :min="0" :max="50" size="small" />
              </div>
            </el-col>
            <el-col :span="8">
              <div class="count-item">
                <span class="label">填空题</span>
                <el-input-number v-model="configForm.blankCount" :min="0" :max="50" size="small" />
              </div>
            </el-col>
            <el-col :span="8">
              <div class="count-item">
                <span class="label">简答题</span>
                <el-input-number v-model="configForm.shortCount" :min="0" :max="50" size="small" />
              </div>
            </el-col>
            <el-col :span="8">
              <div class="count-item">
                <span class="label">编程题</span>
                <el-input-number v-model="configForm.codeCount" :min="0" :max="50" size="small" />
              </div>
            </el-col>
          </el-row>
        </el-form-item>
        
        <el-form-item label="总分">
          <el-input-number v-model="configForm.totalScore" :min="10" :max="500" :step="10" />
          <span class="form-tip">分（可选，默认100分）</span>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="configDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitConfig" :disabled="!isConfigValid">
          生成组卷指令
        </el-button>
      </template>
    </el-dialog>

    <!-- 发布作业对话框 -->
    <el-dialog v-model="publishDialogVisible" title="发布作业/考试" width="550px" class="publish-dialog">
      <el-form :model="publishForm" label-width="100px">
        <el-form-item label="标题" required>
          <el-input v-model="publishForm.title" placeholder="请输入作业/考试标题" />
        </el-form-item>
        <el-form-item label="类型">
          <el-radio-group v-model="publishForm.type">
            <el-radio value="exam">考试</el-radio>
            <el-radio value="homework">作业</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item 
          v-if="publishForm.type === 'exam'" 
          label="时限(分钟)" 
          required
        >
          <el-input-number 
            v-model="publishForm.timeLimit" 
            :min="1" 
            :max="600" 
            style="width: 100%" 
          />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="publishForm.description" type="textarea" :rows="3" placeholder="请输入描述（可选）" />
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker 
            v-model="publishForm.startTime" 
            type="datetime" 
            placeholder="选择开始时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker 
            v-model="publishForm.endTime" 
            type="datetime" 
            placeholder="选择结束时间"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="publishDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="publishAssignment" :loading="publishing">
          确认发布
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  MagicStick, ArrowLeft, Connection, RefreshLeft, User, Document, 
  Promotion, Setting, CircleClose
} from '@element-plus/icons-vue'
import { getSession } from '../../utils/session'
import {
  checkAIHealth,
  createConversation,
  publishAssembleResult,
  abortConversation,
  getCourseKnowledgePoints,
  createConversationWebSocket
} from '../../api/aiAgent'

const router = useRouter()

// 获取课程ID
const session = getSession()
const courseId = computed(() => session?.selectedCourse?.id)

// AI状态
const aiHealthy = ref(false)

// 对话状态
const conversationId = ref('')
const messages = ref([])
const inputMessage = ref('')
const isTyping = ref(false)
const messagesContainer = ref(null)
const wsConnection = ref(null)
const thinkingMessage = ref('')
const streamingContent = ref('')
const rawStreamingBuffer = ref('')
const pendingResult = ref(null)
const currentStreamLog = ref('')

const extractReplyFromPartialJson = (partialJson) => {
  if (!partialJson) return ''
  const replyMatch = partialJson.match(/"reply"\s*:\s*"([^"]*(?:")?)/s)
  if (replyMatch) {
    let reply = replyMatch[1]
    if (reply.endsWith('"')) {
      reply = reply.slice(0, -1)
    }
    try {
      reply = reply
        .replace(/\\n/g, '\n')
        .replace(/\\"/g, '"')
        .replace(/\\\\/g, '\\')
    } catch (error) {
      // ignore parse errors
    }
    return reply
  }
  return ''
}

// 知识点列表
const knowledgePoints = ref([])
const selectedKp = ref(null)

// 快速配置弹框
const configDialogVisible = ref(false)
const configForm = reactive({
  knowledgePoints: [],
  difficulty: 3,
  singleCount: 5,
  multipleCount: 3,
  blankCount: 5,
  shortCount: 2,
  codeCount: 0,
  totalScore: 100
})

// 发布弹框
const publishDialogVisible = ref(false)
const publishSessionId = ref('')
const publishForm = reactive({
  title: '',
  type: 'exam',
  timeLimit: 60,
  description: '',
  startTime: null,
  endTime: null
})
const publishing = ref(false)

// 难度标记
const difficultyMarks = {
  1: '简单',
  2: '较易',
  3: '中等',
  4: '较难',
  5: '困难'
}

// 计算属性
const inputPlaceholder = computed(() => {
  if (!conversationId.value) return '正在初始化对话...'
  if (isTyping.value) return 'AI 正在思考...'
  return '描述您的组卷需求，或点击左侧按钮快速配置...'
})

const availableKnowledgePoints = computed(() => {
  const selectedIds = configForm.knowledgePoints.map(kp => kp.id)
  return knowledgePoints.value.filter(kp => !selectedIds.includes(kp.id))
})

const isConfigValid = computed(() => {
  const hasKp = configForm.knowledgePoints.length > 0
  const hasQuestions = configForm.singleCount + configForm.multipleCount + 
    configForm.blankCount + configForm.shortCount + configForm.codeCount > 0
  return hasKp && hasQuestions
})

// 方法
const goBack = () => {
  router.push('/teacher/question-bank')
}

const checkHealth = async () => {
  try {
    const res = await checkAIHealth()
    aiHealthy.value = res?.status === 'ok'
  } catch {
    aiHealthy.value = false
  }
}

const initConversation = async () => {
  if (!courseId.value) {
    ElMessage.error('未选择课程')
    return
  }
  
  try {
    const res = await createConversation(courseId.value)
    conversationId.value = res.conversation_id
    if (res.message) {
      messages.value.push({
        role: 'assistant',
        content: res.message
      })
    }
    await setupWebSocket()
  } catch (error) {
    ElMessage.error('初始化对话失败')
  }
}

const loadKnowledgePoints = async () => {
  if (!courseId.value) return
  try {
    // 使用 AI Agent 的知识点 API，与对话中的知识点保持一致
    const res = await getCourseKnowledgePoints(courseId.value)
    // AI Agent 直接返回数组，不是 {code, data} 格式
    if (Array.isArray(res)) {
      knowledgePoints.value = res
    } else if (res.data) {
      knowledgePoints.value = res.data
    }
    console.log('加载知识点:', knowledgePoints.value.length, '个')
  } catch (error) {
    console.error('加载知识点失败:', error)
  }
}

const setupWebSocket = async () => {
  if (!conversationId.value) return
  
  if (wsConnection.value) {
    wsConnection.value.close()
  }
  
  pendingResult.value = null
  streamingContent.value = ''
  rawStreamingBuffer.value = ''
  thinkingMessage.value = ''
  currentStreamLog.value = ''
  
  wsConnection.value = createConversationWebSocket(conversationId.value)
  wsConnection.value
    .on('ready', (data) => {
      console.log('WebSocket ready:', data)
    })
    .on('thinking', (data) => {
      isTyping.value = true
      thinkingMessage.value = data.message || 'AI 正在思考...'
      streamingContent.value = ''
      rawStreamingBuffer.value = ''
      pendingResult.value = null
      currentStreamLog.value = data.message || ''
      scrollToBottom()
    })
    .on('chunk', (data) => {
      const chunk = data.data || ''
      rawStreamingBuffer.value += chunk
      const reply = extractReplyFromPartialJson(rawStreamingBuffer.value)
      if (reply) {
        streamingContent.value = reply
        currentStreamLog.value = reply
      }
      scrollToBottom()
    })
    .on('action', (data) => {
      thinkingMessage.value = data.message || '正在执行操作...'
      rawStreamingBuffer.value = ''
      streamingContent.value = ''
      if (data.message) {
        currentStreamLog.value = `${currentStreamLog.value ? `${currentStreamLog.value}\n\n` : ''}${data.message}`
      }
      scrollToBottom()
    })
    .on('result', (data) => {
      pendingResult.value = data.data || null
      thinkingMessage.value = data.message || '组卷完成'
      if (data.message) {
        currentStreamLog.value = `${currentStreamLog.value ? `${currentStreamLog.value}\n\n` : ''}${data.message}`
      }
      scrollToBottom()
    })
    .on('published', (data) => {
      if (data.data && data.data.assignment_id) {
        ElMessage.success(`试卷已成功发布！作业ID: ${data.data.assignment_id}`)
      }
    })
    .on('done', (data) => {
      isTyping.value = false
      thinkingMessage.value = ''
      streamingContent.value = ''
      rawStreamingBuffer.value = ''
      const streamSnapshot = currentStreamLog.value
      
      const replyData = data.data || {}
      if (pendingResult.value && !replyData.result) {
        replyData.result = pendingResult.value
      }
      pendingResult.value = null
      
      const assistantMessage = {
        role: 'assistant',
        content: replyData.message || ''
      }
      if (streamSnapshot) {
        assistantMessage.streamLog = streamSnapshot
      }
      if (replyData.result) {
        assistantMessage.result = replyData.result
      }
      if (replyData.session_id) {
        assistantMessage.sessionId = replyData.session_id
      }
      messages.value.push(assistantMessage)
      scrollToBottom()
      currentStreamLog.value = ''
    })
    .on('error', (data) => {
      isTyping.value = false
      thinkingMessage.value = ''
      streamingContent.value = ''
      rawStreamingBuffer.value = ''
      pendingResult.value = null
      currentStreamLog.value = ''
      ElMessage.error(data.message || '处理失败，请重试')
    })
    .on('aborted', (data) => {
      isTyping.value = false
      thinkingMessage.value = ''
      streamingContent.value = ''
      rawStreamingBuffer.value = ''
      pendingResult.value = null
      currentStreamLog.value = ''
      ElMessage.warning(data.message || '对话已中断')
    })
    .on('close', () => {
      console.log('WebSocket closed')
    })
  
  try {
    await wsConnection.value.connect()
    console.log('WebSocket connected')
  } catch (error) {
    console.error('WebSocket 连接失败:', error)
    ElMessage.error('WebSocket 连接失败，请刷新页面后重试')
  }
}

const sendMessage = async () => {
  if (!inputMessage.value.trim() || !conversationId.value || isTyping.value) return
  
  const userMessage = inputMessage.value.trim()
  inputMessage.value = ''
  
  messages.value.push({
    role: 'user',
    content: userMessage
  })
  
  scrollToBottom()
  isTyping.value = true
  thinkingMessage.value = 'AI 正在思考...'
  streamingContent.value = ''
  rawStreamingBuffer.value = ''
  pendingResult.value = null
  
  try {
    if (!wsConnection.value || !wsConnection.value.connected) {
      await setupWebSocket()
    }
    if (!wsConnection.value || !wsConnection.value.connected) {
      throw new Error('WebSocket 未连接')
    }
    wsConnection.value.sendMessage(userMessage)
  } catch (error) {
    console.error('发送消息失败:', error)
    isTyping.value = false
    thinkingMessage.value = ''
    streamingContent.value = ''
    rawStreamingBuffer.value = ''
    pendingResult.value = null
    ElMessage.error('发送消息失败，请检查网络连接')
  }
}

const abortMessage = async () => {
  if (!conversationId.value) return
  
  try {
    await abortConversation(conversationId.value)
    isTyping.value = false
    messages.value.push({
      role: 'assistant',
      content: '对话已中断。您可以继续提出新的需求。'
    })
  } catch (error) {
    console.error('中断失败:', error)
  }
}

const resetConversation = async () => {
  messages.value = []
  conversationId.value = ''
  if (wsConnection.value) {
    wsConnection.value.close()
    wsConnection.value = null
  }
  streamingContent.value = ''
  thinkingMessage.value = ''
  rawStreamingBuffer.value = ''
  pendingResult.value = null
  currentStreamLog.value = ''
  await initConversation()
}

const showConfigDialog = () => {
  configDialogVisible.value = true
}

const addKnowledgePoint = (kp) => {
  if (kp && !configForm.knowledgePoints.find(item => item.id === kp.id)) {
    configForm.knowledgePoints.push(kp)
  }
  selectedKp.value = null
}

const removeKnowledgePoint = (kp) => {
  const index = configForm.knowledgePoints.findIndex(item => item.id === kp.id)
  if (index > -1) {
    configForm.knowledgePoints.splice(index, 1)
  }
}

const submitConfig = () => {
  const kpNames = configForm.knowledgePoints.map(kp => kp.title).join('、')
  const questionParts = []
  if (configForm.singleCount > 0) questionParts.push(`${configForm.singleCount}道单选题`)
  if (configForm.multipleCount > 0) questionParts.push(`${configForm.multipleCount}道多选题`)
  if (configForm.blankCount > 0) questionParts.push(`${configForm.blankCount}道填空题`)
  if (configForm.shortCount > 0) questionParts.push(`${configForm.shortCount}道简答题`)
  if (configForm.codeCount > 0) questionParts.push(`${configForm.codeCount}道编程题`)
  
  const message = `请帮我组一份试卷，知识点包括：${kpNames}，难度设为${configForm.difficulty}，需要${questionParts.join('、')}，总分${configForm.totalScore}分，请开始组卷。`
  
  inputMessage.value = message
  configDialogVisible.value = false
  
  nextTick(() => {
    sendMessage()
  })
}

const showPublishDialog = (sessionId) => {
  publishSessionId.value = sessionId
  publishForm.title = ''
  publishForm.type = 'exam'
  publishForm.timeLimit = 60
  publishForm.description = ''
  publishForm.startTime = null
  publishForm.endTime = null
  publishDialogVisible.value = true
}

const publishAssignment = async () => {
  if (!publishForm.title.trim()) {
    ElMessage.warning('请输入标题')
    return
  }
  if (
    publishForm.type === 'exam' && 
    (!publishForm.timeLimit || publishForm.timeLimit < 1)
  ) {
    ElMessage.warning('考试类型需要设置时限')
    return
  }
  
  publishing.value = true
  try {
    const payload = {
      title: publishForm.title,
      publisher_user_id: session?.user?.id || 20001,
      assignment_type: publishForm.type,
      description: publishForm.description || undefined,
      start_time: publishForm.startTime?.toISOString(),
      end_time: publishForm.endTime?.toISOString()
    }
    
    if (publishForm.type === 'exam') {
      payload.time_limit = publishForm.timeLimit
    }
    
    const res = await publishAssembleResult(publishSessionId.value, payload)
    if (res.assignment_id) {
      ElMessage.success(`发布成功！作业ID: ${res.assignment_id}`)
      publishDialogVisible.value = false
      
      messages.value.push({
        role: 'assistant',
        content: `🎉 试卷已成功发布为${publishForm.type === 'exam' ? '考试' : '作业'}！\n\n作业ID: ${res.assignment_id}\n标题: ${publishForm.title}\n\n学生现在可以在作业列表中查看并完成。`
      })
      scrollToBottom()
    }
  } catch (error) {
    ElMessage.error('发布失败')
  } finally {
    publishing.value = false
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

const formatMessage = (content) => {
  if (!content) return ''
  return content.replace(/\n/g, '<br>')
}

const getQuestionTypeText = (type) => {
  const map = { single: '单选题', multiple: '多选题', blank: '填空题', short: '简答题', code: '编程题' }
  return map[type] || type
}

const getTotalQuestions = (result) => {
  // 支持两种格式：questions 数组或 questions_by_type 对象
  if (result?.questions && Array.isArray(result.questions)) {
    return result.questions.length
  }
  if (result?.questions_by_type) {
    return Object.values(result.questions_by_type).reduce((sum, arr) => sum + arr.length, 0)
  }
  if (result?.total_questions) {
    return result.total_questions
  }
  return 0
}

// 将 questions 数组转换为按类型分组的对象
const getQuestionsByType = (result) => {
  if (result?.questions_by_type) {
    return result.questions_by_type
  }
  if (result?.questions && Array.isArray(result.questions)) {
    const grouped = {}
    result.questions.forEach(q => {
      const type = q.type || q.question_type || 'unknown'
      if (!grouped[type]) {
        grouped[type] = []
      }
      grouped[type].push(q)
    })
    return grouped
  }
  return {}
}

// 生命周期
onMounted(async () => {
  await checkHealth()
  await loadKnowledgePoints()
  await initConversation()
})

onBeforeUnmount(() => {
  if (wsConnection.value) {
    wsConnection.value.close()
  }
})
</script>

<style scoped>
.smart-paper-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8ec 100%);
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  border-bottom: 1px solid #ebeef5;
  flex-shrink: 0;
}

.back-btn {
  font-weight: 500;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-title h2 {
  margin: 0;
  font-size: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.status-tag {
  display: flex;
  align-items: center;
  gap: 4px;
}

.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
  padding: 20px;
  overflow: hidden;
  min-height: 0;
}

.messages-area {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  margin-bottom: 16px;
  min-height: 0;
}

.message {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.message.user {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.message.assistant .message-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.message.user .message-avatar {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  color: white;
}

.message-content {
  max-width: 75%;
}

.message-bubble {
  padding: 12px 16px;
  border-radius: 16px;
  line-height: 1.6;
  font-size: 14px;
}

.message.assistant .message-bubble {
  background: #f4f5f7;
  border-bottom-left-radius: 4px;
}

.message.user .message-bubble {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-bottom-right-radius: 4px;
}

.typing-indicator {
  display: flex;
  gap: 4px;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  background: #909399;
  border-radius: 50%;
  animation: typing 1.4s infinite both;
}

.typing-indicator span:nth-child(2) { animation-delay: 0.2s; }
.typing-indicator span:nth-child(3) { animation-delay: 0.4s; }

@keyframes typing {
  0%, 60%, 100% { transform: translateY(0); }
  30% { transform: translateY(-6px); }
}

.thinking-bubble {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.thinking-text {
  font-size: 13px;
  color: #606266;
}

.stream-log {
  margin-top: 10px;
}

.stream-log-content {
  padding: 10px 12px;
  background: #f8f9fa;
  border-radius: 8px;
  line-height: 1.5;
  color: #4a4a4a;
}

.result-preview {
  margin-top: 12px;
}

.result-card {
  border-radius: 12px;
}

.result-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
}

.result-stats {
  display: flex;
  gap: 24px;
  margin-bottom: 16px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-item .label {
  font-size: 12px;
  color: #909399;
}

.stat-item .value {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.question-list {
  max-height: 200px;
  overflow-y: auto;
}

.question-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 0;
  border-bottom: 1px dashed #ebeef5;
}

.question-item:last-child {
  border-bottom: none;
}

.q-index {
  color: #909399;
  font-size: 12px;
}

.q-title {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.result-actions {
  margin-top: 16px;
  text-align: center;
}

.input-area {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  padding: 16px 20px;
  background: white;
  flex-shrink: 0;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.quick-config-btn {
  flex-shrink: 0;
}

.message-input {
  flex: 1;
}

.message-input :deep(.el-textarea__inner) {
  border-radius: 12px;
  resize: none;
}

.send-btn {
  flex-shrink: 0;
  height: 54px;
  border-radius: 12px;
}

.abort-btn {
  flex-shrink: 0;
  height: 54px;
  border-radius: 12px;
}

/* 配置弹框样式 */
.config-dialog :deep(.el-dialog__body) {
  padding: 20px 24px;
}

.knowledge-points-selector {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.selected-kps {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  min-height: 32px;
  padding: 8px;
  background: #f8f9fa;
  border-radius: 8px;
}

.kp-tag {
  font-size: 13px;
}

.empty-tip {
  color: #909399;
  font-size: 13px;
}

.question-counts {
  width: 100%;
}

.count-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 12px;
}

.count-item .label {
  font-size: 13px;
  color: #606266;
}

.form-tip {
  margin-left: 8px;
  color: #909399;
  font-size: 13px;
}

/* 发布弹框样式 */
.publish-dialog :deep(.el-dialog__body) {
  padding: 20px 24px;
}
</style>
