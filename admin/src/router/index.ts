import { createRouter, createWebHistory } from 'vue-router'
import { authRoutes } from '@/features/login/router/authRoutes'
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

    // --- Espace connecté : sidebar + pages internes ---
    {
      path: '/',
      component: () => import('@/layout/AppShell.vue'),
      meta: { requiresAuth: true },
      children: [
        {
          path: 'dashboard',
          name: 'dashboard',
          component: () => import('@/features/dashboard/views/Dashboard.vue'),
        },
        {
          path: 'demandes',
          name: 'demandes',
          component: () => import('@/features/demandes/views/DemandesList.vue'),
        },
        {
          path: 'demandes/:id',
          name: 'demande-detail',
          component: () => import('@/features/demandes/views/DemandeDetail.vue'),
          props: true,
        },
        {
          path: 'reporting',
          name: 'reporting',
          component: () => import('@/features/reporting/views/Reporting.vue'),
        },
        {
          path: 'projets/:id',
          name: 'projet-taches',
          component: () => import('@/features/jira/views/ProjetTaches.vue'),
          props: true,
        },
        {
          path: 'admin/utilisateurs',
          name: 'admin-utilisateurs',
          component: () => import('@/features/admin/views/Utilisateurs.vue'),
          meta: { roles: ['ADMIN'] },
        },
        {
          path: 'admin/grille',
          name: 'admin-grille',
          component: () => import('@/features/admin/views/Grille.vue'),
          meta: { roles: ['ADMIN'] },
        },
        {
          path: 'admin/jira',
          name: 'admin-jira',
          component: () => import('@/features/jira/views/JiraConfig.vue'),
          meta: { roles: ['ADMIN'] },
        },
      ],
    },

    // Catch-all : doit rester en DERNIER.
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: () => import('@/features/landing/views/NotFound.vue'),
    },
  ],
})

router.beforeEach((to) => {
  const auth = useAuthStore()

  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if (to.meta.guestOnly && auth.isAuthenticated) {
    return { name: 'dashboard' }
  }
  const roles = to.meta.roles as string[] | undefined
  if (roles && !roles.includes(auth.user?.role ?? '')) {
    return { name: 'dashboard' }
  }
})

export default router
