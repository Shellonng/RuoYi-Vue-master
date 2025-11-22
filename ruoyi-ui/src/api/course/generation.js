import request from '@/utils/request'

/**
 * 上传文档并生成课程结构
 * @param {File} file - 文档文件（支持PDF、Word、PPT）
 * @param {Number} courseId - 课程ID
 * @param {String} courseName - 课程名称
 */
export function uploadAndGenerate(file, courseId, courseName) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('courseName', courseName)
  
  return request({
    url: '/course/generation/uploadAndGenerate/' + courseId,
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    timeout: 300000 // 5分钟超时，因为AI处理需要时间
  })
}

/**
 * 仅解析文档（不保存到数据库）- 用于预览
 * @param {File} file - 文档文件（支持PDF、Word、PPT）
 * @param {String} courseName - 课程名称
 */
export function parseOnly(file, courseName) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('courseName', courseName)
  
  return request({
    url: '/course/generation/parseOnly',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    timeout: 180000
  })
}

/**
 * AI生成课程描述
 * @param {String} courseTitle - 课程标题
 */
export function generateCourseDescription(courseTitle) {
  return request({
    url: '/course/generation/generateDescription',
    method: 'post',
    data: {
      courseTitle: courseTitle
    },
    timeout: 60000 // 1分钟超时
  })
}
