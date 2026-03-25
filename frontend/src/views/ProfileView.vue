<template>
  <section class="profile-page">
    <aside class="profile-side">
      <el-menu :default-active="activeMenu" class="side-menu" @select="handleMenuSelect">
        <el-menu-item index="profile">
          <span class="menu-icon"><svg viewBox="0 0 24 24"><path d="M12 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8zm0 2c-4.4 0-8 2-8 4.5V20h16v-1.5C20 16 16.4 14 12 14z" /></svg></span>
          <span>资料</span>
        </el-menu-item>
        <el-menu-item index="posts">
          <span class="menu-icon"><svg viewBox="0 0 24 24"><path d="M5 5h14v3H5V5zm0 6h14v3H5v-3zm0 6h10v3H5v-3z" /></svg></span>
          <span>帖子</span>
        </el-menu-item>
        <el-menu-item index="reviews">
          <span class="menu-icon"><svg viewBox="0 0 24 24"><path d="M12 3l2.8 5.7 6.2.9-4.5 4.4 1.1 6.2L12 17.2 6.4 20.2l1.1-6.2L3 9.6l6.2-.9L12 3z" /></svg></span>
          <span>评价</span>
        </el-menu-item>
        <el-menu-item v-if="canUseMerchantCenter" index="qualification">
          <span class="menu-icon"><svg viewBox="0 0 24 24"><path d="M12 3l7 3v6c0 4.1-2.8 7.8-7 9-4.2-1.2-7-4.9-7-9V6l7-3z" /></svg></span>
          <span>资质认证</span>
        </el-menu-item>
        <el-menu-item v-if="canUseMerchantCenter" index="chef">
          <span class="menu-icon"><svg viewBox="0 0 24 24"><path d="M6 8a6 6 0 1 1 12 0v2H6V8zm1 4h10v7H7v-7z" /></svg></span>
          <span>厨师管理</span>
        </el-menu-item>
      </el-menu>
    </aside>

    <main class="profile-main">
      <header class="main-head">
        <h2>账户设置 (Profile Settings)</h2>
        <p>管理账号资料、内容数据与商家认证状态</p>
      </header>

      <section class="stats-grid">
        <article class="stat-card"><span>加入时间</span><strong>{{ joinDateText }}</strong></article>
        <article class="stat-card"><span>发布作品</span><strong>{{ myPosts.length }}</strong></article>
        <article class="stat-card"><span>评价</span><strong>{{ myComments.length }}</strong></article>
        <article class="stat-card"><span>获赞数</span><strong>{{ totalReceivedLikes }}</strong></article>
      </section>

      <section v-if="activeMenu === 'profile'" class="content-grid">
        <article class="content-card profile-card">
          <h3>基本资料</h3>
          <div class="avatar-edit">
            <input ref="avatarInputRef" class="hidden-input" type="file" accept="image/*" @change="handleAvatarChange" />
            <button class="avatar-uploader" type="button" @click="openAvatarPicker">
              <img v-if="form.avatar" :src="toImageUrl(form.avatar)" alt="avatar" />
              <span v-else class="plus">+</span>
            </button>
            <p>{{ avatarUploading ? '上传中...' : '点击更换头像' }}</p>
          </div>

          <div class="compact-form">
            <label><span>昵称</span><input v-model.trim="form.nickname" type="text" placeholder="请输入昵称" /></label>
            <label><span>账号</span><input :value="profile?.username || ''" type="text" disabled /></label>
            <label><span>邮箱</span><input v-model.trim="form.email" type="email" placeholder="请输入邮箱" /></label>
            <label class="full"><span>个人简介</span><textarea v-model.trim="form.bio" rows="4" placeholder="介绍你的口味偏好与常去商圈" /></label>
          </div>

          <p v-if="notice" class="notice">{{ notice }}</p>
          <div class="form-actions">
            <button type="button" class="btn ghost" @click="resetProfileForm">取消</button>
            <button type="button" class="btn primary" :disabled="submitting" @click="handleSave">{{ submitting ? '保存中...' : '确认修改' }}</button>
          </div>
        </article>

        <article class="content-card security-card">
          <h3>账号安全</h3>
          <p>建议定期更新密码，保护账号安全。</p>
          <button type="button" class="btn outline" @click="mockChangePassword">修改密码</button>
        </article>
      </section>

      <section v-else-if="activeMenu === 'posts'" class="content-card list-card">
        <h3>我的帖子</h3>
        <RouterLink v-for="item in myPosts" :key="item.id" class="row-item" :to="`/food/${item.id}`">
          <div><strong>{{ item.title }}</strong><p>{{ item.summary || '暂无摘要' }}</p></div>
          <span>浏览 {{ item.viewCount }} · 点赞 {{ item.likeCount }}</span>
        </RouterLink>
        <div v-if="!myPosts.length" class="empty">暂无帖子</div>
      </section>

      <section v-else-if="activeMenu === 'reviews'" class="content-card list-card">
        <h3>我的评价</h3>
        <article v-for="item in myComments" :key="item.id" class="row-item">
          <div><strong>{{ item.postTitle }}</strong><p>{{ item.content }}</p></div>
          <span>{{ formatDate(item.createdAt) }}</span>
        </article>
        <div v-if="!myComments.length" class="empty">暂无评价</div>
      </section>

      <section v-else-if="canUseMerchantCenter && activeMenu === 'qualification'" class="content-card merchant-panel">
        <div class="panel-head">
          <h3>资质认证与门店信息</h3>
          <div class="panel-head-actions">
            <span class="status-chip" :class="merchantStatusClass"><span class="status-dot"></span>{{ merchantStatusText }}</span>
            <button v-if="hasSubmittedQualification && !merchantEditMode" type="button" class="btn outline" @click="enterMerchantEditMode">再次认证/修改</button>
            <button v-if="hasSubmittedQualification && merchantEditMode" type="button" class="btn ghost" @click="cancelMerchantEditMode">取消编辑</button>
          </div>
        </div>

        <div v-if="!merchantEditMode" class="qualification-view">
          <div class="qualification-grid">
            <article class="info-card"><span>商家名称</span><strong>{{ merchantForm.merchantName || '-' }}</strong></article>
            <article class="info-card"><span>门店名称</span><strong>{{ merchantForm.storeName || '-' }}</strong></article>
            <article class="info-card full"><span>门店地址</span><strong>{{ merchantForm.storeAddress || '-' }}</strong></article>
            <article class="info-card"><span>人均消费</span><strong>{{ merchantForm.averageCost ? `${merchantForm.averageCost} 元` : '-' }}</strong></article>
            <article class="info-card full"><span>厨师团队简介</span><strong>{{ merchantForm.chefTeamIntro || '-' }}</strong></article>
          </div>
          <div class="upload-grid">
            <article class="upload-card"><h5>营业执照</h5><img v-if="merchantForm.businessLicenseUrl" :src="toImageUrl(merchantForm.businessLicenseUrl)" alt="license" /><p v-else class="upload-empty">未上传</p></article>
            <article class="upload-card"><h5>健康证</h5><img v-if="merchantForm.foodSafetyCertUrl" :src="toImageUrl(merchantForm.foodSafetyCertUrl)" alt="health-cert" /><p v-else class="upload-empty">未上传</p></article>
          </div>
          <p v-if="merchantForm.status === 2 && merchantForm.rejectReason" class="notice">未通过原因：{{ merchantForm.rejectReason }}</p>
        </div>

        <div v-else class="qualification-edit">
          <div class="form-grid">
            <label class="field"><span>商家名称</span><input v-model.trim="merchantForm.merchantName" type="text" placeholder="请输入商家名称" /></label>
            <label class="field"><span>门店名称</span><input v-model.trim="merchantForm.storeName" type="text" placeholder="请输入门店名称" /></label>
            <label class="field full-row"><span>门店地址</span><input v-model.trim="merchantForm.storeAddress" type="text" placeholder="请输入门店地址" /></label>
            <label class="field"><span>人均消费</span><input v-model.number="merchantForm.averageCost" type="number" min="0" /></label>
            <label class="field full-row"><span>厨师团队简介</span><textarea v-model.trim="merchantForm.chefTeamIntro" rows="3" /></label>
          </div>
          <div class="upload-grid">
            <article class="upload-card">
              <h5>营业执照</h5>
              <input ref="licenseInputRef" class="hidden-input" type="file" accept="image/*" @change="handleLicenseChange" />
              <button type="button" class="btn outline" @click="openLicensePicker">上传/替换图片</button>
              <img v-if="merchantForm.businessLicenseUrl" :src="toImageUrl(merchantForm.businessLicenseUrl)" alt="license" />
            </article>
            <article class="upload-card">
              <h5>健康证</h5>
              <input ref="safetyInputRef" class="hidden-input" type="file" accept="image/*" @change="handleSafetyChange" />
              <button type="button" class="btn outline" @click="openSafetyPicker">上传/替换图片</button>
              <img v-if="merchantForm.foodSafetyCertUrl" :src="toImageUrl(merchantForm.foodSafetyCertUrl)" alt="health-cert" />
            </article>
          </div>
          <p v-if="merchantNotice" class="notice">{{ merchantNotice }}</p>
          <div class="form-actions"><button type="button" class="btn primary" :disabled="merchantSubmitting" @click="saveMerchantProfile">{{ merchantSubmitting ? '保存中...' : (hasSubmittedQualification ? '再次提交认证' : '保存并提交认证') }}</button></div>
        </div>
      </section>

      <section v-else-if="canUseMerchantCenter && activeMenu === 'chef'" class="content-card merchant-panel">
        <div class="panel-head">
          <h3>厨师管理</h3>
          <button type="button" class="btn primary" @click="openChefEditor()">新增厨师</button>
        </div>
        <div class="list-card">
          <article v-for="chef in chefList" :key="chef.id" class="row-item">
            <div class="chef-row">
              <img v-if="chef.avatarUrl" :src="toImageUrl(chef.avatarUrl)" alt="chef-avatar" />
              <div><strong>{{ chef.chefName }}</strong><p>{{ chef.title || '厨师' }}</p><small>{{ chef.intro || '暂无简介' }}</small></div>
            </div>
            <button type="button" class="btn outline" @click="openChefEditor(chef)">编辑</button>
          </article>
          <div v-if="!chefList.length" class="empty">暂无厨师信息</div>
        </div>
      </section>
    </main>

    <div v-if="chefEditorVisible" class="dialog-mask" @click="closeChefEditor">
      <article class="dialog-card" @click.stop>
        <header><h4>{{ editingChefId ? '编辑厨师' : '新增厨师' }}</h4><button type="button" class="btn ghost" @click="closeChefEditor">关闭</button></header>
        <div class="form-grid">
          <label class="field"><span>姓名</span><input v-model.trim="chefForm.chefName" type="text" placeholder="请输入姓名" /></label>
          <label class="field"><span>头衔</span><input v-model.trim="chefForm.title" type="text" placeholder="例如 主理厨师" /></label>
          <label class="field full-row"><span>简介</span><textarea v-model.trim="chefForm.intro" rows="3" placeholder="请输入简介" /></label>
          <label class="field"><span>排序</span><input v-model.number="chefForm.sort" type="number" min="0" /></label>
          <div class="field full-row">
            <span>头像</span>
            <input ref="chefAvatarInputRef" class="hidden-input" type="file" accept="image/*" @change="handleChefAvatarChange" />
            <button type="button" class="btn outline" @click="openChefAvatarPicker">上传头像</button>
            <img v-if="chefForm.avatarUrl" class="avatar-preview" :src="toImageUrl(chefForm.avatarUrl)" alt="chef-avatar-preview" />
          </div>
        </div>
        <footer class="dialog-foot">
          <button v-if="editingChefId" type="button" class="btn danger" :disabled="chefSubmitting" @click="handleDeleteChef">删除</button>
          <button type="button" class="btn primary" :disabled="chefSubmitting" @click="handleSaveChef">{{ chefSubmitting ? '保存中...' : '保存' }}</button>
        </footer>
      </article>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMenu, ElMenuItem } from 'element-plus'
