import http from './http'

export interface UploadResult {
  fileName: string
  url: string
}

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export function uploadImage(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return http.post<unknown, ApiResponse<UploadResult>>('/uploads/image', formData, {
    timeout: 120000,
  })
}
