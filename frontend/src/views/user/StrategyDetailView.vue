<template>
  <section v-if="detail" class="strategy-detail">
    <article class="detail-hero">
      <img :src="resolveImage(detail.coverImage)" :alt="detail.title" />
      <div class="hero-copy">
        <p class="eyebrow">Guide Story</p>
        <h2>{{ detail.title }}</h2>
        <p class="summary">{{ detail.summary || '这篇攻略整理了适合展示和实际使用的路线建议。' }}</p>
        <div class="meta-row">
          <span>作者：{{ detail.authorNickname }}</span>
          <span>发布时间：{{ formatDate(detail.publishedAt || detail.createdAt) }}</span>
        </div>
      </div>
    </article>

    <article class="content-card">
      <div class="content-head">
        <p class="eyebrow">Long Read</p>
        <h3>完整攻略</h3>
      </div>
      <p class="content-text">{{ detail.content }}</p>
    </article>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { fetchStrategyDetail, type StrategyDetail } from '@/api/strategy'

const route = useRoute()
const detail = ref<StrategyDetail | null>(null)
const API_HOST = 'http://localhost:8080'
const fallbackImage = 'https://images.unsplash.com/photo-1504674900247-0877df9cc836?auto=format&fit=crop&w=1200&q=80'

function resolveImage(url: string | null) {
  if (!url) {
    return fallbackImage
  }
  return url.startsWith('http') ? url : `${API_HOST}${url}`
}

function formatDate(value: string) {
  return value.replace('T', ' ').slice(0, 16)
}

onMounted(async () => {
  const response = await fetchStrategyDetail(Number(route.params.id))
  detail.value = response.data
})
</script>

<style scoped>
.strategy-detail {
  display: grid;
  gap: 22px;
}

.detail-hero,
.content-card {
  padding: 28px;
  border: 1px solid var(--line);
  border-radius: var(--radius-xl);
  background: rgba(255, 252, 246, 0.92);
  box-shadow: var(--shadow);
}

.detail-hero {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

.detail-hero img {
  width: 100%;
  height: 420px;
  object-fit: cover;
  border-radius: var(--radius-lg);
}

.hero-copy h2,
.content-head h3 {
  margin: 0;
}

.hero-copy h2 {
  font-size: clamp(32px, 4vw, 52px);
  line-height: 1.12;
}

.summary,
.content-text,
.meta-row {
  color: var(--muted);
  line-height: 1.85;
}

.meta-row {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  margin-top: 18px;
}

.content-text {
  white-space: pre-wrap;
  margin: 18px 0 0;
  font-size: 17px;
}

@media (max-width: 900px) {
  .detail-hero {
    grid-template-columns: 1fr;
  }

  .detail-hero,
  .content-card {
    padding: 22px;
  }

  .detail-hero img {
    height: 300px;
  }
}
</style>
