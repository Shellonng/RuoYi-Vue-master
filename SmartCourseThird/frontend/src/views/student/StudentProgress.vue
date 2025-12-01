<template>
  <div>
    <header class="top">
      <div>
        <h1>学习进度</h1>
        <p class="sub">视频完成率、观看时长与提交状态。</p>
      </div>
      <div class="filters">
        <label>课程ID <input v-model.number="filters.courseId" type="number" /></label>
        <label>学生ID <input v-model.number="filters.studentId" type="number" /></label>
        <button class="primary" @click="reload">刷新</button>
      </div>
    </header>

    <section class="panel">
      <h3>视频完成率</h3>
      <ul class="list">
        <li v-for="v in videos" :key="v.videoId">
          <span>{{ v.title }}</span>
          <span class="tag">{{ v.completionRate }}% · {{ v.watchDuration }} 秒</span>
        </li>
        <li v-if="!videos.length" class="ghost center">暂无视频记录</li>
      </ul>
    </section>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import { BASE_URL } from "../../utils/api";
import { getSession } from "../../utils/session";

const session = getSession() || {};
const filters = reactive({
  courseId: session.selectedCourse?.id || session.courses?.[0]?.id || 0,
  studentId: session.user?.id || 0,
});
const videos = ref([]);

async function reload() {
  if (!filters.courseId || !filters.studentId) {
    videos.value = [];
    return;
  }
  await fetchVideos();
}

async function fetchJson(url, fallback) {
  try {
    const resp = await fetch(url);
    if (!resp.ok) throw new Error("网络错误");
    return await resp.json();
  } catch (e) {
    console.warn("使用示例数据", url, e);
    return fallback;
  }
}

async function fetchVideos() {
  const url = `${BASE_URL}/api/analysis/video/completion?courseId=${filters.courseId}&studentId=${filters.studentId}`;
  const data = await fetchJson(url, { data: { videoStats: [] } });
  videos.value = data.data?.videoStats || [
    { videoId: 1, title: "神经网络基础", completionRate: 85, watchDuration: 1200 },
    { videoId: 2, title: "梯度下降算法", completionRate: 78, watchDuration: 900 },
  ];
}

reload();
</script>

<style scoped>
.top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}
.sub {
  color: #64748b;
}
.filters {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  align-items: center;
}
input {
  width: 110px;
  padding: 8px 10px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
}
.primary {
  padding: 10px 12px;
  border-radius: 10px;
  border: none;
  background: linear-gradient(135deg, #2563eb, #7c3aed);
  color: #fff;
  font-weight: 700;
  cursor: pointer;
}
.panel {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  padding: 14px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05);
  margin-top: 12px;
}
.list {
  list-style: none;
  padding: 0;
  margin: 8px 0 0;
  display: grid;
  gap: 8px;
}
.list li {
  padding: 10px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  background: #f8fafc;
  display: flex;
  justify-content: space-between;
}
.tag {
  padding: 6px 10px;
  border-radius: 10px;
  background: #e0f2fe;
  color: #0369a1;
  font-size: 12px;
}
.ghost {
  color: #94a3b8;
}
.center {
  justify-content: center;
}
</style>
