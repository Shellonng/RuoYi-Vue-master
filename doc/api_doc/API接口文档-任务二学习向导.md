# 任务二（学习向导）API 接口依赖明细表

**提供方：** 任务一 - 课程大脑（T6-1）  
**使用方：** 任务二 - 学习向导（T6-2）  
**文档版本：** v1.0  
**最后更新：** 2025-11-28

---

## 接口明细表

| 接口模块 | 接口名称 | 请求方式 | 请求路径 | 依赖组 | 依赖组 API 耦合 | 核心入参 | 核心出参 |
|---------|---------|---------|---------|--------|----------------|---------|---------|
| 学习数据驾驶舱 | 获取我的课程列表 | GET | /system/student/my-courses | 课程大脑（组一） | /course/student/selected/list | 无（自动获取当前用户） | 课程ID、课程名、封面、进度、总章节、完成章节 |
| 学习数据驾驶舱 | 获取能力雷达图数据 | GET | /learning/radar/getData | 课程大脑（组一）+ 评估专家（组三） | /course/competency/define<br/>/score/kp/mastery | studentId、courseId | 能力各分析：能力点名称、能力分析分分（0-100） |
| 学习数据驾驶舱 | 获取数字孪生分身 | GET | /system/digitalTwin/calculate | 课程大脑（组一）+ 评估专家（组三） | /video/learning/behavior<br/>/exercise/accuracy | studentId、courseId | 分类关注：已掌握、正规实现数、薄弱点数、正规正确率 |
| 知识图谱导航 | 获取课程知识图谱 | GET | /kpRelation/listByCourse/{courseId} | 课程大脑（组一） | /course/kp/mastery | courseId | 知识点节点、关系边、关系类型、掌握状态 |
| 知识图谱导航 | 获取知识点列表（带状态） | GET | /knowledgePoint/listByCourse/{courseId} | 课程大脑（组一）+ 评估专家（组三） | /course/kp/mastery（学生端扩展） | courseId | 知识点ID、名称、描述、难度、掌握状态 |
| AI个性化推荐 | 学习内容推荐 | GET | /system/learning/recommend | 课程大脑（组一）+ 评估专家（组三） | 与LLM结合<br/>调用子项目二成绩API | studentId、courseId | 推荐列表：类型、标题、原因、资源信息 |
| 课程任务列表 | 获取课程章节列表 | GET | /section/listByCourse/{courseId} | 课程大脑（组一） | 无 | courseId | 章节ID、章节名、描述、排序、视频URL |
| 课程任务列表 | 获取课程作业列表 | GET | /assignment/listByCourse/{courseId} | 课程大脑（组一） | 无 | courseId | 作业ID、作业名、描述、截止时间、状态 |
| 资源中心 | 根据知识点查询资源 | GET | /courseResource/byKnowledgePoint/{kpId} | 课程大脑（组一） | 无 | kpId | 资源ID、资源名、资源类型、URL、关联知识点 |

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

### 1. 获取我的课程列表
**请求路径：** `/system/student/my-courses`  
**请求方式：** GET  
**请求参数：** 无（自动获取当前登录用户）

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

### 2. 获取能力雷达图数据
**请求路径：** `/learning/radar/getData`  
**请求方式：** GET  
**请求参数：**
- `studentId` (必填): Long - 学生ID
- `courseId` (选填): Long - 课程ID

**功能说明：**
- 能力各分析：能力点名称、能力分析分数（0-100）
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
            },
            {
                "abilityName": "数据结构应用",
                "score": 78,
                "description": "数据结构的实际应用能力"
            }
        ]
    }
}
```

---

### 3. 获取数字孪生分身
**请求路径：** `/system/digitalTwin/calculate`  
**请求方式：** GET  
**请求参数：**
- `studentId` (必填): Long - 学生ID
- `courseId` (选填): Long - 课程ID

**功能说明：**
分类支持多种统计情况：
1. 分类统计：根据掌握程度分类，返回已掌握、学习中、薄弱点等
2. 正确率分析：根据练习数据分析，返回正确率统计
3. 学习进度统计：根据行为数据分析学习进度

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

---

### 4. 获取课程知识图谱
**请求路径：** `/kpRelation/listByCourse/{courseId}`  
**请求方式：** GET  
**路径参数：**
- `courseId` (必填): Long - 课程ID

**功能说明：**
- 返回知识点节点和边的关系数据
- 包含知识点掌握状态（已掌握、学习中、薄弱点）
- 支持可视化展示（ECharts、G6等）

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
        },
        {
            "id": 2,
            "sourceKpId": 102,
            "sourceKpName": "线性表",
            "targetKpId": 103,
            "targetKpName": "栈和队列",
            "relationType": "prerequisite",
            "aiGenerated": 1,
            "courseId": 1
        }
    ]
}
```

