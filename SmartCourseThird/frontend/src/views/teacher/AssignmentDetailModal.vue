<template>
  <div>
    <div v-if="inline" class="grading-page">
      <div class="grading-page-header">
        <div class="header-leading">
          <nav class="breadcrumb">
            <a class="bc-link" href="#">作业列表</a>
            <span class="sep">›</span>
            <a class="bc-link" href="#">已提交作业</a>
            <span class="sep">›</span>
            <span class="bc-current">批改详情</span>
          </nav>
          <div class="title">批改详情</div>
        </div>
        <div class="header-actions">
          <el-button class="rect-ghost" @click="emit('close')">返回列表</el-button>
          <el-input v-model="teacherId" placeholder="教师ID" style="width: 140px" />
          <el-button class="rect-primary" type="primary" @click="triggerAiGrading">AI 批改</el-button>
          <el-button class="rect-ghost" @click="toggleTeacherEdit">{{ teacherEditing ? '取消修改' : '教师修改' }}</el-button>
          <el-button class="rect-success" type="success" @click="confirmTeacher">确认批改</el-button>
        </div>
      </div>
      <div class="grading-layout">
        <div class="main-pane">
          <div class="card grading-card module block-primary">
            <div class="card-title with-icon"><span class="icon">⭐</span><span>批改结果</span></div>
            <div class="score-cards">
              <div class="score-card">
                <div class="score-name">内容分</div>
                <div class="score-value" v-if="!teacherEditing">{{ edit.contentScore }}</div>
                <el-input-number v-else v-model="edit.contentScore" :min="0" :max="100" />
              </div>
              <div class="score-card">
                <div class="score-name">逻辑分</div>
                <div class="score-value" v-if="!teacherEditing">{{ edit.logicScore }}</div>
                <el-input-number v-else v-model="edit.logicScore" :min="0" :max="100" />
              </div>
              <div class="score-card">
                <div class="score-name">知识分</div>
                <div class="score-value" v-if="!teacherEditing">{{ edit.knowledgeScore }}</div>
                <el-input-number v-else v-model="edit.knowledgeScore" :min="0" :max="100" />
              </div>
              <div class="score-card">
                <div class="score-name">创新分</div>
                <div class="score-value" v-if="!teacherEditing">{{ edit.innovationScore }}</div>
                <el-input-number v-else v-model="edit.innovationScore" :min="0" :max="100" />
              </div>
              <div class="score-card is-total">
                <div class="score-name">总分</div>
                <div class="score-value" v-if="!teacherEditing">{{ edit.totalScore }}</div>
                <el-input-number v-else v-model="edit.totalScore" :min="0" :max="100" />
              </div>
            </div>
          </div>

          <div class="card module block-secondary">
            <div class="card-title with-icon"><span class="icon">💬</span><span>AI 评语</span></div>
            <div class="module-body" :class="teacherEditing ? '' : 'highlight'">
              <el-input v-if="teacherEditing" v-model="edit.aiComment" type="textarea" :rows="4" />
              <div v-else>{{ edit.aiComment || '—' }}</div>
            </div>
          </div>
          <div class="card module block-accent">
            <div class="card-title with-icon"><span class="icon">✏️</span><span>改进建议</span></div>
            <div class="module-body">
              <template v-if="teacherEditing">
                <el-input v-model="edit.improvementSuggestions" type="textarea" :rows="6" />
              </template>
              <template v-else>
                <div v-if="improvementList.length > 0" class="improve-list">
                  <div v-for="(it, idx) in improvementList" :key="idx" class="improve-item" :style="{ animationDelay: (idx*0.2)+'s' }">
                    <div class="improve-row"><span class="improve-label">知识点</span><span class="improve-kp" :class="{ glow: improveGlow[idx] }" @click="flashImprove(idx)">{{ it.kp }}</span></div>
                    <div class="improve-row"><span class="improve-label">建议</span><span class="improve-text">{{ it.suggestion }}</span></div>
                    <div class="improve-row" v-if="it.resource"><span class="improve-label">资源</span><a class="improve-link" :href="it.resource" target="_blank">{{ it.resource }}</a></div>
                  </div>
                </div>
                <div v-else>{{ edit.improvementSuggestions || '—' }}</div>
              </template>
            </div>
          </div>
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

          <div class="card history-card">
            <div class="card-title">{{ historyTitle }}</div>
            <div class="history-list">
              <div v-for="h in filteredHistory" :key="h.id" class="history-item">
                <div class="history-header" @click="toggle(h.id)">{{ h.createdAt || h.updatedAt || '' }}</div>
                <div v-if="open[h.id]" class="history-body">
                  <div class="score-grid">
                    <div class="score-item"><div class="label">内容</div><div class="value">{{ h.contentScore }}</div></div>
                    <div class="score-item"><div class="label">逻辑</div><div class="value">{{ h.logicScore }}</div></div>
                    <div class="score-item"><div class="label">知识</div><div class="value">{{ h.knowledgeScore }}</div></div>
                    <div class="score-item"><div class="label">创新</div><div class="value">{{ h.innovationScore }}</div></div>
                  </div>
                  <div class="total-row"><div class="label">总分</div><div class="value">{{ h.totalScore }}</div></div>
                  <div class="field"><div class="label">AI 评语</div><div class="value">{{ h.aiComment }}</div></div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="side-pane">
          <div class="card info-card">
            <div class="card-title">作业信息</div>
            <div class="info-grid">
              <div class="field"><div class="label">作业名</div><div class="value">{{ assignment.title || '' }}</div></div>
              <div class="field"><div class="label">学生</div><div class="value">{{ realName }}</div></div>
              <div class="field"><div class="label">提交时间</div><div class="value">{{ submission.submitTime || '' }}</div></div>
              <div class="field"><div class="label">提交文件</div>
                <div class="value">
                  <a v-if="submission.filePath" class="download-link" :href="fileHref(String(submission.filePath))" target="_blank" :download="submission.fileName || ''">{{ submission.fileName || submission.filePath }}</a>
                  <span v-else class="muted">暂无附件</span>
                </div>
              </div>
            </div>
          </div>
          <div class="card kp-card module">
            <div class="card-title">关联知识点</div>
            <ul class="kp-list">
              <li v-for="kp in knowledgePoints" :key="kp.id" :class="{ glow: kpGlow[kp.id] }" @click="glowKp(kp.id)" tabindex="0">{{ kp.title }}</li>
              <li v-if="!knowledgePoints || knowledgePoints.length===0" class="muted">暂无关联知识点</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
    <div v-else-if="visible" class="modal-mask" @click.self="emit('close')">
      <div class="modal-wrap">
        <div class="modal-card">
          <button class="modal-close" @click="emit('close')">×</button>
          <div class="modal-columns">
            <div class="modal-left">
              <div class="section-title">作业信息</div>
              <div class="field"><div class="label">作业名</div><div class="value">{{ assignment.title || '' }}</div></div>
              <div class="field"><div class="label">发布人</div><div class="value">{{ assignment.publisherRealName || assignment.publisherUserId || '' }}</div></div>
              <div class="field"><div class="label">发布时间</div><div class="value">{{ assignment.startTime || assignment.createTime || '' }}</div></div>
              <div class="field"><div class="label">作业要求</div><div class="value">{{ assignment.description || '' }}</div></div>
              <div class="field"><div class="label">覆盖的知识点</div>
                <div class="value">
                  <ul>
                    <li v-for="kp in knowledgePoints" :key="kp.id">{{ kp.title }}</li>
                  </ul>
                </div>
              </div>
              <div class="field"><div class="label">作业附件</div>
                <div class="value">
                  <span v-if="assignmentAttachments.length===0" class="muted">暂无附件</span>
                  <ul v-else class="attach-list">
                    <li v-for="(a, i) in assignmentAttachments" :key="i" class="attach-item">
                      <a v-if="attachmentHref(a)" :href="attachmentHref(a)" target="_blank" :download="attachmentDownloadName(a)" class="download-link">{{ attachmentText(a) }}</a>
                      <span v-else>{{ attachmentText(a) }}</span>
                    </li>
                  </ul>
                </div>
              </div>
            </div>
            <div class="modal-middle">
              <div class="section-title">学生与评分</div>
              <div class="field"><div class="label">学生名</div><div class="value">{{ realName }}</div></div>
              <div class="field"><div class="label">学生提交的作业名</div><div class="value">{{ submission.fileName || '' }}</div></div>
              <div class="field"><div class="label">提交时间</div><div class="value">{{ submission.submitTime || '' }}</div></div>
              <div class="field"><div class="label">学生附件</div>
                <div class="value">
                  <a v-if="submission.filePath" class="download-link" :href="fileHref(String(submission.filePath))" target="_blank" :download="submission.fileName || ''">{{ submission.fileName || submission.filePath }}</a>
                  <span v-else class="muted">暂无附件</span>
                </div>
              </div>
              <div class="edit-block">
                <div class="score-row">
                  <div class="score-item">
                    <div class="label">总得分</div>
                    <el-input-number v-model="edit.totalScore" :min="0" :max="100" :disabled="!teacherEditing" />
                  </div>
                </div>
                <div class="score-grid">
                  <div class="score-item"><div class="label">内容</div><el-input-number v-model="edit.contentScore" :min="0" :max="100" :disabled="!teacherEditing" /></div>
                  <div class="score-item"><div class="label">逻辑</div><el-input-number v-model="edit.logicScore" :min="0" :max="100" :disabled="!teacherEditing" /></div>
                  <div class="score-item"><div class="label">知识</div><el-input-number v-model="edit.knowledgeScore" :min="0" :max="100" :disabled="!teacherEditing" /></div>
                  <div class="score-item"><div class="label">创新</div><el-input-number v-model="edit.innovationScore" :min="0" :max="100" :disabled="!teacherEditing" /></div>
                </div>
                <div class="field"><div class="label">AI 评语</div><el-input v-model="edit.aiComment" type="textarea" rows="3" :disabled="!teacherEditing" /></div>
                <div class="field"><div class="label">覆盖的知识点</div><el-input v-model="edit.coveredKnowledgePoints" type="textarea" rows="3" :disabled="!teacherEditing" /></div>
                <div class="field"><div class="label">未覆盖的知识点</div><el-input v-model="edit.missingKnowledgePoints" type="textarea" rows="3" :disabled="!teacherEditing" /></div>
                <div class="field"><div class="label">AI 改进建议</div><el-input v-model="edit.improvementSuggestions" type="textarea" rows="3" :disabled="!teacherEditing" /></div>
              </div>
              <div class="modal-fixed-actions">
                <div class="actions-left">
                  <el-input v-model="teacherId" placeholder="教师ID" style="width: 140px" />
                </div>
                <div class="actions-right">
                  <el-button class="rect-primary" type="primary" @click="triggerAiGrading">AI 批改</el-button>
                  <el-button class="rect-ghost" @click="toggleTeacherEdit">{{ teacherEditing ? '取消修改' : '教师修改' }}</el-button>
                  <el-button class="rect-success" type="success" @click="confirmTeacher">确认批改</el-button>
                </div>
              </div>
            </div>
            <div class="modal-right">
              <div class="section-title">{{ historyTitle }}</div>
              <div class="history-list">
                <div v-for="h in filteredHistory" :key="h.id" class="history-item">
                  <div class="history-header" @click="toggle(h.id)">{{ h.createdAt || h.updatedAt || '' }}</div>
                  <div v-if="open[h.id]" class="history-body">
                    <div class="score-row"><div class="label">总得分</div><div class="value">{{ h.totalScore }}</div></div>
                    <div class="score-grid">
                      <div class="score-item"><div class="label">内容</div><div class="value">{{ h.contentScore }}</div></div>
                      <div class="score-item"><div class="label">逻辑</div><div class="value">{{ h.logicScore }}</div></div>
                      <div class="score-item"><div class="label">知识</div><div class="value">{{ h.knowledgeScore }}</div></div>
                      <div class="score-item"><div class="label">创新</div><div class="value">{{ h.innovationScore }}</div></div>
                    </div>
                    <div class="field"><div class="label">AI 评语</div><div class="value">{{ h.aiComment }}</div></div>
                    <div class="field"><div class="label">覆盖的知识点</div><div class="value">{{ h.coveredKnowledgePoints }}</div></div>
                    <div class="field"><div class="label">未覆盖的知识点</div><div class="value">{{ h.missingKnowledgePoints }}</div></div>
                    <div class="field"><div class="label">AI 改进建议</div><div class="value">{{ h.improvementSuggestions }}</div></div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch, onMounted, computed } from 'vue'
