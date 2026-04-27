import axios from 'axios'
import Cookies from 'js-cookie'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: import.meta.env.DEV ? 'http://localhost:18080' : '/api',
  timeout: 30000
})

// 请求拦截器：注入Token
request.interceptors.request.use(
  (config) => {
    const token = Cookies.get('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器：统一处理401/错误
request.interceptors.response.use(
  (response) => response.data,
  (error) => {
    if (error.response?.status === 401) {
      Cookies.remove('token')
      window.location.href = '/login'
    } else {
      ElMessage.error(error.response?.data?.message || '请求失败')
    }
    return Promise.reject(error)
  }
)

export default request
