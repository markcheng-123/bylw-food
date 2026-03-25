<template>
  <section class="auth-screen">
    <div class="auth-panel auth-copy">
      <p class="eyebrow">欢迎回来</p>
      <h2>用一份真实的在地风味，连接一座城市的生活记忆。</h2>
      <p class="auth-description">
        登录后可以进入系统、查看个人资料，并继续参与发布、评论和点赞等社区互动功能。
      </p>
      <div class="feature-list">
        <div class="feature-item">
          <strong>安全登录</strong>
          <span>密码加密存储，接口带登录态校验。</span>
        </div>
        <div class="feature-item">
          <strong>个人中心</strong>
          <span>资料维护、内容发布和后续社区互动都会围绕你的账号展开。</span>
        </div>
      </div>
    </div>

    <div class="auth-panel form-panel">
      <div class="panel-header">
        <p class="eyebrow">账号登录</p>
        <h3>进入本地特色美食论坛</h3>
      </div>

      <form class="auth-form" @submit.prevent="handleSubmit">
        <label class="field">
          <span>用户名</span>
          <input v-model.trim="form.username" type="text" placeholder="请输入用户名" />
        </label>

        <label class="field">
          <span>密码</span>
          <div class="password-box">
            <input
              v-model.trim="form.password"
              :type="showPassword ? 'text' : 'password'"
              placeholder="请输入密码"
            />
            <button class="toggle-btn" type="button" @click="showPassword = !showPassword">
              {{ showPassword ? '隐藏' : '显示' }}
            </button>
          </div>
        </label>

        <p v-if="errorMessage" class="form-error">{{ errorMessage }}</p>

        <button class="submit-btn" type="submit" :disabled="submitting">
          {{ submitting ? '登录中...' : '登录' }}
        </button>
      </form>

      <p class="switch-tip">
        还没有账号？
        <RouterLink to="/register">立即注册</RouterLink>
      </p>
    </div>
  </section>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { loginUser } from '@/api/user'
import { setAuthSession } from '@/stores/auth'

const router = useRouter()
const route = useRoute()

const form = reactive({
  username: '',
  password: '',
})

const showPassword = ref(false)
const submitting = ref(false)
const errorMessage = ref('')

async function handleSubmit() {
  errorMessage.value = ''

  if (!form.username || !form.password) {
    errorMessage.value = '请先完整填写用户名和密码。'
    return
  }

  try {
    submitting.value = true
    const response = await loginUser(form)
    setAuthSession(response.data.token, response.data.userInfo)
    const redirect = typeof route.query.redirect === 'string'
      ? route.query.redirect
      : (response.data.userInfo.role === 9 ? '/admin/manage' : '/')
    router.push(redirect)
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '登录失败，请稍后重试。'
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.auth-screen {
  display: grid;
  grid-template-columns: 1.15fr 0.95fr;
  gap: 22px;
}

.auth-panel {
  border: 1px solid var(--line);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow);
}

.auth-copy {
  padding: 40px;
  background:
    radial-gradient(circle at top left, rgba(189, 91, 47, 0.2), transparent 34%),
    linear-gradient(145deg, rgba(255, 250, 244, 0.96), rgba(240, 226, 207, 0.92));
}

.auth-copy h2 {
  margin: 0;
  font-size: clamp(32px, 4vw, 52px);
  line-height: 1.14;
  max-width: 11ch;
}

.auth-description {
  margin: 20px 0 0;
  color: var(--muted);
  line-height: 1.8;
  max-width: 54ch;
}

.feature-list {
  display: grid;
  gap: 16px;
  margin-top: 34px;
}

.feature-item {
  padding: 20px;
  background: rgba(255, 255, 255, 0.62);
  border: 1px solid rgba(98, 61, 27, 0.1);
  border-radius: var(--radius-lg);
}

.feature-item strong,
.panel-header h3 {
  display: block;
  margin: 0;
}

.feature-item span {
  display: block;
  margin-top: 8px;
  color: var(--muted);
  line-height: 1.7;
}

.form-panel {
  padding: 32px;
  background: rgba(255, 252, 246, 0.92);
}

.panel-header h3 {
  font-size: 28px;
}

.auth-form {
  display: grid;
  gap: 16px;
  margin-top: 28px;
}

.field {
  display: grid;
  gap: 8px;
  color: var(--text);
}

.field span {
  font-size: 14px;
  color: var(--muted);
}

.field input {
  width: 100%;
  height: 50px;
  border: 1px solid rgba(98, 61, 27, 0.14);
  border-radius: 16px;
  padding: 0 16px;
  background: #fffdfa;
  color: var(--text);
  outline: none;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.field input:focus {
  border-color: rgba(189, 91, 47, 0.55);
  box-shadow: 0 0 0 4px rgba(189, 91, 47, 0.12);
}

.password-box {
  position: relative;
}

.password-box input {
  padding-right: 78px;
}

.toggle-btn {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  border: none;
  background: transparent;
  color: var(--brand-deep);
}

.form-error {
  margin: 0;
  color: #b13f28;
  font-size: 14px;
}

.submit-btn {
  height: 52px;
  border: none;
  border-radius: 999px;
  background: linear-gradient(135deg, var(--brand), var(--brand-deep));
  color: #fff;
  font-weight: 600;
}

.submit-btn:disabled {
  opacity: 0.72;
  cursor: not-allowed;
}

.switch-tip {
  margin: 18px 0 0;
  color: var(--muted);
}

.switch-tip a {
  color: var(--brand-deep);
  font-weight: 600;
}

@media (max-width: 900px) {
  .auth-screen {
    grid-template-columns: 1fr;
  }

  .auth-copy,
  .form-panel {
    padding: 24px;
  }
}
</style>
