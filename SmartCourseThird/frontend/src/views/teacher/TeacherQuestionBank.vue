<template>
  <div class="question-bank-page">
    <!-- 页面头部 -->
    <header class="page-header">
      <div class="header-content">
        <div class="header-left">
          <h1>
            <el-icon class="header-icon"><Collection /></el-icon>
            题库管理
          </h1>
          <p class="sub-title">管理课程题目，支持智能组卷</p>
        </div>
        <div class="header-right">
          <el-tag type="primary" effect="dark" size="large" class="course-tag">
            <el-icon><Reading /></el-icon>
            {{ currentCourse?.title || '未选择课程' }}
          </el-tag>
        </div>
      </div>
    </header>

    <!-- 主内容区 -->
    <main class="page-content">
      <!-- 题库管理组件 -->
      <QuestionBankManager 
        v-if="courseId"
        :course-id="courseId"
        @open-smart-paper="openSmartPaper"
      />
      
      <el-empty v-else description="请先选择课程" />
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Collection, Reading } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getSession, getSelectedCourse } from '../../utils/session'
import QuestionBankManager from '../../components/QuestionBankManager.vue'

const router = useRouter()
const session = ref(getSession() || {})
const currentCourse = ref(null)

const courseId = computed(() => currentCourse.value?.id || null)

const openSmartPaper = () => {
  router.push('/teacher/smart-paper')
}

onMounted(() => {
  currentCourse.value = getSelectedCourse()
  if (!currentCourse.value) {
    ElMessage.warning('请先选择课程')
    router.push('/courses')
  }
})
</script>

<style scoped>
.question-bank-page {
  min-height: 100%;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4ecf7 100%);
}

.page-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 25px 30px;
  margin: -20px -24px 25px -24px;
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
  padding: 0;
}
</style>