import 'element-plus/es/components/menu/style/css'
import {
  fetchMyComments,
  fetchMyLikes,
  fetchMyPosts,
  fetchMyProfile,
  updateMyProfile,
  type UserCenterComment,
  type UserProfile,
} from '@/api/user'
import type { FoodPostCard } from '@/api/post'
import {
  createChef,
  deleteChef,
  fetchMyChefs,
  fetchMyMerchantProfile,
  type ChefInfo,
  updateChef,
  updateMyMerchantProfile,
} from '@/api/merchant'
import { uploadImage } from '@/api/upload'
import { updateAuthUser } from '@/stores/auth'

const API_HOST = 'http://localhost:8080'

const activeMenu = ref('profile')

const profile = ref<UserProfile | null>(null)
const submitting = ref(false)
const notice = ref('')
const avatarUploading = ref(false)
const myPosts = ref<FoodPostCard[]>([])
const myComments = ref<UserCenterComment[]>([])
const myLikes = ref<FoodPostCard[]>([])
const avatarInputRef = ref<HTMLInputElement | null>(null)

const form = reactive({
  nickname: '',
  avatar: '',
  phone: '',
  email: '',
  bio: '热爱本地小馆子，欢迎互相种草。',
})

const merchantSubmitting = ref(false)
const merchantNotice = ref('')
const merchantEditMode = ref(true)
const licenseInputRef = ref<HTMLInputElement | null>(null)
const safetyInputRef = ref<HTMLInputElement | null>(null)
const merchantForm = reactive({
  merchantName: '',
  storeName: '',
  storeAddress: '',
  averageCost: undefined as number | undefined,
  businessLicenseUrl: '',
  foodSafetyCertUrl: '',
  chefTeamIntro: '',
  status: 0,
  rejectReason: '',
})

