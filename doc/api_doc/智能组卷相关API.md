# SmartCourse 教师端 API 使用文档

本文档整理了当前智慧课程平台教师端后端可用的 HTTP 接口、调用规范以及示例，涵盖题目管理、作业管理、文件存储和 AI Agent 组卷相关的能力。

> **🔥 新功能推荐**：现已支持 **对话式 AI 智能组卷**！无需手动配置参数，通过自然语言对话即可完成组卷和发布。详见 [第 5 章：AI Agent 智能组卷服务](#5-ai-agent-智能组卷服务python-fastapi)。

## 1. 基本约定

- **Base URL**：`http://localhost:8080`
- **数据格式**：统一使用 `Content-Type: application/json`（上传文件除外）。
- **认证头**：教师端接口暂以 `userId` 请求头传入当前教师的 `user.id`（示例默认 `20001`）。
- **返回结构**：所有接口使用统一的 `Result` 包裹，结构如下：

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1732160000000
}
```

`data` 字段为具体业务返回体。分页接口的 `data.items` 为列表，`data.pagination` 中包含 `page/pageSize/total/totalPages`。

---

## 2. 题目管理 API

### 2.1 创建题目
- **Method / Path**：`POST /api/questions`
- **Content-Type**：`application/json`
- **请求头**：`userId: 20001` （当前登录教师ID）
- **请求体字段说明**：
  - `title`（必填）：题目标题/题干
  - `questionType`（必填）：题型，可选值：`single`（单选）、`multiple`（多选）、`blank`（填空）、`short`（简答）、`code`（编程）
  - `difficulty`（必填）：难度系数，整数 1-5
  - `correctAnswer`（必填）：正确答案
  - `explanation`（可选）：答案解析
  - `knowledgePoint`（可选）：知识点 ID
  - `courseId`（必填）：课程 ID
  - `chapterId`（可选）：章节 ID
  - `options`（选择题必填）：选项数组，每个选项包含 `optionKey`（A/B/C/D）、`content`（选项内容）、`isCorrect`（是否正确答案）
- **请求体示例**：
```json
{
  "title": "解释 transformer 中 Q/K/V 的来源",
  "questionType": "short",
  "difficulty": 4,
  "correctAnswer": "见解析",
  "explanation": "Q= X·W_Q，K= X·W_K，V= X·W_V",
  "knowledgePoint": "60001",
  "courseId": 40001,
  "chapterId": 72001,
  "options": []
}
```
- **选择题示例**：
```json
{
  "title": "以下哪个不是深度学习框架？",
  "questionType": "single",
  "difficulty": 2,
  "correctAnswer": "A",
  "explanation": "Excel 是表格处理软件，不是深度学习框架",
  "knowledgePoint": "60001",
  "courseId": 40001,
  "chapterId": 72001,
  "options": [
    {"optionKey": "A", "content": "Excel", "isCorrect": true},
    {"optionKey": "B", "content": "PyTorch", "isCorrect": false},
    {"optionKey": "C", "content": "TensorFlow", "isCorrect": false},
    {"optionKey": "D", "content": "Keras", "isCorrect": false}
  ]
}
```
- **curl 示例**：
```bash
curl -X POST http://localhost:8080/api/questions \
  -H "Content-Type: application/json" \
  -H "userId: 20001" \
  -d '{
    "title": "什么是梯度下降？",
    "questionType": "short",
    "difficulty": 3,
    "correctAnswer": "一种优化算法",
    "explanation": "梯度下降是通过迭代寻找函数最小值的优化算法",
    "courseId": 40001,
    "chapterId": 72001,
    "options": []
  }'
```
- **返回示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": 80005,
  "timestamp": 1732160000000
}
```
`data` 字段为新建题目的 ID。

### 2.2 更新题目
- **Method / Path**：`PUT /api/questions/{id}`
- **Content-Type**：`application/json`
- **请求头**：`userId: 20001`
- **路径参数**：`id` - 题目 ID
- **请求体**：与创建题目相同，但所有字段均为可选
- **说明**：
  - 仅更新请求体中出现的字段
  - `options` 传空数组 `[]` 可清空选项
  - `options` 不传或传 `null` 表示不修改选项
- **curl 示例**：
```bash
# 只修改难度和解析
curl -X PUT http://localhost:8080/api/questions/80005 \
  -H "Content-Type: application/json" \
  -H "userId: 20001" \
  -d '{
    "difficulty": 4,
    "explanation": "更详细的解析内容..."
  }'
```
- **返回示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": null,
  "timestamp": 1732160100000
}
```

### 2.3 删除题目
- **Method / Path**：`DELETE /api/questions/{id}`
- **请求头**：`userId: 20001`
- **路径参数**：`id` - 题目 ID
- **说明**：逻辑删除题干及关联选项，不会真实删除数据库记录
- **curl 示例**：
```bash
curl -X DELETE http://localhost:8080/api/questions/80005 \
  -H "userId: 20001"
```
- **返回示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": null,
  "timestamp": 1732160200000
}
```

#### 批量删除
- **Method / Path**：`DELETE /api/questions/batch`
- **Content-Type**：`application/json`
- **请求体**：题目 ID 数组
- **curl 示例**：
```bash
curl -X DELETE http://localhost:8080/api/questions/batch \
  -H "Content-Type: application/json" \
  -H "userId: 20001" \
  -d '[80001, 80002, 80003]'
```

