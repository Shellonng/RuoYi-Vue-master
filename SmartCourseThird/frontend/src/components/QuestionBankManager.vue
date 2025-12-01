<template>
  <div class="question-bank-manager">
    <!-- 顶部操作栏 -->
    <div class="header-actions">
      <div class="left-actions">
        <el-button type="primary" @click="showCreateDialog" :icon="Plus" class="action-btn">
          <span class="btn-text">新建题目</span>
        </el-button>
        <el-button type="success" @click="showImportDialog" :icon="Upload" class="action-btn">
          <span class="btn-text">批量导入</span>
        </el-button>
        <el-button 
          type="danger" 
          @click="batchDelete" 
          :disabled="selectedQuestions.length === 0"
          :icon="Delete"
          class="action-btn"
        >
          <span class="btn-text">批量删除 ({{ selectedQuestions.length }})</span>
        </el-button>
      </div>
      <div class="right-actions">
        <el-button type="warning" @click="loadStatistics" :icon="DataAnalysis" class="action-btn">
          <span class="btn-text">查看统计</span>
        </el-button>
        <el-button type="info" @click="emit('openSmartPaper')" :icon="MagicStick" class="action-btn smart-btn">
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
                <el-icon><CircleCheck /></el-icon> 单选题
              </el-option>
              <el-option label="多选题" value="multiple">
                <el-icon><Checked /></el-icon> 多选题
              </el-option>
              <el-option label="填空题" value="blank">
                <el-icon><EditPen /></el-icon> 填空题
              </el-option>
              <el-option label="简答题" value="short">
                <el-icon><Document /></el-icon> 简答题
              </el-option>
              <el-option label="编程题" value="code">
                <el-icon><Monitor /></el-icon> 编程题
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="难度">
            <el-select v-model="queryForm.difficulty" placeholder="全部难度" clearable class="filter-select">
              <el-option v-for="i in 5" :key="i" :label="`难度 ${i}`" :value="i">
                <el-rate :model-value="i" disabled :max="5" size="small" />
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
            <el-button type="primary" @click="handleSearch" :icon="Search">查询</el-button>
            <el-button @click="resetQuery" :icon="Refresh">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>

    <!-- 题目列表 -->
    <el-card class="table-card" shadow="hover">
      <template #header>
        <div class="table-header">
          <span class="title">
            <el-icon><List /></el-icon>
            题目列表
          </span>
          <el-tag type="info" effect="plain">共 {{ pagination.total || 0 }} 道题</el-tag>
        </div>
      </template>
      
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
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ row.id }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="题目内容" min-width="280" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="question-title">{{ row.title }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="questionType" label="题型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getQuestionTypeTag(row.questionType)" effect="dark">
              {{ getQuestionTypeText(row.questionType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="difficulty" label="难度" width="140" align="center">
          <template #default="{ row }">
            <el-rate 
              :model-value="row.difficulty" 
              disabled 
              show-score 
              text-color="#ff9900"
              size="small"
            />
          </template>
        </el-table-column>
        <el-table-column prop="knowledgePoint" label="知识点" width="150" align="center">
          <template #default="{ row }">
            <el-tag v-if="getKnowledgePointName(row.knowledgePoint)" type="success" effect="light" size="small">
              {{ getKnowledgePointName(row.knowledgePoint) }}
            </el-tag>
            <span v-else style="color: #909399">--</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button-group>
              <el-tooltip content="查看详情" placement="top">
                <el-button size="small" :icon="View" @click="viewDetail(row.id)" />
              </el-tooltip>
              <el-tooltip content="编辑" placement="top">
                <el-button size="small" type="warning" :icon="Edit" @click="editQuestion(row)" />
              </el-tooltip>
              <el-tooltip content="删除" placement="top">
                <el-button size="small" type="danger" :icon="Delete" @click="deleteRow(row.id)" />
              </el-tooltip>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
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
      v-model="dialogVisible"
      :title="isEdit ? '编辑题目' : '创建题目'"
      width="700px"
      class="question-dialog"
      destroy-on-close
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
              <el-row :gutter="10" align="middle">
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
                  <el-button type="danger" :icon="Delete" circle size="small" @click="removeOption(index)" />
                </el-col>
              </el-row>
            </div>
            <el-button @click="addOption" type="primary" plain :icon="Plus" class="add-option-btn">
              添加选项
            </el-button>
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">
          {{ isEdit ? '保存修改' : '创建题目' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 统计信息对话框 -->
    <el-dialog v-model="statisticsVisible" title="题目统计分析" width="900px" class="stats-dialog">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-card class="stat-card" shadow="hover">
            <template #header>
              <div class="stat-header">
                <el-icon color="#409EFF"><PieChart /></el-icon>
                <span>题型分布</span>
              </div>
            </template>
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
            <template #header>
              <div class="stat-header">
                <el-icon color="#E6A23C"><TrendCharts /></el-icon>
                <span>难度分布</span>
              </div>
            </template>
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
            <template #header>
              <div class="stat-header">
                <el-icon color="#67C23A"><Collection /></el-icon>
                <span>知识点分布</span>
              </div>
            </template>
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
    <el-dialog v-model="importDialogVisible" title="批量导入题目" width="900px" class="import-dialog">
      <el-alert
        title="导入格式说明"
        type="info"
        :closable="false"
        show-icon
        class="import-alert"
      >
        <template #default>
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
        <el-button @click="loadTemplateJson" :icon="Document">加载示例模板</el-button>
        <el-button type="warning" @click="validateImportJson" :icon="Check">验证格式</el-button>
      </div>

      <template #footer>
        <el-button @click="importDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitImport" :loading="importing" :icon="Upload">
          开始导入
        </el-button>
      </template>
    </el-dialog>

    <!-- 题目详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="题目详情" width="700px" class="detail-dialog">
      <div v-if="currentDetail" class="detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="题目ID">{{ currentDetail.id }}</el-descriptions-item>
          <el-descriptions-item label="题型">
            <el-tag :type="getQuestionTypeTag(currentDetail.questionType)">
              {{ getQuestionTypeText(currentDetail.questionType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="难度" :span="2">
            <el-rate :model-value="currentDetail.difficulty" disabled show-score />
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
            <el-icon v-if="isCorrectOption(currentDetail.correctAnswer, opt.optionLabel || opt.optionKey)" color="#67C23A"><CircleCheckFilled /></el-icon>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Plus, Upload, Delete, DataAnalysis, MagicStick, Search, Refresh, List,
  View, Edit, CircleCheck, Checked, EditPen, Document, Monitor, PieChart,
  TrendCharts, Collection, Check, CircleCheckFilled
} from '@element-plus/icons-vue'
import { useQuestionStore } from '../store/question'

// Props
const props = defineProps({
  courseId: {
    type: [Number, String],
    required: true
  }
})

// Emits
const emit = defineEmits(['openSmartPaper'])

// Store
const questionStore = useQuestionStore()

// Computed from store
const questions = computed(() => questionStore.questions)
const loading = computed(() => questionStore.loading)
const pagination = computed(() => questionStore.pagination)
const typeStats = computed(() => questionStore.typeStats)
const difficultyStats = computed(() => questionStore.difficultyStats)
const kpStats = computed(() => questionStore.kpStats)
const knowledgePoints = computed(() => questionStore.knowledgePoints)

// Local state
const dialogVisible = ref(false)
const statisticsVisible = ref(false)
const importDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const importing = ref(false)
const selectedQuestions = ref([])
const importJsonText = ref('')
const currentDetail = ref(null)

const queryForm = reactive({
  questionType: '',
  difficulty: null,
  knowledgePoint: ''
})

const formData = reactive({
  id: null,
  title: '',
  questionType: 'short',
  difficulty: 3,
  correctAnswer: '',
  explanation: '',
  chapterId: '',
  knowledgePoint: '',
  options: []
})

// Methods
const loadQuestions = async () => {
  await questionStore.fetchQuestions(props.courseId, {
    questionType: queryForm.questionType || undefined,
    difficulty: queryForm.difficulty || undefined,
    knowledgePoint: queryForm.knowledgePoint || undefined
  })
}

const handleSearch = () => {
  questionStore.setPage(1)
  loadQuestions()
}

const resetQuery = () => {
  queryForm.questionType = ''
  queryForm.difficulty = null
  queryForm.knowledgePoint = ''
  questionStore.setPage(1)
  loadQuestions()
}

const handleSizeChange = (size) => {
  questionStore.setPageSize(size)
  loadQuestions()
}

const handleCurrentChange = (page) => {
  questionStore.setPage(page)
  loadQuestions()
}

const handleSelectionChange = (selection) => {
  selectedQuestions.value = selection
}

const showCreateDialog = () => {
  isEdit.value = false
  resetFormData()
  dialogVisible.value = true
}

const editQuestion = async (row) => {
  isEdit.value = true
  try {
    const res = await questionStore.fetchQuestionDetail(row.id)
    if (res.code === 200) {
      const data = res.data
      // 转换选项格式：后端 optionLabel/optionText -> 前端 optionKey/content/isCorrect
      if (data.options && data.options.length > 0) {
        data.options = data.options.map(opt => ({
          optionKey: opt.optionLabel || opt.optionKey,
          content: opt.optionText || opt.content,
          isCorrect: isCorrectOption(data.correctAnswer, opt.optionLabel || opt.optionKey)
        }))
      } else {
        data.options = []
      }
      Object.assign(formData, data)
      dialogVisible.value = true
    }
  } catch (error) {
    ElMessage.error('获取题目详情失败')
  }
}

const viewDetail = async (id) => {
  try {
    const res = await questionStore.fetchQuestionDetail(id)
    if (res.code === 200) {
      currentDetail.value = res.data
      detailDialogVisible.value = true
    }
  } catch (error) {
    ElMessage.error('获取详情失败')
  }
}

const submitForm = async () => {
  if (!formData.title.trim()) {
    ElMessage.warning('请输入题目内容')
    return
  }
  if (!formData.chapterId) {
    ElMessage.warning('请输入章节ID')
    return
  }
  if (!formData.knowledgePoint) {
    ElMessage.warning('请选择知识点')
    return
  }
  
  submitting.value = true
  try {
    const data = { 
      ...formData,
      courseId: Number(props.courseId),
      difficulty: Number(formData.difficulty),
      chapterId: Number(formData.chapterId)
    }

    if (isEdit.value) {
      const res = await questionStore.editQuestion(formData.id, data)
      if (res.code === 200) {
        ElMessage.success('更新成功')
        dialogVisible.value = false
        loadQuestions()
      } else {
        ElMessage.error(res.message || '更新失败')
      }
    } else {
      const res = await questionStore.addQuestion(data)
      if (res.code === 200) {
        ElMessage.success(`创建成功，题目 ID: ${res.data}`)
        dialogVisible.value = false
        loadQuestions()
      } else {
        ElMessage.error(res.message || '创建失败')
      }
    }
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}

const deleteRow = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除该题目吗？此操作不可恢复！', '删除确认', {
      type: 'warning',
      confirmButtonText: '确定删除',
      cancelButtonText: '取消'
    })
    const res = await questionStore.removeQuestion(id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadQuestions()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const batchDelete = async () => {
  if (selectedQuestions.value.length === 0) {
    ElMessage.warning('请先选择要删除的题目')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedQuestions.value.length} 道题目吗？此操作不可恢复！`,
      '批量删除确认',
      { type: 'warning', confirmButtonText: '确定删除', cancelButtonText: '取消' }
    )

    const ids = selectedQuestions.value.map(item => item.id)
    const res = await questionStore.removeQuestions(ids)
    if (res.code === 200) {
      ElMessage.success(`成功删除 ${ids.length} 道题目`)
      selectedQuestions.value = []
      loadQuestions()
    } else {
      ElMessage.error(res.message || '批量删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
    }
  }
}

const loadStatistics = async () => {
  try {
    await questionStore.fetchStatistics(props.courseId)
    statisticsVisible.value = true
  } catch (error) {
    ElMessage.error('加载统计失败')
  }
}

const showImportDialog = () => {
  importJsonText.value = ''
  importDialogVisible.value = true
}

const loadTemplateJson = () => {
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
  importJsonText.value = JSON.stringify(template, null, 2)
  ElMessage.success('已加载示例模板')
}

const validateImportJson = () => {
  try {
    const data = JSON.parse(importJsonText.value)
    if (!Array.isArray(data)) {
      ElMessage.error('JSON 必须是数组格式')
      return false
    }
    if (data.length === 0) {
      ElMessage.error('题目数组不能为空')
      return false
    }

    for (let i = 0; i < data.length; i++) {
      const item = data[i]
      if (!item.title) {
        ElMessage.error(`第 ${i + 1} 个题目缺少 title`)
        return false
      }
      if (!item.questionType) {
        ElMessage.error(`第 ${i + 1} 个题目缺少 questionType`)
        return false
      }
      if (['single', 'multiple'].includes(item.questionType) && (!item.options || item.options.length === 0)) {
        ElMessage.error(`第 ${i + 1} 个题目是选择题但缺少 options`)
        return false
      }
    }

    ElMessage.success(`格式验证通过，共 ${data.length} 道题目`)
    return true
  } catch (error) {
    ElMessage.error(`JSON 格式错误: ${error.message}`)
    return false
  }
}

const submitImport = async () => {
  if (!validateImportJson()) return

  importing.value = true
  try {
    const data = JSON.parse(importJsonText.value)
    // 添加课程ID
    const questionsWithCourse = data.map(q => ({ ...q, courseId: Number(props.courseId) }))
    const res = await questionStore.batchImport(questionsWithCourse)
    if (res.code === 200) {
      ElMessage.success(`成功导入 ${res.data} 道题目`)
      importDialogVisible.value = false
      loadQuestions()
    } else {
      ElMessage.error(res.message || '导入失败')
    }
  } catch (error) {
    ElMessage.error('导入失败')
  } finally {
    importing.value = false
  }
}

const addOption = () => {
  formData.options.push({
    optionKey: String.fromCharCode(65 + formData.options.length),
    content: '',
    isCorrect: false
  })
}

const removeOption = (index) => {
  formData.options.splice(index, 1)
}

const resetFormData = () => {
  Object.assign(formData, {
    id: null,
    title: '',
    questionType: 'short',
    difficulty: 3,
    correctAnswer: '',
    explanation: '',
    chapterId: '',
    knowledgePoint: '',
    options: []
  })
}

// Utils
const getQuestionTypeText = (type) => {
  const map = { single: '单选', multiple: '多选', blank: '填空', short: '简答', code: '编程' }
  return map[type] || type
}

const getQuestionTypeTag = (type) => {
  const map = { single: 'success', multiple: 'warning', blank: 'info', short: 'primary', code: 'danger' }
  return map[type] || ''
}

const getQuestionTypeColor = (type) => {
  const map = { single: '#67C23A', multiple: '#E6A23C', blank: '#909399', short: '#409EFF', code: '#F56C6C' }
  return map[type] || '#409EFF'
}

const getKnowledgePointName = (kpId) => {
  if (!kpId) return ''
  const kp = knowledgePoints.value.find(item => String(item.id) === String(kpId))
  return kp ? kp.title : ''
}

const isCorrectOption = (correctAnswer, optionLabel) => {
  if (!correctAnswer || !optionLabel) return false
  // 支持多选题答案如 "A,C" 或 "AC"
  const answers = correctAnswer.toUpperCase().replace(/,/g, '').split('')
  return answers.includes(optionLabel.toUpperCase())
}

const getPercentage = (count, total) => {
  if (!total) return 0
  return Math.round((count / total) * 100)
}

const getTotalCount = (stats) => {
  return stats.reduce((sum, item) => sum + (item.count || 0), 0)
}

// Lifecycle
onMounted(() => {
  loadQuestions()
  questionStore.fetchKnowledgePoints(props.courseId)
})

// Watch courseId changes
watch(() => props.courseId, (newVal) => {
  if (newVal) {
    loadQuestions()
    questionStore.fetchKnowledgePoints(newVal)
  }
})
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

.filter-form :deep(.el-form-item) {
  margin-bottom: 0;
}

.filter-select {
  width: 140px;
}

.filter-input {
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
.question-dialog :deep(.el-dialog__body) {
  padding: 20px 30px;
}

.question-form :deep(.el-form-item__label) {
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

.stat-item :deep(.el-progress) {
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

.import-textarea :deep(.el-textarea__inner) {
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
