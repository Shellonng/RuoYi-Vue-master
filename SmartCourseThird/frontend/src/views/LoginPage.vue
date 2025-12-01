<template>
  <div class="page">
    <div class="card">
      <div class="brand">SmartCourse</div>
      <h1>欢迎登录</h1>
      <p class="sub">使用 education_platform 的账号密码登录，自动识别身份并列出课程。</p>
      <form @submit.prevent="go">
        <label class="field">
          <span>账号</span>
          <input v-model="form.username" required placeholder="例如 teacher_zhang / student_li" autocomplete="username" />
        </label>
        <label class="field">
          <span>密码</span>
          <input v-model="form.password" required type="password" placeholder="请输入密码" autocomplete="current-password" />
        </label>
        <p v-if="error" class="error">{{ error }}</p>
        <button class="primary" type="submit" :disabled="loading">
          {{ loading ? "登录中..." : "进入系统" }}
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import { useRouter, useRoute } from "vue-router";
import { login } from "../utils/api";
import { setSession } from "../utils/session";

const router = useRouter();
const route = useRoute();
const form = reactive({ username: "", password: "" });
const loading = ref(false);
const error = ref("");

function resolveEntry(role) {
  return (role || "").toLowerCase() === "teacher" ? "/teacher" : "/student";
}

async function go() {
  if (!form.username || !form.password) return;
  loading.value = true;
  error.value = "";
  try {
    const data = await login(form.username.trim(), form.password);
    const payload = { user: data.user, courses: data.courses || [] };
    if (payload.courses.length === 1) {
      payload.selectedCourse = payload.courses[0];
    }
    setSession(payload);

    if (payload.courses.length === 1) {
      router.replace(resolveEntry(payload.user.role));
    } else {
      router.replace(route.query.redirect || "/courses");
    }
  } catch (e) {
    error.value = e.message || "登录失败";
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: radial-gradient(circle at 20% 20%, rgba(88, 142, 255, 0.12), transparent 35%),
    radial-gradient(circle at 80% 0%, rgba(255, 118, 168, 0.12), transparent 30%), #f6f8fb;
  padding: 20px;
  background-size: cover;
}

.card {
  width: 380px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.08);
  padding: 24px 24px 28px;
  transition: box-shadow 0.3s ease-in-out;
}

.card:hover {
  box-shadow: 0 25px 70px rgba(0, 0, 0, 0.1);
}

.brand {
  font-weight: 800;
  color: #2563eb;
  letter-spacing: 0.4px;
  font-size: 1.8rem;
}

h1 {
  margin: 12px 0 4px;
  font-size: 1.5rem;
}

.sub {
  color: #6b7280;
  margin: 0 0 16px;
}

.error {
  color: #ef4444;
  margin: 4px 0 8px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 14px;
  color: #111827;
}

input {
  padding: 10px 12px;
  border-radius: 10px;
  border: 1px solid #e5e7eb;
  font-size: 14px;
  transition: border-color 0.3s ease, box-shadow 0.3s ease;
}

input:focus {
  border-color: #2563eb;
  box-shadow: 0 0 8px rgba(37, 99, 235, 0.2);
}

.primary {
  width: 100%;
  padding: 12px 14px;
  border-radius: 12px;
  border: none;
  background: linear-gradient(135deg, #2563eb, #7c3aed);
  color: #fff;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 12px 30px rgba(37, 99, 235, 0.25);
  transition: transform 0.2s ease;
}

.primary:hover {
  transform: translateY(-2px);
}

.primary:active {
  transform: translateY(0);
}
</style>