### 2.4 查询题目详情
- **Method / Path**：`GET /api/questions/{id}`
- **路径参数**：`id` - 题目 ID
- **curl 示例**：
```bash
curl http://localhost:8080/api/questions/80001
```
- **返回示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 80001,
    "title": "解释 transformer 中 Q/K/V 的来源",
    "questionType": "short",
    "difficulty": 4,
    "correctAnswer": "见解析",
    "explanation": "Q= X·W_Q，K= X·W_K，V= X·W_V",
    "knowledgePoint": "60001",
    "knowledgePointName": "注意力机制",
    "courseId": 40001,
    "courseName": "深度学习基础",
    "chapterId": 72001,
    "chapterName": "第一章",
    "createdBy": 20001,
    "createdAt": "2024-11-20 10:30:00",
    "updatedAt": "2024-11-20 10:30:00",
    "options": []
  },
  "timestamp": 1732160300000
}
```

### 2.5 分页查询题目

#### 方式 A：GET 请求（推荐简单查询）
- **Method / Path**：`GET /api/questions`
- **查询参数**：
  - `courseId`（可选）：课程 ID
  - `chapterId`（可选）：章节 ID
  - `questionType`（可选）：题型
  - `difficulty`（可选）：难度
  - `knowledgePoint`（可选）：知识点 ID
  - `title`（可选）：题目标题关键词（模糊查询）
  - `createdBy`（可选）：创建者 ID
  - `sortField`（可选）：排序字段，可选 `create_time`、`difficulty`，默认 `create_time`
  - `sortOrder`（可选）：排序方向，`asc` 或 `desc`，默认 `desc`
  - `page`（可选）：页码，默认 1
  - `pageSize`（可选）：每页数量，默认 10
- **curl 示例**：
```bash
# 查询课程40001下的所有简答题，按难度升序
curl "http://localhost:8080/api/questions?courseId=40001&questionType=short&sortField=difficulty&sortOrder=asc&page=1&pageSize=10"

# 搜索标题包含"transformer"的题目
curl "http://localhost:8080/api/questions?title=transformer&page=1&pageSize=5"

# 查询某章节下难度为4的题目
curl "http://localhost:8080/api/questions?chapterId=72001&difficulty=4"
```
- **返回示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "items": [
      {
        "id": 80001,
        "title": "解释 transformer 中 Q/K/V 的来源",
        "questionType": "short",
        "difficulty": 4,
        "knowledgePointName": "注意力机制",
        "courseName": "深度学习基础",
        "chapterName": "第一章",
        "createdAt": "2024-11-20 10:30:00"
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 10,
      "total": 25,
      "totalPages": 3
    }
  },
  "timestamp": 1732160400000
}
```

#### 方式 B：POST 请求（推荐复杂查询）
- **Method / Path**：`POST /api/questions/query`
- **Content-Type**：`application/json`
- **请求体**：`QueryQuestionRequest`（字段同 GET 参数）
- **适用场景**：参数较多、需要组合复杂过滤条件时使用
- **curl 示例**：
```bash
curl -X POST http://localhost:8080/api/questions/query \
  -H "Content-Type: application/json" \
  -d '{
    "courseId": 40001,
    "questionType": "short",
    "difficulty": 4,
    "knowledgePoint": "60001",
    "sortField": "create_time",
    "sortOrder": "desc",
    "page": 1,
    "pageSize": 10
  }'
```
- **返回格式**：与 GET 方式相同

### 2.6 题目统计

#### 2.6.1 按题型统计
- **Method / Path**：`GET /api/questions/statistics/question-type`
- **查询参数**：`courseId`（必填） - 课程 ID
- **curl 示例**：
```bash
curl "http://localhost:8080/api/questions/statistics/question-type?courseId=40001"
```
- **返回示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {"questionType": "single", "count": 20},
    {"questionType": "multiple", "count": 10},
    {"questionType": "blank", "count": 15},
    {"questionType": "short", "count": 12},
    {"questionType": "code", "count": 8}
  ],
  "timestamp": 1732160500000
}
```

#### 2.6.2 按难度统计
- **Method / Path**：`GET /api/questions/statistics/difficulty`
- **查询参数**：`courseId`（必填） - 课程 ID
- **curl 示例**：
```bash
curl "http://localhost:8080/api/questions/statistics/difficulty?courseId=40001"
```
- **返回示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {"difficulty": 1, "count": 5},
    {"difficulty": 2, "count": 12},
    {"difficulty": 3, "count": 18},
    {"difficulty": 4, "count": 15},
    {"difficulty": 5, "count": 8}
  ],
  "timestamp": 1732160600000
}
```

#### 2.6.3 按知识点统计
- **Method / Path**：`GET /api/questions/statistics/knowledge-point`
- **查询参数**：`courseId`（必填） - 课程 ID
- **curl 示例**：
```bash
curl "http://localhost:8080/api/questions/statistics/knowledge-point?courseId=40001"
```
- **返回示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {"knowledgePoint": "60001", "knowledgePointName": "注意力机制", "count": 12},
    {"knowledgePoint": "60002", "knowledgePointName": "卷积神经网络", "count": 18},
    {"knowledgePoint": "60003", "knowledgePointName": "循环神经网络", "count": 10}
  ],
  "timestamp": 1732160700000
}
```

### 2.7 批量导入题目
- **Method / Path**：`POST /api/questions/import`
- **Content-Type**：`application/json`
- **请求头**：`userId: 20001`
- **请求体**：`CreateQuestionRequest` 数组
- **curl 示例**：
```bash
curl -X POST http://localhost:8080/api/questions/import \
  -H "Content-Type: application/json" \
  -H "userId: 20001" \
  -d '[
    {
      "title": "什么是反向传播？",
      "questionType": "short",
      "difficulty": 3,
      "correctAnswer": "一种训练神经网络的算法",
      "courseId": 40001,
      "options": []
    },
    {
      "title": "以下哪个是激活函数？",
      "questionType": "single",
      "difficulty": 2,
      "correctAnswer": "B",
      "courseId": 40001,
      "options": [
        {"optionKey": "A", "content": "损失函数", "isCorrect": false},
        {"optionKey": "B", "content": "ReLU", "isCorrect": true},
        {"optionKey": "C", "content": "优化器", "isCorrect": false}
      ]
    }
  ]'
```
- **返回示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": 2,
  "timestamp": 1732160800000
}
```
`data` 字段为成功导入的题目数量。

---

## 3. 作业/考试管理 API（教师端）

