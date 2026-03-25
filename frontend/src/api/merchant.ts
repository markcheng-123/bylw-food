import http from './http'
import type { MerchantApplicationItem } from './admin'

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export interface ChefInfo {
  id: number
  chefName: string
  title: string | null
  avatarUrl: string | null
  intro: string | null
  sort: number
  createdAt: string
}

export interface MerchantProfile {
  id: number
  userId: number
  merchantName: string | null
  storeName: string | null
  storeAddress: string | null
  averageCost: number | null
  businessLicenseUrl: string | null
  foodSafetyCertUrl: string | null
  chefTeamIntro: string | null
  status: number
  rejectReason: string | null
  lastAuditedAt: string | null
  updatedAt: string
  chefs: ChefInfo[]
}

export interface MerchantCard {
  profileId: number
  merchantUserId: number
  merchantName: string | null
  status: number | null
  storeName: string | null
  storeAddress: string | null
  averageCost: number | null
  businessLicenseUrl: string | null
  foodSafetyCertUrl: string | null
  chefTeamIntro: string | null
  chefs: ChefInfo[]
  hotDishes: string[]
}

export function submitMerchantApplication(payload: {
  merchantName: string
  contactName: string
  contactPhone: string
  businessLicense?: string
  province?: string
  detailAddress?: string
  address?: string
  description?: string
}) {
  return http.post<unknown, ApiResponse<MerchantApplicationItem>>('/merchant-applications', payload)
}

export function fetchMyMerchantApplications() {
  return http.get<unknown, ApiResponse<MerchantApplicationItem[]>>('/merchant-applications/mine')
}

export function fetchMyMerchantProfile() {
  return http.get<unknown, ApiResponse<MerchantProfile>>('/merchant-profiles/me')
}

export function updateMyMerchantProfile(payload: {
  merchantName?: string
  storeName?: string
  storeAddress?: string
  averageCost?: number
  businessLicenseUrl?: string
  foodSafetyCertUrl?: string
  chefTeamIntro?: string
}) {
  return http.put<unknown, ApiResponse<MerchantProfile>>('/merchant-profiles/me', payload)
}

export function fetchMyChefs() {
  return http.get<unknown, ApiResponse<ChefInfo[]>>('/merchant-profiles/me/chefs')
}

export function createChef(payload: {
  chefName: string
  title?: string
  avatarUrl?: string
  intro?: string
  sort?: number
}) {
  return http.post<unknown, ApiResponse<ChefInfo>>('/merchant-profiles/me/chefs', payload)
}

export function updateChef(
  chefId: number,
  payload: {
    chefName: string
    title?: string
    avatarUrl?: string
    intro?: string
    sort?: number
  },
) {
  return http.put<unknown, ApiResponse<ChefInfo>>(`/merchant-profiles/me/chefs/${chefId}`, payload)
}

export function deleteChef(chefId: number) {
  return http.delete<unknown, ApiResponse<null>>(`/merchant-profiles/me/chefs/${chefId}`)
}

export function fetchMerchantCard(merchantUserId: number) {
  return http.get<unknown, ApiResponse<MerchantCard>>(`/merchant-profiles/card/${merchantUserId}`)
}
