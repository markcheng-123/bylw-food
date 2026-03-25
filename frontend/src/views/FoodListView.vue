<template>
  <section class="list-screen">
    <article class="list-hero">
      <div>
        <p class="eyebrow">美食列表</p>
        <h2>按分类和关键词找到你想吃的那一口</h2>
      </div>
      <RouterLink class="primary-btn hero-link" to="/publish">发布美食</RouterLink>
    </article>

    <section class="filter-panel">
      <label class="field search-field">
        <input
          v-model.trim="keyword"
          type="text"
          placeholder="搜索标题/摘要/商家"
          @keyup.enter="applyFilters"
        />
      </label>

      <label class="field">
        <select v-model="categoryId" @change="applyFilters">
          <option value="">全部分类</option>
          <option v-for="item in categories" :key="item.id" :value="String(item.id)">
            {{ item.icon ? `${item.icon} ` : '' }}{{ item.name }}
          </option>
        </select>
      </label>

      <label class="field">
        <ElCascader
          v-model="regionCode"
          class="region-cascader"
          :options="regionOptions"
          :props="regionProps"
          clearable
          placeholder="选择省/市/区"
          @change="applyFilters"
        />
      </label>

      <button class="primary-btn action-btn" type="button" @click="applyFilters">查询</button>

      <button class="ghost-btn action-btn reset-btn" type="button" @click="resetFilters">重置</button>
    </section>

    <div v-if="orderedRecords.length" class="list-grid">
      <FoodCard v-for="item in orderedRecords" :key="item.id" :item="item" :is-hot="hotTopIdSet.has(item.id)" />
    </div>

    <div v-else class="empty-panel">
      <strong>没有找到符合条件的内容</strong>
      <p>可以换个关键词，或者切换分类再试试。</p>
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
import { ElCascader } from 'element-plus'
import { fetchCategoryOptions, type CategoryItem } from '@/api/category'
import { fetchFoodPostList, type PageResult, type FoodPostCard } from '@/api/post'
import FoodCard from '@/components/FoodCard.vue'
import 'element-plus/es/components/cascader/style/css'
import 'element-plus/es/components/input/style/css'
import 'element-plus/es/components/scrollbar/style/css'
import 'element-plus/es/components/popper/style/css'

interface RegionNode extends Record<string, unknown> {
  value: string
  label: string
  children?: RegionNode[]
}

const router = useRouter()
const route = useRoute()

const categories = ref<CategoryItem[]>([])
const keyword = ref('')
const categoryId = ref('')
const regionCode = ref<string[]>([])
const current = ref(1)
const pageSize = 12
const listData = ref<PageResult<FoodPostCard>>({
  total: 0,
  records: [],
})
const regionProps = { value: 'value', label: 'label', children: 'children' }
const regionOptions: RegionNode[] = [
  {
    value: 'zj',
    label: '浙江省',
    children: [
      {
        value: 'hz',
        label: '杭州市',
        children: [
          { value: 'xh', label: '西湖区' },
          { value: 'bj', label: '滨江区' },
          { value: 'xc', label: '上城区' },
        ],
      },
      {
        value: 'nb',
        label: '宁波市',
        children: [
          { value: 'yh', label: '鄞州区' },
          { value: 'jb', label: '江北区' },
        ],
      },
    ],
  },
  {
    value: 'js',
    label: '江苏省',
    children: [
      {
        value: 'nj',
        label: '南京市',
        children: [
          { value: 'gl', label: '鼓楼区' },
          { value: 'xw', label: '玄武区' },
        ],
      },
      {
        value: 'sz',
        label: '苏州市',
        children: [
          { value: 'gy', label: '姑苏区' },
          { value: 'wy', label: '吴中区' },
        ],
      },
    ],
  },
  {
    value: 'gd',
    label: '广东省',
    children: [
      {
        value: 'gz',
        label: '广州市',
        children: [
          { value: 'th', label: '天河区' },
          { value: 'yx', label: '越秀区' },
        ],
      },
      {
        value: 'sz',
        label: '深圳市',
        children: [
          { value: 'ns', label: '南山区' },
          { value: 'ft', label: '福田区' },
        ],
      },
    ],
  },
]

const totalPages = computed(() => Math.max(1, Math.ceil(listData.value.total / pageSize)))
const selectedRegionLabels = computed(() => mapRegionLabels(regionCode.value, regionOptions))
const regionFilterKeyword = computed(() => selectedRegionLabels.value[selectedRegionLabels.value.length - 1] || '')
const regionFilteredRecords = computed(() =>
  [...listData.value.records].filter((item) => {
    const keywordText = regionFilterKeyword.value.trim()
    if (!keywordText) return true
    return (item.address || '').includes(keywordText)
  }),
)

const hotTopIds = computed(() =>
  [...regionFilteredRecords.value]
    .sort((a, b) => {
      const heatA = a.heatScore ?? (a.likeCount + a.commentCount + a.viewCount)
      const heatB = b.heatScore ?? (b.likeCount + b.commentCount + b.viewCount)
      if (heatB !== heatA) return heatB - heatA
      return b.id - a.id
    })
    .slice(0, 4)
    .map((item) => item.id),
)

const hotTopIdSet = computed(() => new Set(hotTopIds.value))