import { BASE_URL } from '../../utils/api'
import { ElMessage } from 'element-plus'
const props = defineProps<{ visible: boolean; submissionId: number | null; inline?: boolean }>()
const emit = defineEmits<{ (e: 'close'): void; (e: 'mark-graded'): void }>()

const loading = ref(false)
const submission = reactive<any>({})
const assignment = reactive<any>({})
const knowledgePoints = ref<any[]>([])
const realName = ref('')
const grading = reactive<any>({})
const edit = reactive({
  totalScore: 0,
  contentScore: 0,
  logicScore: 0,
  knowledgeScore: 0,
  innovationScore: 0,
  aiComment: '',
  improvementSuggestions: '',
  coveredKnowledgePoints: '',
  missingKnowledgePoints: ''
})

const assignmentAttachments = computed<Record<string, unknown>[]>(() => {
  const v = assignment?.attachments as unknown
  if (Array.isArray(v)) return v as Record<string, unknown>[]
  if (typeof v === 'string') {
    const s = String(v).trim()
    try {
      const j = JSON.parse(s)
      if (Array.isArray(j)) return j as Record<string, unknown>[]
    } catch {}
    if (s) {
      return s
        .replace(/^\s*\[|\]\s*$/g, '')
        .split(/[,，]/)
        .map(t => ({ name: t.trim() }))
        .filter(a => a.name)
    }
  }
  return []
})
function attachmentText(a: Record<string, unknown>) {
  const name = String((a as any)?.name || (a as any)?.title || '')
  const path = String((a as any)?.path || (a as any)?.filePath || (a as any)?.location || '')
  const url = String((a as any)?.url || (a as any)?.fileUrl || '')
  return name || path || url || ''
}
function attachmentHref(a: Record<string, unknown>) {
  const url = String((a as any)?.url || (a as any)?.fileUrl || '')
  const path = String((a as any)?.path || (a as any)?.filePath || '')
  if (url) {
    if (/^https?:\/\//i.test(url)) return url
    if (url.startsWith('/')) return `${BASE_URL}${url}`
    return url
  }
  if (path) {
    if (/^https?:\/\//i.test(path)) return path
    if (path.startsWith('/')) return `${BASE_URL}${path}`
    return path
  }
  return ''
}
function attachmentDownloadName(a: Record<string, unknown>) {
  return String((a as any)?.name || (a as any)?.title || '')
}
function fileHref(u: string) {
  const s = String(u || '')
  if (!s) return ''
  if (/^https?:\/\//i.test(s)) return s
  if (s.startsWith('/')) return `${BASE_URL}${s}`
  return s
}
const teacherId = ref('')
const teacherEditing = ref(false)
const originalEdit = reactive({
  totalScore: 0,
  contentScore: 0,
  logicScore: 0,
  knowledgeScore: 0,
  innovationScore: 0,
  aiComment: '',
  improvementSuggestions: '',
  coveredKnowledgePoints: '',
  missingKnowledgePoints: ''
})
const history = ref<any[]>([])
const open = reactive<Record<number, boolean>>({})
function toggle(id: number) { open[id] = !open[id] }
const filteredHistory = computed(() => history.value.filter(h => String(h.status) !== 'failed'))
const historyTitle = ref('批改历史')
let pollTimer: any = null
const llmModel = ref('Qwen/Qwen3-8B')

interface ImproveItem { kp: string; suggestion: string; resource?: string }
const improvementList = computed<ImproveItem[]>(() => {
  const raw = String(edit.improvementSuggestions || '')
  try {
    const j = JSON.parse(raw)
    const arr = Array.isArray(j) ? j : Array.isArray(j?.items) ? j.items : []
    return arr.map((it: any) => ({
      kp: String(it.kp || it.knowledgePoint || ''),
      suggestion: String(it.suggestion || it.advice || ''),
      resource: it.resource ? String(it.resource) : ''
    })).filter((x: ImproveItem) => x.kp || x.suggestion || x.resource)
  } catch { return [] }
})
const kpGlow = reactive<Record<number, boolean>>({})
function glowKp(id: number) {
  kpGlow[id] = true
  setTimeout(() => { kpGlow[id] = false }, 1000)
}
const improveGlow = reactive<Record<number, boolean>>({})
function flashImprove(idx: number) {
  improveGlow[idx] = true
  setTimeout(() => { improveGlow[idx] = false }, 1000)
}
function parseTags(s: string): string[] {
  return String(s || '')
    .split(/[\n,;，、]/)
    .map(t => t.trim())
    .filter(Boolean)
}
function toStringArray(x: unknown): string[] {
  if (Array.isArray(x)) return x.map(s => String(s || '').trim()).filter(Boolean)
  const s = String(x || '')
  try {
    const j = JSON.parse(s)
    if (Array.isArray(j)) return j.map(v => String(v || '').trim()).filter(Boolean)
  } catch {}
  return s
    .split(/[\n,;，、]/)
    .map(t => t.replace(/^\s*"|"\s*$/g, '').trim())
    .filter(Boolean)
}
const coveredTags = computed<string[]>(() => toStringArray(edit.coveredKnowledgePoints as any))
const missingTags = computed<string[]>(() => toStringArray(edit.missingKnowledgePoints as any))

function applyLatestValid() {
  const latestCompleted = [...history.value].filter(h => String(h.status) === 'completed').slice(-1)[0]
  const g = latestCompleted || grading
  edit.totalScore = g?.totalScore ?? 0
  edit.contentScore = g?.contentScore ?? 0
  edit.logicScore = g?.logicScore ?? 0
  edit.knowledgeScore = g?.knowledgeScore ?? 0
  edit.innovationScore = g?.innovationScore ?? 0
  edit.aiComment = g?.aiComment ?? ''
  edit.improvementSuggestions = g?.improvementSuggestions ?? ''
  edit.coveredKnowledgePoints = g?.coveredKnowledgePoints ?? ''
  edit.missingKnowledgePoints = g?.missingKnowledgePoints ?? ''
}

function snapshotEdit() {
  originalEdit.totalScore = edit.totalScore
  originalEdit.contentScore = edit.contentScore
  originalEdit.logicScore = edit.logicScore
  originalEdit.knowledgeScore = edit.knowledgeScore
  originalEdit.innovationScore = edit.innovationScore
  originalEdit.aiComment = edit.aiComment
  originalEdit.improvementSuggestions = edit.improvementSuggestions
  originalEdit.coveredKnowledgePoints = edit.coveredKnowledgePoints
  originalEdit.missingKnowledgePoints = edit.missingKnowledgePoints
}

function restoreEdit() {
  edit.totalScore = originalEdit.totalScore
  edit.contentScore = originalEdit.contentScore
  edit.logicScore = originalEdit.logicScore
  edit.knowledgeScore = originalEdit.knowledgeScore
  edit.innovationScore = originalEdit.innovationScore
  edit.aiComment = originalEdit.aiComment
  edit.improvementSuggestions = originalEdit.improvementSuggestions
  edit.coveredKnowledgePoints = originalEdit.coveredKnowledgePoints
  edit.missingKnowledgePoints = originalEdit.missingKnowledgePoints
}

function toggleTeacherEdit() {
  if (!teacherEditing.value) { snapshotEdit(); teacherEditing.value = true }
  else { restoreEdit(); teacherEditing.value = false }
}

async function loadDetail() {
  if (!props.submissionId) return
  loading.value = true
  try {
    const res = await fetch(`/api/ai-grading/submissions/${props.submissionId}`)
    const json = await res.json()
    const d = json?.data || {}
    Object.assign(submission, d.submission || {})
    Object.assign(assignment, d.assignment || {})
    knowledgePoints.value = Array.isArray(d.knowledgePoints) ? d.knowledgePoints : []
    realName.value = d.realName || ''
    Object.assign(grading, d.grading || {})
    edit.totalScore = grading.totalScore ?? 0
    edit.contentScore = grading.contentScore ?? 0
    edit.logicScore = grading.logicScore ?? 0
    edit.knowledgeScore = grading.knowledgeScore ?? 0
    edit.innovationScore = grading.innovationScore ?? 0
    edit.aiComment = grading.aiComment ?? ''
    edit.improvementSuggestions = grading.improvementSuggestions ?? ''
    edit.coveredKnowledgePoints = grading.coveredKnowledgePoints ?? ''
    edit.missingKnowledgePoints = grading.missingKnowledgePoints ?? ''
  } catch (e) { ElMessage.error('加载详情失败') }
  finally { loading.value = false }
}

async function loadHistory() {
  if (!props.submissionId) return
  try {
    const res = await fetch(`/api/ai-grading/grading/${props.submissionId}/history`)
    const json = await res.json()
    const arr = Array.isArray(json?.data) ? json.data : []
    history.value = arr
    arr.forEach((it: any) => { open[it.id] = false })
    applyLatestValid()
  } catch (e) {}
}

async function triggerAiGrading() {
  if (!props.submissionId) return
  try {
    const res = await fetch('/api/ai-grading/grading/trigger', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ submissionId: props.submissionId, llmModel: llmModel.value }) })
    await res.json()
    ElMessage.success('已触发 AI 批改')
    historyTitle.value = '批改历史(批改中...)'
    if (pollTimer) { clearInterval(pollTimer); pollTimer = null }
    pollTimer = setInterval(async () => {
      try {
        const r = await fetch(`/api/ai-grading/submissions/${props.submissionId}`)
        if (!r.ok) {
          clearInterval(pollTimer)
          pollTimer = null
          historyTitle.value = '批改历史(失败)'
          let msg = ''
          try { const jj = await r.json(); msg = String(jj?.message || '') } catch {}
          if (msg) ElMessage.error(msg)
          await loadDetail()
          await loadHistory()
          return
        }
        const j = await r.json()
        const code = Number(j?.code ?? 200)
        if (code !== 200) {
          clearInterval(pollTimer)
          pollTimer = null
          historyTitle.value = '批改历史(失败)'
          const msg = String(j?.message || '')
          if (msg) ElMessage.error(msg)
          await loadDetail()
          await loadHistory()
          return
        }
        const d = j?.data || {}
        const s = String(d?.grading?.status || '')
        const err = String(d?.grading?.errorMessage || '')
        if (s === 'completed' || s === 'failed' || err) {
          clearInterval(pollTimer)
          pollTimer = null
          historyTitle.value = s === 'completed' ? '批改历史' : '批改历史(失败)'
          if (err) ElMessage.error(err)
          await loadDetail()
          await loadHistory()
          emit('mark-graded')
        }
      } catch {
        clearInterval(pollTimer)
        pollTimer = null
        historyTitle.value = '批改历史(错误)'
        ElMessage.error('网络或服务器错误')
      }
    }, 2000)
  } catch (e) { ElMessage.error('触发失败') }
}

