<template>
  <div class="analysis-container">
    <div class="page-mask" v-if="activeTab === 'detailed' && !filters.studentId"></div>
    <header class="top">
      <div>
        <h1>学情分析</h1>
        <p class="sub">按课程 + 学生查看知识图谱掌握度、趋势与风险。</p>
      </div>
      <div class="filters">
        <div class="student-select-wrapper">
          <label class="student-select">
            <span>学生</span>
            <div 
              class="custom-select" 
              :class="{ open: isDropdownOpen, disabled: studentLoading || !studentOptions.length }" 
              @click="toggleDropdown"
            >
              <div class="selected-text">
                {{ studentLoading ? "加载中..." : (selectedStudentName || "请选择学生") }}
              </div>
              <span class="arrow" :class="{ rotated: isDropdownOpen }">▼</span>
              
              <div class="options-list" v-show="isDropdownOpen">
                <div 
                  class="option-item" 
                  v-for="stu in sortedStudentOptions" 
                  :key="stu.id" 
                  @click.stop="selectStudent(stu)"
                  :class="{
                    'text-red': riskMap[stu.id] === 'HIGH',
                    'text-orange': riskMap[stu.id] === 'MEDIUM',
                    'text-green': riskMap[stu.id] === 'LOW'
                  }"
                >
                  {{ stu.name }}
                </div>
                <div class="option-item empty" v-if="!studentOptions.length">
                  {{ studentLoading ? "加载中..." : "暂无学生" }}
                </div>
              </div>
            </div>
          </label>
        </div>

      </div>
    </header>

    <div class="tabs">
      <div 
        class="tab-item" 
        :class="{ active: activeTab === 'overall' }"
        @click="activeTab = 'overall'"
      >
        整体情况
      </div>
      <div 
        class="tab-item" 
        :class="{ active: activeTab === 'detailed' }"
        @click="activeTab = 'detailed'"
      >
        详细情况
      </div>
    </div>

    <div v-if="error" class="error">{{ error }}</div>
    <div v-else-if="studentError" class="error">{{ studentError }}</div>



    <!-- 详细情况 Tab 内容 -->
    <div v-show="activeTab === 'detailed'">
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
      <div class="list-grid" v-if="knowledgeScores.length">
        <div class="row" v-for="item in knowledgeScores" :key="item.kpId">
          <div class="name">{{ item.kpTitle }}</div>
          <div class="bar">
            <div class="fill" :style="{ width: formatPercent(item.scoreRatio ?? item.masteryScore) + '%' }"></div>
          </div>
          <div class="score">{{ formatPercent(item.scoreRatio ?? item.masteryScore) }}%</div>
          <div class="trend" :class="item.trend">
            <span v-if="item.trend === 'up'">↑</span>
            <span v-else-if="item.trend === 'down'">↓</span>
            <span v-else-if="item.trend === 'flat'">→</span>
            <span v-else>--</span>
          </div>
        </div>
      </div>
      <div v-else class="ghost center">暂无知识点数据</div>
    </section>

    <section class="panel" v-if="suggestions.length">
      <h3>教学建议</h3>
      <ul class="bullets">
        <li v-for="(s, idx) in suggestions" :key="idx">{{ s }}</li>
      </ul>
    </section>

    <section class="panel" v-if="filters.courseId">
      <div class="panel-head">
        <h3>成绩趋势分析</h3>
        <div class="time-filters" v-if="filters.studentId">
          <button :class="{ active: trendPeriod === '7d' }" @click="setTrendPeriod('7d')">最近7天</button>
          <button :class="{ active: trendPeriod === '30d' }" @click="setTrendPeriod('30d')">最近30天</button>
        </div>
      </div>
      <div v-if="filters.studentId" ref="trendChartRef" class="chart tall"></div>
      <div v-else class="ghost center" style="height: 300px;">
        请选择一位学生以查看详细成绩趋势
      </div>
    </section>



    </div>

    <!-- 整体情况 Tab 内容: 学生列表网格 -->
    <div v-show="activeTab === 'overall'">
      <section class="panel" v-if="filters.courseId">
        <div class="panel-head">
          <h3>班级学生概览</h3>
          <span class="hint">共 {{ studentOptions.length }} 人</span>
        </div>
        
        <div class="student-grid" v-if="studentOptions.length">
          <div 
            class="student-card" 
            :class="{ active: filters.studentId === stu.id }"
            v-for="stu in sortedStudentOptions" 
            :key="stu.id"
            @click="selectStudent(stu); activeTab = 'detailed'"
          >
            <div class="avatar-placeholder">{{ stu.name.charAt(0) }}</div>
            <div class="info">
              <div class="name">{{ stu.name }}</div>
              <div class="risk-badge" :class="riskMap[stu.id] || 'LOW'">
                {{ riskMap[stu.id] === 'HIGH' ? '高风险' : riskMap[stu.id] === 'MEDIUM' ? '中风险' : '低风险' }}
              </div>
            </div>
          </div>
        </div>
        <div v-else class="ghost center">暂无学生数据</div>
      </section>

    <!-- 风险报告部分 (Moved back to Overall) -->
    <section class="panel" v-if="filters.courseId">
      <div class="panel-head">
        <h3>风险分析报告</h3>
        <span class="hint">{{ (riskKpList.length || highRiskStudents.length) ? '数据信息' : '当前没有数据' }}</span>
      </div>
      
      <!-- 风险类型分布饼图 -->
      <div class="charts">
        <div ref="riskTypeChartRef" class="chart"></div>
      </div>
      
      <!-- 高风险知识点表格 -->
      <h4>高风险知识点</h4>
      <div class="table">
        <div class="table-head">
          <div>知识点名称</div>
          <div>平均掌握度</div>
          <div>低掌握度学生数</div>
        </div>
        <div class="table-row" v-for="item in riskKpList" :key="item.kpId">
          <div>{{ item.kpTitle }}</div>
          <div>{{ formatPercent(item.averageMastery) }}%</div>
          <div>{{ item.lowMasteryCount }}</div>
        </div>
        <div v-if="!riskKpList.length" class="table-row empty">暂无数据</div>
      </div>
      
      <!-- 高风险学生表格 -->
      <h4>高风险学生</h4>
      <div class="table">
        <div class="table-head">
          <div>学生ID</div>
          <div>知识点</div>
          <div>掌握度</div>
          <div>风险等级</div>
        </div>
        <div class="table-row" v-for="item in highRiskStudents" :key="item.studentId + item.kpId">
          <div>{{ item.studentId }}</div>
          <div>{{ item.kpTitle }}</div>
          <div>{{ formatPercent(item.masteryScore) }}%</div>
          <div>
            <span v-if="item.riskLevel === 'HIGH'" class="risk-high">高风险</span>
            <span v-else-if="item.riskLevel === 'MEDIUM'" class="risk-medium">中风险</span>
            <span v-else class="risk-low">低风险</span>
          </div>
        </div>
        <div v-if="!highRiskStudents.length" class="table-row empty">暂无数据</div>
      </div>
    </section>

    <!-- 成绩趋势分析 (Moved back to Overall) -->

    </div>


  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted, nextTick, watch } from "vue";
