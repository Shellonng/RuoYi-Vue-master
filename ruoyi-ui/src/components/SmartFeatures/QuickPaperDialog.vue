<template>
  <el-dialog
    title="快速配置组卷参数"
    :visible.sync="dialogVisible"
    width="700px"
    :close-on-click-modal="false"
    custom-class="quick-paper-dialog"
  >
    <el-form :model="form" :rules="rules" ref="paperForm" label-width="100px">
      <!-- 知识点选择 -->
      <el-form-item label="知识点" prop="knowledgePoints">
        <el-select
          v-model="form.knowledgePoints"
          multiple
          filterable
          placeholder="选择知识点添加"
          style="width: 100%;"
        >
          <el-option
            v-for="kp in knowledgePointList"
            :key="kp.id"
            :label="kp.title"
            :value="String(kp.id)"
          />
        </el-select>
        <div class="form-tip">请选择至少一个知识点</div>
      </el-form-item>

      <!-- 目标难度 -->
      <el-form-item label="目标难度">
        <el-slider
          v-model="form.targetDifficulty"
          :min="1"
          :max="5"
          :marks="difficultyMarks"
          show-stops
          style="width: 100%;"
        />
      </el-form-item>

      <!-- 题目数量配置 -->
      <el-form-item label="题目数量">
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="question-type-item">
              <span class="type-label">单选题</span>
              <el-input-number
                v-model="form.questionTypeCounts.single"
                :min="0"
                :max="50"
                size="small"
              />
            </div>
          </el-col>
          <el-col :span="12">
            <div class="question-type-item">
              <span class="type-label">多选题</span>
              <el-input-number
                v-model="form.questionTypeCounts.multiple"
                :min="0"
                :max="50"
                size="small"
              />
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="20" style="margin-top: 10px;">
          <el-col :span="12">
            <div class="question-type-item">
              <span class="type-label">填空题</span>
              <el-input-number
                v-model="form.questionTypeCounts.blank"
                :min="0"
                :max="50"
                size="small"
              />
            </div>
          </el-col>
          <el-col :span="12">
            <div class="question-type-item">
              <span class="type-label">简答题</span>
              <el-input-number
                v-model="form.questionTypeCounts.short"
                :min="0"
                :max="50"
                size="small"
              />
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="20" style="margin-top: 10px;">
          <el-col :span="12">
            <div class="question-type-item">
              <span class="type-label">编程题</span>
              <el-input-number
                v-model="form.questionTypeCounts.code"
                :min="0"
                :max="50"
                size="small"
              />
            </div>
          </el-col>
        </el-row>
        <div class="form-tip">共计 {{ totalQuestions }} 道题</div>
      </el-form-item>

      <!-- 总分 -->
      <el-form-item label="总分">
        <el-input-number
          v-model="form.totalScore"
          :min="10"
          :max="1000"
          :step="10"
        />
        <span class="form-tip" style="margin-left: 10px;">分（可选，默认100分）</span>
      </el-form-item>

      <!-- 组卷说明 -->
      <el-form-item label="组卷说明">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="3"
          placeholder="输入组卷说明，如：第一章综合测试、期中考试等"
        />
      </el-form-item>
    </el-form>

    <!-- 底部按钮 -->
    <div slot="footer" class="dialog-footer">
      <el-button @click="handleCancel">取消</el-button>
      <el-button
        type="primary"
        :loading="loading"
        :disabled="!canSubmit"
        @click="handleSubmit"
      >
        生成组卷指令
      </el-button>
    </div>

    <!-- 组卷结果对话框 -->
    <el-dialog
      title="组卷结果"
      :visible.sync="resultDialogVisible"
      width="900px"
      append-to-body
      custom-class="result-dialog"
    >
      <div v-if="result" class="result-content">
        <!-- 组卷摘要 -->
        <el-alert
          :title="resultTitle"
          type="success"
          :closable="false"
          show-icon
        />

        <!-- AI 解释 -->
        <div v-if="result.ai_explanation" class="ai-explanation">
          <h4><i class="el-icon-chat-dot-round"></i> AI 分析</h4>
          <p>{{ result.ai_explanation }}</p>
        </div>

        <!-- 统计信息 -->
        <el-row :gutter="20" style="margin-top: 20px;">
          <el-col :span="12">
            <el-card shadow="hover">
              <div slot="header">知识点分布</div>
              <div v-if="result.knowledge_point_distribution">
                <div
                  v-for="(count, kpId) in result.knowledge_point_distribution"
                  :key="kpId"
                  class="stat-item"
                >
                  <span>{{ getKnowledgePointName(kpId) }}</span>
                  <el-tag size="small">{{ count }} 道</el-tag>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card shadow="hover">
              <div slot="header">难度分布</div>
              <div v-if="result.difficulty_distribution">
                <div
                  v-for="(count, diff) in result.difficulty_distribution"
                  :key="diff"
                  class="stat-item"
                >
                  <span>难度 {{ diff }}</span>
                  <el-tag size="small">{{ count }} 道</el-tag>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 题目列表 -->
        <el-card shadow="hover" style="margin-top: 20px;">
          <div slot="header">
            <span>题目列表</span>
            <span style="float: right; color: #909399; font-size: 13px;">
              平均难度: {{ averageDifficulty }}
            </span>
          </div>
          <el-table :data="result.questions" border stripe max-height="400">
            <el-table-column type="index" label="#" width="50" />
            <el-table-column prop="title" label="题目" min-width="200" show-overflow-tooltip />
            <el-table-column prop="question_type" label="题型" width="100">
              <template slot-scope="scope">
                <el-tag size="small" :type="getQuestionTypeTagType(scope.row.question_type)">
                  {{ getQuestionTypeLabel(scope.row.question_type) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="difficulty" label="难度" width="100">
              <template slot-scope="scope">
                <el-rate :value="scope.row.difficulty" disabled :max="5" show-score />
              </template>
            </el-table-column>
            <el-table-column prop="score" label="分值" width="80" />
          </el-table>
        </el-card>
      </div>

      <div slot="footer">
        <el-button @click="resultDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="handlePublish">发布为作业/考试</el-button>
      </div>
    </el-dialog>
  </el-dialog>
</template>

<script>
import { assembleTest, getCourseKnowledgePoints } from '@/api/smart/aiAgent'
import { listKnowledgePoint } from '@/api/course/knowledgePoint'

export default {
  name: 'QuickPaperDialog',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    courseId: {
      type: [Number, String],
      required: true
    }
  },
  data() {
    return {
      dialogVisible: false,
      loading: false,
      knowledgePointList: [],
      form: {
        knowledgePoints: [],
        targetDifficulty: 3,
        questionTypeCounts: {
          single: 5,
          multiple: 3,
          blank: 0,
          short: 2,
          code: 0
        },
        totalScore: 100,
        description: ''
      },
      rules: {
        knowledgePoints: [
          { required: true, message: '请选择至少一个知识点', trigger: 'change' }
        ]
      },
      difficultyMarks: {
        1: '简单',
        2: '较易',
        3: '中等',
        4: '较难',
        5: '困难'
      },
      result: null,
      resultDialogVisible: false,
      sessionId: null
    }
  },
  computed: {
    totalQuestions() {
      const counts = this.form.questionTypeCounts
      return (counts.single || 0) + (counts.multiple || 0) + 
             (counts.blank || 0) + (counts.short || 0) + (counts.code || 0)
    },
    canSubmit() {
      return this.form.knowledgePoints.length > 0 && this.totalQuestions > 0
    },
    resultTitle() {
      if (!this.result) return ''
      return `组卷成功！共生成 ${this.result.total_questions} 道题目，总分 ${this.result.total_score} 分`
    },
    averageDifficulty() {
      if (!this.result || !this.result.average_difficulty) return '-'
      return this.result.average_difficulty.toFixed(1)
    }
  },
  watch: {
    visible(val) {
      this.dialogVisible = val
      if (val) {
        this.loadKnowledgePoints()
      }
    },
    dialogVisible(val) {
      this.$emit('update:visible', val)
      if (!val) {
        this.resetForm()
      }
    }
  },
  methods: {
    async loadKnowledgePoints() {
      try {
        const response = await listKnowledgePoint({
          courseId: this.courseId,
          pageNum: 1,
          pageSize: 1000
        })
        this.knowledgePointList = response.rows || []
      } catch (error) {
        console.error('[快速组卷] 加载知识点失败:', error)
        this.$message.error('加载知识点失败')
      }
    },

    async handleSubmit() {
      this.$refs.paperForm.validate(async (valid) => {
        if (!valid) return

        if (this.totalQuestions === 0) {
          this.$message.warning('请至少选择一种题型')
          return
        }

        this.loading = true
        try {
          console.log('[快速组卷] 提交组卷请求:', {
            course_id: Number(this.courseId),
            knowledge_points: this.form.knowledgePoints,
            target_difficulty: this.form.targetDifficulty,
            question_type_counts: this.filterEmptyTypes(this.form.questionTypeCounts),
            total_score: this.form.totalScore,
            description: this.form.description
          })

          const res = await assembleTest({
            course_id: Number(this.courseId),
            knowledge_points: this.form.knowledgePoints,
            target_difficulty: this.form.targetDifficulty,
            question_type_counts: this.filterEmptyTypes(this.form.questionTypeCounts),
            total_score: this.form.totalScore,
            description: this.form.description
          })

          console.log('[快速组卷] 组卷成功:', res)

          this.sessionId = res.session_id
          this.result = res.result

          this.$message.success('组卷成功！')
          this.dialogVisible = false
          this.resultDialogVisible = true
        } catch (error) {
          console.error('[快速组卷] 组卷失败:', error)
          const errorMsg = error.response?.data?.detail || error.message || '组卷失败'
          this.$message.error(`组卷失败: ${errorMsg}`)
        } finally {
          this.loading = false
        }
      })
    },

    filterEmptyTypes(counts) {
      const filtered = {}
      Object.keys(counts).forEach(key => {
        if (counts[key] > 0) {
          filtered[key] = counts[key]
        }
      })
      return filtered
    },

    handleCancel() {
      this.dialogVisible = false
    },

    handlePublish() {
      this.$message.info('发布功能开发中...')
      // TODO: 实现发布为作业/考试的功能
    },

    resetForm() {
      this.form = {
        knowledgePoints: [],
        targetDifficulty: 3,
        questionTypeCounts: {
          single: 5,
          multiple: 3,
          blank: 0,
          short: 2,
          code: 0
        },
        totalScore: 100,
        description: ''
      }
      this.$refs.paperForm?.clearValidate()
    },

    getKnowledgePointName(kpId) {
      const kp = this.knowledgePointList.find(k => String(k.id) === String(kpId))
      return kp ? kp.title : `知识点${kpId}`
    },

    getQuestionTypeLabel(type) {
      const labels = {
        single: '单选题',
        multiple: '多选题',
        blank: '填空题',
        short: '简答题',
        code: '编程题'
      }
      return labels[type] || type
    },

    getQuestionTypeTagType(type) {
      const types = {
        single: '',
        multiple: 'success',
        blank: 'info',
        short: 'warning',
        code: 'danger'
      }
      return types[type] || ''
    }
  }
}
</script>

<style scoped lang="scss">
.quick-paper-dialog {
  .form-tip {
    font-size: 12px;
    color: #909399;
    margin-top: 5px;
  }

  .question-type-item {
    display: flex;
    align-items: center;
    justify-content: space-between;

    .type-label {
      font-size: 14px;
      color: #606266;
    }
  }
}

.result-dialog {
  .result-content {
    .ai-explanation {
      margin-top: 20px;
      padding: 15px;
      background: #f5f7fa;
      border-radius: 8px;

      h4 {
        margin: 0 0 10px 0;
        color: #303133;
        font-size: 15px;

        i {
          color: #67c23a;
          margin-right: 5px;
        }
      }

      p {
        margin: 0;
        line-height: 1.6;
        color: #606266;
      }
    }

    .stat-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 8px 0;
      border-bottom: 1px solid #ebeef5;

      &:last-child {
        border-bottom: none;
      }

      span {
        color: #606266;
      }
    }
  }
}
</style>
