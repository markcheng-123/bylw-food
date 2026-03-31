<template>
  <section class="auth-screen" :class="{ 'enter-active': entered }">
    <div
      ref="stageRef"
      class="auth-panel auth-copy"
      :class="{ 'is-private': showPassword }"
      :style="stageStyle"
      @mousemove="handleStageMouseMove"
      @mouseleave="resetGaze"
    >
      <div class="ambient ambient-a"></div>
      <div class="ambient ambient-b"></div>
      <div class="ambient ambient-c"></div>

      <p class="eyebrow">欢迎回来</p>
      <h2>用一份真实的在地风味，连接一座城市的生活记忆。</h2>

      <div class="character-stage">
        <div class="light-beam"></div>
        <span class="sparkle sp-1"></span>
        <span class="sparkle sp-2"></span>
        <span class="sparkle sp-3"></span>
        <div class="stage-floor"></div>

        <div class="crew">
          <div class="character char-blue" :class="{ stretch: isWatchingAccount }">
            <div class="eyes">
              <span class="eye"><i class="pupil"></i></span>
              <span class="eye"><i class="pupil"></i></span>
            </div>
            <div class="back-dots"><span></span><span></span></div>
          </div>

          <div class="character char-black">
            <div class="eyes">
              <span class="eye"><i class="pupil"></i></span>
              <span class="eye"><i class="pupil"></i></span>
            </div>
            <div class="back-dots"><span></span><span></span></div>
          </div>

          <div class="character char-yellow">
            <div class="eyes">
              <span class="eye"><i class="pupil"></i></span>
              <span class="eye"><i class="pupil"></i></span>
            </div>
            <div class="mouth"></div>
            <div class="back-dots"><span></span><span></span></div>
          </div>

          <div class="character char-orange">
            <div class="eyes">
              <span class="eye"><i class="pupil"></i></span>
              <span class="eye"><i class="pupil"></i></span>
            </div>
            <div class="back-dots"><span></span><span></span></div>
          </div>
        </div>
      </div>
    </div>

    <div class="auth-panel form-panel">
      <div class="panel-header">
        <p class="eyebrow">账号登录</p>
        <h3>进入本地特色美食论坛</h3>
      </div>

      <form class="auth-form" @submit.prevent="handleSubmit">
        <label class="field field-username">
          <span>用户名</span>
          <div class="username-box">
            <input
              ref="usernameInputRef"
              v-model.trim="form.username"
              type="text"
              placeholder="请输入用户名"
              @input="handleUsernameInput"
              @focus="handleUsernameFocus"
              @blur="handleUsernameBlur"
              @click="syncAccountGaze"
              @keyup="syncAccountGaze"
              @select="syncAccountGaze"
            />
            <div class="pizza-burst-layer" aria-hidden="true">
              <span
                v-for="impact in pizzaImpacts"
                :key="impact.id"
                class="pizza-impact"
                :style="{
                  left: `${impact.left}px`,
                  top: `${impact.top}px`,
                  '--rot': `${impact.rotate}deg`,
                }"
              />
              <span
                v-for="burst in pizzaBursts"
                :key="burst.id"
                class="pixel-burst"
                :style="{
                  left: `${burst.left}px`,
                  top: `${burst.top}px`,
                  '--dx': `${burst.dx}px`,
                  '--dy': `${burst.dy}px`,
                  '--size': `${burst.size}px`,
                  '--color': burst.color,
                  '--delay': `${burst.delay}s`,
                  '--rot': `${burst.rotate}deg`,
                  '--spin': `${burst.spin}deg`,
                }"
              />
            </div>
          </div>
        </label>

        <label class="field">
          <span>密码</span>
          <div class="password-box">
            <input
              v-model.trim="form.password"
              :type="showPassword ? 'text' : 'password'"
              placeholder="请输入密码"
              autocomplete="current-password"
              @focus="passwordFocused = true"
              @blur="passwordFocused = false"
            />
            <button class="toggle-btn" type="button" @click="showPassword = !showPassword">
              <svg v-if="showPassword" viewBox="0 0 24 24" aria-hidden="true">
                <path d="M2 12s3.5-6 10-6 10 6 10 6-3.5 6-10 6-10-6-10-6z" />
                <circle cx="12" cy="12" r="2.8" />
              </svg>
              <svg v-else viewBox="0 0 24 24" aria-hidden="true">
                <path d="M3 3l18 18" />
                <path d="M6.2 6.3A13.7 13.7 0 0 1 12 5c6.5 0 10 7 10 7a16.6 16.6 0 0 1-4 4.8" />
                <path d="M10.6 10.6a2 2 0 0 0 2.8 2.8" />
                <path d="M4.2 12a16.2 16.2 0 0 0 4.3 4.5A11.8 11.8 0 0 0 12 18c1 0 2-.1 2.9-.4" />
              </svg>
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
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { loginUser } from '@/api/user'
import { setAuthSession } from '@/stores/auth'

