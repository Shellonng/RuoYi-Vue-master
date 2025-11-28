<template>
  <div class="ai-assistant-container">
    <!-- 浮动按钮 -->
    <div 
      class="ai-float-button"
      @click="toggleDialog"
      :class="{ 'shake': isShaking }"
    >
      <i class="el-icon-chat-dot-round"></i>
      <div class="assistant-name">小智</div>
      <el-badge v-if="unreadCount > 0" :value="unreadCount" class="badge" />
    </div>

    <!-- 对话框 -->
    <transition name="slide-up">
      <div v-if="dialogVisible" class="ai-dialog-wrapper">
        <div class="ai-dialog-content">
          <div class="dialog-close" @click="dialogVisible = false">
            <i class="el-icon-close"></i>
          </div>
          <!-- 对话框标题 -->
          <div class="dialog-header">
        <div class="header-left">
          <div class="assistant-avatar">
            <i class="el-icon-cpu"></i>
          </div>
          <div class="header-info">
            <div class="assistant-title">AI教学助手 - 小智</div>
            <div class="assistant-status">
              <span class="status-dot"></span>
              在线服务中
            </div>
          </div>
        </div>
        <div class="header-actions">
          <el-tooltip content="清空对话" placement="left">
            <i class="el-icon-delete" @click="clearChat"></i>
          </el-tooltip>
        </div>
      </div>

          <!-- 聊天消息区域 -->
      <div class="chat-container" ref="chatContainer">
        <div class="message-list">
          <div 
            v-for="(message, index) in messages" 
            :key="index" 
            class="message-item"
            :class="message.role"
          >
            <div class="message-avatar">
              <i v-if="message.role === 'assistant'" class="el-icon-cpu"></i>
              <i v-else class="el-icon-user"></i>
            </div>
            <div class="message-content">
              <div class="message-name">
                {{ message.role === 'assistant' ? '小智' : userName }}
              </div>
              <div class="message-text" v-html="formatMessage(message.content)"></div>
              <div class="message-time">{{ message.time }}</div>
            </div>
          </div>
          
          <!-- 加载中提示 -->
          <div v-if="isLoading" class="message-item assistant">
            <div class="message-avatar">
              <i class="el-icon-cpu"></i>
            </div>
            <div class="message-content">
              <div class="message-name">小智</div>
              <div class="typing-indicator">
                <span></span>
                <span></span>
                <span></span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 快捷功能按钮 -->
      <div class="quick-actions">
        <el-button 
          size="mini" 
          type="primary" 
          plain
          @click="sendQuickQuestion('如何创建新课程？')"
        >
          如何创建课程
        </el-button>
        <el-button 
          size="mini" 
          type="success" 
          plain
          @click="sendQuickQuestion('如何布置作业？')"
        >
          如何布置作业
        </el-button>
        <el-button 
          size="mini" 
          type="warning" 
          plain
          @click="sendQuickQuestion('如何批改作业？')"
        >
          如何批改作业
        </el-button>
      </div>

      <!-- 输入区域 -->
      <div slot="footer" class="input-container">
        <el-input
          v-model="inputMessage"
          type="textarea"
          :rows="2"
          placeholder="请输入您的问题..."
          @keyup.enter.native="handleEnter"
          ref="messageInput"
        ></el-input>
        <div class="input-actions">
          <el-button 
            type="primary" 
            size="small"
            :loading="isLoading"
            @click="sendMessage"
            :disabled="!inputMessage.trim()"
          >
            发送
          </el-button>
        </div>
      </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script>
import { callAIAssistant } from '@/api/ai/assistant';

