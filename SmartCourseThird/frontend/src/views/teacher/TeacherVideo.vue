<template>
  <div class="video-analysis-layout">
    <aside class="video-sidebar">
      <div class="sidebar-header">
        <h3>课程视频</h3>
        <span class="count">{{ videoList.length }} 个视频</span>
      </div>
      <div class="video-list">
        <div
          v-for="video in videoList"
          :key="video.id"
          class="video-item"
          :class="{ active: currentVideo?.id === video.id }"
          @click="selectVideo(video)"
        >
          <div class="video-icon">▶</div>
          <div class="video-info">
            <div class="video-title">{{ video.title }}</div>
            <div class="video-meta">{{ formatDuration(video.duration) }}</div>
          </div>
        </div>
        <div v-if="!videoList.length" class="empty-state">暂无视频</div>
      </div>
    </aside>

    <main class="video-content">
      <template v-if="videoList.length > 0">
        <header class="content-header">
          <div v-if="currentVideo">
            <h2>{{ currentVideo.title }}</h2>
            <p class="desc">{{ currentVideo.description || "暂无描述" }}</p>
          </div>
          <div v-else>
            <h2>视频学习分析</h2>
            <p class="desc">请选择左侧视频查看详细分析</p>
          </div>
        </header>

        <div v-if="currentVideo" class="analysis-dashboard">
          <!-- Left Column: Video Specific Analysis -->
          <div class="video-analysis-col">
            <!-- 核心指标卡片 -->
            <div class="stats-cards">
              <div class="card stat-card">
                <div class="label">完课率 (Liquid)</div>
                <div ref="liquidRef" class="g2-chart"></div>
              </div>
              <div class="card stat-card">
                <div class="label">平均进度 (Gauge)</div>
                <div ref="gaugeRef" class="g2-chart"></div>
              </div>
              <div class="card stat-card">
                <div class="label">总观看时长</div>
                <div class="value">{{ formatDuration(completionStats.totalWatchDuration) }}</div>
                <div class="sub">观看人数: {{ completionStats.uniqueStudents }}</div>
              </div>
            </div>

            <!-- 视频热力图 -->
            <section class="card heatmap-section">
              <h3>观看热力图</h3>
              <p class="hint">展示视频各时间段的观看频率，颜色越深表示观看次数越多。</p>
              <div ref="heatmapRef" class="chart heatmap-chart"></div>
            </section>

            <!-- 完课率分布 (Rose Chart) -->
            <section class="card distribution-section">
              <h3>完课率分布 (Rose)</h3>
              <div ref="roseRef" class="chart distribution-chart"></div>
        </section>
      </div>

      <!-- Right Column: Course Level Insights -->
      <div class="course-insights-col">
            <!-- 整体行为相关性分析 -->
            <section class="card correlation-section">
              <h3>学习行为与成绩相关性</h3>
              <p class="hint">分析观看时长、完课率与最终成绩的关系。</p>
              
              <div class="correlation-content">
                <div ref="correlationRef" class="chart correlation-chart-small"></div>
                <p class="hint">样本人数：{{ correlationData.sampleSize }}（绘制点数：{{ correlationData.samples.length }}）</p>
                <div class="correlation-stats">
                  <div class="stat-row">
                    <span>完课率相关系数</span>
                    <span :class="getCorrelationClass(correlationData.completionRateCorrelation)">
                      {{ correlationData.completionRateCorrelation }}
                    </span>
                  </div>
                  <div class="stat-row">
                    <span>观看时长相关系数</span>
                    <span :class="getCorrelationClass(correlationData.durationCorrelation)">
                      {{ correlationData.durationCorrelation }}
                    </span>
                  </div>
                  <div class="stat-row">
                    <span>观看次数相关系数</span>
                    <span :class="getCorrelationClass(correlationData.watchCountCorrelation)">
                      {{ correlationData.watchCountCorrelation }}
                    </span>
                  </div>
                </div>
              </div>
        </section>

        <!-- AI 分析区域 -->
        <section class="card ai-section">
          <div class="ai-header">
            <div class="header-left">
              <h3>AI 学习洞察</h3>
              <p class="hint">基于大模型分析学习行为数据</p>
            </div>
          </div>
          
          <div class="ai-controls">
            <button @click="analyzeAi" :disabled="aiLoading" class="btn-ai">
              <span v-if="aiLoading" class="spinner"></span>
              {{ aiLoading ? '分析中...' : '生成分析' }}
            </button>
          </div>

        </section>
      </div>
    </div>
  </template>
      
      <div v-else class="no-video-state">
        <div class="empty-icon">📺</div>
        <h3>暂无视频数据</h3>
        <p>该课程尚未上传任何教学视频</p>
      </div>
    </main>
  </div>

  <!-- AI 结果弹窗 -->
  <div v-if="showAiModal" class="modal-overlay">
    <div class="modal-card">
      <div class="modal-header">
        <h4>{{ currentAiTitle }}</h4>
        <button class="close-btn" @click="showAiModal=false">×</button>
      </div>
      <div class="modal-body">
        <div v-if="aiPages.length > 0" class="ai-content-wrapper">
          
          <!-- Summary Page -->
          <div v-if="aiPages[currentAiPage].type === 'summary'" class="ai-page-content">
            <p v-for="(para, idx) in aiPages[currentAiPage].content" :key="idx" class="ai-long-text">
              {{ para }}
            </p>
          </div>

          <!-- Insights Page -->
          <div v-else-if="aiPages[currentAiPage].type === 'insights'" class="ai-page-content">
            <ul class="ai-list large-list">
              <li v-for="(item, idx) in aiPages[currentAiPage].data" :key="idx">
                <div class="list-header">
                  <span class="metric-name">{{ item.metric }}</span>
                  <span class="strength-tag" :class="getStrengthClass(item.strength)">{{ item.strength }}</span>
                </div>
                <p class="list-desc large-desc">{{ item.explanation }}</p>
              </li>
            </ul>
          </div>

          <!-- Recommendations Page -->
          <div v-else-if="aiPages[currentAiPage].type === 'recommendations'" class="ai-page-content">
            <ul class="ai-list large-list">
              <li v-for="(item, idx) in aiPages[currentAiPage].data" :key="idx" class="rec-item">
                <div class="list-header">
                  <span class="issue-name">{{ item.issue }}</span>
                  <span class="priority-badge" :class="getPriorityClass(item.priority)">{{ item.priority }}</span>
                </div>
                <p class="list-desc large-desc">{{ item.action }}</p>
              </li>
            </ul>
          </div>

        </div>
        <div v-else class="empty-ai">暂无分析结果</div>
      </div>
      <div class="modal-footer">
        <button class="btn-nav" :disabled="currentAiPage === 0" @click="prevAiPage">上一页</button>
        <span class="page-indicator">{{ currentAiPage + 1 }} / {{ aiPages.length }}</span>
        <button class="btn-nav" :disabled="currentAiPage === aiPages.length - 1" @click="nextAiPage">下一页</button>
      </div>
    </div>
  </div>