type PixelBurst = {
  id: number
  left: number
  top: number
  dx: number
  dy: number
  size: number
  color: string
  delay: number
  rotate: number
  spin: number
}

type PizzaImpact = {
  id: number
  left: number
  top: number
  rotate: number
}

const router = useRouter()
const route = useRoute()

const form = reactive({
  username: '',
  password: '',
})

const showPassword = ref(false)
const submitting = ref(false)
const errorMessage = ref('')
const entered = ref(false)
const stageRef = ref<HTMLElement | null>(null)
const usernameInputRef = ref<HTMLInputElement | null>(null)
const usernameFocused = ref(false)
const passwordFocused = ref(false)
const gazeX = ref(0)
const gazeY = ref(0)
const pizzaBursts = ref<PixelBurst[]>([])
const pizzaImpacts = ref<PizzaImpact[]>([])
const previousUsername = ref('')
let burstSeq = 0
const cleanupTimers: number[] = []
let measureCanvas: HTMLCanvasElement | null = null

const isWatchingAccount = computed(() =>
  !showPassword.value && usernameFocused.value,
)

const stageStyle = computed(() => ({
  '--gaze-x': `${gazeX.value}px`,
  '--gaze-y': `${gazeY.value}px`,
  '--drift-x': `${gazeX.value * 1.4}px`,
  '--drift-y': `${gazeY.value * 1.1}px`,
  '--glow-shift': `${gazeX.value * 1.8}px`,
}))

function measureCaretX(input: HTMLInputElement, caretIndex: number) {
  const style = window.getComputedStyle(input)
  if (!measureCanvas) {
    measureCanvas = document.createElement('canvas')
  }
  const ctx = measureCanvas.getContext('2d')
  if (!ctx) return 24
  ctx.font = style.font || `${style.fontSize} ${style.fontFamily}`
  const text = input.value.slice(0, Math.max(0, caretIndex))
  const paddingLeft = Number.parseFloat(style.paddingLeft || '16') || 16
  const textWidth = ctx.measureText(text).width
  return Math.max(10, Math.min(input.clientWidth - 12, paddingLeft + textWidth))
}

function measureCharCenterX(input: HTMLInputElement, charIndex: number) {
  const style = window.getComputedStyle(input)
  if (!measureCanvas) {
    measureCanvas = document.createElement('canvas')
  }
  const ctx = measureCanvas.getContext('2d')
  if (!ctx) return measureCaretX(input, charIndex + 1)
  ctx.font = style.font || `${style.fontSize} ${style.fontFamily}`
  const before = input.value.slice(0, Math.max(0, charIndex))
  const char = input.value[charIndex] || ''
  const paddingLeft = Number.parseFloat(style.paddingLeft || '16') || 16
  const beforeWidth = ctx.measureText(before).width
  const charWidth = Math.max(6, ctx.measureText(char).width || 8)
  const center = paddingLeft + beforeWidth + charWidth * 0.5
  return Math.max(10, Math.min(input.clientWidth - 12, center))
}

function spawnPizzaImpact(originX: number, originY = 26) {
  const impact: PizzaImpact = {
    id: ++burstSeq,
    left: Math.max(10, Math.min(360, originX)) + (Math.random() * 2 - 1),
    top: originY + (Math.random() * 2 - 1),
    rotate: Math.random() * 28 - 14,
  }
  pizzaImpacts.value.push(impact)

  const timer = window.setTimeout(() => {
    pizzaImpacts.value = pizzaImpacts.value.filter((item) => item.id !== impact.id)
  }, 380)
  cleanupTimers.push(timer)
}

