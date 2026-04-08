<template>
  <section class="publish-screen">
    <transition name="toast-fade">
      <div v-if="message" :class="messageType === 'success' ? 'toast success-toast' : 'toast error-toast'">{{ message }}</div>
    </transition>

    <article class="publish-hero">
      <div>
        <p class="eyebrow">发布美食</p>
        <h2>分享真实体验，帮助更多人做选择。</h2>
        <p class="hero-text">标题、图片、内容越完整，越容易被看到。</p>
      </div>
    </article>

    <form class="publish-form" @submit.prevent="handleSubmit">
      <section class="form-card">
        <div class="card-header"><h3>基础信息</h3></div>
        <div class="form-grid">
          <label class="field">
            <span>标题</span>
            <input v-model.trim="form.title" type="text" placeholder="例如：这家牛肉面汤头很顶" />
          </label>

          <label class="field">
            <span>分类</span>
            <select v-model="form.categoryId">
              <option value="">请选择分类</option>
              <option v-for="item in categories" :key="item.id" :value="String(item.id)">{{ item.icon ? `${item.icon} ` : '' }}{{ item.name }}</option>
            </select>
          </label>

          <label class="field full-row">
            <span>摘要</span>
            <input v-model.trim="form.summary" type="text" placeholder="一句话概括推荐理由" />
          </label>

          <div class="field full-row location-block">
            <span>定位信息</span>
            <div class="location-row">
              <span class="location-icon" aria-hidden="true">📍</span>
              <ElCascader
                v-model="form.regionCode"
                class="region-cascader"
                :options="regionOptions"
                :props="regionProps"
                clearable
                placeholder="选择省/市/区"
              />
              <input v-model.trim="form.detailAddress" type="text" placeholder="详细街道/门牌号（如：鼓楼东大街 18 号）" />
            </div>
          </div>

          <label class="field">
            <span>人均消费</span>
            <input v-model.number="form.perCapita" type="number" min="0" placeholder="例如：45" />
          </label>
        </div>
      </section>

      <section v-if="isMerchant" class="form-card dish-module">
        <div class="card-header dish-head">
          <h3>菜品与食材（商家必填）</h3>
          <el-button type="primary" class="add-dish-btn" @click="addDish">+ 新增菜品</el-button>
        </div>

        <div v-if="!dishes.length" class="empty-dish">请至少添加一道菜品并完善信息。</div>

        <div class="dish-list-grid">
          <article v-for="(dish, index) in dishes" :key="dish.id" class="dish-card">
            <button type="button" class="remove-dish-icon" @click="removeDish(index)" title="删除菜品">🗑</button>
            <el-form label-position="top" class="dish-form">
              <el-form-item label="菜品名称">
                <el-input v-model.trim="dish.name" placeholder="例如：招牌牛肉面" />
              </el-form-item>

              <el-form-item label="菜品图片（可多张）">
                <el-upload
                  v-model:file-list="dish.files"
                  class="dish-uploader"
                  list-type="picture-card"
                  multiple
                  :limit="9"
                  :http-request="uploadDishImageRequest"
                  :on-success="(response, file) => handleDishUploadSuccess(response, file, dish)"
                  :on-preview="handleDishPreview"
                >
                  <span class="upload-plus">+</span>
                </el-upload>
              </el-form-item>

              <el-form-item label="食材（多选，可输入新增）">
                <el-select
                  v-model="dish.ingredients"
                  multiple
                  filterable
                  allow-create
                  default-first-option
                  reserve-keyword
                  placeholder="请选择或输入食材后回车创建"
                  @change="normalizeDishIngredients(dish)"
                >
                  <el-option v-for="item in ingredientOptions" :key="item" :label="item" :value="item" />
                </el-select>
              </el-form-item>

              <el-form-item label="过敏原">
                <el-select
                  v-model="dish.allergens"
                  multiple
                  filterable
                  placeholder="请选择过敏原标签"
                  @change="normalizeDishAllergens(dish)"
                >
                  <el-option v-for="item in allergenOptions" :key="item" :label="item" :value="item" />
                </el-select>
              </el-form-item>
            </el-form>
          </article>
        </div>

        <el-dialog v-model="previewVisible" title="菜品图片预览" width="560px">
          <img v-if="previewImage" :src="previewImage" alt="dish-preview" class="preview-dialog-image" />
        </el-dialog>
      </section>

      <section v-if="!isMerchant" class="form-card">
        <div class="card-header"><h3>图片上传</h3></div>
        <div class="upload-panel">
          <input ref="fileInputRef" class="hidden-input" type="file" accept="image/*" multiple @change="handleFileChange" />
          <button class="upload-trigger" type="button" @click="openFilePicker">
            <span class="upload-icon">+</span>
            <strong>点击上传图片</strong>
            <small>支持多图上传，单张不超过 25MB</small>
          </button>
        </div>

        <p v-if="uploading" class="hint-text">图片上传中，请稍候...</p>
        <p v-if="uploadError" class="error-text">{{ uploadError }}</p>

        <div v-if="imageList.length" class="preview-grid">
          <figure v-for="item in imageList" :key="item.url" class="preview-card">
            <img :src="toImageUrl(item.url)" :alt="item.fileName" />
            <figcaption>
              <span>{{ item.fileName }}</span>
              <button type="button" @click="removeImage(item.url)">删除</button>
            </figcaption>
          </figure>
        </div>
      </section>

      <section v-else class="form-card">
        <div class="card-header"><h3>帖子图片说明</h3></div>
        <p class="hint-text">商家发布时，帖子图片会自动使用你在每道菜里上传的图片，无需在这里重复上传。</p>
      </section>

      <section class="form-card">
        <div class="card-header"><h3>详细描述</h3></div>
        <label class="field">
          <span>正文</span>
          <textarea v-model.trim="form.content" rows="9" placeholder="可写口味、排队情况、推荐吃法等。" />
        </label>
      </section>

      <div class="submit-bar">
        <button class="submit-btn" type="submit" :disabled="submitting || uploading">{{ submitting ? '发布中...' : '提交发布' }}</button>
      </div>
    </form>
  </section>
