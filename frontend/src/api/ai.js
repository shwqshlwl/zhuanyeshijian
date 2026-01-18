import request from '@/utils/request'

export function chatWithAi(data) {
  return request({
    url: '/ai/chat',
    method: 'post',
    data
  })
}

export function getAiSessionList(params) {
  return request({
    url: '/ai/sessions',
    method: 'get',
    params
  })
}

export function getAiSessionMessages(sessionId) {
  return request({
    url: `/ai/sessions/${sessionId}/messages`,
    method: 'get'
  })
}

export function deleteAiSession(sessionId) {
  return request({
    url: `/ai/sessions/${sessionId}`,
    method: 'delete'
  })
}
