<template>
  <div class="question-bank-manager">
    <!-- 顶部操作栏 -->
    <div class="header-actions">
      <div class="left-actions">
        <el-button type="primary" @click="showCreateDialog" icon="el-icon-plus" class="action-btn">
          <span class="btn-text">新建题目</span>
        </el-button>
        <el-button type="success" @click="showImportDialog" icon="el-icon-upload2" class="action-btn">
          <span class="btn-text">批量导入</span>
        </el-button>
        <el-button 
          type="danger" 
          @click="batchDelete" 
          :disabled="selectedQuestions.length === 0"
          icon="el-icon-delete"
          class="action-btn"
        >
          <span class="btn-text">批量删除 ({{ selectedQuestions.length }})</span>
        </el-button>
      </div>
      <div class="right-actions">
        <el-button type="warning" @click="loadStatistics" icon="el-icon-data-analysis" class="action-btn">
          <span class="btn-text">查看统计</span>
        </el-button>
        <el-button type="info" @click="$emit('openSmartPaper')" icon="el-icon-magic-stick" class="action-btn smart-btn">
          <span class="btn-text">✨ AI 智能组卷</span>
        </el-button>
      </div>
    </div>

    <!-- 查询筛选区 -->
    <el-card class="filter-card" shadow="hover">
      <div class="filter-content">
        <el-form :inline="true" :model="queryForm" class="filter-form">
          <el-form-item label="题型">
            <el-select v-model="queryForm.questionType" placeholder="全部题型" clearable class="filter-select">
              <el-option label="单选题" value="single">
                <i class="el-icon-circle-check"></i> 单选题
              </el-option>
              <el-option label="多选题" value="multiple">
                <i class="el-icon-check"></i> 多选题
              </el-option>
              <el-option label="填空题" value="blank">
                <i class="el-icon-edit"></i> 填空题
              </el-option>
              <el-option label="简答题" value="short">
                <i class="el-icon-document"></i> 简答题
              </el-option>
              <el-option label="编程题" value="code">
                <i class="el-icon-monitor"></i> 编程题
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="难度">
            <el-select v-model="queryForm.difficulty" placeholder="全部难度" clearable class="filter-select">
              <el-option v-for="i in 5" :key="i" :label="`难度 ${i}`" :value="i">
                <el-rate :value="i" disabled :max="5" show-score text-color="#ff9900" />
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="知识点">
            <el-select v-model="queryForm.knowledgePoint" placeholder="全部知识点" clearable filterable class="filter-select knowledge-select">
              <el-option 
                v-for="kp in knowledgePoints" 
                :key="kp.id" 
                :label="kp.title" 
                :value="String(kp.id)"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch" icon="el-icon-search">查询</el-button>
            <el-button @click="resetQuery" icon="el-icon-refresh">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>

    <!-- 题目列表 -->
    <el-card class="table-card" shadow="hover">
      <div slot="header" class="table-header">
        <span class="title">
          <i class="el-icon-tickets"></i>
          题目列表
        </span>
        <el-tag type="info" effect="plain">共 {{ pagination.total || 0 }} 道题</el-tag>
      </div>
      
      <el-table 
        :data="questions" 
        border 
        stripe
        style="width: 100%" 
        v-loading="loading"
        @selection-change="handleSelectionChange"
        row-key="id"
        class="question-table"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column prop="id" label="ID" width="80" align="center">
          <template slot-scope="{ row }">
            <el-tag size="small" type="info">{{ row.id }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="题目内容" min-width="280" show-overflow-tooltip>
          <template slot-scope="{ row }">
            <div class="question-title">{{ row.title }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="questionType" label="题型" width="100" align="center">
          <template slot-scope="{ row }">
            <el-tag :type="getQuestionTypeTag(row.questionType)" effect="dark">
              {{ getQuestionTypeText(row.questionType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="difficulty" label="难度" width="140" align="center">
          <template slot-scope="{ row }">
            <el-rate 
              :value="row.difficulty" 
              disabled 
              show-score 
              text-color="#ff9900"
            />
          </template>
        </el-table-column>
        <el-table-column prop="knowledgePoint" label="知识点" width="150" align="center">
          <template slot-scope="{ row }">
            <el-tag v-if="getKnowledgePointName(row.knowledgePoint)" type="success" effect="light" size="small">
              {{ getKnowledgePointName(row.knowledgePoint) }}
            </el-tag>
            <span v-else style="color: #909399">--</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template slot-scope="{ row }">
            <el-button-group>
              <el-tooltip content="查看详情" placement="top">
                <el-button size="small" icon="el-icon-view" @click="viewDetail(row.id)" />
              </el-tooltip>
              <el-tooltip content="编辑" placement="top">
                <el-button size="small" type="warning" icon="el-icon-edit" @click="editQuestion(row)" />
              </el-tooltip>
              <el-tooltip content="删除" placement="top">
                <el-button size="small" type="danger" icon="el-icon-delete" @click="deleteRow(row.id)" />
              </el-tooltip>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          :current-page.sync="pagination.page"
          :page-size.sync="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          background
        />
      </div>
    </el-card>

    <!-- 创建/编辑题目对话框 -->
    <el-dialog
      :title="isEdit ? '编辑题目' : '创建题目'"
      :visible.sync="dialogVisible"
      width="700px"
      class="question-dialog"
      :close-on-click-modal="false"
    >
      <el-form :model="formData" label-width="100px" class="question-form">
        <el-form-item label="题目内容" required>
          <el-input 
            v-model="formData.title" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入题目内容"
            show-word-limit
            maxlength="500"
          />
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="题型" required>
              <el-select v-model="formData.questionType" placeholder="选择题型" style="width: 100%">
                <el-option label="单选题" value="single" />
                <el-option label="多选题" value="multiple" />
                <el-option label="填空题" value="blank" />
                <el-option label="简答题" value="short" />
                <el-option label="编程题" value="code" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="难度" required>
              <el-rate 
                v-model="formData.difficulty" 
                show-score 
                text-color="#ff9900"
                :texts="['简单', '较易', '中等', '较难', '困难']"
                show-text
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="正确答案" required>
          <el-input v-model="formData.correctAnswer" placeholder="请输入正确答案" />
        </el-form-item>
        
        <el-form-item label="答案解析">
          <el-input 
            v-model="formData.explanation" 
            type="textarea" 
            :rows="2" 
            placeholder="请输入答案解析（可选）"
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="章节ID" required>
              <el-input v-model="formData.chapterId" placeholder="请输入章节ID" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="知识点" required>
              <el-select v-model="formData.knowledgePoint" placeholder="选择知识点" style="width: 100%">
                <el-option 
                  v-for="kp in knowledgePoints" 
                  :key="kp.id" 
                  :label="kp.title" 
                  :value="String(kp.id)" 
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 选择题选项 -->
        <el-form-item 
          v-if="['single', 'multiple'].includes(formData.questionType)" 
          label="选项设置"
        >
          <div class="options-container">
            <div v-for="(option, index) in formData.options" :key="index" class="option-item">
              <el-row :gutter="10" type="flex" align="middle">
                <el-col :span="3">
                  <el-input v-model="option.optionKey" placeholder="A" class="option-key" />
                </el-col>
                <el-col :span="13">
                  <el-input v-model="option.content" placeholder="选项内容" />
                </el-col>
                <el-col :span="4">
                  <el-checkbox v-model="option.isCorrect">正确</el-checkbox>
                </el-col>
                <el-col :span="4">
                  <el-button type="danger" icon="el-icon-delete" circle size="small" @click="removeOption(index)" />
                </el-col>
              </el-row>
            </div>
            <el-button @click="addOption" type="primary" plain icon="el-icon-plus" class="add-option-btn">
              添加选项
            </el-button>
          </div>
        </el-form-item>
      </el-form>

      <div slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">
          {{ isEdit ? '保存修改' : '创建题目' }}
        </el-button>
      </div>
    </el-dialog>

    <!-- 统计信息对话框 -->
    <el-dialog title="题目统计分析" :visible.sync="statisticsVisible" width="900px" class="stats-dialog">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-card class="stat-card" shadow="hover">
            <div slot="header" class="stat-header">
              <i class="el-icon-pie-chart" style="color: #409EFF"></i>
              <span>题型分布</span>
            </div>
            <div v-if="typeStats.length === 0" class="stat-empty">暂无数据</div>
            <div v-else class="stat-list">
              <div v-for="item in typeStats" :key="item.questionType" class="stat-item">
                <span class="stat-label">{{ getQuestionTypeText(item.questionType) }}</span>
                <el-progress 
                  :percentage="getPercentage(item.count, getTotalCount(typeStats))" 
                  :color="getQuestionTypeColor(item.questionType)"
                />
                <el-tag size="small" :type="getQuestionTypeTag(item.questionType)">{{ item.count }}</el-tag>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="stat-card" shadow="hover">
            <div slot="header" class="stat-header">
              <i class="el-icon-s-data" style="color: #E6A23C"></i>
              <span>难度分布</span>
            </div>
            <div v-if="difficultyStats.length === 0" class="stat-empty">暂无数据</div>
            <div v-else class="stat-list">
              <div v-for="item in difficultyStats" :key="item.difficulty" class="stat-item">
                <span class="stat-label">难度 {{ item.difficulty }}</span>
                <el-progress 
                  :percentage="getPercentage(item.count, getTotalCount(difficultyStats))" 
                  color="#E6A23C"
                />
                <el-tag size="small" type="warning">{{ item.count }}</el-tag>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="stat-card" shadow="hover">
            <div slot="header" class="stat-header">
              <i class="el-icon-collection" style="color: #67C23A"></i>
              <span>知识点分布</span>
            </div>
            <div v-if="kpStats.length === 0" class="stat-empty">暂无数据</div>
            <div v-else class="stat-list scrollable">
              <div v-for="item in kpStats" :key="item.knowledgePoint" class="stat-item">
                <span class="stat-label">{{ item.knowledgePointName || `知识点 ${item.knowledgePoint}` }}</span>
                <el-progress 
                  :percentage="getPercentage(item.count, getTotalCount(kpStats))" 
                  color="#67C23A"
                />
                <el-tag size="small" type="success">{{ item.count }}</el-tag>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-dialog>

    <!-- 批量导入对话框 -->
    <el-dialog title="批量导入题目" :visible.sync="importDialogVisible" width="900px" class="import-dialog">
      <el-alert
        title="导入格式说明"
        type="info"
        :closable="false"
        show-icon
        class="import-alert"
      >
        <template>
          <p>请输入 JSON 格式的题目数组，支持的字段：</p>
          <div class="field-list">
            <el-tag size="small">title *</el-tag>
            <el-tag size="small">questionType *</el-tag>
            <el-tag size="small">difficulty *</el-tag>
            <el-tag size="small">correctAnswer *</el-tag>
            <el-tag size="small" type="info">explanation</el-tag>
            <el-tag size="small" type="info">knowledgePoint</el-tag>
            <el-tag size="small" type="info">options</el-tag>
          </div>
        </template>
      </el-alert>

      <el-input
        v-model="importJsonText"
        type="textarea"
        :rows="15"
        placeholder="请输入 JSON 格式的题目数组..."
        class="import-textarea"
      />

      <div class="import-actions">
        <el-button @click="loadTemplateJson" icon="el-icon-document">加载示例模板</el-button>
        <el-button type="warning" @click="validateImportJson" icon="el-icon-check">验证格式</el-button>
      </div>

      <div slot="footer">
        <el-button @click="importDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitImport" :loading="importing" icon="el-icon-upload2">
          开始导入
        </el-button>
      </div>
    </el-dialog>

    <!-- 题目详情对话框 -->
    <el-dialog title="题目详情" :visible.sync="detailDialogVisible" width="700px" class="detail-dialog">
      <div v-if="currentDetail" class="detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="题目ID">{{ currentDetail.id }}</el-descriptions-item>
          <el-descriptions-item label="题型">
            <el-tag :type="getQuestionTypeTag(currentDetail.questionType)">
              {{ getQuestionTypeText(currentDetail.questionType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="难度" :span="2">
            <el-rate :value="currentDetail.difficulty" disabled show-score />
          </el-descriptions-item>
          <el-descriptions-item label="题目内容" :span="2">
            {{ currentDetail.title }}
          </el-descriptions-item>
          <el-descriptions-item label="正确答案" :span="2">
            <el-tag type="success">{{ currentDetail.correctAnswer }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item v-if="currentDetail.explanation" label="解析" :span="2">
            {{ currentDetail.explanation }}
          </el-descriptions-item>
        </el-descriptions>
        
        <div v-if="currentDetail.options && currentDetail.options.length > 0" class="options-preview">
          <h4>选项</h4>
          <div v-for="opt in currentDetail.options" :key="opt.optionLabel || opt.optionKey" class="option-preview-item">
            <el-tag :type="isCorrectOption(currentDetail.correctAnswer, opt.optionLabel || opt.optionKey) ? 'success' : 'info'" effect="plain">
              {{ opt.optionLabel || opt.optionKey }}
            </el-tag>
            <span>{{ opt.optionText || opt.content }}</span>
            <i v-if="isCorrectOption(currentDetail.correctAnswer, opt.optionLabel || opt.optionKey)" class="el-icon-circle-check" style="color: #67C23A"></i>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { 
  getQuestionList, 
  createQuestion, 
  updateQuestion, 
  deleteQuestion, 
  batchDeleteQuestions,
  getQuestionDetail,
  getQuestionTypeStats,
  getQuestionDifficultyStats
} from '@/api/smart/question'

export default {
  name: 'QuestionBankManager',
  props: {
    courseId: {
      type: [Number, String],
      required: true
    }
  },
  data() {
    return {
      questions: [],
      loading: false,
      pagination: {
        page: 1,
        pageSize: 10,
        total: 0
      },
      selectedQuestions: [],
      
      // 查询表单
      queryForm: {
        questionType: '',
        difficulty: null,
        knowledgePoint: ''
      },
      
      // 对话框
      dialogVisible: false,
      statisticsVisible: false,
      importDialogVisible: false,
      detailDialogVisible: false,
      
      isEdit: false,
      submitting: false,
      importing: false,
      
      // 表单数据
      formData: {
        id: null,
        title: '',
        questionType: 'short',
        difficulty: 3,
        correctAnswer: '',
        explanation: '',
        chapterId: '',
        knowledgePoint: '',
        options: []
      },
      
      // 统计数据
      typeStats: [],
      difficultyStats: [],
      kpStats: [],
      
      // 知识点列表
      knowledgePoints: [],
      
      // 导入文本
      importJsonText: '',
      
      // 当前详情
      currentDetail: null
    }
  },
  mounted() {
    this.loadQuestions()
    this.loadKnowledgePoints()
  },
  watch: {
    courseId(newVal) {
      if (newVal) {
        this.loadQuestions()
        this.loadKnowledgePoints()
      }
    }
  },
  methods: {
    /** 加载题目列表 */
    async loadQuestions() {
      this.loading = true
      try {
        const params = {
          courseId: this.courseId,
          page: this.pagination.page,
          pageSize: this.pagination.pageSize,
          questionType: this.queryForm.questionType || undefined,
          difficulty: this.queryForm.difficulty || undefined,
          knowledgePoint: this.queryForm.knowledgePoint || undefined
        }
        
        const res = await getQuestionList(params)
        console.log('[题库] API响应:', res)
        
        // 适配不同的响应格式
        if (res.code === 200 && res.data) {
          // 格式1: { code: 200, data: { items: [], pagination: {} } }
          this.questions = res.data.items || res.data.records || []
          const pag = res.data.pagination || {}
          this.pagination.total = pag.total || res.data.total || 0
        } else if (res.items) {
          // 格式2: { items: [], pagination: {} }
          this.questions = res.items || []
          this.pagination.total = res.pagination?.total || 0
        } else if (Array.isArray(res)) {
          // 格式3: 直接返回数组
          this.questions = res
          this.pagination.total = res.length
        } else {
          // 其他格式
          this.questions = []
          this.pagination.total = 0
        }
        
        console.log('[题库] 加载题目数量:', this.questions.length)
      } catch (error) {
        console.error('[题库] 获取题目列表失败:', error)
        this.$message.error('获取题目列表失败: ' + (error.message || '网络错误'))
      } finally {
        this.loading = false
      }
    },
    
    /** 加载知识点列表 */
    async loadKnowledgePoints() {
      try {
        // 这里需要调用获取知识点的接口
        // 暂时使用空数组，您可以根据实际情况调用对应的 API
        this.knowledgePoints = []
      } catch (error) {
        console.error('获取知识点列表失败:', error)
      }
    },
    
    /** 搜索 */
    handleSearch() {
      this.pagination.page = 1
      this.loadQuestions()
    },
    
    /** 重置查询 */
    resetQuery() {
      this.queryForm = {
        questionType: '',
        difficulty: null,
        knowledgePoint: ''
      }
      this.pagination.page = 1
      this.loadQuestions()
    },
    
    /** 分页大小改变 */
    handleSizeChange(size) {
      this.pagination.pageSize = size
      this.loadQuestions()
    },
    
    /** 当前页改变 */
    handleCurrentChange(page) {
      this.pagination.page = page
      this.loadQuestions()
    },
    
    /** 选择改变 */
    handleSelectionChange(selection) {
      this.selectedQuestions = selection
    },
    
    /** 显示创建对话框 */
    showCreateDialog() {
      this.isEdit = false
      this.resetFormData()
      this.dialogVisible = true
    },
    
    /** 编辑题目 */
    async editQuestion(row) {
      this.isEdit = true
      try {
        const res = await getQuestionDetail(row.id)
        if (res.code === 200) {
          const data = res.data
          // 转换选项格式
          if (data.options && data.options.length > 0) {
            data.options = data.options.map(opt => ({
              optionKey: opt.optionLabel || opt.optionKey,
              content: opt.optionText || opt.content,
              isCorrect: this.isCorrectOption(data.correctAnswer, opt.optionLabel || opt.optionKey)
            }))
          } else {
            data.options = []
          }
          this.formData = { ...data }
          this.dialogVisible = true
        }
      } catch (error) {
        this.$message.error('获取题目详情失败')
      }
    },
    
    /** 查看详情 */
    async viewDetail(id) {
      try {
        const res = await getQuestionDetail(id)
        if (res.code === 200) {
          this.currentDetail = res.data
          this.detailDialogVisible = true
        }
      } catch (error) {
        this.$message.error('获取详情失败')
      }
    },
    
    /** 提交表单 */
    async submitForm() {
      if (!this.formData.title.trim()) {
        this.$message.warning('请输入题目内容')
        return
      }
      if (!this.formData.chapterId) {
        this.$message.warning('请输入章节ID')
        return
      }
      if (!this.formData.knowledgePoint) {
        this.$message.warning('请选择知识点')
        return
      }
      
      this.submitting = true
      try {
        const data = { 
          ...this.formData,
          courseId: Number(this.courseId),
          difficulty: Number(this.formData.difficulty),
          chapterId: Number(this.formData.chapterId)
        }

        if (this.isEdit) {
          const res = await updateQuestion(this.formData.id, data)
          if (res.code === 200) {
            this.$message.success('更新成功')
            this.dialogVisible = false
            this.loadQuestions()
          } else {
            this.$message.error(res.message || '更新失败')
          }
        } else {
          const res = await createQuestion(data)
          if (res.code === 200) {
            this.$message.success(`创建成功，题目 ID: ${res.data}`)
            this.dialogVisible = false
            this.loadQuestions()
          } else {
            this.$message.error(res.message || '创建失败')
          }
        }
      } catch (error) {
        this.$message.error('操作失败')
      } finally {
        this.submitting = false
      }
    },
    
    /** 删除题目 */
    async deleteRow(id) {
      try {
        await this.$confirm('确定删除该题目吗？此操作不可恢复！', '删除确认', {
          type: 'warning',
          confirmButtonText: '确定删除',
          cancelButtonText: '取消'
        })
        
        const res = await deleteQuestion(id)
        if (res.code === 200) {
          this.$message.success('删除成功')
          this.loadQuestions()
        } else {
          this.$message.error(res.message || '删除失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('删除失败')
        }
      }
    },
    
    /** 批量删除 */
    async batchDelete() {
      if (this.selectedQuestions.length === 0) {
        this.$message.warning('请先选择要删除的题目')
        return
      }

      try {
        await this.$confirm(
          `确定要删除选中的 ${this.selectedQuestions.length} 道题目吗？此操作不可恢复！`,
          '批量删除确认',
          { type: 'warning', confirmButtonText: '确定删除', cancelButtonText: '取消' }
        )

        const ids = this.selectedQuestions.map(item => item.id)
        const res = await batchDeleteQuestions(ids)
        if (res.code === 200) {
          this.$message.success(`成功删除 ${ids.length} 道题目`)
          this.selectedQuestions = []
          this.loadQuestions()
        } else {
          this.$message.error(res.message || '批量删除失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('批量删除失败')
        }
      }
    },
    
    /** 加载统计 */
    async loadStatistics() {
      try {
        const [typeRes, diffRes] = await Promise.all([
          getQuestionTypeStats(this.courseId),
          getQuestionDifficultyStats(this.courseId)
        ])
        
        this.typeStats = typeRes.data || typeRes || []
        this.difficultyStats = diffRes.data || diffRes || []
        this.kpStats = [] // 如果有知识点统计接口，可以添加
        
        this.statisticsVisible = true
      } catch (error) {
        this.$message.error('加载统计失败')
      }
    },
    
    /** 显示导入对话框 */
    showImportDialog() {
      this.importJsonText = ''
      this.importDialogVisible = true
    },
    
    /** 加载模板 */
    loadTemplateJson() {
      const template = [
        {
          title: "什么是反向传播？",
          questionType: "short",
          difficulty: 3,
          correctAnswer: "一种训练神经网络的算法",
          explanation: "反向传播是通过链式法则计算梯度并更新网络参数的算法",
          options: []
        },
        {
          title: "以下哪个是激活函数？",
          questionType: "single",
          difficulty: 2,
          correctAnswer: "B",
          options: [
            { optionKey: "A", content: "损失函数", isCorrect: false },
            { optionKey: "B", content: "ReLU", isCorrect: true },
            { optionKey: "C", content: "优化器", isCorrect: false }
          ]
        }
      ]
      this.importJsonText = JSON.stringify(template, null, 2)
      this.$message.success('已加载示例模板')
    },
    
    /** 验证导入 JSON */
    validateImportJson() {
      try {
        const data = JSON.parse(this.importJsonText)
        if (!Array.isArray(data)) {
          this.$message.error('JSON 必须是数组格式')
          return false
        }
        if (data.length === 0) {
          this.$message.error('题目数组不能为空')
          return false
        }

        for (let i = 0; i < data.length; i++) {
          const item = data[i]
          if (!item.title) {
            this.$message.error(`第 ${i + 1} 个题目缺少 title`)
            return false
          }
          if (!item.questionType) {
            this.$message.error(`第 ${i + 1} 个题目缺少 questionType`)
            return false
          }
          if (['single', 'multiple'].includes(item.questionType) && (!item.options || item.options.length === 0)) {
            this.$message.error(`第 ${i + 1} 个题目是选择题但缺少 options`)
            return false
          }
        }

        this.$message.success(`格式验证通过，共 ${data.length} 道题目`)
        return true
      } catch (error) {
        this.$message.error(`JSON 格式错误: ${error.message}`)
        return false
      }
    },
    
    /** 提交导入 */
    async submitImport() {
      if (!this.validateImportJson()) return

      this.importing = true
      try {
        const data = JSON.parse(this.importJsonText)
        // 添加课程ID
        const questionsWithCourse = data.map(q => ({ ...q, courseId: Number(this.courseId) }))
        
        // 批量创建（需要循环调用或使用批量导入接口）
        let successCount = 0
        for (const question of questionsWithCourse) {
          try {
            await createQuestion(question)
            successCount++
          } catch (error) {
            console.error('导入题目失败:', error)
          }
        }
        
        this.$message.success(`成功导入 ${successCount} 道题目`)
        this.importDialogVisible = false
        this.loadQuestions()
      } catch (error) {
        this.$message.error('导入失败')
      } finally {
        this.importing = false
      }
    },
    
    /** 添加选项 */
    addOption() {
      this.formData.options.push({
        optionKey: String.fromCharCode(65 + this.formData.options.length),
        content: '',
        isCorrect: false
      })
    },
    
    /** 移除选项 */
    removeOption(index) {
      this.formData.options.splice(index, 1)
    },
    
    /** 重置表单 */
    resetFormData() {
      this.formData = {
        id: null,
        title: '',
        questionType: 'short',
        difficulty: 3,
        correctAnswer: '',
        explanation: '',
        chapterId: '',
        knowledgePoint: '',
        options: []
      }
    },
    
    // 工具方法
    getQuestionTypeText(type) {
      const map = { single: '单选', multiple: '多选', blank: '填空', short: '简答', code: '编程' }
      return map[type] || type
    },
    
    getQuestionTypeTag(type) {
      const map = { single: 'success', multiple: 'warning', blank: 'info', short: 'primary', code: 'danger' }
      return map[type] || ''
    },
    
    getQuestionTypeColor(type) {
      const map = { single: '#67C23A', multiple: '#E6A23C', blank: '#909399', short: '#409EFF', code: '#F56C6C' }
      return map[type] || '#409EFF'
    },
    
    getKnowledgePointName(kpId) {
      if (!kpId) return ''
      const kp = this.knowledgePoints.find(item => String(item.id) === String(kpId))
      return kp ? kp.title : ''
    },
    
    isCorrectOption(correctAnswer, optionLabel) {
      if (!correctAnswer || !optionLabel) return false
      const answers = correctAnswer.toUpperCase().replace(/,/g, '').split('')
      return answers.includes(optionLabel.toUpperCase())
    },
    
    getPercentage(count, total) {
      if (!total) return 0
      return Math.round((count / total) * 100)
    },
    
    getTotalCount(stats) {
      return stats.reduce((sum, item) => sum + (item.count || 0), 0)
    }
  }
}
</script>

<style scoped>
.question-bank-manager {
  padding: 0;
}

.header-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 12px;
}

.left-actions, .right-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.action-btn {
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.smart-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: white;
}

.smart-btn:hover {
  background: linear-gradient(135deg, #764ba2 0%, #667eea 100%);
}

.filter-card {
  margin-bottom: 20px;
  border-radius: 12px;
}

.filter-content {
  padding: 8px 0;
}

.filter-form >>> .el-form-item {
  margin-bottom: 0;
}

.filter-select {
  width: 140px;
}

.knowledge-select {
  width: 180px;
}

.table-card {
  border-radius: 12px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.table-header .title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 16px;
}

.question-table {
  border-radius: 8px;
  overflow: hidden;
}

.question-title {
  line-height: 1.5;
  color: #303133;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding: 10px 0;
}

/* Dialog styles */
.question-dialog >>> .el-dialog__body {
  padding: 20px 30px;
}

.question-form >>> .el-form-item__label {
  font-weight: 500;
}

.options-container {
  width: 100%;
}

.option-item {
  margin-bottom: 12px;
  padding: 10px;
  background: #f5f7fa;
  border-radius: 8px;
}

.option-key {
  text-align: center;
  font-weight: bold;
}

.add-option-btn {
  width: 100%;
  margin-top: 10px;
}

/* Stats dialog */
.stat-card {
  height: 100%;
}

.stat-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.stat-empty {
  color: #909399;
  text-align: center;
  padding: 40px 0;
}

.stat-list {
  max-height: 250px;
}

.stat-list.scrollable {
  overflow-y: auto;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 15px;
}

.stat-label {
  width: 80px;
  font-size: 13px;
  color: #606266;
}

.stat-item >>> .el-progress {
  flex: 1;
}

/* Import dialog */
.import-alert {
  margin-bottom: 20px;
}

.field-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 10px;
}

.import-textarea >>> .el-textarea__inner {
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 13px;
}

.import-actions {
  display: flex;
  gap: 10px;
  margin-top: 15px;
}

/* Detail dialog */
.detail-content {
  padding: 10px 0;
}

.options-preview {
  margin-top: 20px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 8px;
}

.options-preview h4 {
  margin: 0 0 15px 0;
  color: #303133;
}

.option-preview-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 0;
  border-bottom: 1px dashed #e4e7ed;
}

.option-preview-item:last-child {
  border-bottom: none;
}
</style>