const chefList = ref<ChefInfo[]>([])
const chefEditorVisible = ref(false)
const editingChefId = ref<number | null>(null)
const chefSubmitting = ref(false)
const chefAvatarInputRef = ref<HTMLInputElement | null>(null)
const chefForm = reactive({
  chefName: '',
  title: '',
  avatarUrl: '',
  intro: '',
  sort: 0,
})

const isMerchant = computed(() => profile.value?.role === 2)
const canUseMerchantCenter = computed(() => isMerchant.value)
const totalReceivedLikes = computed(() =>
  myPosts.value.reduce((sum, post) => sum + (post.likeCount || 0), 0),
)
const hasSubmittedQualification = computed(() =>
  merchantForm.status === 1
  || merchantForm.status === 2
  || Boolean(
    merchantForm.merchantName
    || merchantForm.storeName
    || merchantForm.storeAddress
    || merchantForm.businessLicenseUrl
    || merchantForm.foodSafetyCertUrl,
  ),
)

const joinDateText = computed(() => (profile.value?.createdAt ? formatDate(profile.value.createdAt).slice(0, 10) : '-'))

const merchantStatusText = computed(() => {
  if (merchantForm.status === 1) return '已认证'
  if (merchantForm.status === 2) return '未通过'
  return '审核中'
})

