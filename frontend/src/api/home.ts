import http from './http'
import type { CategoryItem } from './category'

export interface FoodPostCard {
  id: number
  categoryId: number
  categoryName: string
  title: string
  summary: string | null
  coverImage: string | null
  address: string | null
  perCapita: number | null
  status: number
  viewCount: number
  likeCount: number
  commentCount: number
  heatScore: number
  likedByCurrentUser: boolean
  certifiedMerchant: boolean
  merchantLabel: string | null
  merchantProfileId: number | null
  merchantUserId: number | null
  merchantName: string | null
  createdAt: string
}

export interface HomeData {
  categories: CategoryItem[]
  hotPosts: FoodPostCard[]
  latestPosts: FoodPostCard[]
}

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export function fetchHomeData() {
  return http.get<unknown, ApiResponse<HomeData>>('/home')
}
