<template>
  <div class="student-assignments-page">
    <header class="page-header">
      <div class="header-content">
        <div class="header-left">
          <h1>
            <el-icon class="header-icon"><Document /></el-icon>
            我的作业
          </h1>
          <p class="sub-title">查看作业详情和完成情况</p>
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
      <el-card class="table-card" shadow="hover">
        <template #header>
          <div class="table-header">
            <span class="title">
              <el-icon><List /></el-icon>
              作业列表
            </span>
            <el-button :icon="Refresh" circle @click="fetchAssignments" />
          </div>
        </template>
        
        <el-table 
          :data="assignments" 
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
          <el-table-column prop="title" label="作业标题" min-width="200" show-overflow-tooltip>
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
               <!-- For now just show published status, ideally should show submission status -->
                <el-tag :type="getStatusTagType(row.status)" effect="light">
                  {{ getStatusText(row.status) }}
                </el-tag>
             </template>
          </el-table-column>
          <el-table-column prop="endTime" label="截止时间" width="180" align="center">
            <template #default="{ row }">
              {{ formatTime(row.endTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right" align="center">
            <template #default="{ row }">
              <el-button 
                size="small" 
                type="primary" 
                :icon="View" 
                @click="viewDetail(row)"
              >
                查看详情
              </el-button>
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

    <!-- 作业详情对话框 (Read Only) -->
    <el-dialog
      v-model="dialogVisible"
      title="作业详情"
      width="800px"
      destroy-on-close
    >
      <div v-if="currentAssignment" class="assignment-detail">
        <div class="detail-header">
          <h3>{{ currentAssignment.title }}</h3>
          <div class="detail-meta">
             <el-tag :type="getTypeTagType(currentAssignment.type)">{{ getTypeText(currentAssignment.type) }}</el-tag>
             <span class="time">截止时间: {{ formatTime(currentAssignment.endTime) }}</span>
          </div>
        </div>
        <div class="detail-desc">
          <p>{{ currentAssignment.description || '暂无描述' }}</p>
        </div>
        <!-- Placeholder for questions list or start button -->
        <div class="detail-actions">
           <el-button type="primary" @click="startAssignment(currentAssignment)">开始答题</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { Document, Reading, List, Refresh, View, Notification } from '@element-plus/icons-vue'
import { getSelectedCourse } from '../../utils/session'
import { getAssignmentList } from '../../api/assignment'
import { ElMessage } from 'element-plus'

const currentCourse = ref(null)
const loading = ref(false)
const assignments = ref([])
const pagination = ref({
  page: 1,
  pageSize: 10,
  total: 0
})

const dialogVisible = ref(false)
const currentAssignment = ref(null)

const courseId = computed(() => currentCourse.value?.id)

const fetchAssignments = async () => {
  if (!courseId.value) return
  
  loading.value = true
  try {
    const res = await getAssignmentList({
      courseId: courseId.value,
      page: pagination.value.page,
      pageSize: pagination.value.pageSize
    })
    
    if (res.code === 200) {
      assignments.value = res.data?.items || []
      pagination.value.total = res.data?.pagination?.total || 0
    }
  } catch (error) {
    console.error('Failed to fetch assignments:', error)
    ElMessage.error('获取作业列表失败')
  } finally {
    loading.value = false
  }
}

const handleSizeChange = (val) => {
  pagination.value.pageSize = val
  fetchAssignments()
}

const handleCurrentChange = (val) => {
  pagination.value.page = val
  fetchAssignments()
}

const viewDetail = (row) => {
  currentAssignment.value = row
  dialogVisible.value = true
}

const startAssignment = (row) => {
  ElMessage.info('答题功能开发中...')
}

// Helper functions
const getTypeTagType = (type) => {
  const map = {
    homework: '',
    exam: 'danger',
    practice: 'success'
  }
  return map[type] || 'info'
}

const getTypeText = (type) => {
  const map = {
    homework: '普通作业',
    exam: '考试',
    practice: '练习'
  }
  return map[type] || '未知'
}

const getStatusTagType = (status) => {
  // Assuming 1 is published
  return status === 1 ? 'success' : 'info'
}

const getStatusText = (status) => {
  return status === 1 ? '已发布' : (status === 0 ? '草稿' : '已结束')
}

const formatTime = (time) => {
  if (!time) return '无限制'
  return new Date(time).toLocaleString()
}

onMounted(() => {
  currentCourse.value = getSelectedCourse()
  if (currentCourse.value) {
    fetchAssignments()
  }
})
</script>

<style scoped>
.student-assignments-page {
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

.assignment-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.detail-header {
  margin-bottom: 20px;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}

.detail-meta {
  margin-top: 10px;
  display: flex;
  gap: 20px;
  color: #666;
  font-size: 14px;
}

.detail-desc {
  margin-bottom: 20px;
  line-height: 1.6;
}

.detail-actions {
  display: flex;
  justify-content: flex-end;
}
</style>
