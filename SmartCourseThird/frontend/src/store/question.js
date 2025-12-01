import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import {
  getQuestions,
  createQuestion,
  updateQuestion,
  deleteQuestion,
  batchDeleteQuestions,
  getQuestionDetail,
  getQuestionTypeStatistics,
  getDifficultyStatistics,
  getKnowledgePointStatistics,
  importQuestions,
  getKnowledgePoints
} from '../api/question'

export const useQuestionStore = defineStore('question', () => {
  // State
  const questions = ref([])
  const total = ref(0)
  const loading = ref(false)
  const pagination = ref({
    page: 1,
    pageSize: 10,
    totalPages: 0
  })
  
  // 统计数据
  const typeStats = ref([])
  const difficultyStats = ref([])
  const kpStats = ref([])
  const knowledgePoints = ref([])

  // Getters
  const hasQuestions = computed(() => questions.value.length > 0)

  // Actions
  async function fetchQuestions(courseId, params = {}) {
    loading.value = true
    try {
      const queryParams = {
        courseId,
        page: pagination.value.page,
        pageSize: pagination.value.pageSize,
        ...params
      }
      const res = await getQuestions(queryParams)
      if (res.code === 200) {
        questions.value = res.data?.items || []
        total.value = res.data?.pagination?.total || 0
        pagination.value = {
          ...pagination.value,
          ...res.data?.pagination
        }
      }
      return res
    } catch (error) {
      console.error('获取题目失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  async function addQuestion(data) {
    const res = await createQuestion(data)
    return res
  }

  async function editQuestion(id, data) {
    const res = await updateQuestion(id, data)
    return res
  }

  async function removeQuestion(id) {
    const res = await deleteQuestion(id)
    return res
  }

  async function removeQuestions(ids) {
    const res = await batchDeleteQuestions(ids)
    return res
  }

  async function fetchQuestionDetail(id) {
    const res = await getQuestionDetail(id)
    return res
  }

  async function fetchStatistics(courseId) {
    const [typeRes, diffRes, kpRes] = await Promise.all([
      getQuestionTypeStatistics(courseId),
      getDifficultyStatistics(courseId),
      getKnowledgePointStatistics(courseId)
    ])
    typeStats.value = typeRes.data || []
    difficultyStats.value = diffRes.data || []
    kpStats.value = kpRes.data || []
  }

  async function fetchKnowledgePoints(courseId) {
    const res = await getKnowledgePoints(courseId)
    if (res.code === 200) {
      knowledgePoints.value = res.data || []
    }
  }

  async function batchImport(data) {
    const res = await importQuestions(data)
    return res
  }

  function setPage(page) {
    pagination.value.page = page
  }

  function setPageSize(size) {
    pagination.value.pageSize = size
    pagination.value.page = 1
  }

  function reset() {
    questions.value = []
    total.value = 0
    pagination.value = { page: 1, pageSize: 10, totalPages: 0 }
  }

  return {
    questions,
    total,
    loading,
    pagination,
    typeStats,
    difficultyStats,
    kpStats,
    knowledgePoints,
    hasQuestions,
    fetchQuestions,
    addQuestion,
    editQuestion,
    removeQuestion,
    removeQuestions,
    fetchQuestionDetail,
    fetchStatistics,
    fetchKnowledgePoints,
    batchImport,
    setPage,
    setPageSize,
    reset
  }
})
