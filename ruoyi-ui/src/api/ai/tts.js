import request from '@/utils/request'

/**
 * 文字转语音
 * @param {string} text - 要转换的文本
 * @param {string} voice - 语音类型（可选）
 */
export function textToSpeech(text, voice = 'CHERRY') {
  return request({
    url: '/ai/tts/convert',
    method: 'post',
    data: {
      text,
      voice
    }
  })
}
