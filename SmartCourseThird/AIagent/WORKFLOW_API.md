# SmartCourse 智能组卷 API 使用说明

本说明介绍如何在 Python 后端（`AIagent/server.py`）中启动并调用智能组卷对话服务，供前端聊天界面或其他系统集成使用。

## 1. 启动后端

在 `AIagent` 目录下执行：

```bash
uvicorn server:app --host 0.0.0.0 --port 8000 --reload
```

- 依赖：Python 3.9+、`pip install -r requirements.txt`（FastAPI、uvicorn、pydantic、pymysql 等）。
- 环境变量：
  - `DB_HOST`, `DB_PORT`, `DB_USER`, `DB_PASSWORD`, `DB_NAME`：题库数据库配置。
  - `OPENAI_API_KEY`（或 `DEFAULT_PUBLISHER_USER_ID` 等）根据需要覆写，未指定时使用配置文件默认值。

## 2. 对话式智能组卷 API

### 2.1 创建会话
- **POST** `/workflow/conversations`
- **请求**

```json
{"course_id": 40001}
```

- **响应**

```json
{
  "conversation_id": "c8d4...f2",
  "reply": "你好...（首条提示）",
  "stage": "collecting",
  "action": "ask_info",
  "spec": {"knowledge_points": null, "target_difficulty": null, "question_type_counts": {}, "total_score": 100},
  "missing_fields": ["knowledge_points","target_difficulty","question_type_counts"],
  "result": null,
  "publish_request": null
}
```

### 2.2 发送消息
- **POST** `/workflow/conversations/{conversation_id}/messages`
- **请求**

```json
{"message": "知识点梯度下降、难度4，两道简答题"}
```

- **响应**：结构同上，`reply` 为助手回答；`result` 在成功组卷后返回题目摘要；`stage` 会依次经历 `collecting` → `assembled` → `published`。

### 2.3 查询会话
- **GET** `/workflow/conversations/{conversation_id}`
- **功能**：页面刷新或断线重连时恢复上下文，返回最近一次助手回复和当前状态。

### 2.4 发布
当助手提示可以发布时，继续发送包含发布信息的消息，例如：

```json
{"message": "发布考试，标题《期末考》，教师ID 20001，时间 2025-06-01 到 2025-06-05"}
```

若仍缺字段，助手会提示必须的 `title`、`publisher_user_id`、`assignment_type` 等。发布成功后 `assignment_id` 会出现在响应中。

## 3. 直接调用组卷 + 发布 API

除了对话式接口，还提供传统 REST API，可由前端静态配置调用：

### 3.1 组卷
- **POST** `/assemble`
- **请求**

```json
{
  "course_id": 40001,
  "knowledge_points": ["梯度下降", "正则化技术"],
  "target_difficulty": 4,
  "question_type_counts": {"short": 2},
  "total_score": 100
}
```

- **响应**：包含 `session_id`（用于发布）、`result`（题目列表、缺失项）和 `complete`。

### 3.2 发布
- **POST** `/sessions/{session_id}/publish`
- **请求**

```json
{
  "title": "机器学习期末考试",
  "publisher_user_id": 20001,
  "assignment_type": "exam",
  "start_time": "2025-06-01T08:00:00",
  "end_time": "2025-06-05T18:00:00",
  "description": "AI Agent 自动生成"
}
```

- **响应**：`{"assignment_id": 12345}`。

## 4. WebSocket 组卷（可选）

`WS /ws/assemble` 支持实时 streaming 需求。连接后发送与 `/assemble` 相同的 JSON，服务端会推送结果和说明，可用于需要展示 LLM 思考过程的前端。

## 5. 状态字段说明

- `spec`：当前已收集的组卷参数。
- `missing_fields`：还需用户补充的信息列表。
- `stage`：`collecting`（收集信息）、`assembled`（已生成试卷）、`published`（已发布）。
- `result`：组卷结果，包括题目明细、平均难度、缺失知识点等。
- `publish_request`：从对话中解析出的发布参数快照。

以上接口即可同时满足终端 CLI（`main.py`）和前端聊天界面的组卷需求。请根据实际部署环境设置数据库与 LLM API 配置。***