> **说明**：原 `POST /api/assignments/generate` 已由 Python AI Agent 服务替代，请参考文档第 5 章通过 `AIagent` 服务进行智能组卷，再使用下列接口管理作业。

### 3.1 手动创建作业
- **Method / Path**：`POST /api/assignments/create`
- **Content-Type**：`application/json`
- **请求头**：`userId: 20001`
- **请求体字段说明**：
  - `title`（必填）：作业/考试标题
  - `courseId`（必填）：课程 ID
  - `type`（必填）：类型，可选值 `homework`（作业）、`exam`（考试）、`quiz`（测验）
  - `description`（可选）：作业描述
  - `questionIds`（必填）：题目 ID 数组
  - `totalScore`（可选）：总分，默认 100
  - `timeLimit`（可选）：时间限制（分钟）
- **请求体示例**：
```json
{
  "title": "第一章课后作业",
  "courseId": 40001,
  "type": "homework",
  "description": "完成第一章所有习题",
  "questionIds": [80001, 80002, 80003],
  "totalScore": 100,
  "timeLimit": 30
}
```
- **curl 示例**：
```bash
curl -X POST http://localhost:8080/api/assignments/create \
  -H "Content-Type: application/json" \
  -H "userId: 20001" \
  -d '{
    "title": "深度学习期中考试",
    "courseId": 40001,
    "type": "exam",
    "description": "涵盖前四章内容",
    "questionIds": [80001, 80002, 80003, 80004, 80005],
    "totalScore": 100,
    "timeLimit": 120
  }'
```
- **返回示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "assignmentId": 90001,
    "title": "深度学习期中考试",
    "status": "draft",
    "createdAt": "2024-11-21 10:00:00"
  },
  "timestamp": 1732160900000
}
```

### 3.2 更新作业
- **Method / Path**：`PUT /api/assignments/{id}`
- **Content-Type**：`application/json`
- **请求头**：`userId: 20001`
- **路径参数**：`id` - 作业 ID
- **请求体**：所有字段可选，与创建作业相同
- **说明**：
  - 仅更新请求体中出现的字段
  - `questionIds` 传 `[]` 表示清空题目
  - `questionIds` 不传或传 `null` 表示不修改题目
- **curl 示例**：
```bash
# 只修改标题和描述
curl -X PUT http://localhost:8080/api/assignments/90001 \
  -H "Content-Type: application/json" \
  -H "userId: 20001" \
  -d '{
    "title": "深度学习期中考试（修订版）",
    "description": "涵盖前五章内容"
  }'
```
- **返回示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": null,
  "timestamp": 1732161000000
}
```

### 3.3 发布与撤销作业

#### 3.3.1 发布作业
- **Method / Path**：`POST /api/assignments/publish`
- **Content-Type**：`application/json`
- **请求头**：`userId: 20001`
- **请求体字段说明**：
  - `assignmentId`（必填）：作业 ID
  - `startTime`（必填）：开始时间，格式 `yyyy-MM-dd HH:mm:ss`
  - `endTime`（必填）：结束时间
  - `mode`（可选）：模式，`question`（按题目）或 `paper`（按卷面），默认 `question`
  - `duration`（可选）：持续时间（分钟）
  - `allowedFileTypes`（可选）：允许的文件类型数组
- **curl 示例**：
```bash
curl -X POST http://localhost:8080/api/assignments/publish \
  -H "Content-Type: application/json" \
  -H "userId: 20001" \
  -d '{
    "assignmentId": 90001,
    "startTime": "2024-11-25 08:00:00",
    "endTime": "2024-11-25 10:00:00",
    "mode": "question",
    "duration": 120,
    "allowedFileTypes": ["pdf", "docx"]
  }'
```
- **返回示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "assignmentId": 90001,
    "status": "published",
    "startTime": "2024-11-25 08:00:00",
    "endTime": "2024-11-25 10:00:00"
  },
  "timestamp": 1732161100000
}
```

#### 3.3.2 撤销发布
- **Method / Path**：`POST /api/assignments/{id}/unpublish`
- **请求头**：`userId: 20001`
- **路径参数**：`id` - 作业 ID
- **curl 示例**：
```bash
curl -X POST http://localhost:8080/api/assignments/90001/unpublish \
  -H "userId: 20001"
```
- **返回示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "assignmentId": 90001,
    "status": "draft"
  },
  "timestamp": 1732161200000
}
```

### 3.4 删除作业
- **Method / Path**：`DELETE /api/assignments/{id}`
- **请求头**：`userId: 20001`
- **路径参数**：`id` - 作业 ID
- **说明**：逻辑删除作业，并清理关联的题目映射关系
- **curl 示例**：
```bash
curl -X DELETE http://localhost:8080/api/assignments/90001 \
  -H "userId: 20001"
```
- **返回示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": null,
  "timestamp": 1732161300000
}
```

### 3.5 查询作业

#### 3.5.1 作业详情
- **Method / Path**：`GET /api/assignments/{id}`
- **路径参数**：`id` - 作业 ID
- **curl 示例**：
```bash
curl http://localhost:8080/api/assignments/90001
```
- **返回示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 90001,
    "title": "深度学习期中考试",
    "courseId": 40001,
    "courseName": "深度学习基础",
    "type": "exam",
    "status": "published",
    "description": "涵盖前四章内容",
    "totalScore": 100,
    "timeLimit": 120,
    "startTime": "2024-11-25 08:00:00",
    "endTime": "2024-11-25 10:00:00",
    "publisherUserId": 20001,
    "publisherName": "张老师",
    "createdAt": "2024-11-21 10:00:00",
    "questions": [
      {
        "id": 80001,
        "title": "解释 transformer 中 Q/K/V 的来源",
        "questionType": "short",
        "difficulty": 4,
        "score": 20
      },
      {
        "id": 80002,
        "title": "以下哪个不是深度学习框架？",
        "questionType": "single",
        "difficulty": 2,
        "score": 10
      }
    ]
  },
  "timestamp": 1732161400000
}
```

