<template>
  <section class="review-shell">
    <aside class="review-nav">
      <button type="button" :class="tab === 'post' ? 'nav-item active' : 'nav-item'" @click="setTab('post')">
        内容审核
        <span v-if="pendingPostCount > 0" class="nav-badge">{{ pendingPostCount }}</span>
      </button>
      <button type="button" :class="tab === 'merchant' ? 'nav-item active' : 'nav-item'" @click="setTab('merchant')">
        商家入驻审核
        <span v-if="pendingMerchantCount > 0" class="nav-badge">{{ pendingMerchantCount }}</span>
      </button>
      <button type="button" :class="tab === 'qualification' ? 'nav-item active' : 'nav-item'" @click="setTab('qualification')">
        商家资质审核
        <span v-if="pendingQualificationCount > 0" class="nav-badge">{{ pendingQualificationCount }}</span>
      </button>
      <button type="button" :class="tab === 'category' ? 'nav-item active' : 'nav-item'" @click="setTab('category')">
        分类管理
      </button>
    </aside>

    <section class="table-zone">
      <header class="tool-bar">
        <input
          v-model.trim="keyword"
          class="search-input"
          type="text"
          :placeholder="tab === 'post' ? '搜索标题/摘要/地址' : tab === 'merchant' ? '搜索商家/联系人/地址' : tab === 'qualification' ? '搜索商家/门店/地址' : '搜索分类名称/介绍'"
        />
        <select v-if="tab !== 'category'" v-model="statusFilter" class="status-select">
          <option value="">全部状态</option>
          <option value="0">待审核</option>
          <option value="1">已通过</option>
          <option :value="tab === 'post' ? '3' : '2'">已驳回</option>
        </select>
        <button type="button" class="refresh-btn" @click="refreshCurrentTab">刷新</button>
      </header>

      <div v-if="tab === 'category'" class="category-create">
        <input v-model.trim="categoryForm.name" type="text" placeholder="分类名称" />
        <input v-model.trim="categoryForm.description" type="text" placeholder="分类介绍" />
        <select v-model="categoryForm.icon">
          <option v-for="icon in categoryIconOptions" :key="icon" :value="icon">{{ icon }}</option>
        </select>
        <button type="button" class="create-btn" @click="handleSaveCategory">新增分类</button>
      </div>

      <div class="table-wrap">
        <table v-if="tab === 'post'" class="data-table">
          <thead>
            <tr>
              <th class="col-thumb">缩略图</th>
              <th>标题 / 信息</th>
              <th>摘要</th>
              <th class="col-time">时间</th>
              <th class="col-status">状态</th>
              <th class="col-action">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in filteredPosts" :key="item.id">
              <td class="col-thumb">
                <img v-if="item.coverImage" class="thumb" :src="resolveImage(item.coverImage)" :alt="item.title" />
                <div v-else class="thumb empty">-</div>
              </td>
              <td>
                <div class="main-title">{{ item.title }}</div>
                <div class="sub-line">{{ item.address || '-' }} · {{ item.categoryName }} · {{ item.authorNickname }}</div>
              </td>
              <td><span class="single-line">{{ item.summary || item.content || '-' }}</span></td>
              <td class="col-time">{{ formatDate(item.createdAt) }}</td>
              <td class="col-status">
                <span :class="statusTagClass(item.status, 'post')">{{ postStatusText(item.status) }}</span>
              </td>
              <td class="col-action">
                <button type="button" class="text-btn pass" @click="approvePost(item.id)" :disabled="item.status !== 0">通过</button>
                <button type="button" class="text-btn reject" @click="rejectPost(item.id)" :disabled="item.status !== 0">驳回</button>
                <button type="button" class="text-btn detail" @click="viewPostDetail(item.id)">详情</button>
              </td>
            </tr>
          </tbody>
        </table>

        <table v-else-if="tab === 'merchant'" class="data-table">
          <thead>
            <tr>
              <th class="col-thumb">缩略图</th>
              <th>标题 / 信息</th>
              <th>摘要</th>
              <th class="col-time">时间</th>
              <th class="col-status">状态</th>
              <th class="col-action">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in filteredMerchantApplications" :key="item.id">
              <td class="col-thumb">
                <img v-if="item.businessLicense" class="thumb" :src="resolveImage(item.businessLicense)" :alt="item.merchantName" />
                <div v-else class="thumb empty">-</div>
              </td>
              <td>
                <div class="main-title">{{ item.merchantName }}</div>
                <div class="sub-line">{{ item.contactName }} · {{ item.contactPhone }} · {{ item.address }}</div>
              </td>
              <td><span class="single-line">{{ item.description || '-' }}</span></td>
              <td class="col-time">{{ formatDate(item.submittedAt || item.createdAt) }}</td>
              <td class="col-status">
                <span :class="statusTagClass(item.status, 'merchant')">{{ merchantStatusText(item.status) }}</span>
              </td>
              <td class="col-action">
                <button type="button" class="text-btn pass" @click="approveMerchant(item.id)" :disabled="item.status !== 0">通过</button>
                <button type="button" class="text-btn reject" @click="rejectMerchant(item.id)" :disabled="item.status !== 0">驳回</button>
                <button type="button" class="text-btn detail" @click="viewMerchantDetail(item)">详情</button>
              </td>
            </tr>
          </tbody>
        </table>

        <table v-else-if="tab === 'qualification'" class="data-table">
          <thead>
            <tr>
              <th class="col-thumb">缩略图</th>
              <th>标题 / 信息</th>
              <th>摘要</th>
              <th class="col-time">时间</th>
              <th class="col-status">状态</th>
              <th class="col-action">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in filteredMerchantProfiles" :key="item.id">
              <td class="col-thumb">
                <img
                  v-if="item.businessLicenseUrl || item.foodSafetyCertUrl"
                  class="thumb"
                  :src="resolveImage(item.businessLicenseUrl || item.foodSafetyCertUrl || '')"
                  :alt="item.merchantName || item.storeName || 'merchant-profile'"
                />
                <div v-else class="thumb empty">-</div>
              </td>
              <td>
                <div class="main-title">{{ item.merchantName || item.storeName || `商家#${item.userId}` }}</div>
                <div class="sub-line">{{ item.storeAddress || '-' }} · 人均 {{ item.averageCost || '-' }} 元</div>
              </td>
              <td><span class="single-line">{{ item.chefTeamIntro || item.rejectReason || '-' }}</span></td>
              <td class="col-time">{{ formatDate(item.updatedAt) }}</td>
              <td class="col-status">
                <span :class="statusTagClass(item.status, 'merchant')">{{ merchantStatusText(item.status) }}</span>
              </td>
              <td class="col-action">
                <button type="button" class="text-btn pass" @click="approveQualification(item.id)" :disabled="item.status !== 0">通过</button>
                <button type="button" class="text-btn reject" @click="rejectQualification(item.id)" :disabled="item.status !== 0">驳回</button>
                <button type="button" class="text-btn detail" @click="viewQualificationDetail(item)">详情</button>
              </td>
            </tr>
          </tbody>
        </table>

        <table v-else class="data-table">
          <thead>
            <tr>
              <th class="col-thumb">图标</th>
              <th>名称 / 介绍</th>
              <th class="col-time">排序</th>
              <th class="col-status">状态</th>
              <th class="col-action">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in filteredCategories" :key="item.id">
              <td class="col-thumb"><div class="thumb">{{ item.icon || '🍽' }}</div></td>
              <td>
                <div class="main-title">{{ item.name }}</div>
                <div class="sub-line">{{ item.description || '-' }}</div>
              </td>
              <td class="col-time">{{ item.sort }}</td>
              <td class="col-status">
                <span :class="item.status === 1 ? 'status-tag approved' : 'status-tag rejected'">{{ item.status === 1 ? '已启用' : '已停用' }}</span>
              </td>
              <td class="col-action">
                <button type="button" class="text-btn detail" @click="toggleCategory(item.id, item.status)">{{ item.status === 1 ? '停用' : '启用' }}</button>
                <button type="button" class="text-btn reject" @click="deleteCategoryById(item.id)">删除</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  auditMerchantApplication,
  auditMerchantProfile,
  auditPost,
  fetchAdminPosts,
  fetchMerchantApplicationsForAdmin,
  fetchMerchantProfilesForAdmin,
  type AdminPostItem,
  type MerchantApplicationItem,
  type MerchantProfileItem,
} from '@/api/admin'
import {
  createCategory,
  deleteCategory,
  fetchAdminCategoryList,
  updateCategoryStatus,
  type CategoryItem,
} from '@/api/category'