import { useRouter } from "vue-router";
import * as echarts from "echarts";
import { BASE_URL } from "../../utils/api";
import { getSession } from "../../utils/session";

const session = getSession() || {};
const router = useRouter();
const filters = reactive({
  courseId: session.selectedCourse?.id || session.courses?.[0]?.id || 0,
  studentId: session.user?.role === "STUDENT" ? session.user?.id || 0 : 0,
});
const courseName = computed(() => session.selectedCourse?.title || "未选择");

const knowledgeScores = ref([]);
const weakKnowledge = ref([]);
const suggestions = ref([]);
const scoreTrend = ref([]);
const riskList = ref([]);
const performanceData = ref({});
const loading = ref(false);
const error = ref("");
const studentLoading = ref(false);
const studentError = ref("");
const studentOptions = ref([]);
const overallAverage = computed(() => {
  const val = performanceData.value.overallAverageScore;
  if (val === undefined || val === null) return "";
  return formatPercent(val);
});

// 风险报告相关数据
const riskTypeCount = ref({});
const riskKpList = ref([]);
const highRiskStudents = ref([]);

// 成绩趋势相关
const trendPeriod = ref('7d');
const fullScoreTrend = ref([]);

const activeTab = ref('overall');
const riskMap = computed(() => {
  const map = {};
  highRiskStudents.value.forEach(item => {
    // If a student has multiple risk records, take the highest risk
    // Priority: HIGH > MEDIUM > LOW
    const currentRisk = map[item.studentId];
    if (!currentRisk || item.riskLevel === 'HIGH') {
      map[item.studentId] = item.riskLevel;
    } else if (item.riskLevel === 'MEDIUM' && currentRisk !== 'HIGH') {
      map[item.studentId] = item.riskLevel;
    }
  });
  return map;
});