#### 3.5.2 作业列表（分页）
- **Method / Path**：`GET /api/assignments/list`
- **查询参数**：
  - `courseId`（可选）：课程 ID
  - `type`（可选）：作业类型
  - `status`（可选）：状态，`draft`（草稿）、`published`（已发布）、`closed`（已关闭）
  - `publisherUserId`（可选）：发布者 ID
  - `page`（可选）：页码，默认 1
  - `pageSize`（可选）：每页数量，默认 10
- **curl 示例**：
```bash
# 查询某课程下所有已发布的考试
curl "http://localhost:8080/api/assignments/list?courseId=40001&type=exam&status=published&page=1&pageSize=10"

# 查询某教师发布的所有作业
curl "http://localhost:8080/api/assignments/list?publisherUserId=20001"
```
- **返回示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "items": [
      {
        "id": 90001,
        "title": "深度学习期中考试",
        "courseId": 40001,
        "courseName": "深度学习基础",
        "type": "exam",
        "status": "published",
        "totalScore": 100,
        "questionCount": 5,
        "startTime": "2024-11-25 08:00:00",
        "endTime": "2024-11-25 10:00:00",
        "publisherName": "张老师",
        "createdAt": "2024-11-21 10:00:00"
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 10,
      "total": 15,
      "totalPages": 2
    }
  },
  "timestamp": 1732161500000
}
```

### 3.6 作业题目维护

#### 3.6.1 添加题目
- **Method / Path**：`POST /api/assignments/{id}/questions/add`
- **Content-Type**：`application/json`
- **请求头**：`userId: 20001`
- **路径参数**：`id` - 作业 ID
- **请求体**：题目 ID 数组
- **curl 示例**：
```bash
curl -X POST http://localhost:8080/api/assignments/90001/questions/add \
  -H "Content-Type: application/json" \
  -H "userId: 20001" \
  -d '[80006, 80007, 80008]'
```
- **返回示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "assignmentId": 90001,
    "addedCount": 3,
    "totalQuestionCount": 8
  },
  "timestamp": 1732161600000
}
```

#### 3.6.2 移除题目
- **Method / Path**：`POST /api/assignments/{id}/questions/remove`
- **Content-Type**：`application/json`
- **请求头**：`userId: 20001`
- **路径参数**：`id` - 作业 ID
- **请求体**：题目 ID 数组
- **curl 示例**：
```bash
curl -X POST http://localhost:8080/api/assignments/90001/questions/remove \
  -H "Content-Type: application/json" \
  -H "userId: 20001" \
  -d '[80006, 80007]'
```
- **返回示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "assignmentId": 90001,
    "removedCount": 2,
    "totalQuestionCount": 6
  },
  "timestamp": 1732161700000
}
```

---

## 4. 文件管理 API

### 4.1 上传文件
- **Method / Path**：`POST /api/files/upload`
- **Content-Type**：`multipart/form-data`
- **请求参数**：
  - `file`（必填）：上传的文件，通过表单字段传递
  - `category`（可选）：文件分类，可选值 `answer`（答案）、`material`（资料）、`attachment`（附件），默认 `answer`
- **curl 示例**：
```bash
# 上传文件（默认分类为 answer）
curl -X POST http://localhost:8080/api/files/upload \
  -F "file=@test.txt"

# 上传文件并指定分类
curl -X POST http://localhost:8080/api/files/upload \
  -F "file=@document.pdf" \
  -F "category=material"
```
- **返回示例**：
```json
{
  "code": 200,
  "message": "文件上传成功",
  "data": {
    "fileName": "原始文件.docx",
    "fileUrl": "/uploads/answer/2024/11/21/uuid.docx",
    "size": 10240,
    "uploadTime": "2024-11-21 17:40:00"
  },
  "timestamp": 1732186800000
}
```
- **Swagger 测试**：访问 `http://localhost:8080/swagger-ui.html`，在文件管理接口中可直接选择文件上传。

### 4.2 下载文件
- **Method / Path**：`GET /api/files/download/**?filePath=/answer/2024/11/21/uuid.docx`
- **请求参数**：
  - `filePath`（必填）：文件路径，为上传返回的 `fileUrl` 去掉 `/uploads` 前缀
- **curl 示例**：
```bash
# 下载文件
curl -O "http://localhost:8080/api/files/download/**?filePath=/answer/2024/11/21/uuid.docx"

# 或指定保存文件名
curl -o downloaded.txt "http://localhost:8080/api/files/download/**?filePath=/answer/2024/11/21/test.txt"
```
- **说明**：浏览器直接访问该 URL 即可触发文件下载。

### 4.3 删除文件
- **Method / Path**：`DELETE /api/files?fileUrl=/uploads/answer/2024/11/21/uuid.docx`
- **请求参数**：
  - `fileUrl`（必填）：完整的文件 URL 路径（包含 `/uploads` 前缀）
- **curl 示例**：
```bash
curl -X DELETE "http://localhost:8080/api/files?fileUrl=/uploads/answer/2024/11/21/uuid.docx"
```
- **返回示例**：
```json
{
  "code": 200,
  "message": "文件删除成功",
  "data": null,
  "timestamp": 1732186900000
}
```

### 4.4 完整测试流程
```bash
# Step 1: 上传文件并保存返回的 fileUrl
FILE_URL=$(curl -s -X POST http://localhost:8080/api/files/upload \
  -F "file=@test.txt" | jq -r '.data.fileUrl')
echo "上传成功，fileUrl: $FILE_URL"

# Step 2: 下载文件
FILE_PATH=$(echo $FILE_URL | sed 's|/uploads||')
curl -O "http://localhost:8080/api/files/download/**?filePath=$FILE_PATH"
echo "下载成功"

# Step 3: 删除文件
curl -X DELETE "http://localhost:8080/api/files?fileUrl=$FILE_URL"
echo "删除成功"
```

---

