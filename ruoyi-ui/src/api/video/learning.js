import request from '@/utils/request'

// 获取今日活跃人数统计
export function getTodayActiveStats() {
  return request({
    url: '/video/learning/todayActiveStats',
    method: 'get'
  })
}

// 获取今日各视频分时活跃人数统计
export function getTodayActiveStatsByVideo() {
  return request({
    url: '/video/learning/todayActiveStatsByVideo',
    method: 'get'
  })
}

// 获取视频ID到小节标题的映射
export function getVideoTitles() {
  return request({
    url: '/video/learning/videoTitles',
    method: 'get'
  })
}
