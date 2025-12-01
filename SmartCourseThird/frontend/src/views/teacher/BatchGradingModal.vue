<template>
  <div v-if="visible" class="modal-mask" @click.self="emit('close')">
    <div class="modal-wrap">
      <div class="modal-card">
        <button class="modal-close" @click="emit('close')">×</button>
        <div class="modal-header">
          <div class="title-wrap">
            <span class="title-icon">⚙️</span>
            <span class="title-text">批量批改</span>
          </div>
          <div class="desc-text">选择提交并触发 AI 批改；右侧可查看处理中/失败任务</div>
        </div>
        <div class="modal-body">
        <div class="batch-columns">
          <div class="batch-left">
            <div class="section-title">全部作业提交</div>
            <div class="table-wrap">
            <table class="assignments-table">
              <thead>
                <tr>
                  <th style="width:44px">选择</th>
                  <th>学生ID</th>
                  <th>学生名</th>
                  <th>作业名</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in allSubmissions" :key="row.id">
                  <td><el-checkbox :model-value="selectedIds.has(row.id)" @change="onSelect(row.id, $event)" /></td>
                  <td>{{ row.studentId }}</td>
                  <td>{{ row.studentName }}</td>
                  <td>{{ row.assignmentName }}</td>
                </tr>
              </tbody>
            </table>
            </div>
            <div class="modal-fixed-actions">
              <div class="actions-left">
                <span class="muted">已选 <span class="em-num">{{ selectedIds.size }}</span> 项</span>
              </div>
              <div class="actions-right">
                <el-button type="primary" :disabled="selectedIds.size===0" @click="batchTrigger">批量批改</el-button>
                <el-button @click="refreshPending">刷新</el-button>
              </div>
            </div>
          </div>
          <div class="batch-right">
            <div class="section-title">处理中/失败（AI 批改中）</div>
            <div class="table-wrap">
            <table class="assignments-table">
              <thead>
                <tr>
                  <th>学生ID</th>
                  <th>学生名</th>
                  <th>作业名</th>
                  <th>状态</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in unresolved" :key="row.id">
                  <td>{{ row.studentId }}</td>
                  <td>{{ row.studentName }}</td>
                  <td>{{ row.assignmentName }}</td>
                  <td>
                    <span class="status-badge" :class="statusClass(row.status)">{{ statusText(row.status) }}</span>
                  </td>
                  <td>
                    <el-button v-if="row.status==='failed'" size="small" type="danger" @click="deleteLatest(row.id)">确认</el-button>
                  </td>
                </tr>
              </tbody>
            </table>
            </div>
          </div>
        </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
const props = defineProps<{ visible: boolean }>()
const emit = defineEmits<{ (e: 'close'): void; (e: 'mark-graded'): void }>()

interface SimpleRow { id: number; studentId: string; studentName: string; assignmentName: string; status?: string }

const allSubmissions = ref<SimpleRow[]>([])
const unresolved = ref<SimpleRow[]>([])
const selectedIds = reactive<Set<number>>(new Set())
const userNameMap = reactive<Record<string, string>>({})
const llmModel = ref('Qwen/Qwen3-8B')

function statusText(s?: string) {
  if (s === 'failed') return '失败'
  if (s === 'processing') return '处理中'
  return String(s || '处理中')
}
function statusClass(s?: string) {
  if (s === 'failed') return 'st-failed'
  return 'st-processing'
}

function onSelect(id: number, checked: boolean) {
  if (checked) selectedIds.add(id); else selectedIds.delete(id)
}

async function loadAllSubmissions() {
  try {
    const res = await fetch('/api/ai-grading/submissions?page=1&pageSize=100')
    const json = await res.json()
    const data = Array.isArray(json?.data) ? json.data : []
    allSubmissions.value = data.map((item: any) => {
      const sub = item.submission || {}
      return {
        id: Number(sub.id ?? 0),
        studentId: String(sub.studentUserId ?? ''),
        studentName: String(item.realName ?? ''),
        assignmentName: String(sub.fileName ?? ''),
      }
    })
  } catch (e) { ElMessage.error('加载全部提交失败') }
}

async function loadUnresolved() {
  try {
    const res = await fetch('/api/ai-grading/unresolved')
    const json = await res.json()
    const data = Array.isArray(json?.data) ? json.data : []
    unresolved.value = data.map((item: any) => {
      const sub = item.submission || {}
      const g = item.grading || {}
      return {
        id: Number(sub.id ?? 0),
        studentId: String(sub.studentUserId ?? ''),
        studentName: String(
          item.realName ?? item.user?.realName ?? item.student?.realName ?? item.student?.user?.realName ?? userNameMap[String(sub.studentUserId ?? '')] ?? ''
        ),
        assignmentName: String(sub.fileName ?? ''),
        status: String(g.status ?? 'processing')
      }
    })
    fillUserNames()
  } catch (e) { ElMessage.error('加载处理中/失败失败') }
}

