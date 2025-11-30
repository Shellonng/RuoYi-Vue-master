<template>
  <div class="video-analysis-container">
    <!-- 主要内容区域 -->
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
                <button @click="runAiAnalysis" :disabled="aiLoading" class="btn-ai">
                  <span v-if="aiLoading" class="spinner"></span>
                  {{ aiLoading ? '分析中...' : '生成分析' }}
                </button>
              </div>
              
              <div v-if="aiAnalysisResult" class="ai-result-preview">
                <div class="ai-content" v-html="formattedAiResult"></div>
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
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { Liquid, Gauge, Rose } from '@antv/g2plot'

const BASE_URL = process.env.NODE_ENV === 'production' ? 'http://localhost:8083' : '/smart-api'

export default {
  name: 'VideoAnalysisTab',
  props: {
    courseId: {
      type: Number,
      required: true
    }
  },
  data() {
    return {
      currentVideo: null,
      videoList: [],
      videoLoading: false,
      videoError: '',
      
      heatmapData: [],
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
      distributionData: {},
      aiAnalysisResult: '',
      aiLoading: false,
      
      // G2Plot instances
      liquidChart: null,
      gaugeChart: null,
      roseChart: null,
      
      // ECharts instances
      heatmapChart: null,
      correlationChart: null,
    }
  },
  computed: {
    formattedAiResult() {
      if (!this.aiAnalysisResult) return ''
      return this.aiAnalysisResult
        .replace(/\n/g, '<br>')
        .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
        .replace(/### (.*?)(<br>|$)/g, '<h4>$1</h4>')
    }
  },
  watch: {
    courseId: {
      immediate: true,
      handler(newVal) {
        if (newVal) {
          this.fetchVideoList()
        }
      }
    }
  },
  beforeDestroy() {
    this.disposeCharts()
  },
  methods: {
    async fetchVideoList() {
      this.videoLoading = true
      this.videoError = ''
      try {
        const url = `${BASE_URL}/api/analysis/video/list?courseId=${this.courseId}`
        const resp = await fetch(url)
        const data = await resp.json()
        
        if (!resp.ok || (data.code !== 200 && data.code !== 0)) {
          throw new Error(data.message || data.msg || '获取视频列表失败')
        }
        
        this.videoList = data.data || []
        
        // 如果有视频，默认选中第一个
        if (this.videoList.length > 0 && !this.currentVideo) {
          this.selectVideo(this.videoList[0])
        }
      } catch (e) {
        console.error('Fetch video list error:', e)
        this.videoError = e.message || '获取视频列表失败'
        this.videoList = []
      } finally {
        this.videoLoading = false
      }
    },
    
    selectVideo(video) {
      this.currentVideo = video
      this.loadAnalysisData()
    },
    
    async loadAnalysisData() {
      if (!this.courseId || !this.currentVideo) return
      
      await Promise.all([
        this.fetchHeatmap(),
        this.fetchCompletionStats(),
        this.fetchCorrelation()
      ])
      
      this.$nextTick(() => {
        this.renderCharts()
      })
    },
    
    async fetchHeatmap() {
      try {
        const url = `${BASE_URL}/api/analysis/video/heatmap?videoId=${this.currentVideo.id}`
        console.log('Fetching heatmap from:', url)
        const resp = await fetch(url)
        const data = await resp.json()
        console.log('Heatmap response:', data)
        
        if (data.code === 200 || data.code === 0) {
          this.heatmapData = data.data?.heatmapBuckets || []
          console.log('Heatmap buckets:', this.heatmapData)
        } else {
          this.heatmapData = []
        }
      } catch (e) {
        console.error('Fetch heatmap error:', e)
        this.heatmapData = []
      }
    },
    
    async fetchCompletionStats() {
      try {
        const url = `${BASE_URL}/api/analysis/video/completion?courseId=${this.courseId}&videoId=${this.currentVideo.id}`
        const resp = await fetch(url)
        const data = await resp.json()
        
        if (data.code === 200 || data.code === 0) {
          const d = data.data
          this.completionStats.completionRate = d.completionRate || 0
          this.completionStats.averageCompletionRate = d.averageCompletionRate || 0
          this.completionStats.uniqueStudents = d.uniqueStudents || 0
          this.completionStats.totalRecords = d.totalRecords || 0
          this.completionStats.totalWatchDuration = d.totalWatchDuration || 0
          this.distributionData = d.completionDistribution || {}
        }
      } catch (e) {
        console.error('Fetch completion stats error:', e)
      }
    },
    
    async fetchCorrelation() {
      try {
        const url = `${BASE_URL}/api/analysis/video/behavior-correlation?courseId=${this.courseId}&videoId=${this.currentVideo.id}`
        console.log('Fetching correlation from:', url)
        const resp = await fetch(url)
        const data = await resp.json()
        console.log('Correlation response:', data)
        
        if (data.code === 200 || data.code === 0) {
          const d = data.data
          this.correlationData.completionRateCorrelation = d.completionRateCorrelation || 0
          this.correlationData.durationCorrelation = d.durationCorrelation || 0
          this.correlationData.watchCountCorrelation = d.watchCountCorrelation || 0
          this.correlationData.sampleSize = d.sampleSize || 0
          this.correlationData.samples = d.samples || []
          console.log('Correlation samples:', this.correlationData.samples)
        }
      } catch (e) {
        console.error('Fetch correlation error:', e)
      }
    },
    
    async runAiAnalysis() {
      if (!this.courseId || !this.currentVideo) return
      
      this.aiLoading = true
      this.aiAnalysisResult = ''
      
      try {
        const url = `${BASE_URL}/api/analysis/video/behavior-ai?courseId=${this.courseId}&videoId=${this.currentVideo.id}`
        const resp = await fetch(url)
        const data = await resp.json()
        
        if (data.code === 200 || data.code === 0) {
          this.aiAnalysisResult = data.data || '未获取到分析结果'
        } else {
          throw new Error(data.message || data.msg || 'AI分析失败')
        }
      } catch (e) {
        console.error('AI analysis error:', e)
        this.$message.error(e.message || 'AI分析失败')
      } finally {
        this.aiLoading = false
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
    
    disposeCharts() {
      if (this.heatmapChart) this.heatmapChart.dispose()
      if (this.liquidChart) this.liquidChart.destroy()
      if (this.gaugeChart) this.gaugeChart.destroy()
      if (this.roseChart) this.roseChart.destroy()
      if (this.correlationChart) this.correlationChart.dispose()
    },
    
    renderCharts() {
      this.renderG2Charts()
      this.renderHeatmap()
      this.renderCorrelationChart()
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
          height: 140,
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
    
    renderHeatmap() {
      if (!this.$refs.heatmapRef) return
      if (this.heatmapChart) {
        this.heatmapChart.dispose()
      }
      
      if (!this.heatmapData || this.heatmapData.length === 0) {
        console.log('No heatmap data available')
        return
      }
      
      this.heatmapChart = echarts.init(this.$refs.heatmapRef)
      
      const xData = this.heatmapData.map(item => this.formatBucket(item.range))
      const yData = this.heatmapData.map(item => item.count)
      
      console.log('Heatmap data:', { xData, yData, raw: this.heatmapData })
      
      this.heatmapChart.setOption({
        tooltip: { trigger: 'axis' },
        grid: { left: '3%', right: '4%', bottom: '8%', containLabel: true },
        xAxis: { 
          type: 'category', 
          data: xData, 
          boundaryGap: false,
          axisLabel: { 
            rotate: 45,
            fontSize: 11
          },
          axisLine: { lineStyle: { color: '#cbd5e1' } }
        },
        yAxis: { 
          type: 'value',
          name: '观看次数',
          axisLine: { lineStyle: { color: '#cbd5e1' } }
        },
        series: [{
          name: '观看热度',
          type: 'line',
          data: yData,
          smooth: true,
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(88, 106, 255, 0.3)' },
              { offset: 1, color: 'rgba(88, 106, 255, 0.05)' }
            ])
          },
          lineStyle: { color: '#586aff', width: 2 },
          itemStyle: { color: '#586aff' }
        }]
      })
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
    
    renderCorrelationChart() {
      if (!this.$refs.correlationRef) return
      if (this.correlationChart) {
        this.correlationChart.dispose()
      }
      
      if (!this.correlationData.samples || this.correlationData.samples.length === 0) {
        console.log('No correlation data available')
        return
      }
      
      this.correlationChart = echarts.init(this.$refs.correlationRef)
      
      const data = this.correlationData.samples.map(s => [s.avgCompletionRate, s.avgScore])
      
      console.log('Correlation data:', { samples: this.correlationData.samples, plotData: data })
      
      this.correlationChart.setOption({
        title: { text: '完课率 vs 成绩', left: 'center' },
        tooltip: { trigger: 'item', formatter: params => `完课率: ${params.data[0]}%<br/>成绩: ${params.data[1]}` },
        grid: { left: '15%', right: '5%', top: '15%', bottom: '15%' },
        xAxis: { name: '完课率(%)', type: 'value', min: 0, max: 100 },
        yAxis: { name: '成绩', type: 'value', min: 0, max: 100 },
        series: [{
          symbolSize: 10,
          data: data,
          type: 'scatter',
          itemStyle: { color: '#8b5cf6' }
        }]
      })
    }
  }
}
</script>

<style scoped>
.video-analysis-container {
  padding: 0;
  background: transparent;
  min-height: calc(100vh - 200px);
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

.ai-result-preview {
  margin-top: 16px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 12px;
  max-height: 400px;
  overflow-y: auto;
}

.ai-content {
  line-height: 1.8;
  color: #334155;
  font-size: 14px;
}

.ai-content h4 {
  margin: 16px 0 8px 0;
  font-size: 15px;
  color: #1e293b;
  font-weight: 600;
}

.ai-content strong {
  color: #3b82f6;
  font-weight: 600;
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
