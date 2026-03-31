<template>
  <div :class="shellClass">
    <aside :class="sidebarClass">
      <RouterLink class="brand" :to="{ name: 'admin-dashboard' }">
        <span class="brand-mark">ADM</span>
        <div v-if="!sidebarCollapsed" class="brand-copy">
          <p>Admin Workspace</p>
          <strong>{{ t('adminWorkspace') }}</strong>
        </div>
      </RouterLink>

      <nav class="sidebar-nav">
        <RouterLink
          v-for="item in visibleMenus"
          :key="item.key"
          :to="item.to"
          class="nav-link"
          :class="{ active: isMenuActive(item.key), compact: sidebarCollapsed }"
          :title="sidebarCollapsed ? item.label : ''"
        >
          <span class="nav-icon" v-html="item.icon"></span>
          <span v-if="!sidebarCollapsed" class="nav-copy">
            <strong>{{ item.label }}</strong>
            <small>{{ item.description }}</small>
          </span>
          <span v-if="item.badge && item.badge > 0" class="nav-badge">{{ item.badge }}</span>
          <span v-else-if="item.dot" class="nav-dot"></span>
        </RouterLink>
      </nav>

      <div class="sidebar-foot">
        <button
          class="foot-tool mode-tool"
          :class="{ compact: sidebarCollapsed }"
          type="button"
          :title="modeLabel"
          @click="toggleTheme"
        >
          <span class="tool-icon">
            <svg viewBox="0 0 24 24" aria-hidden="true">
              <path d="M20 15.5A8.5 8.5 0 1 1 8.5 4 6.8 6.8 0 0 0 20 15.5z" />
            </svg>
          </span>
          <span v-if="!sidebarCollapsed">{{ modeLabel }}</span>
        </button>

        <div ref="localeDropdownRef" class="locale-dropdown">
          <button
            class="foot-tool locale-trigger"
            :class="{ compact: sidebarCollapsed }"
            type="button"
            :title="sidebarCollapsed ? t('language') : ''"
            @click="toggleLocaleMenu"
          >
            <template v-if="sidebarCollapsed">
              <span class="tool-icon">
                <svg viewBox="0 0 24 24" aria-hidden="true">
                  <path d="M4 5h7M7.5 5c0 6-2 10-4.5 12m2.4-4h5.2M14 7h6m-3-2v10m-2.5-2.5h5" />
                </svg>
              </span>
            </template>
            <template v-else>
              <span class="locale-code">CN</span>
              <span class="locale-code active">{{ adminUiState.locale === 'zh' ? 'ZH' : 'EN' }}</span>
              <span :class="localeMenuOpen ? 'locale-arrow open' : 'locale-arrow'">^</span>
            </template>
          </button>

          <div v-if="localeMenuOpen" :class="sidebarCollapsed ? 'locale-menu compact' : 'locale-menu'">
            <button
              type="button"
              :class="adminUiState.locale === 'en' ? 'locale-option active' : 'locale-option'"
              @click="selectLocale('en')"
            >
              <span class="option-code">US</span>
              <span class="option-label">English</span>
              <span class="option-check">{{ adminUiState.locale === 'en' ? '✓' : '' }}</span>
            </button>
            <button
              type="button"
              :class="adminUiState.locale === 'zh' ? 'locale-option active' : 'locale-option'"
              @click="selectLocale('zh')"
            >
              <span class="option-code">CN</span>
              <span class="option-label">中文</span>
              <span class="option-check">{{ adminUiState.locale === 'zh' ? '✓' : '' }}</span>
            </button>
          </div>
        </div>

        <button
          class="foot-tool collapse-tool"
          :class="{ compact: sidebarCollapsed }"
          type="button"
          :title="sidebarCollapsed ? t('expand') : t('collapse')"
          @click="toggleSidebar"
        >
          <span class="tool-icon collapse-icon" :class="{ collapsed: sidebarCollapsed }">▸</span>
          <span v-if="!sidebarCollapsed">{{ t('collapse') }}</span>
        </button>
      </div>
    </aside>

    <main class="admin-main">
      <header class="admin-topbar">
        <div>
          <p class="eyebrow">Admin Console</p>
          <h1>{{ pageTitle }}</h1>
        </div>

        <div class="topbar-right">
          <div class="topbar-status">
            <div class="status-chip">
              <span class="chip-label">{{ t('pendingPosts') }}</span>
              <strong>{{ pendingPostCount }}</strong>
            </div>
            <div class="status-chip danger">
              <span class="chip-label">{{ t('pendingApplications') }}</span>
              <strong>{{ pendingMerchantCount }}</strong>
            </div>
          </div>

          <div ref="userDropdownRef" class="user-dropdown">
            <button class="user-trigger" type="button" @click="toggleUserMenu">
              <img v-if="currentUser?.avatar" class="user-avatar-img" :src="toImageUrl(currentUser.avatar)" alt="avatar" />
              <span v-else class="user-avatar">{{ currentUser?.nickname?.slice(0, 1) || 'A' }}</span>
              <span class="user-text">
                <strong>{{ currentUser?.nickname || 'Admin' }}</strong>
                <small>{{ t('administrator') }}</small>
              </span>
            </button>

            <div v-if="userMenuOpen" class="user-menu">
              <input ref="adminAvatarInputRef" class="hidden-input" type="file" accept="image/*" @change="handleAdminAvatarChange" />
              <button type="button" class="avatar-change-btn" :disabled="adminAvatarUploading" @click="openAdminAvatarPicker">
                {{ adminAvatarUploading ? t('uploading') : t('changeAvatar') }}
              </button>
              <p v-if="avatarNotice" class="avatar-notice">{{ avatarNotice }}</p>
              <button type="button" class="logout-btn" @click="handleLogout">{{ t('logout') }}</button>
            </div>
          </div>
        </div>
      </header>

      <RouterView />
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watchEffect } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { fetchAdminPosts, fetchMerchantApplicationsForAdmin } from '@/api/admin'
import { uploadImage } from '@/api/upload'
import { updateMyProfile } from '@/api/user'
import { useAdminUi } from '@/stores/adminUi'
import { authState, clearAuthSession, updateAuthUser } from '@/stores/auth'

