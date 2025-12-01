<template>
  <div class="assignment-overview">
    <!-- 热力图和环形图 -->
    <div class="charts-area">
      <!-- 作业-学生热力图 -->
      <div class="heatmap-card">
        <div class="chart-header">作业-学生热力图</div>
        <div class="hm-scroll">
          <div class="hm-body-wrapper">
            <div class="hm-body">
              <div v-for="a in assignmentsList" :key="'a-'+a.id" class="hm-row">
                <div class="hm-assignment" :title="a.title">{{ a.title }}</div>
                <div class="hm-cells" :style="hmCellsStyle">
                  <div 
                    v-for="s in studentsList" 
                    :key="s+'|'+a.id" 
                    :class="['hm-cell', cellClass(statusOf(s, a.title, a.id)), selectedStudent===s ? 'is-selected' : '']" 
                    :title="cellTitle(s, a.title, a.id)"
                  ></div>
                </div>
              </div>
            </div>
          </div>
          <div class="hm-bottom">
            <div class="hm-bottom-spacer"></div>
            <div class="hm-students" :style="hmStudentsStyle">
              <div v-for="s in studentsList" :key="'s-bottom-'+s" class="hm-student">
                <span class="hm-student-label clickable" @click="onStudentClick(s)">{{ s }}</span>
              </div>
            </div>
          </div>
        </div>
        <div class="hm-legend">
          <span class="legend-item"><span class="legend-swatch hm-none"></span>未提交</span>
          <span class="legend-item"><span class="legend-swatch hm-pending"></span>已提交未批改</span>
          <span class="legend-item"><span class="legend-swatch hm-graded-1"></span>0-30</span>
          <span class="legend-item"><span class="legend-swatch hm-graded-2"></span>31-60</span>
          <span class="legend-item"><span class="legend-swatch hm-graded-3"></span>61-80</span>
          <span class="legend-item"><span class="legend-swatch hm-graded-4"></span>81-100</span>
        </div>
      </div>

      <!-- 提交状态环形概览 -->
      <div class="donut-card" style="position:relative;overflow:hidden">
        <div class="chart-header">提交状态环形概览</div>
        <div class="donut-content">
          <div class="donut-wrap">
            <svg class="donut" viewBox="0 0 120 120">
              <circle class="donut-bg" cx="60" cy="60" r="50" stroke-width="18" fill="none" />
              <circle 
                v-for="(seg, idx) in donutSegments" 
                :key="idx" 
                class="donut-seg" 
                cx="60" 
                cy="60" 
                r="50" 
                stroke-width="18" 
                fill="none"
                :stroke="seg.color" 
                :stroke-dasharray="seg.dash" 
                :stroke-dashoffset="seg.offset" 
              />
            </svg>
            <div class="donut-center">
              <div class="donut-total">{{ donutTotal }}</div>
              <div class="donut-label">总单元</div>
            </div>
          </div>
          <div class="donut-legend">
            <div class="dl-item">
              <span class="dl-swatch dl-none"></span>
              <span>未提交</span>
              <span class="dl-num">{{ donutNone }}</span>
            </div>
            <div class="dl-item">
              <span class="dl-swatch dl-pending"></span>
              <span>已提交未批改</span>
              <span class="dl-num">{{ donutPending }}</span>
            </div>
            <div class="dl-item">
              <span class="dl-swatch dl-graded"></span>
              <span>已提交已批改</span>
              <span class="dl-num">{{ donutGraded }}</span>
            </div>
          </div>
        </div>
        
        <!-- 学生详情浮层 -->
        <transition name="overlay-slide">
          <div v-if="selectedStudent" class="student-overlay" :style="overlayStyle">
            <div class="overlay-content">
              <button class="overlay-close" @click="closeOverlay">×</button>
              <div class="overlay-deco"></div>
              <div class="overlay-title">
                <span class="overlay-title-icon">🧑‍🎓</span>
                {{ selectedStudent }} 学生作业提交详情
              </div>
              <div class="overlay-list">
                <div
                  v-for="(r, idx) in selectedStudentList"
                  :key="r.submissionId"
                  :class="['ov-card', r.status === '2' ? 'row-graded' : 'row-pending']"
                  :style="{ animationDelay: (idx * 40) + 'ms' }"
                >
                  <div class="ov-head">
                    <div class="ov-title">{{ r.assignmentTitle }}</div>
                    <div class="ov-score-badge" :class="scoreClass(r.score)">
                      {{ r.score !== null && r.score !== undefined ? r.score : '-' }}
                    </div>
                  </div>
                  <div class="ov-body">
                    <div class="ov-file">
                      <span :class="['file-icon', fileIcon(r.fileName)]"></span>
                      <span class="file-name" :title="r.fileName">{{ r.fileName }}</span>
                    </div>
                    <div class="ov-status">
                      <span :class="['st-badge', r.status === '2' ? 'st-graded' : 'st-pending']">
                        <span class="st-icon">{{ r.status === '2' ? '✓' : '✎' }}</span>
                        {{ statusLabel(r.status) }}
                      </span>
                    </div>
                  </div>
                </div>
                <div v-if="selectedStudentList.length === 0" class="ov-empty">暂无提交记录</div>
              </div>
            </div>
          </div>
        </transition>
      </div>
    </div>
  </div>
