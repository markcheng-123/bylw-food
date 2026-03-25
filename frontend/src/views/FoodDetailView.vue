<template>
  <section v-if="detail" class="detail-screen">
    <article class="detail-hero">
      <div class="gallery-panel">
        <img class="main-image" :src="resolveImage(currentImage)" :alt="detail.title" />
        <div class="thumb-list">
          <button v-for="image in detail.imageUrls" :key="image" type="button" :class="image === currentImage ? 'thumb active' : 'thumb'" @click="currentImage = image">
            <img :src="resolveImage(image)" :alt="detail.title" />
          </button>
        </div>
      </div>

      <div class="detail-info">
        <div class="tag-row">
          <span class="detail-tag">{{ detail.categoryName }}</span>
          <button
            v-if="detail.merchantLabel"
            type="button"
            class="merchant-badge"
            @click="openMerchantCard(detail.merchantUserId)"
          >
            {{ detail.merchantLabel || '官方认证' }}
          </button>
        </div>

        <h1>{{ detail.title }}</h1>
        <p class="summary">{{ detail.summary || '发布者没有填写摘要。' }}</p>

        <div class="price-line">
          <span>人均消费</span>
          <strong>{{ detail.perCapita ? `¥${detail.perCapita}` : '未填写' }}</strong>
        </div>

        <div class="address-line">
          <span class="map-icon">📍</span>
          <span class="address-text">{{ detail.address || '地址未填写' }}</span>
          <button type="button" class="map-btn" @click="viewMap">查看地图</button>
        </div>

        <div class="meta-row">
          <span>推荐人 {{ detail.authorNickname }}</span>
          <span>浏览 {{ detail.viewCount }}</span>
          <span>评论 {{ detail.commentCount }}</span>
        </div>

        <div class="action-panel">
          <button class="like-btn" :class="{ liked: detail.likedByCurrentUser }" type="button" :disabled="liking" @click="toggleLike">
            <span class="like-icon">👍</span>
            <span>{{ detail.likedByCurrentUser ? '已点赞' : '点赞支持' }}</span>
            <span>· {{ detail.likeCount }}</span>
          </button>
          <button v-if="isOwner" class="edit-btn" type="button" @click="openEditDialog">编辑帖子</button>
          <button
            v-if="detail.merchantUserId"
            type="button"
            class="merchant-link"
            @click="openMerchantCard(detail.merchantUserId)"
          >
            查看商家信息
          </button>
        </div>
      </div>
    </article>

    <article class="body-card">
      <div class="body-head"><p class="eyebrow">Story</p><h3>内容详情</h3></div>
      <p class="content-text">{{ detail.content }}</p>
    </article>

    <section v-if="detail.dishes?.length" class="dish-section">
      <div class="body-head"><p class="eyebrow">Dishes</p><h3>菜品食材与过敏原</h3></div>
      <div class="dish-grid">
        <article v-for="(dish, index) in detail.dishes" :key="`${dish.name}-${index}`" class="dish-item">
          <h4>{{ dish.name }}</h4>
          <div v-if="dish.imageUrls?.length" class="dish-image-list">
            <img v-for="url in dish.imageUrls" :key="url" class="dish-image" :src="resolveImage(url)" :alt="dish.name" />
          </div>
          <p class="dish-meta-line">
            <strong>食材：</strong>
            <span>{{ getVisibleIngredients(dish, index).join('、') || '未填写' }}</span>
            <button
              v-if="dish.ingredients.length > ingredientPreviewLimit"
              type="button"
              class="toggle-btn"
              @click="toggleIngredients(index)"
            >
              {{ expandedIngredients[index] ? '收起' : '展开' }}
            </button>
          </p>
          <div class="allergen-tags">
            <span
              v-for="tag in getVisibleAllergens(dish, index)"
              :key="tag"
              class="allergen-tag"
            >
              {{ tag }}
            </span>
            <button
              v-if="dish.allergens.length > allergenPreviewLimit"
              type="button"
              class="toggle-btn"
              @click="toggleAllergens(index)"
            >
              {{ expandedAllergens[index] ? '收起' : '展开' }}
            </button>
          </div>
        </article>
      </div>
    </section>

    <CommentSection :post-id="detail.id" @refreshed="handleCommentsRefreshed" />
    <MerchantCardDrawer v-model:visible="merchantDrawerVisible" :merchant-user-id="selectedMerchantUserId" />

    <div v-if="editDialogVisible" class="dialog-mask" @click.self="editDialogVisible = false">
      <article class="dialog-card">
        <h3>编辑帖子</h3>
        <div class="dialog-form">
          <label>
            <span>分类</span>
            <select v-model.number="editForm.categoryId">
              <option v-for="item in categories" :key="item.id" :value="item.id">{{ item.name }}</option>
            </select>
          </label>
          <label>
            <span>标题</span>
            <input v-model.trim="editForm.title" type="text" />
          </label>
          <label>
            <span>摘要</span>
            <input v-model.trim="editForm.summary" type="text" />
          </label>
          <label>
            <span>地址</span>
            <input v-model.trim="editForm.address" type="text" />
          </label>
          <label>
            <span>人均消费</span>
            <input v-model.number="editForm.perCapita" type="number" min="0" />
          </label>
          <label>
            <span>正文</span>
            <textarea v-model.trim="editForm.content" rows="6" />
          </label>
        </div>
        <div class="dialog-actions">
          <button type="button" class="ghost-btn" @click="editDialogVisible = false">取消</button>
          <button type="button" class="primary-btn" :disabled="savingEdit" @click="saveEdit">{{ savingEdit ? '保存中...' : '保存' }}</button>
        </div>
      </article>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import CommentSection from '@/components/CommentSection.vue'
