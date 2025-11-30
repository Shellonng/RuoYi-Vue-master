<template>
  <div class="grading-page">
    <div class="grading-page-header">
      <div class="header-leading">
        <nav class="breadcrumb">
          <a class="bc-link" @click="$emit('close')">作业列表</a>
          <span class="sep">›</span>
          <a class="bc-link" @click="$emit('close')">已提交作业</a>
          <span class="sep">›</span>
          <span class="bc-current">批改详情</span>
        </nav>
        <div class="title">批改详情</div>
      </div>
      <div class="header-actions">
        <el-button size="small" @click="$emit('close')">返回列表</el-button>
        <el-input v-model="teacherId" placeholder="教师ID" size="small" style="width: 120px" />
        <el-button size="small" type="primary" @click="triggerAiGrading">AI 批改</el-button>
        <el-button size="small" @click="toggleTeacherEdit">{{ teacherEditing ? '取消修改' : '教师修改' }}</el-button>
        <el-button size="small" type="success" @click="confirmTeacher">确认批改</el-button>
      </div>
    </div>
    
    <div class="grading-layout" v-loading="loading">
      <div class="main-pane">
        <!-- 批改结果 -->
        <div class="card grading-card module">
          <div class="card-title with-icon"><span class="icon">⭐</span><span>批改结果</span></div>
          <div class="score-cards">
            <div class="score-card">
              <div class="score-name">内容分</div>
              <div class="score-value" v-if="!teacherEditing">{{ edit.contentScore }}</div>
              <el-input-number v-else v-model="edit.contentScore" :min="0" :max="100" controls-position="right" size="small" style="width: 100px;" />
            </div>
            <div class="score-card">
              <div class="score-name">逻辑分</div>
              <div class="score-value" v-if="!teacherEditing">{{ edit.logicScore }}</div>
              <el-input-number v-else v-model="edit.logicScore" :min="0" :max="100" controls-position="right" size="small" style="width: 100px;" />
            </div>
            <div class="score-card">
              <div class="score-name">知识分</div>
              <div class="score-value" v-if="!teacherEditing">{{ edit.knowledgeScore }}</div>
              <el-input-number v-else v-model="edit.knowledgeScore" :min="0" :max="100" controls-position="right" size="small" style="width: 100px;" />
            </div>
            <div class="score-card">
              <div class="score-name">创新分</div>
              <div class="score-value" v-if="!teacherEditing">{{ edit.innovationScore }}</div>
              <el-input-number v-else v-model="edit.innovationScore" :min="0" :max="100" controls-position="right" size="small" style="width: 100px;" />
            </div>
            <div class="score-card is-total">
              <div class="score-name">总分</div>
              <div class="score-value" v-if="!teacherEditing">{{ edit.totalScore }}</div>
              <el-input-number v-else v-model="edit.totalScore" :min="0" :max="100" controls-position="right" size="small" style="width: 100px;" />
            </div>
          </div>
        </div>

        <!-- AI 评语 -->
        <div class="card module">
          <div class="card-title with-icon"><span class="icon">💬</span><span>AI 评语</span></div>
          <div class="module-body" :class="teacherEditing ? '' : 'highlight'">
            <el-input v-if="teacherEditing" v-model="edit.aiComment" type="textarea" :rows="4" />
            <div v-else>{{ edit.aiComment || '—' }}</div>
          </div>
        </div>

        <!-- 改进建议 -->
        <div class="card module">
          <div class="card-title with-icon"><span class="icon">✏️</span><span>改进建议</span></div>
          <div class="module-body">
            <template v-if="teacherEditing">
              <el-input v-model="edit.improvementSuggestions" type="textarea" :rows="6" />
            </template>
            <template v-else>
              <div v-if="improvementList.length > 0" class="improve-list">
                <div v-for="(it, idx) in improvementList" :key="idx" class="improve-item">
                  <div class="improve-line">
                    <span class="improve-tag">知识点</span>
                    <span class="improve-content">{{ it.kp }}</span>
                  </div>
                  <div class="improve-line">
                    <span class="improve-tag">建议</span>
                    <span class="improve-content">{{ it.suggestion }}</span>
                  </div>
                  <div class="improve-line" v-if="it.type">
                    <span class="improve-tag">{{ it.type }}</span>
                    <span class="improve-content">{{ it.detail }}</span>
                  </div>
                </div>
              </div>
              <div v-else>{{ edit.improvementSuggestions || '—' }}</div>
            </template>
          </div>
        </div>

        <!-- 覆盖知识点 -->
        <div class="card module">
          <div class="card-title with-icon"><span class="icon">📚</span><span>覆盖知识点</span></div>
          <div class="module-body" :class="teacherEditing ? '' : 'tags'">
            <template v-if="teacherEditing">
              <el-input v-model="edit.coveredKnowledgePoints" type="textarea" :rows="3" />
            </template>
            <template v-else>
              <span v-for="(t,i) in coveredTags" :key="i" class="tag" tabindex="0">{{ t }}</span>
              <span v-if="!coveredTags.length" class="muted">—</span>
            </template>
          </div>
        </div>

        <!-- 缺失知识点 -->
        <div class="card module">
          <div class="card-title with-icon"><span class="icon">📘</span><span>缺失知识点</span></div>
          <div class="module-body" :class="teacherEditing ? '' : 'tags'">
            <template v-if="teacherEditing">
              <el-input v-model="edit.missingKnowledgePoints" type="textarea" :rows="3" />
            </template>
            <template v-else>
              <span v-for="(t,i) in missingTags" :key="i" class="tag" tabindex="0">{{ t }}</span>
              <span v-if="!missingTags.length" class="muted">—</span>
            </template>
          </div>
        </div>
      </div>

      <div class="side-pane">
        <!-- 作业信息 -->
        <div class="card info-card">
          <div class="card-title">作业信息</div>
          <div class="info-grid">
            <div class="field">
              <div class="label">作业名</div>
              <div class="value">{{ assignmentTitle }}</div>
            </div>
            <div class="field">
              <div class="label">学生</div>
              <div class="value">{{ studentName }}</div>
            </div>
            <div class="field">
              <div class="label">提交时间</div>
              <div class="value">{{ submissionTime }}</div>
            </div>
            <div class="field">
              <div class="label">提交文件</div>
              <div class="value">
                <a v-if="submission.filePath" class="download-link" :href="fileUrl" target="_blank">
                  {{ submission.fileName || submission.filePath }}
                </a>
                <span v-else class="muted">暂无附件</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 关联知识点 -->
        <div class="card kp-card module">
          <div class="card-title">关联知识点</div>
          <ul class="kp-list">
            <li v-for="kp in knowledgePoints" :key="kp.id" :class="{ glow: kpGlow[kp.id] }" @click="glowKp(kp.id)" tabindex="0">
              {{ kp.title }}
            </li>
            <li v-if="!knowledgePoints || knowledgePoints.length===0" class="muted">暂无关联知识点</li>
          </ul>
        </div>
        
        <!-- 批改历史 -->
        <div class="card history-card module">
          <div class="card-title">{{ historyTitle }}</div>
          <div v-if="filteredHistory.length === 0" class="muted" style="padding: 12px 0;">暂无批改历史</div>
          <div v-else class="history-list">
            <div v-for="h in filteredHistory" :key="h.id" class="history-item">
              <div class="history-header" @click="toggleHistory(h.id)">
                <span>{{ formatHistoryTime(h.createdAt || h.updatedAt) }}</span>
                <i :class="historyOpen[h.id] ? 'el-icon-arrow-down' : 'el-icon-arrow-right'"></i>
              </div>
              <div v-if="historyOpen[h.id]" class="history-body">
                <div class="history-scores">
                  <div class="history-score-item">
                    <span class="label">总分:</span>
                    <span class="value">{{ h.totalScore }}</span>
                  </div>
                  <div class="history-score-item">
                    <span class="label">内容:</span>
                    <span class="value">{{ h.contentScore }}</span>
                  </div>
                  <div class="history-score-item">
                    <span class="label">逻辑:</span>
                    <span class="value">{{ h.logicScore }}</span>
                  </div>
                  <div class="history-score-item">
                    <span class="label">知识:</span>
                    <span class="value">{{ h.knowledgeScore }}</span>
                  </div>
                  <div class="history-score-item">
                    <span class="label">创新:</span>
                    <span class="value">{{ h.innovationScore }}</span>
                  </div>
                </div>
                <div class="history-field" v-if="h.aiComment">
                  <div class="label">AI评语:</div>
                  <div class="value">{{ h.aiComment }}</div>
                </div>
                <div class="history-field" v-if="h.coveredKnowledgePoints">
                  <div class="label">覆盖知识点:</div>
                  <div class="value">{{ h.coveredKnowledgePoints }}</div>
                </div>
                <div class="history-field" v-if="h.missingKnowledgePoints">
                  <div class="label">缺失知识点:</div>
                  <div class="value">{{ h.missingKnowledgePoints }}</div>
                </div>
                <div class="history-field" v-if="h.improvementSuggestions">
                  <div class="label">改进建议:</div>
                  <div class="value">{{ h.improvementSuggestions }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import smartRequest from '@/utils/smartRequest'

export default {
  name: 'AssignmentGradingDetail',
  props: {
    submissionId: {
      type: [Number, String],
      required: true
    }
  },
  data() {
    return {
      loading: false,
      teacherId: '',
      teacherEditing: false,
      submission: {},
      assignment: {},
      realName: '',
      gradingDetail: {},
      knowledgePoints: [],
      history: [],
      historyOpen: {},
      historyTitle: '批改历史',
      pollTimer: null,
      originalEdit: {
        contentScore: 0,
        logicScore: 0,
        knowledgeScore: 0,
        innovationScore: 0,
        totalScore: 0,
        aiComment: '',
        improvementSuggestions: '',
        coveredKnowledgePoints: '',
        missingKnowledgePoints: ''
      },
      edit: {
        contentScore: 0,
        logicScore: 0,
        knowledgeScore: 0,
        innovationScore: 0,
        totalScore: 0,
        aiComment: '',
        improvementSuggestions: '',
        coveredKnowledgePoints: '',
        missingKnowledgePoints: ''
      },
      improveGlow: {},
      kpGlow: {}
    }
  },
  computed: {
    assignmentTitle() {
      // 优先从assignment对象获取，然后从submission获取
      if (this.assignment && this.assignment.title) return this.assignment.title
      if (this.submission.assignmentTitle) return this.submission.assignmentTitle
      if (this.submission.assignment && this.submission.assignment.title) return this.submission.assignment.title
      if (this.submission.fileName) return this.submission.fileName
      return '-'
    },
    studentName() {
      // 优先使用realName，然后是其他字段
      if (this.realName) return this.realName
      if (this.submission.studentName) return this.submission.studentName
      if (this.submission.realName) return this.submission.realName
      if (this.submission.studentUsername) return this.submission.studentUsername
      if (this.submission.studentUserId) return this.submission.studentUserId
      return '-'
    },
    submissionTime() {
      if (!this.submission.submitTime) return '-'
      return this.parseDateTime(this.submission.submitTime)
    },
    fileUrl() {
      if (!this.submission.filePath) return ''
      return `http://localhost:8083${this.submission.filePath}`
    },
    improvementList() {
      try {
        const raw = this.edit.improvementSuggestions || ''
        if (!raw) return []
        
        // 尝试解析JSON格式
        try {
          const parsed = JSON.parse(raw)
          if (Array.isArray(parsed)) {
            return parsed.map(item => ({
              kp: String(item.knowledge_point || item.kp || item.knowledgePoint || ''),
              suggestion: String(item.suggestion || item.advice || ''),
              type: item.resources ? '资源' : (item.improvement ? '改进' : ''),
              detail: String(item.resources || item.improvement || '')
            })).filter(x => x.kp || x.suggestion)
          }
        } catch (e) {
          // 不是JSON，继续文本解析
        }
        
        // 文本格式解析
        const text = String(raw)
        const lines = text.split('\n').filter(l => l.trim())
        const result = []
        let current = null
        
        for (const line of lines) {
          const trimmed = line.trim()
          
          // 检测知识点行
          if (trimmed.includes('知识点') || trimmed.startsWith('"knowledge_point"')) {
            if (current && (current.kp || current.suggestion)) result.push(current)
            const kp = trimmed.split(/[:：]/).slice(1).join(':').trim() || 
                      trimmed.replace(/.*知识点[:：]?/, '').replace(/^["']|["']$/g, '').trim()
            current = { kp, suggestion: '', type: '', detail: '' }
          } 
          // 检测建议行
          else if ((trimmed.includes('建议') || trimmed.includes('suggestion')) && current) {
            const suggestion = trimmed.split(/[:：]/).slice(1).join(':').trim() || 
                              trimmed.replace(/.*建议[:：]?/, '').replace(/.*suggestion[:：]?/, '').replace(/^["']|["']$/g, '').trim()
            current.suggestion = suggestion
          } 
          // 检测改进/资源行
          else if ((trimmed.includes('改进') || trimmed.includes('资源') || trimmed.includes('resources')) && current) {
            if (trimmed.includes('资源') || trimmed.includes('resources')) {
              current.type = '资源'
            } else {
              current.type = '改进'
            }
            const detail = trimmed.split(/[:：]/).slice(1).join(':').trim() || 
                          trimmed.replace(/.*[改进资源][:：]?/, '').replace(/.*resources[:：]?/, '').replace(/^["']|["']$/g, '').trim()
            current.detail = detail
          }
        }
        
        if (current && (current.kp || current.suggestion)) result.push(current)
        return result
      } catch (e) {
        console.error('解析改进建议失败:', e)
        return []
      }
    },
    coveredTags() {
      try {
        const data = this.edit.coveredKnowledgePoints
        if (!data) return []
        if (Array.isArray(data)) return data
        if (typeof data === 'string') {
          const parsed = JSON.parse(data)
          return Array.isArray(parsed) ? parsed : []
        }
        return []
      } catch (e) {
        return []
      }
    },
    missingTags() {
      try {
        const data = this.edit.missingKnowledgePoints
        if (!data) return []
        if (Array.isArray(data)) return data
        if (typeof data === 'string') {
          const parsed = JSON.parse(data)
          return Array.isArray(parsed) ? parsed : []
        }
        return []
      } catch (e) {
        return []
      }
    },
    filteredHistory() {
      return this.history.filter(h => String(h.status) !== 'failed')
    }
  },
  mounted() {
    this.fetchDetail()
    this.loadHistory()
  },
  beforeDestroy() {
    if (this.pollTimer) {
      clearInterval(this.pollTimer)
      this.pollTimer = null
    }
  },
  methods: {
    async fetchDetail() {
      this.loading = true
      try {
        const res = await smartRequest({
          url: `/api/ai-grading/submissions/${this.submissionId}`,
          method: 'get'
        })
        
        if (res && res.data) {
          const d = res.data
          // 分别提取各个字段
          this.submission = d.submission || {}
          this.assignment = d.assignment || {}
          this.realName = d.realName || ''
          this.gradingDetail = d.grading || {}
          this.knowledgePoints = d.knowledgePoints || []
          
          console.log('[批改详情] 加载数据:', {
            submission: this.submission,
            assignment: this.assignment,
            realName: this.realName,
            assignmentTitle: this.assignmentTitle,
            studentName: this.studentName
          })
          
          // 填充编辑数据 - 使用最新的grading数据
          const g = this.gradingDetail
          this.edit = {
            contentScore: g.contentScore || 0,
            logicScore: g.logicScore || 0,
            knowledgeScore: g.knowledgeScore || 0,
            innovationScore: g.innovationScore || 0,
            totalScore: g.totalScore || 0,
            aiComment: g.aiComment || '',
            improvementSuggestions: g.improvementSuggestions || '',
            coveredKnowledgePoints: g.coveredKnowledgePoints || '',
            missingKnowledgePoints: g.missingKnowledgePoints || ''
          }
          
          // 保存原始数据快照
          this.snapshotEdit()
        }
      } catch (error) {
        console.error('[批改详情] 加载失败:', error)
        this.$message.error('加载批改详情失败')
      } finally {
        this.loading = false
      }
    },
    
    async loadHistory() {
      try {
        const res = await smartRequest({
          url: `/api/ai-grading/grading/${this.submissionId}/history`,
          method: 'get'
        })
        
        if (res && res.data && Array.isArray(res.data)) {
          this.history = res.data
          // 初始化展开状态（默认都折叠）
          this.history.forEach(h => {
            this.$set(this.historyOpen, h.id, false)
          })
          // 应用最新有效的批改结果
          this.applyLatestValid()
        }
      } catch (error) {
        console.error('[批改历史] 加载失败:', error)
      }
    },
    
    applyLatestValid() {
      // 找到最新的已完成批改记录
      const latestCompleted = [...this.history]
        .filter(h => String(h.status) === 'completed')
        .slice(-1)[0]
      
      const g = latestCompleted || this.gradingDetail
      if (g && g.id) {
        this.edit.totalScore = g.totalScore ?? 0
        this.edit.contentScore = g.contentScore ?? 0
        this.edit.logicScore = g.logicScore ?? 0
        this.edit.knowledgeScore = g.knowledgeScore ?? 0
        this.edit.innovationScore = g.innovationScore ?? 0
        this.edit.aiComment = g.aiComment ?? ''
        this.edit.improvementSuggestions = g.improvementSuggestions ?? ''
        this.edit.coveredKnowledgePoints = g.coveredKnowledgePoints ?? ''
        this.edit.missingKnowledgePoints = g.missingKnowledgePoints ?? ''
      }
    },
    
    snapshotEdit() {
      this.originalEdit = {
        contentScore: this.edit.contentScore,
        logicScore: this.edit.logicScore,
        knowledgeScore: this.edit.knowledgeScore,
        innovationScore: this.edit.innovationScore,
        totalScore: this.edit.totalScore,
        aiComment: this.edit.aiComment,
        improvementSuggestions: this.edit.improvementSuggestions,
        coveredKnowledgePoints: this.edit.coveredKnowledgePoints,
        missingKnowledgePoints: this.edit.missingKnowledgePoints
      }
    },
    
    restoreEdit() {
      this.edit = {
        contentScore: this.originalEdit.contentScore,
        logicScore: this.originalEdit.logicScore,
        knowledgeScore: this.originalEdit.knowledgeScore,
        innovationScore: this.originalEdit.innovationScore,
        totalScore: this.originalEdit.totalScore,
        aiComment: this.originalEdit.aiComment,
        improvementSuggestions: this.originalEdit.improvementSuggestions,
        coveredKnowledgePoints: this.originalEdit.coveredKnowledgePoints,
        missingKnowledgePoints: this.originalEdit.missingKnowledgePoints
      }
    },
    
    async triggerAiGrading() {
      if (!this.submissionId) return
      
      try {
        this.$message.info('正在触发AI批改...')
        
        const res = await smartRequest({
          url: '/api/ai-grading/grading/trigger',
          method: 'post',
          data: {
            submissionId: this.submissionId,
            llmModel: 'Qwen/Qwen3-8B'
          }
        })
        
        this.$message.success('已触发 AI 批改')
        this.historyTitle = '批改历史（批改中...）'
        
        // 开始轮询检查批改状态
        if (this.pollTimer) {
          clearInterval(this.pollTimer)
          this.pollTimer = null
        }
        
        this.pollTimer = setInterval(async () => {
          try {
            const statusRes = await smartRequest({
              url: `/api/ai-grading/submissions/${this.submissionId}`,
              method: 'get'
            })
            
            if (!statusRes || !statusRes.data) {
              clearInterval(this.pollTimer)
              this.pollTimer = null
              this.historyTitle = '批改历史（失败）'
              this.$message.error('获取批改状态失败')
              await this.fetchDetail()
              await this.loadHistory()
              return
            }
            
            const d = statusRes.data
            const status = String(d?.grading?.status || '')
            const errorMsg = String(d?.grading?.errorMessage || '')
            
            if (status === 'completed' || status === 'failed' || errorMsg) {
              clearInterval(this.pollTimer)
              this.pollTimer = null
              
              if (status === 'completed') {
                this.$message.success('AI批改完成')
                this.historyTitle = '批改历史'
              } else {
                this.$message.error(errorMsg || 'AI批改失败')
                this.historyTitle = '批改历史（失败）'
              }
              
              await this.fetchDetail()
              await this.loadHistory()
            }
          } catch (error) {
            clearInterval(this.pollTimer)
            this.pollTimer = null
            this.historyTitle = '批改历史（错误）'
            console.error('[轮询批改状态] 错误:', error)
            this.$message.error('网络或服务器错误')
          }
        }, 2000)
        
      } catch (error) {
        console.error('[AI批改] 失败:', error)
        this.$message.error('触发AI批改失败')
      }
    },
    
    toggleTeacherEdit() {
      if (!this.teacherEditing) {
        // 开启编辑模式，保存快照
        this.snapshotEdit()
        this.teacherEditing = true
      } else {
        // 取消编辑，恢复快照
        this.restoreEdit()
        this.teacherEditing = false
      }
    },
    
    async confirmTeacher() {
      if (!this.submissionId) return
      
      this.teacherEditing = false
      const tid = String(this.teacherId || '')
      
      try {
        // 使用代理路径（开发环境）或直接URL（生产环境）
        const baseURL = process.env.NODE_ENV === 'production' ? 'http://localhost:8083' : '/smart-api'
        const url = `${baseURL}/api/ai-grading/grading/${this.submissionId}/confirm${tid ? '?teacherId=' + encodeURIComponent(tid) : ''}`
        
        const response = await fetch(url, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            contentScore: this.edit.contentScore,
            logicScore: this.edit.logicScore,
            knowledgeScore: this.edit.knowledgeScore,
            innovationScore: this.edit.innovationScore,
            totalScore: this.edit.totalScore,
            aiComment: this.edit.aiComment,
            improvementSuggestions: this.edit.improvementSuggestions,
            coveredKnowledgePoints: this.edit.coveredKnowledgePoints,
            missingKnowledgePoints: this.edit.missingKnowledgePoints
          })
        })
        
        const result = await response.json()
        
        if (!response.ok) {
          throw new Error(result.message || '确认批改失败')
        }
        
        this.$message.success('已确认批改')
        await this.fetchDetail()
        await this.loadHistory()
      } catch (error) {
        console.error('[确认批改] 失败:', error)
        this.$message.error(error.message || '确认批改失败')
      }
    },
    
    flashImprove(idx) {
      this.$set(this.improveGlow, idx, true)
      setTimeout(() => {
        this.$set(this.improveGlow, idx, false)
      }, 1000)
    },
    
    glowKp(id) {
      this.$set(this.kpGlow, id, true)
      setTimeout(() => {
        this.$set(this.kpGlow, id, false)
      }, 1000)
    },
    
    parseDateTime(value) {
      if (!value) return '-'
      try {
        const date = new Date(value)
        return date.toLocaleString('zh-CN')
      } catch (e) {
        return String(value)
      }
    },
    
    toggleHistory(id) {
      this.$set(this.historyOpen, id, !this.historyOpen[id])
    },
    
    formatHistoryTime(value) {
      if (!value) return '-'
      try {
        const date = new Date(value)
        return date.toLocaleString('zh-CN', {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit'
        })
      } catch (e) {
        return String(value)
      }
    }
  }
}
</script>

<style scoped>
.grading-page {
  background: #f0f2f5;
  min-height: 100vh;
  padding: 0;
}

.grading-page-header {
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
  padding: 14px 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.header-leading {
  flex: 1;
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  margin-bottom: 8px;
}

.bc-link {
  color: #409EFF;
  cursor: pointer;
  text-decoration: none;
}

.bc-link:hover {
  text-decoration: underline;
}

.sep {
  color: #909399;
}

.bc-current {
  color: #303133;
  font-weight: 500;
}

.title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.grading-layout {
  display: grid;
  grid-template-columns: 1fr 340px;
  gap: 20px;
  padding: 20px 24px;
  max-width: 1800px;
  margin: 0 auto;
}

.main-pane, .side-pane {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.card {
  background: #fff;
  border-radius: 8px;
  border: 1px solid #ebeef5;
  padding: 18px 20px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 14px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.card-title.with-icon .icon {
  font-size: 16px;
}

.grading-card .score-cards {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
}

.score-card {
  text-align: center;
  padding: 20px 12px;
  background: #fafbfc;
  border-radius: 6px;
  border: 1px solid #ebeef5;
}

.score-card.is-total {
  background: linear-gradient(135deg, #4dd0e1 0%, #26c6da 100%);
  color: #fff;
  border: none;
}

.score-name {
  font-size: 14px;
  color: #909399;
  margin-bottom: 10px;
  font-weight: 400;
}

.score-card.is-total .score-name {
  color: rgba(255, 255, 255, 0.9);
}

.score-value {
  font-size: 32px;
  font-weight: 700;
  color: #303133;
}

.score-card.is-total .score-value {
  color: #fff;
}

.module-body {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
}

.module-body.highlight {
  background: #fafbfc;
  padding: 16px;
  border-radius: 6px;
  border: 1px solid #ebeef5;
  color: #606266;
  line-height: 1.8;
}

.module-body.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag {
  display: inline-block;
  padding: 6px 12px;
  background: #ecf5ff;
  color: #409EFF;
  border-radius: 4px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.3s;
}

.tag:hover {
  background: #409EFF;
  color: #fff;
}

.improve-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.improve-item {
  padding: 0;
  background: transparent;
  border: none;
}

.improve-line {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin-bottom: 8px;
  line-height: 1.8;
}

.improve-line:last-child {
  margin-bottom: 0;
}

.improve-tag {
  display: inline-block;
  padding: 2px 10px;
  background: #f0f0f0;
  color: #666;
  border-radius: 3px;
  font-size: 12px;
  white-space: nowrap;
  min-width: 48px;
  text-align: center;
}

.improve-content {
  flex: 1;
  color: #303133;
  font-size: 14px;
  line-height: 1.8;
}

.info-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.field .label {
  font-size: 13px;
  color: #909399;
  font-weight: 500;
}

.field .value {
  font-size: 14px;
  color: #303133;
}

.download-link {
  color: #409EFF;
  text-decoration: none;
}

.download-link:hover {
  text-decoration: underline;
}

.kp-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.kp-list li {
  padding: 10px 14px;
  background: #fafbfc;
  border-radius: 4px;
  border: 1px solid #ebeef5;
  font-size: 14px;
  color: #606266;
  cursor: pointer;
  transition: all 0.2s;
}

.kp-list li:hover {
  background: #f0f9ff;
  border-color: #b3d8ff;
  color: #409EFF;
}

.muted {
  color: #909399;
  font-style: italic;
}

/* 批改历史样式 */
.history-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.history-item {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  overflow: hidden;
}

.history-header {
  padding: 10px 14px;
  background: #fafbfc;
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  color: #606266;
  transition: all 0.2s;
}

.history-header:hover {
  background: #f0f2f5;
}

.history-header i {
  font-size: 12px;
  transition: transform 0.3s;
}

.history-body {
  padding: 12px 14px;
  background: #fff;
  border-top: 1px solid #ebeef5;
}

.history-scores {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.history-score-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
}

.history-score-item .label {
  color: #909399;
}

.history-score-item .value {
  color: #303133;
  font-weight: 600;
}

.history-field {
  margin-bottom: 10px;
}

.history-field:last-child {
  margin-bottom: 0;
}

.history-field .label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.history-field .value {
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
  word-break: break-word;
}

@media (max-width: 1400px) {
  .grading-layout {
    grid-template-columns: 1fr 320px;
  }
}

@media (max-width: 1200px) {
  .grading-layout {
    grid-template-columns: 1fr;
  }
  
  .side-pane {
    order: -1;
  }
}
</style>