const orderedRecords = computed(() =>
  [...regionFilteredRecords.value].sort((a, b) => {
    const hotA = Number(hotTopIdSet.value.has(a.id))
    const hotB = Number(hotTopIdSet.value.has(b.id))
    if (hotA !== hotB) return hotB - hotA
    const heatA = a.heatScore ?? (a.likeCount + a.commentCount + a.viewCount)
    const heatB = b.heatScore ?? (b.likeCount + b.commentCount + b.viewCount)
    if (heatB !== heatA) return heatB - heatA
    return b.id - a.id
  }),
)

async function loadCategories() {
  const response = await fetchCategoryOptions()
  categories.value = response.data
}

async function loadPosts() {
  const response = await fetchFoodPostList({
    current: current.value,
    size: pageSize,
    keyword: keyword.value || undefined,
    categoryId: categoryId.value ? Number(categoryId.value) : undefined,
    regionCode: regionCode.value.length ? regionCode.value.join('/') : undefined,
  })
  listData.value = response.data
}

function syncFromRoute() {
  keyword.value = typeof route.query.keyword === 'string' ? route.query.keyword : ''
  categoryId.value = typeof route.query.categoryId === 'string' ? route.query.categoryId : ''
  regionCode.value = typeof route.query.regionCode === 'string' && route.query.regionCode
    ? route.query.regionCode.split('/').filter(Boolean)
    : []
  current.value = typeof route.query.current === 'string' ? Number(route.query.current) || 1 : 1
}

function applyFilters() {
  router.push({
    path: '/food',
    query: {
      keyword: keyword.value || undefined,
      categoryId: categoryId.value || undefined,
      regionCode: regionCode.value.length ? regionCode.value.join('/') : undefined,
      current: 1,
    },
  })
}

function resetFilters() {
  keyword.value = ''
  categoryId.value = ''
  regionCode.value = []
  router.push('/food')
}

function changePage(page: number) {
  router.push({
    path: '/food',
    query: {
      keyword: keyword.value || undefined,
      categoryId: categoryId.value || undefined,
      regionCode: regionCode.value.length ? regionCode.value.join('/') : undefined,
      current: page,
    },
  })
}

function mapRegionLabels(codes: string[], nodes: RegionNode[]): string[] {
  if (!codes.length) return []
  const labels: string[] = []
  let currentNodes = nodes
  for (const code of codes) {
    const matched = currentNodes.find((item) => item.value === code)
    if (!matched) break
    labels.push(matched.label)
    currentNodes = matched.children || []
  }
  return labels
}

watch(
  () => route.fullPath,
  async () => {
    syncFromRoute()
    await loadPosts()
  },
)

onMounted(async () => {
  syncFromRoute()
  await loadCategories()
  await loadPosts()
})
</script>

<style scoped>
.list-screen {
  display: grid;
  gap: 14px;
}

.list-hero,
.filter-panel,
.empty-panel {
  padding: 20px;
  border: 1px solid var(--line);
  border-radius: var(--radius-xl);
  background: rgba(255, 252, 246, 0.92);
  box-shadow: var(--shadow);
}

.list-hero {
  display: flex;
  justify-content: space-between;
  gap: 14px;
  align-items: end;
  background:
    radial-gradient(circle at top left, rgba(189, 91, 47, 0.16), transparent 36%),
    linear-gradient(135deg, rgba(255, 250, 242, 0.96), rgba(239, 226, 207, 0.92));
}

.list-hero h2,
.list-card-body h3 {
  margin: 0;
}

.list-hero h2 {
  font-size: clamp(24px, 3.4vw, 34px);
  line-height: 1.2;
  max-width: 14ch;
}

.hero-link {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 140px;
}

.filter-panel {
  display: grid;
  grid-template-columns: 1.2fr 0.85fr 1fr auto auto;
  gap: 12px;
  align-items: center;
}

.field {
  min-width: 0;
}

.field input,
.field select {
  width: 100%;
  height: 44px;
  border: 1px solid rgba(180, 169, 154, 0.36);
  border-radius: 22px;
  padding: 0 14px;
  background: #fffefc;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.03);
  outline: none;
  color: #5f4f41;
}

.field input:focus,
.field select:focus {
  border-color: rgba(140, 52, 23, 0.28);
}

.action-btn {
  height: 44px;
  border-radius: 22px;
  min-width: 88px;
}

.reset-btn {
  background: #fffefc;
  color: var(--muted);
  border: 1px solid rgba(180, 169, 154, 0.36);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.03);
}

.region-cascader {
  width: 100%;
}

.region-cascader :deep(.el-input__wrapper) {
  min-height: 44px;
  border-radius: 22px;
  padding-inline: 14px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.03) !important;
  background: #fffefc;
  border: 1px solid rgba(180, 169, 154, 0.36);
}

.region-cascader :deep(.el-input__wrapper.is-focus) {
  border-color: rgba(140, 52, 23, 0.28);
}

.region-cascader :deep(.el-input__inner) {
  color: #5f4f41;
}

.list-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.empty-panel p { color: var(--muted); }

.empty-panel {
  text-align: center;
}

.pager {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
}

.pager-btn {
  min-width: 98px;
}

@media (min-width: 1024px) {
  .list-grid {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }
}

@media (max-width: 1023px) {
  .list-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 900px) {
  .filter-panel {
    grid-template-columns: 1fr;
  }

  .list-hero,
  .list-grid,
  .pager {
    grid-template-columns: 1fr;
    display: grid;
  }

  .list-hero,
  .empty-panel {
    padding: 22px;
  }
}
</style>
