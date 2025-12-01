<template>
  <div class="student-analysis-page">
    <header class="page-header">
      <div class="header-content">
        <div class="header-left">
          <h1>
            <el-icon class="header-icon"><DataLine /></el-icon>
            学情分析
          </h1>
          <p class="sub-title">查看知识掌握情况与学习建议</p>
        </div>
        <div class="header-right">
          <el-tag type="primary" effect="dark" size="large" class="course-tag">
            <el-icon><Reading /></el-icon>
            {{ currentCourse?.title || '未选择课程' }}
          </el-tag>
        </div>
      </div>
    </header>

    <main class="page-content" v-loading="loading">
      <div v-if="error" class="error-message">
        <el-alert :title="error" type="error" show-icon />
      </div>

      <div v-else-if="currentCourse" class="analysis-dashboard">
        <!-- 顶部概览卡片 -->
        <div class="summary-section">
          <el-card shadow="hover" class="summary-card">
            <div class="summary-content">
              <div class="summary-item">
                <div class="label">课程名称</div>
                <div class="value">{{ currentCourse.title }}</div>
              </div>
              <div class="summary-item">
                <div class="label">整体平均分</div>
                <div class="value highlight">{{ overallAverage }}%</div>
              </div>
            </div>
            <div class="chips-container" v-if="weakKnowledge.length">
              <div class="chips-label">需加强知识点：</div>
              <div class="chips">
                <el-tag 
                  v-for="wk in weakKnowledge" 
                  :key="wk.kpId" 
                  type="danger" 
                  effect="light"
                  class="chip"
                >
                  {{ wk.kpTitle }} · {{ formatPercent(wk.scoreRatio ?? wk.masteryScore) }}%
                </el-tag>
              </div>
            </div>
          </el-card>
        </div>

        <!-- 知识掌握度分析 -->
        <div class="charts-section">
          <el-row :gutter="20">
            <el-col :span="16">
              <el-card shadow="hover" class="chart-card">
                <template #header>
                  <div class="card-header">
                    <span>知识图谱掌握情况</span>
                  </div>
                </template>
                <div ref="graphRef" class="chart-container"></div>
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card shadow="hover" class="chart-card">
                <template #header>
                  <div class="card-header">
                    <span>掌握度分布</span>
                  </div>
                </template>
                <div ref="pieRef" class="chart-container"></div>
              </el-card>
            </el-col>
          </el-row>
        </div>

        <!-- 详细知识点列表与建议 -->
        <div class="details-section">
          <el-row :gutter="20">
            <el-col :span="16">
              <el-card shadow="hover" class="list-card">
                <template #header>
                  <div class="card-header">
                    <span>知识点详细得分</span>
                  </div>
                </template>
                <div class="knowledge-list" v-if="knowledgeScores.length">
                  <div class="list-header">
                    <div class="col-name">知识点</div>
                    <div class="col-progress">掌握程度</div>
                    <div class="col-score">得分率</div>
                    <div class="col-trend">趋势</div>
                  </div>
                  <div class="list-body">
                    <div class="list-row" v-for="item in knowledgeScores" :key="item.kpId">
                      <div class="col-name">{{ item.kpTitle }}</div>
                      <div class="col-progress">
                        <el-progress 
                          :percentage="Math.min(100, Math.max(0, formatPercent(item.scoreRatio ?? item.masteryScore)))" 
                          :color="getProgressColor"
                          :show-text="false"
                        />
                      </div>
                      <div class="col-score">{{ formatPercent(item.scoreRatio ?? item.masteryScore) }}%</div>
                      <div class="col-trend" :class="item.trend">
                        <span v-if="item.trend === 'up'" class="trend-up">↑ 上升</span>
                        <span v-else-if="item.trend === 'down'" class="trend-down">↓ 下降</span>
                        <span v-else class="trend-flat">→ 持平</span>
                      </div>
                    </div>
                  </div>
                </div>
                <el-empty v-else description="暂无知识点数据" />
              </el-card>
            </el-col>
            
            <el-col :span="8">
              <el-card shadow="hover" class="suggestions-card">
                <template #header>
                  <div class="card-header">
                    <span>AI 学习建议</span>
                    <el-icon color="#E6A23C"><MagicStick /></el-icon>
                  </div>
                </template>
                <div v-if="suggestions.length" class="suggestions-list">
                  <div v-for="(s, idx) in suggestions" :key="idx" class="suggestion-item">
                    <span class="suggestion-index">{{ idx + 1 }}</span>
                    <p class="suggestion-text">{{ s }}</p>
                  </div>
                </div>
                <el-empty v-else description="暂无建议" />
              </el-card>
            </el-col>
          </el-row>
        </div>
        
        <!-- 成绩趋势 -->
        <div class="trend-section">
          <el-card shadow="hover" class="chart-card">
             <template #header>
               <div class="card-header">
                 <span>近期成绩趋势</span>
                 <div class="trend-filters">
                    <el-radio-group v-model="trendPeriod" size="small" @change="handlePeriodChange">
                      <el-radio-button label="7d">近7天</el-radio-button>
                      <el-radio-button label="30d">近30天</el-radio-button>
                    </el-radio-group>
                 </div>
               </div>
             </template>
             <div ref="trendChartRef" class="chart-container"></div>
          </el-card>
        </div>

      </div>
      <el-empty v-else description="请先选择课程" />
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { DataLine, Reading, MagicStick } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { getSelectedCourse, getSession } from '../../utils/session'
import { BASE_URL } from '../../utils/api'

