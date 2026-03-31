<template>
  <section class="merchant-screen">
    <article class="merchant-hero">
      <div>
        <p class="eyebrow">Merchant Entry</p>
        <h2>商家入驻申请</h2>
        <p>提交后由管理员审核，通过后将自动开通商家身份，并同步进入后台地图监控统计。</p>
      </div>
    </article>

    <section v-if="hasApproved" class="certified-panel">
      <h3>已认证商家</h3>
      <p>你的商家身份已审核通过，无需重复提交。</p>
      <p v-if="latestApprovedApplication">
        当前认证主体：{{ latestApprovedApplication.merchantName }}（{{ latestApprovedApplication.contactName }}）
      </p>
    </section>

    <form v-else class="merchant-form" @submit.prevent="handleSubmit">
      <input v-model.trim="form.merchantName" type="text" placeholder="商家名称" />
      <input v-model.trim="form.contactName" type="text" placeholder="联系人" />
      <input v-model.trim="form.contactPhone" type="text" placeholder="联系电话" />

      <div class="region-row">
        <select v-model="form.province">
          <option value="">请选择省份</option>
          <option v-for="item in provinceOptions" :key="item" :value="item">{{ item }}</option>
        </select>
        <input v-model.trim="form.detailAddress" type="text" placeholder="详细地址（街道/门牌号）" />
      </div>
      <p v-if="fullAddressPreview" class="address-preview">提交地址：{{ fullAddressPreview }}</p>

      <div class="license-upload">
        <input ref="licenseInputRef" class="hidden-input" type="file" accept="image/*" @change="handleLicenseUpload" />
        <button class="upload-trigger" type="button" :disabled="licenseUploading" @click="openLicensePicker">
          {{ licenseUploading ? '上传中...' : form.businessLicense ? '重新上传营业执照' : '上传营业执照（可选）' }}
        </button>
        <p v-if="form.businessLicense" class="license-ok">已上传：{{ licenseFileName || '营业执照图片' }}</p>
        <p v-if="licenseUploadError" class="license-err">{{ licenseUploadError }}</p>
      </div>

      <textarea v-model.trim="form.description" rows="4" placeholder="申请说明（可选）" />
      <button class="primary-btn" type="submit" :disabled="submitting">{{ submitting ? '提交中...' : '提交申请' }}</button>
    </form>

    <p v-if="notice" :class="noticeType === 'success' ? 'notice success' : 'notice error'">{{ notice }}</p>

    <section class="history-panel">
      <h3>我的申请记录</h3>
      <div class="list">
        <article v-for="item in applications" :key="item.id" class="list-item">
          <div>
            <strong>{{ item.merchantName }}</strong>
            <p>{{ item.contactName }} · {{ item.contactPhone }} · {{ formatDate(item.createdAt) }}</p>
            <p class="record-address">📍 {{ item.address }}</p>
          </div>
          <div class="status-col">
            <span class="status">{{ statusText(item.status) }}</span>
            <small v-if="item.rejectReason">{{ item.rejectReason }}</small>
          </div>
        </article>
      </div>
      <p v-if="!applications.length" class="empty">暂时没有申请记录。</p>
    </section>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { fetchMyMerchantApplications, submitMerchantApplication } from '@/api/merchant'
import type { MerchantApplicationItem } from '@/api/admin'
import { uploadImage } from '@/api/upload'

const provinceOptions = [
  '北京市', '天津市', '上海市', '重庆市',
  '河北省', '山西省', '辽宁省', '吉林省', '黑龙江省',
  '江苏省', '浙江省', '安徽省', '福建省', '江西省', '山东省',
  '河南省', '湖北省', '湖南省', '广东省', '海南省',
  '四川省', '贵州省', '云南省', '陕西省', '甘肃省', '青海省',
  '台湾省', '内蒙古自治区', '广西壮族自治区', '宁夏回族自治区',
  '新疆维吾尔自治区', '西藏自治区', '香港特别行政区', '澳门特别行政区',
]

const form = reactive({
  merchantName: '',
  contactName: '',
  contactPhone: '',
  businessLicense: '',
  province: '',
  detailAddress: '',
  description: '',
})
const submitting = ref(false)
const applications = ref<MerchantApplicationItem[]>([])
const notice = ref('')
const noticeType = ref<'success' | 'error'>('success')
const licenseInputRef = ref<HTMLInputElement | null>(null)
const licenseUploading = ref(false)
const licenseUploadError = ref('')
const licenseFileName = ref('')
const hasApproved = computed(() => applications.value.some((item) => item.status === 1))
const latestApprovedApplication = computed(() => applications.value.find((item) => item.status === 1))
const fullAddressPreview = computed(() => [form.province, form.detailAddress.trim()].filter(Boolean).join(' '))

function formatDate(value: string) {
  return value.replace('T', ' ').slice(0, 16)
}

function statusText(status: number) {
  if (status === 1) return '已通过'
  if (status === 2) return '已驳回'
  return '待审核'
}

