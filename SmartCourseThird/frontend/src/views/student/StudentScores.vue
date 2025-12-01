<template>
  <div>
    <header class="top">
      <div>
        <h1>成绩与反馈</h1>
        <p class="sub">基于后端知识点分析接口的真实数据。</p>
      </div>
      <div class="filters">
        <label>
          <span>课程ID</span>
          <input :value="filters.courseId || ''" type="text" readonly />
        </label>
        <label>
          <span>学生ID</span>
          <input :value="filters.studentId || ''" type="text" readonly />
        </label>
        <button class="primary" @click="reload" :disabled="loading">刷新</button>
        <button v-if="!filters.courseId" class="ghost" @click="goSelectCourse">去选课程</button>
      </div>
    </header>

    <div v-if="error" class="error">{{ error }}</div>

    <section class="panel summary" v-if="filters.courseId && filters.studentId">
      <div class="pill">课程：{{ courseName }}</div>
      <div class="pill" v-if="overallAverage !== ''">整体平均分：{{ overallAverage }}%</div>
      <div class="chips" v-if="weakKnowledge.length">
        <span class="chip" v-for="wk in weakKnowledge" :key="wk.kpId">
          弱项：{{ wk.kpTitle }} · {{ formatPercent(wk.scoreRatio ?? wk.masteryScore) }}%
        </span>
      </div>
    </section>

    <section class="panel" v-if="filters.courseId && filters.studentId">
      <div class="panel-head">
        <h3>知识图谱节点掌握情况</h3>
        <span class="hint">/api/analysis/performance</span>
      </div>
      <div class="charts">
        <div ref="graphRef" class="chart"></div>
        <div ref="pieRef" class="chart"></div>
      </div>
      <div class="list-grid" v-if="mastery.length">
        <div class="row" v-for="item in mastery" :key="item.kpId">
          <div class="name">{{ item.kpTitle }}</div>
          <div class="bar">
            <div class="fill" :style="{ width: formatPercent(item.scoreRatio ?? item.masteryScore) + '%' }"></div>
          </div>
          <div class="score">{{ formatPercent(item.scoreRatio ?? item.masteryScore) }}%</div>
          <div class="trend">{{ item.trend || '—' }}</div>
        </div>
      </div>
      <div v-else class="ghost center">暂无知识点数据</div>
    </section>

    <section class="panel" v-if="filters.courseId && filters.studentId">
      <div class="panel-head">
        <h3>成绩趋势</h3>
        <span class="hint">/api/analysis/performance</span>
      </div>
      <div v-if="scoreTrend.length" ref="lineRef" class="chart tall"></div>
      <div v-else class="ghost center">暂无趋势数据</div>
    </section>

    <section class="panel" v-if="suggestions.length">
      <h3>学习建议</h3>
      <ul class="bullets">
        <li v-for="(s, idx) in suggestions" :key="idx">{{ s }}</li>
      </ul>
    </section>

    <section class="panel">
      <h3>批改反馈</h3>
      <ul class="list">
        <li v-for="fb in feedback.slice(0, 8)" :key="fb.id">
          <span>{{ fb.title }}</span>
          <span class="tag">{{ fb.score ?? '--' }} 分</span>
        </li>
        <li v-if="!feedback.length" class="ghost center">暂无批改反馈</li>
      </ul>
    </section>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted, nextTick } from "vue";
import { useRouter } from "vue-router";
import * as echarts from "echarts";
import { BASE_URL } from "../../utils/api";
import { getSession } from "../../utils/session";

const session = getSession() || {};
const router = useRouter();
const filters = reactive({
  courseId: session.selectedCourse?.id || session.courses?.[0]?.id || 0,
  studentId: session.user?.id || 0,
});
const courseName = computed(() => session.selectedCourse?.title || "未选择");
const mastery = ref([]);
const weakKnowledge = ref([]);
const suggestions = ref([]);
const feedback = ref([]);
const scoreTrend = ref([]);
const loading = ref(false);
const error = ref("");
const performanceData = ref({});
const overallAverage = computed(() => {
  const val = performanceData.value.overallAverageScore;
  if (val === undefined || val === null) return "";
  return formatPercent(val);
});

const pieRef = ref(null);
const graphRef = ref(null);
const lineRef = ref(null);
let pieChart;
let graphChart;
let lineChart;

onMounted(() => {
  reload();
});