</template>

<script>
import smartRequest from '@/utils/smartRequest'
import request from '@/utils/request'

export default {
  name: 'AssignmentOverview',
  props: {
    courseId: {
      type: [Number, String],
      required: false,
      default: null
    }
  },
  data() {
    return {
      submissions: [],
      overviewAssignments: [],
      courseStudents: [], // 课程学生列表
      selectedStudent: null,
      overlayX: 100,
      overlayOpacity: 0,
      CELL: 28
    }
  },
  computed: {
    /** 热力图行数据 */
    hmRows() {
      return this.submissions.map((item, idx) => {
        const sub = item.submission || {}
        const realName = item.realName || ''
        const assignmentIdRaw = sub.assignmentId ?? sub.assignmentID ?? sub.assignment_id ?? sub.assignment
        const assignmentIdNum = Number(assignmentIdRaw ?? 0)
        return {
          seq: idx + 1,
          submissionId: Number(sub.id ?? 0),
          assignmentId: Number.isFinite(assignmentIdNum) ? assignmentIdNum : 0,
          studentId: String(sub.studentUserId ?? ''),
          studentName: realName,
          assignmentName: String(sub.fileName ?? ''),
          score: sub.score ?? null,
          status: String(sub.status ?? '')
        }
      })
    },
    
    /** 学生列表 */
    studentsList() {
      console.log('[热力图] studentsList 计算:', {
        courseId: this.courseId,
        courseStudentsCount: this.courseStudents.length,
        courseStudents: this.courseStudents
      })
      
      // 如果有课程学生数据（无论是否有courseId），都显示所有课程学生（包括没提交的）
      if (this.courseStudents.length > 0) {
        // 获取有提交记录的学生ID集合
        const submittedStudentIds = new Set()
        const currentAssignmentIds = new Set(this.assignmentsList.map(a => a.id))
        this.hmRows.forEach(r => {
          if (currentAssignmentIds.has(r.assignmentId)) {
            submittedStudentIds.add(String(r.studentId))
          }
        })
        
        // 将学生分为两组：有提交记录的和没有提交记录的
        const studentsWithSubmissions = []
        const studentsWithoutSubmissions = []
        
        this.courseStudents.forEach(s => {
          const studentId = String(s.studentUserId || s.id)
          const studentName = String(s.studentName || s.name || studentId)
          
          if (submittedStudentIds.has(studentId)) {
            studentsWithSubmissions.push(studentName)
          } else {
            studentsWithoutSubmissions.push(studentName)
          }
        })
        
        // 合并：有提交记录的在左，没有提交记录的在右
        const students = [...studentsWithSubmissions, ...studentsWithoutSubmissions]
        console.log('[热力图] 使用课程学生列表（有提交:', studentsWithSubmissions.length, ', 无提交:', studentsWithoutSubmissions.length, '）:', students)
        return students
      }
      
      // 否则只显示有提交记录的学生
      const set = new Set()
      const currentAssignmentIds = new Set(this.assignmentsList.map(a => a.id))
      this.hmRows.forEach(r => {
        if (currentAssignmentIds.has(r.assignmentId)) {
          set.add(String(r.studentName || r.studentId))
        }
      })
      const students = Array.from(set)
      console.log('[热力图] 使用提交记录学生列表:', students)
      return students
    },
    
    /** 作业列表 */
    assignmentsList() {
      const selectedCourseId = Number(this.courseId || 0)
      return this.overviewAssignments
        .filter(a => {
          const cId = Number(a?.courseId ?? 0)
          return !selectedCourseId || cId === selectedCourseId
        })
        .map(a => ({ id: Number(a?.id ?? 0), title: String(a?.title ?? '') }))
        .filter(a => a.title)
    },
    
    /** 按学生分组的提交记录 */
    submissionsByStudent() {
      const map = {}
      this.hmRows.forEach(r => {
        const k = String(r.studentName || r.studentId)
        if (!map[k]) map[k] = []
        map[k].push(r)
      })
      return map
    },
    
    /** 作业ID到标题的映射 */
    assignmentTitleMap() {
      const m = {}
      this.overviewAssignments.forEach(a => {
        const id = Number(a?.id ?? 0)
        if (Number.isFinite(id)) m[id] = String(a?.title || '')
      })
      return m
    },
    
    /** 选中学生的提交列表 */
    selectedStudentList() {
      const name = this.selectedStudent
      if (!name) return []
      const list = this.submissionsByStudent[name] || []
      return list
        .filter(r => r.status === '1' || r.status === '2')
        .map(r => ({
          submissionId: r.submissionId,
          assignmentTitle: r.assignmentId ? (this.assignmentTitleMap[Number(r.assignmentId)] || r.assignmentName) : r.assignmentName,
          fileName: r.assignmentName,
          score: r.score,
          status: r.status
        }))
    },
    
    /** 热力图样式 */
    hmStudentsStyle() {
      return { gridTemplateColumns: `repeat(${this.studentsList.length}, ${this.CELL}px)` }
    },
    hmCellsStyle() {
      return { gridTemplateColumns: `repeat(${this.studentsList.length}, ${this.CELL}px)` }
    },
    
    /** 环形图统计 */
    donutNone() {
      let n = 0
      this.assignmentsList.forEach(a => 
        this.studentsList.forEach(s => { 
          if (this.statusOf(s, a.title, a.id) === 'none') n++ 
        })
      )
      return n
    },
    donutPending() {
      let n = 0
      this.assignmentsList.forEach(a => 
        this.studentsList.forEach(s => { 
          if (this.statusOf(s, a.title, a.id) === 'pending') n++ 
        })
      )
      return n
    },
    donutGraded() {
      let n = 0
      this.assignmentsList.forEach(a => 
        this.studentsList.forEach(s => { 
          if (this.statusOf(s, a.title, a.id).startsWith('graded')) n++ 
        })
      )
      return n
    },
    donutTotal() {
      return this.studentsList.length * this.assignmentsList.length
    },
    
    /** 环形图分段 */
    donutSegments() {
      const total = this.donutTotal || 1
      const C = 2 * Math.PI * 50
      const parts = [
        { value: this.donutNone, color: '#9aa3b2' },
        { value: this.donutPending, color: '#ffd166' },
        { value: this.donutGraded, color: '#06d6a0' }
      ]
      const lengths = parts.map(p => C * (p.value / total))
      const used = lengths.slice(0, -1).reduce((a, b) => a + b, 0)
      lengths[lengths.length - 1] = Math.max(0, C - used)
      let offset = 0
      return lengths.map((len, i) => {
        const color = (parts[i] && parts[i].color) ? parts[i].color : '#cccccc'
        const seg = { color, dash: `${len} ${C - len}`, offset }
        offset += len
        return seg
      })
    },
    
    /** 浮层样式 */
    overlayStyle() {
      return {
        position: 'absolute',
        left: '0',
        right: '0',
        top: '0',
        bottom: '0',
        zIndex: 999,
        background: '#ffffff',
        transform: `translateX(${this.overlayX}%)`,
        opacity: this.overlayOpacity,
        transition: 'transform 0.4s cubic-bezier(0.22, 1, 0.36, 1), opacity 0.4s ease'
      }
    }
  },
  mounted() {
    this.fetchData()
  },
  watch: {
    courseId(newVal) {
      if (newVal) {
        this.fetchData()
      }
    }
  },
  methods: {
    /** 获取数据 */
    async fetchData() {
      await Promise.all([
        this.fetchOverviewSubmissions(),
        this.fetchOverviewAssignments(),
        this.fetchCourseStudents()
      ])
    },
    
    /** 获取提交概览数据 */
    async fetchOverviewSubmissions() {
      try {
        const res = await smartRequest({
          url: '/api/ai-grading/submissions',
          method: 'get',
          params: {
            page: 1,
            pageSize: 1000
          }
        })
        const data = Array.isArray(res?.data) ? res.data : []
        this.submissions = data.map(item => ({
          submission: item.submission || {},
          realName: item.realName || '',
          assignmentTitle: String(item.assignmentTitle || '')
        }))
      } catch (error) {
        console.error('[作业概览] 获取提交数据失败:', error)
      }
    },
    
    /** 获取课程学生列表 */
    async fetchCourseStudents() {
      console.log('[热力图] fetchCourseStudents 开始, courseId:', this.courseId)
      
      try {
        if (this.courseId) {
          // 有指定课程ID，获取该课程的学生
          console.log('[热力图] 获取指定课程学生, courseId:', this.courseId)
          const res = await request({
            url: '/system/class/student/list',
            method: 'get',
            params: {
              courseId: this.courseId,
              pageNum: 1,
              pageSize: 1000
            }
          })
          
          console.log('[热力图] API 响应:', res)
          
          if (res.rows && res.rows.length > 0) {
            this.courseStudents = res.rows.map(enrollment => ({
              studentUserId: enrollment.studentUserId,
              studentName: enrollment.studentName || `学生${enrollment.studentUserId}`
            }))
            console.log('[热力图] 成功获取课程学生:', this.courseStudents.length, '人', this.courseStudents)
          } else {
            console.log('[热力图] API 返回空数据')
            this.courseStudents = []
          }
        } else {
          // 没有指定课程ID，获取所有课程，再聚合所有学生
          console.log('[热力图] 获取所有课程的所有学生')
          
          // 1. 先获取所有课程列表
          const coursesRes = await request({
            url: '/course/list',
            method: 'get',
            params: {
              pageNum: 1,
              pageSize: 1000
            }
          })
          
          console.log('[热力图] 获取到课程列表:', coursesRes)
          
          if (!coursesRes.rows || coursesRes.rows.length === 0) {
            console.log('[热力图] 没有课程')
            this.courseStudents = []
            return
          }
          
          // 2. 获取每门课的学生并聚合
          const studentMap = new Map()
          
          for (const course of coursesRes.rows) {
            try {
              const studentsRes = await request({
                url: '/system/class/student/list',
                method: 'get',
                params: {
                  courseId: course.id,
                  pageNum: 1,
                  pageSize: 1000
                }
              })
              
              if (studentsRes.rows && studentsRes.rows.length > 0) {
                studentsRes.rows.forEach(enrollment => {
                  const studentId = enrollment.studentUserId
                  if (!studentMap.has(studentId)) {
                    studentMap.set(studentId, {
                      studentUserId: enrollment.studentUserId,
                      studentName: enrollment.studentName || `学生${enrollment.studentUserId}`
                    })
                  }
                })
              }
            } catch (err) {
              console.warn(`[热力图] 获取课程 ${course.id} 的学生失败:`, err)
            }
          }
          
          this.courseStudents = Array.from(studentMap.values())
          console.log('[热力图] 成功聚合所有学生（去重后）:', this.courseStudents.length, '人', this.courseStudents)
        }
      } catch (error) {
        console.error('[热力图] 获取课程学生列表失败，将只显示有提交记录的学生:', error)
        this.courseStudents = []
      }
    },
    
    /** 获取作业列表 */
    async fetchOverviewAssignments() {
      try {
        const res = await smartRequest({
          url: '/api/ai-grading/assignments',
          method: 'get',
          params: {
            page: 1,
            pageSize: 1000
          }
        })
        const data = Array.isArray(res?.data) ? res.data : []
        this.overviewAssignments = data
      } catch (error) {
        console.error('[作业概览] 获取作业列表失败:', error)
      }
    },
    
    /** 查找提交记录 */
    findSubmission(student, assignmentTitle, assignmentId) {
      const list = this.submissionsByStudent[student] || []
      const t = String(assignmentTitle).trim()
      const byId = list.find(r => Number(r.assignmentId || 0) === Number(assignmentId || 0))
      if (byId) return byId
      return list.find(r => String(r.assignmentName || '').includes(t))
    },
    
    /** 获取状态 */
    statusOf(student, assignmentTitle, assignmentId) {
      const r = this.findSubmission(student, assignmentTitle, assignmentId)
      if (!r) return 'none'
      const st = String(r.status)
      if (st === '1') return 'pending'
      if (st === '2') {
        const s = Number(r.score ?? 0)
        if (s <= 30) return 'graded-1'
        if (s <= 60) return 'graded-2'
        if (s <= 80) return 'graded-3'
        return 'graded-4'
      }
      return 'pending'
    },
    
    /** 单元格样式类 */
    cellClass(code) {
      return code === 'none' ? 'hm-none' : code === 'pending' ? 'hm-pending' : `hm-${code}`
    },
    
    /** 单元格标题 */
    cellTitle(student, assignmentTitle, assignmentId) {
      const r = this.findSubmission(student, assignmentTitle, assignmentId)
      if (!r) return `${student} / ${assignmentTitle}：未提交`
      const st = String(r.status) === '2' ? '已批改' : '已提交未批改'
      const sc = r.score == null ? '-' : String(r.score)
      const file = String(r.assignmentName || '')
      return `${student} / ${assignmentTitle}：${st}（文件：${file}，分数：${sc}）`
    },
    
    /** 点击学生 */
    onStudentClick(s) {
      this.selectedStudent = s
      this.overlayX = 100
      this.overlayOpacity = 0
      this.$nextTick(() => {
        this.overlayX = 0
        this.overlayOpacity = 1
      })
    },
    
    /** 关闭浮层 */
    closeOverlay() {
      this.overlayX = 100
      this.overlayOpacity = 0
      setTimeout(() => {
        this.selectedStudent = null
      }, 400)
    },
    
    /** 状态标签 */
    statusLabel(status) {
      return status === '2' ? '已批改' : '已提交未批改'
    },
    
    /** 分数样式类 */
    scoreClass(s) {
      const v = Number(s ?? -1)
      if (v < 0) return 'score-unknown'
      if (v <= 30) return 'score-1'
      if (v <= 60) return 'score-2'
      if (v <= 80) return 'score-3'
      return 'score-4'
    },
    
    /** 文件图标 */
    fileIcon(name) {
      const ext = String(name || '').toLowerCase().split('.').pop() || ''
      if (ext === 'pdf') return 'icon-pdf'
      if (ext === 'doc' || ext === 'docx') return 'icon-word'
      if (ext === 'xls' || ext === 'xlsx') return 'icon-excel'
      if (ext === 'ppt' || ext === 'pptx') return 'icon-ppt'
      if (ext === 'txt') return 'icon-txt'
      return 'icon-file'
    }
  }
}
</script>

