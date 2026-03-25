<template>
  <section class="map-screen">
    <article class="map-hero">
      <div>
        <p class="section-tag">Map Monitor</p>
        <h2>{{ t('mapHeroTitle') }}</h2>
        <p>{{ t('mapInteractive') }}</p>
      </div>
      <div class="hero-badges">
        <span class="hero-chip">{{ t('highlightedProvinces') }} {{ highlightedCount }}</span>
        <span class="hero-chip warn">{{ t('settledMerchants') }} {{ merchantTotal }}</span>
      </div>
    </article>

    <p v-if="loadError" class="load-error">{{ loadError }}</p>
    <ChinaMerchantMap :data="mapData" />
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { fetchAdminMap, type AdminMapPayload } from '@/api/adminInsights'
import ChinaMerchantMap from '@/components/ChinaMerchantMap.vue'
import { useAdminUi } from '@/stores/adminUi'

const { t } = useAdminUi()
const mapData = ref<AdminMapPayload | null>(null)
const loadError = ref('')

const highlightedCount = computed(() => mapData.value?.highlightedProvinces.length || 0)
const merchantTotal = computed(() =>
  (mapData.value?.provinceStats || []).reduce((sum, item) => sum + item.merchantCount, 0),
)

async function loadData() {
  try {
    const response = await fetchAdminMap()
    mapData.value = response.data
    loadError.value = ''
  } catch (error) {
    mapData.value = { highlightedProvinces: [], provinceStats: [] }
    loadError.value = error instanceof Error ? error.message : '地图数据加载失败'
  }
}

onMounted(loadData)
</script>

<style scoped>
.map-screen {
  display: grid;
  gap: 18px;
}

.map-hero {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  padding: 22px;
  border-radius: 24px;
  border: 1px solid var(--el-border-color-light, var(--admin-line));
  background: var(--el-bg-color-overlay, var(--admin-surface));
  box-shadow: var(--admin-shadow);
}

.section-tag {
  margin: 0 0 10px;
  font-size: 12px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: var(--admin-accent);
}

.map-hero h2 {
  margin: 0;
  color: var(--admin-text);
}

.map-hero p:last-child {
  color: var(--admin-muted);
}

.hero-badges {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  align-items: flex-start;
}

.hero-chip {
  padding: 10px 14px;
  border-radius: 999px;
  background: color-mix(in srgb, var(--el-color-primary, #409eff) 16%, transparent);
  color: var(--el-color-primary, #409eff);
}

.hero-chip.warn {
  background: color-mix(in srgb, var(--el-color-warning, #e6a23c) 16%, transparent);
  color: var(--el-color-warning, #e6a23c);
}

.load-error {
  margin: 0;
  color: var(--el-color-danger, #f56c6c);
  font-size: 13px;
}

@media (max-width: 900px) {
  .map-hero {
    display: grid;
  }
}
</style>