</template>

<script setup>
import { reactive, ref, onMounted, nextTick, watch, computed } from "vue";
import * as echarts from "echarts";
import { Liquid, Gauge, Rose } from "@antv/g2plot";
import { BASE_URL } from "../../utils/api";
import { getSession } from "../../utils/session";

const session = getSession() || {};
const courseId = session.selectedCourse?.id;

const videoList = ref([]);
const currentVideo = ref(null);
const completionStats = reactive({
  completionRate: 0,
  averageCompletionRate: 0,
  uniqueStudents: 0,
  totalRecords: 0,
  totalWatchDuration: 0,
});
const correlationData = reactive({
  completionRateCorrelation: 0,
  durationCorrelation: 0,
  watchCountCorrelation: 0,
  sampleSize: 0,
  samples: []
});

// AI 相关状态
const aiResult = ref(null);
const aiSuggestions = ref(null);
const aiLoading = ref(false);
const showAiModal = ref(false);
const currentAiPage = ref(0);

const aiPages = computed(() => {
  if (!aiResult.value && !aiSuggestions.value) return [];
  const pages = [];
  // Page 0: 总体评价
  if (aiResult.value?.comment) {
    const paragraphs = aiResult.value.comment.split('\n').filter(p => p.trim());
    pages.push({ type: 'summary', title: '总体评价', content: paragraphs });
  }
  // Page 1: 数据洞察
  if (aiSuggestions.value?.insights) {
    pages.push({ type: 'insights', title: '数据洞察', data: aiSuggestions.value.insights });
  }
  // Page 2: 改进建议
  if (aiSuggestions.value?.recommendations) {
    pages.push({ type: 'recommendations', title: '改进建议', data: aiSuggestions.value.recommendations });
  }
  return pages;
});

