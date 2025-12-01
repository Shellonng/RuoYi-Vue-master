import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getSession, getSelectedCourse } from '../utils/session'

export const useCourseStore = defineStore('course', () => {
  // State
  const currentCourse = ref(null)
  const currentUser = ref(null)

  // Getters
  const courseId = computed(() => currentCourse.value?.id || null)
  const userId = computed(() => currentUser.value?.id || null)
  const courseName = computed(() => currentCourse.value?.title || '未选择课程')

  // Actions
  function loadFromSession() {
    const session = getSession()
    currentUser.value = session?.user || null
    currentCourse.value = session?.selectedCourse || null
  }

  function setCourse(course) {
    currentCourse.value = course
  }

  function setUser(user) {
    currentUser.value = user
  }

  return {
    currentCourse,
    currentUser,
    courseId,
    userId,
    courseName,
    loadFromSession,
    setCourse,
    setUser
  }
})