async function confirmTeacher() {
  if (!props.submissionId) return
  teacherEditing.value = false
  const tid = String(teacherId.value || '')
  try {
    const res = await fetch(`/api/ai-grading/grading/${props.submissionId}/confirm?teacherId=${encodeURIComponent(tid)}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        contentScore: edit.contentScore,
        logicScore: edit.logicScore,
        knowledgeScore: edit.knowledgeScore,
        innovationScore: edit.innovationScore,
        totalScore: edit.totalScore,
        aiComment: edit.aiComment,
        improvementSuggestions: edit.improvementSuggestions,
        coveredKnowledgePoints: edit.coveredKnowledgePoints,
        missingKnowledgePoints: edit.missingKnowledgePoints
      })
    })
    await res.json()
    ElMessage.success('已确认批改')
    await loadDetail()
    await loadHistory()
  } catch (e) { ElMessage.error('确认失败') }
}

watch(() => props.submissionId, async (id) => {
  if (id && props.visible) { await loadDetail(); await loadHistory() }
})
watch(() => props.visible, (v) => {
  if (!v) {
    if (pollTimer) { clearInterval(pollTimer); pollTimer = null }
    historyTitle.value = '批改历史'
  }
})
onMounted(async () => { if (props.submissionId && props.visible) { await loadDetail(); await loadHistory() } })
</script>

<style scoped>
.grading-page { padding: 8px 0 }
.grading-page-header { display: flex; align-items: center; justify-content: space-between; padding: 10px 14px }
.header-leading { display: flex; flex-direction: column; gap: 4px }
.breadcrumb { display: inline-flex; align-items: center; gap: 6px; color: #2F5496 }
.bc-link { color: #2F5496; padding: 6px 8px; border-radius: 6px; text-decoration: none }
.bc-link:hover { background: rgba(47,84,150,0.08) }
.sep { color: #97a0b5 }
.bc-current { color: #314160; font-weight: 700 }
.grading-page-header .title { font-size: 20px; font-weight: 700; color: #1f2a44 }
.grading-layout { display: grid; grid-template-columns: 4fr 1fr; gap: 20px; }
.card { background: #fff; border: 1px solid #e5e7eb; border-radius: 12px; padding: 14px 16px; box-shadow: 0 2px 8px rgba(0,0,0,0.04); }
.card-title { font-size: 16px; font-weight: 700; color: #1f2a44; padding-bottom: 12px; border-bottom: 1px solid #e5e7eb; margin-bottom: 12px; display: flex; align-items: center; gap: 6px }
.card.with-icon { position: relative }
.card-title.with-icon .icon { margin-right: 6px }
.grading-card { border: 1px solid #2F5496; background-image: radial-gradient(rgba(47,84,150,0.05) 1px, transparent 1px); background-size: 12px 12px }
.score-cards { display: grid; grid-template-columns: repeat(5, 1fr); gap: 10px }
.score-card { background: #ffffff; border: 1px solid #e5e7eb; border-radius: 8px; padding: 10px 8px; display: grid; grid-template-rows: 1fr 1fr; align-items: center; min-height: 72px }
.score-card .score-name { text-align: center; font-size: 16px; font-weight: 700; color: #1f2a44; margin-bottom: 4px }
.score-card .score-value { text-align: center; font-size: 28px; font-weight: 800; color: #314160; line-height: 1 }
.score-card.is-total .score-name { color: #2ec4b6 }
.score-card.is-total .score-value { color: #2ec4b6; font-size: 30px }
.module { animation: fadeIn .4s ease both }
.module-body { padding: 0 0 0 0 }
.block-primary { border-color: #2F5496; background: rgba(47,84,150,0.05) }
.block-secondary { border-color: #e5e7eb; background: rgba(229,231,235,0.15) }
.block-accent { border-color: #2ec4b6; background: rgba(46,196,182,0.05) }
.score-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 14px }
.score-item .label { color: #1f2a44; font-weight: 700; font-size: 14px }
.score-item .value { color: #475569; font-size: 14px }
.total-row { display: flex; align-items: center; gap: 10px; padding-top: 12px }
.total-row .label { font-size: 14px; font-weight: 700; color: #1f2a44 }
.total-row .value { font-size: 20px; font-weight: 800; color: #2ec4b6 }
.total-em { font-size: 20px; font-weight: 800; color: #2ec4b6 }
.section-block { margin-top: 14px }
.section-title-line { position: relative; padding-left: 12px; font-size: 15px; font-weight: 700; color: #1f2a44; margin-bottom: 8px }
.section-title-line::before { content: ""; position: absolute; left: 0; top: 3px; width: 2px; height: 16px; background: #2F5496; border-radius: 2px }
.section-body { background: #FAFAFA; border: 1px solid #e5e7eb; border-radius: 8px; padding: 12px 16px; color: #475569; font-size: 14px; line-height: 1.6 }
.section-body.highlight { background: rgba(46,196,182,0.08) }
.improve-list { display: flex; flex-direction: column; gap: 10px }
.improve-item { background: #fff; border: 1px dashed #e5e7eb; border-radius: 8px; padding: 8px 10px; animation: fadeIn 0.4s ease both }
.improve-row { display: flex; align-items: center; gap: 8px }
.improve-label { font-size: 12px; color: #6b7280; padding: 2px 6px; background: #f1f5f9; border-radius: 999px }
.improve-kp { color: #2F5496; font-weight: 700; text-decoration: underline }
.improve-kp.glow { background: rgba(46,196,182,0.08) }
.improve-text { color: #314160 }
.improve-link { color: #2F5496; text-decoration: underline }
.info-card { padding: 18px 18px }
.info-card .info-grid { display: grid; grid-template-columns: 1fr; gap: 12px }
.field .label { font-size: 14px; color: #1f2a44; font-weight: 700 }
.field .value { font-size: 15px; color: #475569 }
.kp-card .kp-list { list-style: none; padding: 0; margin: 0; display: flex; flex-direction: column; gap: 6px }
.kp-list li { cursor: pointer; padding: 8px 10px; border-radius: 8px }
.kp-list li.glow { background: rgba(47,84,150,0.08); transition: background 0.6s ease }
.tags { display: flex; flex-wrap: wrap; gap: 8px }
.tag { background: #f1f5f9; border-radius: 4px; padding: 6px 10px; color: #314160; min-height: 30px }
.download-link { color: #2F5496; text-decoration: underline }
.attach-list { list-style: none; padding: 0; margin: 0 }
.attach-item { padding: 4px 0 }
.muted { color: #94a3b8 }
.main-pane { display: flex; flex-direction: column; gap: 20px }
.side-pane { display: flex; flex-direction: column; gap: 14px }
.history-list { position: relative; padding-left: 16px }
.history-list::before { content: ""; position: absolute; left: 6px; top: 0; bottom: 0; width: 2px; background: #e5e7eb }
.history-card .history-item { position: relative; padding: 8px 10px; margin-top: 6px; border-radius: 8px }
.history-card .history-item::before { content: ""; position: absolute; left: -2px; top: 8px; width: 8px; height: 8px; background: #2F5496; border-radius: 50% }
.history-header { cursor: pointer; color: #2F5496 }
.history-card .history-item:hover { background: rgba(0,0,0,0.06) }
@keyframes fadeIn { from { opacity: 0; transform: translateY(6px) } to { opacity: 1; transform: translateY(0) } }
@media (max-width: 768px) {
  .grading-layout { grid-template-columns: 1fr }
  .score-cards { grid-template-columns: repeat(2, 1fr) }
}
.header-actions :deep(.el-button) { border-radius: 4px }
:deep(.el-button.rect-ghost) { background: #fff !important; border-color: #d7dbe5 !important; color: #314160 !important; border-radius: 4px }
:deep(.el-button.rect-ghost:hover) { background: #f7f9fc !important; border-color: #bfc6d6 !important }
:deep(.el-button.rect-primary) { background: #2F5496 !important; border-color: #2F5496 !important; color: #fff !important; border-radius: 4px }
:deep(.el-button.rect-primary:hover) { background: #2a4b86 !important; border-color: #2a4b86 !important }
:deep(.el-button.rect-success) { background: #2ec4b6 !important; border-color: #2ec4b6 !important; color: #fff !important; border-radius: 4px }
:deep(.el-button.rect-success:hover) { background: #27b3a6 !important; border-color: #27b3a6 !important }
.card:hover { box-shadow: 0 6px 16px rgba(0,0,0,0.08); transform: translateY(-2px); transition: box-shadow .2s ease, transform .2s ease }
.header-actions { display: flex; align-items: center; gap: 8px }
</style>
