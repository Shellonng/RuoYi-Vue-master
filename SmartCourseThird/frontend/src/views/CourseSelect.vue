<template>
  <div class="page">
    <header class="head">
      <div class="brand">
        <div class="logo-icon">SC</div>
        <div class="brand-text">
          <h1>SmartCourse</h1>
          <p>智能课程平台</p>
        </div>
      </div>
      
      <div class="user-profile">
        <div class="avatar">{{ userInitial }}</div>
        <div class="user-info">
          <div class="name">{{ user.realName || user.username }}</div>
          <div class="role">{{ roleLabel }}</div>
        </div>
        <button class="logout-btn" @click="logout" title="退出登录">
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path><polyline points="16 17 21 12 16 7"></polyline><line x1="21" y1="12" x2="9" y2="12"></line></svg>
        </button>
      </div>
    </header>

    <main class="main-content">
      <div class="welcome-section">
        <h2>欢迎回来，{{ user.realName || user.username }}</h2>
        <p class="subtitle">请选择您要进入的课程工作台</p>
      </div>

      <div class="course-grid">
        <div 
          v-for="course in courses" 
          :key="course.id" 
          class="course-card"
          @click="enterCourse(course)"
        >
          <div class="card-cover">
            <img :src="`https://picsum.photos/seed/${course.id}/400/250`" alt="Course Cover" loading="lazy" />
            <div class="card-overlay">
              <button class="enter-btn">进入课程</button>
            </div>
          </div>
          <div class="card-body">
            <div class="card-tags">
              <span class="tag term">{{ course.term || "2024秋季" }}</span>
              <span class="tag status" :class="course.status === '已结课' ? 'closed' : 'active'">
                {{ course.status || "进行中" }}
              </span>
            </div>
            <h3 class="card-title">{{ course.title }}</h3>
            <p class="card-desc">{{ course.description || "暂无课程简介..." }}</p>
            <div class="card-footer">
              <div class="stat">
                <span class="icon">👥</span>
                <span>{{ course.studentCount ?? 0 }} 学生</span>
              </div>
              <div class="stat">
                <span class="icon">🆔</span>
                <span>ID: {{ course.id }}</span>
              </div>
            </div>
          </div>
        </div>

        <div v-if="!courses.length" class="empty-state">
          <div class="empty-icon">📭</div>
          <h3>暂无课程</h3>
          <p>您当前还没有关联任何课程，请联系管理员。</p>
          <button class="ghost" @click="logout">切换账号</button>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { computed, ref } from "vue";
import { useRouter } from "vue-router";
import { clearSession, getSession, setSelectedCourse } from "../utils/session";

const router = useRouter();
const session = ref(getSession() || {});

const user = computed(() => session.value.user || {});
const courses = computed(() => session.value.courses || []);

const userInitial = computed(() => {
  const name = user.value.realName || user.value.username || "U";
  return name.charAt(0).toUpperCase();
});

const roleLabel = computed(() => {
  const role = (user.value.role || "").toLowerCase();
  if (role === "teacher") return "教师";
  if (role === "student") return "学生";
  return "用户";
});

function enterCourse(course) {
  console.log("[CourseSelect] Entering course:", course);
  const newSession = setSelectedCourse(course);
  console.log("[CourseSelect] New session after set:", newSession);
  
  if (!newSession.user) {
    console.error("[CourseSelect] CRITICAL: User missing in new session! Aborting navigation.");
    alert("系统错误：会话丢失，请重新登录");
    return;
  }

  session.value = newSession;
  const role = (user.value.role || "").toLowerCase();
  console.log("[CourseSelect] Navigating to role:", role);
  
  // Debug: Check if route exists
  const targetPath = role === "teacher" ? "/teacher/assignments" : "/student";
  const resolved = router.resolve(targetPath);
  console.log("[CourseSelect] Resolved route:", resolved);
  console.log("[CourseSelect] All routes:", router.getRoutes().map(r => r.path));

  router.push(targetPath).catch(err => {
    console.error("[CourseSelect] Navigation failed:", err);
  });
}

