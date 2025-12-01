<template>
  <div>
    <div v-if="inline" class="grading-page">
      <div class="grading-page-header">
        <div class="header-leading">
          <div class="header-row">
            <nav class="breadcrumb">
              <a class="bc-link" href="#">作业列表</a>
              <span class="sep">›</span>
              <a class="bc-link" href="#">已发布作业</a>
              <span class="sep">›</span>
              <span class="bc-current">作业详情</span>
            </nav>
          </div>
          <div class="title">作业详情</div>
        </div>
        <div class="header-actions">
          <el-button class="rect-ghost" @click="emit('close')">返回列表</el-button>
        </div>
      </div>
      <div class="grading-layout">
        <div class="left-pane animate-left">
          <div class="card paper-texture">
            <div class="card-title with-icon"><span class="icon">🗒️</span><span>作业要求</span><span class="badge">作业须知</span></div>
            <div class="section-block">
              <div class="section-subtitle with-icon"><span class="icon">✅</span><span>必做内容</span></div>
              <div class="section-body req-body">
                <div v-if="reqHtml.length" class="rich-text" v-html="reqHtml"></div>
                <div v-else>—</div>
            </div>
          </div>
          <div class="section-block" v-if="questions.length > 0">
            <div class="section-title-line">包含题目</div>
            <div class="section-body">
              <ul class="question-list">
                <li v-for="(q, i) in questions" :key="(q as any)?.id ?? i" class="question-item">
                  <span class="qid">{{ (q as any)?.id }}</span>
                  <span class="qtitle">{{ (q as any)?.title }}</span>
                  <span class="qtype">{{ questionTypeText((q as any)?.questionType) }}</span>
                </li>
              </ul>
            </div>
          </div>
          </div>
        </div>
        <div class="middle-pane animate-middle">
          <div class="card info-card">
            <div class="card-title with-icon"><span class="icon">🗂️</span><span>作业信息</span></div>
            <div class="info-section">
              <div class="section-subtitle">基础信息</div>
              <div class="info-grid two-col">
                <div class="field"><div class="label">作业名</div><div class="value">{{ assignment.title || '' }}</div></div>
                <div class="field"><div class="label">发布者</div><div class="value">{{ assignment.publisherRealName || assignment.publisherUserId || '' }}</div></div>
                <div class="field"><div class="label">课程ID</div><div class="value">{{ assignment.courseId || '' }}</div></div>
                <div class="field"><div class="label">类型</div><div class="value">{{ assignment.type || '' }}</div></div>
              </div>
            </div>
            <div class="info-section">
              <div class="section-subtitle">时间信息</div>
              <div class="info-grid two-col">
                <div class="field"><div class="label">开始时间</div><div class="value time-highlight">{{ assignment.startTime || '' }}</div></div>
                <div class="field">
                  <div class="label">结束时间</div>
                  <div class="value deadline"><span class="cal-icon">📅</span><span>{{ assignment.endTime || '' }}</span></div>
                </div>
                <div class="field"><div class="label">创建时间</div><div class="value">{{ assignment.createTime || '' }}</div></div>
                <div class="field"><div class="label">更新时间</div><div class="value">{{ assignment.updateTime || '' }}</div></div>
              </div>
            </div>
            <div class="info-section">
              <div class="section-subtitle">状态信息</div>
              <div class="info-grid two-col">
                <div class="field"><div class="label">总分</div><div class="value total-strong">{{ assignment.totalScore ?? '' }}</div></div>
                <div class="field"><div class="label">模式</div><div class="value">{{ assignment.mode || '' }}</div></div>
                <div class="field"><div class="label">状态</div><div class="value">{{ assignment.status || '' }}</div></div>
                <div class="field"><div class="label">时间限制</div><div class="value">{{ assignment.timeLimit ?? '' }}</div></div>
                <div class="field"><div class="label">删除标记</div><div class="value">{{ assignment.isDeleted ?? '' }}</div></div>
                <div class="field"><div class="label">删除时间</div><div class="value">{{ assignment.deleteTime || '' }}</div></div>
              </div>
            </div>
          </div>
        </div>
        <div class="right-pane animate-right">
          <div class="card paper-texture">
            <div class="card-title with-icon"><span class="icon">📘</span><span>提交规范</span></div>
            <div class="section-block">
              <div class="section-title-line">文件格式要求</div>
              <div class="section-body">
                <div class="type-chips">
                  <span v-for="t in allowedTypes" :key="t" class="type-chip"><span class="type-icon">{{ typeIcon(t) }}</span><span class="type-text">{{ t.toUpperCase() }}</span></span>
                </div>
              </div>
            </div>
            <div class="section-block">
              <div class="section-title-line">参考资料</div>
              <div class="section-body">
                <div v-if="attachments.length===0" class="empty-attach"><span class="empty-icon">📎</span><span class="empty-text">暂无附件</span></div>
                <ul v-else class="attach-list">
                  <li v-for="(a, i) in attachments" :key="i" class="attach-item">
                    <a v-if="attachmentHref(a)" :href="attachmentHref(a)" target="_blank" :download="attachmentDownloadName(a)" class="download-link">{{ attachmentText(a) }}</a>
                    <span v-else>{{ attachmentText(a) }}</span>
                  </li>
                </ul>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div v-else-if="visible" class="modal-mask" @click.self="emit('close')">
      <div class="modal-wrap compact-wrap">
        <div class="modal-card compact-card">
          <button class="modal-close" @click="emit('close')">×</button>
          <div class="modal-body">
            <div class="modal-title">作业详情</div>

            <div class="card">
              <div class="card-header">作业基本信息</div>
              <div class="card-body info-grid">
                <div class="field"><div class="label">作业名</div><div class="value">{{ assignment.title || '' }}</div></div>
                <div class="field"><div class="label">发布者</div><div class="value">{{ assignment.publisherRealName || assignment.publisherUserId || '' }}</div></div>
                <div class="field"><div class="label">开始时间</div><div class="value">{{ assignment.startTime || '' }}</div></div>
                <div class="field"><div class="label">结束时间</div><div class="value">{{ assignment.endTime || '' }}</div></div>
                <div class="field"><div class="label">课程ID</div><div class="value">{{ assignment.courseId || '' }}</div></div>
                <div class="field"><div class="label">总分</div><div class="value">{{ assignment.totalScore ?? '' }}</div></div>
                <div class="field"><div class="label">时长</div><div class="value">{{ assignment.duration ?? '' }}</div></div>
                <div class="field"><div class="label">模式</div><div class="value">{{ assignment.mode || '' }}</div></div>
                <div class="field"><div class="label">创建时间</div><div class="value">{{ assignment.createTime || '' }}</div></div>
                <div class="field"><div class="label">更新时间</div><div class="value">{{ assignment.updateTime || '' }}</div></div>
                <div class="field"><div class="label">状态</div><div class="value">{{ assignment.status || '' }}</div></div>
                <div class="field"><div class="label">类型</div><div class="value">{{ assignment.type || '' }}</div></div>
                <div class="field"><div class="label">时间限制</div><div class="value">{{ assignment.timeLimit ?? '' }}</div></div>
                <div class="field"><div class="label">删除标记</div><div class="value">{{ assignment.isDeleted ?? '' }}</div></div>
                <div class="field"><div class="label">删除时间</div><div class="value">{{ assignment.deleteTime || '' }}</div></div>
              </div>
            </div>

            <div class="card">
              <div class="card-header">作业要求</div>
              <div class="card-body">
                <div class="field full"><div class="value long-text">{{ assignment.description || '' }}</div></div>
              </div>
            </div>

            <div class="card">
              <div class="card-header">提交规范</div>
              <div class="card-body">
                <div class="field">
                  <div class="label">允许的文件类型</div>
                  <div class="type-chips">
                    <span v-for="t in allowedTypes" :key="t" class="type-chip"><span class="type-icon">{{ typeIcon(t) }}</span><span class="type-text">{{ t.toUpperCase() }}</span></span>
                  </div>
                </div>
                <div class="field full">
                  <div class="label">附件</div>
                  <div class="attachments">
                    <span v-if="attachments.length===0" class="muted">暂无附件</span>
                    <ul v-else class="attach-list">
                      <li v-for="(a, i) in attachments" :key="i" class="attach-item">
                        <a v-if="attachmentHref(a)" :href="attachmentHref(a)" target="_blank" :download="attachmentDownloadName(a)" class="download-link">{{ attachmentText(a) }}</a>
                        <span v-else>{{ attachmentText(a) }}</span>
                      </li>
                    </ul>
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
import { computed, ref, watch, onMounted } from 'vue'
import { getAssignmentDetail } from '../../api/assignment'
import { BASE_URL } from '../../utils/api'
const props = defineProps<{ visible: boolean; assignment: Record<string, unknown>; inline?: boolean }>()
const emit = defineEmits<{ (e: 'close'): void }>()