const currentAiTitle = computed(() => {
  if (aiPages.value.length > 0 && currentAiPage.value < aiPages.value.length) {
    return aiPages.value[currentAiPage.value].title;
  }
  return 'AI 学习洞察';
});

function nextAiPage() {
  if (currentAiPage.value < aiPages.value.length - 1) {
    currentAiPage.value++;
  }
}

function prevAiPage() {
  if (currentAiPage.value > 0) {
    currentAiPage.value--;
  }
}

const heatmapRef = ref(null);
const liquidRef = ref(null);
const gaugeRef = ref(null);
const roseRef = ref(null);
const correlationRef = ref(null);

let heatmapChart, liquidChart, gaugeChart, roseChart, correlationChart;

onMounted(async () => {
  if (courseId) {
    await fetchVideoList();
    // await fetchCorrelation(); // 在 selectVideo 中按视频触发
    
    // 默认选中第一个视频
    if (videoList.value.length > 0) {
      selectVideo(videoList.value[0]);
    }
  }
});


// 执行 AI 分析
async function analyzeAi() {
  if (!courseId) return;
  
  aiLoading.value = true;
  aiResult.value = null;
  aiSuggestions.value = null;
  showAiModal.value = false;
  currentAiPage.value = 0;

  try {
    const FIXED_MODEL = 'deepseek-ai/DeepSeek-V3.2-Exp';
    let url = `${BASE_URL}/api/analysis/video/behavior-ai?courseId=${courseId}&model=${encodeURIComponent(FIXED_MODEL)}`;
    if (currentVideo.value) {
      url += `&videoId=${currentVideo.value.id}`;
    }
    const res = await fetch(url);
    const data = await res.json();
    
    if (data.code === 200 && data.data) {
      aiResult.value = data.data;
      if (data.data.suggestionsJson) {
        try {
          aiSuggestions.value = JSON.parse(data.data.suggestionsJson);
        } catch (parseErr) {
          console.error("JSON Parse error", parseErr);
        }
      }
      showAiModal.value = true;
    } else {
      alert(data.message || "分析失败");
    }
  } catch (e) {
    console.error("AI Analysis failed", e);
    alert("请求失败，请检查网络或密钥配置");
  } finally {
    aiLoading.value = false;
  }
}

function getStrengthClass(s) {
  if (s === '强相关') return 'tag-strong';
  if (s === '中等相关') return 'tag-medium';
  return 'tag-weak';
}

function getPriorityClass(p) {
  if (p === '高') return 'badge-high';
  if (p === '中') return 'badge-medium';
  return 'badge-low';
}

async function fetchVideoList() {
  try {
    const res = await fetch(`${BASE_URL}/api/analysis/video/list?courseId=${courseId}`);
    const data = await res.json();
    if (data.code === 200) {
      videoList.value = data.data || [];
    }
  } catch (e) {
    console.error("Failed to fetch video list", e);
  }
}

async function selectVideo(video) {
  currentVideo.value = video;
  await nextTick();
  await Promise.all([
    fetchHeatmap(video.id),
    fetchCompletionStats(video.id),
    fetchCorrelation(video.id)
  ]);
  renderHeatmap();
  renderG2Charts();
  renderCorrelationChart();
}

const heatmapData = ref([]);
const distributionData = ref({});

async function fetchHeatmap(videoId) {
  try {
    const res = await fetch(`${BASE_URL}/api/analysis/video/heatmap?videoId=${videoId}`);
    const data = await res.json();
    if (data.code === 200) {
      heatmapData.value = data.data?.heatmapBuckets || [];
    }
  } catch (e) {
    console.error("Failed to fetch heatmap", e);
  }
}

async function fetchCompletionStats(videoId) {
  try {
    const res = await fetch(`${BASE_URL}/api/analysis/video/completion?courseId=${courseId}&videoId=${videoId}`);
    const data = await res.json();
    if (data.code === 200) {
      const d = data.data;
      completionStats.completionRate = d.completionRate;
      completionStats.averageCompletionRate = d.averageCompletionRate;
      completionStats.uniqueStudents = d.uniqueStudents;
      completionStats.totalRecords = d.totalRecords;
      completionStats.totalWatchDuration = d.totalWatchDuration;
      distributionData.value = d.completionDistribution || {};
    }
  } catch (e) {
    console.error("Failed to fetch stats", e);
  }
}

