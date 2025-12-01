<template>
  <div class="student-question-bank-page">
    <header class="page-header">
      <div class="header-content">
        <div class="header-left">
          <h1>
            <el-icon class="header-icon"><Collection /></el-icon>
            题库练习
          </h1>
          <p class="sub-title">浏览题目，自主练习与巩固</p>
        </div>
        <div class="header-right">
          <el-tag type="primary" effect="dark" size="large" class="course-tag">
            <el-icon><Reading /></el-icon>
            {{ currentCourse?.title || '未选择课程' }}
          </el-tag>
        </div>
      </div>
    </header>

    <main class="page-content">
      <!-- 筛选区 -->
      <el-card class="filter-card" shadow="hover">
        <el-form :inline="true" :model="queryForm" class="filter-form">
          <el-form-item label="题型">
            <el-select v-model="queryForm.questionType" placeholder="全部题型" clearable style="width: 120px">
              <el-option label="单选题" value="single" />
              <el-option label="多选题" value="multiple" />
              <el-option label="填空题" value="blank" />
              <el-option label="简答题" value="short" />
              <el-option label="编程题" value="code" />
            </el-select>
          </el-form-item>
          <el-form-item label="难度">
            <el-select v-model="queryForm.difficulty" placeholder="全部难度" clearable style="width: 120px">
              <el-option v-for="i in 5" :key="i" :label="`难度 ${i}`" :value="i">
                <el-rate :model-value="i" disabled :max="5" size="small" />
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch" :icon="Search">查询</el-button>
            <el-button @click="resetQuery" :icon="Refresh">重置</el-button>
          </el-form-item>
        </el-form>
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
          row-key="id"
        >
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
          <el-table-column label="操作" width="120" fixed="right" align="center">
            <template #default="{ row }">
              <el-button size="small" type="primary" :icon="View" @click="viewDetail(row)">查看/练习</el-button>
            </template>
          </el-table-column>
        </el-table>

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
    </main>

    <!-- 题目详情/练习对话框 -->
    <el-dialog v-model="detailDialogVisible" title="题目练习" width="700px">
      <div v-if="currentDetail" class="detail-content">
        <div class="question-header">
          <el-tag :type="getQuestionTypeTag(currentDetail.questionType)" class="mr-2">
            {{ getQuestionTypeText(currentDetail.questionType) }}
          </el-tag>
          <span class="question-id">ID: {{ currentDetail.id }}</span>
        </div>
        
        <div class="question-body">
          <div class="question-text">{{ currentDetail.title }}</div>
          
          <div v-if="currentDetail.options && currentDetail.options.length > 0" class="options-list">
            <div v-for="opt in currentDetail.options" :key="opt.optionKey" class="option-item">
              <span class="option-key">{{ opt.optionKey }}.</span>
              <span class="option-text">{{ opt.content }}</span>
            </div>
          </div>
        </div>

        <div class="answer-section">
          <el-button type="success" plain @click="showAnswer = !showAnswer">
            {{ showAnswer ? '隐藏答案' : '查看答案' }}
          </el-button>
          
          <div v-if="showAnswer" class="answer-content">
            <p><strong>正确答案：</strong> {{ currentDetail.correctAnswer }}</p>
            <p><strong>解析：</strong> {{ currentDetail.explanation || '暂无解析' }}</p>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { Collection, Reading, Search, Refresh, List, View } from '@element-plus/icons-vue'
import { getSelectedCourse } from '../../utils/session'
import { useQuestionStore } from '../../store/question'

const questionStore = useQuestionStore()
const currentCourse = ref(null)

const queryForm = reactive({
  questionType: '',
  difficulty: null
})

const detailDialogVisible = ref(false)
const currentDetail = ref(null)
const showAnswer = ref(false)

const questions = computed(() => questionStore.questions)
const loading = computed(() => questionStore.loading)
const pagination = computed(() => questionStore.pagination)

const handleSearch = () => {
  if (!currentCourse.value?.id) return
  questionStore.fetchQuestions(currentCourse.value.id, queryForm)
}

const resetQuery = () => {
  queryForm.questionType = ''
  queryForm.difficulty = null
  handleSearch()
}

const handleSizeChange = (val) => {
  questionStore.setPageSize(val)
  handleSearch()
}

const handleCurrentChange = (val) => {
  questionStore.setPage(val)
  handleSearch()
}

const viewDetail = (row) => {
  currentDetail.value = row
  showAnswer.value = false
  detailDialogVisible.value = true
}

const getQuestionTypeTag = (type) => {
  const map = {
    single: 'primary',
    multiple: 'success',
    blank: 'warning',
    short: 'info',
    code: 'danger'
  }
  return map[type] || 'info'
}

const getQuestionTypeText = (type) => {
  const map = {
    single: '单选题',
    multiple: '多选题',
    blank: '填空题',
    short: '简答题',
    code: '编程题'
  }
  return map[type] || '未知'
}

onMounted(() => {
  currentCourse.value = getSelectedCourse()
  if (currentCourse.value) {
    handleSearch()
  }
})
</script>

<style scoped>
.student-question-bank-page {
  min-height: 100%;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4ecf7 100%);
}

.page-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 25px 30px;
  margin-bottom: 20px;
  color: white;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.4);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left h1 {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 0;
  font-size: 26px;
  font-weight: 700;
}

.header-icon {
  background: rgba(255, 255, 255, 0.2);
  padding: 8px;
  border-radius: 10px;
}

.sub-title {
  margin: 8px 0 0 0;
  opacity: 0.9;
  font-size: 14px;
}

.course-tag {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 16px;
  font-size: 14px;
}

.page-content {
  padding: 0 20px 20px;
}

.filter-card {
  margin-bottom: 20px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: bold;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.question-header {
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.question-id {
  color: #999;
  font-size: 12px;
}

.question-body {
  font-size: 16px;
  margin-bottom: 20px;
}

.question-text {
  margin-bottom: 15px;
  font-weight: 500;
}

.options-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.option-item {
  display: flex;
  gap: 10px;
  padding: 8px;
  background: #f8f9fa;
  border-radius: 4px;
}

.option-key {
  font-weight: bold;
  color: #409eff;
}

.answer-section {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px dashed #ccc;
}

.answer-content {
  margin-top: 15px;
  padding: 15px;
  background: #f0f9eb;
  border-radius: 4px;
  color: #67c23a;
}
</style>