const currentCourse = ref(null)
const session = getSession()
const loading = ref(false)
const error = ref('')

const performanceData = ref({})
const knowledgeScores = ref([])
const weakKnowledge = ref([])
const suggestions = ref([])
const fullScoreTrend = ref([])
const trendPeriod = ref('7d')

const graphRef = ref(null)
const pieRef = ref(null)
const trendChartRef = ref(null)
let graphChart, pieChart, trendChart

const overallAverage = computed(() => {
  const val = performanceData.value.overallAverageScore
  if (val === undefined || val === null) return '-'
  return formatPercent(val)
})

const fetchPerformance = async () => {
  if (!currentCourse.value?.id || !session?.user?.id) return
  
  loading.value = true
  error.value = ''
  try {
    const url = `${BASE_URL}/api/analysis/performance?courseId=${currentCourse.value.id}&studentUserId=${session.user.id}`
    const resp = await fetch(url)
    const data = await resp.json()
    
    if (data.code === 200) {
      performanceData.value = data.data || {}
      knowledgeScores.value = performanceData.value.knowledgeScores || []
      weakKnowledge.value = performanceData.value.weakKnowledge || []
      suggestions.value = performanceData.value.suggestions || []
      fullScoreTrend.value = performanceData.value.scoreTrend || []
      
      await nextTick()
      renderCharts()
    } else {
      error.value = data.message || '获取数据失败'
    }
  } catch (e) {
    console.error('Fetch performance error:', e)
    error.value = '网络请求失败'
  } finally {
    loading.value = false
  }
}

const renderCharts = () => {
  renderGraph()
  renderPie()
  renderTrendChart()
}

const renderGraph = () => {
  if (!graphRef.value) return
  if (graphChart) graphChart.dispose()
  
  graphChart = echarts.init(graphRef.value)
  
  // Construct graph data from knowledgeScores
  const nodes = knowledgeScores.value.map((k, i) => ({
    id: String(k.kpId),
    name: k.kpTitle,
    value: formatPercent(k.scoreRatio ?? k.masteryScore),
    symbolSize: 30 + (formatPercent(k.scoreRatio ?? k.masteryScore) / 100) * 20,
    category: formatPercent(k.scoreRatio ?? k.masteryScore) >= 60 ? 0 : 1
  }))
  
  // Simple links (just for visual, ideally should come from backend structure)
  // Connect sequentially for now or random to root if no structure
  const links = [] 
  if (nodes.length > 1) {
     for (let i = 1; i < nodes.length; i++) {
        links.push({ source: nodes[0].id, target: nodes[i].id })
     }
  }

  const option = {
    tooltip: {},
    legend: [{ data: ['掌握良好', '需加强'] }],
    series: [
      {
        type: 'graph',
        layout: 'force',
        data: nodes,
        links: links,
        categories: [{ name: '掌握良好', itemStyle: { color: '#67C23A' } }, { name: '需加强', itemStyle: { color: '#F56C6C' } }],
        roam: true,
        label: { show: true, position: 'right' },
        force: { repulsion: 100 }
      }
    ]
  }
  graphChart.setOption(option)
}