<style scoped>
.assignment-overview {
  --primary: #2f6df6;
  --muted-bg: #f5f7fb;
  --accent: #2ec4b6;
  --border: #e6e8ee;
  margin-bottom: 20px;
}

.charts-area {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 16px;
}

.heatmap-card, .donut-card {
  background: #fff;
  border: 1.5px solid #d7dbe5;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.08);
}

.donut-card {
  position: relative !important;
}

.chart-header {
  font-size: 16px;
  font-weight: 700;
  color: #1f2a44;
  padding: 10px 12px;
  border-bottom: 1px solid #e6e8ee;
  margin: -12px -12px 8px -12px;
  border-top-left-radius: 10px;
  border-top-right-radius: 10px;
  background: linear-gradient(180deg, #f8fafc, #ffffff);
}

.heatmap-card {
  padding: 12px 12px;
  overflow: visible;
}

.hm-scroll {
  overflow-x: auto;
  overflow-y: hidden;
}

.hm-body-wrapper {
  max-height: calc(6 * 36px);
  overflow-y: auto;
  overflow-x: visible;
  margin-bottom: 0;
}

.hm-body-wrapper::-webkit-scrollbar {
  width: 8px;
}

.hm-body-wrapper::-webkit-scrollbar-track {
  background: #f1f3f5;
  border-radius: 4px;
}

.hm-body-wrapper::-webkit-scrollbar-thumb {
  background: #c1c8d0;
  border-radius: 4px;
}

.hm-body-wrapper::-webkit-scrollbar-thumb:hover {
  background: #a0a8b0;
}

.hm-bottom {
  display: grid;
  grid-template-columns: 200px 1fr;
  align-items: start;
  margin-top: 6px;
}

.hm-bottom-spacer {
  width: 200px;
}

.hm-students {
  display: grid;
  gap: 4px;
  overflow: visible;
}

.hm-student {
  width: 28px;
  height: 40px;
  position: relative;
}

.hm-student-label {
  position: absolute;
  left: 50%;
  top: 4px;
  transform-origin: center top;
  transform: rotate(-45deg) translate(-50%, 0);
  font-size: 13px;
  white-space: nowrap;
  max-width: 160px;
  text-overflow: ellipsis;
  overflow: visible;
  color: #455a7a;
  text-align: center;
}

.hm-student-label.clickable {
  cursor: pointer;
}

.hm-student-label.clickable:hover {
  color: #2F5496;
  text-decoration: underline;
}

.hm-body {
  min-width: 100%;
}

.hm-row {
  display: grid;
  grid-template-columns: 200px 1fr;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.hm-assignment {
  font-size: 13px;
  color: #314160;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.hm-cells {
  display: grid;
  gap: 6px;
}

.hm-cell {
  width: 28px;
  height: 28px;
  border-radius: 5px;
  background: #eef2f7;
  border: 1px solid #e6e8ee;
}

.hm-cell.is-selected {
  outline: 2px solid #2F5496;
  border-color: #2F5496;
}

.hm-none { background: #ffffff; }
.hm-pending { background: #ffd166; }
.hm-graded-1 { background: #C7F9E5; }
.hm-graded-2 { background: #90F0D0; }
.hm-graded-3 { background: #4ED9B6; }
.hm-graded-4 { background: #06d6a0; }

.hm-legend {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 8px;
  color: #7a879c;
  font-size: 13px;
}

.legend-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.legend-swatch {
  width: 14px;
  height: 14px;
  border-radius: 3px;
  display: inline-block;
  border: 1px solid #e6e8ee;
}

.donut-card {
  padding: 12px 12px;
  display: grid;
  grid-template-rows: auto auto;
  align-items: center;
  justify-items: center;
}

.donut-content {
  position: relative;
  overflow: hidden;
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.donut-wrap {
  position: relative;
  width: 160px;
  height: 160px;
}

.donut {
  width: 160px;
  height: 160px;
  transform: rotate(-90deg);
}

.donut-bg {
  stroke: #9aa3b2;
}

.donut-seg {
  transition: stroke-dasharray 0.3s ease;
  stroke-linecap: butt;
}

.donut-center {
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
}

.donut-total {
  font-size: 22px;
  font-weight: 800;
  color: #1f2a44;
}

.donut-label {
  font-size: 13px;
  color: #7a879c;
}

.donut-legend {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-top: 8px;
}

.dl-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  width: 100%;
  font-size: 13px;
  color: #314160;
}

.dl-swatch {
  width: 14px;
  height: 14px;
  border-radius: 3px;
  display: inline-block;
}

.dl-none { background: #9aa3b2; }
.dl-pending { background: #ffd166; }
.dl-graded { background: #06d6a0; }

.dl-num {
  margin-left: auto;
  font-weight: 600;
}

/* 浮层样式 */
.student-overlay {
  position: absolute !important;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
  width: 100%;
  background: #ffffff;
  border-left: 1px solid var(--border);
  box-shadow: 0 2px 5px rgba(0,0,0,0.08);
  padding: 15px;
  display: flex;
  flex-direction: column;
  z-index: 20;
  border-radius: 10px;
  overflow: hidden;
}

.overlay-title {
  font-size: 16px;
  font-weight: 700;
  color: #1f2a44;
  text-align: center;
  margin-bottom: 8px;
}

.overlay-title-icon {
  margin-right: 8px;
  font-size: 18px;
}

.overlay-deco {
  height: 2px;
  background: #2f6df6;
  width: 120px;
  margin: 0 auto 8px auto;
  border-radius: 2px;
}

.overlay-close {
  position: absolute;
  right: 10px;
  top: 8px;
  border: 1px solid #d7dbe5;
  background: #fff;
  color: #314160;
  width: 28px;
  height: 28px;
  border-radius: 6px;
  cursor: pointer;
}

.overlay-close:hover {
  background: #f3f6fb;
}

.overlay-content {
  position: relative;
  height: 100%;
  background: linear-gradient(180deg, rgba(249,251,255,0.9), rgba(255,255,255,0.96));
  backdrop-filter: blur(6px);
  border: 1px solid #e6e8ee;
  border-radius: 10px;
  box-shadow: 0 8px 24px rgba(31,42,68,0.06);
  padding: 15px;
  display: flex;
  flex-direction: column;
  animation: cardFade 0.3s ease both;
  box-sizing: border-box;
}

.overlay-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  overflow-y: auto;
  overflow-x: hidden;
  flex: 1;
  min-height: 0;
  padding: 8px 0;
}

.ov-card {
  border: 1px solid #e6e8ee;
  border-radius: 12px;
  background: linear-gradient(180deg, #ffffff, #fbfcff);
  box-shadow: 0 6px 18px rgba(31,42,68,0.06);
  padding: 12px 14px;
  transition: box-shadow 0.2s ease, transform 0.2s ease, background-color 0.2s ease;
  animation: rowFadeUp 0.25s ease both;
}

.ov-card:hover {
  box-shadow: 0 8px 24px rgba(31,42,68,0.09);
  transform: translateY(-1px);
}

.ov-card.row-graded {
  border-left: 2px solid #2ec4b6;
}

.ov-card.row-pending {
  border-left: 2px solid #ffd166;
}

.ov-head {
  display: grid;
  grid-template-columns: 1fr auto;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.ov-title {
  font-size: 14px;
  color: #314160;
  font-weight: 700;
  letter-spacing: 0.2px;
}

.ov-score-badge {
  min-width: 44px;
  text-align: center;
  padding: 4px 10px;
  border-radius: 14px;
  font-size: 14px;
  font-weight: 700;
  background: rgba(47,109,246,0.08);
  color: #2f6df6;
  border: 1px solid rgba(47,109,246,0.18);
}

.score-1, .ov-score-badge.score-1 { background: rgba(46, 196, 182, 0.12); color: #16785E; border-color: rgba(46,196,182,0.3); }
.score-2, .ov-score-badge.score-2 { background: rgba(46, 196, 182, 0.12); color: #1F6E62; border-color: rgba(46,196,182,0.3); }
.score-3, .ov-score-badge.score-3 { background: rgba(46, 196, 182, 0.12); color: #16785E; border-color: rgba(46,196,182,0.3); }
.score-4, .ov-score-badge.score-4 { background: rgba(46, 196, 182, 0.18); color: #0E8E58; border-color: rgba(46,196,182,0.3); }
.score-unknown, .ov-score-badge.score-unknown { background: rgba(154,163,178,0.15); color: #7a879c; border-color: rgba(154,163,178,0.25); }

.ov-body {
  display: grid;
  grid-template-columns: 1fr auto;
  align-items: center;
  gap: 10px;
}

.ov-file {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #586a88;
}

.file-name {
  max-width: 100%;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.ov-status {
  font-size: 14px;
  color: #7a879c;
  text-align: right;
}

.st-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 13px;
}

.st-graded {
  background: rgba(46, 196, 182, 0.12);
  color: #16785E;
}

.st-pending {
  background: rgba(255, 209, 102, 0.18);
  color: #9c6e00;
}

.st-icon {
  font-size: 12px;
  line-height: 1;
}

.file-icon {
  width: 14px;
  height: 14px;
  border-radius: 3px;
  display: inline-block;
}

.icon-file { background: #b0b8c5; }
.icon-word { background: #2f6df6; }
.icon-pdf { background: #e63946; }
.icon-excel { background: #2ec4b6; }
.icon-ppt { background: #ff7f50; }
.icon-txt { background: #7a879c; }

.ov-empty {
  font-size: 13px;
  color: #9aa3b2;
  text-align: center;
  padding: 12px;
}

@keyframes cardFade {
  from { opacity: 0; transform: translateY(-6px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes rowFadeUp {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

.overlay-slide-enter-active, .overlay-slide-leave-active {
  transition: transform 0.4s cubic-bezier(0.22, 1, 0.36, 1), opacity 0.4s ease;
}

.overlay-slide-enter {
  transform: translateX(100%);
  opacity: 0;
}

.overlay-slide-enter-to {
  transform: translateX(0);
  opacity: 1;
}

.overlay-slide-leave {
  transform: translateX(0);
  opacity: 1;
}

.overlay-slide-leave-to {
  transform: translateX(100%);
  opacity: 0;
}

@media (max-width: 992px) {
  .charts-area {
    grid-template-columns: 1fr;
  }
}
</style>