async function refreshPending() {
  try {
    const res = await fetch('/api/ai-grading/pending')
    await res.json()
    ElMessage.success('已刷新待批改列表')
    await loadUnresolved()
    await loadAllSubmissions()
  } catch (e) { ElMessage.error('刷新失败') }
}

async function batchTrigger() {
  try {
    const res = await fetch('/api/ai-grading/grading/batch', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ submissionIds: Array.from(selectedIds), llmModel: llmModel.value })
    })
    const json = await res.json()
    ElMessage.success('批量批改已触发')
    selectedIds.clear()
    await loadUnresolved()
    emit('mark-graded')
  } catch (e) { ElMessage.error('批量触发失败') }
}

async function deleteLatest(id: number) {
  try {
    const res = await fetch(`/api/ai-grading/grading/${id}/latest`, { method: 'DELETE' })
    const json = await res.json()
    if (json?.data) {
      ElMessage.success('已删除最新批改记录')
      await loadUnresolved()
    } else {
      ElMessage.info('没有可删除的记录')
    }
  } catch (e) { ElMessage.error('删除失败') }
}

async function fillUserNames() {
  const missing = unresolved.value.filter(r => !r.studentName && r.studentId)
  await Promise.all(missing.map(async (r) => {
    try {
      const u = await fetch(`/api/ai-grading/users/${encodeURIComponent(r.studentId)}`)
      const j = await u.json()
      const name = j?.data?.realName || ''
      if (name) {
        userNameMap[r.studentId] = name
        r.studentName = name
      }
    } catch {}
  }))
}

onMounted(async () => {
  await loadAllSubmissions()
  await loadUnresolved()
})
</script>

<style scoped>
.modal-mask { position: fixed; inset: 0; background: rgba(0,0,0,0.45); display: grid; place-items: center; z-index: 2000; padding: 16px }
.modal-wrap { width: 100%; max-width: 980px; padding: 8px }
.modal-card { position: relative; background: #fff; border-radius: 14px; border: 1px solid #e5e7eb; padding: 12px 12px; box-shadow: 0 18px 40px rgba(17, 24, 39, 0.18); height: 72vh; max-height: 720px; display: flex; flex-direction: column }
.modal-close { position: absolute; right: 14px; top: 12px; border: 1px solid #d7dbe5; background: #fff; color: #314160; width: 32px; height: 32px; border-radius: 8px; cursor: pointer }
.modal-close:hover { background: #f3f6fb }
.modal-header { display: flex; flex-direction: column; gap: 6px; padding: 10px 12px; background: linear-gradient(180deg, #f7f9fc 0%, #ffffff 100%); border-bottom: 1px solid #edf2f7 }
.modal-body { flex: 1; overflow: auto; padding: 8px 6px }
.title-wrap { display: inline-flex; align-items: center; gap: 8px }
.title-icon { font-size: 18px }
.title-text { font-size: 18px; font-weight: 800; color: #2F5496 }
.desc-text { font-size: 12px; color: #64748b }
.batch-columns { display: grid; grid-template-columns: 1fr 1fr; gap: 14px }
.section-title { font-weight: 800; color: #1f2a44; margin-bottom: 8px; border-left: 3px solid #2F5496; padding-left: 8px }
.table-wrap { background: #fff; border: 1px solid #e5e7eb; border-radius: 10px; overflow: hidden }
.assignments-table { width: 100%; border-collapse: separate; border-spacing: 0 }
.assignments-table thead th { background: #eef2ff; color: #314160; font-weight: 700; font-size: 13px; text-align: left; position: sticky; top: 0; z-index: 1 }
.assignments-table th, .assignments-table td { border-bottom: 1px solid #e5e7eb; padding: 10px 12px }
.assignments-table tbody tr:nth-child(even) { background: #fafafa }
.assignments-table tbody tr:hover { background: #f3f6fb }
.modal-fixed-actions { display: flex; align-items: center; justify-content: space-between; padding: 10px 0; border-top: 1px solid #edf2f7; margin-top: 10px }
.muted { color: #94a3b8 }
.em-num { font-weight: 800; color: #2F5496 }
.status-badge { display: inline-flex; align-items: center; gap: 6px; padding: 4px 8px; border-radius: 999px; font-size: 12px; font-weight: 700 }
.status-badge.st-processing { color: #8a8f9d; background: rgba(138,143,157,0.12); border: 1px solid rgba(138,143,157,0.25) }
.status-badge.st-failed { color: #b91c1c; background: rgba(185,28,28,0.08); border: 1px solid rgba(185,28,28,0.25) }
.batch-left, .batch-right { display: flex; flex-direction: column; gap: 8px }
@media (max-width: 768px) { .batch-columns { grid-template-columns: 1fr } }

</style>
