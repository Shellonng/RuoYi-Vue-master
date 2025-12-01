# AI 智能组卷功能实现说明

## 功能概述

已成功集成 **对话式 AI 智能组卷** 功能到题库管理页面。该功能通过与 AI 助手对话，自动生成符合需求的试卷。

## 使用方式

1. 进入课程详情页
2. 切换到"题库"标签页
3. 点击右上角 "✨ AI 智能组卷" 按钮
4. 在弹出的对话框中，用自然语言描述组卷需求，例如：
   - "我需要一份神经网络基础测试，难度3，包含2道简答题"
   - "组一份深度学习期中考试，难度4，包含5道选择题和3道简答题"
5. AI 助手会引导你补充必要信息，最终生成试卷

## 实现内容

### 1. 新增文件

- `ruoyi-ui/src/components/SmartFeatures/SmartPaperDialog.vue` - 智能组卷对话界面组件

### 2. 修改文件

- `ruoyi-ui/src/api/smart/aiAgent.js` - 添加对话式组卷 API：
  - `createConversation()` - 创建对话会话
  - `sendMessage()` - 发送对话消息
  - `getConversationStatus()` - 获取对话状态
  - `cancelConversation()` - 取消对话
  - `resetConversation()` - 重置对话

- `ruoyi-ui/src/views/course/detail.vue`：
  - 导入并注册 `SmartPaperDialog` 组件
  - 添加 `smartPaperDialogVisible` 数据字段
  - 修改 `handleOpenSmartPaper()` 方法打开对话框
  - 在模板中添加智能组卷对话框

## 技术细节

### 前端组件特性

1. **对话式交互**
   - 实时消息显示
   - 打字动画效果
   - 消息历史记录

2. **配置实时预览**
   - 知识点标签展示
   - 难度评级显示
   - 题型分布统计
   - 总分配置

3. **用户体验优化**
   - 示例消息快捷输入
   - 重新开始功能
   - 加载状态提示
   - 空状态引导

### API 集成

- **后端服务**：Python FastAPI (端口 8001)
- **代理配置**：`vue.config.js` 中已配置 `/ai-api` 代理到 `http://localhost:8001`
- **请求工具**：使用 `smartRequest.js` 中的 `aiService`

### 工作流程

```
用户点击按钮 
  ↓
创建对话会话 (POST /workflow/conversations)
  ↓
AI 返回欢迎消息
  ↓
用户输入需求
  ↓
发送消息 (POST /workflow/conversations/{id}/messages)
  ↓
AI 解析需求并补充信息
  ↓
用户确认或补充
  ↓
AI 生成试卷 (stage: completed)
  ↓
返回 assignmentId
```

## 启动 AI 服务

在项目根目录的 `AIagent` 文件夹中运行：

```bash
cd AIagent
uvicorn server:app --host 0.0.0.0 --port 8001 --reload
```

或使用 Python：

```bash
python -m uvicorn server:app --port 8001
```

访问 `http://localhost:8001/docs` 查看 API 文档。

## 界面效果

### 对话界面
- 左侧：AI 助手消息（绿色头像）
- 右侧：用户消息（蓝色头像）
- 底部：输入框和发送按钮
- 折叠面板：实时显示当前组卷配置

### 交互流程
1. 欢迎界面：显示使用说明和示例
2. 对话收集：AI 引导收集组卷参数
3. 配置审核：展示完整配置供确认
4. 生成试卷：AI 自动生成并返回作业 ID
5. 完成确认：显示成功提示

## 注意事项

1. **AI 服务依赖**：功能需要 AI Agent 服务运行在 8001 端口
2. **服务健康检查**：如果服务未启动，会显示友好的错误提示
3. **超时设置**：AI 请求超时设置为 60 秒（组卷可能需要较长时间）
4. **对话状态**：关闭对话框前会自动取消未完成的对话

## 后续优化建议

1. 添加试卷预览功能
2. 支持试卷编辑和调整
3. 保存对话历史记录
4. 支持批量生成试卷
5. 添加试卷质量评估
