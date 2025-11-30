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
    <div v-show="activeTab === 'detailed' && filters.courseId">
      <section class="panel summary" v-if="filters.courseId && filters.studentId">
        <div class="pill">课程：{{ courseName }}</div>
        <div class="pill" v-if="overallAverage !== ''">整体平均分：{{ overallAverage }}%</div>
        <div class="chips" v-if="weakKnowledge.length">
          <span class="chip" v-for="wk in weakKnowledge" :key="wk.kpId">
            弱项：{{ wk.kpTitle }} · {{ formatPercent(wk.scoreRatio || wk.masteryScore) }}%
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
              <div class="fill" :style="{ width: formatPercent(item.scoreRatio || item.masteryScore) + '%' }"></div>
            </div>
            <div class="score">{{ formatPercent(item.scoreRatio || item.masteryScore) }}%</div>
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
        <div v-else class="empty-placeholder" style="height: 300px;">
          <i class="el-icon-data-line" style="font-size: 48px; color: #dcdfe6;"></i>
          <p>请选择一位学生以查看详细成绩趋势</p>
        </div>
      </section>
    </div>

    <!-- 整体情况 Tab 内容 -->
    <div v-show="activeTab === 'overall' && filters.courseId" class="overall-tab-content">
      <!-- 班级学生概览 - 不受 loading 影响 -->
      <section class="panel" v-if="filters.courseId">
        <div class="panel-head">
          <h3>班级学生概览</h3>
          <span class="hint">共 {{ studentOptions.length }} 人</span>
        </div>
        
        <div v-if="studentLoading" class="loading-indicator">
          <i class="el-icon-loading" style="font-size: 32px; color: #409eff;"></i>
          <p>正在加载学生列表...</p>
        </div>
        <div v-else-if="studentOptions.length" class="student-grid">
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
        <div v-else class="empty-placeholder">
          <i class="el-icon-user" style="font-size: 48px; color: #dcdfe6;"></i>
          <p>{{ studentError || '暂无学生数据' }}</p>
        </div>
      </section>

      <!-- 风险报告部分 - 独立的加载状态 -->
      <section class="panel" v-if="filters.courseId">
        <div class="panel-head">
          <h3>风险分析报告</h3>
          <span class="hint">{{ loading ? '正在加载...' : ((riskKpList.length || highRiskStudents.length) ? '数据信息' : '当前没有数据') }}</span>
        </div>
        
        <div v-if="loading" class="loading-indicator">
          <i class="el-icon-loading" style="font-size: 32px; color: #409eff;"></i>
          <p>正在加载风险分析数据...</p>
        </div>
        
        <div v-else>
          <!-- 风险类型分布饼图 -->
          <div class="charts">
            <div ref="riskTypeChartRef" class="chart" v-show="Object.keys(riskTypeCount).length > 0"></div>
            <div v-show="Object.keys(riskTypeCount).length === 0" class="chart empty-placeholder">
              <i class="el-icon-pie-chart" style="font-size: 48px; color: #dcdfe6;"></i>
              <p>暂无风险类型数据</p>
            </div>
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
            <div v-if="!riskKpList.length" class="table-row empty">
              <i class="el-icon-warning" style="margin-right: 8px; color: #909399;"></i>
              暂无高风险知识点数据
            </div>
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
            <div v-if="!highRiskStudents.length" class="table-row empty">
              <i class="el-icon-success" style="margin-right: 8px; color: #67c23a;"></i>
              暂无高风险学生（这是好消息！）
            </div>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import request from '@/utils/request'

const BASE_URL = process.env.NODE_ENV === 'production' ? 'http://localhost:8083' : '/smart-api'