function spawnPixelBurst(originX: number, originY = 26) {
  const clampedX = Math.max(10, Math.min(360, originX))
  const originLeft = clampedX + (Math.random() * 4 - 2)
  const originTop = originY + (Math.random() * 8 - 4)
  const palette = ['#d55a57', '#dda73a', '#86be6f', '#c8bfd6', '#d89aa3', '#d4caa2']
  const particleCount = 7 + Math.floor(Math.random() * 3)

  for (let i = 0; i < particleCount; i += 1) {
    const burst: PixelBurst = {
      id: ++burstSeq,
      left: originLeft + (Math.random() * 4 - 2),
      top: originTop + (Math.random() * 3 - 1.5),
      dx: Math.random() * 54 - 27,
      dy: Math.random() * 74 - 56,
      size: 7 + Math.floor(Math.random() * 6),
      color: palette[Math.floor(Math.random() * palette.length)],
      delay: 0.12 + Math.random() * 0.09,
      rotate: Math.random() * 360,
      spin: (Math.random() * 280 + 160) * (Math.random() > 0.5 ? 1 : -1),
    }
    pizzaBursts.value.push(burst)

    const timer = window.setTimeout(() => {
      pizzaBursts.value = pizzaBursts.value.filter((item) => item.id !== burst.id)
    }, 720)
    cleanupTimers.push(timer)
  }
}

function syncAccountGaze(event?: Event) {
  if (!usernameFocused.value || showPassword.value) return
  const input = (event?.target as HTMLInputElement | null) ?? usernameInputRef.value
  if (!input) return
  const caretIndex = input.selectionStart ?? input.value.length
  const caretX = measureCaretX(input, caretIndex)
  const ratio = input.clientWidth > 0 ? caretX / input.clientWidth : 0.5
  const mappedX = 4 + ratio * 6
  gazeX.value = Math.max(3.5, Math.min(9, mappedX))
  gazeY.value = -0.8
}

function handleUsernameFocus(event: FocusEvent) {
  usernameFocused.value = true
  syncAccountGaze(event)
}

function handleUsernameBlur() {
  usernameFocused.value = false
  resetGaze()
}

function handleUsernameInput(event: Event) {
  const input = event.target as HTMLInputElement
  const value = input.value
  const prev = previousUsername.value

  if (value.length >= prev.length) {
    for (let i = prev.length; i < value.length; i += 1) {
      if (/\d/.test(value[i])) {
        const x = measureCharCenterX(input, i)
        spawnPizzaImpact(x)
        const timer = window.setTimeout(() => {
          spawnPixelBurst(x)
        }, 140)
        cleanupTimers.push(timer)
      }
    }
  }

  previousUsername.value = value
  syncAccountGaze(event)
}

function handleStageMouseMove(event: MouseEvent) {
  if (showPassword.value || usernameFocused.value || !stageRef.value) return
  const rect = stageRef.value.getBoundingClientRect()
  const centerX = rect.left + rect.width / 2
  const centerY = rect.top + rect.height / 2
  const dx = (event.clientX - centerX) / (rect.width / 2)
  const dy = (event.clientY - centerY) / (rect.height / 2)
  gazeX.value = Math.max(-9, Math.min(9, dx * 9))
  gazeY.value = Math.max(-7, Math.min(7, dy * 7))
}

function resetGaze() {
  gazeX.value = 0
  gazeY.value = 0
}

function handleWindowMouseMove(event: MouseEvent) {
  if (usernameFocused.value) return
  handleStageMouseMove(event)
}

onMounted(() => {
  window.addEventListener('mousemove', handleWindowMouseMove)
  window.requestAnimationFrame(() => {
    entered.value = true
  })
})

onBeforeUnmount(() => {
  window.removeEventListener('mousemove', handleWindowMouseMove)
  cleanupTimers.forEach((timer) => window.clearTimeout(timer))
})

async function handleSubmit() {
  errorMessage.value = ''

  if (!form.username || !form.password) {
    errorMessage.value = 'Please enter both username and password.'
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
    errorMessage.value = error instanceof Error ? error.message : 'Login failed, please try again later.'
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
  min-height: calc(100vh - 90px);
  align-items: stretch;
}

.auth-panel {
  border: 1px solid var(--line);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow);
}