const merchantStatusClass = computed(() => {
  if (merchantForm.status === 1) return 'certified'
  if (merchantForm.status === 2) return 'rejected'
  return 'reviewing'
})

function handleMenuSelect(index: string) {
  activeMenu.value = index
}

function formatDate(value: string) {
  return value.replace('T', ' ').slice(0, 16)
}

function toImageUrl(url: string) {
  return url.startsWith('http') ? url : `${API_HOST}${url}`
}

function openAvatarPicker() {
  avatarInputRef.value?.click()
}

function openLicensePicker() {
  licenseInputRef.value?.click()
}

function openSafetyPicker() {
  safetyInputRef.value?.click()
}

function openChefAvatarPicker() {
  chefAvatarInputRef.value?.click()
}

function enterMerchantEditMode() {
  merchantEditMode.value = true
}

async function cancelMerchantEditMode() {
  merchantNotice.value = ''
  await loadMerchantData()
}

function resetProfileForm() {
  if (!profile.value) return
  form.nickname = profile.value.nickname || ''
  form.avatar = profile.value.avatar || ''
  form.phone = profile.value.phone || ''
  form.email = profile.value.email || ''
  notice.value = ''
}

function mockChangePassword() {
  window.alert('这里是修改密码入口（Mock），可接入真实流程。')
}

