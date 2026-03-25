<template>
  <section class="dashboard-screen">
    <section class="stats-grid">
      <article class="stat-card">
        <span>{{ t('contentTotal') }}</span>
        <strong>{{ posts.total }}</strong>
        <small>{{ t('dashboardDesc') }}</small>
      </article>
      <article class="stat-card warn">
        <span>{{ t('pendingPosts') }}</span>
        <strong>{{ pendingPostCount }}</strong>
        <small>{{ t('needReview') }}</small>
      </article>
      <article class="stat-card">
        <span>{{ t('merchantApplications') }}</span>
        <strong>{{ merchantApplications.length }}</strong>
        <small>{{ t('historicalApplications') }}</small>
      </article>
      <article class="stat-card warn">
        <span>{{ t('pendingMerchants') }}</span>
        <strong>{{ pendingMerchantCount }}</strong>
        <small>{{ t('needMerchantReview') }}</small>
      </article>
    </section>

    <DataCenterCharts :data="dataCenter" />
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { fetchAdminPosts, fetchMerchantApplicationsForAdmin } from '@/api/admin'
import { fetchAdminDataCenter, type AdminDataCenterPayload } from '@/api/adminInsights'
import DataCenterCharts from '@/components/DataCenterCharts.vue'
import { useAdminUi } from '@/stores/adminUi'
import type { AdminPostItem, MerchantApplicationItem } from '@/api/admin'

const { t } = useAdminUi()

const posts = reactive<{
  total: number
  records: AdminPostItem[]
}>({
  total: 0,
  records: [],
})

const merchantApplications = ref<MerchantApplicationItem[]>([])
const dataCenter = ref<AdminDataCenterPayload | null>(null)

const pendingPostCount = computed(() => posts.records.filter((item) => item.status === 0).length)
const pendingMerchantCount = computed(() => merchantApplications.value.filter((item) => item.status === 0).length)

async function loadData() {
  const [postResponse, merchantResponse, centerResponse] = await Promise.all([
    fetchAdminPosts({ current: 1, size: 30 }),
    fetchMerchantApplicationsForAdmin({}),
    fetchAdminDataCenter(),
  ])
  posts.total = postResponse.data.total
  posts.records = postResponse.data.records
  merchantApplications.value = merchantResponse.data
  dataCenter.value = centerResponse.data
}

onMounted(loadData)
</script>

<style scoped>
.dashboard-screen {
  display: grid;
  gap: 18px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.stat-card {
  padding: 20px;
  border-radius: 22px;
  border: 1px solid var(--admin-line);
  background: var(--admin-surface);
  box-shadow: var(--admin-shadow);
}

.stat-card span,
.stat-card small {
  color: var(--admin-muted);
}

.stat-card strong {
  display: block;
  margin: 10px 0 6px;
  font-size: 34px;
  color: var(--admin-text);
}

.stat-card.warn strong {
  color: #d9a948;
}

@media (max-width: 1100px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>