import MerchantCardDrawer from '@/components/MerchantCardDrawer.vue'
import { unlikePost, likePost } from '@/api/like'
import { fetchFoodPostDetail, updateFoodPost, type FoodPostDetail } from '@/api/post'
import { fetchCategoryOptions, type CategoryItem } from '@/api/category'
import { authState } from '@/stores/auth'

const API_HOST = 'http://localhost:8080'
const fallbackImage = 'https://images.unsplash.com/photo-1559339352-11d035aa65de?auto=format&fit=crop&w=1200&q=80'

const route = useRoute()
const detail = ref<FoodPostDetail | null>(null)
const currentImage = ref('')
const liking = ref(false)
const merchantDrawerVisible = ref(false)
const selectedMerchantUserId = ref<number | null>(null)
const editDialogVisible = ref(false)
const savingEdit = ref(false)
const categories = ref<CategoryItem[]>([])
const expandedIngredients = ref<Record<number, boolean>>({})
const expandedAllergens = ref<Record<number, boolean>>({})
const ingredientPreviewLimit = 1
const allergenPreviewLimit = 1
const editForm = ref({
  categoryId: 0,
  title: '',
  summary: '',
  content: '',
  address: '',
  perCapita: undefined as number | undefined,
})

const isOwner = computed(() => Boolean(detail.value && authState.user?.id === detail.value.authorUserId))

function resolveImage(url: string | null) {
  if (!url) return fallbackImage
  return url.startsWith('http') ? url : `${API_HOST}${url}`
}

async function loadDetail() {
  const response = await fetchFoodPostDetail(Number(route.params.id))
  detail.value = response.data
  currentImage.value = response.data.imageUrls?.[0] || response.data.coverImage || ''
}

function openEditDialog() {
  if (!detail.value) return
  editForm.value = {
    categoryId: detail.value.categoryId,
    title: detail.value.title,
    summary: detail.value.summary || '',
    content: detail.value.content,
    address: detail.value.address || '',
    perCapita: detail.value.perCapita ?? undefined,
  }
  editDialogVisible.value = true
}

