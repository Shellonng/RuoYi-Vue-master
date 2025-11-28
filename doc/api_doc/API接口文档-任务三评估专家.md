# 任务三（评估专家）API 接口依赖明细表

**提供方：** 任务一 - 课程大脑（T6-1）  
**使用方：** 任务三 - 评估专家（T6-1）  
**文档版本：** v1.0  
**最后更新：** 2025-11-28

---

## 接口明细表

| 接口模块 | 接口名称 | 请求方式 | 请求路径 | 依赖组 | 依赖组 API 耦合 | 核心入参 | 核心出参 |
|---------|---------|---------|---------|--------|----------------|---------|---------|
| 成绩管理 | 获取学生成绩列表 | GET | /course/student/scores | 课程大脑（组一） | /score/list（待新增） | courseId、pageNum、pageSize | 学生ID、姓名、平均分、作业数、完成数、视频进度 |
| 学情分析 | 获取知识点错误统计 | GET | /course/kpErrorStats | 课程大脑（组一） | /course/competency/define<br/>/score/kp/mastery | courseId、targetDate | 知识点ID、名称、错误数、总尝试数、错误率 |
| 学情分析 | 获取今日活跃统计 | GET | /video/learning/todayActiveStats | 课程大脑（组一） | /video/learning/behavior<br/>/exercise/accuracy | 无 | 今日活跃人数、总人数、活跃率 |
| 学情分析 | 视频学习热力图（扩展） | GET | /video/learning/heatmap | 课程大脑（组一） | /video/learning/behavior | courseId、videoId | 时间段、观看人数、完成率分布 |
| 题库管理 | 获取题库列表 | GET | /question/list | 评估专家（组三）自行实现 | 待任务三实现 | courseId、kpId、questionType | 题目ID、内容、类型、知识点、难度、答案 |
| 题库管理 | 智能组卷 | POST | /question/smartPaper | 课程大脑（组一）+ 评估专家（组三） | /knowledgePoint/listByCourse/{courseId}<br/>与LLM集成 | courseId、kpIds、difficulty、questionCount | 试卷ID、题目列表、知识点分布、难度分布 |
| 智能批改 | 学生报告智能批改 | POST | /assignment/aiCorrect | 评估专家（组三）+ LLM API | 与LLM API集成 | assignmentId、studentId、submissionContent、fileType | 分数、反馈、建议、维度分数 |
| 课程管理 | 获取课程列表 | GET | /course/list | 课程大脑（组一） | 无 | 无 | 课程ID、课程名、描述、封面、教师ID |
| 课程管理 | 获取课程知识点列表 | GET | /knowledgePoint/listByCourse/{courseId} | 课程大脑（组一） | 无 | courseId | 知识点ID、名称、描述、难度级别、课程ID |
| 课程管理 | AI生成知识图谱 | POST | /kpRelation/generateGraph/{courseId} | 课程大脑（组一）+ LLM API | /knowledgePoint/listByCourse/{courseId}<br/>与LLM集成 | courseId | 生成结果：知识点数、关系边数 |

---

## 通用说明

### 1. 认证方式
所有接口需在请求头中携带Token：
```
Authorization: Bearer {token}
```

### 2. 通用响应格式
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": {}
}
```

### 3. 分页响应格式
```json
{
    "code": 200,
    "msg": "查询成功",
    "total": 100,
    "rows": []
}
```

### 4. 状态码说明
- `200`: 成功
- `401`: 未授权
- `403`: 无权限
- `500`: 服务器错误

---

## 接口详细说明

### 1. 获取学生成绩列表
**请求路径：** `/course/student/scores`  
**请求方式：** GET  
**请求参数：**
- `courseId` (必填): Long - 课程ID
- `pageNum` (选填): int - 页码，默认1
- `pageSize` (选填): int - 每页数量，默认10

**功能说明：**
获取指定课程下所有学生的成绩概览，包括作业完成情况、平均分、视频学习进度等。

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
            "studentNumber": "2023001",
            "averageScore": 85.5,
            "assignmentCount": 5,
            "completedCount": 4,
            "videoProgress": 80,
            "lastActiveTime": "2025-11-27 15:30:00"
        },
        {
            "studentId": 1002,
            "studentName": "李四",
            "studentNumber": "2023002",
            "averageScore": 78.0,
            "assignmentCount": 5,
            "completedCount": 5,
            "videoProgress": 95,
            "lastActiveTime": "2025-11-28 09:15:00"
        }
    ]
}
```

---

### 2. 获取知识点错误统计
**请求路径：** `/course/kpErrorStats`  
**请求方式：** GET  
**请求参数：**
- `courseId` (选填): Long - 课程ID
- `targetDate` (选填): String - 目标日期 (格式: yyyy-MM-dd)

