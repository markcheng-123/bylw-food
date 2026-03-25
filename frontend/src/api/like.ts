import http from './http'

export interface LikeStatus {
  liked: boolean
  likeCount: number
}

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export function likePost(postId: number) {
  return http.post<unknown, ApiResponse<LikeStatus>>(`/posts/${postId}/like`)
}

export function unlikePost(postId: number) {
  return http.delete<unknown, ApiResponse<LikeStatus>>(`/posts/${postId}/like`)
}
