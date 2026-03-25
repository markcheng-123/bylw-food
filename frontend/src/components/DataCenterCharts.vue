<template>
  <section class="chart-grid">
    <article class="chart-card">
      <div class="card-head">
        <div>
          <p class="card-tag">Trend</p>
          <h3>{{ t('trend') }}</h3>
        </div>
      </div>
      <div ref="trendChartRef" class="chart-canvas"></div>
    </article>

    <article class="chart-card">
      <div class="card-head">
        <div>
          <p class="card-tag">Category</p>
          <h3>{{ t('categoryShare') }}</h3>
        </div>
      </div>
      <div ref="categoryChartRef" class="chart-canvas"></div>
    </article>

    <article class="chart-card full">
      <div class="card-head">
        <div>
          <p class="card-tag">Merchant</p>
          <h3>{{ t('merchantCities') }}</h3>
        </div>
      </div>
      <div ref="merchantChartRef" class="chart-canvas"></div>
    </article>
  </section>
</template>

<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import * as echarts from 'echarts'
import type { AdminDataCenterPayload } from '@/api/adminInsights'
import { useAdminUi } from '@/stores/adminUi'

const props = defineProps<{ data: AdminDataCenterPayload | null }>()
const { adminUiState, t } = useAdminUi()

const trendChartRef = ref<HTMLDivElement | null>(null)
const categoryChartRef = ref<HTMLDivElement | null>(null)
const merchantChartRef = ref<HTMLDivElement | null>(null)

let trendChart: echarts.ECharts | null = null
let categoryChart: echarts.ECharts | null = null
let merchantChart: echarts.ECharts | null = null

function readCssVar(name: string, fallback: string) {
  const host = trendChartRef.value?.closest('.admin-shell') as HTMLElement | null
  const style = getComputedStyle(host || document.documentElement)
  const value = style.getPropertyValue(name).trim()
  return value || fallback
}

function initCharts() {
  if (trendChartRef.value && !trendChart) trendChart = echarts.init(trendChartRef.value)
  if (categoryChartRef.value && !categoryChart) categoryChart = echarts.init(categoryChartRef.value)
  if (merchantChartRef.value && !merchantChart) merchantChart = echarts.init(merchantChartRef.value)
}

