<template>
  <div>
    <header class="top">
      <div>
        <h1>我的课程</h1>
        <p class="sub">以下课程来自教育平台登录返回的数据。</p>
      </div>
      <div class="info">
        <span class="badge">共 {{ courses.length }} 门</span>
        <span v-if="currentCourse" class="current">当前课程：{{ currentCourse.title }}</span>
      </div>
    </header>

    <section class="cards">
      <div class="card" v-for="course in courses" :key="course.id">
        <div class="title">{{ course.title }}</div>
        <div class="desc">老师：{{ course.teacherUserId || "--" }}</div>
        <div class="meta">
          学期 {{ course.term || "未设置" }} · 学生 {{ course.studentCount ?? 0 }} · 状态 {{ course.status || "未设置" }}
        </div>
      </div>
      <div v-if="!courses.length" class="ghost">暂无课程，请先登录</div>
    </section>
  </div>
</template>

<script setup>
import { computed, ref } from "vue";
import { getSession } from "../../utils/session";

const session = getSession() || {};
const courses = ref(session.courses || []);
const currentCourse = computed(() => session.selectedCourse || courses.value[0] || null);
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
.info {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  align-items: center;
}
.badge,
.current {
  padding: 8px 12px;
  border-radius: 10px;
  background: #e0f2fe;
  color: #0369a1;
  font-weight: 700;
}
.cards {
  display: grid;
  gap: 12px;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  margin-top: 14px;
}
.card {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05);
}
.title {
  font-weight: 700;
}
.desc,
.meta {
  color: #64748b;
  font-size: 13px;
}
.ghost {
  color: #94a3b8;
}
</style>
