import request from '@/utils/request'

export function login(username, password) {
  const data = new URLSearchParams()
  data.append('username', username)
  data.append('password', password)
  return request.post('/system/auth/login', data, {
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
  })
}

export function logout() {
  return request.post('/system/auth/logout')
}

export function getCurrentUser() {
  return request.get('/system/auth/current')
}

export function getOrgTree() {
  return request.get('/system/org/tree')
}

export function getOrgChildren(parentId) {
  return request.get(`/system/org/children/${parentId}`)
}

export function getUserPage(params) {
  return request.get('/system/user/page', { params })
}

export function getUserById(id) {
  return request.get(`/system/user/${id}`)
}

export function addUser(data) {
  return request.post('/system/user', data)
}

export function updateUser(data) {
  return request.put('/system/user', data)
}

export function deleteUser(id) {
  return request.delete(`/system/user/${id}`)
}

export function resetPassword(id, oldPassword, newPassword) {
  return request.post(`/system/user/resetPassword/${id}?oldPassword=${oldPassword}&newPassword=${newPassword}`)
}

export function changePassword(id, oldPassword, newPassword) {
  return request.post(`/system/user/changePassword?id=${id}&oldPassword=${oldPassword}&newPassword=${newPassword}`)
}

export function getRoleList() {
  return request.get('/system/role/list')
}

export function getRolePage() {
  return request.get('/system/role/page')
}

export function addRole(data) {
  return request.post('/system/role', data)
}

export function updateRole(data) {
  return request.put('/system/role', data)
}

export function deleteRole(id) {
  return request.delete(`/system/role/${id}`)
}

export function getMenuTree() {
  return request.get('/system/menu/tree')
}

export function getPeriodList() {
  return request.get('/system/period/list')
}

export function getPeriodCurrent() {
  return request.get('/system/period/current')
}

export function addPeriod(data) {
  return request.post('/system/period', data)
}

export function updatePeriod(data) {
  return request.put('/system/period', data)
}

export function closePeriod(id) {
  return request.post(`/system/period/close/${id}`)
}

export function addOrg(data) {
  return request.post('/system/org', data)
}

export function updateOrg(data) {
  return request.put('/system/org', data)
}

export function deleteOrg(id) {
  return request.delete(`/system/org/${id}`)
}
