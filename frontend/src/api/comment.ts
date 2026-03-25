import http from './http'

export interface CommentItem {
  id: number
  postId: number
  parentId: number | null
  content: string
  createdAt: string
  authorId: number
  authorNickname: string
  authorAvatar: string | null
  replies: CommentItem[]
}

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export function fetchComments(postId: number) {
  return http.get<unknown, ApiResponse<CommentItem[]>>(`/posts/${postId}/comments`)
}

export function createComment(postId: number, payload: { content: string; parentId?: number }) {
  return http.post<unknown, ApiResponse<CommentItem>>(`/posts/${postId}/comments`, payload)
}