async function uploadSingleImage(file: File) {
  const response = await uploadImage(file)
  return response.data.url
}

async function handleAvatarChange(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  try {
    avatarUploading.value = true
    form.avatar = await uploadSingleImage(file)
    notice.value = '头像上传成功，记得确认修改'
  } catch (error) {
    notice.value = error instanceof Error ? error.message : '头像上传失败'
  } finally {
    avatarUploading.value = false
    if (avatarInputRef.value) avatarInputRef.value.value = ''
  }
}

async function handleLicenseChange(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  try {
    merchantForm.businessLicenseUrl = await uploadSingleImage(file)
    merchantNotice.value = '营业执照上传成功'
  } catch (error) {
    merchantNotice.value = error instanceof Error ? error.message : '营业执照上传失败'
  } finally {
    if (licenseInputRef.value) licenseInputRef.value.value = ''
  }
}

async function handleSafetyChange(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  try {
    merchantForm.foodSafetyCertUrl = await uploadSingleImage(file)
    merchantNotice.value = '健康证上传成功'
  } catch (error) {
    merchantNotice.value = error instanceof Error ? error.message : '健康证上传失败'
  } finally {
    if (safetyInputRef.value) safetyInputRef.value.value = ''
  }
}

async function handleChefAvatarChange(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  try {
    chefForm.avatarUrl = await uploadSingleImage(file)
  } finally {
    if (chefAvatarInputRef.value) chefAvatarInputRef.value.value = ''
  }
}

async function loadProfile() {
  const response = await fetchMyProfile()
  profile.value = response.data
  resetProfileForm()
  updateAuthUser(response.data)
}

async function loadCommunityData() {
  const [postsResponse, commentsResponse, likesResponse] = await Promise.all([
    fetchMyPosts(),
    fetchMyComments(),
    fetchMyLikes(),
  ])
  myPosts.value = postsResponse.data
  myComments.value = commentsResponse.data
  myLikes.value = likesResponse.data
}

async function loadMerchantData() {
  if (!canUseMerchantCenter.value) {
    if (activeMenu.value === 'qualification' || activeMenu.value === 'chef') {
      activeMenu.value = 'profile'
    }
    return
  }
  const [merchantResponse, chefResponse] = await Promise.all([fetchMyMerchantProfile(), fetchMyChefs()])
  const merchant = merchantResponse.data
  merchantForm.merchantName = merchant.merchantName || ''
  merchantForm.storeName = merchant.storeName || ''
  merchantForm.storeAddress = merchant.storeAddress || ''
  merchantForm.averageCost = merchant.averageCost ?? undefined
  merchantForm.businessLicenseUrl = merchant.businessLicenseUrl || ''
  merchantForm.foodSafetyCertUrl = merchant.foodSafetyCertUrl || ''
  merchantForm.chefTeamIntro = merchant.chefTeamIntro || ''
  merchantForm.status = merchant.status
  merchantForm.rejectReason = merchant.rejectReason || ''
  chefList.value = chefResponse.data
  merchantEditMode.value = !(merchant.status === 1 || merchant.status === 2)
}

