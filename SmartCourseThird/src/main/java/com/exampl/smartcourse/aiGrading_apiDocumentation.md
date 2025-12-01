# AI 批改模块接口文档（apiDocumentation）

该文档汇总 AI 批改模块的所有后端接口，供前端集成参考。

## 基本信息
- 基础路径：`/api/ai-grading`
- 数据格式：`application/json`

---

## 数据结构
- `Assignment`（作业）：`id`、`title`、`courseId`、`publisherUserId`、`type`、`description`、`startTime`、`endTime`、`createTime`、`status`、`updateTime`、`mode`、`timeLimit`、`totalScore`、`duration`、`allowedFileTypes`、`attachments`、`isDeleted`、`deleteTime`（见 `src/main/java/com/exampl/smartcourse/entity/aiGrading/Assignment.java:7-27`）
- `AssignmentSubmission`（作业提交）：`id`、`assignmentId`、`studentUserId`、`status`、`score`、`feedback`、`submitTime`、`gradeTime`、`gradedBy`、`content`、`createTime`、`updateTime`、`fileName`、`filePath`、`isDeleted`、`deleteTime`（见 `src/main/java/com/exampl/smartcourse/entity/aiGrading/AssignmentSubmission.java:7-23`）
- `User`（用户）：`id`、`username`、`password`、`email`、`realName`、`avatar`、`role`、`status`、`createTime`、`updateTime`（见 `src/main/java/com/exampl/smartcourse/entity/aiGrading/User.java:1-12`）
- `Student`（学生）：`id`、`userId`、`enrollmentStatus`、`gpa`、`gpaLevel`、`createTime`、`updateTime`、`isDeleted`、`deleteTime`（见 `src/main/java/com/exampl/smartcourse/entity/aiGrading/Student.java:8-17`）
- `KnowledgePoint`（知识点）：`id`、`courseId`、`title`、`description`、`level`、`creatorUserId`、`createTime`、`updateTime`、`isDeleted`、`deleteTime`（见 `src/main/java/com/exampl/smartcourse/entity/aiGrading/KnowledgePoint.java:7-17`）
- `CourseResource`（课程资源）：`id`、`courseId`、`name`、`fileType`、`fileSize`、`fileUrl`、`description`、`downloadCount`、`uploadUserId`、`createTime`、`updateTime`（见 `src/main/java/com/exampl/smartcourse/entity/aiGrading/CourseResource.java:1-19`）
- `AiGrading`（智能批改记录）：
  - 评分维度：`contentScore`、`logicScore`、`knowledgeScore`、`innovationScore`、`totalScore`
  - AI 反馈：`aiComment`、`improvementSuggestions(JSON)`、`coveredKnowledgePoints(JSON)`、`missingKnowledgePoints(JSON)`
  - LLM 元数据：`llmModel`、`llmTokens`、`processingTime`
  - 状态与错误：`status ∈ {pending, processing, completed, failed}`、`errorMessage`
  - 教师确认：`teacherConfirmed`、`teacherModifiedScore`、`teacherComment`、`confirmedBy`、`confirmedAt`
  - 关联与时间：`id`、`assignmentSubmissionId`、`createdAt`、`updatedAt`
  - 定义位置：`src/main/java/com/exampl/smartcourse/entity/aiGrading/AiGrading.java:8-31`

### 删除最新批改记录（新增）
- 方法：`DELETE /api/ai-grading/grading/{submissionId}/latest`
- 描述：删除指定 `submissionId` 对应的最新一条 `ai_grading` 记录；若不存在记录返回 `false`。
- 返回示例：
```
{
  "code": 200,
  "message": "success",
  "data": true | false
}
```
- 失败示例：
```
{
  "code": 404,
  "message": "grading record not found",
  "data": false
}
```

### 批量标记已批改并同步评分（更新）
- 方法：`POST /api/ai-grading/submissions/mark-graded`
- 描述：将所有存在对应 `ai_grading` 记录的 `assignment_submission`：
  - `status` 统一设置为 `2（已批改）`
  - `score` 更新为该提交最新一条 `ai_grading.total_score`（按 `created_at` 取最新，转换为整数）
- 说明：不区分 `ai_grading.status` 的具体值（`processing/failed/completed`），只要存在关联记录即进行同步；已为 `2` 的记录也会刷新其 `score`。
- 返回示例：
```
{
  "code": 200,
  "message": "success",
  "data": 123 // 实际更新的提交数量
}
```
- 失败示例：
```
{
  "code": 500,
  "message": "internal server error",
  "data": 0
}
```