export default {
  name: 'StudentAnalysisTab',
  props: {
    courseId: {
      type: Number,
      required: true
    }
  },
  data() {
    return {
      filters: {
        courseId: null,
        studentId: null
      },
      knowledgeScores: [],
      weakKnowledge: [],
      suggestions: [],
      scoreTrend: [],
      riskList: [],
      performanceData: {},
      loading: false,
      error: '',
      studentLoading: false,
      studentError: '',
      studentOptions: [],
      
      // 风险报告相关
      riskTypeCount: {},
      riskKpList: [],
      highRiskStudents: [],
      
      // 成绩趋势相关
      trendPeriod: '7d',
      fullScoreTrend: [],
      
      activeTab: 'overall',
      isDropdownOpen: false,
      
      // Charts
      graphChart: null,
      pieChart: null,
      riskTypeChart: null,
      trendChart: null,
    }
  },
  computed: {
    courseName() {
      return '当前课程'
    },
    overallAverage() {
      const val = this.performanceData.overallAverageScore
      if (val === undefined || val === null) return ''
      return this.formatPercent(val)
    },
    riskMap() {
      const map = {}
      this.highRiskStudents.forEach(item => {
        const currentRisk = map[item.studentId]
        if (!currentRisk || item.riskLevel === 'HIGH') {
          map[item.studentId] = item.riskLevel
        } else if (item.riskLevel === 'MEDIUM' && currentRisk !== 'HIGH') {
          map[item.studentId] = item.riskLevel
        }
      })
      return map
    },
    selectedStudentName() {
      const s = this.studentOptions.find(s => s.id === this.filters.studentId)
      return s ? s.name : ''
    },
    sortedStudentOptions() {
      if (!this.studentOptions.length) return []
      
      const students = [...this.studentOptions]
      const riskPriority = { 'HIGH': 3, 'MEDIUM': 2, 'LOW': 1 }
      
      return students.sort((a, b) => {
        const riskA = this.riskMap[a.id]
        const riskB = this.riskMap[b.id]
        const pA = riskPriority[riskA] || 0
        const pB = riskPriority[riskB] || 0
        
        if (pA !== pB) return pB - pA
        return a.id - b.id
      })
    }
  },
  watch: {
    courseId: {
      immediate: true,
      handler(newVal) {
        console.log('[Watch courseId] 值变化:', newVal)
        if (newVal) {
          this.filters.courseId = newVal
          console.log('[Watch courseId] 开始加载学生列表和数据')
          this.fetchStudents()
          this.reload()
        }
      }
    },
    'filters.studentId'(newVal) {
      if (newVal) {
        this.reload()
      }
    },
    studentOptions(newVal) {
      if (newVal.length > 0 && !this.filters.studentId && this.activeTab === 'detailed') {
        this.isDropdownOpen = true
      }
    },
    activeTab: {
      handler(newVal) {
        console.log('[activeTab] 标签页切换到:', newVal)
        this.$nextTick(() => {
          if (newVal === 'detailed') {
            if (!this.filters.studentId && this.studentOptions.length > 0) {
              this.isDropdownOpen = true
            }
            if (this.graphChart) this.graphChart.resize()
            if (this.pieChart) this.pieChart.resize()
            if (this.trendChart) this.trendChart.resize()
          } else if (newVal === 'overall') {
            console.log('[activeTab] 切换到整体情况，检查风险报告数据:', {
              riskTypeCount: Object.keys(this.riskTypeCount).length,
              riskKpList: this.riskKpList.length,
              highRiskStudents: this.highRiskStudents.length
            })
            
            // 延迟一下确保 DOM 完全可见
            setTimeout(() => {
              console.log('[activeTab] 延迟后尝试渲染风险类型图表')
              this.tryRenderRiskChart()
            }, 100)
            
            // 如果图表已存在，调整大小
            if (this.riskTypeChart) {
              this.riskTypeChart.resize()
            }
          }
        })
      }
    },
    riskTypeCount: {
      handler(newVal) {
        console.log('[riskTypeCount] 数据变化:', Object.keys(newVal).length, '个风险类型')
        // 当风险数据更新且在整体情况标签页时，尝试渲染图表
        if (this.activeTab === 'overall' && Object.keys(newVal).length > 0) {
          this.$nextTick(() => {
            console.log('[riskTypeCount] 触发图表渲染尝试')
            this.tryRenderRiskChart()
          })
        }
      },
      deep: true
    }
  },
  mounted() {
    console.log('[mounted] 组件已挂载, courseId:', this.courseId, 'activeTab:', this.activeTab)
    // 如果默认是整体情况标签页，延迟检查图表渲染
    if (this.activeTab === 'overall') {
      // 使用多次尝试确保图表能渲染
      console.log('[mounted] 默认标签页是整体情况，开始多次检查渲染')
      
      // 第一次尝试：立即检查
      this.$nextTick(() => {
        this.tryRenderRiskChart()
      })
      
      // 第二次尝试：500ms 后
      setTimeout(() => {
        console.log('[mounted] 500ms 后再次检查')
        this.tryRenderRiskChart()
      }, 500)
      
      // 第三次尝试：1000ms 后
      setTimeout(() => {
        console.log('[mounted] 1000ms 后最后检查')
        this.tryRenderRiskChart()
      }, 1000)
    }
  },
  activated() {
    // 如果使用了 keep-alive，在组件激活时也触发检查
    console.log('[activated] 组件被激活, activeTab:', this.activeTab)
    if (this.activeTab === 'overall') {
      this.$nextTick(() => {
        if (Object.keys(this.riskTypeCount).length > 0 && this.$refs.riskTypeChartRef) {
          console.log('[activated] 重新渲染风险类型图表')
          this.renderRiskTypeChart()
        }
      })
    }
  },
  beforeDestroy() {
    this.disposeCharts()
  },
  methods: {
    toggleDropdown() {
      if (this.studentLoading || !this.studentOptions.length) return
      this.isDropdownOpen = !this.isDropdownOpen
    },
    
    selectStudent(stu) {
      this.filters.studentId = stu.id
      this.isDropdownOpen = false
    },
    
    formatPercent(val) {
      if (val === undefined || val === null || Number.isNaN(val)) return 0
      const num = Number(val)
      return num > 1 ? Number(num.toFixed(1)) : Number((num * 100).toFixed(1))
    },
    
    setTrendPeriod(period) {
      this.trendPeriod = period
      this.renderTrendChart()
    },
    
    tryRenderRiskChart() {
      console.log('[tryRenderRiskChart] 尝试渲染风险图表')
      console.log('[tryRenderRiskChart] 状态检查:', {
        activeTab: this.activeTab,
        hasData: Object.keys(this.riskTypeCount).length,
        hasRef: !!this.$refs.riskTypeChartRef
      })
      
      if (this.activeTab === 'overall' && Object.keys(this.riskTypeCount).length > 0) {
        if (this.$refs.riskTypeChartRef) {
          console.log('[tryRenderRiskChart] 条件满足，开始渲染（忽略可见性检查）')
          this.renderRiskTypeChart()
        } else {
          console.log('[tryRenderRiskChart] ref 不存在，跳过')
        }
      } else {
        console.log('[tryRenderRiskChart] 条件不满足:', {
          wrongTab: this.activeTab !== 'overall',
          noData: Object.keys(this.riskTypeCount).length === 0
        })
      }
    },
    
    async reload(silent = false) {
      if (!this.filters.courseId) {
        this.error = "请先选择课程"
        this.clearData()
        return
      }
      
      console.log('[Reload] 开始重新加载数据, courseId:', this.filters.courseId, 'studentId:', this.filters.studentId)
      
      if (!silent) this.loading = true
      this.error = ''
      try {
        const promises = [this.fetchRisk(), this.fetchRiskReport()]
        if (this.filters.studentId) {
          promises.push(this.fetchPerformance())
        } else {
          this.knowledgeScores = []
          this.weakKnowledge = []
          this.suggestions = []
          this.scoreTrend = []
          this.performanceData = {}
        }
        await Promise.all(promises)
        console.log('[Reload] 数据加载完成')
        await this.$nextTick()
        this.renderCharts()
      } catch (e) {
        console.error('[Reload] 加载失败:', e)
        this.error = e.message || "加载失败"
      } finally {
        if (!silent) this.loading = false
      }
    },
    
    async fetchStudents() {
      if (!this.filters.courseId) return
      console.log('[fetchStudents] 开始加载学生列表, courseId:', this.filters.courseId)
      this.studentLoading = true
      this.studentError = ''
      try {
        // 方案1: 优先尝试从RuoYi获取课程学生列表
        try {
          const response = await request({
            url: '/system/class/student/list',
            method: 'get',
            params: { 
              courseId: this.filters.courseId,
              status: 1  // 只获取已批准的学生
            }
          })
          
          if (response.rows && response.rows.length > 0) {
            // 从选课记录中提取学生信息
            this.studentOptions = response.rows.map(enrollment => ({
              id: enrollment.studentUserId,
              name: enrollment.studentName || `学生${enrollment.studentUserId}`
            }))
            console.log('[fetchStudents] 成功从RuoYi加载学生:', this.studentOptions.length, '人')
            return
          }
        } catch (e) {
          console.warn("RuoYi课程学生API调用失败，尝试备用方案:", e)
        }
        
        // 方案2: 备用方案 - 从8083获取所有学生（无课程筛选）
        const url = `${BASE_URL}/api/ai-grading/users?pageSize=1000`
        const resp = await fetch(url)
        const data = await resp.json()
        
        if (data.code === 200 || data.code === 0) {
          const allUsers = data.data?.records || data.data || []
          const students = allUsers.filter(u => u.role === 'STUDENT')
          
          this.studentOptions = students.map(u => ({
            id: u.id,
            name: u.realName || u.username || `学生${u.id}`
          }))
        } else {
          throw new Error(data.message || "获取学生列表失败")
        }
      } catch (e) {
        console.error("[fetchStudents] 加载失败:", e)
        this.studentError = "获取学生列表失败，请检查网络"
        this.studentOptions = []
      } finally {
        this.studentLoading = false
        console.log('[fetchStudents] 完成, studentOptions:', this.studentOptions.length)
      }
    },
    
    async fetchPerformance() {
      if (!this.filters.courseId || !this.filters.studentId) return
      
      const url = `${BASE_URL}/api/analysis/performance?courseId=${this.filters.courseId}&studentUserId=${this.filters.studentId}`
      const resp = await fetch(url)
      const data = await resp.json()
      if (!resp.ok || data.code !== 200) {
        throw new Error(data.message || data.msg || "获取成绩失败")
      }
      this.performanceData = data.data || {}
      this.knowledgeScores = this.performanceData.knowledgeScores || []
      this.weakKnowledge = this.performanceData.weakKnowledge || []
      this.suggestions = this.performanceData.suggestions || []
      this.fullScoreTrend = this.performanceData.scoreTrend || []
      this.filterTrendData()
    },
    
    async fetchRisk() {
      if (!this.filters.courseId) return
      
      const url = `${BASE_URL}/api/analysis/risk/report?courseId=${this.filters.courseId}`
      const resp = await fetch(url)
      const data = await resp.json()
      if (!resp.ok || data.code !== 200) {
        this.riskList = []
        return
      }
      this.riskList = data.data?.kpRiskList?.map((item) => {
        const mastery = item.averageMastery || 0
        const percent = mastery > 1 ? mastery : mastery * 100
        return { kpTitle: item.kpTitle, riskValue: Math.max(10, 100 - percent) }
      }) || []
    },
    
    async fetchRiskReport() {
      if (!this.filters.courseId) return
      
      const url = `${BASE_URL}/api/analysis/risk/report?courseId=${this.filters.courseId}`
      console.log('[风险报告] 开始加载, courseId:', this.filters.courseId)
      try {
        const resp = await fetch(url)
        const data = await resp.json()
        console.log('[风险报告] API响应:', data)
        const reportData = data.data || {}
        this.riskTypeCount = reportData.riskTypeCount || {}
        this.riskKpList = reportData.kpRiskList || []
        this.highRiskStudents = reportData.highRiskStudents || []
        console.log('[风险报告] 数据加载完成 - 风险类型:', Object.keys(this.riskTypeCount).length, 
                    '高风险知识点:', this.riskKpList.length, 
                    '高风险学生:', this.highRiskStudents.length)
        
        // 数据加载完成后，如果当前在整体情况标签页，则立即渲染风险类型图表
        await this.$nextTick()
        if (this.activeTab === 'overall') {
          console.log('[风险报告] 当前在整体情况标签页，立即渲染图表')
          this.renderRiskTypeChart()
        } else {
          console.log('[风险报告] 当前不在整体情况标签页，等待标签页切换时渲染')
        }
      } catch (e) {
        console.warn("[风险报告] 加载失败", url, e)
        this.riskTypeCount = {}
        this.riskKpList = []
        this.highRiskStudents = []
      }
    },
    
    filterTrendData() {
      if (!this.fullScoreTrend.length) {
        this.scoreTrend = []
        return
      }
      
      const now = new Date()
      let days = 7
      if (this.trendPeriod === '30d') {
        days = 30
      }
      
      const startDate = new Date(now)
      startDate.setDate(startDate.getDate() - days)
      
      this.scoreTrend = this.fullScoreTrend.filter(item => {
        const itemDate = new Date(item.date)
        return itemDate >= startDate && itemDate <= now
      })
    },
    
    clearData() {
      this.knowledgeScores = []
      this.weakKnowledge = []
      this.suggestions = []
      this.scoreTrend = []
      this.riskList = []
      this.riskTypeCount = {}
      this.riskKpList = []
      this.highRiskStudents = []
      this.disposeCharts()
    },
    
    disposeCharts() {
      if (this.graphChart) this.graphChart.dispose()
      if (this.pieChart) this.pieChart.dispose()
      if (this.riskTypeChart) this.riskTypeChart.dispose()
      if (this.trendChart) this.trendChart.dispose()
    },
    
    renderCharts() {
      this.renderGraph()
      this.renderPie()
      this.renderRiskTypeChart()
      this.renderTrendChart()
    },
    
    renderGraph() {
      if (!this.$refs.graphRef) return
      if (this.graphChart) this.graphChart.dispose()
      this.graphChart = echarts.init(this.$refs.graphRef)
      
      const nodes = this.knowledgeScores.map((item, idx) => {
        const percent = this.formatPercent(item.scoreRatio || item.masteryScore)
        const weak = this.weakKnowledge.some((w) => w.kpId === item.kpId)
        return {
          id: String(item.kpId || idx),
          name: item.kpTitle || `KP-${idx + 1}`,
          value: percent,
          symbolSize: Math.max(20, Math.min(60, percent / 2)),
          itemStyle: {
            color: weak ? "#ef4444" : percent >= 80 ? "#10b981" : percent >= 60 ? "#f59e0b" : "#ef4444",
          },
        }
      })
      
      const links = nodes.map((node, idx) => ({
        source: node.id,
        target: nodes[(idx + 1) % nodes.length]?.id || node.id,
      }))
      
      this.graphChart.setOption({
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
      })
    },
    
    renderPie() {
      if (!this.$refs.pieRef) return
      if (this.pieChart) this.pieChart.dispose()
      this.pieChart = echarts.init(this.$refs.pieRef)
      
      const hasData = this.knowledgeScores && this.knowledgeScores.length > 0
      
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
            data: hasData ? this.knowledgeScores.map((item) => ({
              name: item.kpTitle || "KP",
              value: this.formatPercent(item.scoreRatio || item.masteryScore),
            })) : [{ value: 1, name: '暂无数据', itemStyle: { color: '#f0f2f5' } }],
            animationEasing: "elasticOut",
            animationDuration: 1200,
            silent: !hasData
          },
        ],
      }
      
      this.pieChart.setOption(option)
    },
    
    renderRiskTypeChart() {
      console.log('[风险类型图表] 开始渲染, activeTab:', this.activeTab)
      
      if (!this.$refs.riskTypeChartRef) {
        console.log('[风险类型图表] ref不存在，DOM可能未就绪')
        return
      }
      
      const refElement = this.$refs.riskTypeChartRef
      console.log('[风险类型图表] ref元素找到，准备初始化图表')
      
      if (this.riskTypeChart) {
        console.log('[风险类型图表] 销毁旧图表实例')
        this.riskTypeChart.dispose()
      }
      
      this.riskTypeChart = echarts.init(refElement)
      
      const riskTypes = Object.keys(this.riskTypeCount)
      const counts = Object.values(this.riskTypeCount)
      
      console.log('[风险类型图表] 渲染数据 - 类型:', riskTypes, '数量:', counts)
      
      this.riskTypeChart.setOption({
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
      })
      
      console.log('[风险类型图表] 渲染完成')
    },
    
    renderTrendChart() {
      if (!this.$refs.trendChartRef) return
      if (this.trendChart) this.trendChart.dispose()
      this.trendChart = echarts.init(this.$refs.trendChartRef)
      
      this.filterTrendData()
      
      const dates = this.scoreTrend.map(item => {
        const date = new Date(item.date)
        return `${date.getMonth()+1}-${date.getDate()}`
      })
      
      const scores = this.scoreTrend.map(item => this.formatPercent(item.scoreRatio))
      
      // 计算平均分
      const avgScore = scores.reduce((sum, score) => sum + score, 0) / scores.length
      const avgScores = Array(dates.length).fill(avgScore)
      
      this.trendChart.setOption({
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
      })
    }
  }
}
</script>

