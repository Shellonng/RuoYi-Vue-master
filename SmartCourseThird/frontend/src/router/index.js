import { createRouter, createWebHistory } from "vue-router";
import LoginPage from "../views/LoginPage.vue";
import TeacherLayout from "../views/teacher/TeacherLayout.vue";
import TeacherAssignments from "../views/teacher/TeacherAssignments.vue";
import StudentAssignments from "../views/student/StudentAssignments.vue";
import StudentQuestionBank from "../views/student/StudentQuestionBank.vue";
import StudentAnalysis from "../views/student/StudentAnalysis.vue";
import StudentVideo from "../views/student/StudentVideo.vue";
import TeacherAnalysis from "../views/teacher/TeacherAnalysis.vue";
import TeacherVideo from "../views/teacher/TeacherVideo.vue";
import TeacherGrading from "../views/teacher/TeacherGrading.vue";
import TeacherGradingDetail from "../views/teacher/TeacherGradingDetail.vue";
import TeacherQuestionBank from "../views/teacher/TeacherQuestionBank.vue";
import TeacherSmartPaper from "../views/teacher/TeacherSmartPaper.vue";
import StudentLayout from "../views/student/StudentLayout.vue";
import StudentDashboard from "../views/student/StudentDashboard.vue";
import StudentScores from "../views/student/StudentScores.vue";
import StudentProgress from "../views/student/StudentProgress.vue";
import CourseSelect from "../views/CourseSelect.vue";
import { getSession } from "../utils/session";

const routes = [
  { path: "/", redirect: "/login" },
  { path: "/login", name: "Login", component: LoginPage },
  { path: "/courses", name: "CourseSelect", component: CourseSelect },
  {
    path: "/teacher",
    component: TeacherLayout,
    children: [
      { path: "", redirect: "assignments" },
      { path: "assignments", name: "TeacherAssignments", component: TeacherAssignments },
      { path: "analysis", name: "TeacherAnalysis", component: TeacherAnalysis },
      { path: "video", name: "TeacherVideo", component: TeacherVideo },
      { path: "grading", name: "TeacherGrading", component: TeacherGrading },
      { path: "grading/:submissionId", name: "TeacherGradingDetail", component: TeacherGradingDetail },
      { path: "question-bank", name: "TeacherQuestionBank", component: TeacherQuestionBank },
      { path: "smart-paper", name: "TeacherSmartPaper", component: TeacherSmartPaper },
    ],
  },
  {
    path: "/student",
    component: StudentLayout,
    children: [
      { path: "", name: "StudentDashboard", component: StudentDashboard },
      { path: "scores", name: "StudentScores", component: StudentScores },
      { path: "progress", name: "StudentProgress", component: StudentProgress },
	  { path: "assignments", name: "StudentAssignments", component: StudentAssignments },
  	  { path: "question-bank", name: "StudentQuestionBank", component: StudentQuestionBank },
	  { path: "analysis", name: "StudentAnalysis", component: StudentAnalysis },
	  { path: "video", name: "StudentVideo", component: StudentVideo },
    ],
  },
  // { path: "/:pathMatch(.*)*", redirect: "/login" },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to, from, next) => {
  console.log(`[Router] Navigating from ${from.path} to ${to.path}`);
  if (to.path === "/login") {
    return next();
  }
  const session = getSession();
  console.log("[Router] Session check:", session);

  if (!session?.user) {
    console.warn("[Router] No user found in session. Redirecting to login.");
    return next({ path: "/login", query: { redirect: to.fullPath } });
  }
  if ((to.path.startsWith("/teacher") || to.path.startsWith("/student")) && !session.selectedCourse) {
    console.warn("[Router] No selected course. Redirecting to /courses.");
    return next("/courses");
  }
  if (to.path === "/courses" && !session?.courses?.length) {
    console.warn("[Router] No courses found. Redirecting to login.");
    return next("/login");
  }
  next();
});

export default router;