# 任务二（学习向导）API 接口依赖明细表

**提供方：** 任务一 - 课程大脑（T6-1）  
**使用方：** 任务二 - 学习向导（T6-2）  
**文档版本：** v1.0  
**最后更新：** 2025-11-28

---

## 接口明细表

| 接口模块 | 接口名称 | 请求方式 | 请求路径 | 体验组 | 依赖组 API 耦合 | 核心入参 | 核心出参 |
|---------|---------|---------|---------|--------|----------------|---------|---------|
| 学习数据驾驶舱 | 获取我的课程列表 | GET | /system/student/my-courses | 请求大脑（唯一） | /course/student/selected/list | 无（自动获取当前用户） | 课程ID、课程名、封面、进度、总章节、完成章节 |
| 学习数据驾驶舱 | 获取能力雷达图数据 | GET | /learning/radar/getData | 请求大脑（唯一）+ 许可课程ID（值二） | /course/competency/define<br/>/score/kp/mastery | studentId、courseId | 能力各分析：能力点名称、能力分析分分（0-100） |
| 学习数据驾驶舱 | 获取数字孪生分身 | GET | /system/digitalTwin/calculate | 请求大脑（唯一）+ 许可课程ID（值二） | /video/learning/behavior<br/>/exercise/accuracy | studentId、courseId | 分类关注：已掌握、正规实现数、薄弱点数、正规正确率 |
| 知识图谱导航 | 获取课程知识图谱 | GET | /kpRelation/listByCourse/{courseId} | 课程ID（必填） | /course/kp/mastery | courseId | 知识点节点、关系边、关系类型、掌握状态 |
| 知识图谱导航 | 获取知识点列表（带状态） | GET | /knowledgePoint/listByCourse/{courseId} | 课程ID（必填） | /course/kp/mastery（学生端扩展） | courseId | 知识点ID、名称、描述、难度、掌握状态 |
| AI个性化推荐 | 学习内容推荐 | GET | /system/learning/recommend | 请求大脑（唯一）+ 课程ID | 与LLM结合<br/>调用子项目二成绩API | studentId、courseId | 推荐列表：类型、标题、原因、资源信息 |
| 课程任务列表 | 获取课程章节列表 | GET | /section/listByCourse/{courseId} | 课程ID（必填） | 无 | courseId | 章节ID、章节名、描述、排序、视频URL |
| 课程任务列表 | 获取课程作业列表 | GET | /assignment/listByCourse/{courseId} | 课程ID（必填） | 无 | courseId | 作业ID、作业名、描述、截止时间、状态 |
| 资源中心 | 根据知识点查询资源 | GET | /courseResource/byKnowledgePoint/{kpId} | 知识点ID（必填） | 无 | kpId | 资源ID、资源名、资源类型、URL、关联知识点 |

---

# 任务三（评估专家）API 接口依赖明细表

**提供方：** 任务一 - 课程大脑（T6-1）  
**使用方：** 任务三 - 评估专家（T6-1）  
**文档版本：** v1.0  
**最后更新：** 2025-11-28

---

## 接口明细表

