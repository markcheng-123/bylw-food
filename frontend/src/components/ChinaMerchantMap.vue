<template>
  <section class="map-layout">
    <article class="map-panel">
      <div class="panel-head">
        <div>
          <p class="panel-tag">China Map</p>
          <h3>{{ t('chinaMap') }}</h3>
        </div>
      </div>
      <div ref="chartRef" class="map-canvas"></div>
    </article>

    <article class="rank-card">
      <div class="rank-head">
        <p class="panel-tag">Top 5</p>
        <h3>{{ t('highlightedProvinces') }} TOP 5</h3>
      </div>

      <div class="rank-list">
        <div v-for="(item, index) in topProvinces" :key="item.province" class="rank-item">
          <div class="rank-row">
            <span class="rank-index">{{ index + 1 }}</span>
            <span class="rank-name">{{ item.province }}</span>
            <span class="rank-value">{{ item.merchantCount }}</span>
          </div>
          <el-progress
            :percentage="calcPercent(item.merchantCount)"
            :show-text="false"
            :stroke-width="8"
            :color="progressColor"
            class="rank-progress"
          />
        </div>
      </div>
    </article>
  </section>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import * as echarts from 'echarts'
import { ElProgress } from 'element-plus'
import 'element-plus/es/components/progress/style/css'
import type { AdminMapPayload } from '@/api/adminInsights'
import { useAdminUi } from '@/stores/adminUi'
import chinaMapGeoJson from '@/assets/china-map.json'

const props = defineProps<{ data: AdminMapPayload | null }>()
const { adminUiState, t } = useAdminUi()

const chartRef = ref<HTMLDivElement | null>(null)
let chart: echarts.ECharts | null = null

const topProvinces = computed(() => (props.data?.provinceStats || []).slice(0, 5))
const maxValue = computed(() => Math.max(1, ...topProvinces.value.map((item) => item.merchantCount)))
const progressColor = computed(() => readCssVar('--el-color-primary', '#409eff'))

function readCssVar(name: string, fallback: string) {
  const host = chartRef.value?.closest('.admin-shell') as HTMLElement | null
  const style = getComputedStyle(host || document.documentElement)
  const value = style.getPropertyValue(name).trim()
  return value || fallback
}

function calcPercent(value: number) {
  return Math.round((value / maxValue.value) * 100)
}

async function ensureMapRegistered() {
  if (echarts.getMap('china')) return
  echarts.registerMap('china', chinaMapGeoJson as never)
}

function buildSeriesData() {
  const valueMap = new Map((props.data?.provinceStats || []).map((item) => [item.province, item.merchantCount]))
  return (props.data?.highlightedProvinces || []).map((province) => ({
    name: province,
    value: valueMap.get(province) || 0,
  }))
}

