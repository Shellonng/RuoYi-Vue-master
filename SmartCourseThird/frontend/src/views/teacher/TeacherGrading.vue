<template>
  <div class="page-container">
    <header class="top">
      <div>
        <h1>智能批改</h1>
        <p class="sub">查看待批改队列，支持一键触发。</p>
      </div>
      <div class="filters">
        <select v-model="selectedModel" class="course-select model-select">
          <option value="" disabled>选择 AI 模型</option>
          <option v-for="model in models" :key="model" :value="model">
            {{ model }}
          </option>
        </select>


        <button class="ghost" @click="triggerGrading" :disabled="isTriggering">
          {{ isTriggering ? '触发中...' : '批量触发' }}
        </button>
      </div>
    </header>

    <div class="tabs">
      <button 
        :class="['tab-btn', { active: activeTab === 'pending' }]" 
        @click="switchTab('pending')"
      >
        待批改
      </button>
      <button 
        :class="['tab-btn', { active: activeTab === 'unresolved' }]" 
        @click="switchTab('unresolved')"
      >
        处理中/失败
      </button>
    </div>

    <section class="panel">
      <div class="panel-head">
        <h3>{{ activeTab === 'pending' ? '待批改列表' : '异常状态列表' }}</h3>
        <span class="hint">{{ activeTab === 'pending' ? '/api/ai-grading/pending' : '/api/ai-grading/unresolved' }}</span>
      </div>
      
      <ul class="list" v-if="activeTab === 'pending'">
        <li v-for="item in pending" :key="item.submission.id" @click="goToDetail(item.submission.id)">
          <div class="item-main">
            <span class="title">{{ item.assignment.title || ('提交 #' + item.submission.id) }}</span>
            <div class="tags">

              <span class="tag">学生 {{ item.submission.studentUserId }}</span>
            </div>
          </div>
          <span class="status-badge pending">待批改</span>
        </li>
        <li v-if="!pending.length" class="ghost center">暂无待批改</li>
      </ul>

      <ul class="list" v-else>
        <li v-for="item in unresolved" :key="item.submission.id" @click="goToDetail(item.submission.id)">
          <div class="item-main">
            <span class="title">{{ item.assignment.title || ('提交 #' + item.submission.id) }}</span>
            <div class="tags">

              <span class="tag">学生 {{ item.submission.studentUserId }}</span>
            </div>
          </div>
          <div class="status-info">
            <span :class="['status-badge', item.grading?.status]">{{ item.grading?.status }}</span>
            <span v-if="item.grading?.status === 'failed'" class="error-msg" :title="item.grading?.errorMessage">
              {{ item.grading?.errorMessage || '未知错误' }}
            </span>
          </div>
        </li>
        <li v-if="!unresolved.length" class="ghost center">暂无异常记录</li>
      </ul>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { request } from "../../utils/api.js";
import { getSelectedCourse } from "../../utils/session.js";

const router = useRouter();
const currentCourse = ref(null);
const models = ref([]);
const selectedModel = ref("Qwen/Qwen3-8B"); // 默认模型
const isTriggering = ref(false);

const activeTab = ref("pending");
const pending = ref([]);
const unresolved = ref([]);


// 获取模型列表
async function fetchModels() {
  try {
    const data = await request("/api/ai-grading/models");
    if (Array.isArray(data)) {
      models.value = data;
      // 如果默认模型不在列表中，选中第一个
      if (!models.value.includes(selectedModel.value) && models.value.length > 0) {
        selectedModel.value = models.value[0];
      }
    }
  } catch (e) {
    console.warn("获取模型列表失败", e);
  }
}

async function fetchPending() {
  try {
    const params = new URLSearchParams();
    if (currentCourse.value) {
      params.append('courseId', currentCourse.value.id);
    }
    
    const data = await request(`/api/ai-grading/pending?${params.toString()}`);
    pending.value = data || [];
  } catch (e) {
    console.warn("获取待批改列表失败", e);
    pending.value = [];
  }
}

async function fetchUnresolved() {
  try {
    const params = new URLSearchParams();
    if (currentCourse.value) {
      params.append('courseId', currentCourse.value.id);
    }
    
    const data = await request(`/api/ai-grading/unresolved?${params.toString()}`);
    unresolved.value = data || [];
  } catch (e) {
    console.warn("获取异常列表失败", e);
    unresolved.value = [];
  }
}

function refreshList() {
  if (activeTab.value === 'pending') {
    fetchPending();
  } else {
    fetchUnresolved();
  }
}

function switchTab(tab) {
  activeTab.value = tab;
  refreshList();
}

