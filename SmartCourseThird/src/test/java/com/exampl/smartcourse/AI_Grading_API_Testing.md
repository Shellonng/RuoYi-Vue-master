# AI 批改模块接口测试指南

- 目标：验证已实现的 AI 批改模块接口，覆盖触发、列表、查询、教师确认、以及分页排序。
- 基础地址：`http://localhost:8080`

## 环境准备
- JDK：建议使用 JDK 17（若编译报版本 61.0/52.0 错误，请切换到 JDK 17）
- 数据库：`application.properties` 已指向本地 MySQL，写入至少一条 `assignment`（`mode='file'`）与对应 `assignment_submission`（`status=1`）记录
- 文件路径映射：
  - `smartcourse.file.storage.mode=local|server`
  - `smartcourse.file.storage.local-base=D:/githubRepository/SmartCourseThird/submissions`
  - `smartcourse.file.storage.server-base=/var/smartcourse/submissions`
  - 数据库保存相对路径，如：`/submissions/101/student1_error_analysis.pdf`
- LLM 环境变量：`SILICONFLOW_API_KEY`
  - PowerShell 设置：`setx SILICONFLOW_API_KEY "<你的密钥>"`

## 启动服务
- 跳过测试编译启动：
```
./mvnw.cmd -DskipTests spring-boot:run
```

## 接口测试用例

### 1) 模型列表
- URL：`GET /api/ai-grading/models`
- 请求头：`Accept: application/json`
- 请求体：无
- 成功返回：
```
{
  "code": 200,
  "message": "success",
  "data": [ "modelName", ... ]
}
```
  - 字段说明：
    - `code`：状态码，固定 200 表示成功
    - `message`：文本消息，`success`
    - `data`：模型名称字符串数组
- 失败返回：通常无（接口总是返回 200）。若服务异常，将返回 5xx（无固定结构）
- cURL：
```
curl http://localhost:8080/api/ai-grading/models
```

### 2) 智能批改触发（单条，异步）
- URL：`POST /api/ai-grading/grading/trigger`
- 请求头：`Content-Type: application/json`
- 请求体：
```
{ "submissionId": number, "llmModel": string(optional) }
```
  - 字段说明：
    - `submissionId`：提交记录 ID，必填
    - `llmModel`：模型名称，可选，不填使用默认模型
- 成功返回（HTTP 200）：
```
{
  "code": 200,
  "message": "success",
  "data": { "accepted": [ number ], "failed": [ ], "message": "trigger done" }
}
```
  - 字段说明：
    - `code`：固定 200
    - `message`：`success`
    - `data.accepted`：触发成功受理的 `submissionId` 列表
    - `data.failed`：触发失败的 `submissionId` 列表
    - `data.message`：执行结果文本
- 失败返回（HTTP 200，业务失败）：
```
{
  "code": 200,
  "message": "success",
  "data": { "accepted": [ ], "failed": [ number ], "message": "no submissionId(s) provided 或 其他错误" }
}
```
- cURL：
```
curl -X POST http://localhost:8080/api/ai-grading/grading/trigger \
  -H "Content-Type: application/json" \
  -d "{\"submissionId\": 123, \"llmModel\": \"Qwen/Qwen3-8B\"}"
```

### 3) 批量批改触发（异步）
- URL：`POST /api/ai-grading/grading/batch`
- 请求头：`Content-Type: application/json`
- 请求体：
```
{ "submissionIds": [ number, ... ], "llmModel": string(optional) }
```
  - 字段说明：
    - `submissionIds`：提交记录 ID 列表，必填
    - `llmModel`：模型名称，可选
- 成功返回（HTTP 200）：
```
{
  "code": 200,
  "message": "success",
  "data": { "accepted": [ number, ... ], "failed": [ number, ... ], "message": "trigger done" }
}
```
- 失败返回（HTTP 200，业务失败）：
```
{
  "code": 200,
  "message": "success",
  "data": { "accepted": [ ], "failed": [ number, ... ], "message": "no submissionId(s) provided 或 其他错误" }
}
```
- cURL：
```
curl -X POST http://localhost:8080/api/ai-grading/grading/batch \
  -H "Content-Type: application/json" \
  -d "{\"submissionIds\": [123,124], \"llmModel\": \"Qwen/Qwen3-8B\"}"
```

### 4) 处理中/失败列表
- URL：`GET /api/ai-grading/unresolved`
- 请求头：`Accept: application/json`
- 请求体：无
- 成功返回（HTTP 200）：
```
{
  "code": 200,
  "message": "success",
  "data": [ {
    "submission": AssignmentSubmission,
    "assignment": Assignment,
    "student": Student|null,
    "grading": AiGrading(最新),
    "knowledgePoints": [ KnowledgePoint, ... ]
  }, ... ]
}
```
- 失败返回：通常无（返回空数组表示无数据）
- cURL：
```
curl http://localhost:8080/api/ai-grading/unresolved
```

