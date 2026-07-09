import { createRouter, createWebHistory } from 'vue-router'
import  { authRoutes } from '@/features/login/router/authRoutes'
import { useAuthStore } from '@/features/login/stores/authStore'


const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('@/features/landing/views/Home.vue'),
    },
    ...authRoutes,
  ],
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if(to.meta.requiresAuth && !auth.isAuthenticated) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if (to.meta.guestOnly && auth.isAuthenticated) {
    return { name: 'dashboard' }
  }
})
export default router