type ManageTab = 'post' | 'merchant' | 'qualification' | 'category'

const API_HOST = 'http://localhost:8080'
const route = useRoute()
const router = useRouter()

const tab = ref<ManageTab>('post')
const keyword = ref('')
const statusFilter = ref('')

const posts = reactive<{ total: number; records: AdminPostItem[] }>({ total: 0, records: [] })
const merchantApplications = ref<MerchantApplicationItem[]>([])
const merchantProfiles = ref<MerchantProfileItem[]>([])
const categories = ref<CategoryItem[]>([])

const pendingPostCount = ref(0)
const pendingMerchantCount = ref(0)
const pendingQualificationCount = ref(0)

const categoryForm = reactive({ name: '', description: '', icon: '🍜' })
const categoryIconOptions = ['🍜', '🍢', '🍰', '🍲', '🍖', '🍛', '🍚', '🍝', '🍕', '🌮', '🧋', '🥤', '☕', '🍵', '🍹', '🍔', '🍟', '🍗', '🍣', '🍤', '🥟', '🥪', '🥗']

function resolveImage(url: string) {
  if (!url) return ''
  return url.startsWith('http') ? url : `${API_HOST}${url}`
}

function formatDate(value?: string | null) {
  if (!value) return '-'
  return value.replace('T', ' ').slice(0, 16)
}