const SIDEBAR_KEY = 'bylw_admin_sidebar_collapsed'
const API_HOST = 'http://localhost:8080'

const route = useRoute()
const router = useRouter()
const currentUser = computed(() => authState.user)
const pendingPostCount = ref(0)
const pendingMerchantCount = ref(0)
const localeMenuOpen = ref(false)
const localeDropdownRef = ref<HTMLElement | null>(null)
const userMenuOpen = ref(false)
const userDropdownRef = ref<HTMLElement | null>(null)
const adminAvatarInputRef = ref<HTMLInputElement | null>(null)
const adminAvatarUploading = ref(false)
const avatarNotice = ref('')
const sidebarCollapsed = ref(localStorage.getItem(SIDEBAR_KEY) === '1')

const { adminUiState, isLight, modeLabel, t, toggleTheme, setLocale } = useAdminUi()

const pendingTotal = computed(() => pendingPostCount.value + pendingMerchantCount.value)

const shellClass = computed(() => [
  'admin-shell',
  isLight.value ? 'theme-light' : '',
  sidebarCollapsed.value ? 'sidebar-collapsed' : '',
].filter(Boolean).join(' '))

const sidebarClass = computed(() => sidebarCollapsed.value ? 'sidebar compact' : 'sidebar')

const menuItems = computed(() => [
  {
    key: 'manage',
    label: t('manage'),
    description: t('manageDesc'),
    to: { name: 'admin-manage' },
    requiresAdmin: true,
    badge: pendingTotal.value,
    icon: `
      <svg viewBox="0 0 24 24" aria-hidden="true">
        <path d="M12 3l7 4v5c0 4.4-2.8 8.4-7 9-4.2-.6-7-4.6-7-9V7l7-4z" />
      </svg>
    `,
  },
  {
    key: 'dashboard',
    label: t('dashboard'),
    description: t('dashboardDesc'),
    to: { name: 'admin-dashboard' },
    requiresAdmin: true,
    icon: `
      <svg viewBox="0 0 24 24" aria-hidden="true">
        <path d="M4 19h16M7 16V9m5 7V5m5 11v-4" />
      </svg>
    `,
  },
  {
    key: 'map',
    label: t('map'),
    description: t('mapDesc'),
    to: { name: 'admin-map' },
    requiresAdmin: true,
    dot: true,
    icon: `
      <svg viewBox="0 0 24 24" aria-hidden="true">
        <path d="M9 18l-5 2V6l5-2 6 2 5-2v14l-5 2-6-2zm0 0V4m6 16V6" />
      </svg>
    `,
  },
])

