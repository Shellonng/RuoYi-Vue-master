<template>
  <div class="teacher-assignments-page">
    <el-tabs v-model="viewMode" class="view-tabs">
      

      <el-tab-pane name="management">
        <template #label>
          <span class="view-tab-label">
            <el-icon><DataLine /></el-icon>
            高级管理视图
          </span>
        </template>
        <div class="assignments-view">
          <header class="page-header">
            <div class="header-content">
              <div class="header-left">
                <h1>
                  <el-icon class="header-icon"><Document /></el-icon>
                  作业 / 成绩管理
                </h1>
                <p class="sub-title">管理作业和考试，查看学生提交记录</p>
              </div>
              <div class="header-right">
                <el-tag type="primary" effect="dark" size="large" class="course-tag">
                  <el-icon><Reading /></el-icon>
                  {{ currentCourse?.title || '未选择课程' }}
                </el-tag>
              </div>
            </div>
          </header>
          <div class="subnav">
            <button :class="['nav-pill', activeTab==='published' ? 'is-active' : '']" @click="activeTab='published'">
              <span class="icon">📢</span><span>已发布作业</span><span class="count-badge">{{ pubTotal }}</span>
            </button>
            <button :class="['nav-pill', activeTab==='submitted' ? 'is-active' : '']" @click="activeTab='submitted'">
              <span class="icon">📥</span><span>已提交作业</span><span class="count-badge">{{ total }}</span>
            </button>
          </div>

          <div v-if="identity.isTeacher" class="charts-area">
            <div class="heatmap-card">
              <div class="chart-header">作业-学生热力图</div>
              <div class="hm-scroll">
                <div class="hm-body">
                  <div v-for="a in assignmentsList" :key="'a-'+a.id" class="hm-row">
                    <div class="hm-assignment" :title="a.title">{{ a.title }}</div>
                    <div class="hm-cells" :style="hmCellsStyle">
                      <div v-for="s in studentsList" :key="s+'|'+a.id" :class="['hm-cell', cellClass(statusOf(s, a.title, a.id)), selectedStudent===s ? 'is-selected' : '']" :title="cellTitle(s, a.title, a.id)"></div>
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

            <div class="donut-card" style="position:relative;overflow:hidden">
              <div class="chart-header">提交状态环形概览</div>
              <div class="donut-content">
                <div class="donut-wrap">
                  <svg class="donut" viewBox="0 0 120 120">
                    <circle class="donut-bg" cx="60" cy="60" r="50" stroke-width="18" fill="none" />
                    <circle v-for="(seg, idx) in donutSegments" :key="idx" class="donut-seg" cx="60" cy="60" r="50" stroke-width="18" fill="none"
                      :stroke="seg.color" :stroke-dasharray="seg.dash" :stroke-dashoffset="seg.offset" />
                  </svg>
                  <div class="donut-center">
                    <div class="donut-total">{{ donutTotal }}</div>
                    <div class="donut-label">总单元</div>
                  </div>
                </div>
                <div class="donut-legend">
                  <div class="dl-item"><span class="dl-swatch dl-none"></span><span>未提交</span><span class="dl-num">{{ donutNone }}</span></div>
                  <div class="dl-item"><span class="dl-swatch dl-pending"></span><span>已提交未批改</span><span class="dl-num">{{ donutPending }}</span></div>
                  <div class="dl-item"><span class="dl-swatch dl-graded"></span><span>已提交已批改</span><span class="dl-num">{{ donutGraded }}</span></div>
                </div>
              </div>
              <transition name="overlay-slide" appear>
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
                            {{ r.score ?? '-' }}
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

          

          <transition name="slide-h" mode="out-in">
            <div v-if="activeTab==='published'" class="block-card">
              <div class="block-header">
                <div class="block-title"><span class="block-icon">📄</span>已发布的作业</div>
                <div v-if="identity.isTeacher" class="block-actions">
                  <el-button v-if="!showPubInline" class="emphasis-btn" @click="showAddModal=true">新增作业</el-button>
                  <el-button v-if="!showPubInline" class="danger-btn" type="danger" @click="toggleDeleteMode">删除作业</el-button>
                  <el-button v-else @click="closePubInline">返回列表</el-button>
                  <span v-if="deleteMode && !showPubInline" class="muted">删除模式已开启：点击表格项以删除</span>
                </div>
              </div>
              <div class="table-wrapper" v-if="!showPubInline">
                <AssignmentManager v-if="courseId" :course-id="courseId" :use-external-detail="true" @view-detail="onExternalAssignmentDetail" />
                <el-empty v-else description="请先选择课程" />
              </div>
              <div v-else class="table-wrapper">
                <AssignmentPublishDetailModal inline :visible="true" :assignment="currentAssignment" @close="closePubInline" />
              </div>
              <el-pagination
                class="pagination"
                background
                layout="total, sizes, prev, pager, next, jumper"
                :total="pubTotal"
                :page-size="pubPageSize"
                :current-page="pubPage"
                :page-sizes="[10,20,50,100]"
                @current-change="onPubPageChange"
                @size-change="onPubSizeChange"
              />
            </div>
          </transition>

          <transition name="slide-h" mode="out-in">
            <div v-if="activeTab==='submitted' && identity.isTeacher" class="block-card">
              <div class="block-header">
                <div class="block-title"><span class="block-icon">📥</span>已提交的作业</div>
                <div class="block-actions">
                  <el-button v-if="!showSubInline" class="emphasis-btn" @click="showBatch=true">批量评分管理</el-button>
                  <el-button v-else @click="closeSubInline">返回列表</el-button>
                </div>
              </div>
        <div class="table-wrapper" v-if="!showSubInline">
          <el-card class="submissions-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <span class="title">
                  <el-icon><DataLine /></el-icon>
                  学生提交记录
                </span>
                <el-button type="primary" :icon="Refresh" @click="fetchOverviewSubmissions" :loading="submissionsLoading">
                  刷新
                </el-button>
              </div>
            </template>

            <el-table
              :data="submissions"
              border
              stripe
              v-loading="submissionsLoading"
              class="submissions-table"

            >
              <el-table-column prop="id" sortable="custom" label="提交ID" width="90" align="center">
                <template #default="{ row }">
                  <el-tag size="small" type="info">{{ row.submission?.id }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="student" sortable="custom" label="学生" width="140" align="center">
                <template #default="{ row }">
                  <div class="student-info">
                    <el-avatar :size="28" :icon="User" />
                    <span>{{ row.realName || row.submission?.studentUserId }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="title" sortable="custom" label="作业" min-width="180" show-overflow-tooltip>
                <template #default="{ row }">
                  {{ row.assignmentTitle || ('作业 ' + (row.submission?.assignmentId || '')) }}
                </template>
              </el-table-column>
              <el-table-column prop="score" sortable="custom" label="得分" width="100" align="center">
                <template #default="{ row }">
                  <span v-if="row.submission?.score !== null && row.submission?.score !== undefined" class="score-value">
                    {{ row.submission.score }}
                  </span>
                  <el-tag v-else size="small" type="info">待批改</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="status" sortable="custom" label="状态" width="100" align="center">
                <template #default="{ row }">
                  <el-tag :type="getSubmissionStatusType(row.submission?.status)" effect="light">
                    {{ getSubmissionStatusText(row.submission?.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="submitTime" sortable="custom" label="提交时间" width="170" align="center">
                <template #default="{ row }">
                  <span class="time-text">{{ formatTime(row.submission?.submitTime) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="120" fixed="right" align="center">
                <template #default="{ row }">
                  <el-button
                    size="small"
                    type="primary"
                    :icon="View"
                    @click="openSubmissionDetail(row.submission?.id)"
                  >
                    批改
                  </el-button>
                </template>
              </el-table-column>
            </el-table>

            <div v-if="submissions.length === 0 && !submissionsLoading" class="empty-submissions">
              <el-empty description="暂无提交记录" />
            </div>
          </el-card>
        </div>
        <div v-else class="table-wrapper">
          <AssignmentDetailModal inline :visible="true" :submission-id="currentSubmissionId" @close="closeSubInline" @mark-graded="onMarkGraded" />
        </div>
              <el-pagination
                class="pagination"
                background
                layout="total, sizes, prev, pager, next, jumper"
                :total="total"
                :page-size="pageSize"
                :current-page="page"
                :page-sizes="[10,20,50,100]"
                @current-change="onPageChange"
                @size-change="onSizeChange"
              />
            </div>
          </transition>

          <div class="decorative-divider"><span>✏️</span></div>

          <transition name="slide-h" mode="out-in">
            <div v-if="activeTab==='submitted' && identity.isStudent" class="block-card">
              <div class="block-header">
                <div class="block-title"><span class="block-icon">🧑‍🎓</span>我提交的作业</div>
              </div>
              <div class="table-wrapper" v-if="!showSubInline">
                <table class="assignments-table">
                  <thead>
                    <tr>
                      <th>序号</th>
                      <th>
                        <div class="th-with-input">
                          <span class="sort-icon" @click="toggleSortMy('studentId')">{{ sortSymbolMy('studentId') }}</span>
                          <span>学生ID</span>
                          <input v-model="myQuery.studentId" class="table-input" placeholder="搜索学生ID" />
                        </div>
                      </th>
                      <th>
                        <div class="th-with-input">
                          <span class="sort-icon" @click="toggleSortMy('studentName')">{{ sortSymbolMy('studentName') }}</span>
                          <span>学生名</span>
                          <input v-model="myQuery.studentName" class="table-input" placeholder="搜索学生名" />
                        </div>
                      </th>
                      <th>
                        <div class="th-with-input">
                          <span class="sort-icon" @click="toggleSortMy('assignmentName')">{{ sortSymbolMy('assignmentName') }}</span>
                          <span>作业名</span>
                          <input v-model="myQuery.assignmentName" class="table-input" placeholder="搜索作业名" />
                        </div>
                      </th>
                      <th><span class="sort-icon" @click="toggleSortMy('score')">{{ sortSymbolMy('score') }}</span> 分数</th>
                      <th><span class="sort-icon" @click="toggleSortMy('status')">{{ sortSymbolMy('status') }}</span> 批改情况</th>
                      <th class="col-actions">操作</th>
                    </tr>
                  </thead>
                  <tbody v-if="!loadingMy">
                    <tr v-for="row in myFilteredRows" :key="row.seq">
                      <td>{{ row.seq }}</td>
                      <td>{{ row.studentId }}</td>
                      <td>{{ row.studentName }}</td>
                      <td>{{ row.assignmentName }}</td>
                      <td>{{ row.score ?? '-' }}</td>
                      <td>{{ statusLabel(row.status) }}</td>
                      <td class="col-actions">
                        <button class="link-btn" @click="openDetail(row)">查看详情</button>
                      </td>
                    </tr>
                  </tbody>
                  <tbody v-else>
                    <tr v-for="i in 6" :key="'my-sk-'+i" class="skeleton-row">
                      <td colspan="7"><div class="skeleton"></div></td>
                    </tr>
                  </tbody>
                </table>
              </div>
              <div v-else class="table-wrapper">
                <AssignmentDetailModal inline :visible="true" :submission-id="currentSubmissionId" @close="closeSubInline" @mark-graded="onMarkGraded" />
              </div>
              <el-pagination
                class="pagination"
                background
                layout="total, sizes, prev, pager, next, jumper"
                :total="myTotal"
                :page-size="myPageSize"
                :current-page="myPage"
                :page-sizes="[10,20,50,100]"
                @current-change="onMyPageChange"
                @size-change="onMySizeChange"
              />
            </div>
          </transition>

          <BatchGradingModal :visible="showBatch" @close="showBatch=false" @mark-graded="onMarkGraded" />
          <AddAssignmentModal :visible="showAddModal" @close="showAddModal=false" @created="onAssignmentCreated" />
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { reactive, computed, ref, onMounted, watch, nextTick } from 'vue'
import type { CSSProperties } from 'vue'
import { useRouter } from 'vue-router'
import { Document, Reading, EditPen, DataLine, Refresh, View, User } from '@element-plus/icons-vue'
import AssignmentManager from '../../components/AssignmentManager.vue'
import AssignmentDetailModal from './AssignmentDetailModal.vue'
import BatchGradingModal from './BatchGradingModal.vue'
import AssignmentPublishDetailModal from './AssignmentPublishDetailModal.vue'
import AddAssignmentModal from './AddAssignmentModal.vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUser, getSelectedCourse } from '../../utils/session.js'
import { BASE_URL } from '../../utils/api'

interface AssignmentRow {
  seq: number
  submissionId: number
  assignmentId?: number
  studentId: string
  studentName: string
  assignmentName: string
  score: number | null
  status: string
}

interface SubmissionData {
  id?: number
  studentUserId?: string
  assignmentId?: number
  fileName?: string
  score?: number | null
  status?: string | number | null
  submitTime?: string | number
  [key: string]: unknown
}

interface SubmissionApiItem {
  submission?: SubmissionData
  realName?: string
  [key: string]: unknown
}

type UnknownRecord = Record<string, any>

const router = useRouter()
const currentCourse = ref<UnknownRecord | null>(null)
const currentUser = ref<UnknownRecord | null>(null)
const courseId = computed(() => (currentCourse.value?.id as number | null) ?? null)
const viewMode = ref<'overview' | 'management'>('management')
const submissions = ref<UnknownRecord[]>([])
const submissionsBackup = ref<UnknownRecord[]>([])
const submissionsLoading = ref(false)
const overviewAssignments = ref<UnknownRecord[]>([])

async function fetchOverviewSubmissions() {
  if (!currentCourse.value || !currentUser.value) return
  submissionsLoading.value = true
  try {
    const submissionsRes = await fetch(`${BASE_URL}/api/ai-grading/submissions?page=1&pageSize=1000&order=desc&sortBy=submitTime`)
    const submissionsData = await submissionsRes.json()

    const assignmentsRes = await fetch(`${BASE_URL}/api/ai-grading/assignments?page=1&pageSize=1000`)
    const assignmentsData = await assignmentsRes.json()
    const allAssignments: UnknownRecord[] = Array.isArray(assignmentsData.data) ? assignmentsData.data : []
    overviewAssignments.value = allAssignments

    const teacherId = Number(currentUser.value.id ?? 0)
    const selectedCourseId = Number(currentCourse.value.id ?? 0)
    const myAssignmentIds = allAssignments
      .filter((a: UnknownRecord) => Number(a.publisherUserId ?? 0) === teacherId && Number(a.courseId ?? 0) === selectedCourseId)
      .map((a: UnknownRecord) => Number(a.id))
      .filter((id: number) => Number.isFinite(id))

    const assignmentMap: Record<number, string> = {}
    allAssignments.forEach((a: UnknownRecord) => {
      const id = Number(a.id)
      if (Number.isFinite(id)) {
        assignmentMap[id] = String(a.title ?? '')
      }
    })
    const idSet = new Set(myAssignmentIds)

    const allSubmissions: UnknownRecord[] = Array.isArray(submissionsData.data) ? submissionsData.data : []
    const filtered = allSubmissions
      .filter((item: UnknownRecord) => idSet.has(Number(item.submission?.assignmentId)))
      .map((item: UnknownRecord) => {
        const assignmentId = Number(item.submission?.assignmentId)
        return {
          ...item,
          assignmentTitle: assignmentMap[assignmentId] || ''
        }
      })

    submissions.value = filtered
    submissionsBackup.value = [...filtered]
  } catch (error) {
    console.error('获取提交记录失败:', error)
    ElMessage.error('获取提交记录失败')
  } finally {
    submissionsLoading.value = false
  }
}

function getSubmissionStatusType(status?: number | string | null) {
  if (Number(status) === 0) return 'info'
  if (Number(status) === 1) return 'warning'
  if (Number(status) === 2) return 'success'
  return 'info'
}

function getSubmissionStatusText(status?: number | string | null) {
  if (Number(status) === 0) return '未提交'
  if (Number(status) === 1) return '待批改'
  if (Number(status) === 2) return '已批改'
  return '--'
}

function formatTime(time?: string | number | Date) {
  if (!time) return '--'
  return new Date(time).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

function goToGrading(submissionId?: number | string | null) {
  if (!submissionId) return
  router.push(`/teacher/grading/${submissionId}`)
}

// removed overview tab watchers


onMounted(() => {
  currentCourse.value = getSelectedCourse() || null
  currentUser.value = getUser() || null
})

const rows = ref<AssignmentRow[]>([])
const loadingSub = ref(false)
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const sortBy = ref<string>('')
const order = ref<'asc' | 'desc' | ''>('')
const currentSubmissionId = ref<number | null>(null)

const query = reactive({ studentId: '', studentName: '', assignmentName: '' })

const filteredRows = computed(() => {
  const id = query.studentId.trim().toLowerCase()
  const name = query.studentName.trim().toLowerCase()
  const assignment = query.assignmentName.trim().toLowerCase()
  return rows.value.filter(r =>
    (!id || r.studentId.toLowerCase().includes(id)) &&
    (!name || r.studentName.toLowerCase().includes(name)) &&
    (!assignment || r.assignmentName.toLowerCase().includes(assignment))
  )
})

const showSubInline = ref(false)
const showBatch = ref(false)
function openDetail(row: AssignmentRow) { currentSubmissionId.value = row.submissionId; showSubInline.value = true }
function closeSubInline() { showSubInline.value = false }
function openSubmissionDetail(id?: number | null) { if (!id) return; currentSubmissionId.value = Number(id); showSubInline.value = true }

type SortKey = 'studentId' | 'studentName' | 'assignmentName' | 'score' | 'status'
type SortState = 'none' | 'asc' | 'desc'
const sort = reactive<Record<SortKey, SortState>>({ studentId: 'none', studentName: 'none', assignmentName: 'none', score: 'none', status: 'none' })
const sortKeyMap: Record<SortKey, string> = { studentId: 'studentUserId', studentName: 'realName', assignmentName: 'fileName', score: 'score', status: 'status' }
function toggleSort(key: SortKey) {
  const next: Record<SortState, SortState> = { none: 'asc', asc: 'desc', desc: 'none' }
  sort[key] = next[sort[key]]
  sortBy.value = sort[key] === 'none' ? '' : key
  order.value = sort[key] === 'none' ? '' : (sort[key] === 'asc' ? 'asc' : 'desc')
  page.value = 1
  loadSubmissions()
}
function sortSymbol(key: SortKey) {
  return sort[key] === 'none' ? '—' : sort[key] === 'asc' ? '↑' : '↓'
}

const activeTab = ref<'published' | 'submitted'>('published')

watch([activeTab, viewMode], ([tab, mode]) => {
  if (mode === 'management' && tab === 'submitted' && identity.isTeacher) {
    fetchOverviewSubmissions()
  }
})

function buildIdentity() {
  const user = getUser()
  const course = getSelectedCourse()
  const userId = Number(user?.id ?? 0)
  const isTeacher = !!(course && Number(course.teacherUserId ?? 0) === userId)
  const isStudent = !isTeacher
  return { isTeacher, isStudent, userId }
}
const identity = reactive(buildIdentity())

async function loadSubmissions() {
  try {
    loadingSub.value = true
    const q = new URLSearchParams()
    q.set('page', String(page.value))
    q.set('pageSize', String(pageSize.value))
    if (sortBy.value && order.value) { q.set('sortBy', sortKeyMap[sortBy.value as SortKey]); q.set('order', order.value) }
    const url = '/api/ai-grading/submissions?' + q.toString()
    const res = await fetch(url)
    const json = await res.json()
  const data: SubmissionApiItem[] = Array.isArray(json?.data) ? json.data : []
  rows.value = data.map((item: SubmissionApiItem, idx: number) => {
      const sub: SubmissionData = item.submission || {}
      const realName = item.realName || ''
      const assignmentIdRaw = sub.assignmentId ?? (sub as { assignmentID?: unknown }).assignmentID ?? (sub as { assignment_id?: unknown }).assignment_id ?? (sub as { assignment?: unknown }).assignment
      const assignmentIdNum = Number(assignmentIdRaw ?? 0)
      return {
        seq: (page.value - 1) * pageSize.value + idx + 1,
        submissionId: Number(sub.id ?? 0),
        assignmentId: Number.isFinite(assignmentIdNum) ? assignmentIdNum : 0,
        studentId: String(sub.studentUserId ?? ''),
        studentName: realName,
        assignmentName: String(sub.fileName ?? ''),
        score: sub.score ?? null,
        status: String(sub.status ?? '')
      }
  })
  total.value = Number(json?.total ?? rows.value.length)
  // overview submissions
  const filtered = data.map((item: SubmissionApiItem) => ({
    submission: item.submission || {},
    realName: item.realName || '',
    assignmentTitle: String(item.assignmentTitle || '')
  }))
  submissions.value = filtered
  submissionsBackup.value = [...filtered]
  } catch (e) {
    console.error('加载提交数据失败', e)
  }
  finally {
    loadingSub.value = false
  }
}

onMounted(async () => {
  await markGraded()
  if (identity.isTeacher) loadSubmissions()
  if (identity.isTeacher) fetchOverviewSubmissions()
})
function onPageChange(p: number) { page.value = p; loadSubmissions() }
function onSizeChange(ps: number) { pageSize.value = ps; page.value = 1; loadSubmissions() }

interface PublishedRow {
  seq: number
  id: number
  title: string
  publisherRealName: string
  startTime: string
  endTime: string
  raw: Record<string, unknown>
}

interface AssignmentApiItem {
  id?: number
  title?: string
  publisherRealName?: string
  publisherUserId?: string
  startTime?: string
  endTime?: string
  [key: string]: unknown
}

const pubRows = ref<PublishedRow[]>([])
const loadingPub = ref(false)
const pubTotal = ref(0)
const pubPage = ref(1)
const pubPageSize = ref(10)
const pubSortBy = ref<string>('')
const pubOrder = ref<'asc' | 'desc' | ''>('')
const pubQuery = reactive({ title: '', publisherRealName: '' })
const showPubInline = ref(false)
const currentAssignment = ref<Record<string, unknown>>({})
const showAddModal = ref(false)
const deleteMode = ref(false)

function openPubDetail(row: PublishedRow) { currentAssignment.value = row.raw; showPubInline.value = true }
function closePubInline() { showPubInline.value = false }
function onExternalAssignmentDetail(detail: Record<string, unknown>) { currentAssignment.value = detail; showPubInline.value = true }

type PubSortKey = 'title' | 'publisherRealName' | 'startTime' | 'endTime'
const pubSort = reactive<Record<PubSortKey, SortState>>({ title: 'none', publisherRealName: 'none', startTime: 'none', endTime: 'none' })
function toggleSortPub(key: PubSortKey) {
  const next: Record<SortState, SortState> = { none: 'asc', asc: 'desc', desc: 'none' }
  pubSort[key] = next[pubSort[key]]
  pubSortBy.value = pubSort[key] === 'none' ? '' : key
  pubOrder.value = pubSort[key] === 'none' ? '' : (pubSort[key] === 'asc' ? 'asc' : 'desc')
  pubPage.value = 1
  loadAssignments()
}
function sortSymbolPub(key: PubSortKey) { return pubSort[key] === 'none' ? '—' : pubSort[key] === 'asc' ? '↑' : '↓' }

const filteredPubRows = computed(() => {
  const t = pubQuery.title.trim().toLowerCase()
  const p = pubQuery.publisherRealName.trim().toLowerCase()
  return pubRows.value.filter(r => (!t || r.title.toLowerCase().includes(t)) && (!p || r.publisherRealName.toLowerCase().includes(p)))
})

async function loadAssignments() {
  try {
    loadingPub.value = true
    const q = new URLSearchParams()
    q.set('page', String(pubPage.value))
    q.set('pageSize', String(pubPageSize.value))
    if (pubSortBy.value && pubOrder.value) { q.set('sortBy', pubSortBy.value); q.set('order', pubOrder.value) }
    const res = await fetch('/api/ai-grading/assignments?' + q.toString())
    const json = await res.json()
    const data: AssignmentApiItem[] = Array.isArray(json?.data) ? json.data : []
    pubRows.value = data.map((a: AssignmentApiItem, idx: number) => ({
      seq: (pubPage.value - 1) * pubPageSize.value + idx + 1,
      id: Number(a.id ?? 0),
      title: String(a.title ?? ''),
      publisherRealName: String(a.publisherRealName ?? a.publisherUserId ?? ''),
      startTime: String(a.startTime ?? ''),
      endTime: String(a.endTime ?? ''),
      raw: a as Record<string, unknown>
    }))
    pubTotal.value = Number(json?.total ?? pubRows.value.length)
  } catch (error) {
    console.error('加载作业数据失败', error)
  }
  finally {
    loadingPub.value = false
  }
}

onMounted(() => { loadAssignments() })
function onPubPageChange(p: number) { pubPage.value = p; loadAssignments() }
function onPubSizeChange(ps: number) { pubPageSize.value = ps; pubPage.value = 1; loadAssignments() }

function toggleDeleteMode() { deleteMode.value = !deleteMode.value }

async function onPubRowClick(row: PublishedRow) {
  if (!deleteMode.value || !identity.isTeacher) return
  try {
    await ElMessageBox.confirm(`确认删除作业：${row.title}（ID=${row.id}）？`, '删除确认', { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' })
    const res = await fetch(`/api/ai-grading/assignments/${row.id}`, { method: 'DELETE' })
    const json = await res.json()
    if (json?.code === 200 && json?.data) { ElMessage.success('已删除作业'); await loadAssignments() }
    else { ElMessage.info('未删除任何作业') }
  } catch (error) {
    console.error('删除作业失败', error)
  }
}

async function onAssignmentCreated() { deleteMode.value = false; await loadAssignments() }

const myRows = ref<AssignmentRow[]>([])
const loadingMy = ref(false)
const myTotal = ref(0)
const myPage = ref(1)
const myPageSize = ref(10)
const mySortBy = ref<string>('')
const myOrder = ref<'asc' | 'desc' | ''>('')
const myQuery = reactive({ studentId: '', studentName: '', assignmentName: '' })
const mySort = reactive<Record<SortKey, SortState>>({ studentId: 'none', studentName: 'none', assignmentName: 'none', score: 'none', status: 'none' })
const mySortKeyMap: Record<SortKey, string> = { studentId: 'studentUserId', studentName: 'realName', assignmentName: 'fileName', score: 'score', status: 'status' }
const myFilteredRows = computed(() => {
  const id = myQuery.studentId.trim().toLowerCase()
  const name = myQuery.studentName.trim().toLowerCase()
  const assignment = myQuery.assignmentName.trim().toLowerCase()
  return myRows.value.filter(r =>
    (!id || r.studentId.toLowerCase().includes(id)) &&
    (!name || r.studentName.toLowerCase().includes(name)) &&
    (!assignment || r.assignmentName.toLowerCase().includes(assignment))
  )
})

function toggleSortMy(key: SortKey) {
  const next: Record<SortState, SortState> = { none: 'asc', asc: 'desc', desc: 'none' }
  mySort[key] = next[mySort[key]]
  mySortBy.value = mySort[key] === 'none' ? '' : key
  myOrder.value = mySort[key] === 'none' ? '' : (mySort[key] === 'asc' ? 'asc' : 'desc')
  myPage.value = 1
  loadMySubmissions()
}
function sortSymbolMy(key: SortKey) { return mySort[key] === 'none' ? '—' : mySort[key] === 'asc' ? '↑' : '↓' }

async function loadMySubmissions() {
  try {
    loadingMy.value = true
    const myId = String(identity.userId || 1)
    const q = new URLSearchParams()
    q.set('page', String(myPage.value))
    q.set('pageSize', String(myPageSize.value))
    if (mySortBy.value && myOrder.value) { q.set('sortBy', mySortKeyMap[mySortBy.value as SortKey]); q.set('order', myOrder.value) }
    q.set('studentUserId', myId)
    const url = '/api/ai-grading/submissions?' + q.toString()
    const res = await fetch(url)
    const json = await res.json()
    const data: SubmissionApiItem[] = Array.isArray(json?.data) ? json.data : []
    const filtered = data.filter((item: SubmissionApiItem) => String(item?.submission?.studentUserId ?? '') === myId)
    myRows.value = filtered.map((item: SubmissionApiItem, idx: number) => {
      const sub = item.submission || {}
      const realName = item.realName || ''
      return {
        seq: (myPage.value - 1) * myPageSize.value + idx + 1,
        submissionId: Number(sub.id ?? 0),
        studentId: String(sub.studentUserId ?? ''),
        studentName: realName,
        assignmentName: String(sub.fileName ?? ''),
        score: sub.score ?? null,
        status: String(sub.status ?? '')
      }
    })
    myTotal.value = Number(json?.total ?? myRows.value.length)
  } catch (error) {
    console.error('加载我的提交失败', error)
  }
  finally {
    loadingMy.value = false
  }
}

onMounted(() => { if (identity.isStudent) loadMySubmissions() })
function onMyPageChange(p: number) { myPage.value = p; loadMySubmissions() }
function onMySizeChange(ps: number) { myPageSize.value = ps; myPage.value = 1; loadMySubmissions() }
const pendingCount = computed(() => rows.value.filter(r => r.score == null).length)

const hmRows = computed<AssignmentRow[]>(() => {
  return submissions.value.map((item: UnknownRecord, idx: number) => {
    const sub: SubmissionData = (item as any).submission || {}
    const realName = (item as any).realName || ''
    const assignmentIdRaw = sub.assignmentId ?? (sub as { assignmentID?: unknown }).assignmentID ?? (sub as { assignment_id?: unknown }).assignment_id ?? (sub as { assignment?: unknown }).assignment
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
})
const studentsList = computed<string[]>(() => {
  const set = new Set<string>()
  hmRows.value.forEach(r => set.add(String(r.studentName || r.studentId)))
  return Array.from(set)
})
const assignmentsList = computed(() => {
  const teacherId = Number(currentUser.value?.id ?? 0)
  const selectedCourseId = Number(currentCourse.value?.id ?? 0)
  return overviewAssignments.value
    .filter((a: UnknownRecord) => {
      const pubId = Number((a as any)?.publisherUserId ?? 0)
      const cId = Number((a as any)?.courseId ?? 0)
      if (identity.isTeacher) return pubId === teacherId && cId === selectedCourseId
      return cId === selectedCourseId
    })
    .map((a: UnknownRecord) => ({ id: Number((a as any)?.id ?? 0), title: String((a as any)?.title ?? '') }))
    .filter(a => a.title)
})
const submissionsByStudent = computed<Record<string, AssignmentRow[]>>(() => {
  const map: Record<string, AssignmentRow[]> = {}
  hmRows.value.forEach(r => {
    const k = String(r.studentName || r.studentId)
    if (!map[k]) map[k] = []
    map[k].push(r)
  })
  return map
})
function findSubmission(student: string, assignmentTitle: string, assignmentId?: number): AssignmentRow | undefined {
  const list = submissionsByStudent.value[student] || []
  const t = String(assignmentTitle).trim()
  const byId = list.find(r => Number(r.assignmentId || 0) === Number(assignmentId || 0))
  if (byId) return byId
  return list.find(r => String(r.assignmentName || '').includes(t))
}
function statusOf(student: string, assignmentTitle: string, assignmentId?: number) {
  const r = findSubmission(student, assignmentTitle, assignmentId)
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
}
function cellClass(code: string) { return code === 'none' ? 'hm-none' : code === 'pending' ? 'hm-pending' : `hm-${code}` }
function cellTitle(student: string, assignmentTitle: string, assignmentId?: number) {
  const r = findSubmission(student, assignmentTitle, assignmentId)
  if (!r) return `${student} / ${assignmentTitle}：未提交`
  const st = String(r.status) === '2' ? '已批改' : '已提交未批改'
  const sc = r.score == null ? '-' : String(r.score)
  const file = String(r.assignmentName || '')
  return `${student} / ${assignmentTitle}：${st}（文件：${file}，分数：${sc}）`
}
const CELL = 28
const hmStudentsStyle = computed(() => ({ gridTemplateColumns: `repeat(${studentsList.value.length}, ${CELL}px)` }))
const hmCellsStyle = computed(() => ({ gridTemplateColumns: `repeat(${studentsList.value.length}, ${CELL}px)` }))

const donutNone = computed(() => {
  let n = 0
  assignmentsList.value.forEach(a => studentsList.value.forEach(s => { if (statusOf(s, a.title, a.id) === 'none') n++ }))
  return n
})
const donutPending = computed(() => {
  let n = 0
  assignmentsList.value.forEach(a => studentsList.value.forEach(s => { if (statusOf(s, a.title, a.id) === 'pending') n++ }))
  return n
})
const donutGraded = computed(() => {
  let n = 0
  assignmentsList.value.forEach(a => studentsList.value.forEach(s => { if (statusOf(s, a.title, a.id).startsWith('graded')) n++ }))
  return n
})
const donutTotal = computed(() => studentsList.value.length * assignmentsList.value.length)
const donutSegments = computed(() => {
  const total = donutTotal.value || 1
  const C = 2 * Math.PI * 50
  const parts = [
    { value: donutNone.value, color: '#9aa3b2' },
    { value: donutPending.value, color: '#ffd166' },
    { value: donutGraded.value, color: '#06d6a0' }
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
})

const selectedStudent = ref<string | null>(null)
const overlayX = ref(100)
const overlayOpacity = ref(0)
const overlayStyle = computed<CSSProperties>(() => ({ position: 'absolute', left: '0', right: '0', top: '0', bottom: '0', zIndex: 999, background: '#ffffff', transform: `translateX(${overlayX.value}%)`, opacity: overlayOpacity.value, transition: 'transform 0.4s cubic-bezier(0.22, 1, 0.36, 1), opacity 0.4s ease' }))
function onStudentClick(s: string) {
  selectedStudent.value = s
  overlayX.value = 100
  overlayOpacity.value = 0
  nextTick(() => { overlayX.value = 0; overlayOpacity.value = 1 })
}
function closeOverlay() {
  overlayX.value = 100
  overlayOpacity.value = 0
  setTimeout(() => { selectedStudent.value = null }, 400)
}
watch(selectedStudent, async () => { await nextTick() })
const assignmentTitleMap = computed<Record<number, string>>(() => {
  const m: Record<number, string> = {}
  overviewAssignments.value.forEach((a: UnknownRecord) => {
    const id = Number((a as any)?.id ?? 0)
    if (Number.isFinite(id)) m[id] = String((a as any)?.title || '')
  })
  return m
})
const selectedStudentList = computed(() => {
  const name = selectedStudent.value
  if (!name) return []
  const list = submissionsByStudent.value[name] || []
  return list
    .filter(r => r.status === '1' || r.status === '2')
    .map(r => ({
      submissionId: r.submissionId,
      assignmentTitle: r.assignmentId ? (assignmentTitleMap.value[Number(r.assignmentId)] || r.assignmentName) : r.assignmentName,
      fileName: r.assignmentName,
      score: r.score,
      status: r.status
    }))
})
function scoreClass(s: number | null) {
  const v = Number(s ?? -1)
  if (v < 0) return 'score-unknown'
  if (v <= 30) return 'score-1'
  if (v <= 60) return 'score-2'
  if (v <= 80) return 'score-3'
  return 'score-4'
}
function fileIcon(name: string) {
  const ext = String(name || '').toLowerCase().split('.').pop() || ''
  if (ext === 'pdf') return 'icon-pdf'
  if (ext === 'doc' || ext === 'docx') return 'icon-word'
  if (ext === 'xls' || ext === 'xlsx') return 'icon-excel'
  if (ext === 'ppt' || ext === 'pptx') return 'icon-ppt'
  if (ext === 'txt') return 'icon-txt'
  return 'icon-file'
}

async function markGraded() {
  try {
    const res = await fetch('/api/ai-grading/submissions/mark-graded', { method: 'POST' })
    const json = await res.json()
    if (Number(json?.code) === 200) {
      await Promise.all([
        identity.isTeacher ? loadSubmissions() : Promise.resolve(),
        identity.isStudent ? loadMySubmissions() : Promise.resolve(),
        loadAssignments()
      ])
    }
  } catch (error) {
    console.error('标记批改状态失败', error)
  }
}

async function onMarkGraded() { await markGraded() }

function statusLabel(s: string) {
  if (String(s) === '2') return '已批改'
  if (String(s) === '1') return '未批改'
  return s
}
</script>

<style scoped>
.teacher-assignments-page {
  padding: 16px;
  background: #f5f7fa;
}

.view-tabs :deep(.el-tabs__header) {
  margin-bottom: 12px;
}

.view-tab-label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.assignments-page {
  min-height: 100%;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4ecf7 100%);
  border-radius: 12px;
  padding-bottom: 16px;
}

.page-header {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  padding: 25px 30px;
  margin: -20px -24px 25px -24px;
  color: white;
  box-shadow: 0 4px 20px rgba(17, 153, 142, 0.4);
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

.main-tabs {
  margin-top: 10px;
  padding: 0 24px 24px 24px;
}

.main-tabs :deep(.el-tabs__header) {
  margin-bottom: 20px;
}

.main-tabs :deep(.el-tabs__item) {
  font-size: 15px;
  padding: 0 25px;
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 8px;
}

.submissions-card {
  border-radius: 12px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header .title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 16px;
}

.submissions-table {
  border-radius: 8px;
  overflow: hidden;
}

.student-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.score-value {
  font-weight: 700;
  color: #409EFF;
  font-size: 16px;
}

.time-text {
  font-size: 13px;
  color: #909399;
}

.empty-submissions {
  padding: 40px 0;
}
</style>

<style scoped>
.assignments-view {
  --primary: #2f6df6;
  --muted-bg: #f5f7fb;
  --accent: #2ec4b6;
  --border: #e6e8ee;
  padding: 16px;
}

.status-bar { display: flex; align-items: center; gap: 8px; color: var(--primary); background: rgba(47, 109, 246, 0.06); border: 1px solid var(--border); border-radius: 8px; padding: 10px 12px; margin-bottom: 16px; }
.status-divider { color: #97a0b5 }
.subnav { display: flex; gap: 10px; margin: 8px 0 16px 0 }
.nav-pill { min-height: 40px; display: inline-flex; align-items: center; gap: 8px; padding: 8px 14px; border-radius: 999px; border: 1px solid rgba(47,84,150,0.45); color: #2F5496; background: rgba(47,84,150,0.06); cursor: pointer; transition: transform .2s ease, box-shadow .2s ease }
.nav-pill:hover { transform: translateY(-1px); box-shadow: 0 4px 12px rgba(0,0,0,0.08) }
.nav-pill.is-active { background: #2F5496; color: #fff; border-color: #2F5496 }
.count-badge { margin-left: 6px; background: #ffffff; color: #2F5496; border: 1px solid rgba(255,255,255,0.7); border-radius: 999px; padding: 2px 8px; font-size: 12px; font-weight: 700 }
.block-card { background: #fff; border-radius: 8px; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05); border: 1px solid var(--border); margin-bottom: 24px; }
.block-header { display: flex; align-items: center; justify-content: space-between; padding: 12px 16px; background: linear-gradient(180deg, rgba(0,0,0,0.02), rgba(0,0,0,0)); border-bottom: 1px solid var(--border); }
.block-title { font-size: 16px; font-weight: 600; color: #1f2a44; }
.block-icon { margin-right: 6px }
.block-actions { display: flex; align-items: center; gap: 8px }
.table-wrapper { padding: 12px 16px; overflow-x: auto }
.assignments-table { width: 100%; border-collapse: separate; border-spacing: 0 }
.assignments-table th, .assignments-table td { border-bottom: 1px solid var(--border); padding: 10px 12px; white-space: normal; word-break: break-word }
.assignments-table thead th { background: var(--muted-bg); color: #314160; font-weight: 600 }
.assignments-table tbody tr { transition: background-color 0.2s ease }
.assignments-table tbody tr:hover { background-color: rgba(49, 65, 96, 0.06) }
.col-actions { width: 120px; text-align: left }
.pagination { padding: 8px 16px }
.pagination :deep(.el-pager li.is-active) { background-color: var(--primary); color: #fff }
.pagination :deep(.el-pagination.is-background .el-pager li) { border-radius: 6px }
.emphasis-btn :deep(.el-button__inner), .emphasis-btn { background-color: var(--accent) !important; border-color: var(--accent) !important; color: #fff !important; border-radius: 4px; transition: transform 0.3s linear, box-shadow 0.3s linear }
.emphasis-btn:hover { transform: scale(1.03); box-shadow: 0 4px 12px rgba(46,196,182,0.3) }
.link-btn { background: transparent; border: none; color: var(--primary); text-decoration: underline; cursor: pointer; padding: 8px 10px; min-width: 40px; min-height: 40px }
.link-btn:hover { opacity: 0.85 }
.muted { color: #97a0b5; font-size: 13px }
.th-with-input { display: flex; align-items: center; gap: 8px }
.sort-icon { display: inline-block; width: 20px; text-align: center; color: #7a879c; cursor: pointer }
.table-input { border: 1px solid var(--border); border-radius: 6px; padding: 6px 8px; background: #fff; outline: none }
.table-input:focus { border-color: var(--primary) }
.decorative-divider { display: flex; align-items: center; justify-content: center; gap: 12px; height: 24px; margin: 4px 0 16px 0 }
.decorative-divider::before, .decorative-divider::after { content: ""; flex: 1; height: 1px; background: linear-gradient(90deg, transparent, var(--primary), transparent) }
.row-pending { background-color: rgba(46, 196, 182, 0.08) }
.status-icon { margin-right: 6px }
.skeleton-row td { padding: 12px 0 }
.skeleton { height: 16px; border-radius: 6px; background: linear-gradient(90deg, #eceff4 25%, #f6f7fb 37%, #eceff4 63%); background-size: 400% 100%; animation: shimmer 1.2s ease-in-out infinite }
@keyframes shimmer { 0% { background-position: 100% 0 } 100% { background-position: 0 0 } }
.slide-h-enter-active, .slide-h-leave-active { transition: transform 0.3s ease-out, opacity 0.3s ease-out }
.slide-h-enter-from, .slide-h-leave-to { transform: translateX(12px); opacity: 0 }
.charts-area { display: grid; grid-template-columns: 2fr 1fr; gap: 16px; margin: 12px 0 16px 0 }
.heatmap-card, .donut-card { background: #fff; border: 1.5px solid #d7dbe5; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.08) }
.donut-card { position: relative !important }
.chart-header { font-size: 16px; font-weight: 700; color: #1f2a44; padding: 10px 12px; border-bottom: 1px solid #e6e8ee; margin: -12px -12px 8px -12px; border-top-left-radius: 10px; border-top-right-radius: 10px; background: linear-gradient(180deg, #f8fafc, #ffffff) }
.heatmap-card { padding: 12px 12px; overflow: visible }
.hm-scroll { overflow-x: auto; overflow-y: hidden }
.hm-bottom { display: grid; grid-template-columns: 200px 1fr; align-items: start; margin-top: 6px }
.hm-bottom-spacer { width: 200px }
.hm-students { display: grid; gap: 4px; overflow: visible }
.hm-student { width: 28px; height: 40px; position: relative }
.hm-student-label { position: absolute; left: 50%; top: 4px; transform-origin: center top; transform: rotate(-45deg) translate(-50%, 0); font-size: 13px; white-space: nowrap; max-width: 160px; text-overflow: ellipsis; overflow: visible; color: #455a7a; text-align: center }
.hm-body { min-width: 100%; }
.hm-row { display: grid; grid-template-columns: 200px 1fr; align-items: center; gap: 10px; margin-bottom: 8px }
.hm-assignment { font-size: 13px; color: #314160; white-space: nowrap; overflow: hidden; text-overflow: ellipsis }
.hm-cells { display: grid; gap: 6px }
.hm-cell { width: 28px; height: 28px; border-radius: 5px; background: #eef2f7; border: 1px solid #e6e8ee }
.hm-none { background: #ffffff }
.hm-pending { background: #ffd166 }
.hm-graded-1 { background: #C7F9E5 }
.hm-graded-2 { background: #90F0D0 }
.hm-graded-3 { background: #4ED9B6 }
.hm-graded-4 { background: #06d6a0 }
.hm-legend { display: flex; align-items: center; gap: 10px; margin-top: 8px; color: #7a879c; font-size: 13px }
.legend-item { display: inline-flex; align-items: center; gap: 6px }
.legend-swatch { width: 14px; height: 14px; border-radius: 3px; display: inline-block; border: 1px solid #e6e8ee }
.donut-card { padding: 12px 12px; display: grid; grid-template-rows: auto auto; align-items: center; justify-items: center }
.donut-wrap { position: relative; width: 160px; height: 160px }
.donut { width: 160px; height: 160px; transform: rotate(-90deg) }
.donut-bg { stroke: #9aa3b2 }
.donut-seg { transition: stroke-dasharray 0.3s ease; stroke-linecap: butt }
.donut-center { position: absolute; left: 50%; top: 50%; transform: translate(-50%, -50%); text-align: center }
.donut-total { font-size: 22px; font-weight: 800; color: #1f2a44 }
.donut-label { font-size: 13px; color: #7a879c }
.donut-legend { width: 100%; display: flex; flex-direction: column; gap: 6px; margin-top: 8px }
.dl-item { display: flex; align-items: center; justify-content: space-between; gap: 8px; width: 100%; font-size: 13px; color: #314160 }
.dl-swatch { width: 14px; height: 14px; border-radius: 3px; display: inline-block }
.dl-none { background: #9aa3b2 }
.dl-pending { background: #ffd166 }
.dl-graded { background: #06d6a0 }
@media (max-width: 992px) { .charts-area { grid-template-columns: 1fr } }
@media (max-width: 768px) { .table-wrapper { overflow-x: hidden } .assignments-table { width: 100% } .block-actions { flex-wrap: wrap } }
.danger-btn :deep(.el-button__inner), .danger-btn { background-color: #e74c3c !important; border-color: #e74c3c !important; color: #fff !important; border-radius: 4px }
.hm-student-label.clickable { cursor: pointer }
.hm-student-label.clickable:hover { color: #2F5496; text-decoration: underline }
.donut-content { position: relative; overflow: hidden; width: 100%; display: flex; flex-direction: column; align-items: center; justify-content: center }
.student-overlay { position: absolute !important; left: 0; right: 0; top: 0; bottom: 0; width: 100%; background: #ffffff; border-left: 1px solid var(--border); box-shadow: 0 2px 5px rgba(0,0,0,0.08); padding: 15px; display: grid; grid-template-rows: auto 1fr; z-index: 20; border-radius: 10px }
.overlay-title { font-size: 16px; font-weight: 700; color: #1f2a44; text-align: center; margin-bottom: 8px }
.overlay-title-icon { margin-right: 8px; font-size: 18px }
.overlay-deco { height: 2px; background: #2f6df6; width: 120px; margin: 0 auto 8px auto; border-radius: 2px }
.overlay-close { position: absolute; right: 10px; top: 8px; border: 1px solid #d7dbe5; background: #fff; color: #314160; width: 28px; height: 28px; border-radius: 6px; cursor: pointer }
.overlay-close:hover { background: #f3f6fb }
.overlay-content { position: relative; height: 100%; background: linear-gradient(180deg, rgba(249,251,255,0.9), rgba(255,255,255,0.96)); backdrop-filter: blur(6px); border: 1px solid #e6e8ee; border-radius: 10px; box-shadow: 0 8px 24px rgba(31,42,68,0.06); padding: 15px; display: grid; grid-template-rows: auto 1fr; gap: 10px; animation: cardFade 0.3s ease both; box-sizing: border-box }
.overlay-content::before { content: ""; position: absolute; right: 24px; top: 10px; width: 120px; height: 120px; background: radial-gradient(closest-side, rgba(47,109,246,0.08), transparent); filter: blur(12px); border-radius: 50% }
.overlay-content::after { content: ""; position: absolute; left: 24px; bottom: 12px; width: 160px; height: 2px; background: linear-gradient(90deg, rgba(47,109,246,0.15), rgba(46,196,182,0.15)); border-radius: 2px }
.overlay-list { display: flex; flex-direction: column; gap: 10px; overflow: auto; padding: 8px 0 }
.ov-card { border: 1px solid #e6e8ee; border-radius: 12px; background: linear-gradient(180deg, #ffffff, #fbfcff); box-shadow: 0 6px 18px rgba(31,42,68,0.06); padding: 12px 14px; transition: box-shadow 0.2s ease, transform 0.2s ease, background-color 0.2s ease; animation: rowFadeUp 0.25s ease both }
.ov-card:hover { box-shadow: 0 8px 24px rgba(31,42,68,0.09); transform: translateY(-1px) }
.ov-card.row-graded { border-left: 2px solid #2ec4b6 }
.ov-card.row-pending { border-left: 2px solid #ffd166 }
.ov-head { display: grid; grid-template-columns: 1fr auto; align-items: center; gap: 8px; margin-bottom: 8px }
.ov-title { font-size: 14px; color: #314160; font-weight: 700; letter-spacing: 0.2px }
.ov-score-badge { min-width: 44px; text-align: center; padding: 4px 10px; border-radius: 14px; font-size: 14px; font-weight: 700; background: rgba(47,109,246,0.08); color: #2f6df6; border: 1px solid rgba(47,109,246,0.18) }
.ov-body { display: grid; grid-template-columns: 1fr auto; align-items: center; gap: 10px }
.ov-file { display: inline-flex; align-items: center; gap: 8px; font-size: 13px; color: #586a88 }
.file-name { max-width: 100%; white-space: nowrap; overflow: hidden; text-overflow: ellipsis }
.ov-status { font-size: 14px; color: #7a879c; text-align: right }
.score-1 { color: #2C7C75 }
.score-2 { color: #1F6E62 }
.score-3 { color: #16785E }
.score-4 { color: #0E8E58 }
.score-unknown { color: #9aa3b2 }
.ov-score-badge.score-1 { background: rgba(46, 196, 182, 0.12); color: #16785E; border-color: rgba(46,196,182,0.3) }
.ov-score-badge.score-2 { background: rgba(46, 196, 182, 0.12); color: #1F6E62; border-color: rgba(46,196,182,0.3) }
.ov-score-badge.score-3 { background: rgba(46, 196, 182, 0.12); color: #16785E; border-color: rgba(46,196,182,0.3) }
.ov-score-badge.score-4 { background: rgba(46, 196, 182, 0.18); color: #0E8E58; border-color: rgba(46,196,182,0.3) }
.ov-score-badge.score-unknown { background: rgba(154,163,178,0.15); color: #7a879c; border-color: rgba(154,163,178,0.25) }
.st-badge { display: inline-flex; align-items: center; gap: 6px; padding: 2px 6px; border-radius: 4px; font-size: 13px }
.st-graded { background: rgba(46, 196, 182, 0.12); color: #16785E }
.st-pending { background: rgba(255, 209, 102, 0.18); color: #9c6e00 }
.st-icon { font-size: 12px; line-height: 1 }
.file-icon { width: 14px; height: 14px; border-radius: 3px; display: inline-block }
.icon-file { background: #b0b8c5 }
.icon-word { background: #2f6df6 }
.icon-pdf { background: #e63946 }
.icon-excel { background: #2ec4b6 }
.icon-ppt { background: #ff7f50 }
.icon-txt { background: #7a879c }
.ov-empty { font-size: 13px; color: #9aa3b2; text-align: center; padding: 12px }
@keyframes cardFade { from { opacity: 0; transform: translateY(-6px) } to { opacity: 1; transform: translateY(0) } }
@keyframes rowFadeUp { from { opacity: 0; transform: translateY(8px) } to { opacity: 1; transform: translateY(0) } }
:deep(.overlay-slide-enter-active), :deep(.overlay-slide-leave-active) { transition: transform 0.4s cubic-bezier(0.22, 1, 0.36, 1), opacity 0.4s ease }
:deep(.overlay-slide-enter-from) { transform: translateX(100%); opacity: 0 }
:deep(.overlay-slide-enter-to) { transform: translateX(0); opacity: 1 }
:deep(.overlay-slide-leave-from) { transform: translateX(0); opacity: 1 }
:deep(.overlay-slide-leave-to) { transform: translateX(100%); opacity: 0 }
.hm-cell.is-selected { outline: 2px solid #2F5496; border-color: #2F5496 }
@media (max-width: 1440px) { .overlay-title { font-size: 15px } .ov-title { font-size: 13px } .ov-card { padding: 10px 12px } .ov-score-badge { font-size: 11px; padding: 2px 8px } .st-badge { font-size: 11px; padding: 2px 8px } }
@media (max-width: 1200px) { .overlay-content { padding: 10px 12px } .file-name { max-width: 90% } }
@media (max-width: 992px) { .overlay-content { padding: 8px 10px; border-radius: 8px } .ov-card { padding: 8px 10px } .overlay-title { font-size: 14px } .ov-title { font-size: 12px } .ov-file { font-size: 12px } .ov-score-badge { font-size: 11px } .st-badge { font-size: 11px } }
@media (max-width: 768px) { .overlay-content { padding: 6px 8px } .ov-card { padding: 6px 8px } .overlay-title-icon { font-size: 18px } }
</style>
function getSortValue(row: any, prop: string) {
  if (prop === 'id') return Number(row?.submission?.id ?? 0)
  if (prop === 'student') return String(row?.realName ?? row?.submission?.studentUserId ?? '').toLowerCase()
  if (prop === 'title') return String(row?.assignmentTitle ?? ('作业 ' + (row?.submission?.assignmentId ?? ''))).toLowerCase()
  if (prop === 'score') return Number(row?.submission?.score ?? -1)
  if (prop === 'status') return String(row?.submission?.status ?? '')
  if (prop === 'submitTime') {
    const v = row?.submission?.submitTime
    const ts = v ? Number(new Date(v).getTime()) : 0
    return ts
  }
  return ''
}

function onSubmissionsSortChange({ prop, order }: { prop: string; order: 'ascending' | 'descending' | null }) {
  if (!prop || !order) { submissions.value = [...submissionsBackup.value]; return }
  const dir = order === 'ascending' ? 1 : -1
  submissions.value = [...submissions.value].sort((a, b) => {
    const va = getSortValue(a, prop)
    const vb = getSortValue(b, prop)
    if (typeof va === 'number' && typeof vb === 'number') return (va - vb) * dir
    return String(va).localeCompare(String(vb)) * dir
  })
}