#### 请求与幂等性
- 请求参数：无
- 幂等性：重复调用不会产生额外副作用；已为 `2` 的记录不会再次更新。
- 影响范围：仅更新那些“存在任意 `ai_grading` 关联记录”的提交。

#### SQL 行为
- 执行语句：`UPDATE assignment_submission s JOIN (SELECT g.assignment_submission_id AS sid, g.total_score AS ts FROM ai_grading g JOIN (SELECT assignment_submission_id AS sid, MAX(created_at) AS mc FROM ai_grading GROUP BY assignment_submission_id) t ON g.assignment_submission_id = t.sid AND g.created_at = t.mc) lg ON lg.sid = s.id SET s.status = 2, s.score = CAST(ROUND(lg.ts) AS SIGNED)`
- 解释：以存在任意 `ai_grading` 的提交为集合，取各提交最新记录的 `total_score`，批量将 `status` 置为 `2`，并同步 `score`（取整）；包含已为 `2` 的记录的评分刷新。

#### 代码位置
- 控制器：`src/main/java/com/exampl/smartcourse/controller/aiGrading/GradingStatusController.java:28`
- 服务：`src/main/java/com/exampl/smartcourse/service/aiGrading/GradingStatusService.java:25`
- Mapper：`src/main/java/com/exampl/smartcourse/mapper/aiGrading/AssignmentSubmissionMapper.java:45`

#### 调用示例
```
curl -X POST http://localhost:8080/api/ai-grading/submissions/mark-graded
```

## 作业列表（已实现）
- 方法：`GET /api/ai-grading/assignments`
- 描述：获取全部作业，支持分页与排序。
- 请求参数：
  - `page`：页码（默认 1）
  - `pageSize`：每页数量（默认 10）
  - `sortBy`：排序字段（支持 `id`、`title`、`courseId`、`publisherUserId`、`publisherRealName`、`startTime`、`endTime`、`createTime`、`updateTime`、`totalScore`、`duration`、`status`）
  - `order`：排序方向（`asc` 或 `desc`，默认 `asc`）
- 返回示例：
```
{
  "code": 200,
  "message": "success",
  "total": 123,
  "data": [ Assignment 对象数组（每项含 `publisherUserId`、`publisherRealName`） ]
}
```
- 失败示例：
```
{
  "code": 500,
  "message": "internal server error",
  "total": 0,
  "data": []
}
```
### 按学生查询作业列表（新增）
- 方法：`GET /api/ai-grading/assignments/by-student`
- 描述：根据 `studentUserId` 返回该学生有关联提交记录的作业列表，支持分页与排序。
- 请求参数：
  - `studentUserId`：学生用户ID（必填）
  - `page`、`pageSize`、`sortBy`（同作业列表，额外支持 `publisherUserId`、`publisherRealName`）、`order`
- 返回示例：
```
{
  "code": 200,
  "message": "success",
  "total": 12,
  "data": [ Assignment 对象数组（每项含 `publisherUserId`、`publisherRealName`） ]
}
```
- 失败示例：
```
{
  "code": 400,
  "message": "studentUserId is required",
  "total": 0,
  "data": []
}
```
### 作业新增（新增）
- 方法：`POST /api/ai-grading/assignments`
- 请求体：`Assignment` 对象字段（至少需 `title`、`courseId`、`publisherUserId`）
- 行为：为空字段使用默认值（如 `status=0`、`mode='question'`、`totalScore=100`、`createTime=当前时间`）
- 返回示例：
```
{
  "code": 200,
  "message": "success",
  "data": Assignment 对象（含 `id`、`publisherRealName` 等）
}
```
- 失败示例：
```
{
  "code": 400,
  "message": "title/courseId/publisherUserId is required",
  "data": null
}
```

