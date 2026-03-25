import http from './http'

export interface StrategyCard {
  id: number
  title: string
  summary: string | null
  coverImage: string | null
  authorNickname: string
  sort: number
  publishedAt: string | null
  createdAt: string
}

export interface StrategyDetail {
  id: number
  title: string
  summary: string | null
  content: string
  coverImage: string | null
  authorNickname: string
  publishedAt: string | null
  createdAt: string
}

export interface PageResult<T> {
  total: number
  records: T[]
}

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export function fetchStrategyList(params: { keyword?: string; current?: number; size?: number }) {
  return http.get<unknown, ApiResponse<PageResult<StrategyCard>>>('/strategies', { params })
}

export function fetchStrategyDetail(id: number) {
  return http.get<unknown, ApiResponse<StrategyDetail>>(`/strategies/${id}`)
}
