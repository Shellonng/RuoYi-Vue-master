<template>
  <div class="assignment-manager">
    <!-- 顶部操作栏 -->
    <div class="header-actions">
      <div class="left-actions">
        <!-- <el-button type="primary" @click="showCreateDialog" :icon="Plus" class="action-btn">
          <span class="btn-text">创建作业</span>
        </el-button> -->
        <el-button type="success" @click="refreshList" :icon="Refresh" class="action-btn">
          <span class="btn-text">刷新列表</span>
        </el-button>
      </div>
      <div class="right-actions">
        <el-radio-group v-model="filterType" size="default" @change="handleFilterChange">
          <el-radio-button value="">全部</el-radio-button>
          <el-radio-button value="homework">作业</el-radio-button>
          <el-radio-button value="exam">考试</el-radio-button>
        </el-radio-group>
      </div>
    </div>

    <!-- 搜索过滤栏 -->
    <el-card class="filter-card" shadow="hover">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 120px">
            <el-option label="草稿" :value="0" />
            <el-option label="已发布" :value="1" />
            <el-option label="已关闭" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card stat-total" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon :size="36"><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ pagination.total || 0 }}</div>
              <div class="stat-label">总任务数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card stat-draft" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon :size="36"><EditPen /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ draftCount }}</div>
              <div class="stat-label">草稿</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card stat-published" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon :size="36"><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ publishedCount }}</div>
              <div class="stat-label">已发布</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card stat-closed" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon :size="36"><Lock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ closedCount }}</div>
              <div class="stat-label">已关闭</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 作业列表 -->
    <el-card class="table-card" shadow="hover">
      <template #header>
        <div class="table-header">
          <span class="title">
            <el-icon><List /></el-icon>
            作业/考试列表
          </span>
          <el-tag type="info" effect="plain">共 {{ pagination.total || 0 }} 项</el-tag>
        </div>
      </template>
      
      <el-table 
        :data="assignments" 
        border 
        stripe
        style="width: 100%" 
        v-loading="loading"
        row-key="id"
        class="assignment-table"
      >
        <el-table-column prop="id" label="ID" width="80" align="center">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ row.id }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="assignment-title">
              <el-icon v-if="row.type === 'exam'" color="#F56C6C"><Notification /></el-icon>
              <el-icon v-else color="#409EFF"><Document /></el-icon>
              <span>{{ row.title }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)" effect="dark">
              {{ getTypeText(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" effect="light">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="questionCount" label="题目数" width="90" align="center">
          <template #default="{ row }">
            <el-badge :value="row.questionCount || 0" type="primary" class="question-badge" />
          </template>
        </el-table-column>
        <el-table-column prop="totalScore" label="总分" width="80" align="center">
          <template #default="{ row }">
            <span class="total-score">{{ row.totalScore || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right" align="center">
          <template #default="{ row }">
            <el-button-group>
              <el-tooltip content="查看详情" placement="top">
                <el-button size="small" :icon="View" @click="viewDetail(row.id)" />
              </el-tooltip>
              <el-tooltip content="编辑" placement="top">
                <el-button size="small" type="primary" :icon="Edit" @click="showEditDialog(row)" />
              </el-tooltip>
              <el-tooltip v-if="canPublish(row)" content="发布" placement="top">
                <el-button size="small" type="success" :icon="Promotion" @click="showPublishDialog(row)" />
              </el-tooltip>
              <el-tooltip v-if="canUnpublish(row)" content="撤销发布" placement="top">
                <el-button size="small" type="warning" :icon="RefreshLeft" @click="handleUnpublish(row.id)" />
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
          :page-sizes="[10, 20, 50]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          background
        />
      </div>
    </el-card>

    <!-- 创建作业对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="创建作业/考试"
      width="650px"
      class="assignment-dialog"
      destroy-on-close
    >
      <el-form :model="formData" label-width="100px" class="assignment-form">
        <el-form-item label="标题" required>
          <el-input v-model="formData.title" placeholder="请输入作业/考试标题" maxlength="100" show-word-limit />
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="类型" required>
              <el-select v-model="formData.type" placeholder="选择类型" style="width: 100%">
                <el-option label="作业" value="homework">
                  <el-icon><Document /></el-icon> 作业
                </el-option>
                <el-option label="考试" value="exam">
                  <el-icon><Notification /></el-icon> 考试
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="总分">
              <el-input-number v-model="formData.totalScore" :min="0" :max="1000" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="描述">
          <el-input 
            v-model="formData.description" 
            type="textarea" 
            :rows="3" 
            placeholder="作业/考试描述（可选）"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="题目ID" required>
          <el-input 
            v-model="questionIdsInput" 
            placeholder="多个ID用逗号分隔，如：80001,80002,80003"
          />
          <div class="form-tip">
            <el-icon><InfoFilled /></el-icon>
            请输入已存在的题目ID，多个题目用英文逗号分隔
          </div>
        </el-form-item>

        <!-- 开始时间和结束时间（作业和考试都需要） -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始时间" required>
              <el-date-picker
                v-model="formData.startTime"
                type="datetime"
                placeholder="选择开始时间"
                format="YYYY-MM-DD HH:mm"
                value-format="YYYY-MM-DDTHH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间" required>
              <el-date-picker
                v-model="formData.endTime"
                type="datetime"
                placeholder="选择结束时间"
                format="YYYY-MM-DD HH:mm"
                value-format="YYYY-MM-DDTHH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <!-- 时限仅考试类型显示 -->
        <el-form-item v-if="formData.type === 'exam'" label="时限(分钟)" required>
          <el-input-number v-model="formData.timeLimit" :min="1" :max="600" style="width: 100%" />
          <div class="form-tip">
            <el-icon><InfoFilled /></el-icon>
            考试答题时间限制，学生开始考试后倒计时
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">创建</el-button>
      </template>
    </el-dialog>

    <!-- 发布对话框 -->
    <el-dialog
      v-model="publishDialogVisible"
      title="发布作业/考试"
      width="550px"
      class="publish-dialog"
    >
      <el-alert 
        :title="`即将发布：${currentPublishTitle}`" 
        type="info" 
        :closable="false" 
        show-icon
        class="publish-alert"
      />
      
      <el-form :model="publishForm" label-width="120px" class="publish-form">
        <el-form-item label="开始时间" required>
          <el-date-picker
            v-model="publishForm.startTime"
            type="datetime"
            placeholder="选择开始时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 100%"
            :shortcuts="dateShortcuts"
          />
        </el-form-item>
        <el-form-item label="结束时间" required>
          <el-date-picker
            v-model="publishForm.endTime"
            type="datetime"
            placeholder="选择结束时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 100%"
            :shortcuts="dateShortcuts"
          />
        </el-form-item>
        <el-form-item label="答题模式">
          <el-radio-group v-model="publishForm.mode">
            <el-radio-button value="question">
              <el-icon><List /></el-icon> 按题目
            </el-radio-button>
            <el-radio-button value="file">
              <el-icon><Document /></el-icon> 按卷面
            </el-radio-button>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="publishDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPublish" :loading="publishing">确认发布</el-button>
      </template>
    </el-dialog>

    <!-- 编辑作业对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑作业/考试"
      width="600px"
      class="edit-dialog"
      destroy-on-close
    >
      <el-form :model="editForm" label-width="100px" class="edit-form">
        <el-form-item label="标题" required>
          <el-input v-model="editForm.title" placeholder="请输入标题" maxlength="100" show-word-limit />
        </el-form-item>
        
        <el-form-item label="描述">
          <el-input 
            v-model="editForm.description" 
            type="textarea" 
            :rows="3" 
            placeholder="作业/考试描述（可选）"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始时间">
              <el-date-picker
                v-model="editForm.startTime"
                type="datetime"
                placeholder="选择开始时间"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DDTHH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间">
              <el-date-picker
                v-model="editForm.endTime"
                type="datetime"
                placeholder="选择结束时间"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DDTHH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item v-if="editForm.type === 'exam'" label="时限(分钟)">
          <el-input-number v-model="editForm.timeLimit" :min="1" :max="600" style="width: 100%" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit" :loading="editing">保存修改</el-button>
      </template>
    </el-dialog>

    <!-- 作业详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="作业详情" width="700px" class="detail-dialog">
      <div v-if="currentDetail" class="detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="作业ID">
            <el-tag>{{ currentDetail.id }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="类型">
            <el-tag :type="getTypeTagType(currentDetail.type)">
              {{ getTypeText(currentDetail.type) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="标题" :span="2">
            {{ currentDetail.title }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusTagType(currentDetail.status)">
              {{ getStatusText(currentDetail.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="总分">
            <span class="highlight">{{ currentDetail.totalScore || 0 }} 分</span>
          </el-descriptions-item>
          <el-descriptions-item label="题目数量">
            {{ currentDetail.questionCount || 0 }} 题
          </el-descriptions-item>
          <el-descriptions-item v-if="currentDetail.type === 'exam'" label="时限">
            {{ currentDetail.timeLimit || 0 }} 分钟
          </el-descriptions-item>
          <el-descriptions-item v-else label="时限">
            <span class="time-hint">作业无时间限制</span>
          </el-descriptions-item>
          <el-descriptions-item label="开始时间">
            {{ formatTime(currentDetail.startTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="结束时间">
            {{ formatTime(currentDetail.endTime) }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentDetail.description" label="描述" :span="2">
            {{ currentDetail.description }}
          </el-descriptions-item>
        </el-descriptions>
        
        <!-- 题目列表 -->
        <div v-if="currentDetail.questions && currentDetail.questions.length > 0" class="questions-preview">
          <h4>
            <el-icon><List /></el-icon>
            包含题目
          </h4>
          <el-table :data="currentDetail.questions" border size="small" max-height="300">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="title" label="题目" show-overflow-tooltip />
            <el-table-column prop="questionType" label="题型" width="100">
              <template #default="{ row }">
                <el-tag size="small">{{ getQuestionTypeText(row.questionType) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="difficulty" label="难度" width="100">
              <template #default="{ row }">
                <el-rate :model-value="row.difficulty" disabled size="small" />
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Plus, Refresh, Document, EditPen, CircleCheck, Lock, List, View, 
  Promotion, RefreshLeft, Delete, Notification, InfoFilled, Edit
} from '@element-plus/icons-vue'
import { useAssignmentStore } from '../store/assignment'
import { getSession } from '../utils/session'

// Props
const props = defineProps({
  courseId: {
    type: [Number, String],
    required: true
  },
  useExternalDetail: {
    type: Boolean,
    default: false
  }
})
const emit = defineEmits(['view-detail'])

// Store
const assignmentStore = useAssignmentStore()

// Computed from store
const assignments = computed(() => assignmentStore.assignments)
const loading = computed(() => assignmentStore.loading)
const pagination = computed(() => assignmentStore.pagination)

// Local computed
const draftCount = computed(() => 
  assignments.value.filter(a => a.status === 0 || a.status === 'draft').length
)
const publishedCount = computed(() => 
  assignments.value.filter(a => a.status === 1 || a.status === 'published').length
)
const closedCount = computed(() => 
  assignments.value.filter(a => a.status === 2 || a.status === 'closed').length
)

// Local state
const dialogVisible = ref(false)
const publishDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const editDialogVisible = ref(false)
const submitting = ref(false)
const publishing = ref(false)
const editing = ref(false)
const filterType = ref('')
const questionIdsInput = ref('')
const currentPublishId = ref(null)
const currentPublishTitle = ref('')
const currentDetail = ref(null)
const currentEditId = ref(null)

// 搜索表单
const searchForm = reactive({
  status: null
})

const formData = reactive({
  title: '',
  type: 'homework',
  description: '',
  questionIds: [],
  totalScore: 100,
  timeLimit: 60,
  startTime: '',
  endTime: ''
})

const publishForm = reactive({
  assignmentId: null,
  startTime: '',
  endTime: '',
  mode: 'question'
})

// 编辑表单
const editForm = reactive({
  title: '',
  description: '',
  startTime: '',
  endTime: '',
  timeLimit: 60,
  type: 'homework'
})

const dateShortcuts = [
  {
    text: '今天',
    value: new Date()
  },
  {
    text: '明天',
    value: () => {
      const date = new Date()
      date.setTime(date.getTime() + 3600 * 1000 * 24)
      return date
    }
  },
  {
    text: '一周后',
    value: () => {
      const date = new Date()
      date.setTime(date.getTime() + 3600 * 1000 * 24 * 7)
      return date
    }
  }
]

// Methods
const loadAssignments = async () => {
  const session = getSession()
  const params = {
    type: filterType.value || undefined,
    status: searchForm.status !== null ? searchForm.status : undefined
  }
  await assignmentStore.fetchAssignments(props.courseId, params)
}

const refreshList = () => {
  loadAssignments()
  ElMessage.success('列表已刷新')
}

const handleFilterChange = () => {
  assignmentStore.setPage(1)
  loadAssignments()
}

// 搜索方法
const handleSearch = () => {
  assignmentStore.setPage(1)
  loadAssignments()
}

const resetSearch = () => {
  searchForm.status = null
  filterType.value = ''
  assignmentStore.setPage(1)
  loadAssignments()
}

const handleSizeChange = (size) => {
  assignmentStore.setPageSize(size)
  loadAssignments()
}

const handleCurrentChange = (page) => {
  assignmentStore.setPage(page)
  loadAssignments()
}

const showCreateDialog = () => {
  resetFormData()
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!formData.title.trim()) {
    ElMessage.warning('请输入标题')
    return
  }
  if (!questionIdsInput.value.trim()) {
    ElMessage.warning('请输入题目ID')
    return
  }
  if (!formData.startTime || !formData.endTime) {
    ElMessage.warning('请选择开始时间和结束时间')
    return
  }
  if (formData.type === 'exam' && (!formData.timeLimit || formData.timeLimit < 1)) {
    ElMessage.warning('考试类型请设置时限')
    return
  }
  
  submitting.value = true
  try {
    const data = { 
      ...formData,
      courseId: Number(props.courseId),
      timeLimit: formData.type === 'exam' ? formData.timeLimit : null
    }
    // 解析题目ID
    if (questionIdsInput.value) {
      data.questionIds = questionIdsInput.value.split(',').map(id => Number(id.trim())).filter(id => id > 0)
    }
    
    const res = await assignmentStore.addAssignment(data)
    if (res.code === 200) {
      ElMessage.success(`创建成功，作业 ID: ${res.data?.assignmentId || res.data}`)
      dialogVisible.value = false
      loadAssignments()
    } else {
      ElMessage.error(res.message || '创建失败')
    }
  } catch (error) {
    ElMessage.error('创建失败')
  } finally {
    submitting.value = false
  }
}

const canPublish = (row) => {
  return row.status === 0 || row.status === 'draft'
}

const canUnpublish = (row) => {
  return row.status === 1 || row.status === 'published'
}

const showPublishDialog = (row) => {
  currentPublishId.value = row.id
  currentPublishTitle.value = row.title
  publishForm.assignmentId = row.id
  publishForm.startTime = ''
  publishForm.endTime = ''
  publishForm.mode = 'question'
  publishDialogVisible.value = true
}

// 显示编辑对话框
const showEditDialog = async (row) => {
  try {
    const res = await assignmentStore.fetchDetail(row.id)
    if (res.code === 200) {
      const detail = res.data
      currentEditId.value = row.id
      editForm.title = detail.title || ''
      editForm.description = detail.description || ''
      editForm.startTime = detail.startTime || ''
      editForm.endTime = detail.endTime || ''
      editForm.timeLimit = detail.timeLimit || 60
      editForm.type = detail.type || 'homework'
      editDialogVisible.value = true
    } else {
      ElMessage.error('获取详情失败')
    }
  } catch (error) {
    ElMessage.error('获取详情失败')
  }
}

// 提交编辑
const submitEdit = async () => {
  if (!editForm.title.trim()) {
    ElMessage.warning('请输入标题')
    return
  }
  
  editing.value = true
  try {
    const updateData = {
      title: editForm.title,
      description: editForm.description,
      timeLimit: editForm.type === 'exam' ? editForm.timeLimit : null,
      startTime: editForm.startTime || null,
      endTime: editForm.endTime || null
    }
    
    const res = await assignmentStore.editAssignment(currentEditId.value, updateData)
    if (res.code === 200) {
      ElMessage.success('修改成功')
      editDialogVisible.value = false
      loadAssignments()
    } else {
      ElMessage.error(res.message || '修改失败')
    }
  } catch (error) {
    ElMessage.error('修改失败')
  } finally {
    editing.value = false
  }
}

const submitPublish = async () => {
  if (!publishForm.startTime || !publishForm.endTime) {
    ElMessage.warning('请选择开始和结束时间')
    return
  }
  
  publishing.value = true
  try {
    const res = await assignmentStore.publish(publishForm)
    if (res.code === 200) {
      ElMessage.success('发布成功')
      publishDialogVisible.value = false
      loadAssignments()
    } else {
      ElMessage.error(res.message || '发布失败')
    }
  } catch (error) {
    ElMessage.error('发布失败')
  } finally {
    publishing.value = false
  }
}

const handleUnpublish = async (id) => {
  try {
    await ElMessageBox.confirm('确定要撤销发布吗？学生将无法继续访问。', '撤销确认', {
      type: 'warning'
    })
    const res = await assignmentStore.unpublish(id)
    if (res.code === 200) {
      ElMessage.success('撤销成功')
      loadAssignments()
    } else {
      ElMessage.error(res.message || '撤销失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('撤销失败')
    }
  }
}

const deleteRow = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除该作业吗？此操作不可恢复！', '删除确认', {
      type: 'warning',
      confirmButtonText: '确定删除',
      cancelButtonText: '取消'
    })
    const res = await assignmentStore.removeAssignment(id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadAssignments()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const viewDetail = async (id) => {
  try {
    const res = await assignmentStore.fetchDetail(id)
    if (res.code === 200) {
      if (props.useExternalDetail) {
        emit('view-detail', res.data)
      } else {
        currentDetail.value = res.data
        detailDialogVisible.value = true
      }
    } else {
      ElMessage.error('获取详情失败')
    }
  } catch (error) {
    ElMessage.error('获取详情失败')
  }
}

const resetFormData = () => {
  Object.assign(formData, {
    title: '',
    type: 'homework',
    description: '',
    questionIds: [],
    totalScore: 100,
    timeLimit: 60,
    startTime: '',
    endTime: ''
  })
  questionIdsInput.value = ''
}

// Utils
const getTypeText = (type) => {
  const map = { homework: '作业', exam: '考试' }
  return map[type] || type
}

const getStatusText = (status) => {
  if (status === 0 || status === 'draft') return '草稿'
  if (status === 1 || status === 'published') return '已发布'
  if (status === 2 || status === 'closed') return '已关闭'
  return status
}

const getTypeTagType = (type) => {
  const map = { homework: 'primary', exam: 'danger' }
  return map[type] || ''
}

const getStatusTagType = (status) => {
  if (status === 0 || status === 'draft') return 'info'
  if (status === 1 || status === 'published') return 'success'
  if (status === 2 || status === 'closed') return 'danger'
  return ''
}

const getQuestionTypeText = (type) => {
  const map = { single: '单选', multiple: '多选', blank: '填空', short: '简答', code: '编程' }
  return map[type] || type
}

const formatTime = (time) => {
  if (!time) return '--'
  return new Date(time).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// Lifecycle
onMounted(() => {
  loadAssignments()
})

// Watch courseId changes
watch(() => props.courseId, (newVal) => {
  if (newVal) {
    loadAssignments()
  }
})
</script>

<style scoped>
.assignment-manager {
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

.left-actions {
  display: flex;
  gap: 10px;
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

/* Stats cards */
.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 10px 0;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.stat-total .stat-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-draft .stat-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-published .stat-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-closed .stat-icon {
  background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
  color: #666;
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

/* Table card */
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

.assignment-table {
  border-radius: 8px;
  overflow: hidden;
}
.assignment-table :deep(.el-table__row .cell) { padding: 14px 10px }

.assignment-title {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #303133;
}

.question-badge :deep(.el-badge__content) {
  font-weight: 600;
}

.total-score {
  font-weight: 600;
  color: #409EFF;
}

.time-text {
  font-size: 13px;
  color: #909399;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding: 10px 0;
}

/* Dialog styles */
.assignment-dialog :deep(.el-dialog__body),
.publish-dialog :deep(.el-dialog__body) {
  padding: 20px 30px;
}

.form-tip {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #909399;
  margin-top: 6px;
}

.publish-alert {
  margin-bottom: 20px;
}

.publish-form :deep(.el-radio-button__inner) {
  display: flex;
  align-items: center;
  gap: 6px;
}

/* Detail dialog */
.detail-content {
  padding: 10px 0;
}

.highlight {
  color: #409EFF;
  font-weight: 600;
}

.time-hint {
  color: #909399;
  font-style: italic;
}

.questions-preview {
  margin-top: 25px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 8px;
}

.questions-preview h4 {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 15px 0;
  color: #303133;
}

/* 搜索过滤栏样式 */
.filter-card {
  margin-bottom: 20px;
  border-radius: 12px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
}

.search-form :deep(.el-form-item) {
  margin-bottom: 0;
  margin-right: 10px;
}
</style>