| 接口模块 | 接口名称 | 请求方式 | 请求路径 | 体验组 | 依赖组 API 耦合 | 核心入参 | 核心出参 |
|---------|---------|---------|---------|--------|----------------|---------|---------|
| 成绩管理 | 获取学生成绩列表 | GET | /course/student/scores | 课程ID（必填）+ 分页参数（选填） | /score/list（待新增） | courseId、pageNum、pageSize | 学生ID、姓名、平均分、作业数、完成数、视频进度 |
| 学情分析 | 获取知识点错误统计 | GET | /course/kpErrorStats | 课程ID（选填）+ 目标日期（选填） | /course/competency/define<br/>/score/kp/mastery | courseId、targetDate | 知识点ID、名称、错误数、总尝试数、错误率 |
| 学情分析 | 获取今日活跃统计 | GET | /video/learning/todayActiveStats | 无（自动识别当前教师） | /video/learning/behavior<br/>/exercise/accuracy | 无 | 今日活跃人数、总人数、活跃率 |
| 学情分析 | 视频学习热力图（扩展） | GET | /video/learning/heatmap | 课程ID + 视频ID | /video/learning/behavior | courseId、videoId | 时间段、观看人数、完成率分布 |
| 题库管理 | 获取题库列表 | GET | /question/list | 课程ID + 知识点ID（选填） | 待任务三实现 | courseId、kpId、questionType | 题目ID、内容、类型、知识点、难度、答案 |
| 题库管理 | 智能组卷 | POST | /question/smartPaper | 课程ID + 知识点列表 + 难度 | /knowledgePoint/listByCourse/{courseId}<br/>与LLM集成 | courseId、kpIds、difficulty、questionCount | 试卷ID、题目列表、知识点分布、难度分布 |
| 智能批改 | 学生报告智能批改 | POST | /assignment/aiCorrect | 作业ID + 学生ID + 内容 | 与LLM API集成 | assignmentId、studentId、submissionContent、fileType | 分数、反馈、建议、维度分数 |
| 课程管理 | 获取课程列表 | GET | /course/list | 无（自动获取当前教师） | 无 | 无 | 课程ID、课程名、描述、封面、教师ID |
| 课程管理 | 获取课程知识点列表 | GET | /knowledgePoint/listByCourse/{courseId} | 课程ID（必填） | 无 | courseId | 知识点ID、名称、描述、难度级别、课程ID |
| 课程管理 | AI生成知识图谱 | POST | /kpRelation/generateGraph/{courseId} | 课程ID（必填） | /knowledgePoint/listByCourse/{courseId}<br/>与LLM集成 | courseId | 生成结果：知识点数、关系边数 |

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

### 任务二 - 学习向导关键接口

#### 1. 获取我的课程列表
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

#### 2. 获取能力雷达图数据
**请求参数：**
- `studentId` (必填): Long - 学生ID
- `courseId` (选填): Long - 课程ID

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

#### 3. 获取数字孪生分身
**请求参数：**
- `studentId` (必填): Long - 学生ID
- `courseId` (选填): Long - 课程ID

**响应示例：**
```json
{
    "code": 200,
    "msg": "查询成功",
    "data": {
        "masteredCount": 45,
        "learningCount": 25,
        "weakCount": 10,
        "masteryRate": 81.8,
        "totalKps": 80,
        "needReviewCount": 5
    }
}
```

#### 4. 获取课程知识图谱
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

#### 5. AI个性化推荐
**响应示例：**
```json
{
    "code": 200,
    "msg": "推荐成功",
    "data": {
        "recommendations": [
            {
                "type": "video",
                "title": "您在图论知识点上表现较弱",
                "reason": "检测到您在图论相关题目正确率较低（45%）",
                "resource": {
                    "id": 203,
                    "type": "视频",
                    "title": "图论基础 - 深度优先遍历",
                    "url": "/section/detail/15"
                }
            }
        ]
    }
}
```

---

### 任务三 - 评估专家关键接口

#### 1. 获取学生成绩列表
**请求参数：**
- `courseId` (必填): Long - 课程ID
- `pageNum` (选填): int - 页码，默认1
- `pageSize` (选填): int - 每页数量，默认10

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

#### 2. 获取知识点错误统计
**请求参数：**
- `courseId` (选填): Long - 课程ID
- `targetDate` (选填): String - 目标日期 (格式: yyyy-MM-dd)

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
        }
    ]
}
```

#### 3. 视频学习活跃统计
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

#### 4. 学生报告智能批改
**请求参数：**
```json
{
    "assignmentId": 5,
    "studentId": 1001,
    "submissionContent": "作业内容或文件URL",
    "fileType": "pdf"
}
```

**响应示例：**
```json
{
    "code": 200,
    "msg": "批改完成",
    "data": {
        "score": 85,
        "feedback": "论述清晰，但在图论知识点上需要加强...",
        "suggestions": [
            "论点清晰，但需要对图的遍历算法进行深入分析",
            "建议参考教材第5章算法复杂度分析相关内容"
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

#### 5. AI生成知识图谱
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

---

## 使用示例

### 示例1：学生端获取学习推荐
```javascript
// 1. 获取课程知识点列表
const kpResponse = await axios.get('/knowledgePoint/listByCourse/1');

// 2. 获取学习推荐
const recommendResponse = await axios.get('/system/learning/recommend', {
    params: { studentId: 1001, courseId: 1 }
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
```

---

**文档结束**