## 5. AI Agent 智能组卷服务（Python FastAPI）

该服务位于 `AIagent` 目录，提供基于 AI 的智能对话式组卷功能。

### 5.1 启动服务
```bash
# 进入 AIagent 目录
cd AIagent

# 启动 FastAPI 服务（默认端口 8000）
uvicorn server:app --host 0.0.0.0 --port 8000 --reload

# 或使用 Python 直接运行
python -m uvicorn server:app --port 8000
```

访问 `http://localhost:8000/docs` 可查看自动生成的 API 文档。

---

## 🔥 推荐：对话式智能组卷 (Workflow API)

新版 AI Agent 采用对话式交互，无需手动配置参数，通过自然语言对话即可完成组卷。

### 5.2 创建对话会话
- **Method / Path**：`POST /workflow/conversations`
- **Content-Type**：`application/json`
- **说明**：启动一个新的对话会话，AI 助手会主动引导收集信息
- **请求体示例**：
```json
{
  "course_id": 40001
}
```
- **curl 示例**：
```bash
curl -X POST http://localhost:8000/workflow/conversations \
  -H "Content-Type: application/json" \
  -d '{"course_id": 40001}'
```
- **返回示例**：
```json
{
  "conversation_id": "abc123...",
  "message": "您好！我是SmartCourse的智能组卷助手...",
  "stage": "collecting",
  "action": "ask_info",
  "course_id": 40001,
  "spec": {
    "knowledge_points": null,
    "target_difficulty": null,
    "question_type_counts": {},
    "total_score": 100,
    "chapter_ids": null
  },
  "missing_fields": ["knowledge_points", "target_difficulty", "question_type_counts"],
  "session_id": null,
  "result": null,
  "assignment_id": null,
  "publish_request": null
}
```

### 5.3 发送消息
- **Method / Path**：`POST /workflow/conversations/{conversation_id}/messages`
- **Content-Type**：`application/json`
- **说明**：向对话发送消息，AI 会理解需求并自动提取信息
- **请求体示例**：
```json
{
  "message": "我需要一份关于神经网络基础的测试，难度3，包含2道简答题"
}
```
- **curl 示例**：
```bash
CONVERSATION_ID="abc123..."
curl -X POST http://localhost:8000/workflow/conversations/$CONVERSATION_ID/messages \
  -H "Content-Type: application/json" \
  -d '{"message": "我需要关于神经网络的测试，难度3，2道简答题"}'
```
- **返回示例**：
```json
{
  "conversation_id": "abc123...",
  "message": "好的，我理解您需要...",
  "stage": "collecting",
  "action": "ask_info",
  "spec": {
    "knowledge_points": ["神经网络基础"],
    "target_difficulty": 3,
    "question_type_counts": {"short": 2},
    "total_score": 100
  },
  "missing_fields": [],
  "result": null
}
```

### 5.4 对话式组卷流程

完整的对话式组卷流程示例：

```bash
# Step 1: 创建对话
CONV_ID=$(curl -s -X POST http://localhost:8000/workflow/conversations \
  -H "Content-Type: application/json" \
  -d '{"course_id": 40001}' | jq -r '.conversation_id')

echo "对话 ID: $CONV_ID"

# Step 2: 提供组卷需求
curl -X POST http://localhost:8000/workflow/conversations/$CONV_ID/messages \
  -H "Content-Type: application/json" \
  -d '{"message": "我需要一份关于神经网络基础和梯度下降的期中测试"}'

# Step 3: 补充难度和题型
curl -X POST http://localhost:8000/workflow/conversations/$CONV_ID/messages \
  -H "Content-Type: application/json" \
  -d '{"message": "难度设为3，包含2道简答题"}'

# Step 4: 确认组卷
curl -X POST http://localhost:8000/workflow/conversations/$CONV_ID/messages \
  -H "Content-Type: application/json" \
  -d '{"message": "开始组卷"}'

# Step 5: 发布试卷
curl -X POST http://localhost:8000/workflow/conversations/$CONV_ID/messages \
  -H "Content-Type: application/json" \
  -d '{"message": "发布试卷，标题：期中测试，类型：考试，教师ID：20001"}'
```

### 5.5 查询对话状态
- **Method / Path**：`GET /workflow/conversations/{conversation_id}`
- **说明**：获取对话当前状态，用于页面刷新或断线重连
- **curl 示例**：
```bash
curl http://localhost:8000/workflow/conversations/$CONV_ID
```

### 5.6 对话阶段说明

| 阶段 | 说明 | 可执行操作 |
|------|------|-----------|
| `collecting` | 收集组卷信息 | 继续补充需求信息 |
| `assembled` | 组卷完成 | 查看结果、调整参数、发布 |
| `published` | 已发布 | 查看作业 ID |
| `completed` | 对话结束 | 无 |

---

---

### 5.8 查询课程知识点
- **Method / Path**：`GET /courses/{course_id}/knowledge-points`
- **路径参数**：`course_id` - 课程 ID
- **说明**：获取指定课程的所有知识点列表
- **curl 示例**：
```bash
curl http://localhost:8000/courses/40001/knowledge-points
```
- **返回示例**：
```json
{
  "course_id": 40001,
  "course_name": "深度学习基础",
  "knowledge_points": [
    {
      "id": "60001",
      "name": "注意力机制",
      "question_count": 12
    },
    {
      "id": "60002",
      "name": "卷积神经网络",
      "question_count": 18
    },
    {
      "id": "60003",
      "name": "循环神经网络",
      "question_count": 10
    }
  ]
}
```

---

### 5.9 查询题型库存
- **Method / Path**：`GET /courses/{course_id}/question-types`
- **路径参数**：`course_id` - 课程 ID
- **说明**：获取指定课程各题型的题目数量统计
- **curl 示例**：
```bash
curl http://localhost:8000/courses/40001/question-types
```
- **返回示例**：
```json
{
  "course_id": 40001,
  "course_name": "深度学习基础",
  "question_types": {
    "single": 20,
    "multiple": 10,
    "blank": 15,
    "short": 12,
    "code": 8
  },
  "total": 65
}
```