### 作业删除（新增）
- 方法：`DELETE /api/ai-grading/assignments/{id}`
- 描述：删除指定 `id` 的作业
- 返回示例：
```
{
  "code": 200,
  "message": "success",
  "data": true | false
}
```
- 失败示例：
```
{
  "code": 404,
  "message": "assignment not found",
  "data": false
}
```
## 作业详情与文件预览（已实现）
- 方法：`GET /api/ai-grading/submissions/{submissionId}`
- 描述：返回单个提交的详情，包括提交记录、作业信息、学生信息、批改信息（若已批改）。
- 返回结构：
```
{
  "code": 200,
  "message": "success",
  "data": {
    "submission": { assignment_submission 对象 },
    "assignment": { assignment 对象 },
    "student": { student 对象 | null },
    "userId": number,
    "realName": string,
    "grading": { ai_grading 对象 | null } // 仅返回最近一条记录
    ,
    "knowledgePoints": [ KnowledgePoint 对象数组 ]
  }
}
```
- 失败示例：
```
{
  "code": 404,
  "message": "submission not found",
  "data": null
}
```

## 提交列表（已实现）
- 方法：`GET /api/ai-grading/submissions`
- 描述：获取全部学生作业提交，支持分页与排序。
- 请求参数：
  - `page`：页码（默认 1）
  - `pageSize`：每页数量（默认 10）
  - `sortBy`：排序字段（支持 `id`、`assignmentId`、`studentUserId`、`status`、`score`、`submitTime`、`gradeTime`、`createTime`、`updateTime`、`fileName`、`realName`）
  - `order`：排序方向（`asc` 或 `desc`，默认 `asc`）
- 返回示例：
```
{
  "code": 200,
  "message": "success",
  "total": 123,
  "data": [ { "student": Student 对象, "submission": AssignmentSubmission 对象, "userId": number, "realName": string } ]
}
```
- 失败示例：
```
{
  "code": 500,
  "message": "internal server error",
  "total": 0,
  "data": []
}
```
## 智能批改触发（已实现）
- 方法：`POST /api/ai-grading/grading/trigger`
- 描述：对单个提交触发 AI 批改。后端根据 `assignment_submission.file_path` 读取学生作业文件（支持 `pdf`/`doc`/`docx`/`txt`），并携带该作业的要求、关联知识点，以及该作业所属课程的资源列表（按 `course_id` 查询 `course_resource`，传入 `name` 与 `description`）作为上下文，调用 LLM 客户端生成批改结果，写入 `ai_grading` 表。
- 异步说明：服务端接收请求后立即将该提交对应的 `ai_grading.status` 置为 `processing` 并返回；后台异步调用 LLM，完成后更新状态为 `completed`（或失败时为 `failed`）。
- 请求体示例：
```
{ "submissionId": number, "llmModel": "Qwen/Qwen3-8B" }
```
- 返回示例：
```
{
  "code": 200,
  "message": "success",
  "data": { "accepted": [ number ], "failed": [ ], "message": "trigger done" }
}
```
- 说明：`submissionId` 为必填；`llmModel` 可选（不传时使用默认模型）。返回中的 `accepted`/`failed` 表示触发受理情况；实际评分与评语在后台生成，前端可通过“批改结果展示”接口轮询。

- 失败示例：
```
{
  "code": 400,
  "message": "no submissionId(s) provided",
  "data": { "accepted": [], "failed": [], "message": "no submissionId(s) provided" }
}
```

### 调试信息（LLM 调用失败排查）
- 失败时会将原因写入 `ai_grading.error_message`，可能包含：
  - `SILICONFLOW_API_KEY missing`（未配置密钥）
  - `network error: <type>: <message>`（网络错误）
  - `http status <code>, requestId=<id>, bodySnippet=<...>`（服务端非 2xx 响应）
  - `parse error: <type>: <message>, contentSnippet=<...>`（返回内容解析失败）
- 服务器日志中也会输出请求体与响应体的片段（截断），以及模型、延迟、token 统计，便于定位问题。



## 待批改作业列表（已实现，结构与作业详情一致）
- 方法：`GET /api/ai-grading/pending`
- 描述：返回所有“已提交未批改且作业模式为 file”的提交列表。每项返回内容与“作业详情”一致；由于待批改，`grading` 通常为 `null`。

### 请求参数（可选）
- `courseId`：课程ID（依据关联 `assignment.course_id` 过滤）
- `assignmentId`：作业ID
- `studentUserId`：学生用户ID
- `keyword`：按作业标题关键字过滤（不区分大小写）

### 返回数据结构
整体返回：
```
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "submission": { assignment_submission 对象 },
      "assignment": { assignment 对象 },
      "student": { student 对象 | null },
      "grading": { ai_grading 对象 | null },
      "knowledgePoints": [ KnowledgePoint 对象数组 ]
    },
    ...
  ]
}
```
- 失败示例：
```
{
  "code": 500,
  "message": "internal server error",
  "data": []
}
```