<style scoped>
.analysis-container {
  position: relative;
  min-height: 600px;
  padding: 20px;
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

.loading-indicator {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100px 20px;
  gap: 16px;
}

.loading-indicator p {
  font-size: 16px;
  color: #606266;
  margin: 0;
}

.overall-tab-content {
  min-height: 400px;
}

.top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  position: relative;
  margin-bottom: 20px;
}

.top h1 {
  font-size: 28px;
  font-weight: 700;
  color: #0f172a;
  margin: 0 0 8px 0;
}

.student-select-wrapper {
  position: relative;
  z-index: 1001;
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
  margin: 0;
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

button {
  padding: 8px 12px;
  border-radius: 10px;
  border: none;
  cursor: pointer;
  font-size: 14px;
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
  padding: 20px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05);
  margin-top: 16px;
}

.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.panel-head h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: #0f172a;
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
  grid-template-columns: repeat(3, 1fr);
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

.empty-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 200px;
  padding: 40px 20px;
  color: #909399;
  text-align: center;
}

.empty-placeholder p {
  margin: 12px 0 0 0;
  font-size: 14px;
  color: #909399;
}

.empty-placeholder i {
  margin-bottom: 8px;
}

.panel h4 {
  margin: 20px 0 12px 0;
  font-size: 16px;
  font-weight: 600;
  color: #0f172a;
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
