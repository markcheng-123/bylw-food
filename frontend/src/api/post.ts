import http from './http'

export interface CreateFoodPostPayload {
  categoryId: number
  title: string
  summary?: string
  content: string
  address?: string
  detailAddress?: string
  regionCode?: string[]
  perCapita?: number
  imageUrls: string[]
  dishes?: {
    name: string
    ingredients: string[]
    imageUrls?: string[]
    allergens?: string[]
  }[]
}

export interface FoodPostResult {
  id: number
  categoryId: number
  title: string
  summary: string | null
  content: string
  coverImage: string | null
  address: string | null
  perCapita: number | null
  status: number
  createdAt: string
  imageUrls: string[]
}

export interface FoodPostDetail {
  id: number
  authorUserId: number
  categoryId: number
  categoryName: string
  title: string
  summary: string | null
  content: string
  coverImage: string | null
  address: string | null
  perCapita: number | null
  status: number
  viewCount: number
  likeCount: number
  commentCount: number
  authorNickname: string
  likedByCurrentUser: boolean
  certifiedMerchant: boolean
  merchantLabel: string | null
  merchantProfileId: number | null
  merchantUserId: number | null
  merchantName: string | null
  createdAt: string
  imageUrls: string[]
  dishes: {
    name: string
    ingredients: string[]
    imageUrls: string[]
    allergens: string[]
  }[]
}

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
  certifiedMerchant: boolean
  merchantLabel: string | null
  merchantProfileId: number | null
  merchantUserId: number | null
  merchantName: string | null
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

export function createFoodPost(payload: CreateFoodPostPayload) {
  return http.post<unknown, ApiResponse<FoodPostResult>>('/posts', payload)
}

export function fetchFoodPostList(params: {
  categoryId?: number
  keyword?: string
  regionCode?: string
  current?: number
  size?: number
}) {
  return http.get<unknown, ApiResponse<PageResult<FoodPostCard>>>('/posts', { params })
}

export function fetchFoodPostDetail(id: number) {
  return http.get<unknown, ApiResponse<FoodPostDetail>>(`/posts/${id}`)
}

export function updateFoodPost(
  id: number,
  payload: {
    categoryId: number
    title: string
    summary?: string
    content: string
    address?: string
    perCapita?: number
  },
) {
  return http.put<unknown, ApiResponse<FoodPostResult>>(`/posts/${id}`, payload)
}