**功能说明：**
返回班级整体在各知识点上的错题统计数据，帮助教师识别学生普遍薄弱的知识点。支持按日期筛选，分析学习趋势。

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
            "errorRate": 29.17,
            "studentCount": 45,
            "avgErrorPerStudent": 0.78
        },
        {
            "kpId": 105,
            "kpName": "图的遍历",
            "errorCount": 28,
            "totalAttempts": 90,
            "errorRate": 31.11,
            "studentCount": 45,
            "avgErrorPerStudent": 0.62
        },
        {
            "kpId": 108,
            "kpName": "动态规划",
            "errorCount": 42,
            "totalAttempts": 95,
            "errorRate": 44.21,
            "studentCount": 45,
            "avgErrorPerStudent": 0.93
        }
    ]
}
```

**使用建议：**
- 错误率 > 40%：需要重点讲解
- 错误率 30%-40%：建议补充练习
- 错误率 < 30%：学生掌握良好

---

### 3. 获取今日活跃统计
**请求路径：** `/video/learning/todayActiveStats`  
**请求方式：** GET  
**请求参数：** 无（自动获取当前教师的课程）

**功能说明：**
统计今日学生学习活跃度，包括观看视频、完成作业、参与讨论等行为。

**响应示例：**
```json
{
    "code": 200,
    "msg": "查询成功",
    "data": {
        "todayActiveUsers": 28,
        "totalUsers": 45,
        "activeRate": 62.22,
        "videoWatchCount": 156,
        "assignmentSubmitCount": 23,
        "avgStudyTime": 45.5
    }
}
```

---

### 4. 视频学习热力图（扩展功能）
**请求路径：** `/video/learning/heatmap`  
**请求方式：** GET  
**请求参数：**
- `courseId` (必填): Long - 课程ID
- `videoId` (必填): Long - 视频ID

**功能说明：**
统计视频各时间段的观看人数分布，生成热力图数据，帮助教师了解：
- 哪些部分学生反复观看（可能是难点）
- 哪些部分学生快进跳过（可能是冗余）
- 完整观看率统计

**响应示例：**
```json
{
    "code": 200,
    "msg": "查询成功",
    "data": {
        "videoId": 15,
        "videoTitle": "图论基础 - 深度优先遍历",
        "totalDuration": 1800,
        "totalViews": 45,
        "completionRate": 73.33,
        "heatmapData": [
            {
                "timeStart": 0,
                "timeEnd": 300,
                "viewCount": 45,
                "avgWatchCount": 1.2,
                "skipRate": 5.0
            },
            {
                "timeStart": 300,
                "timeEnd": 600,
                "viewCount": 43,
                "avgWatchCount": 2.3,
                "skipRate": 8.0
            },
            {
                "timeStart": 600,
                "timeEnd": 900,
                "viewCount": 38,
                "avgWatchCount": 1.8,
                "skipRate": 15.0
            }
        ]
    }
}
```

**数据说明：**
- `viewCount`: 该时间段有多少人观看
- `avgWatchCount`: 平均重复观看次数（>1.5表示难点）
- `skipRate`: 跳过率（>20%可能内容冗余）

---

### 5. 获取题库列表（待实现）
**请求路径：** `/question/list`  
**请求方式：** GET  
**请求参数：**
- `courseId` (选填): Long - 课程ID
- `kpId` (选填): Long - 知识点ID
- `questionType` (选填): String - 题目类型（单选、多选、判断、简答）
- `difficulty` (选填): String - 难度（简单、中等、困难）
- `pageNum` (选填): int - 页码
- `pageSize` (选填): int - 每页数量

**功能说明：**
此接口由任务三实现，任务一暂未提供。建议数据模型包含：
- 题目ID、题目内容、题目类型
- 关联知识点ID列表
- 难度级别、标准答案、解析

**建议响应格式：**
```json
{
    "code": 200,
    "msg": "查询成功",
    "total": 150,
    "rows": [
        {
            "id": 301,
            "questionContent": "以下关于线性表的说法，正确的是？",
            "questionType": "单选",
            "difficulty": "简单",
            "kpIds": [102],
            "options": [
                {"key": "A", "value": "线性表必须顺序存储"},
                {"key": "B", "value": "线性表可以顺序存储或链式存储"},
                {"key": "C", "value": "线性表只能链式存储"},
                {"key": "D", "value": "线性表不能为空"}
            ],
            "answer": "B",
            "explanation": "线性表既可以采用顺序存储结构，也可以采用链式存储结构。"
        }
    ]
}
```

---

### 6. 智能组卷
**请求路径：** `/question/smartPaper`  
**请求方式：** POST  
**请求参数：**
```json
{
    "courseId": 1,
    "kpIds": [101, 102, 103, 105],
    "difficulty": "中等",
    "questionCount": 20,
    "questionTypes": {
        "单选": 10,
        "多选": 5,
        "简答": 5
    },
    "totalScore": 100
}
```

**功能说明：**
此接口由任务三实现，建议集成LLM实现智能组卷：
- 根据知识点分布均衡选题
- 根据难度梯度分配题目
- 考虑知识点依赖关系（先考基础，再考进阶）
- 避免重复选题

**建议响应格式：**
```json
{
    "code": 200,
    "msg": "组卷成功",
    "data": {
        "paperId": "PAPER_2025112801",
        "paperName": "数据结构综合测试",
        "totalScore": 100,
        "questionCount": 20,
        "estimatedTime": 90,
        "questions": [
            {
                "id": 301,
                "questionContent": "...",
                "questionType": "单选",
                "score": 5,
                "kpIds": [102]
            }
        ],
        "kpDistribution": {
            "线性表": 5,
            "栈和队列": 5,
            "图的遍历": 10
        },
        "difficultyDistribution": {
            "简单": 6,
            "中等": 10,
            "困难": 4
        }
    }
}
```

---

### 7. 学生报告智能批改
**请求路径：** `/assignment/aiCorrect`  
**请求方式：** POST  
**请求参数：**
```json
{
    "assignmentId": 5,
    "studentId": 1001,
    "submissionContent": "作业内容或文件URL",
    "fileType": "pdf",
    "questionList": [
        {
            "questionId": 1,
            "questionContent": "请简述图的深度优先遍历算法原理",
            "studentAnswer": "深度优先遍历（DFS）是一种图遍历算法..."
        }
    ]
}
```

**功能说明：**
使用LLM API对学生提交的简答题、报告类作业（支持PDF/Word）进行智能批改，生成个性化评语和建议。

**批改维度：**
- 内容相关性（25分）：是否紧扣主题
- 逻辑结构（25分）：逻辑是否清晰
- 知识覆盖度（30分）：知识点覆盖情况
- 创新性（20分）：是否有独到见解

**响应示例：**
```json
{
    "code": 200,
    "msg": "批改完成",
    "data": {
        "assignmentId": 5,
        "studentId": 1001,
        "score": 85,
        "feedback": "论述清晰，对深度优先遍历算法的原理理解准确。但在时间复杂度分析部分略显简单，建议补充更详细的分析过程。",
        "suggestions": [
            "论点清晰，但需要对图的遍历算法进行更深入的时间复杂度分析",
            "建议补充DFS与BFS的对比分析",
            "可以结合具体实例说明算法的应用场景"
        ],
        "dimensionScores": {
            "内容相关性": 23,
            "逻辑结构": 22,
            "知识覆盖度": 25,
            "创新性": 15
        },
        "questionScores": [
            {
                "questionId": 1,
                "score": 85,
                "feedback": "回答准确，理解深刻",
                "highlights": ["算法原理正确", "步骤清晰"],
                "improvements": ["缺少复杂度分析"]
            }
        ],
        "kpMastery": [
            {
                "kpId": 105,
                "kpName": "图的遍历",
                "masteryLevel": "良好",
                "suggestion": "建议进一步学习图的应用"
            }
        ]
    }
}
```

---

### 8. 获取课程列表
**请求路径：** `/course/list`  
**请求方式：** GET  
**请求参数：** 无（自动获取当前登录教师的课程）

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
            "teacherUserId": 2001,
            "studentCount": 45,
            "createTime": "2025-09-01",
            "status": "进行中"
        },
        {
            "id": 2,
            "courseName": "算法设计",
            "description": "算法设计与分析",
            "coverImage": "http://...",
            "teacherUserId": 2001,
            "studentCount": 38,
            "createTime": "2025-09-01",
            "status": "进行中"
        }
    ]
}
```

