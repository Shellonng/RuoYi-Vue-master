# AI 智能组卷流式输出功能实现说明

## 📋 功能概述

完全复刻 SmartCourseThird 项目的 AI 智能组卷功能，实现：
- ✅ **WebSocket 实时流式输出**：AI 回复逐字显示，模拟打字效果
- ✅ **组卷结果预览卡片**：精美的试卷统计卡片，包含试题数、总分、难度等信息
- ✅ **Finished Working 日志**：显示 AI 工作流程的执行步骤
- ✅ **发布试卷功能**：直接从对话中将 AI 生成的试卷发布为正式作业

---

## 🏗️ 技术架构

### 1. WebSocket 通信层

**文件：** `ruoyi-ui/src/api/smart/aiAgent.js`

#### ConversationWebSocket 类

```javascript
export class ConversationWebSocket {
  constructor(conversationId)
  connect()           // 连接 WebSocket
  sendMessage(msg)    // 发送消息
  on(eventType, fn)   // 注册事件监听器
  close()             // 关闭连接
  isConnected()       // 检查连接状态
}
```

#### 支持的事件类型

| 事件类型 | 说明 | 数据格式 |
|---------|------|---------|
| `ready` | WebSocket 已连接 | `{ conversationId }` |
| `thinking` | AI 正在思考 | `{ message }` |
| `chunk` | 流式数据块 | `{ chunk, content }` |
| `action` | AI 执行操作 | `{ action, message }` |
| `result` | 组卷结果数据 | `{ total_questions, total_score, questions, ... }` |
| `done` | 流式输出完成 | `{}` |
| `error` | 错误信息 | `{ error, message }` |
| `aborted` | 对话已中止 | `{}` |

---

### 2. 前端组件层

**文件：** `ruoyi-ui/src/components/SmartFeatures/SmartPaperDialog.vue`

#### 核心数据结构

```javascript
data() {
  return {
    ws: null,                    // WebSocket 连接
    isTyping: false,             // AI 正在打字
    streamingContent: '',        // 流式内容缓冲区
    rawStreamingBuffer: '',      // 原始流式数据
    pendingResult: null,         // 待附加的结果
    currentStreamLog: [],        // 工作日志
    messages: [                  // 消息列表
      {
        role: 'assistant',
        text: '...',
        time: '10:30',
        completed: true,
        result: {...},           // 组卷结果
        streamLog: [...],        // 工作日志
        sessionId: 'xxx'
      }
    ]
  }
}
```

#### 关键方法

```javascript
// 1. 设置 WebSocket 连接
async setupWebSocket()

// 2. 处理流式数据块
handleStreamChunk(data) {
  this.rawStreamingBuffer += chunk
  const reply = this.extractReplyFromPartialJson(buffer)
  // 更新消息显示
}

// 3. 提取 JSON 中的 reply 字段
extractReplyFromPartialJson(partialJson)

// 4. 处理流式输出完成
handleStreamDone() {
  // 附加 result 和 streamLog
  // 重置缓冲区
}

// 5. 发布试卷
async publishPaper(result, sessionId)

// 6. 预览试卷
previewPaper(result)
```

---

## 🎨 UI 组件说明

### 1. 消息显示区域

```vue
<div class="message-item assistant">
  <div class="message-avatar">
    <i class="el-icon-magic-stick"></i>
  </div>
  <div class="message-content">
    <!-- 消息文本（支持流式更新） -->
    <div class="message-text" v-html="formatMessage(msg.text)"></div>
    
    <!-- Finished working 日志 -->
    <div class="stream-log">
      <el-collapse>
        <el-collapse-item title="🔧 Finished working">
          <div class="log-item">
            <i class="el-icon-check"></i>
            <span>分析知识点依赖关系...</span>
          </div>
        </el-collapse-item>
      </el-collapse>
    </div>
    
    <!-- 组卷结果预览卡片 -->
    <div class="result-preview">
      <div class="result-header">
        <i class="el-icon-document"></i>
        <span>组卷结果预览</span>
      </div>
      <div class="result-stats">
        <div class="stat-item">
          <span class="stat-label">试题总数</span>
          <span class="stat-value">10</span>
        </div>
        <!-- ... -->
      </div>
      <div class="result-actions">
        <el-button type="primary" @click="publishPaper">
          发布为正式试卷
        </el-button>
        <el-button @click="previewPaper">
          预览试卷
        </el-button>
      </div>
    </div>
  </div>
</div>
```

### 2. 流式输出效果

**正在输入状态：**
```vue
<div v-if="isTyping && !streamingContent" class="typing-indicator">
  <span></span>
  <span></span>
  <span></span>
</div>
```

**流式文本更新：**
- 每次收到 `chunk` 事件
- 累积到 `rawStreamingBuffer`
- 提取 `reply` 字段
- 更新最后一条助手消息的 `text`
- 实现逐字显示效果

### 3. 结果预览卡片

**设计特点：**
- 渐变紫色背景 (`linear-gradient(135deg, #667eea 0%, #764ba2 100%)`)
- 圆角阴影效果
- 三大统计指标：试题总数、总分、平均难度
- 题型分布标签
- 发布和预览按钮

---

## 🔧 后端 API 要求

### WebSocket 端点

```
ws://localhost:8001/ws/workflow/conversations/{conversationId}/messages
```

### 消息格式

#### 客户端 → 服务器

```json
{
  "message": "我需要一份神经网络测试，难度3"
}
```

#### 服务器 → 客户端

**1. thinking 事件**
```json
{
  "type": "thinking",
  "payload": {
    "message": "正在分析需求..."
  }
}
```

**2. chunk 事件（流式输出）**
```json
{
  "type": "chunk",
  "payload": {
    "chunk": "好的",
    "content": "好的，我"
  }
}
```