const isDropdownOpen = ref(false);
const selectedStudentName = computed(() => {
  const s = studentOptions.value.find(s => s.id === filters.studentId);
  return s ? s.name : '';
});

const sortedStudentOptions = computed(() => {
  if (!studentOptions.value.length) return [];
  
  const students = [...studentOptions.value];
  const riskPriority = { 'HIGH': 3, 'MEDIUM': 2, 'LOW': 1 };
  
  return students.sort((a, b) => {
    const riskA = riskMap.value[a.id];
    const riskB = riskMap.value[b.id];
    const pA = riskPriority[riskA] || 0;
    const pB = riskPriority[riskB] || 0;
    
    if (pA !== pB) return pB - pA;
    return a.id - b.id;
  });
});

function toggleDropdown() {
  if (studentLoading.value || !studentOptions.value.length) return;
  isDropdownOpen.value = !isDropdownOpen.value;
}

function selectStudent(stu) {
  filters.studentId = stu.id;
  isDropdownOpen.value = false;
  // reload() is triggered by watch(filters.studentId)
}

// Auto-open dropdown when options are loaded and no student is selected
watch(studentOptions, (newVal) => {
  if (newVal.length > 0 && !filters.studentId && activeTab.value === 'detailed') {
    isDropdownOpen.value = true;
  }
});

// Watch tab change to trigger auto-open if switching to detailed
watch(activeTab, async (newVal) => {
  if (newVal === 'detailed') {
    if (!filters.studentId && studentOptions.value.length > 0) {
      isDropdownOpen.value = true;
    }
    await nextTick();
    graphChart && graphChart.resize();
    pieChart && pieChart.resize();
    trendChart && trendChart.resize();
  } else if (newVal === 'overall') {
    await nextTick();
    riskTypeChart && riskTypeChart.resize();
  }
});

const graphRef = ref(null);
const pieRef = ref(null);
const riskTypeChartRef = ref(null);
const trendChartRef = ref(null);
let graphChart, pieChart, riskTypeChart, trendChart;


onMounted(() => {
  reload();
  
  // 监听课程ID变化，获取学生列表
  watch(
    () => filters.courseId,
    (newVal) => {
      if (newVal) {
        fetchStudents();
      }
    },
    { immediate: true }
  );

  // 监听学生ID变化，刷新数据
  watch(
    () => filters.studentId,
    (newVal) => {
      if (newVal) {
        reload();
      }
    }
  );


});

function formatPercent(val) {
  if (val === undefined || val === null || Number.isNaN(val)) return 0;
  const num = Number(val);
  return num > 1 ? Number(num.toFixed(1)) : Number((num * 100).toFixed(1));
}

function goSelectCourse() {
  router.push("/courses");
}

function setTrendPeriod(period) {
  trendPeriod.value = period;
  renderTrendChart();
}

