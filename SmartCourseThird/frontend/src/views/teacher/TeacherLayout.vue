<script setup>
import { computed, ref } from "vue";
import { useRouter } from "vue-router";
import { clearSession, getSession } from "../../utils/session";

const router = useRouter();
const session = ref(getSession() || {});
const user = computed(() => session.value.user || {});
const course = computed(() => session.value.selectedCourse);
const displayName = computed(() => user.value.realName || user.value.username || "教师");

function logout() {
  clearSession();
  router.replace("/login");
}

function switchCourse() {
  router.push("/courses");
}
</script>

<template>
  <div class="layout">
    <aside class="sidebar">
      <div class="brand">
        <div class="title">教师端</div>
        <p class="meta">{{ displayName }}</p>
        <p class="course" v-if="course">当前课程：{{ course.title }}</p>
      </div>
      <nav>
        
        <router-link to="/teacher/assignments" class="nav" active-class="active">作业 / 成绩</router-link>
        <router-link to="/teacher/analysis" class="nav" active-class="active">学情分析</router-link>
        <router-link to="/teacher/video" class="nav" active-class="active">视频学习分析</router-link>
        <router-link to="/teacher/grading" class="nav" active-class="active">智能批改</router-link>
        <router-link to="/teacher/question-bank" class="nav" active-class="active">题库管理</router-link>
      </nav>
      <div class="actions">
        <button class="ghost" @click="switchCourse">切换课程</button>
        <button class="ghost danger" @click="logout">退出</button>
      </div>
    </aside>
    <main class="content">
      <router-view />
    </main>
  </div>
</template>

<style scoped>
.layout {
  display: grid;
  grid-template-columns: 260px 1fr;
  min-height: 100vh;
}
.sidebar {
  background: #0f172a;
  color: #e2e8f0;
  padding: 20px 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  position: relative;
  z-index: 1000;
}
.brand {
  font-weight: 800;
  font-size: 18px;
  margin-bottom: 8px;
}
.brand .meta {
  margin: 4px 0 2px;
  color: #cbd5e1;
  font-size: 13px;
  font-weight: 400;
}
.brand .course {
  margin: 0;
  color: #e2e8f0;
  font-size: 13px;
}
.nav {
  display: block;
  padding: 10px 12px;
  border-radius: 10px;
  text-decoration: none;
  color: inherit;
  border: 1px solid transparent;
}
.nav.active {
  background: rgba(59, 130, 246, 0.15);
  border-color: rgba(59, 130, 246, 0.4);
}
.actions {
  margin-top: auto;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.ghost {
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.16);
  color: inherit;
  padding: 10px 12px;
  border-radius: 10px;
  cursor: pointer;
}
.ghost.danger {
  border-color: rgba(248, 113, 113, 0.5);
  color: #fecdd3;
}
.content {
  background: #f8fafc;
  padding: 20px 24px 40px;
}
@media (max-width: 900px) {
  .layout {
    grid-template-columns: 1fr;
  }
  .sidebar {
    flex-direction: row;
    align-items: center;
    overflow-x: auto;
  }
  .nav {
    white-space: nowrap;
  }
}
</style>
