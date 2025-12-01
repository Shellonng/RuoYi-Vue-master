<template>
  <el-dialog
    title="✨ AI 智能组卷助手"
    :visible.sync="dialogVisible"
    width="800px"
    :close-on-click-modal="false"
    :before-close="handleClose"
    custom-class="smart-paper-dialog"
  >
    <div class="chat-container">
      <!-- 消息列表 -->
      <div class="messages-area" ref="messagesArea">
        <div
          v-for="(msg, index) in messages"
          :key="index"
          :class="['message-item', msg.role]"
        >
          <div class="message-avatar">
            <i :class="msg.role === 'user' ? 'el-icon-user-solid' : 'el-icon-magic-stick'"></i>
          </div>
          <div class="message-content">
            <!-- 消息文本 -->
            <div class="message-text" v-html="formatMessage(msg.text)"></div>
            
            <!-- 正在生成结果的加载指示器 -->
            <div v-if="msg.isGeneratingResult" class="generating-result">
              <div class="result-loading">
                <i class="el-icon-loading"></i>
                <span>正在生成组卷结果...</span>
              </div>
            </div>
            
            <!-- 工作日志（Finished working 部分） -->
            <div v-if="msg.streamLog && msg.streamLog.length > 0" class="stream-log">
              <el-collapse>
                <el-collapse-item title="🔧 Finished working" name="log">
                  <div class="log-content">
                    <div v-for="(log, i) in msg.streamLog" :key="i" class="log-item">
                      <i class="el-icon-check"></i>
                      <span>{{ log.content }}</span>
                    </div>
                  </div>
                </el-collapse-item>
              </el-collapse>
            </div>
            
            <!-- 组卷结果预览卡片 -->
            <div v-if="msg.result" class="result-preview">
              <div class="result-header">
                <i class="el-icon-document"></i>
                <span>组卷结果预览</span>
              </div>
              <div class="result-stats">
                <div class="stat-item">
                  <span class="stat-label">试题总数</span>
                  <span class="stat-value">{{ getResultValue(msg.result, 'total_questions', 'question_count', 'count') }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">总分</span>
                  <span class="stat-value">{{ formatScore(msg.result) }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">平均难度</span>
                  <span class="stat-value">{{ formatDifficulty(msg.result) }}</span>
                </div>
              </div>
              
              <!-- 题型分布 -->
              <div v-if="msg.result.questions && msg.result.questions.length > 0" class="question-types">
                <div class="type-header">题型分布</div>
                <div class="type-list">
                  <el-tag
                    v-for="(typeGroup, typeKey) in groupQuestionsByType(msg.result.questions)"
                    :key="typeKey"
                    type="info"
                    size="small"
                  >
                    {{ questionTypeLabel(typeKey) }}: {{ typeGroup.length }}道
                  </el-tag>
                </div>
              </div>
              
              <!-- 发布按钮 -->
              <div class="result-actions">
                <el-button
                  type="primary"
                  size="small"
                  icon="el-icon-upload2"
                  @click="handlePublishClick(msg)"
                >
                  发布为正式试卷
                </el-button>
                <el-button
                  type="default"
                  size="small"
                  icon="el-icon-view"
                  @click="previewPaper(msg.result)"
                >
                  预览试卷
                </el-button>
              </div>
            </div>
            
            <div class="message-time">{{ msg.time }}</div>
          </div>
        </div>

        <!-- 正在输入... -->
        <div v-if="isTyping && !streamingContent" class="message-item assistant">
          <div class="message-avatar">
            <i class="el-icon-magic-stick"></i>
          </div>
          <div class="message-content">
            <div class="typing-indicator">
              <span></span>
              <span></span>
              <span></span>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-if="messages.length === 0 && !loading" class="empty-state">
          <i class="el-icon-chat-dot-round"></i>
          <p>您好！我是 SmartCourse 的智能组卷助手</p>
          <p class="empty-tips">请告诉我您的组卷需求，例如：</p>
          <div class="example-messages">
            <el-tag @click="sendExample('我需要一份神经网络基础测试，难度3，包含2道简答题')">
              神经网络测试，难度3，2道简答题
            </el-tag>
            <el-tag @click="sendExample('组一份深度学习期中考试，难度4，包含5道选择题和3道简答题')">
              深度学习期中考试
            </el-tag>
          </div>
        </div>
      </div>

      <!-- 当前状态显示 -->
      <div v-if="currentSpec" class="spec-display">
        <el-collapse>
          <el-collapse-item title="📋 当前组卷配置" name="spec">
            <div class="spec-content">
              <div v-if="currentSpec.knowledge_points && currentSpec.knowledge_points.length > 0">
                <strong>知识点：</strong>
                <el-tag v-for="(kp, i) in currentSpec.knowledge_points" :key="i" size="small" style="margin-right: 5px;">
                  {{ kp }}
                </el-tag>
              </div>
              <div v-if="currentSpec.target_difficulty">
                <strong>难度：</strong>
                <el-rate :value="currentSpec.target_difficulty" disabled :max="5" show-score text-color="#ff9900" />
              </div>
              <div v-if="currentSpec.question_type_counts && Object.keys(currentSpec.question_type_counts).length > 0">
                <strong>题型分布：</strong>
                <span v-for="(count, type) in currentSpec.question_type_counts" :key="type" style="margin-right: 10px;">
                  {{ questionTypeLabel(type) }}: {{ count }}道
                </span>
              </div>
              <div v-if="currentSpec.total_score">
                <strong>总分：</strong>{{ currentSpec.total_score }}
              </div>
            </div>
          </el-collapse-item>
        </el-collapse>
      </div>

      <!-- 输入框 -->
      <div class="input-area">
        <el-button
          class="quick-config-btn"
          type="success"
          icon="el-icon-setting"
          @click="showQuickConfig"
          circle
          title="快速配置组卷参数"
        />
        <el-input
          v-model="userInput"
          placeholder="输入您的需求或回答问题..."
          @keyup.enter.native="sendMessage"
          :disabled="loading || stage === 'completed'"
          clearable
        >
          <el-button
            slot="append"
            icon="el-icon-s-promotion"
            @click="sendMessage"
            :loading="loading"
            :disabled="!userInput.trim() || stage === 'completed'"
          >
            发送
          </el-button>
        </el-input>
      </div>

      <!-- 操作按钮 -->
      <div class="action-buttons">
        <el-button size="small" @click="resetConversation" :disabled="loading || messages.length === 0">
          <i class="el-icon-refresh-left"></i> 重新开始
        </el-button>
        <el-button
          v-if="isTyping"
          size="small"
          type="warning"
          @click="forceStopTyping"
        >
          <i class="el-icon-close"></i> 停止加载
        </el-button>
      </div>
    </div>

    <!-- 快速配置组卷参数对话框 -->
    <quick-paper-config
      :visible.sync="quickConfigVisible"
      :course-id="courseId"
      @submit="handleQuickConfig"
    />

    <!-- 发布作业/考试对话框 -->
    <el-dialog
      title="发布作业/考试"
      :visible.sync="publishDialogVisible"
      width="600px"
      append-to-body
      custom-class="publish-paper-dialog"
    >
      <el-form :model="publishForm" label-width="100px" class="publish-form">
        <el-form-item label="标题" required>
          <el-input
            v-model="publishForm.title"
            placeholder="请输入作业/考试标题"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="类型" required>
          <el-radio-group v-model="publishForm.type">
            <el-radio label="考试">考试</el-radio>
            <el-radio label="作业">作业</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="publishForm.type === '考试'" label="时限(分钟)" required>
          <el-input-number
            v-model="publishForm.timeLimit"
            :min="1"
            :max="600"
            style="width: 100%"
          />
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
            v-model="publishForm.startTime"
            type="datetime"
            placeholder="选择开始时间"
            format="yyyy-MM-dd HH:mm"
            value-format="yyyy-MM-dd HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束时间" required>
          <el-date-picker
            v-model="publishForm.endTime"
            type="datetime"
            placeholder="选择结束时间"
            format="yyyy-MM-dd HH:mm"
            value-format="yyyy-MM-dd HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button @click="publishDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          @click="confirmPublish"
          :loading="isPublishing"
        >
          确认发布
        </el-button>
      </div>
    </el-dialog>
  </el-dialog>
</template>

<script>
import {
  createConversation,
  resetConversation as resetConversationAPI,
  cancelConversation,
  ConversationWebSocket,
  publishAssembleResult
} from '@/api/smart/aiAgent'
import { getQuestionDetail } from '@/api/smart/question'
import QuickPaperConfig from './QuickPaperConfig.vue'

export default {
  name: 'SmartPaperDialog',
  components: {
    QuickPaperConfig
  },
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    courseId: {
      type: [Number, String],
      required: true
    }
  },
  data() {
    return {
      dialogVisible: false,
      conversationId: null,
      messages: [],
      userInput: '',
      loading: false,
      isTyping: false, // AI 正在打字
      streamingContent: '', // 流式内容缓冲区
      rawStreamingBuffer: '', // 原始流式数据缓冲区
      stage: '', // collecting, review, generating, completed
      currentSpec: null,
      sessionId: null,
      assignmentId: null,
      quickConfigVisible: false,
      ws: null, // WebSocket 连接
      pendingResult: null, // 待附加的结果数据
      currentStreamLog: [], // 当前流的工作日志
      currentTimeoutId: null, // 超时定时器 ID
      
      // 发布对话框相关
      publishDialogVisible: false,
      isPublishing: false,
      currentPublishResult: null, // 当前要发布的result数据
      currentPublishSessionId: null, // 当前要发布的sessionId
      publishForm: {
        title: 'AI 智能组卷',
        type: '考试',
        timeLimit: 60,
        description: 'AI 智能助手自动生成',
        startTime: '',
        endTime: ''
      }
    }
  },
  watch: {
    visible(val) {
      this.dialogVisible = val
      if (val && !this.conversationId) {
        this.initConversation()
      }
    },
    dialogVisible(val) {
      this.$emit('update:visible', val)
    }
  },
  beforeDestroy() {
    // 组件销毁时清理 WebSocket
    if (this.ws) {
      this.ws.close()
    }
  },
  methods: {
    /**
     * 初始化对话会话
     */
    async initConversation() {
      this.loading = true
      try {
        console.log('[智能组卷] 开始创建对话会话, courseId:', this.courseId, 'type:', typeof this.courseId)
        
        // 确保 courseId 是有效的
        const courseIdNum = Number(this.courseId)
        if (!courseIdNum || isNaN(courseIdNum) || courseIdNum <= 0) {
          console.error('[智能组卷] 无效的课程ID:', this.courseId)
          throw new Error('课程ID无效')
        }
        
        const res = await createConversation(courseIdNum)
        console.log('[智能组卷] 创建对话会话成功:', res)
        
        this.conversationId = res.conversation_id
        this.stage = res.stage
        this.currentSpec = res.spec
        
        // 设置 WebSocket 连接
        await this.setupWebSocket()
        
        // 添加助手的欢迎消息
        this.messages.push({
          role: 'assistant',
          text: res.message,
          time: this.getCurrentTime(),
          sessionId: null
        })
        
        this.scrollToBottom()
      } catch (error) {
        console.error('[智能组卷] 创建对话失败:', error)
        const errorMsg = error.response?.data?.detail || error.message || 'AI 服务连接失败'
        this.$message.error(`创建对话失败: ${errorMsg}`)
      } finally {
        this.loading = false
      }
    },

    /**
     * 设置 WebSocket 连接
     */
    async setupWebSocket() {
      try {
        if (!this.conversationId) {
          throw new Error('对话 ID 未创建')
        }
        
        this.ws = new ConversationWebSocket(this.conversationId)
        
        // 注册事件处理器
        this.ws.on('ready', () => {
          console.log('[WebSocket] 已准备好')
        })
        
        this.ws.on('thinking', () => {
          console.log('[WebSocket] AI 正在思考...')
          this.isTyping = true
          this.scrollToBottom()
        })
        
        this.ws.on('chunk', (data) => {
          console.log('[WebSocket] 收到 chunk 事件:', {
            chunk: data.chunk,
            content: data.content,
            fullData: data
          })
          this.handleStreamChunk(data)
        })
        
        this.ws.on('action', (data) => {
          console.log('[WebSocket] AI 执行操作:', data)
          if (data) {
            this.currentStreamLog.push({
              type: 'action',
              content: data.action || data.message || '执行操作',
              timestamp: new Date().toISOString()
            })
          }
        })
        
        this.ws.on('result', (data) => {
          console.log('[WebSocket] 收到结果数据:', data)
          if (data) {
            this.pendingResult = data
            
            // 🔥 显示“正在总结组卷结果...”加载状态
            const lastMsg = this.messages[this.messages.length - 1]
            if (lastMsg && lastMsg.role === 'assistant' && !lastMsg.completed) {
              lastMsg.text = '正在总结组卷结果...'
              lastMsg.isGeneratingResult = true  // 标记为生成结果中
            }
          }
        })
        
        this.ws.on('done', (data) => {
          console.log('[WebSocket] 流式输出完成')
          console.log('[WebSocket] done事件完整数据:', JSON.stringify(data, null, 2))
          console.log('[WebSocket] done事件data.session_id:', data?.session_id)
          
          // 🔥 从done事件中提取session_id
          if (data && data.session_id) {
            console.log('[WebSocket] ✅ 成功提取到session_id:', data.session_id)
            this.sessionId = data.session_id
            // 同时保存到pendingResult，这样handleStreamDone可以用
            if (this.pendingResult) {
              this.pendingResult.session_id = data.session_id
            } else {
              this.pendingResult = { session_id: data.session_id }
            }
          } else {
            console.warn('[WebSocket] ⚠️ done事件中没有session_id!')
          }
          this.handleStreamDone()
        })
        
        this.ws.on('error', (error) => {
          console.error('[WebSocket] 错误:', error)
          this.$message.error('WebSocket 连接错误')
          this.isTyping = false
          this.loading = false
        })
        
        this.ws.on('aborted', () => {
          console.log('[WebSocket] 对话已中止')
          this.isTyping = false
          this.loading = false
        })
        
        // 连接 WebSocket
        await this.ws.connect()
        console.log('[WebSocket] 连接成功')
      } catch (error) {
        console.error('[WebSocket] 连接失败:', error)
        this.$message.warning('WebSocket 连接失败，将使用普通模式')
      }
    },

    /**
     * 处理流式数据块
     * 完全按照 SmartCourseThird 的方式处理
     */
    handleStreamChunk(data) {
      console.log('[流式输出] 收到数据块:', data)
      if (!data) {
        console.warn('[流式输出] 收到空数据')
        return
      }
      
      // SmartCourseThird 方式：直接从 data.chunk 或 data.content 获取
      const chunk = data.chunk || data.content || ''
      
      if (!chunk) {
        console.warn('[流式输出] 收到空数据块:', data)
        return
      }
      
      // 累积原始数据（和 SmartCourseThird 一样）
      this.rawStreamingBuffer += chunk
      console.log('[流式输出] 缓冲区长度:', this.rawStreamingBuffer.length)
      
      // 🔥 关键：从 JSON 中提取 reply 字段（和 SmartCourseThird 的 extractReplyFromPartialJson 一样）
      const extractedReply = this.extractReplyFromPartialJson(this.rawStreamingBuffer)
      
      if (extractedReply !== null) {
        // 成功提取到 reply，使用纯文本
        this.streamingContent = extractedReply
        console.log('[流式输出] 提取到回复文本')
      } else {
        // 如果无法解析 JSON，强力清理 JSON 格式
        let content = this.rawStreamingBuffer
        
        // 去掉所有可能的 JSON 包装
        content = content.replace(/^\s*```json\s*/i, '')  // 去掉 ```json
        content = content.replace(/\s*```\s*$/i, '')       // 去掉结尾的 ```
        content = content.replace(/^\s*\{\s*"reply"\s*:\s*"/i, '')  // 去掉 {"reply":"
        content = content.replace(/"\s*\}\s*$/i, '')      // 去掉结尾的 "}
        content = content.replace(/^\s*\{\s*/i, '')       // 去掉开头的 {
        content = content.replace(/\s*\}\s*$/i, '')       // 去掉结尾的 }
        
        // 处理所有转义字符
        content = content.replace(/\\n/g, '\n')
        content = content.replace(/\\r/g, '\r')
        content = content.replace(/\\t/g, '\t')
        content = content.replace(/\\"/g, '"')
        content = content.replace(/\\\\/g, '\\')
        
        this.streamingContent = content
      }
      
      // 更新或创建消息（和 SmartCourseThird 一样的逻辑）
      const lastMsg = this.messages[this.messages.length - 1]
      if (lastMsg && lastMsg.role === 'assistant' && !lastMsg.completed) {
        // 更新现有消息
        lastMsg.text = this.streamingContent
      } else if (this.isTyping) {
        // 创建新消息
        this.messages.push({
          role: 'assistant',
          text: this.streamingContent,
          time: this.getCurrentTime(),
          completed: false,
          sessionId: null
        })
      }
      
      this.scrollToBottom()
    },

    /**
     * 从部分 JSON 中提取 reply 字段
     * 完全复刻 SmartCourseThird 的实现
     */
    extractReplyFromPartialJson(partialJson) {
      if (!partialJson || typeof partialJson !== 'string') {
        return null
      }
      
      try {
        // 方法1: 尝试直接解析完整 JSON
        try {
          const parsed = JSON.parse(partialJson)
          if (parsed.reply) {
            return parsed.reply
          }
        } catch (e) {
          // JSON 不完整，继续尝试正则提取
        }
        
        // 方法2: 使用正则提取 "reply": "..." 的内容
        const replyMatch = partialJson.match(/"reply"\s*:\s*"((?:[^"\\]|\\.)*)"/s)
        if (replyMatch && replyMatch[1]) {
          // 处理 JSON 转义字符
          let reply = replyMatch[1]
          reply = reply.replace(/\\n/g, '\n')
          reply = reply.replace(/\\r/g, '\r')
          reply = reply.replace(/\\t/g, '\t')
          reply = reply.replace(/\\"/g, '"')
          reply = reply.replace(/\\\\/g, '\\')
          return reply
        }
        
        return null
      } catch (error) {
        console.warn('[JSON解析] 错误:', error)
        return null
      }
    },

    /**
     * 处理流式输出完成
     */
    handleStreamDone() {
      console.log('[流式输出] 完成')
      
      // 清除超时定时器
      if (this.currentTimeoutId) {
        clearTimeout(this.currentTimeoutId)
        this.currentTimeoutId = null
      }
      
      this.isTyping = false
      this.loading = false
      
      // 标记最后一条消息为完成
      const lastMsg = this.messages[this.messages.length - 1]
      if (lastMsg && lastMsg.role === 'assistant') {
        lastMsg.completed = true
        lastMsg.isGeneratingResult = false  // 清除加载状态
        
        // 如果有流式内容，使用流式内容
        if (this.streamingContent) {
          lastMsg.text = this.streamingContent
        }
        
        // 附加结果数据
        if (this.pendingResult) {
          console.log('[流式输出] 附加结果:', this.pendingResult)
          lastMsg.result = this.pendingResult
          lastMsg.sessionId = this.pendingResult.session_id || this.sessionId
          this.assignmentId = this.pendingResult.assignment_id || this.assignmentId
        }
        
        // 附加工作日志
        if (this.currentStreamLog.length > 0) {
          console.log('[流式输出] 附加日志:', this.currentStreamLog.length, '条')
          lastMsg.streamLog = [...this.currentStreamLog]
        }
      } else {
        console.warn('[流式输出] 找不到待完成的消息')
      }
      
      // 重置缓冲区
      this.streamingContent = ''
      this.rawStreamingBuffer = ''
      this.pendingResult = null
      this.currentStreamLog = []
      
      this.scrollToBottom()
    },

    /**
     * 发送消息（通过 WebSocket）
     */
    async sendMessage() {
      if (!this.userInput.trim() || this.loading || this.isTyping) return

      const message = this.userInput.trim()
      
      // 添加用户消息
      this.messages.push({
        role: 'user',
        text: message,
        time: this.getCurrentTime()
      })
      
      this.userInput = ''
      this.scrollToBottom()

      // 通过 WebSocket 发送消息
      this.loading = true
      this.isTyping = true
      
      // 设置60秒超时保护
      const timeoutId = setTimeout(() => {
        if (this.isTyping) {
          console.warn('[超时] AI 响应超时，强制结束')
          this.$message.warning('AI 响应超时，请重试')
          this.isTyping = false
          this.loading = false
          
          // 添加错误消息
          this.messages.push({
            role: 'assistant',
            text: '抱歉，响应超时了。请重新发送消息。',
            time: this.getCurrentTime(),
            completed: true
          })
          this.scrollToBottom()
        }
      }, 60000)
      
      // 保存超时 ID 以便清除
      this.currentTimeoutId = timeoutId
      
      try {
        if (this.ws && this.ws.isConnected()) {
          console.log('[发送消息]:', message)
          const success = this.ws.sendMessage(message)
          if (!success) {
            throw new Error('WebSocket 未连接')
          }
        } else {
          throw new Error('WebSocket 连接已断开')
        }
      } catch (error) {
        console.error('发送消息失败:', error)
        this.$message.error('发送失败，请检查连接')
        this.loading = false
        this.isTyping = false
        clearTimeout(timeoutId)
      }
    },

    sendExample(text) {
      this.userInput = text
      this.sendMessage()
    },

    /**
     * 强制停止打字状态（应急按钮）
     */
    forceStopTyping() {
      console.log('[手动停止] 用户手动停止加载')
      
      // 清除超时定时器
      if (this.currentTimeoutId) {
        clearTimeout(this.currentTimeoutId)
        this.currentTimeoutId = null
      }
      
      this.isTyping = false
      this.loading = false
      
      // 如果有未完成的消息，标记为完成
      const lastMsg = this.messages[this.messages.length - 1]
      if (lastMsg && lastMsg.role === 'assistant' && !lastMsg.completed) {
        lastMsg.completed = true
        if (this.streamingContent) {
          lastMsg.text = this.streamingContent
        } else {
          lastMsg.text = '(加载已停止)'
        }
      }
      
      // 重置缓冲区
      this.streamingContent = ''
      this.rawStreamingBuffer = ''
      
      this.$message.info('已停止加载')
    },

    async resetConversation() {
      try {
        await this.$confirm('确定要重新开始吗？当前对话内容将被清空。', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })

        this.loading = true
        await resetConversationAPI(this.conversationId)
        
        // 重置本地状态
        this.messages = []
        this.stage = 'collecting'
        this.currentSpec = null
        this.sessionId = null
        this.assignmentId = null
        
        // 重新初始化
        await this.initConversation()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('重置对话失败:', error)
          this.$message.error('重置失败，请重试')
        }
      } finally {
        this.loading = false
      }
    },

    confirmPaper() {
      if (this.assignmentId) {
        this.$message.success(`试卷已生成，作业ID: ${this.assignmentId}`)
        this.handleClose()
      } else {
        this.$message.warning('试卷尚未生成完成')
      }
    },

    showQuickConfig() {
      this.quickConfigVisible = true
    },

    handleQuickConfig(configText) {
      // 将配置文本作为消息发送
      this.userInput = configText
      this.sendMessage()
      this.quickConfigVisible = false
    },

    async handleClose() {
      if (this.conversationId && this.stage !== 'completed') {
        try {
          await cancelConversation(this.conversationId)
        } catch (error) {
          console.error('取消对话失败:', error)
        }
      }
      
      // 重置所有状态
      this.conversationId = null
      this.messages = []
      this.stage = ''
      this.currentSpec = null
      this.sessionId = null
      this.assignmentId = null
      this.userInput = ''
      
      this.dialogVisible = false
    },

    scrollToBottom() {
      this.$nextTick(() => {
        const area = this.$refs.messagesArea
        if (area) {
          area.scrollTop = area.scrollHeight
        }
      })
    },

    formatMessage(text) {
      // 简单的文本格式化，支持换行
      return text.replace(/\n/g, '<br>')
    },

    questionTypeLabel(type) {
      const labels = {
        single: '单选题',
        multiple: '多选题',
        blank: '填空题',
        short: '简答题',
        code: '编程题',
        judge: '判断题',
        essay: '论述题'
      }
      return labels[type] || '简答题'  // 默认为简答题而不是unknown
    },

    /**
     * 从结果对象中获取值（支持多种字段名）
     */
    getResultValue(result, ...fieldNames) {
      if (!result) return 0
      for (const fieldName of fieldNames) {
        if (result[fieldName] !== undefined && result[fieldName] !== null) {
          return result[fieldName]
        }
      }
      return 0
    },

    /**
     * 格式化总分显示
     */
    formatScore(result) {
      if (!result) return '0分'
      
      // 优先从questions数组计算（最准确）
      if (result.questions && Array.isArray(result.questions)) {
        const calculatedScore = result.questions.reduce((sum, q) => {
          const qScore = q.score || q.points || q.question_score || 0
          return sum + Number(qScore)
        }, 0)
        if (calculatedScore > 0) {
          return `${calculatedScore}分`
        }
      }
      
      // 备选：从result字段获取
      const score = this.getResultValue(result, 'total_score', 'score', 'total_points')
      return `${score}分`
    },

    /**
     * 格式化难度显示（保留一位小数）
     */
    formatDifficulty(result) {
      if (!result) return '0.0/5'
      const difficulty = this.getResultValue(result, 'average_difficulty', 'difficulty', 'avg_difficulty')
      return `${Number(difficulty).toFixed(1)}/5`
    },

    /**
     * 按题型分组试题
     */
    groupQuestionsByType(questions) {
      if (!questions || !Array.isArray(questions)) {
        return {}
      }
      
      const groups = {}
      questions.forEach(q => {
        if (!q) return
        const type = q.type || q.question_type || 'unknown'
        if (!groups[type]) {
          groups[type] = []
        }
        groups[type].push(q)
      })
      return groups
    },

    /**
     * 处理发布按钮点击（先检查msg完整数据）
     */
    handlePublishClick(msg) {
      console.log('[发布] 点击发布按钮')
      console.log('[发布] msg完整对象:', JSON.stringify(msg, null, 2))
      console.log('[发布] msg.sessionId:', msg.sessionId)
      console.log('[发布] msg.result:', msg.result)
      console.log('[发布] this.sessionId:', this.sessionId)
      console.log('[发布] this.conversationId:', this.conversationId)
      
      // 尝试多种方式获取sessionId
      const sessionId = msg.sessionId 
                     || msg.session_id 
                     || (msg.result && msg.result.session_id)
                     || this.sessionId
                     || this.conversationId
      
      console.log('[发布] 最终使用的sessionId:', sessionId)
      
      this.publishPaper(msg.result, sessionId)
    },

    /**
     * 发布试卷（显示发布对话框）
     */
    async publishPaper(result, sessionId) {
      console.log('[发布试卷] 被调用 result:', result, 'sessionId:', sessionId)
      console.log('[发布试卷] sessionId类型:', typeof sessionId, '值:', sessionId)
      
      if (!sessionId) {
        console.warn('[发布试卷] 缺少sessionId，无法发布')
        this.$message.warning('缺少会话信息，无法发布')
        return
      }
      
      // 保存当前要发布的数据
      this.currentPublishResult = result
      this.currentPublishSessionId = sessionId
      
      // 设置默认标题
      const now = new Date()
      const dateStr = now.toLocaleDateString('zh-CN')
      this.publishForm.title = `AI 智能组卷 - ${dateStr}`
      
      console.log('[发布试卷] 准备显示对话框, publishDialogVisible:', this.publishDialogVisible)
      
      // 显示发布对话框
      this.publishDialogVisible = true
      
      console.log('[发布试卷] 对话框状态已设置为true, 当前值:', this.publishDialogVisible)
    },

    /**
     * 确认发布试卷
     */
    async confirmPublish() {
      // 表单验证
      if (!this.publishForm.title.trim()) {
        this.$message.warning('请输入标题')
        return
      }
      
      if (!this.publishForm.startTime || !this.publishForm.endTime) {
        this.$message.warning('请选择开始时间和结束时间')
        return
      }
      
      if (this.publishForm.type === '考试' && (!this.publishForm.timeLimit || this.publishForm.timeLimit < 1)) {
        this.$message.warning('考试类型请设置答题时限')
        return
      }
      
      if (!this.currentPublishSessionId) {
        this.$message.warning('缺少会话信息，无法发布')
        return
      }
      
      this.isPublishing = true
      
      try {
        // 获取当前用户信息（从sessionStorage或store）
        const userStr = sessionStorage.getItem('userInfo') || sessionStorage.getItem('user')
        let userId = 20001 // 默认教师ID
        if (userStr) {
          try {
            const user = JSON.parse(userStr)
            userId = user.userId || user.id || 20001
          } catch (e) {
            console.warn('[发布试卷] 解析用户信息失败:', e)
          }
        }
        
        // 构建发布数据（按照后端API要求的字段名）
        const publishData = {
          title: this.publishForm.title,
          publisher_user_id: userId,
          assignment_type: this.publishForm.type === '考试' ? 'exam' : 'homework',
          description: this.publishForm.description || 'AI 智能助手自动生成',
          start_time: this.publishForm.startTime,
          end_time: this.publishForm.endTime,
          mode: 'question' // 默认为题目模式
        }
        
        // 如果是考试类型，添加时间限制
        if (this.publishForm.type === '考试' && this.publishForm.timeLimit) {
          publishData.time_limit = this.publishForm.timeLimit
        }
        
        console.log('[发布试卷] 准备发布, sessionId:', this.currentPublishSessionId, 'data:', publishData)
        console.log('[发布试卷] 当前courseId:', this.courseId)
        
        // 调用发布API
        const response = await publishAssembleResult(this.currentPublishSessionId, publishData)
        
        console.log('[发布试卷] API返回, response:', response)
        console.log('[发布试卷] response类型:', typeof response)
        console.log('[发布试卷] response.assignment_id:', response?.assignment_id)
        console.log('[发布试卷] JSON.stringify(response):', JSON.stringify(response))
        
        // 检查response是否有效
        if (!response || typeof response !== 'object') {
          throw new Error('后端返回数据格式错误')
        }
        
        // 关闭对话框
        this.publishDialogVisible = false
        
        // 显示成功消息
        const assignmentId = response.assignment_id || response.data?.assignment_id || 'N/A'
        console.log('[发布试卷] 提取的assignmentId:', assignmentId)
        
        if (assignmentId === 'N/A') {
          console.warn('[发布试卷] ⚠️ 警告：未能从响应中提取到assignment_id')
          console.warn('[发布试卷] 完整响应对象:', response)
        }
        
        this.$message.success(`试卷发布成功！作业ID: ${assignmentId}`)
        
        // 添加成功消息到对话
        this.messages.push({
          role: 'assistant',
          text: `✅ 试卷已成功发布！\n\n📋 **作业 ID**: ${assignmentId}\n📝 **标题**: ${this.publishForm.title}\n📁 **类型**: ${this.publishForm.type}`,
          time: new Date().toLocaleTimeString()
        })
        
        this.scrollToBottom()
        
        // 更新状态
        this.stage = 'completed'
        this.assignmentId = assignmentId
        
        // 触发发布成功事件
        this.$emit('published', assignmentId)
        
      } catch (error) {
        console.error('[发布试卷] 发布失败:', error)
        const errorMsg = error.message || error.msg || '发布失败，请重试'
        this.$message.error(errorMsg)
      } finally {
        this.isPublishing = false
      }
    },

    /**
     * 预览试卷
     */
    async previewPaper(result) {
      if (!result || !result.questions) {
        this.$message.warning('暂无试题数据')
        return
      }

      // 🔥 关键：如果题目有ID但缺少选项，从后端获取完整数据
      const loading = this.$loading({
        lock: false,  // 不锁屏，允许用户操作
        text: '加载题目详情中...',
        spinner: 'el-icon-loading',
        background: 'rgba(255, 255, 255, 0.9)',
        target: document.querySelector('.smart-paper-dialog')  // 只在对话框内显示
      })

      try {
        // 获取完整的题目数据（包含选项）
        const enrichedQuestions = await this.enrichQuestionsWithOptions(result.questions)
        
        // 构建预览内容
        const previewHtml = this.buildPreviewHtml(result, enrichedQuestions)
        
        loading.close()
        
        this.$alert(previewHtml, '试卷详情', {
          dangerouslyUseHTMLString: true,
          confirmButtonText: '关闭',
          customClass: 'paper-preview-dialog',
          width: '1400px',  // 固定宽度
          center: false
        })
      } catch (error) {
        loading.close()
        console.error('[预览试卷] 错误:', error)
        this.$message.error('加载题目详情失败')
      }
    },

    /**
     * 丰富题目数据：如果题目有ID但缺少选项，从后端获取
     */
    async enrichQuestionsWithOptions(questions) {
      const enrichedQuestions = []
      
      for (const q of questions) {
        const questionId = q.question_id || q.questionId || q.id
        const questionType = q.type || q.question_type || 'unknown'
        const hasOptions = q.options && q.options.length > 0
        
        // 如果是选择题且有ID但没有选项，从后端获取
        if ((questionType === 'single' || questionType === 'multiple') && questionId && !hasOptions) {
          console.log(`[预览] 题目 ${questionId} 缺少选项，从后端获取...`)
          try {
            const fullQuestion = await this.fetchQuestionWithOptions(questionId)
            enrichedQuestions.push(fullQuestion)
          } catch (error) {
            console.error(`[预览] 获取题目 ${questionId} 失败:`, error)
            enrichedQuestions.push(q) // 使用原始数据
          }
        } else {
          enrichedQuestions.push(q)
        }
      }
      
      return enrichedQuestions
    },

    /**
     * 从后端获取完整的题目数据（包含 question_option 表中的选项）
     */
    async fetchQuestionWithOptions(questionId) {
      try {
        // 调用后端 API 获取题目详情（后端会自动关联 question_option 表）
        const response = await getQuestionDetail(questionId)
        
        console.log(`[预览] 获取题目 ${questionId} 成功:`, response)
        
        // 后端应该返回包含 options 的完整数据
        // response 格式：{ code: 200, data: { question_id, content, options: [...] } }
        if (response && response.data) {
          return response.data
        } else if (response) {
          // 有些API直接返回数据
          return response
        }
        
        throw new Error('题目数据格式错误')
      } catch (error) {
        console.error(`[预览] 获取题目 ${questionId} 失败:`, error)
        throw error
      }
    },

    /**
     * 构建预览HTML
     */
    buildPreviewHtml(result, questions) {
      const totalScore = this.formatScore(result)
      const totalQuestions = this.getResultValue(result, 'total_questions', 'question_count', 'count')
      const avgDifficulty = this.formatDifficulty(result)
      
      let previewHtml = '<div style="padding: 30px; max-height: 80vh; overflow-y: auto;">'
      previewHtml += `<h2 style="text-align: center; color: #303133; margin-bottom: 10px;">试卷预览</h2>`
      previewHtml += `<div style="text-align: center; color: #909399; margin-bottom: 20px;">总分：${totalScore} | 试题数：${totalQuestions}道 | 平均难度：${avgDifficulty}</div>`
      previewHtml += '<hr style="border: none; border-top: 2px solid #DCDFE6; margin: 20px 0;"/>'
      
      questions.forEach((q, index) => {
        // 支持多种字段名
        const questionText = q.content || q.question_text || q.stem || q.question || q.title || `题目 ${index + 1}`
        const questionType = q.type || q.question_type || q.questionType || 'short'
        const difficulty = q.difficulty || q.target_difficulty || 0
        const score = q.score || q.points || 0
        
        // 🔥 关键修复：处理选项数据
        let options = []
        if (q.options && Array.isArray(q.options)) {
          options = q.options
        } else if (q.choices && Array.isArray(q.choices)) {
          options = q.choices
        } else if (q.answers && Array.isArray(q.answers)) {
          options = q.answers
        }
        
        console.log(`[预览] 题目 ${index + 1}:`, {
          questionText,
          questionType,
          difficulty,
          score,
          options,
          rawQuestion: q
        })
        
        previewHtml += `<div style="margin: 25px 0; padding: 15px; background: #F5F7FA; border-radius: 8px;">`
        previewHtml += `<div style="font-size: 16px; font-weight: bold; color: #303133; margin-bottom: 10px;">`
        previewHtml += `${index + 1}. <span style="color: #409EFF;">[${this.questionTypeLabel(questionType)}]</span> ${questionText}`
        previewHtml += `</div>`
        
        // 显示选项（如果有）
        if (options && options.length > 0) {
          console.log(`[预览] 题目 ${index + 1} 的选项详情:`, JSON.stringify(options, null, 2))
          previewHtml += `<div style="margin: 10px 0 10px 30px;">`
          
          options.forEach((opt, optIndex) => {
            const optionLabel = String.fromCharCode(65 + optIndex) // A, B, C, D...
            let optionText = ''
            
            console.log(`[预览] 处理选项 ${optionLabel}:`, opt, 'typeof:', typeof opt)
            
            // 🔥 关键：根据实际数据结构提取选项文本
            if (typeof opt === 'string') {
              // 情况1: 纯字符串
              optionText = opt
            } else if (opt && typeof opt === 'object') {
              // 情况2: 对象，按优先级提取字段
              // 🔥 重要：optionText 是正确的字段名（不是 optionContent）
              optionText = opt.optionText      // ⬅️ 最高优先级，后端实际使用的字段
                        || opt.optionContent 
                        || opt.content 
                        || opt.text 
                        || opt.option 
                        || opt.label 
                        || opt.value
                        || opt.answer
                        || ''
              
              console.log(`[预览] 提取的optionText:`, optionText)
            } else {
              optionText = String(opt || '')
            }
            
            // 最终显示
            if (optionText) {
              previewHtml += `<div style="margin: 8px 0; color: #606266; line-height: 1.6;">`
              previewHtml += `<strong style="color: #409EFF;">${optionLabel}.</strong> ${optionText}`
              previewHtml += `</div>`
            } else {
              console.error(`[预览] 选项 ${optionLabel} 无法提取文本:`, opt)
              previewHtml += `<div style="margin: 8px 0; color: #F56C6C; line-height: 1.6;">`
              previewHtml += `<strong>${optionLabel}.</strong> [选项内容为空]`
              previewHtml += `</div>`
            }
          })
          
          previewHtml += `</div>`
        } else if (questionType === 'single' || questionType === 'multiple') {
          // 如果是选择题但没有选项，显示提示
          console.warn(`[预览] 题目 ${index + 1} 是选择题但没有选项，原始数据:`, q)
          previewHtml += `<div style="margin: 10px 0 10px 30px; padding: 10px; background: #FEF0F0; border-left: 3px solid #F56C6C; color: #F56C6C;">`
          previewHtml += `⚠️ 此题目为选择题，但后端未返回选项数据`
          previewHtml += `</div>`
        } else {
          console.log(`[预览] 题目 ${index + 1} 不是选择题，无需选项`)
        }
        
        previewHtml += `<div style="color: #909399; font-size: 13px; margin-top: 10px; padding-top: 10px; border-top: 1px solid #E4E7ED;">`
        previewHtml += `<span style="margin-right: 15px;">📊 难度：${difficulty}/5</span>`
        previewHtml += `<span>💯 分值：${score}分</span>`
        previewHtml += `</div>`
        previewHtml += `</div>`
      })
      
      previewHtml += '</div>'
      
      return previewHtml
    },

    getCurrentTime() {
      const now = new Date()
      return `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`
    }
  }
}
</script>

<style scoped lang="scss">
.smart-paper-dialog {
  .chat-container {
    display: flex;
    flex-direction: column;
    height: 600px;
  }

  .messages-area {
    flex: 1;
    overflow-y: auto;
    padding: 20px;
    background: #f5f7fa;
    border-radius: 8px;
    margin-bottom: 15px;
  }

  .message-item {
    display: flex;
    margin-bottom: 20px;
    animation: fadeInUp 0.3s ease;

    &.user {
      flex-direction: row-reverse;

      .message-avatar {
        margin-left: 12px;
        background: #409eff;
      }

      .message-content {
        align-items: flex-end;
        background: #409eff;
        color: white;
      }

      .message-time {
        color: rgba(255, 255, 255, 0.8);
      }
    }

    &.assistant {
      .message-avatar {
        margin-right: 12px;
        background: #67c23a;
      }

      .message-content {
        background: white;
      }
    }
  }

  .message-avatar {
    width: 36px;
    height: 36px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 18px;
    flex-shrink: 0;
  }

  .message-content {
    max-width: 70%;
    display: flex;
    flex-direction: column;
    padding: 12px 16px;
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }

  .message-text {
    line-height: 1.6;
    word-break: break-word;
  }

  .message-time {
    font-size: 12px;
    color: #909399;
    margin-top: 6px;
  }

  .typing-indicator {
    display: flex;
    gap: 4px;
    padding: 8px;

    span {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background: #67c23a;
      animation: typing 1.4s infinite;

      &:nth-child(2) {
        animation-delay: 0.2s;
      }

      &:nth-child(3) {
        animation-delay: 0.4s;
      }
    }
  }

  // 正在生成结果的加载样式
  .generating-result {
    margin-top: 15px;
    
    .result-loading {
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 15px;
      background: #f0f9ff;
      border: 1px dashed #409eff;
      border-radius: 8px;
      color: #409eff;
      font-size: 14px;
      
      i {
        font-size: 18px;
        animation: rotating 1s linear infinite;
      }
    }
  }

  .empty-state {
    text-align: center;
    padding: 60px 20px;
    color: #909399;

    i {
      font-size: 64px;
      color: #dcdfe6;
      margin-bottom: 20px;
    }

    p {
      font-size: 16px;
      margin: 10px 0;
    }

    .empty-tips {
      font-size: 14px;
      color: #c0c4cc;
      margin-top: 20px;
    }

    .example-messages {
      margin-top: 15px;
      display: flex;
      flex-direction: column;
      gap: 10px;
      align-items: center;

      .el-tag {
        cursor: pointer;
        transition: all 0.3s;

        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
        }
      }
    }
  }

  .spec-display {
    margin-bottom: 15px;

    .spec-content {
      padding: 10px;
      font-size: 14px;

      > div {
        margin-bottom: 8px;

        &:last-child {
          margin-bottom: 0;
        }
      }

      strong {
        color: #303133;
        margin-right: 8px;
      }
    }
  }

  .input-area {
    margin-bottom: 15px;
    display: flex;
    align-items: flex-start;
    gap: 10px;

    .quick-config-btn {
      flex-shrink: 0;
      margin-top: 5px;
    }

    .el-input {
      flex: 1;
    }
  }

  .action-buttons {
    display: flex;
    justify-content: space-between;
    padding-top: 10px;
    border-top: 1px solid #ebeef5;
  }

  // ========== 流式输出和结果预览样式 ==========

  .stream-log {
    margin-top: 12px;
    background: #f9fafb;
    border-radius: 6px;
    overflow: hidden;

    .log-content {
      padding: 8px;
      max-height: 200px;
      overflow-y: auto;
    }

    .log-item {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 4px 0;
      font-size: 13px;
      color: #606266;

      i {
        color: #67c23a;
        font-size: 14px;
      }
    }
  }

  .result-preview {
    margin-top: 15px;
    padding: 15px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 10px;
    color: white;
    box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);

    .result-header {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 16px;
      font-weight: bold;
      margin-bottom: 12px;

      i {
        font-size: 20px;
      }
    }

    .result-stats {
      display: flex;
      justify-content: space-around;
      padding: 15px 0;
      border-top: 1px solid rgba(255, 255, 255, 0.3);
      border-bottom: 1px solid rgba(255, 255, 255, 0.3);

      .stat-item {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 5px;

        .stat-label {
          font-size: 12px;
          opacity: 0.9;
        }

        .stat-value {
          font-size: 24px;
          font-weight: bold;
        }
      }
    }

    .question-types {
      margin-top: 12px;
      padding-top: 12px;

      .type-header {
        font-size: 14px;
        margin-bottom: 8px;
        opacity: 0.9;
      }

      .type-list {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;

        .el-tag {
          background: rgba(255, 255, 255, 0.2);
          border-color: rgba(255, 255, 255, 0.4);
          color: white;
        }
      }
    }

    .result-actions {
      margin-top: 15px;
      display: flex;
      gap: 10px;
      justify-content: center;

      .el-button {
        border: none;
        
        &.el-button--primary {
          background: white;
          color: #667eea;

          &:hover {
            background: #f0f0f0;
          }
        }

        &.el-button--default {
          background: rgba(255, 255, 255, 0.2);
          color: white;

          &:hover {
            background: rgba(255, 255, 255, 0.3);
          }
        }
      }
    }
  }

  // 用户消息中的结果卡片保持原色
  .message-item.user .result-preview {
    background: rgba(255, 255, 255, 0.2);
  }
}

// ========== 预览试卷对话框样式 ==========
::v-deep .paper-preview-dialog {
  width: 1400px !important;  // 固定宽度
  max-width: 90vw !important;  // 最大不超过视窗90%
  
  .el-message-box {
    width: 1400px !important;
    max-width: 90vw !important;
  }
  
  .el-message-box__message {
    max-height: 80vh !important;
    overflow-y: auto !important;
    padding: 0 !important;
  }
  
  .el-message-box__header {
    padding: 20px 20px 10px !important;
  }
  
  .el-message-box__title {
    font-size: 20px !important;
    font-weight: bold !important;
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes typing {
  0%, 60%, 100% {
    opacity: 0.3;
    transform: scale(0.8);
  }
  30% {
    opacity: 1;
    transform: scale(1);
  }
}

@keyframes rotating {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
