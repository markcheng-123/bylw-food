import { createRouter, createWebHistory } from 'vue-router'
import { authState, isLoggedIn } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: () => import('@/layouts/AppLayout.vue'),
      children: [
        {
          path: '',
          name: 'home',
          component: () => import('@/views/HomeView.vue'),
        },
        {
          path: 'food',
          name: 'food-list',
          component: () => import('@/views/FoodListView.vue'),
        },
        {
          path: 'food/:id',
          name: 'food-detail',
          component: () => import('@/views/FoodDetailView.vue'),
          props: true,
        },
        {
          path: 'strategies',
          redirect: '/food',
        },
        {
          path: 'strategies/:id',
          redirect: '/food',
        },
        {
          path: 'publish',
          name: 'publish',
          component: () => import('@/views/PublishView.vue'),
          meta: {
            requiresAuth: true,
          },
        },
        {
          path: 'merchant/apply',
          name: 'merchant-apply',
          component: () => import('@/views/MerchantApplyView.vue'),
          meta: {
            requiresAuth: true,
          },
        },
        {
          path: 'profile',
          name: 'profile',
          component: () => import('@/views/ProfileView.vue'),
          meta: {
            requiresAuth: true,
          },
        },
      ],
    },
    {
      path: '/admin',
      component: () => import('@/layouts/AdminLayout.vue'),
      meta: {
        requiresAuth: true,
        requiresAdmin: true,
      },
      redirect: {
        name: 'admin-manage',
      },
      children: [
        {
          path: 'dashboard',
          name: 'admin-dashboard',
          component: () => import('@/views/AdminDashboardView.vue'),
          meta: {
            requiresAuth: true,
            requiresAdmin: true,
            sidebarKey: 'dashboard',
          },
        },
        {
          path: 'manage',
          name: 'admin-manage',
          component: () => import('@/views/AdminConsoleView.vue'),
          meta: {
            requiresAuth: true,
            requiresAdmin: true,
            sidebarKey: 'manage',
          },
        },
        {
          path: 'map',
          name: 'admin-map',
          component: () => import('@/views/AdminMapMonitorView.vue'),
          meta: {
            requiresAuth: true,
            requiresAdmin: true,
            sidebarKey: 'map',
          },
        },
      ],
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
      meta: {
        guestOnly: true,
      },
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/RegisterView.vue'),
      meta: {
        guestOnly: true,
      },
    },
  ],
  scrollBehavior() {
    return { top: 0 }
  },
})

router.beforeEach((to) => {
  if (to.meta.requiresAuth && !isLoggedIn()) {
    return {
      path: '/login',
      query: {
        redirect: to.fullPath,
      },
    }
  }

  if (to.meta.guestOnly && isLoggedIn()) {
    return authState.user?.role === 9 ? '/admin/manage' : '/'
  }

  if (to.meta.requiresAdmin && authState.user?.role !== 9) {
    return '/'
  }

  return true
})

export default router