async function reload(silent = false) {
  if (!filters.courseId) {
    error.value = "请先选择课程";
    clearData();
    return;
  }
  // Allow loading without studentId for Overall tab (Risk Report)
  // if (!filters.studentId && session.user?.role !== "STUDENT") { ... } 

  if (!silent) loading.value = true;
  error.value = "";
  try {
    const promises = [fetchRisk(), fetchRiskReport()];
    if (filters.studentId) {
      promises.push(fetchPerformance());
    } else {
      // Clear performance data if no student selected
      knowledgeScores.value = [];
      weakKnowledge.value = [];
      suggestions.value = [];
      scoreTrend.value = [];
      performanceData.value = {};
    }
    await Promise.all(promises);
    await nextTick();
    renderCharts();
  } catch (e) {
    error.value = e.message || "加载失败";
  } finally {
    if (!silent) loading.value = false;
  }
}

async function fetchStudents() {
  if (!filters.courseId) return;
  studentLoading.value = true;
  studentError.value = "";
  try {
    const url = `${BASE_URL}/api/ai-grading/users?pageSize=1000`; // Fetch enough users to filter
    const resp = await fetch(url);
    const data = await resp.json();
    
    if (data.code === 200 || data.code === 0) { // Handle potential different success codes
       const allUsers = data.data?.records || data.data || [];
       // Filter for students
       const students = allUsers.filter(u => u.role === 'STUDENT');
       
       studentOptions.value = students.map(u => ({
         id: u.id,
         name: u.realName || u.username || `学生${u.id}`
       }));
    } else {
       throw new Error(data.message || "获取学生列表失败");
    }
  } catch (e) {
    console.error("Fetch students error:", e);
    studentError.value = "获取学生列表失败，请检查网络";
    // Fallback to empty or keep previous options if needed, but for now clear to avoid confusion
    studentOptions.value = [];
  } finally {
    studentLoading.value = false;
  }
}

async function fetchJson(url, fallback) {
  try {
    const resp = await fetch(url);
    if (!resp.ok) throw new Error("网络错误");
    return await resp.json();
  } catch (e) {
    console.warn("使用示例数据", url, e);
    return fallback;
  }
}

async function fetchPerformance() {
  if (!filters.courseId || !filters.studentId) return;
  
  const url = `${BASE_URL}/api/analysis/performance?courseId=${filters.courseId}&studentUserId=${filters.studentId}`;
  const resp = await fetch(url);
  const data = await resp.json();
  if (!resp.ok || data.code !== 200) {
    throw new Error(data.message || data.msg || "获取成绩失败");
  }
  performanceData.value = data.data || {};
  knowledgeScores.value = performanceData.value.knowledgeScores || [];
  weakKnowledge.value = performanceData.value.weakKnowledge || [];
  suggestions.value = performanceData.value.suggestions || [];
  fullScoreTrend.value = performanceData.value.scoreTrend || [];
  // 根据时间范围过滤趋势数据
  filterTrendData();
}

async function fetchRisk() {
  if (!filters.courseId) return;
  
  const url = `${BASE_URL}/api/analysis/risk/report?courseId=${filters.courseId}`;
  const resp = await fetch(url);
  const data = await resp.json();
  if (!resp.ok || data.code !== 200) {
    riskList.value = [];
    return;
  }
  riskList.value =
    data.data?.kpRiskList?.map((item) => {
      const mastery = item.averageMastery ?? 0;
      const percent = mastery > 1 ? mastery : mastery * 100;
      return { kpTitle: item.kpTitle, riskValue: Math.max(10, 100 - percent) };
    }) || [];
}

async function fetchRiskReport() {
  if (!filters.courseId) return;
  
  const url = `${BASE_URL}/api/analysis/risk/report?courseId=${filters.courseId}`;
  const data = await fetchJson(url, { 
    data: { 
      riskTypeCount: {},
      kpRiskList: [],
      highRiskStudents: []
    } 
  });
  
  const reportData = data.data || {};
  riskTypeCount.value = reportData.riskTypeCount || {};
  riskKpList.value = reportData.kpRiskList || [];
  highRiskStudents.value = reportData.highRiskStudents || [];
}