function formatPercent(val) {
  if (val === undefined || val === null || Number.isNaN(val)) return 0;
  const num = Number(val);
  return num > 1 ? Number(num.toFixed(1)) : Number((num * 100).toFixed(1));
}

function goSelectCourse() {
  router.push("/courses");
}

async function reload() {
  if (!filters.courseId || !filters.studentId) {
    error.value = "请先选择课程和学生";
    mastery.value = [];
    feedback.value = [];
    weakKnowledge.value = [];
    suggestions.value = [];
    scoreTrend.value = [];
    disposeCharts();
    return;
  }
  loading.value = true;
  error.value = "";
  try {
    await Promise.all([fetchPerformance(), fetchFeedback()]);
    await nextTick();
    renderPie();
    renderGraph();
    renderLine();
  } catch (e) {
    error.value = e.message || "加载失败";
  } finally {
    loading.value = false;
  }
}

async function fetchPerformance() {
  const url = `${BASE_URL}/api/analysis/performance?courseId=${filters.courseId}&studentUserId=${filters.studentId}`;
  const resp = await fetch(url);
  const data = await resp.json();
  if (!resp.ok || data.code !== 200) throw new Error(data.message || data.msg || "获取成绩失败");
  performanceData.value = data.data || {};
  mastery.value = performanceData.value.knowledgeScores || [];
  weakKnowledge.value = performanceData.value.weakKnowledge || [];
  suggestions.value = performanceData.value.suggestions || [];
  scoreTrend.value = performanceData.value.scoreTrend || [];
}

async function fetchFeedback() {
  const url = `${BASE_URL}/api/ai-grading/pending?studentUserId=${filters.studentId}`;
  const resp = await fetch(url);
  const data = await resp.json();
  if (!resp.ok || data.code !== 200) {
    feedback.value = [];
    return;
  }
  feedback.value = (data.data || []).map((item) => ({
    id: item.id,
    title: item.assignmentTitle || `提交 #${item.id}`,
    score: item.score || "--",
  }));
}

function buildGraphData() {
  const nodes = mastery.value.map((item, idx) => {
    const percent = formatPercent(item.scoreRatio ?? item.masteryScore);
    const weak = weakKnowledge.value.some((w) => w.kpId === item.kpId);
    return {
      id: String(item.kpId || idx),
      name: item.kpTitle || `KP-${idx + 1}`,
      value: percent,
      symbolSize: Math.max(20, Math.min(60, percent / 2)),
      itemStyle: {
        color: weak ? "#ef4444" : percent >= 80 ? "#10b981" : percent >= 60 ? "#f59e0b" : "#ef4444",
      },
    };
  });
  const links = nodes.map((node, idx) => ({
    source: node.id,
    target: nodes[(idx + 1) % nodes.length]?.id || node.id,
  }));
  return { nodes, links };
}

function disposeCharts() {
  pieChart && pieChart.dispose();
  graphChart && graphChart.dispose();
  lineChart && lineChart.dispose();
}

function renderPie() {
  if (!pieRef.value || !mastery.value.length) return;
  pieChart && pieChart.dispose();
  pieChart = echarts.init(pieRef.value);
  pieChart.setOption({
    backgroundColor: "#fff",
    tooltip: { trigger: "item", formatter: "{b}<br/>{c}%" },
    legend: { bottom: 0 },
    series: [
      {
        name: "得分率",
        type: "pie",
        radius: ["45%", "70%"],
        roseType: "radius",
        itemStyle: {
          color: (params) => {
            const colors = ["#2563eb", "#7c3aed", "#f59e0b", "#10b981", "#ef4444"];
            return colors[params.dataIndex % colors.length];
          },
          shadowBlur: 10,
          shadowColor: "rgba(0,0,0,0.08)",
        },
        label: { formatter: "{b}\n{c}%" },
        data: mastery.value.map((item) => ({
          name: item.kpTitle || "KP",
          value: formatPercent(item.scoreRatio ?? item.masteryScore),
        })),
        animationEasing: "elasticOut",
        animationDuration: 1200,
      },
    ],
  });
}

function renderGraph() {
  if (!graphRef.value || !mastery.value.length) return;
  const { nodes, links } = buildGraphData();
  graphChart && graphChart.dispose();
  graphChart = echarts.init(graphRef.value);
  graphChart.setOption({
    backgroundColor: "#fff",
    tooltip: {
      formatter: (params) => {
        if (!params.data) return "";
        return `${params.data.name}<br/>掌握度：${params.data.value}%`;
      },
    },
    series: [
      {
        type: "graph",
        layout: "circular",
        data: nodes,
        links,
        roam: true,
        label: { show: true, position: "inside", color: "#fff", fontSize: 12 },
        lineStyle: { color: "#cbd5e1", opacity: 0.6 },
        emphasis: { focus: "adjacency" },
      },
    ],
  });
}