export default {
  name: 'AIAssistant',
  data() {
    return {
      dialogVisible: false,
      inputMessage: '',
      messages: [],
      isLoading: false,
      unreadCount: 0,
      isShaking: false,
      welcomeShown: false
    };
  },
  computed: {
    userName() {
      return this.$store.state.user.name || '老师';
    }
  },
  mounted() {
    console.log('AI助手组件已挂载');
    console.log('当前用户:', this.$store.state.user);
    console.log('dialogVisible:', this.dialogVisible);
    console.log('组件容器:', this.$el);
    
    // 每隔一段时间抖动一下，提醒用户
    setInterval(() => {
      if (!this.dialogVisible && this.messages.length === 0) {
        this.isShaking = true;
        setTimeout(() => {
          this.isShaking = false;
        }, 500);
      }
    }, 30000); // 每30秒
  },
  methods: {
    toggleDialog() {
      this.dialogVisible = !this.dialogVisible;
      if (this.dialogVisible) {
        this.unreadCount = 0;
        
        // 首次打开显示欢迎消息
        if (!this.welcomeShown) {
          this.showWelcomeMessage();
          this.welcomeShown = true;
        }
      }
    },
    openDialog() {
      this.dialogVisible = true;
      this.unreadCount = 0;
      
      // 首次打开显示欢迎消息
      if (!this.welcomeShown) {
        this.showWelcomeMessage();
        this.welcomeShown = true;
      }
    },
    showWelcomeMessage() {
      const hour = new Date().getHours();
      let greeting = '您好';
      if (hour < 12) greeting = '早上好';
      else if (hour < 18) greeting = '下午好';
      else greeting = '晚上好';
      
      const welcomeMessage = {
        role: 'assistant',
        content: `${greeting}，${this.userName}老师！👋\n\n我是您的AI教学助手小智，很高兴为您服务！\n\n我可以帮助您：\n• 解答教学平台使用问题\n• 提供课程管理建议\n• 协助作业和考试管理\n• 分析学生学习情况\n• 推荐教学资源和方法\n\n您有什么问题想要咨询吗？`,
        time: this.getCurrentTime()
      };
      
      this.messages.push(welcomeMessage);
      this.$nextTick(() => {
        this.scrollToBottom();
      });
    },
    handleDialogOpened() {
      // 对话框打开后聚焦输入框
      this.$nextTick(() => {
        if (this.$refs.messageInput) {
          this.$refs.messageInput.focus();
        }
      });
    },
    async sendMessage() {
      if (!this.inputMessage.trim() || this.isLoading) return;
      
      const userMessage = {
        role: 'user',
        content: this.inputMessage.trim(),
        time: this.getCurrentTime()
      };
      
      this.messages.push(userMessage);
      this.inputMessage = '';
      this.isLoading = true;
      
      this.$nextTick(() => {
        this.scrollToBottom();
      });
      
      try {
        // 调用AI助手API，发送消息历史
        const response = await callAIAssistant({
          messages: this.messages.map(msg => ({
            role: msg.role,
            content: msg.content
          }))
        });
        
        const assistantMessage = {
          role: 'assistant',
          content: response.data || '抱歉，我暂时无法回答这个问题。',
          time: this.getCurrentTime()
        };
        
        this.messages.push(assistantMessage);
      } catch (error) {
        console.error('AI助手调用失败:', error);
        const errorMessage = {
          role: 'assistant',
          content: '抱歉，我遇到了一些问题。请稍后再试，或联系技术支持。',
          time: this.getCurrentTime()
        };
        this.messages.push(errorMessage);
      } finally {
        this.isLoading = false;
        this.$nextTick(() => {
          this.scrollToBottom();
        });
      }
    },
    sendQuickQuestion(question) {
      this.inputMessage = question;
      this.sendMessage();
    },
    handleEnter(e) {
      // Ctrl+Enter 或 Command+Enter 发送消息
      if ((e.ctrlKey || e.metaKey) && e.keyCode === 13) {
        e.preventDefault();
        this.sendMessage();
      }
    },
    clearChat() {
      this.$confirm('确定要清空所有对话记录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.messages = [];
        this.welcomeShown = false;
        this.showWelcomeMessage();
        this.$message.success('对话已清空');
      }).catch(() => {});
    },
    scrollToBottom() {
      if (this.$refs.chatContainer) {
        this.$refs.chatContainer.scrollTop = this.$refs.chatContainer.scrollHeight;
      }
    },
    getCurrentTime() {
      const now = new Date();
      return `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`;
    },
    formatMessage(content) {
      // 简单的格式化：支持换行和基本的Markdown
      return content
        .replace(/\n/g, '<br>')
        .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
        .replace(/\*(.*?)\*/g, '<em>$1</em>')
        .replace(/`(.*?)`/g, '<code>$1</code>');
    }
  }
};
</script>

<style lang="scss" scoped>
.ai-assistant-container {
  position: fixed !important;
  right: 0;
  bottom: 0;
  z-index: 2000 !important;
  pointer-events: none;
}

.ai-float-button {
  position: fixed !important;
  right: 30px !important;
  bottom: 30px !important;
  width: 60px !important;
  height: 60px !important;
  background: linear-gradient(135deg, #4A90E2 0%, #50C9C3 100%) !important;
  border-radius: 50% !important;
  display: flex !important;
  flex-direction: column !important;
  align-items: center !important;
  justify-content: center !important;
  cursor: pointer !important;
  box-shadow: 0 4px 12px rgba(74, 144, 226, 0.4) !important;
  transition: all 0.3s ease !important;
  color: white !important;
  z-index: 2001 !important;
  pointer-events: auto !important;
  animation: float 3s ease-in-out infinite; // 添加浮动动画
  
  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6);
    animation: none; // 悬停时停止浮动
  }
  
  &.shake {
    animation: shake 0.5s;
  }
  
  i {
    font-size: 28px;
    margin-bottom: 2px;
  }
  
  .assistant-name {
    font-size: 10px;
    font-weight: 600;
  }
  
  .badge {
    position: absolute;
    top: -5px;
    right: -5px;
  }
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-5px); }
  75% { transform: translateX(5px); }
}

