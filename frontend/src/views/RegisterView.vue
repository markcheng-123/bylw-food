<template>
  <section class="auth-screen register-screen">
    <div class="auth-panel form-panel">
      <div class="panel-header">
        <p class="eyebrow">创建账号</p>
        <h3>注册你的本地风味身份</h3>
      </div>

      <form class="auth-form" @submit.prevent="handleSubmit">
        <label class="field">
          <span>用户名</span>
          <input v-model.trim="form.username" type="text" placeholder="4到20位字母、数字或下划线" />
        </label>

        <label class="field">
          <span>昵称</span>
          <input v-model.trim="form.nickname" type="text" placeholder="请输入展示昵称" />
        </label>

        <div class="inline-fields">
          <label class="field">
            <span>手机号</span>
            <input v-model.trim="form.phone" type="text" placeholder="选填" />
          </label>

          <label class="field">
            <span>邮箱</span>
            <input v-model.trim="form.email" type="email" placeholder="选填" />
          </label>
        </div>

        <label class="field">
          <span>密码</span>
          <div class="password-box">
            <input
              v-model.trim="form.password"
              :type="showPassword ? 'text' : 'password'"
              placeholder="6到20位密码"
            />
            <button class="toggle-btn" type="button" @click="showPassword = !showPassword">
              {{ showPassword ? '隐藏' : '显示' }}
            </button>
          </div>
        </label>

        <label class="field">
          <span>确认密码</span>
          <div class="password-box">
            <input
              v-model.trim="confirmPassword"
              :type="showConfirmPassword ? 'text' : 'password'"
              placeholder="请再次输入密码"
            />
            <button class="toggle-btn" type="button" @click="showConfirmPassword = !showConfirmPassword">
              {{ showConfirmPassword ? '隐藏' : '显示' }}
            </button>
          </div>
        </label>

        <p v-if="errorMessage" class="form-error">{{ errorMessage }}</p>

        <button class="submit-btn" type="submit" :disabled="submitting">
          {{ submitting ? '注册中...' : '注册并登录' }}
        </button>
      </form>

      <p class="switch-tip">
        已经有账号？
        <RouterLink to="/login">去登录</RouterLink>
      </p>
    </div>

    <div class="auth-panel auth-copy register-copy">
      <p class="eyebrow">为什么加入</p>
      <h2>把你熟悉的一碗面、一条街、一家小店，留在城市记忆里。</h2>
      <p class="auth-description">
        注册后可以维护个人资料，后续还能发布美食内容、参与评论互动，并逐步形成你的本地生活档案。
      </p>
      <div class="highlight-card">
        <strong>从内容发布到社区展示</strong>
        <span>这套系统已支持发布、评论、点赞、攻略和后台管理，适合作为完整毕设演示。</span>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { loginUser, registerUser } from '@/api/user'
import { setAuthSession } from '@/stores/auth'

const router = useRouter()

const form = reactive({
  username: '',
  nickname: '',
  phone: '',
  email: '',
  password: '',
})

const confirmPassword = ref('')
const showPassword = ref(false)
const showConfirmPassword = ref(false)
const submitting = ref(false)
const errorMessage = ref('')

async function handleSubmit() {
  errorMessage.value = ''

  if (!form.username || !form.nickname || !form.password) {
    errorMessage.value = '请先完整填写必填项。'
    return
  }

  if (form.password !== confirmPassword.value) {
    errorMessage.value = '两次输入的密码不一致。'
    return
  }

  try {
    submitting.value = true
    await registerUser({
      username: form.username,
      nickname: form.nickname,
      password: form.password,
      phone: form.phone || undefined,
      email: form.email || undefined,
    })
    const loginResponse = await loginUser({
      username: form.username,
      password: form.password,
    })
    setAuthSession(loginResponse.data.token, loginResponse.data.userInfo)
    router.push('/')
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '注册失败，请稍后重试。'
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.register-screen {
  display: grid;
  grid-template-columns: 1fr 1.05fr;
  gap: 22px;
}

.auth-panel {
  border: 1px solid var(--line);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow);
}

.form-panel,
.auth-copy {
  padding: 32px;
}

.form-panel {
  background: rgba(255, 252, 246, 0.92);
}

.register-copy {
  background:
    radial-gradient(circle at top right, rgba(47, 107, 79, 0.16), transparent 32%),
    linear-gradient(145deg, rgba(255, 249, 242, 0.95), rgba(239, 226, 207, 0.9));
}

.panel-header h3,
.auth-copy h2 {
  margin: 0;
}

.auth-copy h2 {
  font-size: clamp(32px, 4vw, 50px);
  line-height: 1.15;
  max-width: 12ch;
}

.auth-description {
  margin: 18px 0 0;
  color: var(--muted);
  line-height: 1.8;
  max-width: 52ch;
}

.auth-form {
  display: grid;
  gap: 16px;
  margin-top: 26px;
}

.inline-fields {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.field {
  display: grid;
  gap: 8px;
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

.highlight-card {
  margin-top: 28px;
  padding: 22px;
  border-radius: var(--radius-lg);
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(98, 61, 27, 0.1);
}

.highlight-card strong {
  display: block;
}

.highlight-card span {
  display: block;
  margin-top: 8px;
  color: var(--muted);
  line-height: 1.7;
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
  .register-screen,
  .inline-fields {
    grid-template-columns: 1fr;
  }

  .form-panel,
  .auth-copy {
    padding: 24px;
  }
}
</style>