**3. action 事件（工作日志）**
```json
{
  "type": "action",
  "payload": {
    "action": "analyze_knowledge_points",
    "message": "分析知识点依赖关系..."
  }
}
```

**4. result 事件（组卷结果）**
```json
{
  "type": "result",
  "payload": {
    "session_id": "xxx",
    "assignment_id": 123,
    "total_questions": 10,
    "total_score": 100,
    "average_difficulty": 3.5,
    "questions": [
      {
        "type": "single",
        "content": "神经网络的基本单元是？",
        "difficulty": 3,
        "score": 10
      }
    ]
  }
}
```

**5. done 事件（完成）**
```json
{
  "type": "done",
  "payload": {}
}
```

---

## 🚀 使用流程

### 1. 打开对话框

```javascript
// 在课程详情页点击 "AI 智能组卷" 按钮
this.showSmartPaperDialog = true
```

### 2. 创建会话

```javascript
// 组件自动调用
async initConversation() {
  const res = await createConversation(this.courseId)
  this.conversationId = res.conversation_id
  await this.setupWebSocket()
}
```

### 3. 发送消息

**方式一：直接输入**
```
用户输入："我需要一份深度学习测试，难度4，包含5道选择题"
↓
点击发送
↓
WebSocket 发送消息
↓
AI 流式回复
```

**方式二：快速配置**
```
点击 ⚙️ 按钮
↓
选择知识点、难度、题型
↓
生成配置文本
↓
自动发送
```

### 4. 流式输出过程

```
1. thinking 事件 → 显示"思考中..."指示器
2. chunk 事件   → 逐字显示 AI 回复
3. action 事件  → 记录到 streamLog
4. result 事件  → 保存到 pendingResult
5. done 事件    → 附加 result 和 streamLog 到消息
```

### 5. 查看结果

- **Finished working 日志**：点击展开查看 AI 执行步骤
- **组卷结果预览卡片**：查看试题统计、题型分布
- **预览试卷**：弹窗显示完整试题列表
- **发布试卷**：确认后生成正式作业

---

## 🎯 与 SmartCourseThird 的对比

| 功能 | SmartCourseThird | 当前实现 | 状态 |
|-----|-----------------|---------|------|
| WebSocket 流式输出 | ✅ | ✅ | 完全一致 |
| Finished working 日志 | ✅ | ✅ | 完全一致 |
| 组卷结果预览卡片 | ✅ | ✅ | 完全一致 |
| 发布试卷按钮 | ✅ | ✅ | 完全一致 |
| 预览试卷功能 | ✅ | ✅ | 完全一致 |
| 打字指示器 | ✅ | ✅ | 完全一致 |
| 快速配置对话框 | ✅ | ✅ | 完全一致 |
| 重新开始对话 | ✅ | ✅ | 完全一致 |

---

## 🔍 调试指南

### 查看 WebSocket 日志

```javascript
// 浏览器控制台会输出：
[WebSocket] 对话 xxx 已连接
[WebSocket] AI 正在思考...
[WebSocket] 收到流式数据块: {chunk: "好"}
[WebSocket] 收到结果数据: {total_questions: 10}
[WebSocket] 流式输出完成
```

### 检查连接状态

```javascript
// 在 Vue DevTools 中查看
this.ws.isConnected()  // true/false
this.isTyping          // true/false
this.streamingContent  // 当前流式内容
this.messages          // 消息列表
```

### 测试 WebSocket

```javascript
// 在浏览器控制台手动测试
const ws = new WebSocket('ws://localhost:8001/ws/workflow/conversations/xxx/messages')
ws.onopen = () => console.log('已连接')
ws.onmessage = (e) => console.log('收到消息:', JSON.parse(e.data))
ws.send(JSON.stringify({ message: '测试消息' }))
```

---

## ⚠️ 注意事项

### 1. WebSocket 连接失败处理

```javascript
async setupWebSocket() {
  try {
    await this.ws.connect()
  } catch (error) {
    // 降级到普通模式
    this.$message.warning('WebSocket 连接失败，将使用普通模式')
  }
}
```

### 2. 重连机制

```javascript
// ConversationWebSocket 自动重连
if (event.code !== 1000 && this.reconnectAttempts < 3) {
  setTimeout(() => this.connect(), 2000)
}
```

### 3. 内存泄漏防护

```javascript
beforeDestroy() {
  // 组件销毁时清理 WebSocket
  if (this.ws) {
    this.ws.close()
  }
}
```

### 4. JSON 解析容错

```javascript
extractReplyFromPartialJson(partialJson) {
  try {
    // 使用正则提取，避免 JSON.parse 失败
    const replyMatch = partialJson.match(/"reply"\s*:\s*"((?:[^"\\]|\\.)*)"/)
    return replyMatch ? replyMatch[1] : null
  } catch {
    return null
  }
}
```

---

## 📦 文件清单

```
ruoyi-ui/src/
├── api/smart/
│   └── aiAgent.js                      # 新增 ConversationWebSocket 类
├── components/SmartFeatures/
│   ├── SmartPaperDialog.vue            # 重构：支持流式输出和结果预览
│   └── QuickPaperConfig.vue            # 快速配置对话框
└── views/course/
    └── detail.vue                      # 课程详情页（集成入口）

doc/
└── AI智能组卷流式输出功能实现说明.md   # 本文档
```

---

## 🎉 完成效果

用户体验：
1. 点击"AI 智能组卷"按钮
2. 输入需求或使用快速配置
3. **实时看到 AI "打字"回复**
4. **展开查看 AI 工作步骤**
5. **看到精美的组卷结果预览卡片**
6. 点击"发布"按钮生成正式试卷

与 SmartCourseThird 完全一致！✨
