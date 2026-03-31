<template>
  <div class="shell">
    <header class="topbar">
      <RouterLink class="brand-block" to="/">
        <p class="eyebrow">Local Flavor Community</p>
        <h1>本地特色美食论坛</h1>
      </RouterLink>

      <nav class="nav">
        <RouterLink to="/">首页</RouterLink>
        <RouterLink to="/food">美食列表</RouterLink>
        <RouterLink to="/publish">发布美食</RouterLink>
        <RouterLink to="/merchant/apply">商家入驻</RouterLink>
        <RouterLink v-if="currentUser?.role === 9" to="/admin">后台管理</RouterLink>
        <RouterLink to="/profile">个人中心</RouterLink>
      </nav>

      <div class="user-actions">
        <template v-if="currentUser">
          <RouterLink class="profile-entry" to="/profile">
            <img v-if="currentUser.avatar" class="avatar-img" :src="toImageUrl(currentUser.avatar)" alt="avatar" />
            <span v-else class="avatar">{{ currentUser.nickname?.slice(0, 1) || '我' }}</span>
            <span class="user-meta">
              <strong>{{ currentUser.nickname }}</strong>
              <span :class="roleClass">{{ roleLabel }}</span>
            </span>
          </RouterLink>
          <button class="ghost-btn" type="button" @click="handleLogout">退出登录</button>
        </template>
        <template v-else>
          <RouterLink class="ghost-btn inline-link" to="/login">登录</RouterLink>
          <RouterLink class="primary-btn inline-link" to="/register">注册</RouterLink>
        </template>
      </div>
    </header>

    <main class="page">
      <RouterView />
    </main>

    <AiChatWindow />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { authState, clearAuthSession } from '@/stores/auth'
import AiChatWindow from '@/components/ai/AiChatWindow.vue'

const API_HOST = 'http://localhost:8080'
const router = useRouter()
const currentUser = computed(() => authState.user)

function toImageUrl(url: string) {
  return url.startsWith('http') ? url : `${API_HOST}${url}`
}

const roleLabel = computed(() => {
  if (currentUser.value?.role === 9) return '管理员'
  if (currentUser.value?.role === 2) return '商家'
  return '普通用户'
})

const roleClass = computed(() => {
  if (currentUser.value?.role === 9) return 'role-badge admin'
  if (currentUser.value?.role === 2) return 'role-badge merchant'
  return 'role-badge user'
})

function handleLogout() {
  clearAuthSession()
  router.push('/login')
}
</script>

<style scoped>
.brand-block {
  display: block;
}

.user-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.profile-entry {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  padding: 8px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.55);
  color: var(--text);
}

.avatar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--brand), var(--brand-deep));
  color: #fff;
  font-size: 14px;
  font-weight: 700;
}

.avatar-img {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  object-fit: cover;
}

.user-meta {
  display: grid;
  gap: 2px;
}

.user-meta strong {
  font-size: 14px;
}

.role-badge {
  display: inline-flex;
  align-items: center;
  width: fit-content;
  padding: 3px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}

.role-badge.user {
  background: rgba(112, 129, 148, 0.12);
  color: #5d6775;
}

.role-badge.merchant {
  background: rgba(199, 113, 34, 0.14);
  color: #9b4d16;
}

.role-badge.admin {
  background: rgba(135, 42, 27, 0.14);
  color: #8c3417;
}

.inline-link {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 88px;
}

:deep(.nav > a) {
  position: relative;
  padding: 6px 2px;
  border-radius: 0;
  font-size: 15px;
  font-weight: 500;
  color: var(--muted);
  transition: color 0.22s ease;
}

:deep(.nav > a::after) {
  content: '';
  position: absolute;
  left: 0;
  bottom: -3px;
  width: 100%;
  height: 2px;
  background: var(--brand);
  transform: scaleX(0.35);
  transform-origin: center;
  opacity: 0;
  transition: transform 0.22s ease, opacity 0.22s ease;
}

:deep(.nav > a:hover) {
  color: var(--brand-deep);
}

:deep(.nav > a.router-link-exact-active) {
  color: var(--brand-deep);
  font-weight: 700;
}

:deep(.nav > a.router-link-exact-active::after) {
  transform: scaleX(1);
  opacity: 1;
}

@media (max-width: 900px) {
  .user-actions {
    width: 100%;
  }
}
</style>