function logout() {
  clearSession();
  router.replace("/login");
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f8fafc;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
}

.head {
  height: 72px;
  background: #fff;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 40px;
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #4f46e5 0%, #3b82f6 100%);
  border-radius: 10px;
  color: #fff;
  font-weight: 800;
  font-size: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.3);
}

.brand-text h1 {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: #1e293b;
  line-height: 1.2;
}

.brand-text p {
  margin: 0;
  font-size: 12px;
  color: #64748b;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 6px 12px;
  background: #f1f5f9;
  border-radius: 30px;
  border: 1px solid #e2e8f0;
}

.avatar {
  width: 36px;
  height: 36px;
  background: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  color: #3b82f6;
  border: 2px solid #e2e8f0;
}

.user-info {
  display: flex;
  flex-direction: column;
}

.name {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
}

.role {
  font-size: 11px;
  color: #64748b;
}

.logout-btn {
  background: transparent;
  border: none;
  color: #94a3b8;
  cursor: pointer;
  padding: 8px;
  border-radius: 50%;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logout-btn:hover {
  background: #fee2e2;
  color: #ef4444;
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 24px;
}

.welcome-section {
  margin-bottom: 40px;
}

.welcome-section h2 {
  font-size: 28px;
  font-weight: 800;
  color: #1e293b;
  margin: 0 0 8px 0;
}

.subtitle {
  color: #64748b;
  font-size: 16px;
}

.course-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 32px;
}

.course-card {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid #e2e8f0;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  display: flex;
  flex-direction: column;
  position: relative;
}

.course-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 40px -10px rgba(0, 0, 0, 0.1);
  border-color: #cbd5e1;
}

.card-cover {
  height: 180px;
  position: relative;
  overflow: hidden;
  background: #e2e8f0;
}

.card-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.course-card:hover .card-cover img {
  transform: scale(1.05);
}

.card-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
  backdrop-filter: blur(2px);
}

.course-card:hover .card-overlay {
  opacity: 1;
}

.enter-btn {
  padding: 10px 24px;
  background: #fff;
  color: #0f172a;
  border: none;
  border-radius: 20px;
  font-weight: 700;
  font-size: 14px;
  transform: translateY(10px);
  transition: transform 0.3s ease;
}

.course-card:hover .enter-btn {
  transform: translateY(0);
}

.card-body {
  padding: 20px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.card-tags {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.tag {
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 600;
}

.tag.term {
  background: #f1f5f9;
  color: #64748b;
}

.tag.status {
  background: #dbeafe;
  color: #2563eb;
}

.tag.status.closed {
  background: #f1f5f9;
  color: #94a3b8;
}

.card-title {
  margin: 0 0 8px 0;
  font-size: 18px;
  font-weight: 700;
  color: #1e293b;
  line-height: 1.4;
}

.card-desc {
  color: #64748b;
  font-size: 13px;
  line-height: 1.6;
  margin: 0 0 20px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  flex: 1;
}

.card-footer {
  padding-top: 16px;
  border-top: 1px solid #f1f5f9;
  display: flex;
  justify-content: space-between;
  color: #94a3b8;
  font-size: 12px;
}

.stat {
  display: flex;
  align-items: center;
  gap: 6px;
}

.empty-state {
  grid-column: 1 / -1;
  text-align: center;
  padding: 60px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.empty-icon {
  font-size: 48px;
}

.empty-state h3 {
  margin: 0;
  color: #1e293b;
}

.empty-state p {
  color: #64748b;
  margin: 0;
}

.ghost {
  padding: 8px 16px;
  border: 1px solid #e2e8f0;
  background: #fff;
  border-radius: 8px;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s;
}

.ghost:hover {
  border-color: #cbd5e1;
  color: #1e293b;
}

@media (max-width: 768px) {
  .head {
    padding: 0 20px;
  }
  .main-content {
    padding: 24px 20px;
  }
  .user-profile .name, .user-profile .role {
    display: none;
  }
}
</style>