async function fetchCorrelation(videoId) {
  try {
    const res = await fetch(`${BASE_URL}/api/analysis/video/behavior-correlation?courseId=${courseId}${videoId ? `&videoId=${videoId}` : ""}`);
    const data = await res.json();
    if (data.code === 200) {
      const d = data.data;
      correlationData.completionRateCorrelation = d.completionRateCorrelation;
      correlationData.durationCorrelation = d.durationCorrelation;
      correlationData.watchCountCorrelation = d.watchCountCorrelation;
      correlationData.sampleSize = d.sampleSize || 0;
      correlationData.samples = d.samples || [];
    }
  } catch (e) {
    console.error("Failed to fetch correlation", e);
  }
}

function formatBucket(bucket) {
  if (bucket === undefined || bucket === null) return "";
  const str = String(bucket);
  const parts = str.split("-");
  if (parts.length === 2) {
    const start = parseInt(parts[0]);
    const end = parseInt(parts[1]);
    if (!isNaN(start) && !isNaN(end)) {
      return `${formatTimeSimple(start)}-${formatTimeSimple(end)}`;
    }
  }
  // Try to format single number as time
  if (!isNaN(parseFloat(str)) && isFinite(str)) {
     return formatTimeSimple(parseInt(str));
  }
  return str;
}

function formatTimeSimple(s) {
  const m = Math.floor(s / 60);
  const sec = s % 60;
  return `${m}:${sec.toString().padStart(2, '0')}`;
}

function renderHeatmap() {
  if (!heatmapRef.value) return;
  const ready = heatmapRef.value ? heatmapRef.value.clientWidth > 0 && heatmapRef.value.clientHeight > 0 : false;
  if (!ready) {
    waitForSize(heatmapRef.value).then(ok => { if (ok) renderHeatmap(); });
    return;
  }
  heatmapChart && heatmapChart.dispose();
  heatmapChart = echarts.init(heatmapRef.value);

  const xData = heatmapData.value.map(item => formatBucket(item.range));
  const yData = heatmapData.value.map(item => item.count);

  heatmapChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '8%', containLabel: true },
    xAxis: { 
      type: 'category', 
      data: xData, 
      boundaryGap: false,
      name: '播放时间',
      nameLocation: 'middle',
      nameGap: 30,
      nameTextStyle: { color: '#64748b' }
    },
    yAxis: { type: 'value' },
    series: [{
      data: yData,
      type: 'line',
      smooth: true,
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(59, 130, 246, 0.5)' },
          { offset: 1, color: 'rgba(59, 130, 246, 0.05)' }
        ])
      },
      itemStyle: { color: '#3b82f6' }
    }]
  });
}

function renderG2Charts() {
  // Liquid Chart
  if (liquidRef.value) {
    const ready = liquidRef.value.clientWidth > 0 && liquidRef.value.clientHeight > 0;
    if (!ready) { waitForSize(liquidRef.value).then(ok => { if (ok) renderG2Charts(); }); return; }
    liquidChart?.destroy();
    liquidChart = new Liquid(liquidRef.value, {
      percent: completionStats.completionRate / 100,
      outline: { border: 4, distance: 8 },
      wave: { length: 128 },
      height: 160,
      autoFit: true,
    });
    liquidChart.render();
  }

  // Gauge Chart
  if (gaugeRef.value) {
    const readyG = gaugeRef.value.clientWidth > 0 && gaugeRef.value.clientHeight > 0;
    if (!readyG) { waitForSize(gaugeRef.value).then(ok => { if (ok) renderG2Charts(); }); return; }
    gaugeChart?.destroy();
    gaugeChart = new Gauge(gaugeRef.value, {
      percent: completionStats.averageCompletionRate / 100,
      range: {
        ticks: [0, 1/3, 2/3, 1],
        color: ['#F4664A', '#FAAD14', '#30BF78'],
      },
      indicator: {
        pointer: { style: { stroke: '#D0D0D0' } },
        pin: { style: { stroke: '#D0D0D0' } },
      },
      statistic: {
        content: {
          style: { fontSize: '18px', lineHeight: '18px' },
        },
      },
      height: 140,
      autoFit: true,
    });
    gaugeChart.render();
  }

  // Rose Chart
  if (roseRef.value) {
    const readyR = roseRef.value.clientWidth > 0 && roseRef.value.clientHeight > 0;
    if (!readyR) { waitForSize(roseRef.value).then(ok => { if (ok) renderG2Charts(); }); return; }
    roseChart?.destroy();
    const data = [
      { type: '0-50%', value: distributionData.value['0-50'] || 0 },
      { type: '50-80%', value: distributionData.value['50-80'] || 0 },
      { type: '80-100%', value: distributionData.value['80-100'] || 0 }
    ];
    roseChart = new Rose(roseRef.value, {
      data,
      xField: 'type',
      yField: 'value',
      seriesField: 'type',
      radius: 0.9,
      legend: { position: 'bottom' },
      height: 300,
      autoFit: true,
    });
    roseChart.render();
  }
}