function renderLine() {
  if (!lineRef.value || !scoreTrend.value.length) return;
  lineChart && lineChart.dispose();
  lineChart = echarts.init(lineRef.value);
  lineChart.setOption({
    backgroundColor: "#fff",
    tooltip: { trigger: "axis", formatter: (params) => `${params[0].axisValue}<br/>得分：${params[0].data}%` },
    xAxis: {
      type: "category",
      data: scoreTrend.value.map((i) => new Date(i.date).toLocaleDateString()),
      boundaryGap: false,
      axisLine: { lineStyle: { color: "#cbd5e1" } },
    },
    yAxis: { type: "value", max: 100, axisLine: { lineStyle: { color: "#cbd5e1" } } },
    series: [
      {
        type: "line",
        data: scoreTrend.value.map((i) => formatPercent(i.scoreRatio)),
        smooth: true,
        symbolSize: 8,
        lineStyle: { width: 3, color: "#2563eb" },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: "rgba(37,99,235,0.35)" },
            { offset: 1, color: "rgba(37,99,235,0.05)" },
          ]),
        },
      },
    ],
    animationDuration: 900,
    animationEasing: "cubicOut",
  });
}
</script>

<style scoped>
.top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}
.sub {
  color: #64748b;
}
.filters {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  align-items: center;
}
.filters label {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-weight: 600;
  color: #0f172a;
}
.error {
  margin: 12px 0;
  color: #ef4444;
  background: #fee2e2;
  border: 1px solid #fecdd3;
  padding: 10px 12px;
  border-radius: 10px;
}
.summary {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
}
.pill {
  padding: 8px 12px;
  border-radius: 10px;
  background: #e0f2fe;
  color: #0369a1;
  font-weight: 700;
}
.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.chip {
  padding: 6px 10px;
  background: #fff7ed;
  color: #ea580c;
  border-radius: 10px;
  border: 1px solid #fed7aa;
  font-size: 13px;
}
input {
  width: 110px;
  padding: 8px 10px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: #f8fafc;
  cursor: not-allowed;
}
.primary {
  padding: 10px 12px;
  border-radius: 10px;
  border: none;
  background: linear-gradient(135deg, #2563eb, #7c3aed);
  color: #fff;
  font-weight: 700;
  cursor: pointer;
}
.ghost {
  padding: 10px 12px;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  background: #fff;
  cursor: pointer;
}
.panel {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  padding: 14px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05);
  margin-top: 12px;
}
.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.hint {
  color: #94a3b8;
  font-size: 13px;
}
.list {
  list-style: none;
  padding: 0;
  margin: 8px 0 0;
  display: grid;
  gap: 8px;
}
.list li {
  padding: 10px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  background: #f8fafc;
  display: flex;
  justify-content: space-between;
}
.tag {
  padding: 6px 10px;
  border-radius: 10px;
  background: #e0f2fe;
  color: #0369a1;
  font-size: 12px;
}
.ghost {
  color: #94a3b8;
}
.center {
  justify-content: center;
}
.charts {
  display: grid;
  gap: 12px;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  margin-top: 10px;
}
.chart {
  width: 100%;
  height: 300px;
  border-radius: 12px;
  background: #fff;
}
.chart.tall {
  height: 320px;
}
.list-grid {
  display: grid;
  gap: 8px;
  margin-top: 12px;
}
.row {
  display: grid;
  grid-template-columns: 1.2fr 3fr 0.8fr 0.8fr;
  gap: 10px;
  align-items: center;
  padding: 10px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  background: #f8fafc;
}
.name {
  font-weight: 700;
}
.bar {
  width: 100%;
  height: 10px;
  border-radius: 10px;
  background: #e2e8f0;
  overflow: hidden;
}
.fill {
  height: 100%;
  background: linear-gradient(135deg, #2563eb, #7c3aed);
}
.score {
  font-weight: 700;
  color: #0f172a;
}
.trend {
  color: #475569;
  font-size: 13px;
}
.bullets {
  margin: 10px 0 0;
  padding-left: 18px;
  color: #0f172a;
}
</style>
