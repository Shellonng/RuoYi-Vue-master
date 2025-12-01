<template>
  <div class="page-container">
    <header class="top">
      <div>
        <h1>批改详情</h1>
        <p class="sub">查看学生作业的详细批改结果</p>
      </div>
      <div class="actions">
        <button class="primary" @click="submitGradingConfirmation" :disabled="isSubmitting">
          {{ isSubmitting ? '提交中...' : '确认批改' }}
        </button>
      </div>
    </header>

    <div class="content-grid">
      <section class="panel submission-info">
        <div class="panel-head">
          <h3>作业信息</h3>
        </div>
        <div class="info-grid" v-if="submissionData">
          <div class="info-item">
            <label>作业标题:</label>
            <span>{{ submissionData.assignment?.title }}</span>
          </div>
          <div class="info-item">
            <label>学生姓名:</label>
            <span>{{ submissionData.user?.realName || submissionData.submission?.studentUserId }}</span>
          </div>
          <div class="info-item">
            <label>提交时间:</label>
            <span>{{ submissionData.submission?.submitTime }}</span>
          </div>
          <div class="info-item" v-if="submissionData.submission?.fileName">
            <label>提交文件:</label>
            <div class="file-info">
              <span>{{ submissionData.submission.fileName }}</span>
              <a 
                :href="`http://localhost:8080/api/files/download/**?filePath=${submissionData.submission.filePath}`" 
                target="_blank" 
                class="download-link"
              >
                下载
              </a>
            </div>
          </div>
        </div>
      </section>

      <section class="panel grading-result" v-if="gradingResult">
        <div class="panel-head">
          <h3>批改结果</h3>
        </div>
        
        <!-- 分数输入区域 -->
        <div class="scores-input" v-if="isEditing">
          <div class="score-input-item">
            <label>内容分:</label>
            <input v-model.number="editScores.contentScore" type="number" min="0" max="100" />
          </div>
          <div class="score-input-item">
            <label>逻辑分:</label>
            <input v-model.number="editScores.logicScore" type="number" min="0" max="100" />
          </div>
          <div class="score-input-item">
            <label>知识分:</label>
            <input v-model.number="editScores.knowledgeScore" type="number" min="0" max="100" />
          </div>
          <div class="score-input-item">
            <label>创新分:</label>
            <input v-model.number="editScores.innovationScore" type="number" min="0" max="100" />
          </div>
          <div class="score-input-item total">
            <label>总分:</label>
            <input v-model.number="editScores.totalScore" type="number" min="0" max="100" />
          </div>
        </div>
        
        <!-- 分数展示区域 -->
        <div class="scores" v-else>
          <div class="score-item">
            <label>内容分:</label>
            <span>{{ gradingResult.contentScore }}</span>
          </div>
          <div class="score-item">
            <label>逻辑分:</label>
            <span>{{ gradingResult.logicScore }}</span>
          </div>
          <div class="score-item">
            <label>知识分:</label>
            <span>{{ gradingResult.knowledgeScore }}</span>
          </div>
          <div class="score-item">
            <label>创新分:</label>
            <span>{{ gradingResult.innovationScore }}</span>
          </div>
          <div class="score-item total">
            <label>总分:</label>
            <span>{{ gradingResult.totalScore }}</span>
          </div>
        </div>

        <!-- 评语编辑区域 -->
        <div class="feedback-section" v-if="isEditing">
          <h4>教师评语</h4>
          <textarea v-model="editComment" placeholder="请输入教师评语"></textarea>
        </div>
        
        <!-- 评语展示区域 -->
        <div class="feedback-section" v-else>
          <h4>AI评语</h4>
          <div class="feedback-content">
            {{ gradingResult.aiComment }}
          </div>
        </div>

        <div class="feedback-section" v-if="!isEditing">
          <h4>改进建议</h4>
          <div class="feedback-content">
            {{ gradingResult.improvementSuggestions }}
          </div>
        </div>

        <div class="feedback-section" v-if="!isEditing">
          <h4>覆盖知识点</h4>
          <div class="feedback-content">
            {{ gradingResult.coveredKnowledgePoints }}
          </div>
        </div>

        <div class="feedback-section" v-if="!isEditing">
          <h4>缺失知识点</h4>
          <div class="feedback-content">
            {{ gradingResult.missingKnowledgePoints }}
          </div>
        </div>
        
        <!-- 编辑按钮 -->
        <div class="edit-actions" v-if="!isEditing">
          <button @click="startEdit" class="secondary">修改分数和评语</button>
        </div>
      </section>

      <section class="panel knowledge-points" v-if="submissionData">
        <div class="panel-head">
          <h3>关联知识点</h3>
        </div>
        <ul class="kp-list">
          <li v-for="kp in submissionData.knowledgePoints" :key="kp.id">
            {{ kp.title }}
          </li>
          <li v-if="!submissionData.knowledgePoints?.length" class="empty">无关联知识点</li>
        </ul>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from "vue";
