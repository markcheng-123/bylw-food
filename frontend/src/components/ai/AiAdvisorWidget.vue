<template>
  <div class="advisor-widget">
    <button v-if="!open" class="float-btn" type="button" @click="open = true">
      <span>🦞</span>
    </button>

    <section v-else class="chat-panel">
      <header class="panel-head" v-memo="[aiState]">
        <div class="ai-meta">
          <div class="avatar-shell">
            <span class="lobster-avatar" :class="`state-${aiState}`">🦞</span>
          </div>
          <div>
            <h4>AI 食物智能顾问</h4>
            <p>{{ stateLabel }}</p>
          </div>
        </div>
        <button class="close-btn" type="button" @click="handleClose">×</button>
      </header>

      <div ref="listRef" class="message-list">
        <MessageBubble
          v-for="msg in messages"
          :key="msg.id"
          :role="msg.role"
          :content="msg.content"
          :avatar="msg.role === 'ai' ? aiAvatar : userAvatar"
        />
      </div>

      <footer class="input-row">
        <input
          v-model.trim="inputText"
          class="chat-input-native"
          placeholder="说出预算、口味、区域，例如：人均50，想吃辣，在武昌"
          @keyup.enter="sendMessage"
        />
        <el-button type="primary" :loading="loading" :disabled="!inputText || loading" @click="sendMessage">发送</el-button>
      </footer>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { ElButton } from 'element-plus'
import MessageBubble from './MessageBubble.vue'
import { authState } from '@/stores/auth'
import { getApiBaseUrl, getApiHost } from '@/config/runtime'

type AiState = 'idle' | 'thinking' | 'speaking'
type Role = 'user' | 'ai'

interface ChatMessage {
  id: number
  role: Role
  content: string
}

const open = ref(false)
const loading = ref(false)
const aiState = ref<AiState>('idle')
const inputText = ref('')
const listRef = ref<HTMLElement | null>(null)

const messages = ref<ChatMessage[]>([
  { id: 1, role: 'ai', content: '输入预算、口味、区域，我将按站内真实数据推荐。' },
])

const API_BASE_URL = getApiBaseUrl()
const API_HOST = getApiHost()
const aiAvatar =
  'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="64" height="64"><rect width="64" height="64" rx="32" fill="%23f4e5d3"/><text x="32" y="40" text-anchor="middle" font-size="22">🦞</text></svg>'

const userAvatar = computed(() => {
  const avatar = authState.user?.avatar
  if (!avatar) {
    return 'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="64" height="64"><rect width="64" height="64" rx="32" fill="%23d8c2ad"/><text x="32" y="38" text-anchor="middle" font-size="20" fill="%2360402a">我</text></svg>'
  }
  return avatar.startsWith('http') ? avatar : `${API_HOST}${avatar}`
})

const stateLabel = computed(() => {
  if (aiState.value === 'thinking') return '思考中...'
  if (aiState.value === 'speaking') return '回答中...'
  return '在线'
})

let idSeed = 2
let typingTimer: number | null = null
let typingBuffer = ''
let streamFinished = false
let currentAiMsgId: number | null = null
let currentAbort: AbortController | null = null
let scrollTicking = false

function pushMessage(role: Role, content: string) {
  messages.value.push({ id: idSeed++, role, content })
  queueScroll()
}

function getCurrentAiMessage() {
  if (!currentAiMsgId) return null
  return messages.value.find((item) => item.id === currentAiMsgId) || null
}

function queueScroll() {
  if (scrollTicking) return
  scrollTicking = true
  requestAnimationFrame(() => {
    scrollTicking = false
    if (!listRef.value) return
    listRef.value.scrollTop = listRef.value.scrollHeight
  })
}

function startTyping() {
  if (typingTimer) return
  typingTimer = window.setInterval(() => {
    const target = getCurrentAiMessage()
    if (!target) return

    if (!typingBuffer.length) {
      if (streamFinished) {
        stopTyping()
        aiState.value = 'idle'
      }
      return
    }

    const step = Math.min(4, typingBuffer.length)
    target.content += typingBuffer.slice(0, step)
    typingBuffer = typingBuffer.slice(step)
    queueScroll()
  }, 30)
}

function stopTyping() {
  if (typingTimer) {
    window.clearInterval(typingTimer)
    typingTimer = null
  }
}

function resetStreamState() {
  typingBuffer = ''
  streamFinished = false
  currentAiMsgId = null
  stopTyping()
}

function handleClose() {
  open.value = false
  currentAbort?.abort()
  currentAbort = null
  loading.value = false
  aiState.value = 'idle'
  resetStreamState()
}

function handleOpenAdvisor() {
  open.value = true
}

