<template>
  <section class="strategy-screen">
    <article class="strategy-hero">
      <div>
        <p class="eyebrow">City Notes</p>
        <h2>把吃什么、怎么排、什么时间去最舒服，整理成一份真正有用的城市味觉攻略。</h2>
      </div>
      <div class="hero-side">
        <strong>{{ total }} 篇</strong>
        <span>当前可浏览攻略</span>
      </div>
    </article>

    <section class="filter-panel">
      <label class="field">
        <span>搜索攻略</span>
        <input v-model.trim="keyword" type="text" placeholder="按标题、摘要或正文搜索" @keyup.enter="applyFilters" />
      </label>
      <button class="ghost-btn action-btn" type="button" @click="applyFilters">搜索</button>
      <button class="ghost-btn action-btn" type="button" @click="resetFilters">重置</button>
    </section>

    <div v-if="records.length" class="strategy-grid">
      <RouterLink v-for="item in records" :key="item.id" class="strategy-card" :to="`/strategies/${item.id}`">
        <img :src="resolveImage(item.coverImage)" :alt="item.title" />
        <div class="card-body">
          <div class="card-meta">
            <span>{{ item.authorNickname }}</span>
            <span>{{ formatDate(item.publishedAt || item.createdAt) }}</span>
          </div>
          <h3>{{ item.title }}</h3>
          <p>{{ item.summary || '这是一篇适合点开细读的本地攻略。' }}</p>
        </div>
      </RouterLink>
    </div>

    <div v-else class="empty-panel">
      <strong>暂时没有匹配的攻略</strong>
      <p>你可以换个关键词试试，或者先准备几篇答辩展示用攻略数据。</p>
    </div>

    <div class="pager">
      <button class="ghost-btn pager-btn" type="button" :disabled="current <= 1" @click="changePage(current - 1)">上一页</button>
      <span>第 {{ current }} / {{ totalPages }} 页</span>
      <button class="ghost-btn pager-btn" type="button" :disabled="current >= totalPages" @click="changePage(current + 1)">下一页</button>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { fetchStrategyList, type StrategyCard } from '@/api/strategy'

const route = useRoute()
const router = useRouter()

const API_HOST = 'http://localhost:8080'
const fallbackImage = 'https://images.unsplash.com/photo-1498654896293-37aacf113fd9?auto=format&fit=crop&w=1200&q=80'
const keyword = ref('')
const current = ref(1)
const size = 6
const total = ref(0)
const records = ref<StrategyCard[]>([])

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / size)))

function resolveImage(url: string | null) {
  if (!url) {
    return fallbackImage
  }
  return url.startsWith('http') ? url : `${API_HOST}${url}`
}

function formatDate(value: string) {
  return value.replace('T', ' ').slice(0, 16)
}

function syncFromRoute() {
  keyword.value = typeof route.query.keyword === 'string' ? route.query.keyword : ''
  current.value = typeof route.query.current === 'string' ? Number(route.query.current) || 1 : 1
}

async function loadStrategies() {
  const response = await fetchStrategyList({
    keyword: keyword.value || undefined,
    current: current.value,
    size,
  })
  total.value = response.data.total
  records.value = response.data.records
}

function applyFilters() {
  router.push({
    path: '/strategies',
    query: {
      keyword: keyword.value || undefined,
      current: 1,
    },
  })
}

function resetFilters() {
  keyword.value = ''
  router.push('/strategies')
}

function changePage(page: number) {
  router.push({
    path: '/strategies',
    query: {
      keyword: keyword.value || undefined,
      current: page,
    },
  })
}

watch(
  () => route.fullPath,
  async () => {
    syncFromRoute()
    await loadStrategies()
  },
)

onMounted(async () => {
  syncFromRoute()
  await loadStrategies()
})
</script>

<style scoped>
.strategy-screen {
  display: grid;
  gap: 22px;
}

.strategy-hero,
.filter-panel,
.empty-panel {
  padding: 28px;
  border: 1px solid var(--line);
  border-radius: var(--radius-xl);
  background: rgba(255, 252, 246, 0.92);
  box-shadow: var(--shadow);
}

.strategy-hero {
  display: grid;
  grid-template-columns: 1.3fr 0.7fr;
  gap: 20px;
  background:
    radial-gradient(circle at top left, rgba(47, 107, 79, 0.16), transparent 34%),
    radial-gradient(circle at bottom right, rgba(189, 91, 47, 0.18), transparent 32%),
    linear-gradient(135deg, rgba(253, 248, 240, 0.95), rgba(239, 226, 207, 0.92));
}

.strategy-hero h2 {
  margin: 0;
  font-size: clamp(30px, 4vw, 48px);
  line-height: 1.16;
  max-width: 14ch;
}

.hero-side {
  padding: 20px;
  border-radius: var(--radius-lg);
  background: rgba(255, 255, 255, 0.66);
}

.hero-side strong {
  display: block;
  font-size: 40px;
}

.hero-side span,
.card-meta,
.strategy-card p,
.empty-panel p {
  color: var(--muted);
}

.filter-panel {
  display: grid;
  grid-template-columns: 1fr 120px 120px;
  gap: 14px;
  align-items: end;
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
  height: 50px;
  padding: 0 16px;
  border: 1px solid rgba(98, 61, 27, 0.14);
  border-radius: 16px;
  background: #fffdfa;
}

.action-btn {
  height: 50px;
}

.strategy-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.strategy-card {
  overflow: hidden;
  border-radius: var(--radius-lg);
  background: #fffdfa;
  border: 1px solid rgba(98, 61, 27, 0.08);
  box-shadow: var(--shadow);
}

.strategy-card img {
  width: 100%;
  height: 240px;
  object-fit: cover;
}

.card-body {
  padding: 18px;
}

.card-meta {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  font-size: 14px;
}

.card-body h3 {
  margin: 14px 0 0;
  font-size: 24px;
  line-height: 1.3;
}

.card-body p {
  margin: 12px 0 0;
  line-height: 1.75;
}

.empty-panel {
  text-align: center;
}

.pager {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 14px;
}

.pager-btn {
  min-width: 98px;
}

@media (max-width: 1100px) {
  .strategy-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 900px) {
  .strategy-hero,
  .filter-panel,
  .strategy-grid {
    grid-template-columns: 1fr;
  }

  .strategy-hero,
  .filter-panel,
  .empty-panel {
    padding: 22px;
  }

  .pager {
    display: grid;
    grid-template-columns: 1fr;
  }
}
</style>