// 浮动动画
@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-8px);
  }
}

// 对话框样式
.ai-dialog-wrapper {
  position: fixed;
  right: 30px;
  bottom: 100px; // 在按钮上方
  width: 400px;
  height: 650px; // 增加高度
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  pointer-events: auto;
  z-index: 2002;
}

.ai-dialog-content {
  display: flex;
  flex-direction: column;
  height: 100%;
  position: relative;
}

.dialog-close {
  position: absolute;
  top: 18px; // 与清空按钮对齐
  right: 20px;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.3s;
  z-index: 10;
  color: #909399;
  
  &:hover {
    background: #f5f7fa;
    color: #606266;
  }
  
  i {
    font-size: 16px;
  }
}

.slide-up-enter-active, .slide-up-leave-active {
  transition: all 0.3s ease;
}

.slide-up-enter, .slide-up-leave-to {
  transform: translateY(20px);
  opacity: 0;
}

.dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 15px 50px 15px 20px; // 右侧预留关闭按钮空间
  border-bottom: 1px solid #eee;
  
  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;
  }
  
  .assistant-avatar {
    width: 40px;
    height: 40px;
    background: linear-gradient(135deg, #4A90E2 0%, #50C9C3 100%);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    
    i {
      font-size: 22px;
      color: white;
    }
  }
  
  .header-info {
    .assistant-title {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
      margin-bottom: 4px;
    }
    
    .assistant-status {
      font-size: 12px;
      color: #67c23a;
      display: flex;
      align-items: center;
      gap: 4px;
      
      .status-dot {
        width: 6px;
        height: 6px;
        background: #67c23a;
        border-radius: 50%;
        animation: pulse 2s infinite;
      }
    }
  }
  
  .header-actions {
    display: flex;
    align-items: center;
    
    i {
      font-size: 16px;
      color: #909399;
      cursor: pointer;
      padding: 4px;
      border-radius: 4px;
      transition: all 0.3s;
      
      &:hover {
        color: #606266;
        background: #f5f7fa;
      }
    }
  }
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.chat-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #f5f7fa;
  
  &::-webkit-scrollbar {
    width: 6px;
  }
  
  &::-webkit-scrollbar-thumb {
    background: #dcdfe6;
    border-radius: 3px;
  }
}

.message-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message-item {
  display: flex;
  gap: 10px;
  animation: fadeIn 0.3s;
  
  &.user {
    flex-direction: row-reverse;
    
    .message-content {
      align-items: flex-end;
      
      .message-text {
        background: linear-gradient(135deg, #4A90E2 0%, #50C9C3 100%);
        color: white;
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
    flex-shrink: 0;
    
    i {
      font-size: 20px;
    }
  }
  
  &.assistant .message-avatar {
    background: linear-gradient(135deg, #4A90E2 0%, #50C9C3 100%);
    color: white;
  }
  
  &.user .message-avatar {
    background: #409eff;
    color: white;
  }
  
  .message-content {
    display: flex;
    flex-direction: column;
    max-width: 70%;
    
    .message-name {
      font-size: 12px;
      color: #909399;
      margin-bottom: 4px;
    }
    
    .message-text {
      background: white;
      padding: 10px 14px;
      border-radius: 8px;
      font-size: 14px;
      line-height: 1.6;
      word-break: break-word;
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
    }
    
    .message-time {
      font-size: 11px;
      color: #c0c4cc;
      margin-top: 4px;
    }
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.typing-indicator {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 10px 14px;
  background: white;
  border-radius: 8px;
  
  span {
    width: 8px;
    height: 8px;
    background: #909399;
    border-radius: 50%;
    animation: typing 1.4s infinite;
    
    &:nth-child(2) {
      animation-delay: 0.2s;
    }
    
    &:nth-child(3) {
      animation-delay: 0.4s;
    }
  }
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
  }
  30% {
    transform: translateY(-10px);
  }
}

.quick-actions {
  padding: 10px 20px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  border-top: 1px solid #eee;
}

.input-container {
  padding: 15px 20px;
  
  .el-textarea {
    margin-bottom: 10px;
  }
  
  .input-actions {
    display: flex;
    justify-content: flex-end;
  }
}
</style>