function filterTrendData() {
  if (!fullScoreTrend.value.length) {
    scoreTrend.value = [];
    return;
  }
  
  const now = new Date();
  let days = 7;
  if (trendPeriod.value === '30d') {
    days = 30;
  }
  
  const startDate = new Date(now);
  startDate.setDate(startDate.getDate() - days);
  
  scoreTrend.value = fullScoreTrend.value.filter(item => {
    const itemDate = new Date(item.date);
    return itemDate >= startDate && itemDate <= now;
  });
}

function clearData() {
  knowledgeScores.value = [];
  weakKnowledge.value = [];
  suggestions.value = [];
  scoreTrend.value = [];
  riskList.value = [];
  riskTypeCount.value = {};
  riskKpList.value = [];
  highRiskStudents.value = [];
  disposeCharts();
}

function disposeCharts() {
  [graphChart, pieChart, riskTypeChart, trendChart].forEach((chart) => chart && chart.dispose());
}

function renderCharts() {
  renderGraph();
  renderPie();
  renderRiskTypeChart();
  renderTrendChart();
}

function renderGraph() {
  if (!graphRef.value) return;
  graphChart && graphChart.dispose();
  graphChart = echarts.init(graphRef.value);
  
  const nodes = knowledgeScores.value.map((item, idx) => {
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
  
  graphChart.setOption({
    backgroundColor: "#fff",
    tooltip: { 
      trigger: "item", 
      formatter: (params) => params.data.name + "<br/>得分率: " + params.data.value + "%" 
    },
    series: [{
      type: "graph",
      layout: "force",
      animation: true,
      force: { repulsion: 100, gravity: 0.1, edgeLength: 100, layoutAnimation: true },
      draggable: true,
      roam: true,
      label: { show: true, position: "bottom" },
      emphasis: { focus: "adjacency", blurScope: "coordinateSystem" },
      data: nodes,
      links: links,
    }],
  });
}

function renderPie() {
  if (!pieRef.value) return;
  pieChart && pieChart.dispose();
  pieChart = echarts.init(pieRef.value);

  const hasData = knowledgeScores.value && knowledgeScores.value.length > 0;

  const option = {
    backgroundColor: "#fff",
    tooltip: hasData ? { trigger: "item", formatter: "{b}<br/>{c}%" } : { show: false },
    legend: hasData ? { bottom: 0 } : { show: false },
    title: hasData ? undefined : {
      text: '暂无数据',
      left: 'center',
      top: 'center',
      textStyle: { color: '#909399', fontSize: 14, fontWeight: 'normal' }
    },
    series: [
      {
        name: "得分率",
        type: "pie",
        radius: ["45%", "70%"],
        roseType: hasData ? "radius" : false,
        label: { 
          show: hasData,
          formatter: "{b}\n{c}%" 
        },
        data: hasData ? knowledgeScores.value.map((item) => ({
          name: item.kpTitle || "KP",
          value: formatPercent(item.scoreRatio ?? item.masteryScore),
        })) : [{ value: 1, name: '暂无数据', itemStyle: { color: '#f0f2f5' } }],
        animationEasing: "elasticOut",
        animationDuration: 1200,
        silent: !hasData
      },
    ],
  };

  pieChart.setOption(option);
}

function renderRiskTypeChart() {
  if (!riskTypeChartRef.value) return;
  riskTypeChart && riskTypeChart.dispose();
  riskTypeChart = echarts.init(riskTypeChartRef.value);
  
  const riskTypes = Object.keys(riskTypeCount.value);
  const counts = Object.values(riskTypeCount.value);
  
  riskTypeChart.setOption({
    backgroundColor: "#fff",
    tooltip: { trigger: "item" },
    legend: { bottom: 0 },
    series: [
      {
        name: "风险类型分布",
        type: "pie",
        radius: ["40%", "70%"],
        avoidLabelOverlap: false,
        label: { show: false, position: "center" },
        emphasis: {
          label: { show: true, fontSize: "16", fontWeight: "bold" }
        },
        labelLine: { show: false },
        data: riskTypes.map((type, index) => ({
          name: type,
          value: counts[index]
        }))
      }
    ]
  });
}

function renderTrendChart() {
  if (!trendChartRef.value) return;
  trendChart && trendChart.dispose();
  trendChart = echarts.init(trendChartRef.value);
  
  filterTrendData();
  
  const dates = scoreTrend.value.map(item => {
    const date = new Date(item.date);
    return `${date.getMonth()+1}-${date.getDate()}`;
  });
  
  const scores = scoreTrend.value.map(item => formatPercent(item.scoreRatio));
  
  // 计算平均分
  const avgScore = scores.reduce((sum, score) => sum + score, 0) / scores.length;
  const avgScores = Array(dates.length).fill(avgScore);
  
  trendChart.setOption({
    backgroundColor: "#fff",
    tooltip: { trigger: "axis" },
    legend: { data: ['得分率', '平均线'] },
    xAxis: { 
      type: 'category', 
      data: dates,
      axisLine: { lineStyle: { color: "#cbd5e1" } } 
    },
    yAxis: { 
      type: 'value',
      axisLine: { lineStyle: { color: "#cbd5e1" } } 
    },
    series: [
      {
        name: '得分率',
        type: 'line',
        data: scores,
        smooth: true,
        showSymbol: false,
        lineStyle: { width: 3, color: '#8b5cf6' }
      },
      {
        name: '平均线',
        type: 'line',
        data: avgScores,
        smooth: true,
        showSymbol: false,
        lineStyle: { 
          width: 2, 
          color: '#f59e0b',
          type: 'dashed'
        }
      }
    ]
  });
}
</script>

<style scoped>
.analysis-container {
  position: relative;
  min-height: 600px;
}

.tabs {
  display: flex;
  gap: 24px;
  margin-bottom: 20px;
  border-bottom: 1px solid #e2e8f0;
}

.tab-item {
  padding: 12px 4px;
  font-weight: 600;
  color: #64748b;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: all 0.2s;
}

.tab-item:hover {
  color: #3b82f6;
}

.tab-item.active {
  color: #3b82f6;
  border-bottom-color: #3b82f6;
}

.page-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  z-index: 900;
  backdrop-filter: blur(2px);
}

.top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  position: relative;
  /* z-index: auto; so it gets covered by mask, except the elevated wrapper */
}