</template>

<script setup lang="ts">
import { getApiHost } from '@/config/runtime'
import { computed, onMounted, ref } from 'vue'
import { ElButton, ElCascader, ElDialog, ElForm, ElFormItem, ElInput, ElOption, ElSelect, ElUpload } from 'element-plus'
import type { UploadFile, UploadRequestOptions, UploadUserFile } from 'element-plus'
import { regionData } from 'element-china-area-data'
import { fetchCategoryOptions, type CategoryItem } from '@/api/category'
import { createFoodPost } from '@/api/post'
import { uploadImage } from '@/api/upload'
import { authState } from '@/stores/auth'
import 'element-plus/es/components/cascader/style/css'
import 'element-plus/es/components/button/style/css'
import 'element-plus/es/components/dialog/style/css'
import 'element-plus/es/components/form/style/css'
import 'element-plus/es/components/input/style/css'
import 'element-plus/es/components/select/style/css'
import 'element-plus/es/components/upload/style/css'
import 'element-plus/es/components/scrollbar/style/css'
import 'element-plus/es/components/popper/style/css'

interface RegionNode extends Record<string, unknown> {
  value: string
  label: string
  children?: RegionNode[]
}

interface UploadedImage {
  fileName: string
  url: string
}

interface DishUploadFile extends UploadUserFile {
  rawUrl?: string
}

interface DishFormItem {
  id: number
  name: string
  ingredients: string[]
  allergens: string[]
  files: DishUploadFile[]
}

const API_HOST = getApiHost()

const categories = ref<CategoryItem[]>([])
const fileInputRef = ref<HTMLInputElement | null>(null)
const imageList = ref<UploadedImage[]>([])
const uploading = ref(false)
const submitting = ref(false)
const uploadError = ref('')
let toastTimer: number | null = null
const message = ref('')
const messageType = ref<'success' | 'error'>('success')
const isMerchant = computed(() => authState.user?.role === 2)
const dishes = ref<DishFormItem[]>([])
const previewVisible = ref(false)
const previewImage = ref('')
let dishIdSeed = 1

const ingredientOptions = ['牛肉', '猪肉', '鸡肉', '鱼肉', '虾', '豆腐', '菌菇', '辣椒']
const allergenOptions = ['🥜 花生', '🦐 海鲜', '🥛 乳制品', '🥚 蛋类', '🌾 麸质', '🌰 坚果', '🫘 大豆', '🐟 鱼类']
const regionProps = { value: 'value', label: 'label', children: 'children' }
const regionOptions: RegionNode[] = regionData as unknown as RegionNode[]

const form = ref({
  categoryId: '',
  title: '',
  summary: '',
  content: '',
  regionCode: [] as string[],
  detailAddress: '',
  perCapita: undefined as number | undefined,
})

