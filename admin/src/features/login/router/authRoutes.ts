import type { RouteRecordRaw } from 'vue-router'

export const authRoutes: RouteRecordRaw[] = [
  { path: '/login', name: 'login', component: () => import('../views/Login.vue'), meta: { guestOnly: true } },
  { path: '/register', name: 'register', component: () => import('../views/Register.vue'), meta: { guestOnly: true } },
]