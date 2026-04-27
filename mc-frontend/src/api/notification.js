import request from '@/utils/request'

export function sendNotification(data) {
  return request.post('/notification/send', data)
}

export function getTemplateList(params) {
  return request.get('/notification/template/list', { params })
}

export function createTemplate(data) {
  return request.post('/notification/template', data)
}

export function updateTemplate(data) {
  return request.put('/notification/template', data)
}

export function deleteTemplate(id) {
  return request.delete(`/notification/template/${id}`)
}

export function getRecordList(params) {
  return request.get('/notification/record/list', { params })
}

export function getStatistics(params) {
  return request.get('/notification/statistics', { params })
}

export function retrySend(id) {
  return request.post(`/notification/retry/${id}`)
}
