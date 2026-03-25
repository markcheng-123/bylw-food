import { computed, reactive } from 'vue'

type AdminTheme = 'dark' | 'light'
type AdminLocale = 'zh' | 'en'

const THEME_KEY = 'bylw_admin_theme'
const LOCALE_KEY = 'bylw_admin_locale'

const messages = {
  zh: {
    adminWorkspace: '后台控制中心',
    adminAccount: '管理员账号',
    systemAdmin: '系统后台管理员',
    systemStatus: '系统状态',
    running: '运行中',
    pendingReviews: '待处理审核',
    logout: '退出登录',
    pendingPosts: '待审核内容',
    pendingApplications: '待审核申请',
    manage: '后台管理',
    manageDesc: '审核内容、处理申请',
    dashboard: '数据概览',
    dashboardDesc: '查看运营与审核统计',
    map: '地图监控',
    mapDesc: '查看区域热度与状态',
    darkMode: '深色模式',
    lightMode: '浅色模式',
    switchTheme: '切换主题',
    manageTitle: '后台管理',
    dashboardTitle: '数据概览',
    mapTitle: '地图监控',
    dashboardHeroTitle: '数据中心',
    manageHeroTitle: '后台管理工作台',
    mapHeroTitle: '中国美食分布监控',
    contentTotal: '内容总数',
    merchantApplications: '商家申请',
    pendingMerchants: '待审核商家',
    needReview: '建议优先处理',
    historicalApplications: '含历史申请',
    needMerchantReview: '需要审核资料',
    trend: '近 7 日活跃趋势',
    categoryShare: '美食分类占比',
    merchantCities: '城市商家数量',
    newUsers: '新增用户',
    newPosts: '新增美食发布',
    highlightedProvinces: '高亮省份',
    settledMerchants: '入驻商家',
    mapInteractive: '支持省份高亮、悬停提示和点击查看该省入驻商家 Top5。',
    contentReview: '内容审核',
    merchantReview: '商家审核',
    categoryManage: '分类管理',
    statusFilter: '状态筛选',
    all: '全部',
    pending: '待审核',
    approved: '已通过',
    rejected: '已驳回',
    summary: '摘要',
    address: '推荐地址',
    perCapita: '人均消费',
    contentBody: '正文内容',
    coverMissing: '未上传封面图',
    reviewRemark: '审核说明',
    approveAction: '审核通过',
    rejectContent: '驳回内容',
    merchantAddress: '商家地址',
    submittedAt: '提交时间',
    merchantIntro: '商家简介',
    lastRejectReason: '上次驳回原因',
    licenseMissing: '未上传营业执照',
    reviewReason: '审核说明 / 驳回原因',
    rejectApplication: '驳回申请',
    categoryName: '分类名称',
    iconOptional: '图标（可选）',
    sortValue: '排序值',
    updateCategory: '更新分类',
    createCategory: '新增分类',
    enabled: '启用中',
    disabled: '已停用',
    edit: '编辑',
    disable: '停用',
    enable: '启用',
    top5Merchants: '入驻商家排行',
    close: '关闭',
    backendFields: '后端字段',
    chinaMap: '美食分布实时地图',
  },
  en: {
    adminWorkspace: 'Admin Control Center',
    adminAccount: 'Administrator',
    systemAdmin: 'System Administrator',
    systemStatus: 'System Status',
    running: 'Running',
    pendingReviews: 'Pending Reviews',
    logout: 'Sign Out',
    pendingPosts: 'Pending Posts',
    pendingApplications: 'Pending Applications',
    manage: 'Back Office',
    manageDesc: 'Review content and applications',
    dashboard: 'Data Center',
    dashboardDesc: 'Monitor analytics and review stats',
    map: 'Map Monitor',
    mapDesc: 'Watch regional heat and status',
    darkMode: 'Dark Mode',
    lightMode: 'Light Mode',
    switchTheme: 'Switch Theme',
    manageTitle: 'Back Office',
    dashboardTitle: 'Data Center',
    mapTitle: 'Map Monitor',
    dashboardHeroTitle: 'Data Center',
    manageHeroTitle: 'Back Office Workspace',
    mapHeroTitle: 'China Food Distribution Monitor',
    contentTotal: 'Total Posts',
    merchantApplications: 'Merchant Applications',
    pendingMerchants: 'Pending Merchants',
    needReview: 'Need priority review',
    historicalApplications: 'Includes history',
    needMerchantReview: 'Need document review',
    trend: '7-Day Activity Trend',
    categoryShare: 'Category Share',
    merchantCities: 'Merchants by City',
    newUsers: 'New Users',
    newPosts: 'New Posts',
    highlightedProvinces: 'Highlighted Provinces',
    settledMerchants: 'Settled Merchants',
    mapInteractive: 'Supports province highlight, hover tooltip and click to view Top 5 merchants.',
    contentReview: 'Post Review',
    merchantReview: 'Merchant Review',
    categoryManage: 'Category Manage',
    statusFilter: 'Status Filter',
    all: 'All',
    pending: 'Pending',
    approved: 'Approved',
    rejected: 'Rejected',
    summary: 'Summary',
    address: 'Address',
    perCapita: 'Per Capita',
    contentBody: 'Content',
    coverMissing: 'No cover image',
    reviewRemark: 'Review Remark',
    approveAction: 'Approve',
    rejectContent: 'Reject Post',
    merchantAddress: 'Merchant Address',
    submittedAt: 'Submitted At',
    merchantIntro: 'Merchant Intro',
    lastRejectReason: 'Last Reject Reason',
    licenseMissing: 'No license image',
    reviewReason: 'Review Remark / Reject Reason',
    rejectApplication: 'Reject Application',
    categoryName: 'Category Name',
    iconOptional: 'Icon (optional)',
    sortValue: 'Sort Value',
    updateCategory: 'Update Category',
    createCategory: 'Create Category',
    enabled: 'Enabled',
    disabled: 'Disabled',
    edit: 'Edit',
    disable: 'Disable',
    enable: 'Enable',
    top5Merchants: 'Top 5 Merchants',
    close: 'Close',
    backendFields: 'Backend Fields',
    chinaMap: 'Real-time Food Distribution Map',
  },
} as const

function loadTheme(): AdminTheme {
  const value = localStorage.getItem(THEME_KEY)
  return value === 'light' ? 'light' : 'dark'
}

function loadLocale(): AdminLocale {
  const value = localStorage.getItem(LOCALE_KEY)
  return value === 'en' ? 'en' : 'zh'
}

export const adminUiState = reactive({
  theme: loadTheme(),
  locale: loadLocale(),
})

export function useAdminUi() {
  const isLight = computed(() => adminUiState.theme === 'light')
  const modeLabel = computed(() => (isLight.value ? messages[adminUiState.locale].darkMode : messages[adminUiState.locale].lightMode))

  function t(key: keyof typeof messages.zh) {
    return messages[adminUiState.locale][key]
  }

  function toggleTheme() {
    adminUiState.theme = adminUiState.theme === 'dark' ? 'light' : 'dark'
    localStorage.setItem(THEME_KEY, adminUiState.theme)
  }

  function toggleLocale() {
    adminUiState.locale = adminUiState.locale === 'zh' ? 'en' : 'zh'
    localStorage.setItem(LOCALE_KEY, adminUiState.locale)
  }

  function setLocale(locale: AdminLocale) {
    adminUiState.locale = locale
    localStorage.setItem(LOCALE_KEY, adminUiState.locale)
  }

  return {
    adminUiState,
    isLight,
    modeLabel,
    t,
    toggleTheme,
    toggleLocale,
    setLocale,
  }
}