function renderCorrelationChart() {
  if (!correlationRef.value) return;
  const ready = correlationRef.value.clientWidth > 0 && correlationRef.value.clientHeight > 0;
  if (!ready) { waitForSize(correlationRef.value).then(ok => { if (ok) renderCorrelationChart(); }); return; }
  correlationChart && correlationChart.dispose();
  correlationChart = echarts.init(correlationRef.value);

  const data = correlationData.samples.map(s => [s.avgCompletionRate, s.avgScore]);

  correlationChart.setOption({
    title: { text: '完课率 vs 成绩', left: 'center' },
    tooltip: { trigger: 'item', formatter: params => `完课率: ${params.data[0]}%<br/>成绩: ${params.data[1]}` },
    xAxis: { name: '完课率(%)', type: 'value', min: 0, max: 100 },
    yAxis: { name: '成绩', type: 'value', min: 0, max: 100 },
    series: [{
      symbolSize: 10,
      data: data,
      type: 'scatter',
      itemStyle: { color: '#8b5cf6' }
    }]
  });
}

function formatDuration(seconds) {
  if (!seconds) return "0s";
  const h = Math.floor(seconds / 3600);
  const m = Math.floor((seconds % 3600) / 60);
  const s = seconds % 60;
  return h > 0 ? `${h}h ${m}m ${s}s` : `${m}m ${s}s`;
}

function getCorrelationClass(val) {
  if (val > 0.5) return 'text-green-600 font-bold';
  if (val < -0.5) return 'text-red-600 font-bold';
  return 'text-gray-600';
}

watch(() => session.selectedCourse, (newVal) => {
  if (newVal) {
    // window.location.reload(); // Removed to prevent redirect loops
    // Instead, just re-fetch data
    if (courseId !== newVal.id) {
       // Ideally we should update courseId and reload, but courseId is const.
       // Since we are using window.location.reload as a brute force, removing it is safer.
       // The layout or router should handle course switches.
    }
  }
});
</script>

<style scoped>
.video-analysis-layout {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 32px;
  height: calc(100vh - 100px);
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
}

/* Sidebar Styling */
.video-sidebar {
  background: #ffffff;
  border-radius: 24px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid rgba(226, 232, 240, 0.8);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.02);
  transition: all 0.3s ease;
}

.sidebar-header {
  padding: 24px;
  border-bottom: 1px solid #f1f5f9;
  background: linear-gradient(to bottom, #ffffff, #f8fafc);
}

.sidebar-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: #1e293b;
  letter-spacing: -0.02em;
}

.sidebar-header .count {
  font-size: 13px;
  color: #64748b;
  margin-top: 4px;
  display: block;
}

.video-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

/* Custom Scrollbar */
.video-list::-webkit-scrollbar {
  width: 6px;
}
.video-list::-webkit-scrollbar-track {
  background: transparent;
}
.video-list::-webkit-scrollbar-thumb {
  background-color: #cbd5e1;
  border-radius: 20px;
  border: 2px solid transparent;
  background-clip: content-box;
}

.video-item {
  display: flex;
  gap: 16px;
  padding: 16px;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid transparent;
  margin-bottom: 8px;
}

.video-item:hover {
  background: #f8fafc;
  transform: translateY(-1px);
}

.video-item.active {
  background: #eff6ff;
  border-color: #bfdbfe;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.1);
}

.video-icon {
  width: 40px;
  height: 40px;
  background: #f1f5f9;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #64748b;
  font-size: 14px;
  transition: all 0.2s;
  flex-shrink: 0;
}

.video-item:hover .video-icon {
  background: #e2e8f0;
  color: #475569;
}

