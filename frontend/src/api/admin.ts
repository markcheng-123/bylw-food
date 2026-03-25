import http from './http'

export interface AdminPostItem {
  id: number
  categoryId: number
  categoryName: string
  title: string
  summary: string | null
  content: string
  coverImage: string | null
  address: string | null
  perCapita: number | null
  status: number
  authorNickname: string
  createdAt: string
}

export interface MerchantApplicationItem {
  id: number
  userId: number
  applicantNickname: string
  merchantName: string
  contactName: string
  contactPhone: string
  businessLicense: string | null
  address: string
  description: string | null
  status: number
  rejectReason: string | null
  submittedAt: string
  auditedAt: string | null
  createdAt: string
}

export interface MerchantProfileItem {
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

export function fetchAdminPosts(params: { current?: number; size?: number; status?: number }) {
  return http.get<unknown, ApiResponse<PageResult<AdminPostItem>>>('/admin/posts', { params })
}

export function auditPost(id: number, payload: { result: number; remark?: string }) {
  return http.put<unknown, ApiResponse<null>>(`/admin/posts/${id}/audit`, payload)
}

export function fetchMerchantApplicationsForAdmin(params: { status?: number }) {
  return http.get<unknown, ApiResponse<MerchantApplicationItem[]>>('/admin/merchant-applications', { params })
}

export function auditMerchantApplication(id: number, payload: { result: number; rejectReason?: string }) {
  return http.put<unknown, ApiResponse<MerchantApplicationItem>>(`/admin/merchant-applications/${id}/audit`, payload)
}

export function fetchMerchantProfilesForAdmin(params: { status?: number }) {
  return http.get<unknown, ApiResponse<MerchantProfileItem[]>>('/admin/merchant-profiles', { params })
}

export function auditMerchantProfile(id: number, payload: { result: number; rejectReason?: string }) {
  return http.put<unknown, ApiResponse<MerchantProfileItem>>(`/admin/merchant-profiles/${id}/audit`, payload)
}