import { useRoute } from "vue-router";
import { getSubmissionDetail, getGradingResult, confirmGrading } from "../../utils/api.js";
import { getUser } from "../../utils/session.js";

const route = useRoute();
const submissionData = ref(null);
const gradingResult = ref(null);
const isEditing = ref(false);
const isSubmitting = ref(false);

// 编辑时的分数和评语
const editScores = reactive({
  contentScore: 0,
  logicScore: 0,
  knowledgeScore: 0,
  innovationScore: 0,
  totalScore: 0
});

const editComment = ref("");

onMounted(async () => {
  const submissionId = route.params.submissionId;
  if (submissionId) {
    await loadSubmissionDetail(submissionId);
    await loadGradingResult(submissionId);
  }
});

async function loadSubmissionDetail(submissionId) {
  try {
    submissionData.value = await getSubmissionDetail(submissionId);
  } catch (e) {
    console.error("加载提交详情失败:", e);
  }
}

async function loadGradingResult(submissionId) {
  try {
    gradingResult.value = await getGradingResult(submissionId);
    
    // 初始化编辑数据
    if (gradingResult.value) {
      editScores.contentScore = gradingResult.value.contentScore;
      editScores.logicScore = gradingResult.value.logicScore;
      editScores.knowledgeScore = gradingResult.value.knowledgeScore;
      editScores.innovationScore = gradingResult.value.innovationScore;
      editScores.totalScore = gradingResult.value.totalScore;
      editComment.value = gradingResult.value.teacherComment || "";
    }
  } catch (e) {
    console.error("加载批改结果失败:", e);
  }
}

function startEdit() {
  isEditing.value = true;
}

async function submitGradingConfirmation() {
  const submissionId = route.params.submissionId;
  const user = getUser();
  const teacherId = user?.id;
  
  if (!teacherId) {
    alert("无法获取教师信息，请重新登录");
    return;
  }
  
  isSubmitting.value = true;
  
  try {
    const gradingData = isEditing.value ? {
      contentScore: editScores.contentScore,
      logicScore: editScores.logicScore,
      knowledgeScore: editScores.knowledgeScore,
      innovationScore: editScores.innovationScore,
      totalScore: editScores.totalScore,
      teacherComment: editComment.value
    } : {};
    
    await confirmGrading(submissionId, teacherId, gradingData);
    alert("批改已确认");
    
    // 退出编辑模式并重新加载数据
    isEditing.value = false;
    await loadGradingResult(submissionId);
  } catch (e) {
    console.error("确认批改失败:", e);
    alert("确认批改失败: " + e.message);
  } finally {
    isSubmitting.value = false;
  }
}
</script>

<style scoped>
.page-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
  color: #1e293b;
}

.top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
  margin-bottom: 32px;
  padding: 20px 24px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(12px);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
  position: sticky;
  top: 20px;
  z-index: 10;
}

h1 {
  font-size: 24px;
  font-weight: 800;
  background: linear-gradient(135deg, #1e293b 0%, #334155 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin: 0 0 4px 0;
  letter-spacing: -0.5px;
}

.sub {
  color: #64748b;
  font-size: 14px;
  margin: 0;
}

button {
  padding: 10px 20px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  border: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.primary {
  background: linear-gradient(135deg, #4f46e5 0%, #3b82f6 100%);
  color: #fff;
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.2);
}

.primary:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(79, 70, 229, 0.3);
}

.primary:active:not(:disabled) {
  transform: translateY(0);
}

.primary:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  background: #94a3b8;
  box-shadow: none;
}