const visibleMenus = computed(() =>
  menuItems.value.filter((item) => !item.requiresAdmin || currentUser.value?.role === 9),
)

const pageTitle = computed(() => {
  switch (route.meta.sidebarKey) {
    case 'manage':
      return t('manageTitle')
    case 'map':
      return t('mapTitle')
    default:
      return t('dashboardTitle')
  }
})

function syncBodyThemeClass() {
  document.body.classList.add('admin-body')
  document.body.classList.toggle('admin-body-light', isLight.value)
  document.body.classList.toggle('admin-body-dark', !isLight.value)
}

function isMenuActive(key: string) {
  return route.meta.sidebarKey === key
}

function toggleLocaleMenu() {
  localeMenuOpen.value = !localeMenuOpen.value
}

function toggleUserMenu() {
  userMenuOpen.value = !userMenuOpen.value
}

function toImageUrl(url: string) {
  return url.startsWith('http') ? url : `${API_HOST}${url}`
}

function openAdminAvatarPicker() {
  adminAvatarInputRef.value?.click()
}

function selectLocale(locale: 'zh' | 'en') {
  setLocale(locale)
  localeMenuOpen.value = false
}

function handleLogout() {
  clearAuthSession()
  userMenuOpen.value = false
  router.push('/login')
}

async function handleAdminAvatarChange(event: Event) {
  const current = currentUser.value
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file || !current) return
  try {
    adminAvatarUploading.value = true
    avatarNotice.value = ''
    const uploadRes = await uploadImage(file)
    const profileRes = await updateMyProfile({
      nickname: current.nickname,
      avatar: uploadRes.data.url,
      phone: current.phone || undefined,
      email: current.email || undefined,
    })
    updateAuthUser(profileRes.data)
    avatarNotice.value = t('avatarUpdateSuccess')
  } catch (error) {
    avatarNotice.value = error instanceof Error ? error.message : t('avatarUpdateFailed')
  } finally {
    adminAvatarUploading.value = false
    if (adminAvatarInputRef.value) adminAvatarInputRef.value.value = ''
  }
}

function toggleSidebar() {
  sidebarCollapsed.value = !sidebarCollapsed.value
  localStorage.setItem(SIDEBAR_KEY, sidebarCollapsed.value ? '1' : '0')
  localeMenuOpen.value = false
  userMenuOpen.value = false
}

function handleDocumentClick(event: MouseEvent) {
  const target = event.target
  if (!(target instanceof Node)) return
  if (!localeDropdownRef.value?.contains(target)) {
    localeMenuOpen.value = false
  }
  if (!userDropdownRef.value?.contains(target)) {
    userMenuOpen.value = false
  }
}

function handleReviewUpdated() {
  void loadNotifications()
}

async function loadNotifications() {
  const [postResponse, merchantResponse] = await Promise.all([
    fetchAdminPosts({ current: 1, size: 1, status: 0 }),
    fetchMerchantApplicationsForAdmin({ status: 0 }),
  ])
  pendingPostCount.value = postResponse.data.total
  pendingMerchantCount.value = merchantResponse.data.length
}