.student-select-wrapper {
  position: relative;
  z-index: 1001; /* Above mask */
}

.custom-select {
  position: relative;
  width: 160px;
  height: 38px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 10px;
  user-select: none;
  transition: all 0.2s;
}

.custom-select:hover {
  border-color: #3b82f6;
}

.custom-select.open {
  border-color: #3b82f6;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.2);
}

.custom-select.disabled {
  background: #f1f5f9;
  cursor: not-allowed;
  opacity: 0.7;
}

.selected-text {
  font-size: 14px;
  color: #334155;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.arrow {
  font-size: 12px;
  color: #94a3b8;
  transition: transform 0.2s;
}

.arrow.rotated {
  transform: rotate(180deg);
}

.options-list {
  position: absolute;
  top: calc(100% + 4px);
  left: 0;
  width: 100%;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
  max-height: 240px;
  overflow-y: auto;
  z-index: 1002;
  padding: 4px;
}

.option-item {
  padding: 8px 12px;
  font-size: 14px;
  color: #334155;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.1s;
}

.option-item:hover {
  background: #eff6ff;
  color: #3b82f6;
}

.option-item.empty {
  text-align: center;
  color: #94a3b8;
  cursor: default;
}

.text-red {
  color: #ef4444;
}

.text-orange {
  color: #f59e0b;
}

.text-green {
  color: #10b981;
}

.student-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 16px;
  margin-top: 16px;
}

.student-card {
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: all 0.2s;
  background: #fff;
}

.student-card:hover {
  border-color: #3b82f6;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.1);
  transform: translateY(-2px);
}

.student-card.active {
  border-color: #3b82f6;
  background-color: #eff6ff;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.2);
}

.avatar-placeholder {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #eff6ff;
  color: #3b82f6;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 18px;
}

.info {
  flex: 1;
}

.name {
  font-weight: 600;
  color: #334155;
  margin-bottom: 4px;
}