### 5) 批改结果展示
- URL：`GET /api/ai-grading/grading/{submissionId}/result`
- 请求头：`Accept: application/json`
- 路径参数：`submissionId`（提交记录 ID）
- 成功返回（HTTP 200）：
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
    "improvementSuggestions": string(JSON),
    "coveredKnowledgePoints": string(JSON),
    "missingKnowledgePoints": string(JSON),
    "llmModel": string,
    "teacherConfirmed": boolean,
    "teacherModifiedScore": number|null,
    "teacherComment": string|null,
    "confirmedBy": number|null,
    "confirmedAt": string|null,
    "createdAt": string,
    "updatedAt": string
  }
}
```
- 空数据返回（HTTP 200）：
```
{
  "code": 200,
  "message": "success",
  "data": null
}
```
- cURL：
```
curl http://localhost:8080/api/ai-grading/grading/123/result
```

### 6) 教师修改与确认
- URL：`POST /api/ai-grading/grading/{submissionId}/confirm?teacherId={teacherId}`
- 请求头：`Content-Type: application/json`
- 路径参数：`submissionId`
- 查询参数：`teacherId`（必填）
- 请求体（可选字段）：
```
{
  "contentScore"?: number,
  "logicScore"?: number,
  "knowledgeScore"?: number,
  "innovationScore"?: number,
  "totalScore"?: number,
  "aiComment"?: string,
  "improvementSuggestions"?: string(JSON),
  "coveredKnowledgePoints"?: string(JSON),
  "missingKnowledgePoints"?: string(JSON),
  "llmModel"?: string,
  "teacherComment"?: string
}
```
- 成功返回（HTTP 200）：
```
{
  "code": 200,
  "message": "success",
  "data": AiGrading
}
```
  - 关键字段：`teacherConfirmed=1`、`teacherModifiedScore`、`teacherComment`、`confirmedBy`、`confirmedAt`、`status=completed`
- 失败返回（HTTP 400）：缺少 `teacherId` 时由框架返回 400（无固定结构）
- cURL：
```
curl -X POST "http://localhost:8080/api/ai-grading/grading/123/confirm?teacherId=456" \
  -H "Content-Type: application/json" \
  -d "{\"totalScore\": 95.0, \"teacherComment\": \"补充评语\"}"
```

### 7) 批改历史查询
- URL：`GET /api/ai-grading/grading/{submissionId}/history`
- 请求头：`Accept: application/json`
- 路径参数：`submissionId`
- 成功返回（HTTP 200）：
```
{
  "code": 200,
  "message": "success",
  "data": [ AiGrading, ... ]
}
```
- 失败返回：通常无（空数组表示无数据）
- cURL：
```
curl http://localhost:8080/api/ai-grading/grading/123/history
```

### 8) 待批改作业列表
- URL：`GET /api/ai-grading/pending`
- 请求头：`Accept: application/json`
- 查询参数（可选）：`courseId`、`assignmentId`、`studentUserId`、`keyword`
- 成功返回（HTTP 200）：
```
{
  "code": 200,
  "message": "success",
  "data": [ {
    "submission": AssignmentSubmission,
    "assignment": Assignment,
    "student": Student|null,
    "grading": AiGrading|null,
    "knowledgePoints": [ KnowledgePoint, ... ]
  }, ... ]
}
```
- 失败返回：通常无（空数组表示无数据）
- cURL：
```
curl "http://localhost:8080/api/ai-grading/pending?courseId=1&keyword=算法"
```

### 9) 作业列表（分页/排序）
- URL：`GET /api/ai-grading/assignments`
- 请求头：`Accept: application/json`
- 查询参数：`page`、`pageSize`、`sortBy`、`order`
- 成功返回（HTTP 200）：
```
{
  "code": 200,
  "message": "success",
  "total": number,
  "data": [ Assignment, ... ]
}
```
- 失败返回：通常无（返回空数组和 `total` 表示 0）
- cURL：
```
curl "http://localhost:8080/api/ai-grading/assignments?page=1&pageSize=20&sortBy=startTime&order=desc"
```

### 10) 提交列表（学生+提交，分页/排序）
- URL：`GET /api/ai-grading/submissions`
- 请求头：`Accept: application/json`
- 查询参数：`page`、`pageSize`、`sortBy`、`order`
- 成功返回（HTTP 200）：
```
{
  "code": 200,
  "message": "success",
  "total": number,
  "data": [ { "student": Student, "submission": AssignmentSubmission }, ... ]
}
```
- 失败返回：通常无（返回空数组和 `total` 表示 0）
- cURL：
```
curl "http://localhost:8080/api/ai-grading/submissions?page=1&pageSize=20&sortBy=submitTime&order=desc"
```

## 文件路径映射验证
- 本地模式示例：
  - DB 路径：`/submissions/101/student1_error_analysis.pdf`
  - 实际读取：`D:/githubRepository/SmartCourseThird/submissions/101/student1_error_analysis.pdf`
- 切换服务器模式：
  - 配置：`smartcourse.file.storage.mode=server`
  - 根路径：`smartcourse.file.storage.server-base=/var/smartcourse/submissions`

## 常见问题
- 编译报 JDK 版本错误
  - 现象：`类文件具有错误的版本 61.0, 应为 52.0`
  - 解决：将 JDK 切换至 17，并使用 `./mvnw.cmd -DskipTests spring-boot:run`
- 触发失败
  - 缺少 `SILICONFLOW_API_KEY` 或文件路径不存在时，后台会将 `status=failed` 并写入 `error_message`，可通过 `/api/ai-grading/unresolved` 查看