function toImageUrl(url: string) {
  return url.startsWith('http') ? url : `${API_HOST}${url}`
}

function openFilePicker() {
  fileInputRef.value?.click()
}

function showMessage(type: 'success' | 'error', text: string) {
  messageType.value = type
  message.value = text
  if (toastTimer) window.clearTimeout(toastTimer)
  toastTimer = window.setTimeout(() => {
    message.value = ''
  }, 2600)
}

function removeImage(url: string) {
  imageList.value = imageList.value.filter((item) => item.url !== url)
}

function addDish() {
  dishes.value.push({ id: dishIdSeed++, name: '', ingredients: [], allergens: [], files: [] })
}

function removeDish(index: number) {
  dishes.value.splice(index, 1)
}

async function loadCategories() {
  const response = await fetchCategoryOptions()
  categories.value = response.data
}

async function handleFileChange(event: Event) {
  const input = event.target as HTMLInputElement
  const files = input.files
  uploadError.value = ''
  if (!files?.length) return

  try {
    uploading.value = true
    for (const file of Array.from(files)) {
      const response = await uploadImage(file)
      imageList.value.push(response.data)
    }
  } catch (error) {
    uploadError.value = error instanceof Error ? error.message : '图片上传失败，请稍后重试。'
  } finally {
    uploading.value = false
    if (fileInputRef.value) fileInputRef.value.value = ''
  }
}

async function uploadDishImageRequest(options: UploadRequestOptions) {
  try {
    uploading.value = true
    const file = options.file as File
    const response = await uploadImage(file)
    options.onSuccess?.(response.data)
  } catch (error) {
    options.onError?.(error as unknown as any)
    showMessage('error', error instanceof Error ? error.message : '菜品图片上传失败，请稍后重试。')
  } finally {
    uploading.value = false
  }
}

function handleDishUploadSuccess(response: UploadedImage, file: UploadFile, dish: DishFormItem) {
  const target = dish.files.find((item) => item.uid === file.uid)
  if (!target) return
  target.name = response.fileName || target.name || `dish-${Date.now()}`
  target.rawUrl = response.url
  target.url = toImageUrl(response.url)
}

function handleDishPreview(file: UploadFile) {
  previewImage.value = file.url || ''
  previewVisible.value = !!previewImage.value
}

function getDishImageUrls(dish: DishFormItem) {
  return dish.files
    .map((item) => item.rawUrl)
    .filter((url): url is string => !!url)
}

function normalizeDishIngredients(dish: DishFormItem) {
  const unique = new Set<string>()
  const normalized: string[] = []
  for (const item of dish.ingredients) {
    const text = String(item || '').trim()
    if (!text) continue
    const key = text.toLowerCase()
    if (unique.has(key)) continue
    unique.add(key)
    normalized.push(text)
  }
  dish.ingredients = normalized
}

function normalizeDishAllergens(dish: DishFormItem) {
  const unique = new Set<string>()
  const normalized: string[] = []
  for (const item of dish.allergens) {
    const text = String(item || '').trim()
    if (!text) continue
    if (unique.has(text)) continue
    unique.add(text)
    normalized.push(text)
  }
  dish.allergens = normalized
}

function validateMerchantDishes() {
  if (!isMerchant.value) return true
  if (!dishes.value.length) {
    showMessage('error', '商家发布内容时，至少添加一道菜品。')
    return false
  }
  for (const dish of dishes.value) {
    if (!dish.name.trim()) {
      showMessage('error', '请填写菜品名称。')
      return false
    }
    if (!dish.ingredients.length) {
      showMessage('error', `菜品「${dish.name || '未命名'}」至少选择一个食材。`)
      return false
    }
    if (!getDishImageUrls(dish).length) {
      showMessage('error', `菜品「${dish.name || '未命名'}」请至少上传一张菜品图片。`)
      return false
    }
  }
  return true
}

