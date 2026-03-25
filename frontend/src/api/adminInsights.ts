import http from './http'

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export interface ActiveTrendItem {
  date: string
  newUsers: number
  newPosts: number
}

export interface CategoryShareItem {
  name: string
  value: number
}

export interface MerchantCityItem {
  city: string
  merchantCount: number
}

export interface ProvinceMerchantItem {
  province: string
  merchantCount: number
  topMerchants: string[]
}

export interface AdminDataCenterPayload {
  activeTrend: ActiveTrendItem[]
  categoryShare: CategoryShareItem[]
  merchantCities: MerchantCityItem[]
}

export interface AdminMapPayload {
  highlightedProvinces: string[]
  provinceStats: ProvinceMerchantItem[]
}

export function fetchAdminDataCenter() {
  return http.get<unknown, ApiResponse<AdminDataCenterPayload>>('/admin/insights/data-center')
}

export function fetchAdminMap() {
  return http.get<unknown, ApiResponse<AdminMapPayload>>('/admin/insights/map')
}
