import http from './http'

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export function chatWithAi(message: string) {
  return http.post<unknown, ApiResponse<{ reply: string }>>('/ai/chat', { message })
}