function renderChart() {
  if (!chart) return
  const safeData = props.data || { highlightedProvinces: [], provinceStats: [] }

  const textColor = readCssVar('--admin-map-label', adminUiState.theme === 'light' ? '#475569' : '#d6e2f7')
  const areaColor = readCssVar('--admin-map-area', adminUiState.theme === 'light' ? '#dbe7f4' : '#273246')
  const borderColor = readCssVar('--admin-map-border', adminUiState.theme === 'light' ? '#a5bfd9' : '#6f8db4')
  const emphasisArea = readCssVar('--admin-map-emphasis-area', adminUiState.theme === 'light' ? '#9ec4ef' : '#4e7fb9')
  const emphasisBorder = readCssVar('--admin-map-emphasis-border', adminUiState.theme === 'light' ? '#d9ebff' : '#9cc1ef')
  const tooltipBg = readCssVar('--admin-chart-tooltip-bg', adminUiState.theme === 'light' ? 'rgba(255,255,255,0.96)' : 'rgba(19,26,37,0.92)')
  const tooltipBorder = readCssVar('--admin-chart-tooltip-border', adminUiState.theme === 'light' ? 'rgba(148,163,184,0.35)' : 'rgba(148,163,184,0.22)')

  chart.setOption({
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'item',
      backgroundColor: tooltipBg,
      borderColor: tooltipBorder,
      textStyle: { color: textColor },
      formatter: (params: { name: string }) => {
        const province = safeData.provinceStats.find((item) => item.province === params.name)
        return `${params.name}<br/>${t('settledMerchants')}: ${province?.merchantCount || 0}`
      },
    },
    visualMap: {
      min: 0,
      max: Math.max(...safeData.provinceStats.map((item) => item.merchantCount), 10),
      show: false,
      inRange: { color: [readCssVar('--admin-map-visual-1', '#5b7fae'), readCssVar('--admin-map-visual-2', '#4f9ad4')] },
    },
    series: [
      {
        type: 'map',
        map: 'china',
        roam: false,
        layoutCenter: ['50%', '52%'],
        layoutSize: '96%',
        data: safeData.highlightedProvinces.length > 0 ? buildSeriesData() : [],
        label: { show: false, color: textColor },
        itemStyle: {
          areaColor,
          borderColor,
          borderWidth: 1,
        },
        emphasis: {
          label: { show: true, color: '#ffffff' },
          itemStyle: {
            areaColor: emphasisArea,
            borderColor: emphasisBorder,
            borderWidth: 1.2,
          },
          scale: false,
        },
      },
    ],
  })
}

function handleResize() {
  chart?.resize()
}

watch([() => props.data, () => adminUiState.theme, () => adminUiState.locale], async () => {
  await nextTick()
  renderChart()
}, { deep: true })

onMounted(async () => {
  if (chartRef.value) {
    chart = echarts.init(chartRef.value)
  }
  try {
    await ensureMapRegistered()
    renderChart()
  } catch (error) {
    console.error('Failed to initialize China map', error)
  }
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  chart?.dispose()
})
</script>

<style scoped>
.map-layout {
  display: grid;
  grid-template-columns: minmax(0, 1.45fr) minmax(280px, 0.7fr);
  gap: 16px;
}

.map-panel,
.rank-card {
  border-radius: 24px;
  border: 1px solid var(--el-border-color-light, var(--admin-line));
  background: var(--el-bg-color-overlay, var(--admin-surface));
  box-shadow: var(--admin-shadow);
}

.map-panel,
.rank-card {
  padding: 20px;
}

.panel-head,
.rank-head {
  margin-bottom: 12px;
}

.panel-tag {
  margin: 0 0 10px;
  font-size: 12px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: var(--el-color-primary, var(--admin-accent));
}

.panel-head h3,
.rank-head h3 {
  margin: 0;
  color: var(--el-text-color-primary, var(--admin-text));
}

.map-canvas {
  width: 100%;
  height: 560px;
}

.rank-card {
  display: grid;
  grid-template-rows: auto 1fr;
}

.rank-list {
  display: grid;
  gap: 14px;
  align-content: start;
}

.rank-item {
  border-radius: 12px;
  border: 1px solid var(--el-border-color-lighter, rgba(140, 160, 188, 0.2));
  background: var(--el-fill-color-light, rgba(255, 255, 255, 0.03));
  padding: 12px;
}

.rank-row {
  display: grid;
  grid-template-columns: 22px 1fr auto;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.rank-index {
  color: var(--el-color-primary, #5ea3ff);
  font-weight: 700;
}

.rank-name {
  color: var(--el-text-color-primary, var(--admin-text));
  font-weight: 600;
}

.rank-value {
  color: var(--el-text-color-secondary, var(--admin-muted));
  font-variant-numeric: tabular-nums;
}

:deep(.rank-progress .el-progress-bar__outer) {
  background: var(--el-fill-color-dark, rgba(127, 148, 177, 0.22));
}

@media (max-width: 1100px) {
  .map-layout {
    grid-template-columns: 1fr;
  }

  .map-canvas {
    height: 480px;
  }
}
</style>