const allowedTypes = computed<string[]>(() => {
  const v = (props.assignment && (props.assignment.allowedFileTypes as unknown))
  if (Array.isArray(v)) {
    return v
      .map((s: unknown) => String(s ?? '').trim())
      .flatMap((s: string) => s.replace(/^\s*\[|\]\s*$/g, '').split(/[,，]/))
      .map((s: string) => s.trim().replace(/^"+|"+$/g, '').toLowerCase())
      .filter(Boolean)
  }
  if (typeof v === 'string') {
    const s = String(v).trim()
    try {
      const j = JSON.parse(s)
      if (Array.isArray(j)) return j.map((u: unknown) => String(u ?? '').trim().replace(/^"+|"+$/g, '').toLowerCase()).filter(Boolean)
    } catch {}
    return s
      .replace(/^\s*\[|\]\s*$/g, '')
      .split(/[,，]/)
      .map(t => t.trim().replace(/^"+|"+$/g, '').toLowerCase())
      .filter(Boolean)
  }
  return []
})

const reqHtml = computed<string>(() => {
  const raw = String((props.assignment as Record<string, unknown>)?.description ?? '').trim()
  if (!raw) return ''
  const esc = raw
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
  return esc
    .replace(/(\d+\s*类错误)/g, '<span class="emphasis">$1</span>')
    .replace(/(\d+\s*种以上解决方案)/g, '<span class="emphasis">$1</span>')
    .replace(/(\d+\s*字)/g, '<span class="emphasis">$1</span>')
})