async function loadApplications() {
  const response = await fetchMyMerchantApplications()
  applications.value = response.data
}

async function handleSubmit() {
  notice.value = ''
  if (!form.merchantName || !form.contactName || !form.contactPhone) {
    noticeType.value = 'error'
    notice.value = '请先填写商家名称、联系人和联系电话。'
    return
  }
  if (!form.province || !form.detailAddress.trim()) {
    noticeType.value = 'error'
    notice.value = '请先选择省份并填写详细地址。'
    return
  }

  try {
    submitting.value = true
    await submitMerchantApplication({
      merchantName: form.merchantName,
      contactName: form.contactName,
      contactPhone: form.contactPhone,
      businessLicense: form.businessLicense || undefined,
      province: form.province,
      detailAddress: form.detailAddress.trim(),
      address: fullAddressPreview.value,
      description: form.description || undefined,
    })
    form.merchantName = ''
    form.contactName = ''
    form.contactPhone = ''
    form.businessLicense = ''
    form.province = ''
    form.detailAddress = ''
    form.description = ''
    licenseFileName.value = ''
    noticeType.value = 'success'
    notice.value = '申请提交成功，请等待管理员审核。'
    await loadApplications()
  } catch (error) {
    noticeType.value = 'error'
    notice.value = error instanceof Error ? error.message : '提交失败，请稍后重试。'
  } finally {
    submitting.value = false
  }
}

function openLicensePicker() {
  licenseInputRef.value?.click()
}

async function handleLicenseUpload(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  licenseUploadError.value = ''
  if (!file) return

  try {
    licenseUploading.value = true
    const response = await uploadImage(file)
    form.businessLicense = response.data.url
    licenseFileName.value = response.data.fileName
  } catch (error) {
    licenseUploadError.value = error instanceof Error ? error.message : '营业执照上传失败，请重试。'
  } finally {
    licenseUploading.value = false
    if (licenseInputRef.value) licenseInputRef.value.value = ''
  }
}

onMounted(loadApplications)
</script>

<style scoped>
.merchant-screen {
  display: grid;
  gap: 20px;
}

.merchant-hero,
.merchant-form,
.certified-panel,
.history-panel {
  padding: 24px;
  border: 1px solid var(--line);
  border-radius: var(--radius-xl);
  background: rgba(255, 252, 246, 0.92);
  box-shadow: var(--shadow);
}

.merchant-hero h2 {
  margin: 0;
  font-size: clamp(30px, 4vw, 44px);
}

.merchant-hero p:last-child,
.list-item p,
.empty {
  color: var(--muted);
}

.merchant-form {
  display: grid;
  gap: 10px;
}

.certified-panel h3 {
  margin: 0;
}

.certified-panel p {
  margin: 8px 0 0;
  color: var(--muted);
}

.merchant-form input,
.merchant-form select,
.merchant-form textarea {
  border: 1px solid rgba(98, 61, 27, 0.16);
  border-radius: 14px;
  padding: 12px 14px;
  background: #fffdfa;
  font: inherit;
}

.region-row {
  display: grid;
  grid-template-columns: 220px 1fr;
  gap: 8px;
}

.address-preview {
  margin: 0;
  font-size: 13px;
  color: #77685c;
}

.hidden-input {
  display: none;
}

.license-upload {
  display: grid;
  gap: 8px;
  padding: 12px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.66);
  border: 1px dashed rgba(189, 91, 47, 0.35);
}

.upload-trigger {
  height: 44px;
  border: none;
  border-radius: 12px;
  background: rgba(189, 91, 47, 0.14);
  color: var(--brand-deep);
}

.license-ok {
  margin: 0;
  color: #246244;
}

.license-err {
  margin: 0;
  color: #9e3119;
}

.notice {
  margin: 0;
  padding: 12px 14px;
  border-radius: 12px;
  border: 1px solid transparent;
}

.notice.success {
  background: rgba(47, 107, 79, 0.12);
  border-color: rgba(47, 107, 79, 0.25);
  color: #246244;
}

.notice.error {
  background: rgba(177, 63, 40, 0.12);
  border-color: rgba(177, 63, 40, 0.25);
  color: #9e3119;
}

.list {
  display: grid;
  gap: 12px;
  margin-top: 14px;
}

.list-item {
  display: flex;
  justify-content: space-between;
  gap: 14px;
  align-items: center;
  padding: 14px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.72);
}

.record-address {
  margin-top: 4px;
  color: #6f5f52;
}

.status-col {
  display: grid;
  justify-items: end;
  gap: 4px;
}

.status {
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(189, 91, 47, 0.12);
  color: var(--brand-deep);
}

@media (max-width: 900px) {
  .list-item {
    display: grid;
    grid-template-columns: 1fr;
  }

  .status-col {
    justify-items: start;
  }

  .region-row {
    grid-template-columns: 1fr;
  }
}
</style>