async function triggerGrading() {
  const currentList = activeTab.value === 'pending' ? pending.value : unresolved.value;
  // 获取当前列表所有项目的submissionId
  const submissionIds = currentList.map(item => item.submission.id);
  
  if (submissionIds.length === 0) {
    alert("当前列表没有可触发的项目");
    return;
  }

  if (!selectedModel.value) {
    alert("请先选择 AI 模型");
    return;
  }
  
  isTriggering.value = true;
  try {
    await request("/api/ai-grading/grading/batch", {
      method: "POST",
      body: JSON.stringify({ 
        submissionIds,
        llmModel: selectedModel.value
      })
    });
    alert("已触发批量批改，请稍后刷新查看结果");
    refreshList();
  } catch (e) {
    console.error("触发批改失败", e);
    alert("触发批改失败: " + e.message);
  } finally {
    isTriggering.value = false;
  }
}

function goToDetail(submissionId) {
  router.push(`/teacher/grading/${submissionId}`);
}

onMounted(() => {
  currentCourse.value = getSelectedCourse();
  if (currentCourse.value) {
    refreshList();
  }
  fetchModels();
  
  fetchModels();
});
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

.filters {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  align-items: center;
}

select {
  padding: 10px 16px;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  background: #fff;
  color: #475569;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s ease;
  cursor: pointer;
  outline: none;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

select:hover {
  border-color: #cbd5e1;
}

select:focus {
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.course-select {
  min-width: 140px;
}

.model-select {
  min-width: 200px;
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

.ghost {
  background: #fff;
  border: 1px solid #e2e8f0;
  color: #475569;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.ghost:hover:not(:disabled) {
  background: #f8fafc;
  border-color: #cbd5e1;
  color: #1e293b;
}

.tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 24px;
  padding: 4px;
  background: #f1f5f9;
  border-radius: 14px;
  width: fit-content;
}

.tab-btn {
  padding: 8px 20px;
  border-radius: 10px;
  color: #64748b;
  font-weight: 600;
  background: transparent;
  box-shadow: none;
}

.tab-btn:hover {
  color: #475569;
  background: rgba(255, 255, 255, 0.5);
}

.tab-btn.active {
  background: #fff;
  color: #3b82f6;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.panel {
  background: #fff;
  border-radius: 20px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 10px 30px -10px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.panel-head {
  padding: 20px 24px;
  border-bottom: 1px solid #f1f5f9;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
}

.panel-head h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: #1e293b;
}

.hint {
  font-family: 'JetBrains Mono', monospace;
  font-size: 12px;
  color: #94a3b8;
  background: #f8fafc;
  padding: 4px 8px;
  border-radius: 6px;
}

.list {
  list-style: none;
  padding: 12px;
  margin: 0;
  display: grid;
  gap: 12px;
  background: #f8fafc;
}

.list li {
  background: #fff;
  padding: 16px 20px;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all 0.2s ease;
  cursor: pointer;
  position: relative;
}

.list li:hover {
  border-color: #3b82f6;
  transform: translateY(-2px);
  box-shadow: 0 10px 20px -5px rgba(59, 130, 246, 0.1);
  z-index: 1;
}

.item-main {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.title {
  font-weight: 600;
  color: #1e293b;
  font-size: 15px;
}

.tags {
  display: flex;
  gap: 8px;
}

.tag {
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.3px;
}

.tag:first-child {
  background: #eff6ff;
  color: #3b82f6;
}

.tag:last-child {
  background: #f0fdf4;
  color: #16a34a;
}

.status-badge {
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.status-badge::before {
  content: '';
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

.status-badge.pending {
  background: #fff7ed;
  color: #ea580c;
  border: 1px solid #ffedd5;
}
.status-badge.pending::before { background: #ea580c; }

.status-badge.processing {
  background: #eff6ff;
  color: #2563eb;
  border: 1px solid #dbeafe;
}
.status-badge.processing::before { background: #2563eb; }

.status-badge.failed {
  background: #fef2f2;
  color: #dc2626;
  border: 1px solid #fee2e2;
}
.status-badge.failed::before { background: #dc2626; }

.error-msg {
  font-size: 12px;
  color: #ef4444;
  margin-top: 4px;
}

.ghost.center {
  text-align: center;
  color: #94a3b8;
  padding: 40px;
  background: transparent;
  border: 2px dashed #e2e8f0;
  box-shadow: none;
}

@media (max-width: 768px) {
  .top {
    flex-direction: column;
    align-items: flex-start;
  }
  .filters {
    width: 100%;
  }
  .filters select, .filters button {
    flex: 1;
  }
}
</style>