async function handleSubmit() {
  uploadError.value = ''
  message.value = ''

  if (!form.value.title || !form.value.categoryId || !form.value.content) {
    showMessage('error', '请先完整填写标题、分类和内容描述。')
    return
  }
  if (!isMerchant.value && !imageList.value.length) {
    showMessage('error', '请至少上传一张图片。')
    return
  }
  if (!validateMerchantDishes()) {
    return
  }
  if (!form.value.regionCode.length || !form.value.detailAddress.trim()) {
    showMessage('error', '请填写完整定位信息（省/市/区 + 详细地址）。')
    return
  }

  try {
    submitting.value = true
    const regionNameParts = mapRegionLabels(form.value.regionCode, regionOptions)
    const fullAddress = [...regionNameParts, form.value.detailAddress.trim()].join(' ')
    await createFoodPost({
      categoryId: Number(form.value.categoryId),
      title: form.value.title,
      summary: form.value.summary || undefined,
      content: form.value.content,
      address: fullAddress || undefined,
      detailAddress: form.value.detailAddress.trim() || undefined,
      regionCode: form.value.regionCode,
      perCapita: form.value.perCapita,
      imageUrls: isMerchant.value
        ? Array.from(new Set(dishes.value.flatMap((dish) => getDishImageUrls(dish))))
        : imageList.value.map((item) => item.url),
      dishes: isMerchant.value
        ? dishes.value.map((dish) => ({
            name: dish.name.trim(),
            ingredients: dish.ingredients,
            imageUrls: getDishImageUrls(dish),
            allergens: dish.allergens,
          }))
        : undefined,
    })
    showMessage('success', '发布成功，内容已进入审核流程。')
    form.value = { categoryId: '', title: '', summary: '', content: '', regionCode: [], detailAddress: '', perCapita: undefined }
    imageList.value = []
    dishes.value = []
  } catch (error) {
    showMessage('error', error instanceof Error ? error.message : '发布失败，请稍后重试。')
  } finally {
    submitting.value = false
  }
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

onMounted(async () => {
  try {
    await loadCategories()
    if (isMerchant.value) addDish()
  } catch (error) {
    showMessage('error', error instanceof Error ? error.message : '分类加载失败。')
  }
})
</script>

<style scoped>
.toast { position: sticky; top: 8px; z-index: 10; padding: 14px 18px; border-radius: 18px; box-shadow: var(--shadow); font-weight: 600; }
.success-toast { background: rgba(47, 107, 79, 0.94); color: #fff; }
.error-toast { background: rgba(177, 63, 40, 0.94); color: #fff; }
.toast-fade-enter-active, .toast-fade-leave-active { transition: all .25s ease; }
.toast-fade-enter-from, .toast-fade-leave-to { opacity: 0; transform: translateY(-8px); }
.publish-screen { display: grid; gap: 16px; }
.publish-hero, .form-card { padding: 22px; border: 1px solid var(--line); border-radius: var(--radius-xl); background: rgba(255,252,246,.92); box-shadow: var(--shadow); }
.publish-hero h2 { margin: 0; font-size: clamp(24px,3.4vw,34px); }
.hero-text { color: var(--muted); }
.publish-form { display: grid; gap: 14px; }
.card-header { margin-bottom: 10px; display: flex; justify-content: space-between; align-items: center; gap: 10px; }
.card-header h3 { margin: 0; }
.form-grid { display: grid; grid-template-columns: repeat(2,minmax(0,1fr)); gap: 12px; }
.full-row { grid-column: 1 / -1; }
.field { display: grid; gap: 6px; }
.field span { font-size: 13px; color: var(--muted); }
.field input, .field select, .field textarea { width: 100%; border: 1px solid rgba(98,61,27,.16); border-radius: 12px; padding: 10px 12px; background: #f9f9f9; outline: none; }
.field input, .field select { height: 44px; }
.field textarea { resize: vertical; min-height: 140px; }
.field input:focus, .field select:focus, .field textarea:focus { border-color: rgba(140, 52, 23, .32); box-shadow: 0 0 0 3px rgba(189, 91, 47, .12); }
.location-block { background: rgba(255, 252, 246, .8); border: 1px solid rgba(98,61,27,.12); border-radius: 14px; padding: 12px; }
.location-row { display: grid; grid-template-columns: 26px minmax(190px, .85fr) 1fr; gap: 8px; align-items: center; }
.location-icon { width: 24px; height: 24px; border-radius: 999px; display: inline-flex; align-items: center; justify-content: center; background: rgba(189,91,47,.14); color: var(--brand-deep); font-size: 13px; }
.region-cascader { width: 100%; }
.region-cascader :deep(.el-input__wrapper) { min-height: 44px; border-radius: 12px; padding-inline: 12px; background: #f9f9f9; border: 1px solid rgba(98,61,27,.16); box-shadow: none !important; }
.region-cascader :deep(.el-input__wrapper.is-focus) { border-color: rgba(140, 52, 23, .32); box-shadow: 0 0 0 3px rgba(189, 91, 47, .12) !important; }
.upload-panel { border: 1px dashed rgba(189,91,47,.4); border-radius: 24px; background: linear-gradient(135deg, rgba(255,247,238,.9), rgba(255,252,246,.92)); }
.hidden-input { display: none; }
.upload-trigger { width: 100%; min-height: 140px; border: none; background: transparent; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 8px; }
.upload-icon { display: inline-flex; align-items: center; justify-content: center; width: 46px; height: 46px; border-radius: 50%; background: rgba(189,91,47,.12); color: var(--brand-deep); font-size: 24px; }
.hint-text { color: var(--muted); }
.error-text { color: #c13f2a; }
.preview-grid { display: grid; grid-template-columns: repeat(3,minmax(0,1fr)); gap: 10px; margin-top: 12px; }
.preview-card { margin: 0; overflow: hidden; border-radius: var(--radius-lg); border: 1px solid rgba(98,61,27,.12); background: #fffdfa; }
.preview-card img { width: 100%; height: 150px; object-fit: cover; display: block; }
.preview-card figcaption { display: flex; justify-content: space-between; gap: 8px; padding: 10px; align-items: center; }
.preview-card button { border: none; background: transparent; color: #b13f28; }
.submit-bar { display: flex; justify-content: flex-end; }
.submit-btn { min-width: 140px; height: 44px; border: none; border-radius: 999px; background: linear-gradient(135deg, var(--brand), var(--brand-deep)); color: #fff; font-weight: 600; }
.submit-btn:disabled { opacity: .72; cursor: not-allowed; }
.dish-module { padding-top: 16px; }
.dish-head { align-items: center; margin-bottom: 12px; }
.add-dish-btn { height: 34px; padding-inline: 14px; border-radius: 999px; }
.empty-dish { color: var(--muted); padding: 8px 2px; }
.dish-list-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}
.dish-card {
  position: relative;
  border: 1px solid rgba(190, 168, 142, 0.34);
  border-radius: 8px;
  padding: 14px 14px 8px;
  background: rgba(251, 247, 241, 0.72);
}
.dish-form :deep(.el-form-item) { margin-bottom: 12px; }
.dish-form :deep(.el-form-item__label) { padding-bottom: 4px; font-size: 12px; color: #7f736a; }
.dish-form :deep(.el-input__wrapper),
.dish-form :deep(.el-select__wrapper) {
  border-radius: 8px;
  min-height: 38px;
  box-shadow: none;
  border: 1px solid #e6dfd6;
  background: #fff;
}
.dish-form :deep(.el-input__wrapper.is-focus),
.dish-form :deep(.el-select__wrapper.is-focused) {
  border-color: rgba(140, 52, 23, 0.38);
  box-shadow: 0 0 0 3px rgba(189, 91, 47, 0.1);
}
.dish-form :deep(.el-select__tags) {
  gap: 4px;
}
.dish-form :deep(.el-tag) {
  background: rgba(189, 91, 47, 0.1);
  border-color: rgba(189, 91, 47, 0.28);
  color: #8c3417;
}
.dish-uploader :deep(.el-upload--picture-card),
.dish-uploader :deep(.el-upload-list__item) {
  width: 96px;
  height: 96px;
  border-radius: 8px;
}
.dish-uploader :deep(.el-upload--picture-card) {
  border: 1px dashed #d9cbb7;
  background: #fff;
}
.upload-plus { font-size: 20px; color: #9b7f66; line-height: 1; }
.remove-dish-icon {
  position: absolute;
  right: 10px;
  top: 10px;
  width: 28px;
  height: 28px;
  border-radius: 8px;
  border: 1px solid rgba(224, 73, 73, 0.3);
  background: #fff3f3;
  color: #d14646;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
}
.preview-dialog-image { width: 100%; max-height: 420px; object-fit: contain; display: block; }
@media (max-width: 900px) {
  .publish-hero, .form-grid, .preview-grid { grid-template-columns: 1fr; display: grid; }
  .publish-hero, .form-card { padding: 22px; }
  .location-row { grid-template-columns: 1fr; }
  .location-icon { display: none; }
  .dish-list-grid { grid-template-columns: 1fr; }
  .dish-uploader :deep(.el-upload--picture-card),
  .dish-uploader :deep(.el-upload-list__item) { width: 86px; height: 86px; }
}
</style>