### 业务逻辑
- 来源筛选：`assignment_submission.status = 1` 且关联的 `assignment.mode = 'file'`
- 支持基于 `assignment.title` 的关键字过滤；返回项为组合数据，与详情结构一致。

## 处理中/失败列表（已实现，结构与作业详情一致）
- 方法：`GET /api/ai-grading/unresolved`
- 描述：返回所有“最新批改记录状态为 processing 或 failed”的提交列表。每项返回内容与“作业详情”一致；其中 `grading` 为该提交的最新 `ai_grading` 记录。

### 返回数据结构
整体返回（与待批改相同）：
```
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "submission": { assignment_submission 对象 },
      "assignment": { assignment 对象 },
      "student": { student 对象 | null },
      "grading": { ai_grading 对象 | 最新记录 },
      "knowledgePoints": [ KnowledgePoint 对象数组 ]
    },
    ...
  ]
}
```
- 失败示例：
```
{
  "code": 500,
  "message": "internal server error",
  "data": []
}
```

### 业务逻辑
- 取每个提交的`最新一条 ai_grading`记录，筛选 `status ∈ {processing, failed}`
- 返回项为组合数据，与详情结构一致。

---


## 批改结果展示（已实现）
- 方法：`GET /api/ai-grading/grading/{submissionId}/result`
- 描述：返回指定 `assignment_submission.id` 对应的批改结果，字段精简为前端所需。
- 返回结构：
```
{
  "code": 200,
  "message": "success",
  "data": {
    "contentScore": number,
    "logicScore": number,
    "knowledgeScore": number,
    "innovationScore": number,
    "totalScore": number,
    "aiComment": string,
    "improvementSuggestions": string,
    "coveredKnowledgePoints": string,
    "missingKnowledgePoints": string,
    "llmModel": string,
    "teacherConfirmed": 0 | 1 | true | false,
    "teacherModifiedScore": number | null,
    "teacherComment": string | null,
    "confirmedBy": number | null,
    "confirmedAt": string | null,
    "createdAt": string,
    "updatedAt": string
  }
}
```
- 失败示例：
```
{
  "code": 404,
  "message": "grading not found",
  "data": null
}
```

## 教师修改与确认（已实现）
- 方法：`POST /api/ai-grading/grading/{submissionId}/confirm`
- 描述：教师对指定提交的评分与评语进行修改并确认。此操作会向 `ai_grading` 表新增一条记录以保留历史。
- 查询参数：`teacherId`
- 请求体字段（可选，未填写则沿用最近一次结果）：
```
{
  "contentScore": number,
  "logicScore": number,
  "knowledgeScore": number,
  "innovationScore": number,
  "totalScore": number,
  "aiComment": string,
  "improvementSuggestions": string,
  "coveredKnowledgePoints": string,
  "missingKnowledgePoints": string,
  "llmModel": string,
  "teacherComment": string
}
```
- 返回：
```
{
  "code": 200,
  "message": "success",
  "data": AiGrading 记录对象（含 `id`、`teacherConfirmed=1`、`teacherModifiedScore=totalScore`、`confirmedAt` 等）
}
```
- 失败示例：
```
{
  "code": 400,
  "message": "teacherId is required",
  "data": null
}
```

## 批改历史查询（已实现）
- 方法：`GET /api/ai-grading/grading/{submissionId}/history`
- 描述：返回指定提交的所有历史批改记录，按创建时间升序。
- 返回：
```
{
  "code": 200,
  "message": "success",
  "data": AiGrading[]
}
```
- 失败示例：
```
{
  "code": 404,
  "message": "submission not found",
  "data": []
}
```

