# 智慧课程 API 接口文档

**提供方：** 任务一 - 课程大脑（T6-1）  
**使用方：** 任务二 - 学习向导（T6-2）、任务三 - 评估专家（T6-1）  
**文档版本：** v1.0  
**最后更新：** 2025-11-28

---

## 目录

1. [接口说明](#接口说明)
2. [通用说明](#通用说明)
3. [学生端接口（任务二-学习向导）](#学生端接口任务二-学习向导)
4. [教师端接口（任务三-评估专家）](#教师端接口任务三-评估专家)
5. [数据模型定义](#数据模型定义)
6. [错误码说明](#错误码说明)

---

## 接口说明

本文档提供任务一（课程大脑）已实现的API接口，供任务二（学习向导）和任务三（评估专家）调用。

**系统基础URL：** `http://localhost:8080`  
**接口前缀：** 无统一前缀，各模块有独立路径

---

## 通用说明

### 认证方式
所有接口需在请求头中携带Token：
```
Authorization: Bearer {token}
```

### 通用响应格式
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": {}
}
```

### 分页响应格式
```json
{
    "code": 200,
    "msg": "查询成功",
    "total": 100,
    "rows": []
}
```

### 状态码说明
- `200`: 成功
- `401`: 未授权
- `403`: 无权限
- `500`: 服务器错误

---

## 学生端接口（任务二-学习向导）

### 1. 学习数据驾驶舱

#### 1.1 获取我的课程列表
**接口名称：** 获取我的课程列表（当前登录学生）  
**请求方式：** GET  
**请求路径：** `/system/student/my-courses`  
**请求参数：** 无（自动获取当前登录用户）  

**核心入参：** 无  
**核心 API 耦合：** `/course/student/selected/list`  

**响应示例：**
```json
{
    "code": 200,
    "msg": "查询成功",
    "data": [
        {
            "id": 1,
            "courseName": "数据结构",
            "coverImage": "http://...",
            "progress": 65,
            "totalSections": 30,
            "completedSections": 20
        }
    ]
}
```

---

#### 1.2 获取学习雷达图（能力各分析）
**接口名称：** 获取学习雷达图数据  
**请求方式：** GET  
**请求路径：** `/learning/radar/getData`  
**请求参数：**
- `studentId` (必填): Long - 学生ID（唯一）
- `courseId` (选填): Long - 课程ID（值二）

**核心入参：** 请求大脑（唯一）+ 许可选课（值二）  
**核心 API 耦合：** `/course/competency/define`, `/score/kp/mastery`  

**响应说明：**
- 能力各分析：能力点名称、能力分析分分（0-100）
- 能力点覆盖的知识点掌握情况

**响应示例：**
```json
{
    "code": 200,
    "msg": "查询成功",
    "data": {
        "abilities": [
            {
                "abilityName": "基础知识掌握",
                "score": 85,
                "description": "基础概念和原理的掌握程度"
            },
            {
                "abilityName": "算法设计",
                "score": 72,
                "description": "算法设计和分析能力"
            }
        ]
    }
}
```

---

#### 1.3 获取数字孪生分身（正规统计）
**接口名称：** 获取数字孪生分身统计  
**请求方式：** GET  
**请求路径：** `/system/digitalTwin/calculate`  
**请求参数：**
- `studentId` (必填): Long - 学生ID
- `courseId` (选填): Long - 课程ID（值二）

**核心入参：** studentId、courseId（值二）  
**核心 API 耦合：** `/video/learning/behavior/exercise/accuracy`  

**响应说明：**
分配支持功能各类情况：
1. 分配支持实现：根据分钟算分交互实现，返回成绩实现、正规实现等
2. 文本分类：根据分钟算分交互，返回分配支持各实现
3. 正规分析支持：根据行为各数据的行为分析分数，分数各实现统计

**响应示例：**
```json
{
    "code": 200,
    "msg": "查询成功",
    "data": {
        "categoryScores": {
            "正确题目": 45,
            "学习中": 25,
            "薄弱点": 10
        },
        "分配正确率": 81.8,
        "分类实现": 20,
        "分配支持": 5
    }
}
```

---

### 2. 知识图谱导航

#### 2.1 获取课程知识图谱
**接口名称：** 获取课程知识图谱  
**请求方式：** GET  
**请求路径：** `/kpRelation/listByCourse/{courseId}`  
**路径参数：**
- `courseId` (必填): Long - 课程ID

**核心入参：** courseId  
**核心 API 耦合：** `/course/kp/mastery`  

**响应说明：**
- 返回知识点节点和边的关系数据
- 包含知识点掌握状态（已掌握、学习中、薄弱点）

**响应示例：**
```json
{
    "code": 200,
    "msg": "查询成功",
    "data": [
        {
            "id": 1,
            "sourceKpId": 101,
            "sourceKpName": "数据结构基础",
            "targetKpId": 102,
            "targetKpName": "线性表",
            "relationType": "prerequisite",
            "aiGenerated": 1,
            "courseId": 1
        }
    ]
}
```

---

#### 2.2 获取课程知识点列表（带掌握状态）
**接口名称：** 获取课程知识点列表  
**请求方式：** GET  
**请求路径：** `/knowledgePoint/listByCourse/{courseId}`  
**路径参数：**
- `courseId` (必填): Long - 课程ID

**核心入参：** courseId  
**核心 API 耦合：** `/course/kp/mastery` (学生端扩展)  

**响应说明：**
返回课程的所有知识点，学生端需要额外调用掌握度接口标注状态：
- 已掌握（绿色）
- 学习中（黄色）
- 薄弱点（红色）

**响应示例：**
```json
{
    "code": 200,
    "msg": "查询成功",
    "data": [
        {
            "id": 101,
            "kpName": "数据结构基础",
            "description": "数据结构的基本概念",
            "level": "基础",
            "courseId": 1,
            "masteryStatus": "已掌握"
        }
    ]
}
```

---

### 3. 集成并利用知识图谱

#### 3.1 AI驱动的个性化推荐（学习内容推荐）
**接口名称：** 学习内容推荐  
**请求方式：** GET  
**请求路径：** `/system/digitalTwin/calculate`  
**请求参数：**
- `studentId` (必填): Long - 学生ID
- `courseId` (必填): Long - 课程ID

**核心入参：** studentId、courseId  
**核心 API 耦合：** 可与LLM结合实现规则可见规则  

**响应说明：**
系统综合分析学生的：
1. 调度数据（调度子项目二的API）
2. 行为数据（如视频/资源上行为数据）
3. 任务完成情况

通过LLM分析以上数据，结合知识图谱环图，动态生成学习建议。

**推荐逻辑示例：**
- "您在图论经络知识点上表现较弱，建议参新学习第3.2节视频，并完成配套专项练习。"
- "检测到您已掌握链表，建议挑战更新资料：建议重新学习第3节课配套练习'。"

**响应示例：**
```json
{
    "code": 200,
    "msg": "推荐成功",
    "data": {
        "recommendations": [
            {
                "type": "video",
                "title": "您在图图知识点上表现较弱",
                "reason": "检测到您在图图相关题目正确率较低（45%）",
                "resource": {
                    "id": 203,
                    "type": "视频",
                    "title": "图论基础 - 深度优先遍历",
                    "url": "/section/detail/15"
                }
            },
            {
                "type": "exercise",
                "title": "建议完成配套专项练习",
                "reason": "巩固薄弱知识点",
                "resource": {
                    "id": 305,
                    "type": "习题",
                    "title": "图论专项练习",
                    "url": "/assignment/detail/8"
                }
            }
        ]
    }
}
```

---

### 4. 课程任务列表

#### 4.1 获取课程章节列表
**接口名称：** 获取课程章节列表  
**请求方式：** GET  
**请求路径：** `/section/listByCourse/{courseId}`  
**路径参数：**
- `courseId` (必填): Long - 课程ID

**核心入参：** courseId  
**核心 API 耦合：** 无  

**响应示例：**
```json
{
    "code": 200,
    "msg": "查询成功",
    "data": [
        {
            "id": 10,
            "sectionName": "第1章 数据结构概述",
            "description": "介绍数据结构的基本概念",
            "orderNum": 1,
            "courseId": 1,
            "videoUrl": "http://..."
        }
    ]
}
```

---

#### 4.2 获取课程作业列表
**接口名称：** 获取课程作业列表  
**请求方式：** GET  
**请求路径：** `/assignment/listByCourse/{courseId}`  
**路径参数：**
- `courseId` (必填): Long - 课程ID

**核心入参：** courseId  
**核心 API 耦合：** 无  

**响应示例：**
```json
{
    "code": 200,
    "msg": "查询成功",
    "data": [
        {
            "id": 5,
            "assignmentName": "数据结构第一次作业",
            "description": "完成线性表相关习题",
            "deadline": "2025-12-15 23:59:59",
            "courseId": 1,
            "status": "未提交"
        }
    ]
}
```

---

### 5. 资源中心

#### 5.1 根据知识点ID查询关联资源
**接口名称：** 根据知识点ID查询关联资源  
**请求方式：** GET  
**请求路径：** `/courseResource/byKnowledgePoint/{kpId}`  
**路径参数：**
- `kpId` (必填): Long - 知识点ID

**核心入参：** kpId  
**核心 API 耦合：** 无  

**响应示例：**
```json
{
    "code": 200,
    "msg": "查询成功",
    "data": [
        {
            "id": 201,
            "resourceName": "线性表PPT",
            "resourceType": "PPT",
            "resourceUrl": "http://...",
            "kpIds": [102, 103],
            "uploadTime": "2025-11-20"
        }
    ]
}
```

---

## 教师端接口（任务三-评估专家）

### 1. 成绩管理

#### 1.1 获取学生成绩列表
**接口名称：** 获取学生成绩列表  
**请求方式：** GET  
**请求路径：** `/course/student/scores`  
**请求参数：**
- `courseId` (必填): Long - 课程ID
- `pageNum` (选填): int - 页码，默认1
- `pageSize` (选填): int - 每页数量，默认10

**核心入参：** courseId（唯一）+ 许可选项（值二）  
**核心 API 耦合：** `/score/list` (需要新增)  

**响应示例：**
```json
{
    "code": 200,
    "msg": "查询成功",
    "total": 45,
    "rows": [
        {
            "studentId": 1001,
            "studentName": "张三",
            "averageScore": 85.5,
            "assignmentCount": 5,
            "completedCount": 4,
            "videoProgress": 80
        }
    ]
}
```

---

### 2. 学情分析

#### 2.1 获取知识点错误统计
**接口名称：** 获取知识点错误统计  
**请求方式：** GET  
**请求路径：** `/course/kpErrorStats`  
**请求参数：**
- `courseId` (选填): Long - 课程ID
- `targetDate` (选填): String - 目标日期 (格式: yyyy-MM-dd)

**核心入参：** courseId（值二）+ 许可日期（值二）  
**核心 API 耦合：** `/course/competency/define`, `/score/kp/mastery`  

**响应说明：**
返回班级整体在各知识点上的错题统计数据，帮助教师识别学生普遍薄弱的知识点。

**响应示例：**
```json
{
    "code": 200,
    "msg": "查询成功",
    "data": [
        {
            "kpId": 102,
            "kpName": "线性表",
            "errorCount": 35,
            "totalAttempts": 120,
            "errorRate": 29.17
        },
        {
            "kpId": 105,
            "kpName": "图的遍历",
            "errorCount": 28,
            "totalAttempts": 90,
            "errorRate": 31.11
        }
    ]
}
```

---

#### 2.2 视频学习分析（视频学习热力图与完成率统计）
**接口名称：** 获取今日活跃人数统计  
**请求方式：** GET  
**请求路径：** `/video/learning/todayActiveStats`  
**请求参数：** 无（自动获取当前教师的课程）

**核心入参：** 无（自动识别当前用户）  
**核心 API 耦合：** `/video/learning/behavior/exercise/accuracy`  

**响应示例：**
```json
{
    "code": 200,
    "msg": "查询成功",
    "data": {
        "todayActiveUsers": 28,
        "totalUsers": 45,
        "activeRate": 62.22
    }
}
```

**扩展说明：**
任务三可以基于此接口扩展实现：
- 视频热力图：统计视频各时间段的观看人数分布
- 视频完成率：统计每个视频的完成情况
- 学习时长统计：统计学生的累计学习时长

---

### 3. 题库管理

#### 3.1 获取题库列表（待实现）
**接口名称：** 获取题库列表  
**请求方式：** GET  
**请求路径：** `/question/list`  
**请求参数：**
- `courseId` (选填): Long - 课程ID
- `kpId` (选填): Long - 知识点ID
- `questionType` (选填): String - 题目类型（单选、多选、判断、简答）
- `pageNum` (选填): int - 页码
- `pageSize` (选填): int - 每页数量

**核心入参：** courseId（值二）+ 许可选项  
**核心 API 耦合：** 需要任务三自行实现  

**响应说明：**
此接口由任务三实现，任务一暂未提供。建议数据模型包含：
- 题目ID、题目内容、题目类型
- 关联知识点ID列表
- 难度级别、标准答案、解析

---

#### 3.2 智能组卷（待实现）
**接口名称：** 智能组卷  
**请求方式：** POST  
**请求路径：** `/question/smartPaper`  
**请求参数：**
```json
{
    "courseId": 1,
    "kpIds": [101, 102, 103],
    "difficulty": "中等",
    "questionCount": 20,
    "questionTypes": ["单选", "多选", "简答"]
}
```

**核心入参：** courseId、知识点ID列表、难度、题目数量  
**核心 API 耦合：** `/knowledgePoint/listByCourse/{courseId}`  

**响应说明：**
此接口由任务三实现，建议集成LLM实现智能组卷：
- 根据知识点分布均衡选题
- 根据难度梯度分配题目
- 考虑知识点依赖关系

---

### 4. 智能批改与反馈

#### 4.1 学生报告智能批改（待实现）
**接口名称：** 学生报告智能批改  
**请求方式：** POST  
**请求路径：** `/assignment/aiCorrect`  
**请求参数：**
```json
{
    "assignmentId": 5,
    "studentId": 1001,
    "submissionContent": "作业内容或文件URL",
    "fileType": "pdf"
}
```

**核心入参：** 作业ID、学生ID、提交内容、文件类型  
**核心 API 耦合：** 与LLM集成  

**响应说明：**
使用LLM API对学生提交的简答题、报告类作业（支持PDF/Word）进行智能批改，生成个性化评语和建议。

**批改维度示例：**
- 内容相关性：是否紧扣主题
- 逻辑结构：逻辑是否清晰
- 知识覆盖度：知识点覆盖情况
- 创新性：是否有独到见解

**响应示例：**
```json
{
    "code": 200,
    "msg": "批改完成",
    "data": {
        "score": 85,
        "feedback": "论述清晰，但在图论经络知识点上表现较弱...",
        "suggestions": [
            "论点请晰，但需更新经络图的遍历算法的深入分析",
            "建议参新教新教新算法复杂度分析相关资料"
        ],
        "dimensionScores": {
            "内容相关性": 90,
            "逻辑结构": 85,
            "知识覆盖度": 80,
            "创新性": 75
        }
    }
}
```

---

### 5. 课程与知识点管理

#### 5.1 获取课程列表
**接口名称：** 获取课程列表（当前教师的课程）  
**请求方式：** GET  
**请求路径：** `/course/list`  
**请求参数：** 无（自动获取当前登录教师的课程）

**核心入参：** 无（自动识别当前用户）  
**核心 API 耦合：** 无  

**响应示例：**
```json
{
    "code": 200,
    "msg": "查询成功",
    "total": 3,
    "rows": [
        {
            "id": 1,
            "courseName": "数据结构",
            "description": "数据结构基础课程",
            "coverImage": "http://...",
            "teacherUserId": 2001
        }
    ]
}
```

---

#### 5.2 获取课程知识点列表
**接口名称：** 获取课程知识点列表  
**请求方式：** GET  
**请求路径：** `/knowledgePoint/listByCourse/{courseId}`  
**路径参数：**
- `courseId` (必填): Long - 课程ID

**核心入参：** courseId  
**核心 API 耦合：** 无  

**响应示例：**
```json
{
    "code": 200,
    "msg": "查询成功",
    "data": [
        {
            "id": 101,
            "kpName": "数据结构基础",
            "description": "数据结构的基本概念",
            "level": "基础",
            "courseId": 1
        }
    ]
}
```

---

#### 5.3 AI生成知识图谱
**接口名称：** AI生成课程知识点关系图谱  
**请求方式：** POST  
**请求路径：** `/kpRelation/generateGraph/{courseId}`  
**路径参数：**
- `courseId` (必填): Long - 课程ID

**核心入参：** courseId  
**核心 API 耦合：** 与LLM集成，调用`/knowledgePoint/listByCourse/{courseId}`  

**响应说明：**
系统调用LLM API，自动分析课程知识点，生成知识点之间的关系（前置知识、属于子节点、相关知识等），并保存到数据库。

**响应示例：**
```json
{
    "code": 200,
    "msg": "知识图谱生成成功",
    "data": "成功生成15个知识点，28条关系边"
}
```

---

## 数据模型定义

### Course（课程）
```json
{
    "id": "Long - 课程ID",
    "courseName": "String - 课程名称",
    "description": "String - 课程描述",
    "coverImage": "String - 封面图片URL",
    "teacherUserId": "Long - 教师用户ID",
    "createTime": "Date - 创建时间"
}
```

### KnowledgePoint（知识点）
```json
{
    "id": "Long - 知识点ID",
    "kpName": "String - 知识点名称",
    "description": "String - 知识点描述",
    "level": "String - 难度级别（基础/进阶/高级）",
    "courseId": "Long - 所属课程ID",
    "creatorUserId": "Long - 创建者ID"
}
```

### KpRelation（知识点关系）
```json
{
    "id": "Long - 关系ID",
    "sourceKpId": "Long - 源知识点ID",
    "sourceKpName": "String - 源知识点名称",
    "targetKpId": "Long - 目标知识点ID",
    "targetKpName": "String - 目标知识点名称",
    "relationType": "String - 关系类型（prerequisite-前置知识, contains-包含, related-相关）",
    "aiGenerated": "Integer - 是否AI生成（0-否，1-是）",
    "courseId": "Long - 所属课程ID"
}
```

### CourseResource（课程资源）
```json
{
    "id": "Long - 资源ID",
    "resourceName": "String - 资源名称",
    "resourceType": "String - 资源类型（视频/PPT/文档/习题）",
    "resourceUrl": "String - 资源URL",
    "kpIds": "List<Long> - 关联知识点ID列表",
    "uploadUserId": "Long - 上传者ID",
    "uploadTime": "Date - 上传时间"
}
```

### Section（章节）
```json
{
    "id": "Long - 章节ID",
    "sectionName": "String - 章节名称",
    "description": "String - 章节描述",
    "orderNum": "Integer - 排序号",
    "courseId": "Long - 所属课程ID",
    "videoUrl": "String - 视频URL"
}
```

### Assignment（作业）
```json
{
    "id": "Long - 作业ID",
    "assignmentName": "String - 作业名称",
    "description": "String - 作业描述",
    "deadline": "Date - 截止时间",
    "courseId": "Long - 所属课程ID",
    "status": "String - 状态（未提交/已提交/已批改）"
}
```

---

## 错误码说明

| 错误码 | 说明 | 解决方案 |
|--------|------|----------|
| 200 | 操作成功 | - |
| 401 | 未授权，Token失效或未提供 | 重新登录获取Token |
| 403 | 无权限访问该资源 | 检查用户角色权限 |
| 404 | 资源不存在 | 检查请求的ID是否正确 |
| 500 | 服务器内部错误 | 联系管理员查看服务器日志 |
| 10001 | 参数校验失败 | 检查必填参数是否完整 |
| 10002 | 课程不存在 | 检查courseId是否正确 |
| 10003 | 知识点不存在 | 检查kpId是否正确 |
| 10004 | LLM API调用失败 | 检查API配置或稍后重试 |

---

## 使用示例

### 示例1：学生端获取学习推荐

```javascript
// 1. 获取课程知识点列表
const kpResponse = await axios.get('/knowledgePoint/listByCourse/1');

// 2. 获取学习推荐
const recommendResponse = await axios.get('/system/digitalTwin/calculate', {
    params: {
        studentId: 1001,
        courseId: 1
    }
});

// 3. 结合知识图谱展示
const graphResponse = await axios.get('/kpRelation/listByCourse/1');
```

### 示例2：教师端智能批改

```javascript
// 1. 获取作业列表
const assignmentResponse = await axios.get('/assignment/listByCourse/1');

// 2. 提交批改请求
const correctResponse = await axios.post('/assignment/aiCorrect', {
    assignmentId: 5,
    studentId: 1001,
    submissionContent: 'PDF文件URL',
    fileType: 'pdf'
});

// 3. 查看批改结果
console.log(correctResponse.data.feedback);
```

---

## 附录

### A. 接口依赖关系图

```
任务一（课程大脑）
    ↓ 提供接口
任务二（学习向导） ← 使用课程、知识点、关系数据
任务三（评估专家） ← 使用课程、知识点、成绩数据
```

### B. 数据库表关系

主要表：
- `course` - 课程表
- `knowledge_point` - 知识点表
- `kp_relation` - 知识点关系表
- `course_resource` - 课程资源表
- `section` - 章节表
- `assignment` - 作业表
- `video_learning_behavior` - 视频学习行为表

### C. 联系方式

如有疑问，请联系任务一开发团队：
- 邮箱：project-team@example.com
- 文档更新日期：2025-11-28

---

**文档结束**
