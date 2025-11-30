# 学情分析 API 接口文档

本文档概述了学情分析模块的后端 API，涵盖学生成绩表现、学习风险、视频参与度以及知识点掌握情况。

## 1. 成绩表现分析 (`/api/analysis`)

### 1.1 获取成绩分析
获取学生在某门课程中的综合成绩分析，包括知识点得分、薄弱环节和成绩趋势。

- **URL**: `/api/analysis/performance`
- **Method**: `GET`
- **Parameters**:
  - `courseId` (Long, 必填): 课程 ID。
  - `studentUserId` (Long, 必填): 学生用户 ID。
  - `weakThreshold` (Double, 选填): 判定薄弱知识点的阈值 (0-1)。

- **Response**: `Result<PerformanceAnalysisResponse>`
  - `knowledgeScores`: 知识点得分列表。
  - `weakKnowledge`: 薄弱知识点列表。
  - `scoreTrend`: 成绩趋势点列表 (日期, 得分比率)。
  - `overallAverageScore`: 整体平均得分 (0-1)。
  - `suggestions`: 学习建议列表。

## 2. 学习风险分析 (`/analysis/risk`)

### 2.1 获取学生风险列表
评估并列出学生的具体学习风险，支持按课程或知识点筛选。

- **URL**: `/analysis/risk/list`
- **Method**: `GET`
- **Parameters**:
  - `courseId` (Long, 选填): 按课程筛选。
  - `studentId` (Long, 选填): 按学生筛选。
  - `kpId` (Long, 选填): 按知识点筛选。

- **Response**: `Result<StudentRiskResponse>`
  - `totalRisks`: 识别出的风险总数。
  - `riskTypeCount`: 各类型风险的计数映射。
  - `riskLevelCount`: 各等级风险的计数映射。
  - `risks`: 详细的风险项 (`RiskItem`) 列表。

### 2.2 获取风险报告
生成课程级别的风险报告，识别高风险学生和共性薄弱环节。

- **URL**: `/analysis/risk/report`
- **Method**: `GET`
- **Parameters**:
  - `courseId` (Long, 选填): 课程 ID。

- **Response**: `Result<RiskReportResponse>`
  - `riskTypeCount`: 风险类型的汇总计数。
  - `kpRiskList`: 关联风险的知识点列表。
  - `highRiskStudents`: 被标记为高风险的学生列表。
  - `overallLowMasteryRatio`: 低掌握度学生比例。
  - `summary`: 报告的文本摘要。

## 3. 视频分析 (`/analysis/video`)

### 3.1 视频热力图
获取视频参与度的热力图数据，展示视频中哪些部分被观看最频繁。

- **URL**: `/analysis/video/heatmap`
- **Method**: `GET`
- **Parameters**:
  - `videoId` (Long, 必填): 视频 ID。
  - `studentId` (Long, 选填): 筛选特定学生。

- **Response**: `Result<VideoHeatmapResponse>`
  - `heatmapBuckets`: 时间区间与观看次数的列表。
  - `totalWatchDuration`: 总观看时长。
  - `averageCompletionRate`: 平均完课率。

### 3.2 完课统计
获取课程中视频的完课统计数据。

- **URL**: `/analysis/video/completion`
- **Method**: `GET`
- **Parameters**:
  - `courseId` (Long, 选填)
  - `videoId` (Long, 选填)
  - `studentId` (Long, 选填)

- **Response**: `Result<VideoCompletionStatsResponse>`
  - `completionRate`: 完成观看的百分比。
  - `averageCompletionRate`: 平均进度。
  - `completionDistribution`: 完课率分布情况。

### 3.3 行为相关性分析
分析视频观看行为与成绩表现之间的相关性。

- **URL**: `/analysis/video/behavior-correlation`
- **Method**: `GET`
- **Parameters**:
  - `courseId` (Long, 选填)

- **Response**: `Result<BehaviorPerformanceCorrelationResponse>`
  - `completionRateCorrelation`: 完课率与成绩的相关系数。
  - `durationCorrelation`: 观看时长与成绩的相关系数。
  - `samples`: 用于分析的学生数据样本列表。

## 4. 知识点掌握度 (`/api/mastery`)

### 4.1 学生掌握度
获取学生在课程中各知识点的掌握水平。

- **URL**: `/api/mastery/student`
- **Method**: `GET`
- **Parameters**:
  - `courseId` (Long, 选填)
  - `studentUserId` (Long, 必填)

- **Response**: `Result<List<MasteryDTO>>`
  - `MasteryDTO` 列表:
    - `kpId`: 知识点 ID。
    - `kpTitle`: 标题。
    - `masteryScore`: 掌握度分数 (0-1)。
    - `masteryStatus`: 状态 (例如 "良好", "薄弱")。
    - `trend`: 趋势方向。
    - `masteryDescription`: 文本描述。

### 4.2 班级掌握度
获取班级的整体掌握情况。

- **URL**: `/api/mastery/class`
- **Method**: `GET`
- **Parameters**:
  - `courseId` (Long, 选填)
  - `classId` (Long, 必填)
  - `lowScoreThreshold` (Double, 选填)

- **Response**: `Result<ClassMasteryResponse>`
  - `kpStats`: 每个知识点的聚合统计。
  - `weakKps`: 班级薄弱知识点列表。
  - `overallAverageScore`: 班级平均掌握度分数。
  - `suggestions`: 教学建议。
