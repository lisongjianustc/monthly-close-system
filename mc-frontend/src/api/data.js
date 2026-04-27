import request from '@/utils/request'

export function uploadFile(formData) {
  return request.post('/data/import/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function executeImport(batchId, templateCode) {
  return request.post(`/data/import/execute/${batchId}?templateCode=${templateCode || ''}`)
}

export function getImportProgress(batchId) {
  return request.get(`/data/import/progress/${batchId}`)
}

export function getBatchRecords(batchId, params) {
  return request.get(`/data/import/records/${batchId}`, { params })
}

export function getFailRecords(batchId) {
  return request.get(`/data/import/failures/${batchId}`)
}

export function validateData(batchId) {
  return request.post(`/data/import/validate/${batchId}`)
}

export function cancelImport(batchId) {
  return request.post(`/data/import/cancel/${batchId}`)
}

export function getTemplateList() {
  return request.get('/data/import/template/list')
}