const renderPie = () => {
  if (!pieRef.value) return
  if (pieChart) pieChart.dispose()
  
  pieChart = echarts.init(pieRef.value)
  
  const good = knowledgeScores.value.filter(k => formatPercent(k.scoreRatio ?? k.masteryScore) >= 80).length
  const fair = knowledgeScores.value.filter(k => {
     const s = formatPercent(k.scoreRatio ?? k.masteryScore)
     return s >= 60 && s < 80
  }).length
  const poor = knowledgeScores.value.filter(k => formatPercent(k.scoreRatio ?? k.masteryScore) < 60).length

  const option = {
    tooltip: { trigger: 'item' },
    legend: { bottom: '5%' },
    series: [
      {
        name: '掌握程度',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: { show: false },
        data: [
          { value: good, name: '优秀 (≥80%)', itemStyle: { color: '#67C23A' } },
          { value: fair, name: '良好 (60-79%)', itemStyle: { color: '#E6A23C' } },
          { value: poor, name: '待加强 (<60%)', itemStyle: { color: '#F56C6C' } }
        ]
      }
    ]
  }
  pieChart.setOption(option)
}

const renderTrendChart = () => {
  if (!trendChartRef.value) return
  if (trendChart) trendChart.dispose()
  
  trendChart = echarts.init(trendChartRef.value)
  
  // Filter data based on trendPeriod
  let data = fullScoreTrend.value || []
  const now = new Date()
  const days = trendPeriod.value === '7d' ? 7 : 30
  const cutoff = new Date(now.getTime() - days * 24 * 60 * 60 * 1000)
  
  data = data.filter(d => new Date(d.date) >= cutoff)
  data.sort((a, b) => new Date(a.date) - new Date(b.date))
  
  const option = {
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: data.map(d => d.date) },
    yAxis: { type: 'value', min: 0, max: 100 },
    series: [{
      data: data.map(d => d.score),
      type: 'line',
      smooth: true,
      areaStyle: {
         color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(64, 158, 255, 0.5)' },
          { offset: 1, color: 'rgba(64, 158, 255, 0.1)' }
        ])
      },
      itemStyle: { color: '#409EFF' }
    }]
  }
  trendChart.setOption(option)
}

const handlePeriodChange = () => {
  renderTrendChart()
}

const formatPercent = (val) => {
  if (val === undefined || val === null || Number.isNaN(val)) return 0
  const num = Number(val)
  return num > 1 ? Number(num.toFixed(1)) : Number((num * 100).toFixed(1))
}

const getProgressColor = (percentage) => {
  if (percentage < 60) return '#F56C6C'
  if (percentage < 80) return '#E6A23C'
  return '#67C23A'
}

onMounted(() => {
  currentCourse.value = getSelectedCourse()
  if (currentCourse.value) {
    fetchPerformance()
  }
  
  window.addEventListener('resize', () => {
    graphChart && graphChart.resize()
    pieChart && pieChart.resize()
    trendChart && trendChart.resize()
  })
})
</script>

<style scoped>
.student-analysis-page {
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

.summary-section {
  margin-bottom: 20px;
}

.summary-content {
  display: flex;
  gap: 40px;
  margin-bottom: 15px;
}

.summary-item .label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 5px;
}

.summary-item .value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.summary-item .value.highlight {
  color: #409EFF;
}

.chips-container {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.chips-label {
  font-size: 14px;
  color: #606266;
}

.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.charts-section {
  margin-bottom: 20px;
}

.chart-card {
  height: 100%;
}

.chart-container {
  height: 300px;
}

.details-section {
  margin-bottom: 20px;
}

.list-header {
  display: flex;
  padding: 10px 0;
  border-bottom: 1px solid #EBEEF5;
  font-weight: bold;
  color: #909399;
}

.list-row {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #EBEEF5;
}

.list-row:last-child {
  border-bottom: none;
}

.col-name { flex: 2; padding-right: 10px; }
.col-progress { flex: 3; padding-right: 20px; }
.col-score { flex: 1; text-align: center; font-weight: bold; }
.col-trend { flex: 1; text-align: center; font-size: 12px; }

.trend-up { color: #67C23A; }
.trend-down { color: #F56C6C; }
.trend-flat { color: #909399; }

.suggestion-item {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
  padding: 10px;
  background: #f9fafc;
  border-radius: 4px;
}

.suggestion-index {
  background: #409EFF;
  color: white;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  flex-shrink: 0;
}

.suggestion-text {
  margin: 0;
  font-size: 14px;
  line-height: 1.5;
  color: #606266;
}

.trend-section {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