onMounted(() => {
  syncBodyThemeClass()
  void loadNotifications()
  document.addEventListener('click', handleDocumentClick)
  window.addEventListener('admin-review-updated', handleReviewUpdated as EventListener)
})

watchEffect(() => {
  syncBodyThemeClass()
})

onBeforeUnmount(() => {
  document.body.classList.remove('admin-body', 'admin-body-light', 'admin-body-dark')
  document.removeEventListener('click', handleDocumentClick)
  window.removeEventListener('admin-review-updated', handleReviewUpdated as EventListener)
})
</script>

<style scoped>
:global(body.admin-body) {
  background: #0f1726 !important;
}

:global(body.admin-body.admin-body-light) {
  background: #edf3f6 !important;
}

/* Guard against unexpected overlay layers injected into admin page */
:global(body.admin-body #app [style*="position: fixed"][style*="dashed"]),
:global(body.admin-body #app [style*="position:fixed"][style*="dashed"]),
:global(body.admin-body #app [style*="position: fixed"][style*="dotted"]),
:global(body.admin-body #app [style*="position:fixed"][style*="dotted"]) {
  display: none !important;
}

.admin-shell {
  --admin-bg: linear-gradient(180deg, #0a1220 0%, #0f1726 55%, #111b2b 100%);
  --admin-sidebar-bg: #0d1624;
  --admin-surface: rgba(255, 255, 255, 0.08);
  --admin-surface-soft: rgba(255, 255, 255, 0.05);
  --admin-line: rgba(175, 201, 235, 0.14);
  --admin-text: #f7fbff;
  --admin-muted: rgba(214, 227, 246, 0.72);
  --admin-accent: #8cbaf1;
  --admin-chip: rgba(124, 180, 242, 0.14);
  --admin-shadow: 0 20px 60px rgba(0, 0, 0, 0.18);
  --admin-chart-text: #d7e3f7;
  --admin-chart-line: rgba(148, 163, 184, 0.18);
  --admin-chart-tooltip-bg: rgba(19, 26, 37, 0.92);
  --admin-chart-tooltip-border: rgba(148, 163, 184, 0.22);
  --admin-chart-ring-border: #1a2433;
  --admin-chart-center-value: #edf4ff;
  --admin-chart-center-label: #9fb2d0;
  --admin-chart-line-blue-start: #3b82f6;
  --admin-chart-line-blue-end: #22d3ee;
  --admin-chart-line-cyan-start: #06b6d4;
  --admin-chart-line-cyan-end: #2dd4bf;
  --admin-pie-1: #60a5fa;
  --admin-pie-2: #22d3ee;
  --admin-pie-3: #a78bfa;
  --admin-pie-4: #34d399;
  --admin-pie-5: #f59e0b;
  --admin-pie-6: #818cf8;
  --admin-bar-start: #60a5fa;
  --admin-bar-end: #3b82f6;
  --admin-map-label: #d6e2f7;
  --admin-map-area: #273246;
  --admin-map-border: #6f8db4;
  --admin-map-emphasis-area: #4e7fb9;
  --admin-map-emphasis-border: #9cc1ef;
  --admin-map-visual-1: #5b7fae;
  --admin-map-visual-2: #4f9ad4;
  --el-bg-color-overlay: #1b2433;
  --el-fill-color-light: rgba(255, 255, 255, 0.04);
  --el-fill-color-dark: rgba(127, 148, 177, 0.22);
  --el-border-color-light: rgba(175, 201, 235, 0.14);
  --el-border-color-lighter: rgba(175, 201, 235, 0.1);
  --el-text-color-primary: #f7fbff;
  --el-text-color-secondary: rgba(214, 227, 246, 0.72);
  --el-color-primary: #5ea3ff;
  --el-color-warning: #d9a441;
  --el-color-success: #57c08f;
  --el-color-danger: #ee7a84;
  position: fixed;
  inset: 0;
  width: 100vw;
  min-height: 100vh;
  height: 100vh;
  display: grid;
  grid-template-columns: 280px 1fr;
  background: var(--admin-bg);
  transition: grid-template-columns 0.25s ease;
  overflow: hidden;
  isolation: isolate;
}

.sidebar-collapsed {
  grid-template-columns: 96px 1fr;
}

.theme-light {
  --admin-bg: linear-gradient(180deg, #eff5f7 0%, #edf3f6 100%);
  --admin-sidebar-bg: #f7fbfc;
  --admin-surface: #ffffff;
  --admin-surface-soft: #f2f7f9;
  --admin-line: rgba(160, 181, 193, 0.95);
  --admin-text: #445468;
  --admin-muted: #75869a;
  --admin-accent: #0ea5a4;
  --admin-chip: rgba(14, 165, 164, 0.1);
  --admin-shadow: 0 14px 34px rgba(146, 166, 177, 0.24);
  --admin-chart-text: #475569;
  --admin-chart-line: rgba(148, 163, 184, 0.24);
  --admin-chart-tooltip-bg: rgba(255, 255, 255, 0.96);
  --admin-chart-tooltip-border: rgba(148, 163, 184, 0.35);
  --admin-chart-ring-border: #eef3f9;
  --admin-chart-center-value: #1f2937;
  --admin-chart-center-label: #64748b;
  --admin-chart-line-blue-start: #3b82f6;
  --admin-chart-line-blue-end: #22d3ee;
  --admin-chart-line-cyan-start: #06b6d4;
  --admin-chart-line-cyan-end: #2dd4bf;
  --admin-pie-1: #60a5fa;
  --admin-pie-2: #22d3ee;
  --admin-pie-3: #a78bfa;
  --admin-pie-4: #34d399;
  --admin-pie-5: #f59e0b;
  --admin-pie-6: #818cf8;
  --admin-bar-start: #60a5fa;
  --admin-bar-end: #3b82f6;
  --admin-map-label: #475569;
  --admin-map-area: #dbe7f4;
  --admin-map-border: #a5bfd9;
  --admin-map-emphasis-area: #9ec4ef;
  --admin-map-emphasis-border: #d9ebff;
  --admin-map-visual-1: #93b4d9;
  --admin-map-visual-2: #5f98d6;
  --el-bg-color-overlay: #ffffff;
  --el-fill-color-light: #f5f7fa;
  --el-fill-color-dark: rgba(148, 163, 184, 0.18);
  --el-border-color-light: rgba(160, 181, 193, 0.95);
  --el-border-color-lighter: rgba(160, 181, 193, 0.6);
  --el-text-color-primary: #334155;
  --el-text-color-secondary: #64748b;
  --el-color-primary: #409eff;
  --el-color-warning: #e6a23c;
  --el-color-success: #67c23a;
  --el-color-danger: #f56c6c;
}

.sidebar {
  display: flex;
  flex-direction: column;
  gap: 24px;
  padding: 20px 18px;
  border-right: 1px solid var(--admin-line);
  background: var(--admin-sidebar-bg);
  position: sticky;
  top: 0;
  height: 100vh;
  overflow-y: auto;
}

.sidebar.compact {
  align-items: center;
  padding-inline: 12px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 14px;
  color: var(--admin-text);
  min-height: 64px;
}

.brand-mark {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 52px;
  height: 52px;
  border-radius: 18px;
  background: linear-gradient(135deg, #1a73e8, #0a58ca);
  color: #fff;
  font-size: 13px;
  font-weight: 800;
  letter-spacing: 0.08em;
  box-shadow: 0 10px 24px rgba(26, 115, 232, 0.24);
}

.brand-copy p,
.nav-copy small {
  margin: 0;
  color: var(--admin-muted);
}

.brand-copy strong {
  color: var(--admin-text);
}

.sidebar-nav {
  display: grid;
  gap: 10px;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 14px;
  min-height: 58px;
  padding: 0 14px;
  border-radius: 18px;
  border: 1px solid transparent;
  background: transparent;
  color: var(--admin-text);
  position: relative;
  transition: all 0.2s ease;
}

.nav-link.compact {
  width: 56px;
  justify-content: center;
  padding: 0;
}

.nav-link.active,
.nav-link.router-link-active {
  background: rgba(14, 165, 164, 0.1);
  border-color: rgba(14, 165, 164, 0.18);
}

.theme-light .nav-link.active,
.theme-light .nav-link.router-link-active {
  box-shadow: inset 0 0 0 1px rgba(14, 165, 164, 0.08);
}

.theme-light .nav-link,
.theme-light .foot-tool,
.theme-light .status-chip,
.theme-light .admin-topbar,
.theme-light .sidebar,
.theme-light .locale-menu {
  box-shadow: 0 8px 22px rgba(170, 186, 196, 0.16);
}

.theme-light .nav-link,
.theme-light .foot-tool,
.theme-light .status-chip {
  border-color: rgba(171, 190, 201, 0.88);
}

.nav-icon,
.tool-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
}

.nav-icon :deep(svg),
.tool-icon svg {
  width: 22px;
  height: 22px;
  stroke: currentColor;
  stroke-width: 1.8;
  fill: none;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.nav-copy {
  display: grid;
  gap: 2px;
  flex: 1;
}

.nav-copy strong {
  color: var(--admin-text);
  font-size: 15px;
}

.nav-badge {
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  border-radius: 999px;
  background: #ff5f6d;
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.nav-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #22c55e;
}

.sidebar-foot {
  margin-top: auto;
  display: grid;
  gap: 12px;
}

.foot-tool {
  display: flex;
  align-items: center;
  gap: 12px;
  min-height: 54px;
  padding: 0 14px;
  border-radius: 18px;
  border: 1px solid transparent;
  background: transparent;
  color: var(--admin-text);
  font: inherit;
  position: relative;
}

.foot-tool.compact {
  width: 56px;
  justify-content: center;
  padding: 0;
}

.mode-tool:hover,
.locale-trigger:hover,
.collapse-tool:hover {
  background: var(--admin-surface-soft);
}

.locale-dropdown {
  position: relative;
}

.locale-code {
  font-size: 15px;
  color: var(--admin-muted);
}

.locale-code.active {
  color: var(--admin-text);
  font-weight: 700;
}

.locale-arrow {
  margin-left: auto;
  color: var(--admin-muted);
  transition: transform 0.2s ease;
}

.locale-arrow.open {
  transform: rotate(180deg);
}

.locale-menu {
  position: absolute;
  left: 0;
  bottom: calc(100% + 10px);
  width: 194px;
  padding: 10px;
  border-radius: 18px;
  border: 1px solid var(--admin-line);
  background: #1f2631;
  box-shadow: var(--admin-shadow);
  z-index: 120;
  opacity: 1;
}

.locale-menu.compact {
  left: 0;
}

.theme-light .locale-menu {
  background: #ffffff;
}

.locale-option {
  width: 100%;
  display: grid;
  grid-template-columns: 38px 1fr 24px;
  align-items: center;
  gap: 10px;
  min-height: 48px;
  padding: 0 10px;
  border: none;
  border-radius: 14px;
  background: transparent;
  color: var(--admin-text);
  font: inherit;
  text-align: left;
}

.locale-option.active {
  background: var(--admin-chip);
  color: var(--admin-accent);
}

.option-code,
.option-check {
  font-weight: 700;
}

.collapse-icon {
  transition: transform 0.2s ease;
}

.collapse-icon.collapsed {
  transform: rotate(180deg);
}

.admin-main {
  padding: 28px;
  height: 100vh;
  overflow-y: auto;
}

.admin-topbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
  margin-bottom: 24px;
  padding: 24px 26px;
  border-radius: 24px;
  border: 1px solid var(--admin-line);
  background: var(--admin-surface);
  box-shadow: var(--admin-shadow);
}

.admin-topbar h1,
.admin-topbar p {
  margin: 0;
}

.admin-topbar h1 {
  color: var(--admin-text);
}

.eyebrow {
  margin-bottom: 10px;
  font-size: 12px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: var(--admin-accent);
}

.topbar-status {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 14px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.status-chip {
  min-width: 132px;
  padding: 12px 14px;
  border-radius: 18px;
  border: 1px solid var(--admin-line);
  background: var(--admin-surface-soft);
}

.status-chip.danger {
  background: rgba(255, 95, 109, 0.08);
}

.theme-light .status-chip.danger {
  border-color: rgba(227, 169, 176, 0.85);
}

.chip-label {
  display: block;
  margin-bottom: 6px;
  font-size: 12px;
  color: var(--admin-muted);
}

.status-chip strong {
  font-size: 24px;
  color: var(--admin-text);
}

.user-dropdown {
  position: relative;
}

.user-trigger {
  min-height: 56px;
  border-radius: 16px;
  border: 1px solid var(--admin-line);
  background: var(--admin-surface-soft);
  color: var(--admin-text);
  padding: 8px 12px;
  display: inline-flex;
  align-items: center;
  gap: 10px;
  font: inherit;
}

.theme-light .user-trigger {
  border-color: rgba(171, 190, 201, 0.88);
  box-shadow: 0 8px 22px rgba(170, 186, 196, 0.16);
}

.user-avatar {
  width: 34px;
  height: 34px;
  border-radius: 999px;
  background: linear-gradient(135deg, #1a73e8, #0a58ca);
  color: #fff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 700;
}

.user-avatar-img {
  width: 34px;
  height: 34px;
  border-radius: 999px;
  object-fit: cover;
}

.user-text {
  display: grid;
  gap: 2px;
  text-align: left;
}

.user-text strong {
  color: var(--admin-text);
  font-size: 14px;
}

.user-text small {
  color: var(--admin-muted);
  font-size: 12px;
}

.user-menu {
  position: absolute;
  top: calc(100% + 10px);
  right: 0;
  min-width: 140px;
  padding: 8px;
  border-radius: 14px;
  border: 1px solid var(--admin-line);
  background: #1f2631;
  box-shadow: var(--admin-shadow);
  z-index: 140;
}

.theme-light .user-menu {
  background: #ffffff;
  border-color: rgba(171, 190, 201, 0.88);
  box-shadow: 0 8px 22px rgba(170, 186, 196, 0.16);
}

.logout-btn {
  width: 100%;
  min-height: 40px;
  border: none;
  border-radius: 10px;
  background: rgba(255, 95, 109, 0.14);
  color: #d56a74;
  font: inherit;
}

.hidden-input {
  display: none;
}

.avatar-change-btn {
  width: 100%;
  min-height: 40px;
  border: 1px solid var(--admin-line);
  border-radius: 10px;
  background: var(--admin-surface-soft);
  color: var(--admin-text);
  font: inherit;
  margin-bottom: 8px;
}

.avatar-notice {
  margin: 0 0 8px;
  font-size: 12px;
  color: #76e0a8;
}

.logout-btn:hover {
  background: rgba(255, 95, 109, 0.2);
}

@media (max-width: 1100px) {
  .admin-shell,
  .sidebar-collapsed {
    grid-template-columns: 1fr;
  }

  .sidebar,
  .sidebar.compact {
    align-items: stretch;
    border-right: none;
    border-bottom: 1px solid var(--admin-line);
    position: relative;
    height: auto;
    overflow: visible;
  }

  .nav-link.compact,
  .foot-tool.compact {
    width: 100%;
    justify-content: flex-start;
    padding: 0 14px;
  }

  .brand-copy,
  .nav-copy {
    display: grid !important;
  }
}

@media (max-width: 900px) {
  .admin-main {
    padding: 18px;
    height: auto;
    overflow: visible;
  }

  .admin-topbar {
    display: grid;
  }

  .topbar-right {
    justify-content: flex-start;
  }
}
</style>
