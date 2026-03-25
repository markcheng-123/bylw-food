import http from './http'
import type { FoodPostCard } from './post'

export interface UserProfile {
  id: number
  username: string
  nickname: string
  avatar: string | null
  phone: string | null
  email: string | null
  role: number
  status: number
  createdAt: string
}

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export interface LoginPayload {
  username: string
  password: string
}

export interface RegisterPayload extends LoginPayload {
  nickname: string
  phone?: string
  email?: string
}

export interface UpdateProfilePayload {
  nickname: string
  avatar?: string
  phone?: string
  email?: string
}

export interface LoginResult {
  token: string
  userInfo: UserProfile
}

export interface UserCenterComment {
  id: number
  postId: number
  postTitle: string
  postCoverImage: string | null
  content: string
  parentId: number | null
  createdAt: string
}

export function registerUser(payload: RegisterPayload) {
  return http.post<unknown, ApiResponse<UserProfile>>('/users/register', payload)
}

export function loginUser(payload: LoginPayload) {
  return http.post<unknown, ApiResponse<LoginResult>>('/users/login', payload)
}

export function fetchMyProfile() {
  return http.get<unknown, ApiResponse<UserProfile>>('/users/me')
}

export function updateMyProfile(payload: UpdateProfilePayload) {
  return http.put<unknown, ApiResponse<UserProfile>>('/users/me', payload)
}

export function fetchMyPosts() {
  return http.get<unknown, ApiResponse<FoodPostCard[]>>('/users/me/posts')
}

export function fetchMyComments() {
  return http.get<unknown, ApiResponse<UserCenterComment[]>>('/users/me/comments')
}

export function fetchMyLikes() {
  return http.get<unknown, ApiResponse<FoodPostCard[]>>('/users/me/likes')
}