---

### 9. 获取课程知识点列表
**请求路径：** `/knowledgePoint/listByCourse/{courseId}`  
**请求方式：** GET  
**路径参数：**
- `courseId` (必填): Long - 课程ID

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
            "orderNum": 1,
            "parentKpId": null
        },
        {
            "id": 102,
            "kpName": "线性表",
            "description": "顺序表和链表的实现",
            "level": "基础",
            "courseId": 1,
            "orderNum": 2,
            "parentKpId": 101
        }
    ]
}
```

---

### 10. AI生成知识图谱
**请求路径：** `/kpRelation/generateGraph/{courseId}`  
**请求方式：** POST  
**路径参数：**
- `courseId` (必填): Long - 课程ID

**功能说明：**
系统调用LLM API，自动分析课程知识点，生成知识点之间的关系（前置知识、包含关系、相关知识等），并保存到数据库。

**处理流程：**
1. 获取课程的所有知识点
2. 调用LLM分析知识点之间的关系
3. 生成关系数据并保存
4. 返回生成结果统计

**响应示例：**
```json
{
    "code": 200,
    "msg": "知识图谱生成成功",
    "data": {
        "summary": "成功生成15个知识点，28条关系边",
        "kpCount": 15,
        "relationCount": 28,
        "relationTypes": {
            "prerequisite": 12,
            "contains": 8,
            "related": 8
        },
        "generateTime": "2025-11-28 10:30:00",
        "aiModel": "gpt-4"
    }
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
    "studentCount": "Integer - 学生人数",
    "createTime": "Date - 创建时间",
    "status": "String - 状态（进行中/已结束）"
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
    "orderNum": "Integer - 排序号",
    "parentKpId": "Long - 父知识点ID",
    "creatorUserId": "Long - 创建者ID"
}
```

### StudentScore（学生成绩）
```json
{
    "studentId": "Long - 学生ID",
    "studentName": "String - 学生姓名",
    "studentNumber": "String - 学号",
    "averageScore": "Double - 平均分",
    "assignmentCount": "Integer - 作业总数",
    "completedCount": "Integer - 已完成作业数",
    "videoProgress": "Integer - 视频学习进度（0-100）",
    "lastActiveTime": "Date - 最后活跃时间"
}
```

### KpErrorStats（知识点错误统计）
```json
{
    "kpId": "Long - 知识点ID",
    "kpName": "String - 知识点名称",
    "errorCount": "Integer - 错误次数",
    "totalAttempts": "Integer - 总尝试次数",
    "errorRate": "Double - 错误率（%）",
    "studentCount": "Integer - 参与学生数",
    "avgErrorPerStudent": "Double - 人均错误次数"
}
```

---

## 使用示例

### 示例1：教师端仪表盘数据加载
```javascript
// 1. 获取课程列表
const courseResponse = await axios.get('/course/list', {
    headers: { 'Authorization': `Bearer ${token}` }
});