async function handleSave() {
  notice.value = ''
  try {
    submitting.value = true
    const response = await updateMyProfile({
      nickname: form.nickname,
      avatar: form.avatar || undefined,
      phone: form.phone || undefined,
      email: form.email || undefined,
    })
    profile.value = response.data
    updateAuthUser(response.data)
    notice.value = '个人资料保存成功'
  } catch (error) {
    notice.value = error instanceof Error ? error.message : '保存失败'
  } finally {
    submitting.value = false
  }
}

async function saveMerchantProfile() {
  merchantNotice.value = ''
  try {
    merchantSubmitting.value = true
    const response = await updateMyMerchantProfile({
      merchantName: merchantForm.merchantName || undefined,
      storeName: merchantForm.storeName || undefined,
      storeAddress: merchantForm.storeAddress || undefined,
      averageCost: merchantForm.averageCost,
      businessLicenseUrl: merchantForm.businessLicenseUrl || undefined,
      foodSafetyCertUrl: merchantForm.foodSafetyCertUrl || undefined,
      chefTeamIntro: merchantForm.chefTeamIntro || undefined,
    })
    merchantForm.status = response.data.status
    merchantForm.rejectReason = response.data.rejectReason || ''
    merchantEditMode.value = false
    merchantNotice.value = '已提交，当前状态：审核中'
  } catch (error) {
    merchantNotice.value = error instanceof Error ? error.message : '保存失败'
  } finally {
    merchantSubmitting.value = false
  }
}

function resetChefForm() {
  chefForm.chefName = ''
  chefForm.title = ''
  chefForm.avatarUrl = ''
  chefForm.intro = ''
  chefForm.sort = 0
}

function openChefEditor(chef?: ChefInfo) {
  if (!chef) {
    editingChefId.value = null
    resetChefForm()
  } else {
    editingChefId.value = chef.id
    chefForm.chefName = chef.chefName
    chefForm.title = chef.title || ''
    chefForm.avatarUrl = chef.avatarUrl || ''
    chefForm.intro = chef.intro || ''
    chefForm.sort = chef.sort || 0
  }
  chefEditorVisible.value = true
}

function closeChefEditor() {
  chefEditorVisible.value = false
}

async function refreshChefList() {
  const response = await fetchMyChefs()
  chefList.value = response.data
}

async function handleSaveChef() {
  if (!chefForm.chefName.trim()) {
    merchantNotice.value = '请填写厨师姓名'
    return
  }
  try {
    chefSubmitting.value = true
    const payload = {
      chefName: chefForm.chefName.trim(),
      title: chefForm.title || undefined,
      avatarUrl: chefForm.avatarUrl || undefined,
      intro: chefForm.intro || undefined,
      sort: chefForm.sort,
    }
    if (editingChefId.value) {
      await updateChef(editingChefId.value, payload)
      merchantNotice.value = '厨师信息更新成功'
    } else {
      await createChef(payload)
      merchantNotice.value = '厨师添加成功'
    }
    await refreshChefList()
    chefEditorVisible.value = false
  } catch (error) {
    merchantNotice.value = error instanceof Error ? error.message : '厨师保存失败'
  } finally {
    chefSubmitting.value = false
  }
}

async function handleDeleteChef() {
  if (!editingChefId.value) return
  try {
    chefSubmitting.value = true
    await deleteChef(editingChefId.value)
    merchantNotice.value = '厨师删除成功'
    await refreshChefList()
    chefEditorVisible.value = false
  } catch (error) {
    merchantNotice.value = error instanceof Error ? error.message : '厨师删除失败'
  } finally {
    chefSubmitting.value = false
  }
}

onMounted(async () => {
  try {
    await loadProfile()
    await loadCommunityData()
    await loadMerchantData()
  } catch (error) {
    notice.value = error instanceof Error ? error.message : '资料加载失败'
  }
})
</script>

