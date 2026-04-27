import { defineStore } from 'pinia'
import { ref } from 'vue'
import Cookies from 'js-cookie'
import { login as apiLogin, logout as apiLogout, getCurrentUser } from '@/api/system'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(Cookies.get('token') || '')
  const userInfo = ref(null)

  async function login(username, password) {
    const res = await apiLogin(username, password)
    if (res.code === 0 && res.data?.token) {
      token.value = res.data.token
      Cookies.set('token', res.data.token, { expires: 7 })
      await fetchUserInfo()
      return true
    }
    return false
  }

  async function logout() {
    try {
      await apiLogout()
    } finally {
      token.value = ''
      userInfo.value = null
      Cookies.remove('token')
    }
  }

  async function fetchUserInfo() {
    const res = await getCurrentUser()
    if (res.code === 0) {
      userInfo.value = res.data
    }
  }

  return { token, userInfo, login, logout, fetchUserInfo }
})
