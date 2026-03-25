import http from './http'

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export interface HealthInfo {
  project: string
  status: string
  stage: string
}

export function getHealth() {
  return http.get<unknown, ApiResponse<HealthInfo>>('/system/health')
}