<style scoped>
.profile-page { display: grid; grid-template-columns: 220px 1fr; gap: 14px; }
.profile-side { border-radius: 20px; background: linear-gradient(180deg, #4a3226, #3f291f); box-shadow: 0 14px 30px rgba(54,33,23,.24); padding: 12px; }
.side-menu { border-right: none; background: transparent; }
.side-menu :deep(.el-menu-item) { height: 44px; border-radius: 10px; color: rgba(255,239,224,.84); margin-bottom: 6px; }
.side-menu :deep(.el-menu-item.is-active) { background: rgba(255,255,255,.14); color: #fff7ee; }
.side-menu :deep(.el-menu-item:hover) { background: rgba(255,255,255,.1); }
.menu-icon { width: 16px; height: 16px; margin-right: 8px; display: inline-flex; }
.menu-icon svg { width: 16px; height: 16px; fill: none; stroke: currentColor; stroke-width: 1.8; stroke-linecap: round; stroke-linejoin: round; }

.profile-main { display: grid; gap: 12px; }
.main-head { padding: 14px 16px; border-radius: 16px; background: rgba(255,252,246,.92); border: 1px solid rgba(98,61,27,.12); box-shadow: 0 10px 24px rgba(69,43,20,.08); }
.main-head h2 { margin: 0; color: #3f2b1d; font-size: 22px; }
.main-head p { margin: 6px 0 0; color: #8d7865; font-size: 13px; }

.stats-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 10px; }
.stat-card { border-radius: 14px; background: rgba(255,252,246,.94); border: 1px solid rgba(98,61,27,.1); box-shadow: 0 8px 18px rgba(69,43,20,.07); padding: 12px; }
.stat-card span { color: #9b8675; font-size: 12px; }
.stat-card strong { display: block; margin-top: 6px; color: #3f2b1d; font-size: 24px; }

.content-grid { display: grid; grid-template-columns: 1.2fr .8fr; gap: 12px; }
.content-card { border-radius: 16px; background: rgba(255,252,246,.94); border: 1px solid rgba(98,61,27,.1); box-shadow: 0 10px 22px rgba(69,43,20,.08); padding: 14px; }
.content-card h3 { margin: 0 0 10px; color: #3f2b1d; }

.avatar-edit { display: grid; justify-items: center; gap: 6px; margin-bottom: 10px; }
.hidden-input { display: none; }
.avatar-uploader { width: 90px; height: 90px; border: 1px dashed rgba(140,52,23,.38); border-radius: 14px; background: rgba(255,255,255,.72); display: inline-flex; align-items: center; justify-content: center; overflow: hidden; }
.avatar-uploader img { width: 100%; height: 100%; object-fit: cover; }
.plus { font-size: 24px; color: #94674a; line-height: 1; }
.avatar-edit p { margin: 0; font-size: 12px; color: #a08f81; }

.compact-form { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 8px; }
.compact-form label { display: grid; gap: 4px; }
.compact-form label.full { grid-column: 1 / -1; }
.compact-form span { font-size: 12px; color: #8f7d6e; }
.compact-form input, .compact-form textarea,
.field input, .field textarea { width: 100%; border: 1px solid rgba(98,61,27,.16); border-radius: 10px; background: #f9f7f3; padding: 8px 10px; outline: none; }

.notice { margin: 8px 0 0; color: #8c3417; font-size: 12px; }
.form-actions { margin-top: 12px; display: flex; justify-content: center; gap: 10px; }

.btn { min-width: 110px; height: 38px; border-radius: 999px; border: 1px solid transparent; font: inherit; }
.btn.primary { background: linear-gradient(135deg, #bd5b2f, #8c3417); color: #fff; }
.btn.ghost, .btn.outline { background: #f5f2ec; color: #6f6053; border-color: rgba(98,61,27,.2); }
.btn.danger { background: rgba(255,240,240,.9); border-color: rgba(196,81,81,.38); color: #b43f3f; }

.security-card p { margin: 0 0 12px; color: #8d7b6d; line-height: 1.6; }
.list-card { display: grid; gap: 8px; }
.row-item { border: 1px solid rgba(98,61,27,.1); border-radius: 12px; background: rgba(255,255,255,.68); padding: 10px 12px; display: flex; justify-content: space-between; gap: 12px; align-items: center; }
.row-item strong { color: #3f2b1d; }
.row-item p, .row-item span, .row-item small { margin: 4px 0 0; color: #978779; font-size: 13px; }
.empty { color: #9a897b; text-align: center; padding: 16px; }

.merchant-panel { display: grid; gap: 10px; }
.panel-head { display: flex; justify-content: space-between; align-items: center; gap: 12px; }
.panel-head-actions { display: inline-flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.status-chip { display: inline-flex; align-items: center; gap: 8px; padding: 6px 12px; border-radius: 999px; font-size: 12px; border: 1px solid transparent; }
.status-dot { width: 8px; height: 8px; border-radius: 999px; }
.status-chip.certified { background: rgba(68,125,255,.14); color: #2f63d8; border-color: rgba(68,125,255,.28); }
.status-chip.certified .status-dot { background: #3b82f6; }
.status-chip.reviewing { background: rgba(240,157,65,.14); color: #b76a14; border-color: rgba(240,157,65,.28); }
.status-chip.reviewing .status-dot { background: #f59e0b; }
.status-chip.rejected { background: rgba(224,73,73,.14); color: #b63a3a; border-color: rgba(224,73,73,.28); }
.status-chip.rejected .status-dot { background: #ef4444; }

.qualification-view, .qualification-edit { display: grid; gap: 10px; }
.form-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 10px; }
.field { display: grid; gap: 6px; }
.field span { font-size: 13px; color: #8f7d6e; }
.full-row { grid-column: 1 / -1; }
.qualification-grid { display: grid; grid-template-columns: repeat(2, minmax(0,1fr)); gap: 10px; }
.info-card { border: 1px solid rgba(98,61,27,.12); border-radius: 12px; background: rgba(255,255,255,.72); padding: 10px; display: grid; gap: 4px; }
.info-card span { color: #8f7d6e; font-size: 13px; }
.info-card strong { font-size: 14px; font-weight: 600; }
.info-card.full { grid-column: 1 / -1; }
.upload-grid { display: grid; grid-template-columns: repeat(2,minmax(0,1fr)); gap: 10px; }
.upload-card { border: 1px solid rgba(98,61,27,.12); border-radius: 14px; background: rgba(255,255,255,.7); padding: 10px; display: grid; gap: 8px; }
.upload-card h5 { margin: 0; }
.upload-card img { width: 100%; height: 160px; object-fit: cover; border-radius: 12px; }
.upload-empty { margin: 0; color: #8f7d6e; }
.chef-row { display: grid; grid-template-columns: 56px 1fr; gap: 12px; align-items: start; }
.chef-row img { width: 56px; height: 56px; border-radius: 12px; object-fit: cover; }

.dialog-mask { position: fixed; inset: 0; background: rgba(20,20,24,.46); display: grid; place-items: center; z-index: 50; padding: 16px; }
.dialog-card { width: min(620px, 96vw); border-radius: 20px; background: #fffbf5; padding: 16px; box-shadow: 0 18px 34px rgba(20,20,20,.22); }
.dialog-card header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px; }
.dialog-card h4 { margin: 0; }
.dialog-foot { margin-top: 14px; display: flex; justify-content: space-between; align-items: center; }
.avatar-preview { width: 92px; height: 92px; border-radius: 12px; object-fit: cover; border: 1px solid rgba(98,61,27,.12); }

@media (max-width: 960px) {
  .profile-page { grid-template-columns: 1fr; }
  .stats-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .content-grid, .form-grid, .upload-grid, .qualification-grid { grid-template-columns: 1fr; }
  .compact-form { grid-template-columns: 1fr; }
  .row-item { flex-direction: column; align-items: flex-start; }
}
</style>
