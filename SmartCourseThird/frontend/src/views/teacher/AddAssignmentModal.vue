<template>
  <div v-if="visible" class="modal-mask" @click.self="emit('close')">
    <div class="modal-wrap compact-wrap">
      <div class="modal-card compact-card">
        <button class="modal-close" @click="emit('close')">×</button>
        <div class="modal-body">
          <div class="modal-title">新增作业</div>
          <div class="card">
            <div class="card-header">作业基本信息</div>
            <div class="card-body info-grid">
              <div class="field"><div class="label">作业名</div><el-input v-model="form.title" placeholder="请输入作业名" /></div>
              <div class="field"><div class="label">课程ID</div><el-input-number v-model="form.courseId" :min="1" /></div>
              <div class="field"><div class="label">发布者ID</div><el-input-number v-model="form.publisherUserId" :min="1" /></div>
              <div class="field"><div class="label">模式</div>
                <el-select v-model="form.mode" placeholder="选择模式">
                  <el-option label="文件" value="file" />
                  <el-option label="题目" value="question" />
                </el-select>
              </div>
              <div class="field"><div class="label">总分</div><el-input-number v-model="form.totalScore" :min="0" :max="1000" /></div>
              <div class="field"><div class="label">时长(分钟)</div><el-input-number v-model="form.duration" :min="0" :max="10000" /></div>
              <div class="field"><div class="label">开始时间</div><el-date-picker v-model="form.startTime" type="datetime" placeholder="选择开始时间" />
              </div>
              <div class="field"><div class="label">结束时间</div><el-date-picker v-model="form.endTime" type="datetime" placeholder="选择结束时间" />
              </div>
            </div>
          </div>

          <div class="card">
            <div class="card-header">作业要求</div>
            <div class="card-body">
              <div class="field full"><el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入作业要求" /></div>
            </div>
          </div>

          <div class="card">
            <div class="card-header">提交规范</div>
            <div class="card-body">
              <div class="field">
                <div class="label">允许的文件类型</div>
                <el-input v-model="allowedText" placeholder="例如: pdf,docx,txt" />
                <div class="type-chips">
                  <span v-for="t in allowedTypes" :key="t" class="type-chip"><span class="type-icon">{{ typeIcon(t) }}</span><span class="type-text">{{ t.toUpperCase() }}</span></span>
                </div>
              </div>
              <div class="field full">
                <div class="label">附件</div>
                <input type="file" multiple @change="onFilesChange" />
                <ul class="attach-list">
                  <li v-for="(a,i) in attachments" :key="i" class="attach-item">{{ a.name }}</li>
                </ul>
              </div>
            </div>
          </div>

          <div class="actions">
            <el-button class="primary-btn" type="primary" :loading="submitting" @click="submit">提交</el-button>
            <el-button @click="emit('close')">取消</el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, computed } from 'vue'
import { BASE_URL } from '../../utils/api'
import { ElMessage } from 'element-plus'
import { getSession } from '../../utils/session'
const props = defineProps<{ visible: boolean }>()
const emit = defineEmits<{ (e: 'close'): void; (e: 'created'): void }>()
const identity: any = { userId: Number((getSession() as any)?.user?.id ?? 301) }

const form = reactive<any>({
  title: '',
  courseId: 1,
  publisherUserId: 301,
  mode: 'file',
  totalScore: 100,
  duration: 0,
  startTime: '',
  endTime: '',
  description: ''
})

const allowedText = ref('pdf,docx,txt')
const allowedTypes = computed(() => allowedText.value.split(',').map(s => s.trim().toLowerCase()).filter(Boolean))
const attachments = ref<{ name: string; url: string }[]>([])
const selectedFiles = ref<File[]>([])
const attachmentsJson = computed(() => JSON.stringify(attachments.value))
function onFilesChange(e: Event) {
  const input = e.target as HTMLInputElement
  const files = input.files ? Array.from(input.files) : []
  selectedFiles.value = files
  attachments.value = files.map(f => ({ name: f.name, url: '' }))
}

function typeIcon(t: string) {
  if (t === 'pdf') return '📕'
  if (t === 'doc' || t === 'docx') return '📝'
  if (t === 'txt') return '📄'
  return '📁'
}

const submitting = ref(false)
function normalizedTime(v: any) {
  if (!v) return null
  try { return new Date(v).toISOString() } catch { return null }
}

