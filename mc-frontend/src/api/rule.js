import request from '@/utils/request'

export function getRuleList(params) {
  return request.get('/rule/list', { params })
}

export function createRule(data) {
  return request.post('/rule', data)
}

export function updateRule(data) {
  return request.put('/rule', data)
}

export function publishRule(id) {
  return request.post(`/rule/publish/${id}`)
}

export function executeRule(data) {
  return request.post('/rule/execute', data)
}

export function validateExpression(expression) {
  return request.post('/rule/validate', null, { params: { expression } })
}