function postStatusText(status: number) {
  if (status === 1) return '已通过'
  if (status === 3) return '已驳回'
  return '待审核'
}

function merchantStatusText(status: number) {
  if (status === 1) return '已通过'
  if (status === 2) return '已驳回'
  return '待审核'
}

function statusTagClass(status: number, type: 'post' | 'merchant') {
  const rejectedValue = type === 'post' ? 3 : 2
  if (status === 1) return 'status-tag approved'
  if (status === rejectedValue) return 'status-tag rejected'
  return 'status-tag pending'
}

function textIncludes(base: string | null | undefined, search: string) {
  return (base || '').toLowerCase().includes(search.toLowerCase())
}

const filteredPosts = computed(() => {
  const search = keyword.value.trim()
  return posts.records.filter((item) => {
    const passStatus = statusFilter.value ? item.status === Number(statusFilter.value) : true
    const passKeyword = !search
      || textIncludes(item.title, search)
      || textIncludes(item.summary, search)
      || textIncludes(item.address, search)
      || textIncludes(item.authorNickname, search)
    return passStatus && passKeyword
  })
})

const filteredMerchantApplications = computed(() => {
  const search = keyword.value.trim()
  return merchantApplications.value.filter((item) => {
    const passStatus = statusFilter.value ? item.status === Number(statusFilter.value) : true
    const passKeyword = !search
      || textIncludes(item.merchantName, search)
      || textIncludes(item.contactName, search)
      || textIncludes(item.contactPhone, search)
      || textIncludes(item.address, search)
    return passStatus && passKeyword
  })
})

const filteredMerchantProfiles = computed(() => {
  const search = keyword.value.trim()
  return merchantProfiles.value.filter((item) => {
    const passStatus = statusFilter.value ? item.status === Number(statusFilter.value) : true
    const passKeyword = !search
      || textIncludes(item.merchantName, search)
      || textIncludes(item.storeName, search)
      || textIncludes(item.storeAddress, search)
    return passStatus && passKeyword
  })
})

const filteredCategories = computed(() => {
  const search = keyword.value.trim().toLowerCase()
  if (!search) return categories.value
  return categories.value.filter((item) =>
    (item.name || '').toLowerCase().includes(search)
    || (item.description || '').toLowerCase().includes(search))
})

function syncTabFromRoute() {
  const queryTab = route.query.tab
  tab.value = queryTab === 'merchant' || queryTab === 'qualification' || queryTab === 'category' ? queryTab : 'post'
}

function setTab(nextTab: ManageTab) {
  keyword.value = ''
  statusFilter.value = ''
  router.replace({ name: 'admin-manage', query: { tab: nextTab } })
}

async function loadPosts() {
  const response = await fetchAdminPosts({ current: 1, size: 100 })
  posts.total = response.data.total
  posts.records = response.data.records
}

async function loadMerchantApplications() {
  const response = await fetchMerchantApplicationsForAdmin({})
  merchantApplications.value = response.data
}

async function loadMerchantProfiles() {
  const response = await fetchMerchantProfilesForAdmin({})
  merchantProfiles.value = response.data
}

async function loadCategories() {
  const response = await fetchAdminCategoryList()
  categories.value = response.data
}

