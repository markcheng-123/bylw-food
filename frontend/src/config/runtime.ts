export interface RuntimeConfig {
  API_BASE_URL?: string
}

declare global {
  interface Window {
    __APP_CONFIG__?: RuntimeConfig
  }
}

export function getApiBaseUrl(): string {
  return (window.__APP_CONFIG__?.API_BASE_URL || 'http://localhost:8080/api').trim()
}

export function getApiHost(): string {
  return getApiBaseUrl().replace(/\/api\/?$/, '')
}