.secondary {
  background: #fff;
  border: 1px solid #e2e8f0;
  color: #475569;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.secondary:hover {
  background: #f8fafc;
  border-color: #cbd5e1;
  color: #1e293b;
}

.content-grid {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 24px;
  align-items: start;
}

.panel {
  background: #fff;
  border-radius: 20px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 10px 30px -10px rgba(0, 0, 0, 0.05);
  overflow: hidden;
  padding: 24px;
}

.grading-result {
  grid-column: 1;
  grid-row: 1 / span 2;
}

.submission-info {
  grid-column: 2;
  grid-row: 1;
}

.knowledge-points {
  grid-column: 2;
  grid-row: 2;
}

.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f1f5f9;
}

.panel-head h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: #1e293b;
}

.info-grid {
  display: grid;
  gap: 16px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-item label {
  font-size: 12px;
  font-weight: 600;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.info-item span {
  font-size: 15px;
  color: #0f172a;
  font-weight: 500;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 12px;
  background: #f8fafc;
  padding: 8px 12px;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}

.download-link {
  color: #2563eb;
  text-decoration: none;
  font-size: 13px;
  font-weight: 600;
  padding: 4px 10px;
  background: #eff6ff;
  border-radius: 6px;
  transition: all 0.2s;
}

.download-link:hover {
  background: #dbeafe;
}

.scores, .scores-input {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 32px;
}

.score-item, .score-input-item {
  text-align: center;
  padding: 20px;
  border-radius: 16px;
  background: #f8fafc;
  border: 1px solid #f1f5f9;
  transition: all 0.2s;
}

.score-item:hover {
  background: #fff;
  border-color: #e2e8f0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  transform: translateY(-2px);
}

.score-item.total, .score-input-item.total {
  grid-column: span 4;
  background: linear-gradient(135deg, #eff6ff 0%, #f0f9ff 100%);
  border-color: #dbeafe;
}

.score-item label, .score-input-item label {
  display: block;
  margin-bottom: 8px;
  color: #64748b;
  font-size: 13px;
  font-weight: 600;
}

.score-item span {
  font-size: 32px;
  font-weight: 800;
  color: #0f172a;
  display: block;
  line-height: 1;
}

.score-item.total span {
  font-size: 48px;
  background: linear-gradient(135deg, #2563eb 0%, #3b82f6 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.score-input-item input {
  width: 100%;
  padding: 8px;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  font-size: 24px;
  font-weight: 700;
  text-align: center;
  color: #0f172a;
  outline: none;
  transition: all 0.2s;
}

.score-input-item input:focus {
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.feedback-section {
  margin-bottom: 24px;
}

.feedback-section h4 {
  margin: 0 0 12px 0;
  color: #1e293b;
  font-size: 16px;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 8px;
}

.feedback-section h4::before {
  content: '';
  width: 4px;
  height: 16px;
  background: #3b82f6;
  border-radius: 2px;
}

.feedback-content {
  padding: 16px;
  background: #f8fafc;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  color: #334155;
  line-height: 1.6;
  font-size: 15px;
}

.feedback-section textarea {
  width: 100%;
  min-height: 120px;
  padding: 16px;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  resize: vertical;
  font-family: inherit;
  font-size: 15px;
  outline: none;
  transition: all 0.2s;
}

.feedback-section textarea:focus {
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.kp-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.kp-list li {
  padding: 6px 12px;
  background: #f1f5f9;
  border-radius: 20px;
  font-size: 13px;
  color: #475569;
  font-weight: 500;
}

.kp-list .empty {
  background: transparent;
  color: #94a3b8;
  font-style: italic;
  padding: 0;
}

.edit-actions {
  text-align: center;
  padding-top: 24px;
  border-top: 1px solid #f1f5f9;
  margin-top: 24px;
}

@media (max-width: 1024px) {
  .content-grid {
    grid-template-columns: 1fr;
  }
  .grading-result {
    grid-column: 1;
    grid-row: 2;
  }
  .submission-info {
    grid-column: 1;
    grid-row: 1;
  }
  .knowledge-points {
    grid-column: 1;
    grid-row: 3;
  }
}

@media (max-width: 640px) {
  .scores, .scores-input {
    grid-template-columns: repeat(2, 1fr);
  }
  .score-item.total, .score-input-item.total {
    grid-column: span 2;
  }
}
</style>