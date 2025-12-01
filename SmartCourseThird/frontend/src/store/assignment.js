import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import {
  getAssignmentList,
  createAssignment,
  updateAssignment,
  deleteAssignment,
  publishAssignment,
  unpublishAssignment,
  getAssignmentDetail
} from '../api/assignment'

export const useAssignmentStore = defineStore('assignment', () => {
  // State
  const assignments = ref([])
  const total = ref(0)
  const loading = ref(false)
  const pagination = ref({
    page: 1,
    pageSize: 10,
    totalPages: 0
  })

  // Getters
  const hasAssignments = computed(() => assignments.value.length > 0)
  const draftAssignments = computed(() => 
    assignments.value.filter(a => a.status === 0 || a.status === 'draft')
  )
  const publishedAssignments = computed(() => 
    assignments.value.filter(a => a.status === 1 || a.status === 'published')
  )

  // Actions
  async function fetchAssignments(courseId, params = {}) {
    loading.value = true
    try {
      const queryParams = {
        courseId,
        page: pagination.value.page,
        pageSize: pagination.value.pageSize,
        ...params
      }
      const res = await getAssignmentList(queryParams)
      if (res.code === 200) {
        assignments.value = res.data?.items || []
        total.value = res.data?.pagination?.total || 0
        pagination.value = {
          ...pagination.value,
          ...res.data?.pagination
        }
      }
      return res
    } catch (error) {
      console.error('获取作业失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  async function addAssignment(data) {
    const res = await createAssignment(data)
    return res
  }

  async function editAssignment(id, data) {
    const res = await updateAssignment(id, data)
    return res
  }

  async function removeAssignment(id) {
    const res = await deleteAssignment(id)
    return res
  }

  async function publish(data) {
    const res = await publishAssignment(data)
    return res
  }

  async function unpublish(id) {
    const res = await unpublishAssignment(id)
    return res
  }

  async function fetchDetail(id) {
    const res = await getAssignmentDetail(id)
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
    assignments.value = []
    total.value = 0
    pagination.value = { page: 1, pageSize: 10, totalPages: 0 }
  }

  return {
    assignments,
    total,
    loading,
    pagination,
    hasAssignments,
    draftAssignments,
    publishedAssignments,
    fetchAssignments,
    addAssignment,
    editAssignment,
    removeAssignment,
    publish,
    unpublish,
    fetchDetail,
    setPage,
    setPageSize,
    reset
  }
})