function renderCharts() {
  if (!props.data) return
  initCharts()

  const chartText = readCssVar('--admin-chart-text', adminUiState.theme === 'light' ? '#475569' : '#d7e3f7')
  const chartLine = readCssVar('--admin-chart-line', adminUiState.theme === 'light' ? 'rgba(148,163,184,0.24)' : 'rgba(148,163,184,0.18)')
  const tooltipBg = readCssVar('--admin-chart-tooltip-bg', adminUiState.theme === 'light' ? 'rgba(255,255,255,0.96)' : 'rgba(19,26,37,0.92)')
  const tooltipBorder = readCssVar('--admin-chart-tooltip-border', adminUiState.theme === 'light' ? 'rgba(148,163,184,0.35)' : 'rgba(148,163,184,0.22)')
  const ringBorder = readCssVar('--admin-chart-ring-border', adminUiState.theme === 'light' ? '#eef3f9' : '#1a2433')
  const centerValueColor = readCssVar('--admin-chart-center-value', adminUiState.theme === 'light' ? '#1f2937' : '#edf4ff')
  const centerLabelColor = readCssVar('--admin-chart-center-label', adminUiState.theme === 'light' ? '#64748b' : '#9fb2d0')

  const lineBlue = new echarts.graphic.LinearGradient(0, 0, 1, 0, [
    { offset: 0, color: readCssVar('--admin-chart-line-blue-start', '#3b82f6') },
    { offset: 1, color: readCssVar('--admin-chart-line-blue-end', '#22d3ee') },
  ])
  const lineCyan = new echarts.graphic.LinearGradient(0, 0, 1, 0, [
    { offset: 0, color: readCssVar('--admin-chart-line-cyan-start', '#06b6d4') },
    { offset: 1, color: readCssVar('--admin-chart-line-cyan-end', '#2dd4bf') },
  ])

  const totalCategory = props.data.categoryShare.reduce((sum, item) => sum + item.value, 0)

  trendChart?.setOption({
    backgroundColor: 'transparent',
    animationDuration: 500,
    tooltip: {
      trigger: 'axis',
      backgroundColor: tooltipBg,
      borderColor: tooltipBorder,
      textStyle: { color: chartText },
    },
    legend: { top: 0, textStyle: { color: chartText } },
    grid: { left: 40, right: 20, top: 48, bottom: 30 },
    xAxis: {
      type: 'category',
      data: props.data.activeTrend.map((item) => item.date),
      axisLine: { lineStyle: { color: chartLine } },
      axisLabel: { color: chartText },
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: chartLine } },
      axisLabel: { color: chartText },
    },
    series: [
      {
        name: t('newUsers'),
        type: 'line',
        smooth: true,
        showSymbol: false,
        symbol: 'none',
        symbolSize: 8,
        data: props.data.activeTrend.map((item) => item.newUsers),
        lineStyle: { color: lineBlue, width: 3 },
        itemStyle: { color: readCssVar('--admin-chart-line-blue-end', '#22d3ee') },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(59,130,246,0.22)' },
            { offset: 1, color: 'rgba(59,130,246,0.04)' },
          ]),
        },
      },
      {
        name: t('newPosts'),
        type: 'line',
        smooth: true,
        showSymbol: false,
        symbol: 'none',
        symbolSize: 8,
        data: props.data.activeTrend.map((item) => item.newPosts),
        lineStyle: { color: lineCyan, width: 3 },
        itemStyle: { color: readCssVar('--admin-chart-line-cyan-end', '#2dd4bf') },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(45,212,191,0.20)' },
            { offset: 1, color: 'rgba(45,212,191,0.03)' },
          ]),
        },
      },
    ],
  })

  categoryChart?.setOption({
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'item',
      backgroundColor: tooltipBg,
      borderColor: tooltipBorder,
      textStyle: { color: chartText },
    },
    legend: { bottom: 0, textStyle: { color: chartText } },
    graphic: [
      {
        type: 'text',
        left: 'center',
        top: '43%',
        style: {
          text: `${totalCategory}`,
          fill: centerValueColor,
          fontSize: 26,
          fontWeight: 700,
        },
      },
      {
        type: 'text',
        left: 'center',
        top: '55%',
        style: {
          text: 'Total',
          fill: centerLabelColor,
          fontSize: 12,
        },
      },
    ],
    series: [
      {
        name: t('categoryShare'),
        type: 'pie',
        radius: ['50%', '78%'],
        center: ['50%', '45%'],
        itemStyle: {
          borderColor: ringBorder,
          borderWidth: 3,
        },
        label: { color: chartText, formatter: '{b}  {d}%' },
        color: [
          readCssVar('--admin-pie-1', '#60a5fa'),
          readCssVar('--admin-pie-2', '#22d3ee'),
          readCssVar('--admin-pie-3', '#a78bfa'),
          readCssVar('--admin-pie-4', '#34d399'),
          readCssVar('--admin-pie-5', '#f59e0b'),
          readCssVar('--admin-pie-6', '#818cf8'),
        ],
        data: props.data.categoryShare,
      },
    ],
  })

  merchantChart?.setOption({
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      backgroundColor: tooltipBg,
      borderColor: tooltipBorder,
      textStyle: { color: chartText },
    },
    grid: { left: 40, right: 20, top: 24, bottom: 50 },
    xAxis: {
      type: 'category',
      data: props.data.merchantCities.map((item) => item.city),
      axisLine: { lineStyle: { color: chartLine } },
      axisLabel: { color: chartText },
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: chartLine } },
      axisLabel: { color: chartText },
    },
    series: [
      {
        name: t('merchantCities'),
        type: 'bar',
        barWidth: 28,
        itemStyle: {
          borderRadius: [8, 8, 0, 0],
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: readCssVar('--admin-bar-start', '#60a5fa') },
            { offset: 1, color: readCssVar('--admin-bar-end', '#3b82f6') },
          ]),
        },
        data: props.data.merchantCities.map((item) => item.merchantCount),
      },
    ],
  })
}

function handleResize() {
  trendChart?.resize()
  categoryChart?.resize()
  merchantChart?.resize()
}

watch([() => props.data, () => adminUiState.locale, () => adminUiState.theme], async () => {
  await nextTick()
  renderCharts()
}, { deep: true })

onMounted(() => {
  initCharts()
  renderCharts()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  categoryChart?.dispose()
  merchantChart?.dispose()
})
</script>

<style scoped>
.chart-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.chart-card {
  padding: 20px;
  border-radius: 22px;
  border: 1px solid var(--el-border-color-light, var(--admin-line));
  background: var(--el-bg-color-overlay, var(--admin-surface));
  box-shadow: var(--admin-shadow);
}

.chart-card.full {
  grid-column: 1 / -1;
}

.card-head {
  margin-bottom: 12px;
}

.card-tag {
  margin: 0 0 10px;
  font-size: 12px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: var(--el-color-primary, var(--admin-accent));
}

.card-head h3 {
  margin: 0;
  color: var(--el-text-color-primary, var(--admin-text));
}

.chart-canvas {
  width: 100%;
  height: 320px;
}

@media (max-width: 1100px) {
  .chart-grid {
    grid-template-columns: 1fr;
  }

  .chart-card.full {
    grid-column: auto;
  }
}
</style>