async function loadPendingCounts() {
  const [postResponse, merchantResponse, qualificationResponse] = await Promise.all([
    fetchAdminPosts({ current: 1, size: 1, status: 0 }),
    fetchMerchantApplicationsForAdmin({ status: 0 }),
    fetchMerchantProfilesForAdmin({ status: 0 }),
  ])
  pendingPostCount.value = postResponse.data.total
  pendingMerchantCount.value = merchantResponse.data.length
  pendingQualificationCount.value = qualificationResponse.data.length
}

async function refreshCurrentTab() {
  if (tab.value === 'post') {
    await loadPosts()
  } else if (tab.value === 'merchant') {
    await loadMerchantApplications()
  } else if (tab.value === 'qualification') {
    await loadMerchantProfiles()
  } else {
    await loadCategories()
  }
  await loadPendingCounts()
}

async function approvePost(id: number) {
  await auditPost(id, { result: 1, remark: '审核通过' })
  await refreshCurrentTab()
}

async function rejectPost(id: number) {
  const reason = window.prompt('请输入驳回原因：')
  if (reason === null) return
  await auditPost(id, { result: 0, remark: reason.trim() || '内容不符合规范' })
  await refreshCurrentTab()
}

function viewPostDetail(id: number) {
  window.open(`/food/${id}`, '_blank')
}

async function approveMerchant(id: number) {
  await auditMerchantApplication(id, { result: 1 })
  await refreshCurrentTab()
}

async function rejectMerchant(id: number) {
  const reason = window.prompt('请输入驳回原因：')
  if (reason === null) return
  await auditMerchantApplication(id, { result: 0, rejectReason: reason.trim() || '资料不完整' })
  await refreshCurrentTab()
}

function viewMerchantDetail(item: MerchantApplicationItem) {
  if (item.businessLicense) {
    window.open(resolveImage(item.businessLicense), '_blank')
    return
  }
  window.alert(`商家：${item.merchantName}\n联系人：${item.contactName}\n电话：${item.contactPhone}\n地址：${item.address}`)
}

async function approveQualification(id: number) {
  await auditMerchantProfile(id, { result: 1 })
  await refreshCurrentTab()
}

async function rejectQualification(id: number) {
  const reason = window.prompt('请输入驳回原因：')
  if (reason === null) return
  await auditMerchantProfile(id, { result: 0, rejectReason: reason.trim() || '资质材料不符合要求' })
  await refreshCurrentTab()
}

function viewQualificationDetail(item: MerchantProfileItem) {
  const target = item.businessLicenseUrl || item.foodSafetyCertUrl
  if (target) {
    window.open(resolveImage(target), '_blank')
    return
  }
  window.alert(`商家：${item.merchantName || item.storeName || '-'}\n地址：${item.storeAddress || '-'}\n团队介绍：${item.chefTeamIntro || '-'}`)
}

async function handleSaveCategory() {
  if (!categoryForm.name.trim()) return
  const maxSort = categories.value.reduce((max, item) => (item.sort > max ? item.sort : max), 0)
  await createCategory({
    name: categoryForm.name.trim(),
    description: categoryForm.description.trim() || undefined,
    icon: categoryForm.icon || undefined,
    sort: maxSort + 1,
    status: 1,
  })
  categoryForm.name = ''
  categoryForm.description = ''
  categoryForm.icon = '🍜'
  await refreshCurrentTab()
}

async function toggleCategory(id: number, status: number) {
  await updateCategoryStatus(id, status === 1 ? 0 : 1)
  await refreshCurrentTab()
}

async function deleteCategoryById(id: number) {
  if (!window.confirm('确认删除该分类吗？')) return
  await deleteCategory(id)
  await refreshCurrentTab()
}

watch(() => route.query.tab, syncTabFromRoute, { immediate: true })
watch(tab, () => {
  keyword.value = ''
  statusFilter.value = ''
})

onMounted(async () => {
  await Promise.all([loadPosts(), loadMerchantApplications(), loadMerchantProfiles(), loadCategories(), loadPendingCounts()])
})
</script>

<style scoped>
.review-shell {
  display: grid;
  grid-template-columns: 220px 1fr;
  gap: 16px;
}

.review-nav {
  display: grid;
  align-content: start;
  gap: 8px;
  padding: 12px;
  border-radius: 14px;
  background: var(--admin-surface-soft);
  border: 1px solid var(--admin-line);
  box-shadow: var(--admin-shadow);
}

.nav-item {
  position: relative;
  min-height: 40px;
  border-radius: 10px;
  border: 1px solid var(--admin-line);
  background: var(--admin-surface);
  color: var(--admin-muted);
  text-align: left;
  padding: 0 12px;
  font: inherit;
}

