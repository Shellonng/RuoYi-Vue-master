<template>
  <el-dialog
    :title="assignmentId ? '批量批改（当前作业）' : '批量批改'"
    :visible.sync="dialogVisible"
    width="90%"
    top="5vh"
    :close-on-click-modal="false"
    append-to-body
    :modal-append-to-body="true"
    :lock-scroll="true"
    custom-class="batch-grading-modal"
    @close="handleClose"
  >
    <div class="batch-grading-container">
      <div class="batch-desc">选择提交并触发 AI 批改；右侧可查看处理中/失败任务</div>
      
      <div class="batch-columns">
        <!-- 左侧：全部作业提交 -->
        <div class="batch-left">
          <div class="section-title">全部作业提交</div>
          <div class="table-wrap">
            <el-table
              :data="allSubmissions"
              border
              stripe
              height="400"
              @selection-change="handleSelectionChange"
            >
              <el-table-column type="selection" width="50" />
              <el-table-column prop="studentId" label="学生ID" width="120" />
              <el-table-column prop="studentName" label="学生名" width="120" />
              <el-table-column prop="assignmentName" label="作业名" show-overflow-tooltip />
            </el-table>
          </div>
          <div class="batch-actions">
            <div class="actions-left">
              <span class="muted">已选 <span class="em-num">{{ selectedSubmissions.length }}</span> 项</span>
            </div>
            <div class="actions-right">
              <el-button type="primary" :disabled="selectedSubmissions.length === 0" @click="batchTrigger">批量批改</el-button>
              <el-button @click="refreshData">刷新</el-button>
            </div>
          </div>
        </div>

        <!-- 右侧：处理中/失败 -->
        <div class="batch-right">
          <div class="section-title">处理中/失败（AI 批改中）</div>
          <div class="table-wrap">
            <el-table
              :data="unresolvedList"
              border
              stripe
              height="400"
            >
              <el-table-column prop="studentId" label="学生ID" width="120" />
              <el-table-column prop="studentName" label="学生名" width="120" />
              <el-table-column prop="assignmentName" label="作业名" show-overflow-tooltip />
              <el-table-column label="状态" width="100">
                <template slot-scope="scope">
                  <el-tag :type="getStatusType(scope.row.status)" size="small">
                    {{ getStatusText(scope.row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100">
                <template slot-scope="scope">
                  <el-button
                    v-if="scope.row.status === 'failed'"
                    size="mini"
                    type="danger"
                    @click="deleteLatestGrading(scope.row.id)"
                  >
                    确认
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script>
export default {
  name: 'BatchGradingModal',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    assignmentId: {
      type: Number,
      default: null
    },
    submissionList: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      dialogVisible: false,
      allSubmissions: [],
      unresolvedList: [],
      selectedSubmissions: [],
      llmModel: 'Qwen/Qwen3-8B',
      userNameMap: {}
    }
  },
  watch: {
    visible(val) {
      this.dialogVisible = val
      if (val) {
        this.loadData()
      }
    }
  },
  methods: {
    handleClose() {
      this.$emit('update:visible', false)
      this.$emit('close')
    },
    
    handleSelectionChange(selection) {
      this.selectedSubmissions = selection
    },
    
    getStatusText(status) {
      if (status === 'failed') return '失败'
      if (status === 'processing') return '处理中'
      return status || '处理中'
    },
    
    getStatusType(status) {
      if (status === 'failed') return 'danger'
      return 'warning'
    },
    
    async loadData() {
      await Promise.all([
        this.loadAllSubmissions(),
        this.loadUnresolved()
      ])
    },
    
    async loadAllSubmissions() {
      try {
        // 使用传入的submissionList而不是调用API
        if (this.submissionList && this.submissionList.length > 0) {
          this.allSubmissions = this.submissionList.map(item => ({
            id: Number(item.id || 0),
            studentId: String(item.studentUserId || item.studentUsername || item.studentId || ''),
            studentName: String(item.studentName || item.studentUsername || ''),
            assignmentName: this.assignmentId ? (item.title || item.fileName || '当前作业') : String(item.fileName || '')
          }))
        } else {
          // 如果没有传入submissionList，则调用API（兜底方案）
          const baseURL = process.env.NODE_ENV === 'production' ? 'http://localhost:8083' : '/smart-api'
          let url = `${baseURL}/api/ai-grading/submissions?page=1&pageSize=100`
          if (this.assignmentId) {
            url += `&assignmentId=${this.assignmentId}`
          }
          const response = await fetch(url)
          const json = await response.json()
          const data = Array.isArray(json?.data) ? json.data : []
          
          this.allSubmissions = data.map(item => {
            const sub = item.submission || {}
            return {
              id: Number(sub.id || 0),
              studentId: String(sub.studentUserId || ''),
              studentName: String(item.realName || ''),
              assignmentName: String(sub.fileName || '')
            }
          })
        }
      } catch (error) {
        console.error('[批量批改] 加载全部提交失败:', error)
        this.$message.error('加载全部提交失败')
      }
    },
    
    async loadUnresolved() {
      try {
        const baseURL = process.env.NODE_ENV === 'production' ? 'http://localhost:8083' : '/smart-api'
        const response = await fetch(`${baseURL}/api/ai-grading/unresolved`)
        const json = await response.json()
        const data = Array.isArray(json?.data) ? json.data : []
        
        this.unresolvedList = data.map(item => {
          const sub = item.submission || {}
          const g = item.grading || {}
          const studentId = String(sub.studentUserId || '')
          
          return {
            id: Number(sub.id || 0),
            studentId: studentId,
            studentName: String(
              item.realName ||
              item.user?.realName ||
              item.student?.realName ||
              item.student?.user?.realName ||
              this.userNameMap[studentId] ||
              ''
            ),
            assignmentName: String(sub.fileName || ''),
            status: String(g.status || 'processing')
          }
        })
        
        await this.fillUserNames()
      } catch (error) {
        console.error('[批量批改] 加载处理中/失败列表失败:', error)
        this.$message.error('加载处理中/失败列表失败')
      }
    },
    
    async refreshData() {
      try {
        const baseURL = process.env.NODE_ENV === 'production' ? 'http://localhost:8083' : '/smart-api'
        const response = await fetch(`${baseURL}/api/ai-grading/pending`)
        await response.json()
        
        this.$message.success('已刷新待批改列表')
        await this.loadData()
      } catch (error) {
        console.error('[批量批改] 刷新失败:', error)
        this.$message.error('刷新失败')
      }
    },
    
    async batchTrigger() {
      if (this.selectedSubmissions.length === 0) {
        this.$message.warning('请先选择要批改的提交')
        return
      }
      
      try {
        const submissionIds = this.selectedSubmissions.map(s => s.id)
        const baseURL = process.env.NODE_ENV === 'production' ? 'http://localhost:8083' : '/smart-api'
        
        const response = await fetch(`${baseURL}/api/ai-grading/grading/batch`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            submissionIds: submissionIds,
            llmModel: this.llmModel
          })
        })
        
        await response.json()
        this.$message.success('批量批改已触发')
        
        // 清空选择
        this.selectedSubmissions = []
        
        // 刷新数据
        await this.loadUnresolved()
        
        // 通知父组件
        this.$emit('graded')
      } catch (error) {
        console.error('[批量批改] 批量触发失败:', error)
        this.$message.error('批量触发失败')
      }
    },
    
    async deleteLatestGrading(submissionId) {
      try {
        const baseURL = process.env.NODE_ENV === 'production' ? 'http://localhost:8083' : '/smart-api'
        const response = await fetch(`${baseURL}/api/ai-grading/grading/${submissionId}/latest`, {
          method: 'DELETE'
        })
        
        const json = await response.json()
        
        if (json?.data) {
          this.$message.success('已删除最新批改记录')
          await this.loadUnresolved()
        } else {
          this.$message.info('没有可删除的记录')
        }
      } catch (error) {
        console.error('[批量批改] 删除失败:', error)
        this.$message.error('删除失败')
      }
    },
    
    async fillUserNames() {
      const missing = this.unresolvedList.filter(r => !r.studentName && r.studentId)
      
      await Promise.all(missing.map(async (r) => {
        try {
          const baseURL = process.env.NODE_ENV === 'production' ? 'http://localhost:8083' : '/smart-api'
          const response = await fetch(`${baseURL}/api/ai-grading/users/${encodeURIComponent(r.studentId)}`)
          const json = await response.json()
          const name = json?.data?.realName || ''
          
          if (name) {
            this.userNameMap[r.studentId] = name
            r.studentName = name
          }
        } catch (error) {
          // 忽略单个用户名获取失败
        }
      }))
    }
  }
}
</script>

<style>
/* 全局样式：确保批量评分对话框在最上层 */
.batch-grading-modal {
  z-index: 3000 !important;
}

.batch-grading-modal .el-dialog__wrapper {
  z-index: 3000 !important;
}
</style>

<style scoped>
.batch-grading-container {
  padding: 10px;
}

.batch-desc {
  font-size: 13px;
  color: #64748b;
  margin-bottom: 16px;
  padding: 8px 12px;
  background: #f8fafc;
  border-radius: 6px;
}

.batch-columns {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.batch-left,
.batch-right {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.section-title {
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 8px;
  border-left: 3px solid #409EFF;
  padding-left: 8px;
  font-size: 15px;
}

.table-wrap {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
}

.batch-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 0;
  border-top: 1px solid #edf2f7;
  margin-top: 12px;
}

.actions-left {
  color: #94a3b8;
  font-size: 13px;
}

.em-num {
  font-weight: 700;
  color: #409EFF;
  font-size: 16px;
}

.actions-right {
  display: flex;
  gap: 10px;
}

@media (max-width: 1200px) {
  .batch-columns {
    grid-template-columns: 1fr;
  }
}
</style>
