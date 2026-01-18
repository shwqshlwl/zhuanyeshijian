import request from '@/utils/request'

export function chatWithAi(data) {
  return request({
    url: '/ai/chat',
    method: 'post',
    data
  })
}