async function sendMessage() {
  const text = inputText.value.trim()
  if (!text || loading.value) return

  pushMessage('user', text)
  inputText.value = ''
  loading.value = true
  aiState.value = 'thinking'
  resetStreamState()

  const controller = new AbortController()
  currentAbort = controller

  try {
    const resp = await fetch(`${API_BASE_URL}/ai/chat/stream?prompt=${encodeURIComponent(text)}`, {
      method: 'GET',
      headers: {
        Accept: 'text/event-stream',
      },
      signal: controller.signal,
    })

    if (!resp.ok || !resp.body) {
      throw new Error(`SSE 请求失败: ${resp.status}`)
    }

    const reader = resp.body.getReader()
    const decoder = new TextDecoder('utf-8')
    let buffer = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      buffer += decoder.decode(value, { stream: true })
      const blocks = buffer.split('\n\n')
      buffer = blocks.pop() || ''

      for (const block of blocks) {
        if (!block.trim()) continue

        let eventName = 'data'
        const dataLines: string[] = []
        for (const line of block.split('\n')) {
          if (line.startsWith('event:')) {
            eventName = line.slice(6).trim()
          } else if (line.startsWith('data:')) {
            dataLines.push(line.slice(5).trim())
          }
        }
        const dataText = dataLines.join('\n')
        if (!dataText) continue

        if (eventName === 'error') {
          throw new Error(dataText)
        }
        if (eventName === 'done' || dataText === '[DONE]') {
          streamFinished = true
          continue
        }

        if (!currentAiMsgId) {
          pushMessage('ai', '')
          currentAiMsgId = messages.value[messages.value.length - 1].id
          aiState.value = 'speaking'
          startTyping()
        }
        typingBuffer += dataText
      }
    }

    streamFinished = true
    if (!typingBuffer.length) {
      aiState.value = 'idle'
    }
  } catch (error) {
    pushMessage('ai', error instanceof Error ? error.message : 'AI 连接失败，请稍后再试。')
    aiState.value = 'idle'
  } finally {
    loading.value = false
    currentAbort = null
  }
}

onMounted(() => {
  window.addEventListener('open-ai-advisor', handleOpenAdvisor)
})

onBeforeUnmount(() => {
  window.removeEventListener('open-ai-advisor', handleOpenAdvisor)
  currentAbort?.abort()
  stopTyping()
})
</script>

<style scoped>
.advisor-widget {
  position: fixed;
  right: 22px;
  bottom: 22px;
  z-index: 90;
}

.float-btn {
  width: 62px;
  height: 62px;
  border-radius: 50%;
  border: 1px solid rgba(139, 77, 43, 0.34);
  background: linear-gradient(145deg, #fff6ea, #f0dcc4);
  box-shadow: 0 14px 28px rgba(56, 34, 18, 0.2);
  font-size: 28px;
  cursor: pointer;
}

.chat-panel {
  width: min(390px, calc(100vw - 24px));
  height: min(540px, calc(100vh - 34px));
  border-radius: 16px;
  border: 1px solid rgba(132, 94, 62, 0.13);
  background: #fffdf9;
  box-shadow: 0 26px 56px rgba(67, 41, 19, 0.24), 0 8px 18px rgba(67, 41, 19, 0.08);
  display: grid;
  grid-template-rows: auto 1fr auto;
  overflow: hidden;
}

.panel-head {
  padding: 10px 12px;
  border-bottom: 1px solid rgba(132, 94, 62, 0.12);
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.ai-meta {
  display: flex;
  align-items: center;
  gap: 10px;
}

.avatar-shell {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(145deg, #fff8f0, #fff2df);
  border: 1px solid rgba(132, 94, 62, 0.16);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.lobster-avatar {
  font-size: 27px;
  line-height: 1;
  filter: drop-shadow(0 2px 4px rgba(178, 56, 36, 0.22));
}

.lobster-avatar.state-idle {
  animation: lobster-float 2.4s ease-in-out infinite;
}

.lobster-avatar.state-thinking {
  animation: lobster-think 1.2s linear infinite;
}

.lobster-avatar.state-speaking {
  animation: lobster-speak 0.6s ease-in-out infinite;
}

.ai-meta h4 {
  margin: 0;
  font-size: 14px;
  color: #4f3a2a;
}

.ai-meta p {
  margin: 2px 0 0;
  font-size: 12px;
  color: #8d7a66;
}

.close-btn {
  width: 30px;
  height: 30px;
  border-radius: 999px;
  border: 1px solid rgba(132, 94, 62, 0.2);
  background: #fff;
  color: #6f5946;
  font-size: 20px;
  line-height: 1;
  cursor: pointer;
}

.message-list {
  padding: 12px;
  overflow-y: auto;
  display: grid;
  align-content: start;
  gap: 10px;
  background: linear-gradient(180deg, rgba(250, 244, 236, 0.8), rgba(255, 253, 249, 0.9));
}

.input-row {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 10px;
  align-items: center;
  padding: 10px 12px 12px;
  border-top: 1px solid rgba(132, 94, 62, 0.12);
  background: #fffdf9;
}

.chat-input-native {
  height: 40px;
  width: 100%;
  border-radius: 999px;
  background: #fff;
  border: 1px solid rgba(132, 94, 62, 0.16);
  box-shadow: none;
  outline: none;
  padding: 0 14px;
  font-size: 14px;
  color: #4f3a2a;
}

.chat-input-native::placeholder {
  color: #9a8979;
}

.chat-input-native:focus {
  border-color: rgba(140, 52, 23, 0.36);
  box-shadow: 0 0 0 3px rgba(189, 91, 47, 0.14);
}

.chat-input-native:-webkit-autofill,
.chat-input-native:-webkit-autofill:hover,
.chat-input-native:-webkit-autofill:focus {
  -webkit-text-fill-color: #4f3a2a;
  transition: background-color 600000s 0s, color 600000s 0s;
}

.input-row :deep(.el-button) {
  height: 40px;
  border-radius: 999px;
}

@media (max-width: 768px) {
  .advisor-widget {
    right: 12px;
    bottom: 12px;
  }
}

@keyframes lobster-float {
  0% { transform: translateY(0); }
  50% { transform: translateY(-2px); }
  100% { transform: translateY(0); }
}

@keyframes lobster-think {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

@keyframes lobster-speak {
  0% { transform: scale(1); }
  50% { transform: scale(1.08); }
  100% { transform: scale(1); }
}
</style>