.video-item.active .video-icon {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  color: #fff;
  box-shadow: 0 4px 10px rgba(37, 99, 235, 0.2);
}

.video-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.video-title {
  font-size: 15px;
  font-weight: 600;
  color: #334155;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 4px;
  line-height: 1.4;
}

.video-item.active .video-title {
  color: #1d4ed8;
}

.video-meta {
  font-size: 12px;
  color: #94a3b8;
  font-weight: 500;
}

/* Main Content Styling */
.video-content {
  overflow-y: auto;
  padding-right: 12px;
}

.content-header {
  margin-bottom: 24px;
  background: #fff;
  padding: 20px 24px;
  border-radius: 24px;
  border: 1px solid rgba(226, 232, 240, 0.6);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.01);
}

.content-header h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 800;
  color: #0f172a;
  letter-spacing: -0.03em;
  background: linear-gradient(135deg, #0f172a 0%, #334155 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.content-header .desc {
  color: #64748b;
  margin: 0;
  font-size: 14px;
  line-height: 1.6;
}

/* Dashboard Grid Layout */
.analysis-dashboard {
  display: grid;
  grid-template-columns: 2fr 1fr; /* 2:1 Ratio */
  gap: 24px;
  align-items: start;
}

.video-analysis-col {
  display: flex;
  flex-direction: column;
  gap: 24px;
  min-width: 0; /* Prevent overflow */
}

.course-insights-col {
  display: flex;
  flex-direction: column;
  gap: 24px;
  min-width: 0;
}

/* Stats Cards */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.card {
  background: #ffffff;
  border-radius: 20px;
  padding: 24px;
  border: 1px solid rgba(226, 232, 240, 0.8);
  box-shadow: 0 4px 20px -4px rgba(0, 0, 0, 0.05);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.card:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1);
}

.stat-card {
  position: relative;
  overflow: hidden;
  padding: 20px;
}

.stat-card::after {
  content: '';
  position: absolute;
  top: 0;
  right: 0;
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, transparent, rgba(59, 130, 246, 0.05));
  border-radius: 0 0 0 100%;
}

.stat-card .label {
  font-size: 12px;
  font-weight: 600;
  color: #64748b;
  margin-bottom: 8px;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.stat-card .value {
  font-size: 28px;
  font-weight: 800;
  color: #0f172a;
  letter-spacing: -0.02em;
  line-height: 1;
}

.stat-card .sub {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 6px;
  font-weight: 500;
}

/* Charts Sections */
.heatmap-section h3, .distribution-section h3, .correlation-section h3 {
  margin: 0 0 4px 0;
  font-size: 16px;
  font-weight: 700;
  color: #1e293b;
}

.hint {
  color: #94a3b8;
  font-size: 13px;
  margin-bottom: 16px;
  line-height: 1.4;
}

.chart {
  width: 100%;
  height: 300px;
  border-radius: 12px;
}

/* Correlation Section */
.correlation-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.correlation-chart-small {
  width: 100%;
  height: 200px;
}

.correlation-stats {
  display: flex;
  flex-direction: column;
  gap: 12px;
  background: #f8fafc;
  padding: 16px;
  border-radius: 12px;
}

.stat-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
}

.stat-row span:first-child {
  color: #64748b;
}

/* AI Section Styles */
.ai-section {
  background: linear-gradient(to bottom right, #ffffff, #fcfeff);
  border: 1px solid #e2e8f0;
}

.ai-header {
  margin-bottom: 16px;
}

.header-left h3 {
  margin: 0 0 4px 0;
  font-size: 16px;
  font-weight: 700;
  color: #1e293b;
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-left h3::before {
  content: '🤖';
  font-size: 18px;
}

.ai-controls {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 20px;
}

.model-select {
  padding: 8px 12px;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  font-size: 13px;
  color: #334155;
  width: 100%;
  background-color: white;
}

.btn-ai {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px 16px;
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  width: 100%;
}

.btn-ai:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.2);
}

.btn-ai:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  background: #94a3b8;
}

