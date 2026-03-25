import { reactive } from 'vue'
import type { UserProfile } from '@/api/user'

const TOKEN_KEY = 'bylw_token'
const USER_KEY = 'bylw_user'

function loadUser(): UserProfile | null {
  const raw = localStorage.getItem(USER_KEY)
  if (!raw) {
    return null
  }
  try {
    return JSON.parse(raw) as UserProfile
  } catch (error) {
    localStorage.removeItem(USER_KEY)
    return null
  }
}

export const authState = reactive<{
  token: string
  user: UserProfile | null
}>({
  token: localStorage.getItem(TOKEN_KEY) || '',
  user: loadUser(),
})

export function getToken() {
  return authState.token
}

export function setAuthSession(token: string, user: UserProfile) {
  authState.token = token
  authState.user = user
  localStorage.setItem(TOKEN_KEY, token)
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

export function updateAuthUser(user: UserProfile) {
  authState.user = user
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

export function clearAuthSession() {
  authState.token = ''
  authState.user = null
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}

export function isLoggedIn() {
  return Boolean(authState.token)
}
