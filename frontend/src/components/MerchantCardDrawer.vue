<template>
  <div v-if="visible" class="card-mask" @click="close">
    <article class="card-shell" @click.stop>
      <header class="card-head">
        <div>
          <p class="eyebrow">商家名片</p>
          <h3>{{ card?.merchantName || card?.storeName || '商家信息' }}</h3>
          <p>{{ card?.storeAddress || '暂未填写门店地址' }}</p>
          <span v-if="card" :class="statusClass">
            <span class="status-dot" aria-hidden="true"></span>
            {{ statusText }}
          </span>
        </div>
        <button type="button" class="close-btn" @click="close">关闭</button>
      </header>

      <div v-if="loading" class="state-line">加载中...</div>
      <div v-else-if="error" class="state-line error">{{ error }}</div>

      <section v-else-if="card" class="card-body">
        <div class="left-pane">
          <div class="cert-box">
            <h4>营业执照</h4>
            <img
              v-if="card.businessLicenseUrl"
              :src="toImageUrl(card.businessLicenseUrl)"
              alt="business-license"
              @click="openImagePreview(toImageUrl(card.businessLicenseUrl))"
            />
            <p v-else>未上传营业执照</p>
          </div>
          <div class="cert-box">
            <h4>食品健康安全证</h4>
            <img
              v-if="card.foodSafetyCertUrl"
              :src="toImageUrl(card.foodSafetyCertUrl)"
              alt="food-safety-cert"
              @click="openImagePreview(toImageUrl(card.foodSafetyCertUrl))"
            />
            <p v-else>未上传食品健康安全证</p>
          </div>
        </div>

        <div class="right-pane">
          <section class="meta-block">
            <h4>门店信息</h4>
            <p>门店名称：{{ card.storeName || '未填写' }}</p>
            <p>门店地址：{{ card.storeAddress || '未填写' }}</p>
            <p>人均消费：{{ card.averageCost ? `${card.averageCost} 元` : '未填写' }}</p>
          </section>

          <section class="meta-block">
            <h4>厨师团队</h4>
            <p>{{ card.chefTeamIntro || '暂无团队介绍' }}</p>
            <ul class="chef-list">
              <li v-for="chef in card.chefs" :key="chef.id">
                <img
                  v-if="chef.avatarUrl"
                  :src="toImageUrl(chef.avatarUrl)"
                  :alt="chef.chefName"
                  @click="openImagePreview(toImageUrl(chef.avatarUrl))"
                />
                <div>
                  <strong>{{ chef.chefName }}</strong>
                  <p>{{ chef.title || '厨师' }}</p>
                  <small>{{ chef.intro || '暂无简介' }}</small>
                </div>
              </li>
            </ul>
            <p v-if="!card.chefs.length" class="empty-row">暂无厨师信息</p>
          </section>

          <section class="meta-block">
            <h4>热门菜品</h4>
            <div class="dish-tags">
              <span v-for="dish in card.hotDishes" :key="dish">{{ dish }}</span>
              <p v-if="!card.hotDishes.length">暂无热门菜品</p>
            </div>
          </section>
        </div>
      </section>
    </article>

    <div v-if="previewImage" class="image-preview-mask" @click.stop="closeImagePreview">
      <img class="image-preview" :src="previewImage" alt="image-preview" @click.stop />
      <button type="button" class="image-preview-close" @click.stop="closeImagePreview">关闭</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { getApiHost } from '@/config/runtime'
import { computed, ref, watch } from 'vue'
import { fetchMerchantCard, type MerchantCard } from '@/api/merchant'

const API_HOST = getApiHost()

const props = defineProps<{
  visible: boolean
  merchantUserId: number | null
}>()

const emit = defineEmits<{
  (event: 'update:visible', value: boolean): void
}>()

const card = ref<MerchantCard | null>(null)
const loading = ref(false)
const error = ref('')
const previewImage = ref('')

const statusText = computed(() => {
  if (!card.value) return ''
  if (card.value.status === 1) return '官方认证商家'
  if (card.value.status === 2) return '资料审核未通过'
  return '商家资料审核中'
})

const statusClass = computed(() => {
  if (!card.value) return 'status-chip'
  if (card.value.status === 1) return 'status-chip certified'
  if (card.value.status === 2) return 'status-chip rejected'
  return 'status-chip reviewing'
})

function close() {
  emit('update:visible', false)
}

function openImagePreview(url: string) {
  previewImage.value = url
}

function closeImagePreview() {
  previewImage.value = ''
}

function toImageUrl(url: string) {
  return url.startsWith('http') ? url : `${API_HOST}${url}`
}

async function loadCard() {
  if (!props.visible || !props.merchantUserId) return
  loading.value = true
  error.value = ''
  try {
    const response = await fetchMerchantCard(props.merchantUserId)
    card.value = response.data
  } catch (err) {
    error.value = err instanceof Error ? err.message : '商家信息加载失败'
    card.value = null
  } finally {
    loading.value = false
  }
}