async function saveEdit() {
  if (!detail.value) return
  try {
    savingEdit.value = true
    await updateFoodPost(detail.value.id, {
      categoryId: editForm.value.categoryId,
      title: editForm.value.title,
      summary: editForm.value.summary || undefined,
      content: editForm.value.content,
      address: editForm.value.address || undefined,
      perCapita: editForm.value.perCapita,
    })
    editDialogVisible.value = false
    await loadDetail()
  } finally {
    savingEdit.value = false
  }
}

async function toggleLike() {
  if (!detail.value) return
  try {
    liking.value = true
    const response = detail.value.likedByCurrentUser ? await unlikePost(detail.value.id) : await likePost(detail.value.id)
    detail.value.likedByCurrentUser = response.data.liked
    detail.value.likeCount = response.data.likeCount
  } finally {
    liking.value = false
  }
}

function viewMap() {
  if (!detail.value?.address) return
  const query = encodeURIComponent(detail.value.address)
  window.open(`https://uri.amap.com/search?keyword=${query}`, '_blank')
}

function handleCommentsRefreshed(count: number) {
  if (!detail.value) return
  detail.value.commentCount = count
}

function toggleIngredients(index: number) {
  expandedIngredients.value[index] = !expandedIngredients.value[index]
}

function toggleAllergens(index: number) {
  expandedAllergens.value[index] = !expandedAllergens.value[index]
}

function getVisibleIngredients(dish: FoodPostDetail['dishes'][number], index: number) {
  if (expandedIngredients.value[index]) return dish.ingredients
  return dish.ingredients.slice(0, ingredientPreviewLimit)
}

function getVisibleAllergens(dish: FoodPostDetail['dishes'][number], index: number) {
  if (expandedAllergens.value[index]) return dish.allergens
  return dish.allergens.slice(0, allergenPreviewLimit)
}

function openMerchantCard(merchantUserId: number | null) {
  if (!merchantUserId) return
  selectedMerchantUserId.value = merchantUserId
  merchantDrawerVisible.value = true
}

onMounted(async () => {
  await Promise.all([loadDetail(), fetchCategoryOptions().then((res) => { categories.value = res.data })])
})
</script>