**关系类型说明：**
- `prerequisite`: 前置知识关系
- `contains`: 包含关系
- `related`: 相关知识关系

---

### 5. 获取知识点列表（带掌握状态）
**请求路径：** `/knowledgePoint/listByCourse/{courseId}`  
**请求方式：** GET  
**路径参数：**
- `courseId` (必填): Long - 课程ID

**功能说明：**
返回课程的所有知识点，学生端需要额外调用掌握度接口标注状态：
- 已掌握（绿色）：正确率 >= 80%
- 学习中（黄色）：正确率 50% - 79%
- 薄弱点（红色）：正确率 < 50%

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
            "masteryStatus": "已掌握",
            "masteryRate": 85.5
        },
        {
            "id": 102,
            "kpName": "线性表",
            "description": "顺序表和链表的实现",
            "level": "基础",
            "courseId": 1,
            "masteryStatus": "学习中",
            "masteryRate": 65.0
        },
        {
            "id": 105,
            "kpName": "图的遍历",
            "description": "深度优先和广度优先遍历",
            "level": "进阶",
            "courseId": 1,
            "masteryStatus": "薄弱点",
            "masteryRate": 45.0
        }
    ]
}
```

---

### 6. AI个性化推荐
**请求路径：** `/system/learning/recommend`  
**请求方式：** GET  
**请求参数：**
- `studentId` (必填): Long - 学生ID
- `courseId` (必填): Long - 课程ID

**功能说明：**
系统综合分析学生的：
1. 成绩数据（调用子项目二的API）
2. 行为数据（如视频/资源上的行为数据）
3. 任务完成情况

通过LLM分析以上数据，结合知识图谱关系，动态生成学习建议。

**推荐逻辑示例：**
- "您在图论知识点上表现较弱，建议重新学习第3.2节视频，并完成配套专项练习。"
- "检测到您已掌握链表，建议挑战更高难度：树结构相关内容。"

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
            },
            {
                "type": "challenge",
                "title": "挑战更高难度",
                "reason": "您在线性表知识点掌握良好（正确率85%）",
                "resource": {
                    "id": 210,
                    "type": "视频",
                    "title": "树结构 - 二叉树进阶",
                    "url": "/section/detail/20"
                }
            }
        ]
    }
}
```

---

### 7. 获取课程章节列表
**请求路径：** `/section/listByCourse/{courseId}`  
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
            "id": 10,
            "sectionName": "第1章 数据结构概述",
            "description": "介绍数据结构的基本概念",
            "orderNum": 1,
            "courseId": 1,
            "videoUrl": "http://...",
            "duration": 1800,
            "completed": true
        },
        {
            "id": 11,
            "sectionName": "第2章 线性表",
            "description": "顺序表和链表的实现",
            "orderNum": 2,
            "courseId": 1,
            "videoUrl": "http://...",
            "duration": 2400,
            "completed": false
        }
    ]
}
```

---

### 8. 获取课程作业列表
**请求路径：** `/assignment/listByCourse/{courseId}`  
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
            "id": 5,
            "assignmentName": "数据结构第一次作业",
            "description": "完成线性表相关习题",
            "deadline": "2025-12-15 23:59:59",
            "courseId": 1,
            "status": "未提交",
            "score": null
        },
        {
            "id": 6,
            "assignmentName": "数据结构第二次作业",
            "description": "完成栈和队列相关习题",
            "deadline": "2025-12-20 23:59:59",
            "courseId": 1,
            "status": "已提交",
            "score": 85
        }
    ]
}
```

**状态说明：**
- `未提交`: 学生尚未提交作业
- `已提交`: 学生已提交，待批改
- `已批改`: 教师已批改完成

---

