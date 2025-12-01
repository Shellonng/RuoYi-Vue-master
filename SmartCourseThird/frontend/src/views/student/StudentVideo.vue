<template>
  <div class="student-video-page">
    <div class="video-layout">
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
            :class="{ active: currentVideo?.id === video.id }"
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
        <template v-if="currentVideo">
          <header class="content-header">
            <h2>{{ currentVideo.title }}</h2>
            <p class="desc">{{ currentVideo.description || "暂无描述" }}</p>
          </header>

          <div class="player-container">
             <!-- Placeholder for video player -->
             <div class="video-player-placeholder">
                <div v-if="currentVideo.url">
                  <video :src="currentVideo.url" controls class="video-element"></video>
                </div>
                <div v-else class="no-url">
                   <el-icon size="64"><VideoPlay /></el-icon>
                   <p>视频播放器 (模拟)</p>
                   <p>Video ID: {{ currentVideo.id }}</p>
                </div>
             </div>
          </div>
          
          <!-- Simple stats for student -->
          <div class="video-stats">
             <el-card shadow="hover" class="stat-card">
                <template #header>我的学习进度</template>
                <el-progress :percentage="0" status="success" />
                <p class="stat-hint">观看时长: 0分钟</p>
             </el-card>
          </div>
        </template>

        <div v-else class="no-video-state">
          <el-empty description="请选择左侧视频开始学习" />
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { VideoPlay } from '@element-plus/icons-vue'
import { getSelectedCourse } from '../../utils/session'
import { BASE_URL } from '../../utils/api'

const currentCourse = ref(null)
const videoList = ref([])
const currentVideo = ref(null)

const courseId = computed(() => currentCourse.value?.id)

const fetchVideoList = async () => {
  if (!courseId.value) return
  try {
    const res = await fetch(`${BASE_URL}/api/analysis/video/list?courseId=${courseId.value}`)
    const data = await res.json()
    if (data.code === 200) {
      videoList.value = data.data || []
      if (videoList.value.length > 0) {
        selectVideo(videoList.value[0])
      }
    }
  } catch (error) {
    console.error('Failed to fetch video list:', error)
  }
}

const selectVideo = (video) => {
  currentVideo.value = video
}

const formatDuration = (seconds) => {
  if (!seconds) return '00:00'
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return `${m}:${s.toString().padStart(2, '0')}`
}

onMounted(() => {
  currentCourse.value = getSelectedCourse()
  if (currentCourse.value) {
    fetchVideoList()
  }
})
</script>

<style scoped>
.student-video-page {
  height: 100%;
  background: #f5f7fa;
}

.video-layout {
  display: flex;
  height: calc(100vh - 60px); /* Adjust based on layout header/padding */
}

.video-sidebar {
  width: 300px;
  background: white;
  border-right: 1px solid #eee;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid #eee;
}

.sidebar-header h3 {
  margin: 0 0 5px 0;
  font-size: 18px;
}

.count {
  font-size: 12px;
  color: #999;
}

.video-list {
  flex: 1;
  overflow-y: auto;
}

.video-item {
  display: flex;
  align-items: center;
  padding: 15px 20px;
  cursor: pointer;
  transition: background 0.2s;
  border-bottom: 1px solid #f5f5f5;
}

.video-item:hover {
  background: #f9fafc;
}

.video-item.active {
  background: #ecf5ff;
  border-left: 3px solid #409eff;
}

.video-icon {
  margin-right: 12px;
  color: #909399;
}

.video-item.active .video-icon {
  color: #409eff;
}

.video-info {
  flex: 1;
}

.video-title {
  font-size: 14px;
  margin-bottom: 4px;
  color: #303133;
}

.video-meta {
  font-size: 12px;
  color: #909399;
}

.empty-state {
  padding: 20px;
  text-align: center;
  color: #909399;
}

.video-content {
  flex: 1;
  padding: 30px;
  overflow-y: auto;
}

.content-header {
  margin-bottom: 20px;
}

.content-header h2 {
  margin: 0 0 10px 0;
}

.desc {
  color: #606266;
}

.player-container {
  background: black;
  width: 100%;
  height: 400px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
  overflow: hidden;
}

.video-element {
  width: 100%;
  height: 100%;
}

.no-url {
  color: white;
  text-align: center;
}

.no-video-state {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.video-stats {
  margin-top: 20px;
}

.stat-card {
  width: 300px;
}

.stat-hint {
  margin-top: 10px;
  font-size: 12px;
  color: #999;
}
</style>
