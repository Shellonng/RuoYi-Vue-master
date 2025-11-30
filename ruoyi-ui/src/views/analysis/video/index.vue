<template>
  <div class="video-analysis-container">
    <!-- 顶部课程选择器 -->
    <header class="top-header">
      <div>
        <h1>视频学习分析</h1>
        <p class="sub">分析学生视频观看行为、完课率与学习成效的关系</p>
      </div>
      <div class="filters">
        <label class="course-select-label">
          <span>课程</span>
          <select v-model="filters.courseId" class="course-select">
            <option :value="null">请选择课程</option>
            <option v-for="course in courseOptions" :key="course.id" :value="course.id">
              {{ course.title }}
            </option>
          </select>
        </label>
      </div>
    </header>

    <!-- 未选择课程提示 -->
    <div v-if="!filters.courseId" class="course-select-prompt">
      <i class="el-icon-video-camera" style="font-size: 64px; color: #409eff;"></i>
      <h3>请先选择课程</h3>
      <p>请在右上角的课程下拉框中选择一个课程，以查看该课程的视频学习分析数据</p>
    </div>

    <!-- 主要内容区域 -->
    <div v-else class="video-analysis-layout">
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
          :class="{ active: currentVideo && currentVideo.id === video.id }"
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

    <!-- AI 结果弹窗 -->
    <el-dialog
      :title="currentAiTitle"
      :visible.sync="showAiModal"
      width="700px"
      top="5vh"
      :close-on-click-modal="false"
    >
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
      
      <div slot="footer" class="dialog-footer">
        <el-button :disabled="currentAiPage === 0" @click="prevAiPage">上一页</el-button>
        <span class="page-indicator">{{ currentAiPage + 1 }} / {{ aiPages.length }}</span>
        <el-button :disabled="currentAiPage === aiPages.length - 1" @click="nextAiPage">下一页</el-button>
      </div>
    </el-dialog>
  </div>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { Liquid, Gauge, Rose } from '@antv/g2plot'
import request from '@/utils/request'

const BASE_URL = process.env.NODE_ENV === 'production' ? 'http://localhost:8083' : '/smart-api'