## 批量批改管理（已实现）
- 方法：`POST /api/ai-grading/grading/batch`
- 描述：一次性对多个提交触发 AI 批改。服务端在接收后，对于每个 `submissionId` 都会立即创建/更新对应的 `ai_grading` 记录并将 `status` 置为 `processing`，随后后台异步调用 LLM，完成后更新为 `completed` 或 `failed`。
- 请求体示例：
```
{ "submissionIds": [ number, ... ], "llmModel": "Qwen/Qwen3-8B" }
```
- 返回示例：
```
{
  "code": 200,
  "message": "success",
  "data": { "accepted": [ number, ... ], "failed": [ number, ... ], "message": "trigger done" }
}
```
- 说明：`submissionIds` 为必填数组；`llmModel` 可选。触发为异步受理，评分结果请使用“批改结果展示”或“批改历史查询”接口获取。
- 失败示例：
```
{
  "code": 400,
  "message": "submissionIds is required",
  "data": { "accepted": [], "failed": [], "message": "submissionIds is required" }
}
```
## 模型列表（已实现）
- 方法：`GET /api/ai-grading/models`
- 描述：返回硅基流动可用模型列表，供前端选择传入。
- 返回示例：
```
{
  "code": 200,
  "message": "success",
  "data": [
    "deepseek-ai/DeepSeek-V3.2-Exp",
    "Pro/deepseek-ai/DeepSeek-V3.2-Exp",
    "Pro/deepseek-ai/DeepSeek-V3.1-Terminus",
    "deepseek-ai/DeepSeek-V3.1-Terminus",
    "Qwen/Qwen3-Next-80B-A3B-Instruct",
    "Qwen/Qwen3-Next-80B-A3B-Thinking",
    "Qwen/Qwen3-30B-A3B-Thinking-2507",
    "Qwen/Qwen3-235B-A22B-Thinking-2507",
    "Qwen/Qwen3-235B-A22B-Instruct-2507",
    "baidu/ERNIE-4.5-300B-A47B",
    "tencent/Hunyuan-A13B-Instruct",
    "Tongyi-Zhiwen/QwenLong-L1-32B",
    "Qwen/Qwen3-30B-A3B",
    "Qwen/Qwen3-32B",
    "Qwen/Qwen3-14B",
    "Qwen/Qwen3-8B",
    "Qwen/Qwen3-235B-A22B",
    "Qwen/QwQ-32B",
    "Pro/deepseek-ai/DeepSeek-R1",
    "Pro/deepseek-ai/DeepSeek-V3",
    "deepseek-ai/DeepSeek-R1",
    "deepseek-ai/DeepSeek-V3",
    "deepseek-ai/DeepSeek-R1-0528-Qwen3-8B",
    "deepseek-ai/DeepSeek-V2.5"
  ]
}
```
- 失败示例：
```
{
  "code": 500,
  "message": "internal server error",
  "data": []
}
```

---

## 数据结构补充

## 作业详情补充
- 返回的 `data` 新增字段：`user`，为该提交对应的 `user` 记录。
- 最新返回结构：
```
{
  "code": 200,
  "message": "success",
  "data": {
    "submission": { assignment_submission 对象 },
    "assignment": { assignment 对象 },
    "student": { student 对象 | null },
    "user": { user 对象 | null },
    "grading": { ai_grading 对象 | 最近记录 },
    "knowledgePoints": [ KnowledgePoint 对象数组 ]
  }
}
```

## 用户管理（新增）
- 方法：`GET /api/ai-grading/users`
- 描述：分页获取用户列表，支持排序。
- 请求参数：
  - `page`、`pageSize`、`sortBy`（支持 `id`、`username`、`email`、`realName`、`role`、`status`、`createTime`、`updateTime`）、`order`（`asc`/`desc`）
- 返回：
```
{ "code": 200, "message": "success", "total": number, "data": [ User 对象数组 ] }
```
- 失败示例：
```
{ "code": 500, "message": "internal server error", "total": 0, "data": [] }
```

- 方法：`GET /api/ai-grading/users/{id}`
- 描述：获取单个用户详情。
- 返回：
```
{ "code": 200, "message": "success", "data": User }
```
- 失败示例：
```
{ "code": 404, "message": "user not found", "data": null }
```

- 方法：`POST /api/ai-grading/users`
- 描述：创建用户。
- 请求体：`User` 对象字段
- 返回：
```
{ "code": 200, "message": "success", "data": User }
```
- 失败示例：
```
{ "code": 400, "message": "username/password is required", "data": null }
```

- 方法：`PUT /api/ai-grading/users/{id}`
- 描述：更新用户。
- 请求体：`User` 对象字段
- 返回：
```
{ "code": 200, "message": "success", "data": User }
```
- 失败示例：
```
{ "code": 404, "message": "user not found", "data": null }
```

- 方法：`DELETE /api/ai-grading/users/{id}`
- 描述：删除用户。
- 返回：
```
{ "code": 200, "message": "success", "data": true }
```
- 失败示例：
```
{ "code": 404, "message": "user not found", "data": false }
```