---

### 5.10 传统方式智能组卷
- **Method / Path**：`POST /assemble`
- **Content-Type**：`application/json`
- **说明**：根据指定条件智能生成试卷，返回会话 ID 和组卷结果
- **请求体字段说明**：
  - `course_id`（必填）：课程 ID
  - `knowledge_points`（可选）：知识点 ID 数组，为空表示不限知识点
  - `target_difficulty`（可选）：目标难度 1-5，默认 3
  - `question_type_counts`（必填）：各题型数量，如 `{"single": 10, "short": 5}`
  - `total_score`（可选）：总分，默认 100
  - `description`（可选）：组卷说明
- **curl 示例**：
```bash
curl -X POST http://localhost:8000/assemble \
  -H "Content-Type: application/json" \
  -d '{
    "course_id": 40001,
    "knowledge_points": ["60001", "60002"],
    "target_difficulty": 3,
    "question_type_counts": {
      "single": 10,
      "multiple": 5,
      "blank": 5,
      "short": 3
    },
    "total_score": 100,
    "description": "第一章综合测试"
  }'
```
- **返回示例**：
```json
{
  "session_id": "sess_20241121_100001",
  "status": "success",
  "message": "组卷成功",
  "result": {
    "course_id": 40001,
    "total_questions": 23,
    "total_score": 100,
    "average_difficulty": 3.2,
    "questions": [
      {
        "id": 80001,
        "title": "解释 transformer 中 Q/K/V 的来源",
        "question_type": "short",
        "difficulty": 4,
        "knowledge_point": "60001",
        "score": 15
      },
      {
        "id": 80015,
        "title": "以下哪个是激活函数？",
        "question_type": "single",
        "difficulty": 2,
        "knowledge_point": "60002",
        "score": 5
      }
    ],
    "knowledge_point_distribution": {
      "60001": 12,
      "60002": 11
    },
    "difficulty_distribution": {
      "1": 2,
      "2": 5,
      "3": 8,
      "4": 6,
      "5": 2
    }
  },
  "ai_explanation": "本次组卷共选择 23 道题目，涵盖注意力机制和卷积神经网络两个知识点。题目难度分布合理，平均难度 3.2，适合期中测试。单选题侧重基础概念，简答题考查深度理解..."
}
```

---

### 5.11 查询组卷会话
- **Method / Path**：`GET /sessions/{session_id}`
- **路径参数**：`session_id` - 会话 ID（由组卷接口返回）
- **说明**：查询已完成的组卷会话详情
- **curl 示例**：
```bash
curl http://localhost:8000/sessions/sess_20241121_100001
```
- **返回示例**：
```json
{
  "session_id": "sess_20241121_100001",
  "status": "completed",
  "created_at": "2024-11-21 10:00:01",
  "request": {
    "course_id": 40001,
    "knowledge_points": ["60001", "60002"],
    "target_difficulty": 3,
    "question_type_counts": {
      "single": 10,
      "short": 3
    }
  },
  "result": {
    "total_questions": 13,
    "total_score": 100,
    "questions": [...]
  },
  "published": false
}
```

---

### 5.12 发布组卷结果（传统方式）
- **Method / Path**：`POST /sessions/{session_id}/publish`
- **Content-Type**：`application/json`
- **路径参数**：`session_id` - 会话 ID
- **说明**：将组卷结果直接写入 MySQL 数据库，创建作业/考试记录
- **请求体字段说明**：
  - `title`（必填）：作业/考试标题
  - `publisher_user_id`（必填）：发布者（教师）ID
  - `assignment_type`（必填）：类型，`homework` / `exam` / `quiz`
  - `start_time`（可选）：开始时间，ISO 格式
  - `end_time`（可选）：结束时间
  - `description`（可选）：描述
  - `mode`（可选）：模式，`question` / `paper`，默认 `question`
- **curl 示例**：
```bash
curl -X POST http://localhost:8000/sessions/sess_20241121_100001/publish \
  -H "Content-Type: application/json" \
  -d '{
    "title": "AI 自动生成 - 第一章测试",
    "publisher_user_id": 20001,
    "assignment_type": "exam",
    "start_time": "2024-11-25T08:00:00",
    "end_time": "2024-11-25T10:00:00",
    "description": "由 AI Agent 智能组卷生成",
    "mode": "question"
  }'
```
- **返回示例**：
```json
{
  "session_id": "sess_20241121_100001",
  "assignment_id": 90005,
  "status": "published",
  "message": "作业已成功发布到数据库",
  "details": {
    "title": "AI 自动生成 - 第一章测试",
    "course_id": 40001,
    "question_count": 13,
    "total_score": 100,
    "start_time": "2024-11-25 08:00:00",
    "end_time": "2024-11-25 10:00:00"
  }
}
```

---

### 5.13 WebSocket 流式组卷（可选）
- **Method / Path**：`WebSocket /ws/assemble`
- **说明**：通过 WebSocket 连接进行实时流式组卷，可获取组卷过程中的实时反馈
- **连接示例**（JavaScript）：
```javascript
const ws = new WebSocket('ws://localhost:8000/ws/assemble');

ws.onopen = () => {
  // 发送组卷请求
  ws.send(JSON.stringify({
    course_id: 40001,
    knowledge_points: ["60001"],
    target_difficulty: 3,
    question_type_counts: {
      single: 5,
      short: 3
    }
  }));
};

ws.onmessage = (event) => {
  const data = JSON.parse(event.data);
  console.log('组卷进度:', data);
  
  if (data.type === 'progress') {
    console.log(`当前进度: ${data.progress}%`);
  } else if (data.type === 'complete') {
    console.log('组卷完成:', data.result);
    ws.close();
  }
};
```
- **流式消息格式**：
```json
// 进度消息
{
  "type": "progress",
  "progress": 30,
  "message": "正在筛选单选题..."
}

// 完成消息
{
  "type": "complete",
  "session_id": "sess_20241121_100001",
  "result": {
    "total_questions": 8,
    "questions": [...]
  }
}
```