watch(
  () => [props.visible, props.merchantUserId],
  async () => {
    await loadCard()
  },
  { immediate: true },
)
</script>

<style scoped>
.card-mask { position: fixed; inset: 0; background: rgba(17, 18, 22, 0.48); display: grid; place-items: center; padding: 18px; z-index: 60; }
.card-shell { width: min(980px, 96vw); max-height: 92vh; overflow: auto; border-radius: 24px; background: linear-gradient(145deg, #fffaf0, #fffef8); box-shadow: 0 24px 50px rgba(20, 20, 20, 0.2); padding: 20px; }
.card-head { display: flex; justify-content: space-between; gap: 16px; align-items: start; border-bottom: 1px solid rgba(112, 86, 50, 0.12); padding-bottom: 12px; }
.card-head h3 { margin: 4px 0 6px; font-size: 26px; }
.card-head p { margin: 0; color: var(--muted); }
.status-chip { margin-top: 8px; display: inline-flex; align-items: center; gap: 8px; padding: 4px 10px; border-radius: 999px; font-size: 12px; border: 1px solid transparent; }
.status-dot { width: 8px; height: 8px; border-radius: 999px; }
.status-chip.certified { color: #2f63d8; background: rgba(68, 125, 255, 0.14); border-color: rgba(68, 125, 255, 0.28); }
.status-chip.certified .status-dot { background: #3b82f6; box-shadow: 0 0 0 4px rgba(59, 130, 246, .18); }
.status-chip.reviewing { color: #b76a14; background: rgba(240, 157, 65, 0.14); border-color: rgba(240, 157, 65, 0.28); }
.status-chip.reviewing .status-dot { background: #f59e0b; box-shadow: 0 0 0 4px rgba(245, 158, 11, .18); }
.status-chip.rejected { color: #b63a3a; background: rgba(224, 73, 73, 0.14); border-color: rgba(224, 73, 73, 0.28); }
.status-chip.rejected .status-dot { background: #ef4444; box-shadow: 0 0 0 4px rgba(239, 68, 68, .16); }
.close-btn { height: 38px; border: 1px solid rgba(112, 86, 50, 0.18); border-radius: 999px; background: #fff; padding: 0 14px; }
.state-line { padding: 20px; color: var(--muted); }
.state-line.error { color: #c3422f; }
.card-body { display: grid; grid-template-columns: 1fr 1.2fr; gap: 16px; margin-top: 14px; }
.left-pane, .right-pane { display: grid; gap: 12px; align-content: start; }
.cert-box, .meta-block { border: 1px solid rgba(112, 86, 50, 0.12); border-radius: 18px; padding: 14px; background: rgba(255, 255, 255, 0.72); }
.cert-box h4, .meta-block h4 { margin: 0 0 10px; }
.cert-box img { width: 100%; height: 210px; object-fit: cover; border-radius: 14px; }
.cert-box img, .chef-list img { cursor: zoom-in; }
.cert-box p { margin: 0; color: var(--muted); }
.meta-block p { margin: 6px 0; color: var(--muted); }
.chef-list { list-style: none; margin: 10px 0 0; padding: 0; display: grid; gap: 10px; }
.chef-list li { display: grid; grid-template-columns: 58px 1fr; gap: 10px; align-items: start; border: 1px solid rgba(112, 86, 50, 0.08); border-radius: 14px; padding: 10px; }
.chef-list img { width: 58px; height: 58px; border-radius: 12px; object-fit: cover; }
.chef-list p, .chef-list small { margin: 2px 0 0; color: var(--muted); }
.empty-row { margin-top: 8px; color: var(--muted); }
.dish-tags { display: flex; flex-wrap: wrap; gap: 8px; }
.dish-tags span { padding: 6px 10px; border-radius: 999px; background: linear-gradient(135deg, rgba(212, 177, 132, 0.22), rgba(202, 154, 102, 0.2)); color: #6e4922; font-size: 12px; }
.dish-tags p { margin: 0; color: var(--muted); }
.image-preview-mask { position: fixed; inset: 0; z-index: 80; background: rgba(12, 13, 16, 0.78); display: grid; place-items: center; padding: 20px; }
.image-preview { max-width: min(1100px, 92vw); max-height: 82vh; border-radius: 14px; object-fit: contain; box-shadow: 0 18px 42px rgba(0,0,0,.42); background: #fff; }
.image-preview-close { margin-top: 14px; height: 40px; border: 1px solid rgba(255,255,255,.35); border-radius: 999px; background: rgba(32,34,40,.7); color: #fff; padding: 0 16px; }
@media (max-width: 900px) {
  .card-body { grid-template-columns: 1fr; }
  .card-shell { padding: 16px; }
}
</style>


