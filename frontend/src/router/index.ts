import { createRouter, createWebHistory } from 'vue-router'
import { authState, isLoggedIn } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: () => import('@/layouts/user/AppLayout.vue'),
      children: [
        {
          path: '',
          name: 'home',
          component: () => import('@/views/user/HomeView.vue'),
        },
        {
          path: 'food',
          name: 'food-list',
          component: () => import('@/views/user/FoodListView.vue'),
        },
        {
          path: 'food/:id',
          name: 'food-detail',
          component: () => import('@/views/user/FoodDetailView.vue'),
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
          component: () => import('@/views/user/PublishView.vue'),
          meta: {
            requiresAuth: true,
          },
        },
        {
          path: 'merchant/apply',
          name: 'merchant-apply',
          component: () => import('@/views/user/MerchantApplyView.vue'),
          meta: {
            requiresAuth: true,
          },
        },
        {
          path: 'profile',
          name: 'profile',
          component: () => import('@/views/user/ProfileView.vue'),
          meta: {
            requiresAuth: true,
          },
        },
      ],
    },
    {
      path: '/admin',
      component: () => import('@/layouts/admin/AdminLayout.vue'),
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
          component: () => import('@/views/admin/AdminDashboardView.vue'),
          meta: {
            requiresAuth: true,
            requiresAdmin: true,
            sidebarKey: 'dashboard',
          },
        },
        {
          path: 'manage',
          name: 'admin-manage',
          component: () => import('@/views/admin/AdminConsoleView.vue'),
          meta: {
            requiresAuth: true,
            requiresAdmin: true,
            sidebarKey: 'manage',
          },
        },
        {
          path: 'map',
          name: 'admin-map',
          component: () => import('@/views/admin/AdminMapMonitorView.vue'),
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
      component: () => import('@/views/auth/LoginView.vue'),
      meta: {
        guestOnly: true,
      },
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/auth/RegisterView.vue'),
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