---

### 5.14 传统方式完整流程示例
```bash
# Step 1: 检查服务健康
curl http://localhost:8000/health

# Step 2: 查询课程知识点
curl http://localhost:8000/courses/40001/knowledge-points

# Step 3: 查询题型库存
curl http://localhost:8000/courses/40001/question-types

# Step 4: 智能组卷
SESSION_ID=$(curl -s -X POST http://localhost:8000/assemble \
  -H "Content-Type: application/json" \
  -d '{
    "course_id": 40001,
    "knowledge_points": ["60001", "60002"],
    "target_difficulty": 3,
    "question_type_counts": {"single": 10, "short": 5}
  }' | jq -r '.session_id')

echo "组卷成功，Session ID: $SESSION_ID"

# Step 5: 查询组卷结果
curl http://localhost:8000/sessions/$SESSION_ID

# Step 6: 发布到数据库
curl -X POST http://localhost:8000/sessions/$SESSION_ID/publish \
  -H "Content-Type: application/json" \
  -d '{
    "title": "AI 生成测试卷",
    "publisher_user_id": 20001,
    "assignment_type": "exam",
    "start_time": "2024-11-25T08:00:00",
    "end_time": "2024-11-25T10:00:00"
  }'
```

---

## 6. 开发与调试指南

### 6.1 环境准备

#### 数据库初始化
```bash
# 1. 创建数据库
mysql -u root -p -e "CREATE DATABASE smartcourse DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 2. 导入初始数据
mysql -u root -p smartcourse < backend-setup/sql.sql
```

初始化后系统包含：
- 课程 `40001`：深度学习基础
- 章节 `72001`：第一章
- 教师 `20001`：张老师
- 若干示例题目和知识点

#### 启动后端服务
```bash
# 启动 Spring Boot 应用（端口 8080）
./mvnw spring-boot:run

# 或使用 IDE 运行 SmartCourseApplication
```

#### 启动 AI Agent 服务
```bash
# 进入 AIagent 目录
cd AIagent

# 安装依赖
pip install -r requirements.txt

# 启动服务（端口 8000）
uvicorn server:app --port 8000 --reload
```

---

### 6.2 测试工具

#### Swagger UI（推荐）
- **访问地址**：`http://localhost:8080/swagger-ui.html`
- **功能**：在线测试所有 Spring Boot 接口，支持文件上传
- **优点**：无需安装，界面友好，支持参数验证

#### Postman
- **导入**：可将 Swagger JSON 导出后导入 Postman
- **优点**：支持环境变量、测试脚本、批量测试

#### curl 命令
- **优点**：轻量级，适合快速测试和脚本化
- **示例**：本文档提供了所有接口的 curl 示例

---

### 6.3 完整业务流程示例

#### 场景一：教师手动创建作业
```bash
# Step 1: 创建简答题
Q1=$(curl -s -X POST http://localhost:8080/api/questions \
  -H "Content-Type: application/json" \
  -H "userId: 20001" \
  -d '{
    "title": "什么是反向传播？",
    "questionType": "short",
    "difficulty": 3,
    "correctAnswer": "反向传播是一种训练神经网络的算法",
    "courseId": 40001,
    "options": []
  }' | jq -r '.data')

echo "题目 1 ID: $Q1"

# Step 2: 创建单选题
Q2=$(curl -s -X POST http://localhost:8080/api/questions \
  -H "Content-Type: application/json" \
  -H "userId: 20001" \
  -d '{
    "title": "以下哪个是激活函数？",
    "questionType": "single",
    "difficulty": 2,
    "correctAnswer": "B",
    "courseId": 40001,
    "options": [
      {"optionKey": "A", "content": "损失函数", "isCorrect": false},
      {"optionKey": "B", "content": "ReLU", "isCorrect": true},
      {"optionKey": "C", "content": "优化器", "isCorrect": false}
    ]
  }' | jq -r '.data')

echo "题目 2 ID: $Q2"

# Step 3: 创建作业
ASSIGNMENT_ID=$(curl -s -X POST http://localhost:8080/api/assignments/create \
  -H "Content-Type: application/json" \
  -H "userId: 20001" \
  -d "{
    \"title\": \"第一章课后作业\",
    \"courseId\": 40001,
    \"type\": \"homework\",
    \"questionIds\": [$Q1, $Q2],
    \"totalScore\": 100
  }" | jq -r '.data.assignmentId')

echo "作业 ID: $ASSIGNMENT_ID"

# Step 4: 发布作业
curl -X POST http://localhost:8080/api/assignments/publish \
  -H "Content-Type: application/json" \
  -H "userId: 20001" \
  -d "{
    \"assignmentId\": $ASSIGNMENT_ID,
    \"startTime\": \"2024-11-25 08:00:00\",
    \"endTime\": \"2024-11-27 23:59:59\",
    \"mode\": \"question\"
  }"

echo "作业已发布"
```

#### 场景二：AI 智能组卷并发布
```bash
# Step 1: 检查 AI Agent 服务
curl http://localhost:8000/health

# Step 2: 查看课程题型库存
curl http://localhost:8000/courses/40001/question-types

# Step 3: AI 智能组卷
SESSION_ID=$(curl -s -X POST http://localhost:8000/assemble \
  -H "Content-Type: application/json" \
  -d '{
    "course_id": 40001,
    "knowledge_points": ["60001", "60002"],
    "target_difficulty": 3,
    "question_type_counts": {
      "single": 15,
      "multiple": 5,
      "short": 5
    },
    "total_score": 100
  }' | jq -r '.session_id')

echo "组卷会话 ID: $SESSION_ID"

# Step 4: 查看组卷结果
curl http://localhost:8000/sessions/$SESSION_ID | jq '.result'

# Step 5: 发布到数据库
curl -X POST http://localhost:8000/sessions/$SESSION_ID/publish \
  -H "Content-Type: application/json" \
  -d '{
    "title": "AI 生成 - 期中测试",
    "publisher_user_id": 20001,
    "assignment_type": "exam",
    "start_time": "2024-11-30T09:00:00",
    "end_time": "2024-11-30T11:00:00",
    "description": "由 AI Agent 智能组卷生成"
  }'

echo "考试已发布"
```