.risk-badge {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 500;
}

.risk-badge.HIGH {
  background: #fee2e2;
  color: #ef4444;
}

.risk-badge.MEDIUM {
  background: #fef3c7;
  color: #d97706;
}

.risk-badge.LOW {
  background: #d1fae5;
  color: #059669;
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

.student-select {
  display: flex;
  align-items: center;
  gap: 6px;
}

.student-select select {
  padding: 8px 10px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
}

input {
  width: 110px;
  padding: 8px 10px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
}

button {
  padding: 8px 12px;
  border-radius: 10px;
  border: none;
  cursor: pointer;
}

button.primary {
  background: linear-gradient(135deg, #2563eb, #7c3aed);
  color: #fff;
  font-weight: 700;
}

button.ghost {
  background: transparent;
  border: 1px solid #cbd5e1;
  color: #64748b;
}

button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.error {
  padding: 12px;
  background: #fee2e2;
  color: #ef4444;
  border-radius: 10px;
  margin: 12px 0;
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
  margin-bottom: 12px;
}

.time-filters {
  display: flex;
  gap: 8px;
}

.time-filters button {
  padding: 6px 12px;
  background: #f1f5f9;
  color: #64748b;
  border-radius: 8px;
  font-size: 14px;
}

.time-filters button.active {
  background: #3b82f6;
  color: white;
}

.hint {
  color: #94a3b8;
  font-size: 13px;
}

.charts {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}

.chart {
  height: 300px;
  min-height: 300px;
}

.chart.tall {
  height: 400px;
  min-height: 400px;
}

.summary {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
}

.pill {
  padding: 6px 12px;
  background: #dbeafe;
  color: #1d4ed8;
  border-radius: 20px;
  font-size: 14px;
}

.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.chip {
  padding: 4px 10px;
  background: #fecaca;
  color: #dc2626;
  border-radius: 20px;
  font-size: 13px;
}

.list-grid {
  display: grid;
  gap: 12px;
  margin-top: 16px;
}

.row {
  display: grid;
  grid-template-columns: 2fr 3fr 1fr 1fr;
  gap: 12px;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f1f5f9;
}

.row:last-child {
  border-bottom: none;
}

.bar {
  height: 8px;
  background: #e2e8f0;
  border-radius: 4px;
  overflow: hidden;
}

.fill {
  height: 100%;
  background: linear-gradient(90deg, #60a5fa, #3b82f6);
  border-radius: 4px;
}

.score {
  font-weight: 700;
  text-align: right;
}

.trend {
  text-align: center;
  font-weight: bold;
}

.trend.up {
  color: #10b981;
}

.trend.down {
  color: #ef4444;
}

.trend.flat {
  color: #f59e0b;
}

.bullets {
  padding-left: 20px;
  margin: 8px 0;
}

.bullets li {
  margin: 8px 0;
}

.table {
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  overflow: hidden;
  margin: 12px 0;
}

.table-head,
.table-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  padding: 10px 12px;
  font-size: 13px;
  align-items: center;
}

.table-head {
  background: #e2e8f0;
  color: #0f172a;
  font-weight: 700;
}

.table-row:nth-child(even) {
  background: #f8fafc;
}

.table-row:nth-child(odd) {
  background: #eef2f7;
}

.table-row.empty {
  grid-template-columns: 1fr;
  text-align: center;
}

.risk-high {
  color: #ef4444;
  font-weight: bold;
}

.risk-medium {
  color: #f59e0b;
  font-weight: bold;
}

.risk-low {
  color: #10b981;
  font-weight: bold;
}

.ghost {
  color: #94a3b8;
}

.center {
  text-align: center;
}

@media (max-width: 900px) {
  .top {
    flex-direction: column;
    align-items: stretch;
  }
  
  .filters {
    justify-content: flex-end;
  }
  
  .row {
    grid-template-columns: 2fr 1fr 1fr;
  }
  
  .row .trend {
    display: none;
  }
  
  .table-head,
  .table-row {
    grid-template-columns: repeat(3, 1fr);
  }
}
</style>