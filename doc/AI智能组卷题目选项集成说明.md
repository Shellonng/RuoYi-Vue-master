# AI 智能组卷 - 题目选项集成说明

## 📋 问题描述

AI 生成的组卷结果中，选择题的 `options` 字段为空数组，因为：
1. AI 匹配的是题目ID
2. 题目内容存储在 `question` 表
3. 选项内容存储在 `question_option` 表（需要关联查询）

## 🔧 解决方案

### 方案一：后端 API 自动关联选项（推荐）

修改 `getQuestionDetail(id)` API，确保返回数据包含选项：

```java
// QuestionController.java
@GetMapping("/api/questions/{id}")
public R getQuestionDetail(@PathVariable Long id) {
    // 1. 查询题目基本信息
    Question question = questionService.getById(id);
    
    // 2. 查询题目的所有选项（从 question_option 表）
    List<QuestionOption> options = questionOptionService.list(
        new QueryWrapper<QuestionOption>()
            .eq("question_id", id)
            .orderByAsc("option_order")
    );
    
    // 3. 组装返回数据
    QuestionVO vo = new QuestionVO();
    vo.setQuestionId(question.getQuestionId());
    vo.setContent(question.getContent());
    vo.setType(question.getType());
    vo.setDifficulty(question.getDifficulty());
    vo.setScore(question.getScore());
    vo.setOptions(options); // ⬅️ 关键：包含选项
    
    return R.ok(vo);
}
```

### 方案二：AI 返回完整数据

在 AI Agent 生成组卷结果时，查询题目并立即获取选项：

```python
# whisper_server.py 或相关文件
async def generate_paper(course_id, requirements):
    # AI 匹配题目
    matched_questions = match_questions_by_ai(requirements)
    
    # 为每道题目获取选项
    enriched_questions = []
    for q_id in matched_questions:
        # 查询题目
        question = db.query("SELECT * FROM question WHERE question_id = %s", q_id)
        
        # 查询选项
        options = db.query(
            "SELECT * FROM question_option WHERE question_id = %s ORDER BY option_order",
            q_id
        )
        
        # 组装完整数据
        enriched_questions.append({
            "question_id": question["question_id"],
            "content": question["content"],
            "type": question["type"],
            "difficulty": question["difficulty"],
            "score": question["score"],
            "options": [opt["content"] for opt in options]  # ⬅️ 包含选项
        })
    
    return {
        "total_questions": len(enriched_questions),
        "questions": enriched_questions
    }
```

## 🎯 数据库表结构

```sql
-- 题目表
CREATE TABLE `question` (
  `question_id` bigint PRIMARY KEY,
  `content` text NOT NULL COMMENT '题目内容',
  `type` varchar(20) COMMENT '题型: single/multiple/blank/short',
  `difficulty` int COMMENT '难度 1-5',
  `score` decimal(5,2) COMMENT '分值',
  ...
);

-- 选项表
CREATE TABLE `question_option` (
  `option_id` bigint PRIMARY KEY,
  `question_id` bigint NOT NULL COMMENT '关联题目ID',
  `option_order` char(1) COMMENT '选项序号 A/B/C/D',
  `content` text NOT NULL COMMENT '选项内容',
  `is_correct` tinyint(1) COMMENT '是否正确答案',
  ...
);
```

## 📊 期望的返回数据格式

### AI 生成的组卷结果

```json
{
  "total_questions": 6,
  "total_score": 100,
  "average_difficulty": 3.5,
  "questions": [
    {
      "question_id": 60029,
      "content": "卷积神经网络（CNN）中的池化层主要作用是？",
      "type": "single",
      "difficulty": 3,
      "score": 10,
      "options": [
        "增加特征图的尺寸",
        "减少特征图的尺寸，降低计算量",
        "增加模型的参数数量",
        "直接输出分类结果"
      ]
    },
    {
      "question_id": 60032,
      "content": "神经网络中的激活函数作用是？",
      "type": "single",
      "difficulty": 3,
      "score": 10,
      "options": [
        "引入非线性特性",
        "加速训练过程",
        "防止过拟合",
        "增加模型容量"
      ]
    }
  ]
}
```

## 🚀 前端已完成的改进

### 1. 自动获取缺失的选项
```javascript
// 如果题目有ID但没有options，自动调用API获取
async enrichQuestionsWithOptions(questions) {
  for (const q of questions) {
    if (q.question_id && (!q.options || q.options.length === 0)) {
      // 调用 getQuestionDetail(q.question_id) 获取完整数据
      const fullQuestion = await fetchQuestionWithOptions(q.question_id)
      // fullQuestion 包含从 question_option 表查询的选项
    }
  }
}
```

### 2. 支持多种选项格式
```javascript
// 支持字符串数组
options: ["选项A", "选项B", "选项C", "选项D"]

// 支持对象数组
options: [
  { content: "选项A", is_correct: true },
  { content: "选项B", is_correct: false }
]
```

### 3. 友好的错误提示
- 如果选择题没有选项，显示红色警告
- 简答题等不需要选项的题型不显示警告

## ✅ 验证步骤

### 1. 测试后端API
```bash
curl http://localhost:8083/api/questions/60029
```

期望返回：
```json
{
  "code": 200,
  "data": {
    "question_id": 60029,
    "content": "...",
    "options": [
      {"content": "选项A"},
      {"content": "选项B"}
    ]
  }
}
```

### 2. 测试前端显示
1. 打开 AI 智能组卷对话框
2. 生成试卷
3. 点击"预览试卷"
4. 查看浏览器控制台：
   ```
   [预览] 题目 60029 缺少选项，从后端获取...
   [预览] 获取题目 60029 成功: {...}
   [预览] 题目 1 的选项: ["选项A", "选项B", ...]
   ```

## 🎨 最终效果

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                试卷预览
    总分：100分 | 试题数：6道 | 平均难度：3.5/5
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

 1. [单选题] 卷积神经网络（CNN）中的池化层主要作用是？
    A. 增加特征图的尺寸
    B. 减少特征图的尺寸，降低计算量
    C. 增加模型的参数数量
    D. 直接输出分类结果
    📊 难度：3/5    💯 分值：10分

 2. [多选题] Transformer 架构的优势包括？
    A. 并行计算能力强
    B. 能够捕获长距离依赖
    C. 参数量小
    D. 训练速度快
    📊 难度：4/5    💯 分值：15分
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

## 📝 总结

**前端**：已完成，会自动调用 `getQuestionDetail(id)` 获取选项

**后端**：需要确保 `getQuestionDetail(id)` API 返回数据包含 `question_option` 表的选项

**推荐**：在后端 API 层面自动关联选项，这样最简单高效！