.auth-copy {
  position: relative;
  overflow: hidden;
  padding: 40px;
  min-height: calc(100vh - 90px);
  background:
    radial-gradient(circle at top left, rgba(189, 91, 47, 0.2), transparent 34%),
    linear-gradient(145deg, rgba(255, 250, 244, 0.96), rgba(240, 226, 207, 0.92));
  opacity: 0;
  transform: translateX(-44px);
}

.auth-copy h2 {
  margin: 0;
  font-size: clamp(32px, 4vw, 52px);
  line-height: 1.14;
  max-width: 11ch;
  position: relative;
  z-index: 2;
}

.auth-copy .eyebrow {
  position: relative;
  z-index: 2;
}

.auth-copy .eyebrow,
.auth-copy h2 {
  opacity: 0;
  transform: translateX(-26px);
}

.ambient {
  position: absolute;
  border-radius: 999px;
  pointer-events: none;
}

.ambient-a {
  width: 420px;
  height: 420px;
  left: -120px;
  top: -160px;
  background: radial-gradient(circle, rgba(106, 80, 245, 0.24), rgba(106, 80, 245, 0));
  transform: translateX(var(--glow-shift));
}

.ambient-b {
  width: 280px;
  height: 280px;
  right: -80px;
  top: 24%;
  background: radial-gradient(circle, rgba(255, 164, 88, 0.24), rgba(255, 164, 88, 0));
  animation: floatSoft 6s ease-in-out infinite;
}

.ambient-c {
  width: 360px;
  height: 360px;
  left: 8%;
  bottom: 8%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.3), rgba(255, 255, 255, 0));
  animation: floatSoft 7.5s ease-in-out infinite reverse;
}

.panel-header h3 {
  display: block;
  margin: 0;
  font-size: 28px;
}

.character-stage {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 72%;
  pointer-events: none;
  overflow: hidden;
}

.light-beam {
  position: absolute;
  left: -20%;
  right: -20%;
  top: 2%;
  height: 38%;
  background: radial-gradient(ellipse at center, rgba(255, 255, 255, 0.3), rgba(255, 255, 255, 0));
  transform: translateX(calc(var(--glow-shift) * 0.5));
}

.sparkle {
  position: absolute;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.8);
  opacity: 0.35;
  animation: sparkleFloat 5s ease-in-out infinite;
}

.sp-1 { left: 16%; top: 20%; }
.sp-2 { left: 42%; top: 14%; animation-delay: 1.2s; }
.sp-3 { left: 72%; top: 26%; animation-delay: 2.3s; }

.stage-floor {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 112px;
  background: rgba(49, 35, 22, 0.16);
}

.crew {
  position: absolute;
  left: 42%;
  right: -18%;
  bottom: 0;
  height: 96%;
  transform: translate(var(--drift-x), var(--drift-y));
  opacity: 0;
}

.character {
  position: absolute;
  bottom: 0;
  transform-origin: bottom center;
  transition: transform 0.3s ease, height 0.3s ease;
}