export default {
  name: 'VideoAnalysis',
  data() {
    return {
      filters: {
        courseId: null
      },
      courseOptions: [],
      videoList: [],
      currentVideo: null,
      completionStats: {
        completionRate: 0,
        averageCompletionRate: 0,
        uniqueStudents: 0,
        totalRecords: 0,
        totalWatchDuration: 0,
      },
      correlationData: {
        completionRateCorrelation: 0,
        durationCorrelation: 0,
        watchCountCorrelation: 0,
        sampleSize: 0,
        samples: []
      },
      heatmapData: [],
      distributionData: {},
      
      // AI 相关状态
      aiResult: null,
      aiSuggestions: null,
      aiLoading: false,
      showAiModal: false,
      currentAiPage: 0,
      
      // Charts
      heatmapChart: null,
      liquidChart: null,
      gaugeChart: null,
      roseChart: null,
      correlationChart: null,
    }
  },
  computed: {
    aiPages() {
      if (!this.aiResult && !this.aiSuggestions) return []
      const pages = []
      
      // Page 0: 总体评价
      if (this.aiResult && this.aiResult.comment) {
        const paragraphs = this.aiResult.comment.split('\n').filter(p => p.trim())
        pages.push({ type: 'summary', title: '总体评价', content: paragraphs })
      }
      
      // Page 1: 数据洞察
      if (this.aiSuggestions && this.aiSuggestions.insights) {
        pages.push({ type: 'insights', title: '数据洞察', data: this.aiSuggestions.insights })
      }
      
      // Page 2: 改进建议
      if (this.aiSuggestions && this.aiSuggestions.recommendations) {
        pages.push({ type: 'recommendations', title: '改进建议', data: this.aiSuggestions.recommendations })
      }
      
      return pages
    },
    currentAiTitle() {
      if (this.aiPages.length > 0 && this.currentAiPage < this.aiPages.length) {
        return this.aiPages[this.currentAiPage].title
      }
      return 'AI 学习洞察'
    }
  },
  watch: {
    'filters.courseId'(newVal, oldVal) {
      if (newVal && newVal !== oldVal) {
        // 课程切换时清空数据
        this.videoList = []
        this.currentVideo = null
        this.clearAllData()
        // 重新获取视频列表
        this.fetchVideoList()
      }
    }
  },
  mounted() {
    this.fetchCourses()
  },
  beforeDestroy() {
    this.disposeCharts()
  },
  methods: {
    async fetchCourses() {
      try {
        const response = await request({
          url: '/course/list',
          method: 'get'
        })
        
        this.courseOptions = response.rows || []
        
        // 自动选择第一个课程
        if (this.courseOptions.length > 0 && !this.filters.courseId) {
          this.filters.courseId = this.courseOptions[0].id
        }
      } catch (e) {
        console.error("获取课程列表失败:", e)
        this.$message.error('获取课程列表失败')
      }
    },
    
    async fetchVideoList() {
      if (!this.filters.courseId) return
      
      try {
        const res = await fetch(`${BASE_URL}/api/analysis/video/list?courseId=${this.filters.courseId}`)
        const data = await res.json()
        if (data.code === 200) {
          this.videoList = data.data || []
          // 默认选中第一个视频
          if (this.videoList.length > 0) {
            this.selectVideo(this.videoList[0])
          }
        }
      } catch (e) {
        console.error('Failed to fetch video list', e)
        this.$message.error('获取视频列表失败')
      }
    },
    
    async selectVideo(video) {
      this.currentVideo = video
      await this.$nextTick()
      await Promise.all([
        this.fetchHeatmap(video.id),
        this.fetchCompletionStats(video.id),
        this.fetchCorrelation(video.id)
      ])
      this.renderHeatmap()
      this.renderG2Charts()
      this.renderCorrelationChart()
    },
    
    async fetchHeatmap(videoId) {
      try {
        const res = await fetch(`${BASE_URL}/api/analysis/video/heatmap?videoId=${videoId}`)
        const data = await res.json()
        if (data.code === 200) {
          this.heatmapData = data.data?.heatmapBuckets || []
        }
      } catch (e) {
        console.error('Failed to fetch heatmap', e)
      }
    },
    
    async fetchCompletionStats(videoId) {
      try {
        const res = await fetch(`${BASE_URL}/api/analysis/video/completion?courseId=${this.filters.courseId}&videoId=${videoId}`)
        const data = await res.json()
        if (data.code === 200) {
          const d = data.data
          this.completionStats.completionRate = d.completionRate
          this.completionStats.averageCompletionRate = d.averageCompletionRate
          this.completionStats.uniqueStudents = d.uniqueStudents
          this.completionStats.totalRecords = d.totalRecords
          this.completionStats.totalWatchDuration = d.totalWatchDuration
          this.distributionData = d.completionDistribution || {}
        }
      } catch (e) {
        console.error('Failed to fetch stats', e)
      }
    },
    
    async fetchCorrelation(videoId) {
      try {
        const res = await fetch(`${BASE_URL}/api/analysis/video/behavior-correlation?courseId=${this.filters.courseId}${videoId ? `&videoId=${videoId}` : ""}`)
        const data = await res.json()
        if (data.code === 200) {
          const d = data.data
          this.correlationData.completionRateCorrelation = d.completionRateCorrelation
          this.correlationData.durationCorrelation = d.durationCorrelation
          this.correlationData.watchCountCorrelation = d.watchCountCorrelation
          this.correlationData.sampleSize = d.sampleSize || 0
          this.correlationData.samples = d.samples || []
        }
      } catch (e) {
        console.error('Failed to fetch correlation', e)
      }
    },
    
    formatBucket(bucket) {
      if (bucket === undefined || bucket === null) return ""
      const str = String(bucket)
      const parts = str.split("-")
      if (parts.length === 2) {
        const start = parseInt(parts[0])
        const end = parseInt(parts[1])
        if (!isNaN(start) && !isNaN(end)) {
          return `${this.formatTimeSimple(start)}-${this.formatTimeSimple(end)}`
        }
      }
      if (!isNaN(parseFloat(str)) && isFinite(str)) {
        return this.formatTimeSimple(parseInt(str))
      }
      return str
    },
    
    formatTimeSimple(s) {
      const m = Math.floor(s / 60)
      const sec = s % 60
      return `${m}:${sec.toString().padStart(2, '0')}`
    },
    
    renderHeatmap() {
      if (!this.$refs.heatmapRef) return
      if (this.heatmapChart) {
        this.heatmapChart.dispose()
      }
      this.heatmapChart = echarts.init(this.$refs.heatmapRef)
      
      const xData = this.heatmapData.map(item => this.formatBucket(item.range))
      const yData = this.heatmapData.map(item => item.count)
      
      this.heatmapChart.setOption({
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
      })
    },
    
    renderG2Charts() {
      // Liquid Chart
      if (this.$refs.liquidRef) {
        if (this.liquidChart) {
          this.liquidChart.destroy()
        }
        this.liquidChart = new Liquid(this.$refs.liquidRef, {
          percent: this.completionStats.completionRate / 100,
          outline: { border: 4, distance: 8 },
          wave: { length: 128 },
          height: 160,
          autoFit: true,
        })
        this.liquidChart.render()
      }
      
      // Gauge Chart
      if (this.$refs.gaugeRef) {
        if (this.gaugeChart) {
          this.gaugeChart.destroy()
        }
        this.gaugeChart = new Gauge(this.$refs.gaugeRef, {
          percent: this.completionStats.averageCompletionRate / 100,
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
        })
        this.gaugeChart.render()
      }
      
      // Rose Chart
      if (this.$refs.roseRef) {
        if (this.roseChart) {
          this.roseChart.destroy()
        }
        const data = [
          { type: '0-50%', value: this.distributionData['0-50'] || 0 },
          { type: '50-80%', value: this.distributionData['50-80'] || 0 },
          { type: '80-100%', value: this.distributionData['80-100'] || 0 }
        ]
        this.roseChart = new Rose(this.$refs.roseRef, {
          data,
          xField: 'type',
          yField: 'value',
          seriesField: 'type',
          radius: 0.9,
          legend: { position: 'bottom' },
          height: 300,
          autoFit: true,
        })
        this.roseChart.render()
      }
    },
    
    renderCorrelationChart() {
      if (!this.$refs.correlationRef) return
      if (this.correlationChart) {
        this.correlationChart.dispose()
      }
      this.correlationChart = echarts.init(this.$refs.correlationRef)
      
      const data = this.correlationData.samples.map(s => [s.avgCompletionRate, s.avgScore])
      
      this.correlationChart.setOption({
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
      })
    },
    
    async analyzeAi() {
      if (!this.filters.courseId) return
      
      this.aiLoading = true
      this.aiResult = null
      this.aiSuggestions = null
      this.showAiModal = false
      this.currentAiPage = 0
      
      try {
        const FIXED_MODEL = 'deepseek-ai/DeepSeek-V3.2-Exp'
        let url = `${BASE_URL}/api/analysis/video/behavior-ai?courseId=${this.filters.courseId}&model=${encodeURIComponent(FIXED_MODEL)}`
        if (this.currentVideo) {
          url += `&videoId=${this.currentVideo.id}`
        }
        const res = await fetch(url)
        const data = await res.json()
        
        if (data.code === 200 && data.data) {
          this.aiResult = data.data
          if (data.data.suggestionsJson) {
            try {
              this.aiSuggestions = JSON.parse(data.data.suggestionsJson)
            } catch (parseErr) {
              console.error("JSON Parse error", parseErr)
            }
          }
          this.showAiModal = true
        } else {
          this.$message.error(data.message || "分析失败")
        }
      } catch (e) {
        console.error("AI Analysis failed", e)
        this.$message.error("请求失败，请检查网络或密钥配置")
      } finally {
        this.aiLoading = false
      }
    },
    
    nextAiPage() {
      if (this.currentAiPage < this.aiPages.length - 1) {
        this.currentAiPage++
      }
    },
    
    prevAiPage() {
      if (this.currentAiPage > 0) {
        this.currentAiPage--
      }
    },
    
    formatDuration(seconds) {
      if (!seconds) return "0s"
      const h = Math.floor(seconds / 3600)
      const m = Math.floor((seconds % 3600) / 60)
      const s = seconds % 60
      return h > 0 ? `${h}h ${m}m ${s}s` : `${m}m ${s}s`
    },
    
    getCorrelationClass(val) {
      if (val > 0.5) return 'text-green'
      if (val < -0.5) return 'text-red'
      return 'text-gray'
    },
    
    getStrengthClass(s) {
      if (s === '强相关') return 'tag-strong'
      if (s === '中等相关') return 'tag-medium'
      return 'tag-weak'
    },
    
    getPriorityClass(p) {
      if (p === '高') return 'badge-high'
      if (p === '中') return 'badge-medium'
      return 'badge-low'
    },
    
    clearAllData() {
      this.completionStats = {
        completionRate: 0,
        averageCompletionRate: 0,
        uniqueStudents: 0,
        totalRecords: 0,
        totalWatchDuration: 0,
      }
      this.correlationData = {
        completionRateCorrelation: 0,
        durationCorrelation: 0,
        watchCountCorrelation: 0,
        sampleSize: 0,
        samples: []
      }
      this.heatmapData = []
      this.distributionData = {}
      this.aiResult = null
      this.aiSuggestions = null
      this.disposeCharts()
    },
    
    disposeCharts() {
      if (this.heatmapChart) this.heatmapChart.dispose()
      if (this.liquidChart) this.liquidChart.destroy()
      if (this.gaugeChart) this.gaugeChart.destroy()
      if (this.roseChart) this.roseChart.destroy()
      if (this.correlationChart) this.correlationChart.dispose()
    }
  }
}
</script>

