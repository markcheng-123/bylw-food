import http from './http'

export interface CategoryItem {
  id: number
  name: string
  icon: string | null
  sort: number
  status: number
  description: string | null
}

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export function fetchCategoryOptions() {
  return http.get<unknown, ApiResponse<CategoryItem[]>>('/categories/options')
}

export function fetchAdminCategoryList() {
  return http.get<unknown, ApiResponse<CategoryItem[]>>('/categories/admin/list')
}

export function createCategory(payload: {
  name: string
  icon?: string
  sort: number
  status: number
  description?: string
}) {
  return http.post<unknown, ApiResponse<CategoryItem>>('/categories/admin', payload)
}

export function updateCategory(
  id: number,
  payload: {
    name: string
    icon?: string
    sort: number
    status: number
    description?: string
  },
) {
  return http.put<unknown, ApiResponse<CategoryItem>>(`/categories/admin/${id}`, payload)
}

export function updateCategoryStatus(id: number, status: number) {
  return http.put<unknown, ApiResponse<CategoryItem>>(`/categories/admin/${id}/status`, { status })
}

export function deleteCategory(id: number) {
  return http.delete<unknown, ApiResponse<null>>(`/categories/admin/${id}`)
}