### 9. 根据知识点查询资源
**请求路径：** `/courseResource/byKnowledgePoint/{kpId}`  
**请求方式：** GET  
**路径参数：**
- `kpId` (必填): Long - 知识点ID

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
            "uploadTime": "2025-11-20",
            "fileSize": "2.5MB"
        },
        {
            "id": 202,
            "resourceName": "线性表习题集",
            "resourceType": "文档",
            "resourceUrl": "http://...",
            "kpIds": [102],
            "uploadTime": "2025-11-21",
            "fileSize": "1.2MB"
        }
    ]
}
```

**资源类型说明：**
- `视频`: 教学视频
- `PPT`: 课件
- `文档`: PDF/Word文档
- `习题`: 练习题集

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
    "progress": "Integer - 学习进度（0-100）",
    "totalSections": "Integer - 总章节数",
    "completedSections": "Integer - 已完成章节数",
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
    "masteryStatus": "String - 掌握状态（已掌握/学习中/薄弱点）",
    "masteryRate": "Double - 掌握率（0-100）",
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
    "uploadTime": "Date - 上传时间",
    "fileSize": "String - 文件大小"
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
    "videoUrl": "String - 视频URL",
    "duration": "Integer - 视频时长（秒）",
    "completed": "Boolean - 是否已完成"
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
    "status": "String - 状态（未提交/已提交/已批改）",
    "score": "Integer - 成绩（0-100）"
}
```

---

## 使用示例

### 示例1：学生端首页数据加载
```javascript
// 1. 获取我的课程列表
const courseResponse = await axios.get('/system/student/my-courses', {
    headers: { 'Authorization': `Bearer ${token}` }
});

// 2. 选择某个课程，获取知识图谱
const courseId = courseResponse.data[0].id;
const graphResponse = await axios.get(`/kpRelation/listByCourse/${courseId}`);

// 3. 获取知识点列表（带掌握状态）
const kpResponse = await axios.get(`/knowledgePoint/listByCourse/${courseId}`);

// 4. 获取能力雷达图数据
const radarResponse = await axios.get('/learning/radar/getData', {
    params: { studentId: 1001, courseId: courseId }
});
```

### 示例2：学生端获取学习推荐
```javascript
// 1. 获取数字孪生分身统计
const twinResponse = await axios.get('/system/digitalTwin/calculate', {
    params: { studentId: 1001, courseId: 1 }
});

// 2. 获取AI推荐
const recommendResponse = await axios.get('/system/learning/recommend', {
    params: { studentId: 1001, courseId: 1 }
});

// 3. 展示推荐资源
recommendResponse.data.recommendations.forEach(rec => {
    console.log(`推荐类型: ${rec.type}`);
    console.log(`推荐标题: ${rec.title}`);
    console.log(`推荐理由: ${rec.reason}`);
    console.log(`资源: ${rec.resource.title}`);
});
```

### 示例3：知识图谱可视化
```javascript
// 1. 获取知识图谱数据
const graphData = await axios.get('/kpRelation/listByCourse/1');

// 2. 获取知识点列表（带掌握状态）
const kpList = await axios.get('/knowledgePoint/listByCourse/1');

// 3. 构建图谱节点和边
const nodes = kpList.data.map(kp => ({
    id: kp.id,
    name: kp.kpName,
    status: kp.masteryStatus,
    color: kp.masteryStatus === '已掌握' ? 'green' : 
           kp.masteryStatus === '学习中' ? 'yellow' : 'red'
}));

const edges = graphData.data.map(rel => ({
    source: rel.sourceKpId,
    target: rel.targetKpId,
    type: rel.relationType
}));

// 4. 使用G6或ECharts渲染知识图谱
renderKnowledgeGraph(nodes, edges);
```

---

## 注意事项

### 1. 认证与权限
- 所有接口都需要携带有效的JWT Token
- 学生只能访问自己的数据
- 跨课程访问会进行权限校验

### 2. 数据刷新频率
- 掌握状态建议每次进入页面时刷新
- 知识图谱可以缓存，课程内容更新时刷新
- AI推荐建议每天最多调用3次（避免频繁调用LLM）

### 3. 性能优化建议
- 使用分页加载大数据量列表
- 知识图谱数据可以前端缓存
- 推荐结果可以缓存1小时

### 4. 错误处理
- 接口调用失败时，前端应提供友好提示
- Token过期时，自动跳转到登录页
- 数据加载失败时，提供重试按钮

---

## 联系方式

如有疑问，请联系任务一开发团队：
- 项目负责人：课程大脑团队
- 文档更新日期：2025-11-28

---

**文档结束**