<style scoped>
.video-analysis-container {
  padding: 20px;
  background: #f8fafc;
  min-height: calc(100vh - 100px);
}

.top-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 16px 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.top-header h1 {
  font-size: 28px;
  font-weight: 700;
  color: #0f172a;
  margin: 0 0 8px 0;
}

.top-header .sub {
  color: #64748b;
  margin: 0;
  font-size: 14px;
}

.filters {
  display: flex;
  gap: 12px;
  align-items: center;
}

.course-select-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
  color: #334155;
}

.course-select {
  padding: 8px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: #fff;
  font-size: 14px;
  color: #334155;
  cursor: pointer;
  transition: all 0.2s;
  min-width: 200px;
}

.course-select:hover {
  border-color: #3b82f6;
}

.course-select:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.2);
}

.course-select-prompt {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  padding: 60px 20px;
  text-align: center;
  background: #fff;
  border-radius: 12px;
  margin-top: 20px;
}

.course-select-prompt h3 {
  margin: 20px 0 12px 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.course-select-prompt p {
  margin: 0;
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
}

.video-analysis-layout {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 32px;
  min-height: calc(100vh - 280px);
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

.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: #94a3b8;
  font-size: 14px;
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
  grid-template-columns: 2fr 1fr;
  gap: 24px;
  align-items: start;
}

.video-analysis-col {
  display: flex;
  flex-direction: column;
  gap: 24px;
  min-width: 0;
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

.text-green {
  color: #10b981;
  font-weight: bold;
}

.text-red {
  color: #ef4444;
  font-weight: bold;
}

.text-gray {
  color: #6b7280;
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

/* AI Modal Content */
.ai-content-wrapper {
  min-height: 300px;
  max-height: 500px;
  overflow-y: auto;
}

.ai-long-text {
  font-size: 16px;
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
  gap: 8px;
}

.metric-name, .issue-name {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
}

.large-desc {
  font-size: 14px;
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

.dialog-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0 0;
  border-top: 1px solid #f1f5f9;
}

.page-indicator {
  font-size: 14px;
  color: #64748b;
  font-weight: 500;
}

/* Responsive */
@media (max-width: 1280px) {
  .analysis-dashboard {
    grid-template-columns: 1fr;
  }
  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
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
</style>