function typeIcon(t: string) {
  if (t === 'pdf') return '📕'
  if (t === 'doc' || t === 'docx') return '📝'
  if (t === 'txt') return '📄'
  return '📁'
}

const attachments = computed<Record<string, unknown>[]>(() => {
  const v = (props.assignment && (props.assignment.attachments as unknown))
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

const extraQuestions = ref<Record<string, unknown>[]>([])
async function loadQuestions() {
  const a = props.assignment || {}
  const t = String((a as Record<string, unknown>)?.type || '')
  const idRaw = (a as Record<string, unknown>)?.id as unknown
  const id = Number(idRaw)
  const hasQ = Array.isArray((a as Record<string, unknown>)?.questions)
  if (t === 'exam' && Number.isFinite(id) && !hasQ) {
    try {
      const res = await getAssignmentDetail(id)
      const data = res?.data || {}
      const q = Array.isArray((data as Record<string, unknown>)?.questions) ? (data as Record<string, unknown>)?.questions as Record<string, unknown>[] : []
      extraQuestions.value = q
    } catch {}
  }
}
onMounted(() => { loadQuestions() })
watch(() => props.assignment, () => { extraQuestions.value = []; loadQuestions() })

const questions = computed<Record<string, unknown>[]>(() => {
  const a = props.assignment || {}
  const q = (a as Record<string, unknown>)?.questions
  return Array.isArray(q) ? (q as Record<string, unknown>[]) : extraQuestions.value
})

function questionTypeText(t: unknown) {
  const s = String(t || '')
  if (s === 'single') return '单选'
  if (s === 'multiple') return '多选'
  if (s === 'blank') return '填空'
  if (s === 'short') return '简答'
  if (s === 'code') return '编程'
  return s
}
</script>

<style scoped>
.compact-wrap { width: auto; max-width: 760px; padding: 0 16px; }
.compact-card { height: auto; max-height: 80vh; overflow: auto; padding-bottom: 12px; }
.modal-body { padding: 12px 16px; font-family: Inter, system-ui, -apple-system, Segoe UI, Roboto, Helvetica, Arial, sans-serif; }
.modal-title, .card-header, .card-title { font-size: 18px; font-weight: 700; color: #1f2a44; }
.grading-page { padding: 8px 0 }
.grading-page-header { display: flex; align-items: center; justify-content: space-between; padding: 10px 14px }
.header-actions { display: inline-flex; align-items: center; gap: 8px }
.header-leading { display: flex; flex-direction: column; gap: 4px }
.header-row { display: flex; align-items: center; gap: 12px }
.breadcrumb { display: inline-flex; align-items: center; gap: 6px; color: #2F5496 }
.bc-link { color: #2F5496; padding: 6px 8px; border-radius: 6px; text-decoration: none }
.bc-link:hover { background: rgba(47,84,150,0.08) }
.sep { color: #97a0b5 }
.bc-current { color: #314160; font-weight: 700 }
.grading-page-header .title { font-size: 20px; font-weight: 700; color: #1f2a44 }
.grading-layout { display: grid; grid-template-columns: 8fr 7fr 5fr; gap: 30px; }
.grading-page { background-image: radial-gradient(rgba(0,0,0,0.03) 1px, transparent 1px); background-size: 12px 12px }
.card { background: #fff; border: 1px solid #e5e7eb; border-radius: 8px; padding: 14px 16px; box-shadow: 0 1px 6px rgba(0,0,0,0.06); margin-bottom: 12px; }
.paper-texture { background-image: radial-gradient(rgba(0,0,0,0.05) 1px, transparent 1px); background-size: 10px 10px }
.card-title.with-icon .icon { margin-right: 6px }
.card-title .badge { margin-left: auto; font-size: 12px; color: #6b7280; background: #f1f5f9; border: 1px solid #e5e7eb; border-radius: 999px; padding: 2px 8px }
.info-grid { display: grid; grid-template-columns: repeat(1, minmax(0,1fr)); gap: 10px; }
.info-grid.two-col { grid-template-columns: repeat(2, minmax(0,1fr)); gap: 12px; }
.field { display: flex; flex-direction: column; gap: 4px; }
.field .label { font-size: 14px; color: #1f2a44; font-weight: 700 }
.field .value { font-size: 14px; color: #475569 }
.section-subtitle { font-size: 14px; font-weight: 700; color: #374151; margin-bottom: 10px; display: inline-flex; align-items: center; gap: 6px }
.info-section + .info-section { margin-top: 12px; padding-top: 12px; border-top: 1px solid #e5e7eb }
.time-highlight { color: #2ec4b6; font-weight: 700 }
.deadline { color: #2ec4b6; display: inline-flex; align-items: center; gap: 6px }
.cal-icon { font-size: 14px }
.total-strong { font-size: 16px; font-weight: 800; color: #2ec4b6 }
.section-block { margin-top: 14px }
.section-title-line { position: relative; padding-left: 12px; font-size: 16px; font-weight: 700; color: #1f2a44; margin-bottom: 8px }
.section-title-line::before { content: ""; position: absolute; left: 0; top: 3px; width: 2px; height: 16px; background: #2F5496; border-radius: 2px }
.section-body { background: #FAFAFA; border: 1px solid #e5e7eb; border-radius: 8px; padding: 16px; color: #475569; font-size: 15px; line-height: 1.6 }
.type-chips { display: flex; flex-wrap: wrap; gap: 8px; }
.type-chip { display: inline-flex; align-items: center; gap: 6px; padding: 8px 12px; border-radius: 4px; color: #2F5496; background: rgba(47,84,150,0.06); border: 1px solid rgba(47,84,150,0.45); box-shadow: 0 0 0 rgba(0,0,0,0); transition: transform .15s ease, box-shadow .15s ease; }
.type-chip:hover { transform: translateY(-1px); box-shadow: 0 4px 10px rgba(0,0,0,0.12) }
.type-icon { font-size: 15px; }
.type-text { font-size: 13px; font-weight: 700; letter-spacing: .5px }
.attachments { display: flex; flex-direction: column; gap: 6px; }
.attach-list { list-style: none; padding: 0; margin: 0; }
.attach-item { font-size: 14px; color: #666666; padding: 4px 0; }
.download-link { color: #2F5496; text-decoration: underline }
.muted { color: #94a3b8; }
.empty-attach { display: inline-flex; align-items: center; gap: 8px; color: #9aa3b2; font-size: 12px }
.empty-icon { font-size: 16px }
:root {}
.question-list { list-style: none; padding: 0; margin: 0; display: flex; flex-direction: column; gap: 8px }
.question-item { display: grid; grid-template-columns: 60px 1fr 100px; gap: 6px; align-items: center; font-size: 13px; color: #475569 }
.question-item .qid { color: #1f2a44; font-weight: 700 }
.question-item .qtype { color: #2F5496; font-weight: 700 }
:deep(.el-button.rect-ghost) { background-color: #ffffff !important; border-color: #2F5496 !important; color: #2F5496 !important; border-radius: 4px }
:deep(.el-button.rect-ghost:hover) { background-color: #f7f9fc !important }
.animate-left { animation: fadeInUp .4s ease both; animation-delay: .0s }
.animate-middle { animation: fadeInUp .4s ease both; animation-delay: .2s }
.animate-right { animation: fadeInUp .4s ease both; animation-delay: .4s }
@keyframes fadeInUp { from { opacity: 0; transform: translateY(6px) } to { opacity: 1; transform: translateY(0) } }
.left-pane, .middle-pane, .right-pane { display: flex; flex-direction: column; gap: 14px; }
.card:hover { box-shadow: 0 6px 16px rgba(0,0,0,0.12); transform: translateY(-2px); transition: box-shadow .2s ease, transform .2s ease }
@media (max-width: 992px) {
  .grading-layout { grid-template-columns: 1fr }
}
@media (max-width: 576px) {
  .info-grid.two-col { grid-template-columns: 1fr }
}
.rich-text { white-space: pre-wrap }
</style>