<style scoped>
.detail-screen { display: grid; gap: 18px; }
.detail-hero { display: grid; grid-template-columns: 1.05fr .95fr; gap: 16px; }
.gallery-panel, .detail-info, .body-card, .dish-section { padding: 20px; border: 1px solid var(--line); border-radius: 18px; background: rgba(255,252,246,.94); box-shadow: var(--shadow); }
.main-image { width: 100%; height: 420px; object-fit: cover; border-radius: 14px; display: block; }
.thumb-list { display: grid; grid-template-columns: repeat(4,minmax(0,1fr)); gap: 8px; margin-top: 10px; }
.thumb { border: none; background: transparent; padding: 0; border-radius: 12px; overflow: hidden; opacity: .72; }
.thumb.active { opacity: 1; outline: 2px solid rgba(189,91,47,.45); }
.thumb img { width: 100%; height: 74px; object-fit: cover; display: block; }
.tag-row { display: flex; gap: 8px; flex-wrap: wrap; align-items: center; }
.detail-tag { display: inline-flex; padding: 6px 10px; border-radius: 999px; background: rgba(189,91,47,.12); color: var(--brand-deep); }
.merchant-badge { display: inline-flex; align-items: center; padding: 6px 10px; border-radius: 999px; border: 1px solid rgba(193,150,85,.45); background: linear-gradient(135deg, rgba(255,243,221,.95), rgba(245,223,186,.96)); color: #8c5b18; font-size: 12px; font-weight: 600; }
.detail-info h1 { margin: 12px 0 0; font-size: clamp(30px,4vw,44px); line-height: 1.12; color: #3f2b1d; }
.summary, .content-text { color: var(--muted); line-height: 1.75; }
.price-line { margin-top: 14px; display: flex; align-items: center; gap: 10px; }
.price-line span { color: var(--muted); }
.price-line strong { font-size: 34px; line-height: 1; color: var(--brand); font-weight: 800; }
.address-line { margin-top: 12px; min-height: 44px; border-radius: 12px; border: 1px solid rgba(98,61,27,.14); background: rgba(255,255,255,.65); padding: 0 12px; display: flex; align-items: center; gap: 8px; }
.map-icon { font-size: 14px; }
.address-text { flex: 1; color: #6f5f52; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.map-btn { border: none; background: transparent; color: var(--brand-deep); font-size: 13px; }
.meta-row { margin-top: 10px; display: flex; gap: 12px; flex-wrap: wrap; color: #9d8c7e; font-size: 13px; }
.action-panel { margin-top: 16px; display: flex; gap: 10px; align-items: center; flex-wrap: wrap; }
.like-btn { min-width: 160px; height: 44px; border: none; border-radius: 999px; background: linear-gradient(135deg,var(--brand),var(--brand-deep)); color: #fff; padding: 0 16px; font-weight: 700; display: inline-flex; align-items: center; gap: 6px; box-shadow: 0 10px 20px rgba(140,52,23,.28); }
.like-btn.liked { filter: saturate(1.1) brightness(0.96); }
.like-icon { font-size: 15px; line-height: 1; }
.edit-btn { min-width: 110px; height: 40px; border: 1px solid rgba(140,52,23,.2); border-radius: 999px; background: #fff; color: var(--brand-deep); }
.merchant-link { border: none; background: transparent; color: #8c5b18; font-size: 13px; }
.body-head h3, .body-head p { margin: 0; }
.eyebrow { margin-bottom: 8px; font-size: 12px; letter-spacing: .16em; text-transform: uppercase; color: var(--brand-deep); }
.content-text { white-space: pre-wrap; margin: 12px 0 0; }
.dish-section { display: grid; gap: 12px; }
.dish-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 10px; }
.dish-item { border: 1px solid rgba(98,61,27,.12); border-radius: 12px; padding: 12px; background: rgba(255,255,255,.62); min-height: 100%; }
.dish-item h4 { margin: 0 0 8px; }
.dish-image-list { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 8px; }
.dish-image {
  width: calc((100% - 32px) / 5);
  max-width: 140px;
  min-width: 96px;
  aspect-ratio: 1 / 1;
  object-fit: cover;
  border-radius: 10px;
  display: block;
}
.dish-meta-line { margin: 6px 0 0; color: #6a5b4f; display: inline-flex; align-items: center; gap: 6px; flex-wrap: wrap; }
.allergen-tags { display: flex; gap: 8px; flex-wrap: wrap; margin-top: 8px; align-items: center; }
.allergen-tag { display: inline-flex; padding: 4px 9px; border-radius: 999px; background: rgba(203,54,54,.12); color: #b03535; font-size: 12px; }
.toggle-btn { border: none; background: transparent; color: var(--brand-deep); font-size: 12px; padding: 0; }
.dialog-mask { position: fixed; inset: 0; background: rgba(20,20,24,.45); display: grid; place-items: center; z-index: 60; padding: 16px; }
.dialog-card { width: min(720px, 96vw); background: #fffdf8; border-radius: 18px; border: 1px solid var(--line); padding: 16px; }
.dialog-card h3 { margin: 0 0 12px; }
.dialog-form { display: grid; gap: 10px; }
.dialog-form label { display: grid; gap: 6px; }
.dialog-form span { color: var(--muted); font-size: 13px; }
.dialog-form input, .dialog-form textarea, .dialog-form select { width: 100%; border: 1px solid rgba(98,61,27,.14); border-radius: 12px; padding: 10px 12px; background: #fff; }
.dialog-actions { margin-top: 12px; display: flex; justify-content: flex-end; gap: 10px; }
@media (max-width: 900px) {
  .detail-hero, .thumb-list { grid-template-columns: 1fr; }
  .gallery-panel, .detail-info, .body-card, .dish-section { padding: 16px; }
  .main-image { height: 300px; }
  .dish-image { width: calc((100% - 16px) / 3); }
  .dish-grid { grid-template-columns: 1fr; }
}
@media (max-width: 560px) {
  .dish-image { width: calc((100% - 8px) / 2); }
}
</style>