.spinner {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.ai-result-container {
  animation: slideDown 0.3s ease-out;
}

@keyframes slideDown {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}

.ai-summary-box {
  background: #eff6ff;
  border-left: 3px solid #3b82f6;
  padding: 12px 16px;
  border-radius: 0 8px 8px 0;
  margin-bottom: 20px;
}

.ai-summary-box h4 {
  margin: 0 0 6px 0;
  color: #1e40af;
  font-size: 14px;
}

.ai-summary-box p {
  margin: 0;
  color: #1e3a8a;
  font-size: 13px;
  line-height: 1.6;
}

.ai-details-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.detail-group h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #0f172a;
  border-bottom: 1px solid #f1f5f9;
  padding-bottom: 8px;
}

.ai-list.compact {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.ai-list.compact li {
  padding: 12px;
  background: #fff;
  border: 1px solid #f1f5f9;
  border-radius: 8px;
  transition: border-color 0.2s;
}

.ai-list.compact li:hover {
  border-color: #e2e8f0;
}

.metric-name, .issue-name {
  font-weight: 600;
  color: #334155;
  font-size: 13px;
  margin-right: 8px;
}

.list-desc {
  margin: 6px 0 0 0;
  color: #64748b;
  font-size: 12px;
  line-height: 1.5;
}

/* Tags and Badges */
.strength-tag {
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 4px;
  font-weight: 600;
}
.tag-strong { background: #dcfce7; color: #166534; }
.tag-medium { background: #fef9c3; color: #854d0e; }
.tag-weak { background: #f1f5f9; color: #64748b; }

.priority-badge {
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 4px;
  font-weight: 600;
  text-transform: uppercase;
}
.badge-high { background: #fee2e2; color: #991b1b; }
.badge-medium { background: #ffedd5; color: #9a3412; }
.badge-low { background: #f1f5f9; color: #64748b; }

.g2-chart {
  width: 100%;
  height: 140px;
}

/* No Video State */
.no-video-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 60vh;
  color: #94a3b8;
  text-align: center;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
  opacity: 0.5;
  filter: grayscale(100%);
}

.no-video-state h3 {
  font-size: 20px;
  color: #475569;
  font-weight: 600;
  margin: 0 0 8px 0;
}

.no-video-state p {
  font-size: 14px;
  color: #94a3b8;
  margin: 0;
}

/* Responsive */
@media (max-width: 1280px) {
  .analysis-dashboard {
    grid-template-columns: 1fr;
  }
  .ai-controls {
    flex-direction: row;
  }
  .model-select {
    flex: 1;
  }
  .btn-ai {
    width: auto;
  }
}

@media (max-width: 768px) {
  .video-analysis-layout {
    grid-template-columns: 1fr;
  }
  .video-sidebar {
    height: 300px;
  }
  .stats-cards {
    grid-template-columns: 1fr;
  }
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}
.modal-card {
  background: #fff;
  border-radius: 16px;
  width: 640px;
  max-width: 90vw;
  box-shadow: 0 10px 30px rgba(0,0,0,0.1);
  overflow: hidden;
}
.modal-header {
  padding: 16px 20px;
  border-bottom: 1px solid #f1f5f9;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.modal-body {
  padding: 16px 20px;
}
.close-btn {
  background: transparent;
  border: none;
  font-size: 18px;
  cursor: pointer;
}

.modal-footer {
  padding: 16px 20px;
  border-top: 1px solid #f1f5f9;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #f8fafc;
}

.btn-nav {
  padding: 8px 16px;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  background: #fff;
  cursor: pointer;
  font-size: 14px;
  color: #334155;
  transition: all 0.2s;
}

.btn-nav:hover:not(:disabled) {
  background: #eff6ff;
  color: #3b82f6;
  border-color: #bfdbfe;
}

.btn-nav:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  background: #f1f5f9;
}

.page-indicator {
  font-size: 14px;
  color: #64748b;
  font-weight: 500;
}

.ai-content-wrapper {
  min-height: 300px;
  max-height: 500px;
  overflow-y: auto;
}

.ai-long-text {
  font-size: 18px;
  line-height: 1.8;
  color: #334155;
  margin: 0 0 16px 0;
  text-indent: 2em;
  text-align: justify;
}

.large-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.large-list li {
  padding: 16px;
  border-bottom: 1px solid #f1f5f9;
}

.large-list li:last-child {
  border-bottom: none;
}

.list-header {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.metric-name, .issue-name {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin-right: 12px;
}

.large-desc {
  font-size: 16px;
  color: #475569;
  line-height: 1.6;
  margin: 0;
}

.empty-ai {
  text-align: center;
  color: #94a3b8;
  padding: 40px;
  font-size: 15px;
}
</style>