// 2. 选择某个课程，获取学生成绩
const courseId = courseResponse.data.rows[0].id;
const scoreResponse = await axios.get('/course/student/scores', {
    params: { courseId: courseId, pageNum: 1, pageSize: 50 }
});

// 3. 获取知识点错误统计
const errorStats = await axios.get('/course/kpErrorStats', {
    params: { courseId: courseId }
});

// 4. 获取今日活跃统计
const activeStats = await axios.get('/video/learning/todayActiveStats');
```

### 示例2：智能批改作业
```javascript
// 1. 获取待批改作业列表
const assignmentList = await axios.get('/assignment/listByCourse/1');

// 2. 选择某个作业，获取学生提交
const submissions = await axios.get('/assignment/submissions', {
    params: { assignmentId: 5 }
});

// 3. 批量AI批改
for (const submission of submissions.data) {
    const correctResult = await axios.post('/assignment/aiCorrect', {
        assignmentId: 5,
        studentId: submission.studentId,
        submissionContent: submission.content,
        fileType: 'pdf'
    });
    
    console.log(`学生 ${submission.studentName} 批改完成，得分：${correctResult.data.score}`);
}
```

### 示例3：生成知识图谱
```javascript
// 1. 获取课程知识点列表
const kpList = await axios.get('/knowledgePoint/listByCourse/1');

// 2. AI生成知识图谱
const graphResult = await axios.post('/kpRelation/generateGraph/1');

console.log(graphResult.data.summary);
// 输出: "成功生成15个知识点，28条关系边"

// 3. 查看生成的知识图谱
const graphData = await axios.get('/kpRelation/listByCourse/1');
```

---

## 注意事项

### 1. 认证与权限
- 所有接口都需要携带有效的JWT Token
- 教师只能访问自己教授的课程数据
- 不允许跨课程访问其他教师的数据

### 2. LLM API调用限制
- AI批改建议使用队列异步处理
- 智能组卷每天限制调用10次
- 知识图谱生成建议在课程初始化时调用一次

### 3. 性能优化建议
- 学生成绩列表使用分页加载
- 知识点错误统计可以每日凌晨预计算
- 视频热力图数据可以缓存1小时

### 4. 数据统计更新频率
- 今日活跃统计：实时更新
- 知识点错误统计：每小时更新
- 成绩排名：每次作业批改后更新

### 5. 错误处理
- LLM API调用失败时，应提供降级方案
- 批改超时时，应返回部分结果
- 数据加载失败时，提供重试机制

---

## 联系方式

如有疑问，请联系任务一开发团队：
- 项目负责人：课程大脑团队
- 文档更新日期：2025-11-28

---

**文档结束**
