<template>
  <RouterLink class="food-card" :to="`/food/${item.id}`">
    <span v-if="isHot" class="hot-ribbon">爆款</span>
    <img class="cover" :src="resolveImage(item.coverImage)" :alt="item.title" />

    <div class="body">
      <div class="title-row">
        <h3>{{ item.title }}</h3>
        <div class="mini-tags">
          <span class="mini-tag">{{ item.categoryName }}</span>
          <span v-if="item.merchantLabel" class="mini-tag merchant">商家</span>
        </div>
      </div>

      <p class="summary">{{ item.summary || '点击查看完整详情与互动数据。' }}</p>

      <div class="footer">
        <span class="address">
          <span class="pin">📍</span>
          <span class="text">{{ item.address || '地址未填写' }}</span>
        </span>
        <span class="stats">浏览 {{ item.viewCount }} · 点赞 {{ item.likeCount }}</span>
      </div>
    </div>
  </RouterLink>
</template>

<script setup lang="ts">
import { getApiHost } from '@/config/runtime'
import type { FoodPostCard } from '@/api/post'

const API_HOST = getApiHost()
const fallbackImage = 'https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?auto=format&fit=crop&w=1200&q=80'

defineProps<{
  item: FoodPostCard
  isHot?: boolean
}>()

function resolveImage(url: string | null) {
  if (!url) return fallbackImage
  return url.startsWith('http') ? url : `${API_HOST}${url}`
}
</script>

<style scoped>
.food-card {
  position: relative;
  overflow: hidden;
  border-radius: 18px;
  border: 1px solid rgba(98, 61, 27, 0.1);
  background: #fffdfa;
  box-shadow: 0 10px 26px rgba(69, 43, 20, 0.08);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.food-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 14px 32px rgba(69, 43, 20, 0.14);
}

.hot-ribbon {
  position: absolute;
  left: 10px;
  top: 10px;
  z-index: 2;
  padding: 4px 10px;
  border-radius: 999px;
  background: linear-gradient(135deg, #ffb347, #ff7a18);
  color: #fff;
  font-size: 12px;
  font-weight: 700;
}

.cover {
  width: 100%;
  height: 168px;
  object-fit: cover;
  display: block;
}

.body {
  padding: 12px;
  display: grid;
  gap: 8px;
}

.title-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  align-items: start;
  gap: 8px;
}

.title-row h3 {
  margin: 0;
  min-width: 0;
  font-size: 20px;
  line-height: 1.25;
  color: #3f2b1d;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.mini-tags {
  display: inline-flex;
  gap: 6px;
  flex-shrink: 0;
  min-width: 0;
  max-width: 100%;
}

.mini-tag {
  height: 22px;
  padding: 0 8px;
  border-radius: 999px;
  background: rgba(189, 91, 47, 0.12);
  color: #8c3417;
  font-size: 11px;
  display: inline-flex;
  align-items: center;
  max-width: 96px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.mini-tag.merchant {
  background: rgba(245, 223, 186, 0.9);
  border: 1px solid rgba(193, 150, 85, 0.45);
  color: #8c5b18;
}

.summary {
  margin: 0;
  color: #8b7867;
  font-size: 13px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.footer {
  display: grid;
  gap: 4px;
  font-size: 12px;
  color: #a19082;
}

.address {
  min-width: 0;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.text {
  min-width: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.stats {
  justify-self: end;
  max-width: 100%;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>