#### 场景三：文件管理流程
```bash
# Step 1: 准备测试文件
echo "这是答案解析文档" > answer.txt

# Step 2: 上传文件
FILE_URL=$(curl -s -X POST http://localhost:8080/api/files/upload \
  -F "file=@answer.txt" \
  -F "category=answer" | jq -r '.data.fileUrl')

echo "文件 URL: $FILE_URL"

# Step 3: 下载文件验证
FILE_PATH=$(echo $FILE_URL | sed 's|/uploads||')
curl -O "http://localhost:8080/api/files/download/**?filePath=$FILE_PATH"

# Step 4: 查看下载的文件
cat answer.txt

# Step 5: 删除文件
curl -X DELETE "http://localhost:8080/api/files?fileUrl=$FILE_URL"

echo "文件已删除"
```

---

### 6.4 常见问题排查

#### 1. 文件上传失败
**问题**：Swagger 显示 `{"file": "string"}` 输入框  
**解决**：确保 Controller 使用 `@RequestPart` 并配置 `consumes = MediaType.MULTIPART_FORM_DATA_VALUE`

#### 2. AI Agent 连接失败
**问题**：Spring Boot 无法连接 Python AI Agent 服务  
**排查**：
```bash
# 检查 AI Agent 是否运行
curl http://localhost:8000/health

# 检查端口占用
netstat -tuln | grep 8000
```

#### 3. 数据库连接错误
**问题**：启动时报数据库连接失败  
**解决**：检查 `application.properties` 中的数据库配置
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/smartcourse
spring.datasource.username=root
spring.datasource.password=your_password
```

#### 4. 跨域问题
**问题**：前端调用接口时报 CORS 错误  
**解决**：确保已配置跨域支持（通常在 `WebMvcConfig` 中）

---

### 6.5 性能优化建议

1. **分页查询**：始终使用分页，避免一次性查询大量数据
   ```bash
   # ✅ 推荐
   curl "http://localhost:8080/api/questions?page=1&pageSize=20"
   
   # ❌ 避免
   curl "http://localhost:8080/api/questions?pageSize=10000"
   ```

2. **批量操作**：使用批量接口而非循环调用
   ```bash
   # ✅ 推荐：批量删除
   curl -X DELETE http://localhost:8080/api/questions/batch \
     -d '[80001, 80002, 80003]'
   
   # ❌ 避免：循环单个删除
   for id in 80001 80002 80003; do
     curl -X DELETE http://localhost:8080/api/questions/$id
   done
   ```

3. **缓存利用**：对不常变化的数据（如知识点列表）进行缓存

4. **索引优化**：确保数据库表已创建必要索引（课程 ID、题型、难度等）

---

### 6.6 扩展开发指南

#### 添加新接口
1. 在 Controller 中添加新方法
2. 添加 Swagger 注解（`@Operation`、`@Parameter`）
3. 更新本 API 文档
4. 编写单元测试

#### 集成前端
- 所有接口遵循 RESTful 规范
- 返回统一的 `Result` 结构
- 前端可使用 Axios 或 Fetch API 调用
- 建议使用 TypeScript 定义接口类型

#### 部署生产环境
```bash
# 打包 Spring Boot 应用
./mvnw clean package

# 运行 JAR 文件
java -jar target/SmartCourse-0.0.1-SNAPSHOT.jar

# 使用 Docker 部署
docker build -t smartcourse:latest .
docker run -p 8080:8080 smartcourse:latest
```

---

## 7. 接口快速索引

### 题目管理
- 创建题目：`POST /api/questions`
- 更新题目：`PUT /api/questions/{id}`
- 删除题目：`DELETE /api/questions/{id}`
- 批量删除：`DELETE /api/questions/batch`
- 题目详情：`GET /api/questions/{id}`
- 分页查询：`GET /api/questions` 或 `POST /api/questions/query`
- 题型统计：`GET /api/questions/statistics/question-type`
- 难度统计：`GET /api/questions/statistics/difficulty`
- 知识点统计：`GET /api/questions/statistics/knowledge-point`
- 批量导入：`POST /api/questions/import`

### 作业管理
- 创建作业：`POST /api/assignments/create`
- 更新作业：`PUT /api/assignments/{id}`
- 删除作业：`DELETE /api/assignments/{id}`
- 发布作业：`POST /api/assignments/publish`
- 撤销发布：`POST /api/assignments/{id}/unpublish`
- 作业详情：`GET /api/assignments/{id}`
- 作业列表：`GET /api/assignments/list`
- 添加题目：`POST /api/assignments/{id}/questions/add`
- 移除题目：`POST /api/assignments/{id}/questions/remove`

### 文件管理
- 上传文件：`POST /api/files/upload`
- 下载文件：`GET /api/files/download/**`
- 删除文件：`DELETE /api/files`

### AI Agent 智能组卷
#### 🔥 对话式组卷（推荐）
- 创建对话：`POST /workflow/conversations`
- 发送消息：`POST /workflow/conversations/{id}/messages`
- 查询对话：`GET /workflow/conversations/{id}`

#### 传统方式组卷
- 健康检查：`GET /health`
- 课程知识点：`GET /courses/{course_id}/knowledge-points`
- 题型库存：`GET /courses/{course_id}/question-types`
- 智能组卷：`POST /assemble`
- 查询会话：`GET /sessions/{session_id}`
- 发布结果：`POST /sessions/{session_id}/publish`
- WebSocket 组卷：`WS /ws/assemble`

---

=================
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
