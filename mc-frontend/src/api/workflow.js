import request from '@/utils/request'

export function getTemplateList() {
  return request.get('/workflow/template/list')
}

export function getTemplateById(id) {
  return request.get(`/workflow/template/${id}`)
}

export function createTemplate(data) {
  return request.post('/workflow/template', data)
}

export function updateTemplate(data) {
  return request.put('/workflow/template', data)
}

export function publishTemplate(id) {
  return request.post(`/workflow/template/publish/${id}`)
}

export function deleteTemplate(id) {
  return request.delete(`/workflow/template/${id}`)
}

export function instantiate(templateId, periodId, unitId) {
  return request.post('/workflow/instance/instantiate', { templateId, periodId, unitId })
}

export function getInstanceList(params) {
  return request.get('/workflow/instance/list', { params })
}

export function getInstanceById(id) {
  return request.get(`/workflow/instance/${id}`)
}

export function getTaskList(params) {
  return request.get('/workflow/task/list', { params })
}

export function getPendingTasks(assigneeId) {
  return request.get(`/workflow/task/pending/${assigneeId}`)
}

export function getOverdueTasks() {
  return request.get('/workflow/task/overdue')
}

export function startTask(id) {
  return request.put(`/workflow/task/start/${id}`)
}

export function submitTask(id, comment) {
  return request.put(`/workflow/task/submit/${id}?comment=${comment}`)
}

export function approveTask(id, comment) {
  return request.put(`/workflow/task/approve/${id}?comment=${comment}`)
}

export function rejectTask(id, comment) {
  return request.put(`/workflow/task/reject/${id}?comment=${comment}`)
}

export function getTaskTrace(taskId) {
  return request.get(`/workflow/task/trace/${taskId}`)
}
