import axios from 'axios'
import { clearAuthSession, getToken } from '@/stores/auth'

interface ApiEnvelope<T> {
  code: number
  message: string
  data: T
}

const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL as string) || 'http://localhost:8080/api'

const http = axios.create({
  baseURL: apiBaseUrl,
  timeout: 10000,
})

http.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => {
    const payload = response.data as ApiEnvelope<unknown>
    if (payload && typeof payload.code === 'number' && payload.code !== 200) {
      throw new Error(payload.message || '请求失败')
    }
    return payload as never
  },
  (error) => {
    if (error.response?.status === 401) {
      clearAuthSession()
      if (!window.location.pathname.startsWith('/login')) {
        window.location.href = '/login'
      }
    }
    const message = error.response?.data?.message || error.message || '请求失败'
    return Promise.reject(new Error(message))
  },
)

export default http