.nav-item.active {
  background: color-mix(in srgb, var(--admin-accent) 14%, var(--admin-surface));
  color: var(--admin-text);
  border-color: color-mix(in srgb, var(--admin-accent) 42%, var(--admin-line));
}

.nav-badge {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  min-width: 18px;
  height: 18px;
  line-height: 18px;
  border-radius: 999px;
  background: #d25963;
  color: #fff;
  text-align: center;
  font-size: 11px;
}

.table-zone {
  background: var(--admin-surface);
  border-radius: 14px;
  border: 1px solid var(--admin-line);
  box-shadow: var(--admin-shadow);
  padding: 12px;
}

.tool-bar {
  display: grid;
  grid-template-columns: minmax(220px, 1fr) 150px auto;
  gap: 10px;
  margin-bottom: 10px;
}

.search-input,
.status-select,
.category-create input,
.category-create select {
  min-height: 38px;
  border-radius: 10px;
  border: 1px solid var(--admin-line);
  background: var(--admin-surface-soft);
  color: var(--admin-text);
  padding: 0 10px;
  font: inherit;
}

.search-input::placeholder {
  color: var(--admin-muted);
}

.refresh-btn,
.create-btn {
  min-height: 38px;
  border: 1px solid color-mix(in srgb, var(--admin-accent) 45%, var(--admin-line));
  border-radius: 10px;
  background: color-mix(in srgb, var(--admin-accent) 22%, var(--admin-surface));
  color: var(--admin-text);
  font: inherit;
  padding: 0 14px;
}

.category-create {
  display: grid;
  grid-template-columns: 1fr 1.4fr 100px auto;
  gap: 10px;
  margin-bottom: 10px;
}

.category-create select {
  text-align: center;
  font-size: 20px;
}

.table-wrap {
  overflow: auto;
  border: 1px solid var(--admin-line);
  border-radius: 10px;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
  background: var(--admin-surface);
}

.data-table th,
.data-table td {
  border-bottom: 1px solid var(--admin-line);
  color: var(--admin-text);
  padding: 10px;
  text-align: left;
  vertical-align: middle;
  font-size: 13px;
}

.data-table th {
  color: var(--admin-muted);
  font-weight: 600;
  background: var(--admin-surface-soft);
}

.col-thumb {
  width: 76px;
}

.col-time {
  width: 144px;
  color: var(--admin-muted);
}

.col-status {
  width: 110px;
}

.col-action {
  width: 180px;
}

.thumb {
  width: 48px;
  height: 48px;
  border-radius: 6px;
  object-fit: cover;
  display: grid;
  place-items: center;
  background: var(--admin-surface-soft);
  border: 1px solid var(--admin-line);
}

.thumb.empty {
  color: var(--admin-muted);
}

.main-title {
  font-weight: 700;
  color: var(--admin-text);
}

.sub-line {
  margin-top: 4px;
  font-size: 12px;
  color: var(--admin-muted);
}

.single-line {
  display: inline-block;
  width: 100%;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: var(--admin-text);
}

.status-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 64px;
  height: 24px;
  padding: 0 8px;
  border-radius: 999px;
  font-size: 12px;
  border: 1px solid transparent;
}

.status-tag.pending {
  background: rgba(255, 208, 108, 0.18);
  border-color: rgba(255, 208, 108, 0.35);
  color: #ffd77e;
}

.status-tag.approved {
  background: rgba(122, 225, 168, 0.18);
  border-color: rgba(122, 225, 168, 0.35);
  color: #91e5b8;
}

.status-tag.rejected {
  background: rgba(255, 136, 136, 0.17);
  border-color: rgba(255, 136, 136, 0.32);
  color: #ffabab;
}

.text-btn {
  background: transparent;
  border: none;
  color: var(--admin-muted);
  font: inherit;
  padding: 0 6px;
}

.text-btn.pass {
  color: var(--el-color-success, #67c23a);
}

.text-btn.reject {
  color: var(--el-color-danger, #f56c6c);
}

.text-btn.detail {
  color: var(--el-color-primary, #409eff);
}

.text-btn:disabled {
  opacity: 0.35;
}

@media (max-width: 1200px) {
  .review-shell {
    grid-template-columns: 1fr;
  }

  .review-nav {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 900px) {
  .tool-bar,
  .category-create {
    grid-template-columns: 1fr;
  }
}
</style>