.char-blue {
  left: 6%;
  width: 28%;
  height: 88%;
  border-radius: 12px;
  background: linear-gradient(180deg, #5c42ff, #4929df 80%);
  transform: skewX(-12deg);
}

.char-blue.stretch {
  height: 96%;
  transform: skewX(-12deg) translateX(14px) translateY(-7px);
  animation: sneakPeek 0.4s cubic-bezier(0.22, 1, 0.36, 1) forwards;
}

.char-blue .eye,
.char-blue .pupil {
  transition: all 0.2s ease;
}

.char-blue.stretch .eye {
  width: 26px;
  height: 20px;
}

.char-blue.stretch .pupil {
  width: 9px;
  height: 9px;
  left: 8px;
  top: 5px;
}

.char-black {
  left: 24%;
  width: 21%;
  height: 76%;
  border-radius: 10px;
  background: linear-gradient(180deg, #171c23, #0f141b);
}

.char-yellow {
  left: 40%;
  width: 30%;
  height: 64%;
  border-radius: 50px 50px 10px 10px;
  background: linear-gradient(180deg, #dbc84c, #c7b03e);
}

.char-orange {
  left: -6%;
  width: 36%;
  height: 52%;
  border-radius: 64px 64px 0 0;
  background: linear-gradient(180deg, #ff8755, #f57445);
}

.eyes { position: absolute; display: flex; gap: 10px; left: 16px; top: 24px; }
.char-blue .eyes { left: 20px; top: 28px; }
.char-orange .eyes { top: 48px; }

.eye { position: relative; width: 22px; height: 17px; border-radius: 50%; background: #fafbff; }
.pupil {
  position: absolute;
  width: 8px;
  height: 8px;
  left: 7px;
  top: 5px;
  border-radius: 50%;
  background: #171b22;
  transform: translate(var(--gaze-x), var(--gaze-y));
  transition: transform 0.08s linear;
}

.mouth { position: absolute; left: 24px; top: 80px; width: 72px; height: 4px; border-radius: 10px; background: #1b2029; }

.back-dots { display: none; position: absolute; top: 26px; right: 14px; gap: 8px; }
.back-dots span { width: 6px; height: 6px; border-radius: 50%; background: rgba(255, 255, 255, 0.85); }

.is-private .character { animation: turnAround 0.35s ease forwards; }
.is-private .char-blue { transform: scaleX(-1) skewX(12deg); }
.is-private .char-blue.stretch { transform: scaleX(-1) skewX(12deg) translateX(-18px); animation: none; }
.is-private .eyes,
.is-private .mouth { display: none; }
.is-private .back-dots { display: flex; }

.form-panel {
  padding: 32px;
  background: rgba(255, 252, 246, 0.92);
  min-height: calc(100vh - 90px);
  opacity: 0;
  transform: translateX(46px);
}

.auth-form { display: grid; gap: 16px; margin-top: 28px; }
.field { display: grid; gap: 8px; color: var(--text); }
.field span { font-size: 14px; color: var(--muted); }

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

.field-username { position: relative; }
.username-box { position: relative; }

.pizza-burst-layer {
  position: absolute;
  left: 0;
  right: 0;
  top: 0;
  height: 50px;
  pointer-events: none;
  overflow: hidden;
  z-index: 6;
}

.pizza-impact {
  position: absolute;
  z-index: 3;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  transform: translate(-50%, -50%) rotate(var(--rot));
  transform-origin: center;
  background:
    radial-gradient(circle at 64% 36%, #f7d275 0 17%, transparent 18%),
    radial-gradient(circle at 38% 66%, #f7d275 0 16%, transparent 17%),
    radial-gradient(circle at 54% 52%, #db5b57 0 54%, #c34c49 58% 74%, #f2c48a 75% 100%);
  box-shadow:
    0 0 0 1px rgba(124, 73, 23, 0.24),
    0 1px 2px rgba(0, 0, 0, 0.22);
  animation: pizzaImpact 0.46s ease-out forwards;
}

.pixel-burst {
  position: absolute;
  z-index: 2;
  width: var(--size);
  height: var(--size);
  background: linear-gradient(135deg, color-mix(in srgb, var(--color) 86%, #fff 14%), var(--color));
  clip-path: polygon(14% 18%, 90% 8%, 72% 94%, 38% 70%);
  border-radius: 2px;
  transform: translate(-50%, -50%) rotate(var(--rot)) scale(0.84);
  transform-origin: center;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.18), inset 0 0 0 1px rgba(255, 252, 240, 0.2);
  animation: pixelPop 0.72s ease-out forwards;
  animation-delay: var(--delay);
}

.pixel-burst::after {
  content: '';
  position: absolute;
  width: calc(var(--size) * 0.34);
  height: calc(var(--size) * 0.34);
  right: 12%;
  top: 14%;
  border-radius: 50%;
  background: rgba(255, 240, 178, 0.9);
  box-shadow: 0 0 0 1px rgba(226, 188, 94, 0.55);
}

.password-box { position: relative; }

.toggle-btn {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  width: 28px;
  height: 28px;
  padding: 0;
  border: none;
  background: transparent;
  color: var(--brand-deep);
  z-index: 3;
  cursor: pointer;
}

.toggle-btn svg {
  width: 20px;
  height: 20px;
  stroke: currentColor;
  stroke-width: 1.8;
  fill: none;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.form-error { margin: 0; color: #b13f28; font-size: 14px; }

.submit-btn {
  height: 52px;
  border: none;
  border-radius: 999px;
  background: linear-gradient(135deg, var(--brand), var(--brand-deep));
  color: #fff;
  font-weight: 600;
}

.submit-btn:disabled { opacity: 0.72; cursor: not-allowed; }

.switch-tip { margin: 18px 0 0; color: var(--muted); }
.switch-tip a { color: var(--brand-deep); font-weight: 600; }

.auth-screen.enter-active .auth-copy {
  animation: leftPanelIn 0.95s cubic-bezier(0.22, 1, 0.36, 1) forwards;
}

.auth-screen.enter-active .auth-copy .eyebrow {
  animation: leftTextIn 0.72s ease forwards;
  animation-delay: 0.22s;
}

.auth-screen.enter-active .auth-copy h2 {
  animation: leftTextIn 0.9s ease forwards;
  animation-delay: 0.34s;
}

.auth-screen.enter-active .crew {
  animation: crewPopIn 1s cubic-bezier(0.2, 1.2, 0.2, 1) forwards;
  animation-delay: 1.15s;
}

.auth-screen.enter-active .form-panel {
  animation: rightPanelIn 0.95s cubic-bezier(0.22, 1, 0.36, 1) forwards;
  animation-delay: 1.15s;
}

@keyframes sneakPeek {
  0% { transform: skewX(-12deg) translateX(6px) translateY(-4px); }
  100% { transform: skewX(-12deg) translateX(14px) translateY(-7px); }
}

@keyframes leftPanelIn {
  from { opacity: 0; transform: translateX(-44px); }
  to { opacity: 1; transform: translateX(0); }
}

@keyframes leftTextIn {
  from { opacity: 0; transform: translateX(-26px); }
  to { opacity: 1; transform: translateX(0); }
}

@keyframes crewPopIn {
  0% { opacity: 0; transform: translate(var(--drift-x), calc(var(--drift-y) + 90px)) scale(0.88); }
  65% { opacity: 1; transform: translate(var(--drift-x), calc(var(--drift-y) - 14px)) scale(1.03); }
  100% { opacity: 1; transform: translate(var(--drift-x), var(--drift-y)) scale(1); }
}

@keyframes rightPanelIn {
  from { opacity: 0; transform: translateX(46px); }
  to { opacity: 1; transform: translateX(0); }
}

@keyframes floatSoft {
  0% { transform: translateY(0); }
  50% { transform: translateY(-12px); }
  100% { transform: translateY(0); }
}

@keyframes sparkleFloat {
  0% { transform: translateY(0); opacity: 0.15; }
  50% { transform: translateY(-18px); opacity: 0.65; }
  100% { transform: translateY(0); opacity: 0.15; }
}

@keyframes pixelPop {
  0% { opacity: 0.98; transform: translate(-50%, -50%) rotate(var(--rot)) scale(0.72); }
  100% { opacity: 0; transform: translate(calc(-50% + var(--dx)), calc(-50% + var(--dy))) rotate(calc(var(--rot) + var(--spin))) scale(1.02); }
}

@keyframes pizzaImpact {
  0% {
    opacity: 0;
    transform: translate(-50%, -50%) rotate(var(--rot)) scale(0.42);
    filter: saturate(1.05);
  }
  38% {
    opacity: 1;
    transform: translate(-50%, -50%) rotate(var(--rot)) scale(1);
    filter: saturate(1.08);
  }
  62% {
    opacity: 1;
    transform: translate(-50%, -50%) rotate(calc(var(--rot) + 2deg)) scale(1.04);
    background:
      repeating-conic-gradient(
        from 18deg,
        #d95f5a 0 20deg,
        #f3c88f 20deg 32deg,
        #d14f4b 32deg 48deg
      );
    clip-path: polygon(51% 49%, 100% 11%, 73% 100%, 5% 84%, 0 25%);
  }
  100% {
    opacity: 0;
    transform: translate(-50%, -50%) rotate(calc(var(--rot) + 9deg)) scale(1.18);
    clip-path: polygon(56% 47%, 100% 6%, 68% 100%, 0 78%, 0 20%);
    filter: blur(0.3px);
  }
}

@keyframes turnAround {
  0% { transform: scaleX(1); }
  100% { transform: scaleX(-1); }
}

@media (max-width: 900px) {
  .auth-screen {
    grid-template-columns: 1fr;
    min-height: auto;
  }

  .auth-copy,
  .form-panel {
    padding: 24px;
    min-height: 420px;
  }

  .character-stage { height: 64%; }

  .crew {
    left: 24%;
    right: -18%;
    transform: translate(var(--drift-x), var(--drift-y)) scale(0.92);
    transform-origin: left bottom;
  }
}
</style>