async function submit() {
  if (!form.title || !form.courseId) { ElMessage.warning('请填写作业名与课程ID'); return }
  form.publisherUserId = Number(form.publisherUserId || (identity as any)?.userId || 301)
  // 先上传附件到 /courses/{courseId}/{publisherUserId}/
  if (selectedFiles.value.length > 0) {
    try {
      const uploaded: { name: string; url: string }[] = []
      for (const f of selectedFiles.value) {
        const fd = new FormData()
        fd.append('file', f)
        fd.append('courseId', String(form.courseId))
        fd.append('uploaderUserId', String(form.publisherUserId || (identity as any)?.userId || 301))
        const res = await fetch(`${BASE_URL}/api/files/upload-to-courses`, { method: 'POST', body: fd })
        const json = await res.json()
        const url = String(json?.data?.fileUrl || json?.data?.url || '')
        const name = String(json?.data?.fileName || f.name)
        if (url) uploaded.push({ name, url })
      }
      if (uploaded.length) attachments.value = uploaded
    } catch (e) {
      ElMessage.error('附件上传失败，请稍后重试')
      return
    }
  }

  const body: any = {
    title: form.title,
    courseId: Number(form.courseId),
    publisherUserId: Number(form.publisherUserId),
    mode: String(form.mode || 'question'),
    totalScore: Number(form.totalScore || 100),
    duration: Number(form.duration || 0),
    startTime: normalizedTime(form.startTime),
    endTime: normalizedTime(form.endTime),
    description: form.description || '',
    allowedFileTypes: String(allowedText.value || '').trim(),
    attachments: attachments.value.length ? attachmentsJson.value : '[]'
  }
  submitting.value = true
  try {
    const res = await fetch('/api/ai-grading/assignments', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) })
    const json = await res.json()
    if (json?.code === 200) { ElMessage.success('新增作业成功'); emit('created'); emit('close') }
    else { ElMessage.error('新增作业失败') }
  } catch (e) { ElMessage.error('新增作业失败') }
  finally { submitting.value = false }
}
</script>

<style scoped>
.modal-mask { position: fixed; inset: 0; background: rgba(0,0,0,0.45); display: grid; place-items: center; z-index: 2000; padding: 16px }
.modal-wrap { width: 100%; max-width: 820px }
.modal-card { background: #fff; border-radius: 12px; border: 1px solid #e5e7eb; position: relative }
.modal-close { position: absolute; right: 12px; top: 10px; border: 1px solid #d7dbe5; background: #fff; color: #314160; width: 28px; height: 28px; border-radius: 6px; cursor: pointer }
.modal-close:hover { background: #f3f6fb }
.compact-wrap { width: auto; max-width: 760px; padding: 0 16px; }
.compact-card { height: auto; max-height: 80vh; overflow: auto; padding-bottom: 12px; }
.modal-body { padding: 12px 16px; font-family: Inter, system-ui, -apple-system, Segoe UI, Roboto, Helvetica, Arial, sans-serif; }
.modal-title { font-size: 18px; font-weight: 700; color: #333333; margin-bottom: 12px; }
.card { background: #E6F3FF; border: 1px solid #EEEEEE; border-radius: 12px; padding: 12px 14px; box-shadow: 0 0 0 rgba(0,0,0,0); transition: box-shadow .15s ease, transform .15s ease; margin-bottom: 12px; }
.card:hover { box-shadow: 0 8px 8px #CCCCCC; transform: translateY(-1px); }
.card-header { font-size: 14px; font-weight: 700; color: #333333; padding-bottom: 6px; border-bottom: 1px solid #EEEEEE; }
.card-body { padding-top: 10px; }
.info-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px 16px; }
.field { display: flex; flex-direction: column; gap: 6px; }
.field.full { grid-column: 1 / -1; }
.type-chips { display: flex; flex-wrap: wrap; gap: 8px; padding-top: 6px; }
.type-chip { display: inline-flex; align-items: center; gap: 6px; padding: 6px 10px; background: #fff; border: 1px solid #EEEEEE; border-radius: 999px; color: #2F5496; box-shadow: 0 0 0 rgba(0,0,0,0); transition: transform .15s ease, box-shadow .15s ease; }
.type-chip:hover { transform: scale(1.02); box-shadow: 0 4px 8px #CCCCCC; }
.type-icon { font-size: 14px; }
.type-text { font-size: 12px; font-weight: 700; }
.attach-list { list-style: none; padding: 0; margin: 0; }
.attach-item { font-size: 13px; color: #666666; padding: 4px 0; }
.actions { display: flex; gap: 8px; justify-content: flex-end; padding-top: 8px; }
.primary-btn { background: #2F5496; color: #fff; border: none; border-radius: 8px; padding: 6px 12px; cursor: pointer; transition: background .15s ease, transform .15s ease; }
.primary-btn:hover { background: #214173; transform: scale(1.02); }
</style>

