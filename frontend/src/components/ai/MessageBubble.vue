<template>
  <div :class="role === 'user' ? 'row user' : 'row ai'">
    <img v-if="role === 'ai'" class="avatar" :src="avatar" alt="ai-avatar" />
    <div class="bubble" v-html="formattedContent"></div>
    <img v-if="role === 'user'" class="avatar" :src="avatar" alt="user-avatar" />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  role: 'user' | 'ai'
  content: string
  avatar: string
}>()

function escapeHtml(input: string): string {
  return input
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
}

function renderBasicMarkdown(text: string): string {
  // Keep rendering minimal and safe: basic emphasis + line breaks.
  const escaped = escapeHtml(text)
  return escaped
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    // Handle single-star emphasis like "*类别*".
    .replace(/(^|[^*])\*([^*\n]+)\*(?=$|[^*])/g, '$1<strong>$2</strong>')
    // If model returns unpaired stars, drop them to avoid visual noise.
    .replace(/\*/g, '')
    .replace(/\n/g, '<br>')
}

const formattedContent = computed(() => renderBasicMarkdown(props.content || ''))
</script>

<style scoped>
.row {
  display: flex;
  gap: 10px;
  align-items: flex-end;
}

.row.user {
  justify-content: flex-end;
}

.row.ai {
  justify-content: flex-start;
}

.avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.bubble {
  max-width: 78%;
  border-radius: 14px;
  padding: 9px 12px;
  font-size: 13px;
  line-height: 1.55;
  white-space: pre-wrap;
  word-break: break-word;
}

.row.user .bubble {
  background: linear-gradient(135deg, #996242, #7f4728);
  color: #fff;
  border-bottom-right-radius: 6px;
}

.row.ai .bubble {
  background: #f8f4ed;
  color: #5f4d3f;
  border: 1px solid rgba(128, 98, 71, 0.14);
  border-bottom-left-radius: 6px;
}
</style>
