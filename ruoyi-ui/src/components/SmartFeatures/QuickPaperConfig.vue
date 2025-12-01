<template>
  <el-dialog
    title="快速配置组卷参数"
    :visible.sync="dialogVisible"
    width="650px"
    :close-on-click-modal="false"
    append-to-body
    custom-class="config-dialog"
  >
    <el-form :model="configForm" label-width="100px" class="config-form">
      <el-form-item label="知识点">
        <div class="knowledge-points-selector">
          <el-select
            v-model="selectedKp"
            placeholder="选择知识点添加"
            filterable
            clearable
            @change="addKnowledgePoint"
            style="width: 300px"
          >
            <el-option
              v-for="kp in availableKnowledgePoints"
              :key="kp.id"
              :label="kp.title"
              :value="kp"
            />
          </el-select>
          <div class="selected-kps">
            <el-tag
              v-for="kp in configForm.knowledgePoints"
              :key="kp.id"
              closable
              @close="removeKnowledgePoint(kp)"
              class="kp-tag"
              type="success"
            >
              {{ kp.title }}
            </el-tag>
            <span v-if="configForm.knowledgePoints.length === 0" class="empty-tip">
              请选择至少一个知识点
            </span>
          </div>
        </div>
      </el-form-item>

      <el-form-item label="目标难度">
        <el-slider
          v-model="configForm.difficulty"
          :min="1"
          :max="5"
          :step="0.5"
          show-stops
          :marks="difficultyMarks"
          style="width: 400px"
        />
      </el-form-item>

      <el-form-item label="题目数量">
        <el-row :gutter="15" class="question-counts">
          <el-col :span="8">
            <div class="count-item">
              <span class="label">单选题</span>
              <el-input-number v-model="configForm.singleCount" :min="0" :max="50" size="small" />
            </div>
          </el-col>
          <el-col :span="8">
            <div class="count-item">
              <span class="label">多选题</span>
              <el-input-number v-model="configForm.multipleCount" :min="0" :max="50" size="small" />
            </div>
          </el-col>
          <el-col :span="8">
            <div class="count-item">
              <span class="label">填空题</span>
              <el-input-number v-model="configForm.blankCount" :min="0" :max="50" size="small" />
            </div>
          </el-col>
          <el-col :span="8">
            <div class="count-item">
              <span class="label">简答题</span>
              <el-input-number v-model="configForm.shortCount" :min="0" :max="50" size="small" />
            </div>
          </el-col>
          <el-col :span="8">
            <div class="count-item">
              <span class="label">编程题</span>
              <el-input-number v-model="configForm.codeCount" :min="0" :max="50" size="small" />
            </div>
          </el-col>
        </el-row>
      </el-form-item>

      <el-form-item label="总分">
        <el-input-number v-model="configForm.totalScore" :min="10" :max="500" :step="10" />
        <span class="form-tip">分（可选，默认100分）</span>
      </el-form-item>
    </el-form>

    <template slot="footer">
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="submitConfig" :disabled="!isConfigValid">
        生成组卷指令
      </el-button>
    </template>
  </el-dialog>
</template>

<script>
import { listKnowledgePoint } from '@/api/course/knowledgePoint'

export default {
  name: 'QuickPaperConfig',
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
      selectedKp: null,
      availableKnowledgePoints: [],
      configForm: {
        knowledgePoints: [],
        difficulty: 3,
        singleCount: 5,
        multipleCount: 3,
        blankCount: 0,
        shortCount: 2,
        codeCount: 0,
        totalScore: 100
      },
      difficultyMarks: {
        1: '简单',
        2: '较易',
        3: '中等',
        4: '较难',
        5: '困难'
      }
    }
  },
  computed: {
    isConfigValid() {
      const totalQuestions = this.configForm.singleCount + this.configForm.multipleCount +
                            this.configForm.blankCount + this.configForm.shortCount +
                            this.configForm.codeCount
      return this.configForm.knowledgePoints.length > 0 && totalQuestions > 0
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
        this.availableKnowledgePoints = response.rows || []
      } catch (error) {
        console.error('[快速配置] 加载知识点失败:', error)
        this.$message.error('加载知识点失败')
      }
    },

    addKnowledgePoint(kp) {
      if (kp && !this.configForm.knowledgePoints.find(k => k.id === kp.id)) {
        this.configForm.knowledgePoints.push(kp)
      }
      this.selectedKp = null
    },

    removeKnowledgePoint(kp) {
      const index = this.configForm.knowledgePoints.findIndex(k => k.id === kp.id)
      if (index > -1) {
        this.configForm.knowledgePoints.splice(index, 1)
      }
    },

    submitConfig() {
      // 构建自然语言文本
      const kpNames = this.configForm.knowledgePoints.map(k => k.title).join('、')
      const types = []
      if (this.configForm.singleCount > 0) types.push(`${this.configForm.singleCount}道单选题`)
      if (this.configForm.multipleCount > 0) types.push(`${this.configForm.multipleCount}道多选题`)
      if (this.configForm.blankCount > 0) types.push(`${this.configForm.blankCount}道填空题`)
      if (this.configForm.shortCount > 0) types.push(`${this.configForm.shortCount}道简答题`)
      if (this.configForm.codeCount > 0) types.push(`${this.configForm.codeCount}道编程题`)

      const configText = `我需要组一份试卷，知识点包括：${kpNames}，难度：${this.configForm.difficulty}，题目包括：${types.join('、')}，总分${this.configForm.totalScore}分`

      this.$emit('submit', configText)
      this.dialogVisible = false
    }
  }
}
</script>

<style scoped lang="scss">
.config-dialog {
  .config-form {
    .form-tip {
      font-size: 12px;
      color: #909399;
      margin-left: 10px;
    }

    .knowledge-points-selector {
      .selected-kps {
        margin-top: 10px;
        min-height: 32px;
        padding: 8px;
        border: 1px dashed #dcdfe6;
        border-radius: 4px;
        background: #fafafa;

        .kp-tag {
          margin-right: 8px;
          margin-bottom: 8px;
        }

        .empty-tip {
          color: #c0c4cc;
          font-size: 13px;
        }
      }
    }

    .question-counts {
      .count-item {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 10px;

        .label {
          font-size: 14px;
          color: #606266;
        }
      }
    }
  }
}
</style>
