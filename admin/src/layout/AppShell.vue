<script setup lang="ts">
import { computed, ref } from 'vue'
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/features/login/stores/authStore'
import Logo from '@/features/landing/components/ui/Logo.vue'
import BaseIcon from '@/features/landing/components/ui/BaseIcon.vue'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()

const open = ref(false) // sidebar mobile

interface NavChild {
  label: string
  path: string
  statut?: string
}
interface NavItem {
  label: string
  icon: string
  path?: string
  children?: NavChild[]
  section?: boolean
}

const nav = computed<NavItem[]>(() => {
  const base: NavItem[] = [
    { label: 'Tableau de bord', icon: 'dashboard', path: '/dashboard' },
    {
      label: 'Demandes',
      icon: 'inbox',
      children: [
        { label: 'Toutes', path: '/demandes' },
        { label: 'Nouvelles', path: '/demandes', statut: 'NOUVELLE' },
        { label: 'Qualifiées', path: '/demandes', statut: 'QUALIFIEE' },
        { label: 'Gagnées', path: '/demandes', statut: 'GAGNEE' },
      ],
    },
    { label: 'Reporting', icon: 'insights', path: '/reporting' },
    { label: 'Assistant', icon: 'forum', path: '/chat' },
    { label: 'Partager le formulaire', icon: 'share', path: '/partager' },
  ]
  if (auth.user?.role === 'ADMIN') {
    base.push(
      { label: 'Administration', icon: '', section: true },
      { label: 'Utilisateurs', icon: 'group', path: '/admin/utilisateurs' },
      { label: 'Grille de référence', icon: 'request_quote', path: '/admin/grille' },
      { label: 'Intégration Jira', icon: 'integration_instructions', path: '/admin/jira' },
    )
  }
  return base
})

// Un groupe est ouvert si l'un de ses enfants est actif (ou déplié manuellement).
const manual = ref<Record<string, boolean>>({})
function toggle(label: string) {
  manual.value[label] = !isExpanded(label)
}
function childActive(c: NavChild): boolean {
  if (route.path !== c.path) return false
  const q = (route.query.statut as string) || ''
  return (c.statut ?? '') === q
}
function groupActive(item: NavItem): boolean {
  return !!item.children?.some(childActive)
}
function isExpanded(label: string): boolean {
  const item = nav.value.find((n) => n.label === label)
  if (item && groupActive(item)) return true
  return manual.value[label] ?? false
}

async function onLogout() {
  await auth.logout()
  router.push('/login')
}
</script>

<template>
  <div
    class="app-fonts h-screen flex overflow-hidden bg-page-bg text-text"
    :style="{
      '--font-body': `'FriendlySans', system-ui, sans-serif`,
      '--font-display': `'FriendlySans', system-ui, sans-serif`,
      fontFamily: `var(--font-body)`,
    }"
  >
    <!-- Overlay mobile -->
    <div v-if="open" class="fixed inset-0 bg-black/30 z-30 lg:hidden" @click="open = false"></div>

    <!-- Sidebar -->
    <aside
      class="fixed lg:sticky top-0 z-40 h-screen w-[260px] shrink-0 bg-white-card border-r border-line flex flex-col transition-transform duration-300 lg:translate-x-0"
      :class="open ? 'translate-x-0' : '-translate-x-full'"
    >
      <div class="h-16 flex items-center px-5 border-b border-line">
        <RouterLink to="/dashboard" @click="open = false"><Logo /></RouterLink>
      </div>

      <nav class="flex-1 overflow-y-auto p-3 space-y-1">
        <template v-for="item in nav" :key="item.label">
          <!-- Intitulé de section -->
          <p
            v-if="item.section"
            class="px-3 pt-5 pb-1 text-[10px] font-bold uppercase tracking-wider text-muted"
          >
            {{ item.label }}
          </p>

          <!-- Lien simple -->
          <RouterLink
            v-else-if="item.path"
            :to="item.path"
            class="flex items-center gap-3 px-3 py-2.5 rounded-xl text-[14px] font-medium text-text/80 hover:bg-soft-card transition-colors"
            active-class="!bg-soft-card !text-text font-semibold"
            @click="open = false"
          >
            <BaseIcon :name="item.icon" class="text-[20px]" />
            {{ item.label }}
          </RouterLink>

          <!-- Groupe avec sous-menu -->
          <div v-else>
            <button
              class="w-full flex items-center gap-3 px-3 py-2.5 rounded-xl text-[14px] font-medium text-text/80 hover:bg-soft-card transition-colors"
              :class="groupActive(item) ? 'text-text font-semibold' : ''"
              @click="toggle(item.label)"
            >
              <BaseIcon :name="item.icon" class="text-[20px]" />
              <span class="flex-1 text-left">{{ item.label }}</span>
              <BaseIcon
                name="expand_more"
                class="text-[18px] transition-transform"
                :class="isExpanded(item.label) ? 'rotate-180' : ''"
              />
            </button>
            <div v-show="isExpanded(item.label)" class="mt-1 ml-4 pl-4 border-l border-line space-y-0.5">
              <RouterLink
                v-for="c in item.children"
                :key="c.label + (c.statut ?? '')"
                :to="c.statut ? { path: c.path, query: { statut: c.statut } } : { path: c.path }"
                class="block px-3 py-2 rounded-lg text-[13px] text-text/70 hover:bg-soft-card hover:text-text transition-colors"
                :class="childActive(c) ? 'bg-soft-card text-text font-semibold' : ''"
                @click="open = false"
              >
                {{ c.label }}
              </RouterLink>
            </div>
          </div>
        </template>
      </nav>

      <!-- Utilisateur + logout -->
      <div class="border-t border-line p-3">
        <div class="flex items-center gap-3 px-2 py-2">
          <span class="w-9 h-9 rounded-full bg-gradient-to-tr from-brand-from to-brand-to flex items-center justify-center text-white font-bold">
            {{ (auth.user?.email ?? '?').charAt(0).toUpperCase() }}
          </span>
          <div class="min-w-0">
            <p class="text-[13px] font-semibold truncate">{{ auth.user?.email ?? '—' }}</p>
            <p class="text-[11px] text-muted">{{ auth.user?.role ?? '' }}</p>
          </div>
        </div>
        <button
          class="mt-1 w-full flex items-center gap-3 px-3 py-2.5 rounded-xl text-[14px] font-medium text-text/70 hover:bg-soft-card transition-colors"
          @click="onLogout"
        >
          <BaseIcon name="logout" class="text-[20px]" />
          Se déconnecter
        </button>
      </div>
    </aside>

    <!-- Contenu -->
    <div class="flex-1 min-w-0 flex flex-col">
      <!-- Top bar mobile -->
      <header class="lg:hidden h-16 flex items-center gap-3 px-4 border-b border-line bg-white-card">
        <button class="p-2 -ml-2" aria-label="Menu" @click="open = true">
          <BaseIcon name="menu" class="text-[24px]" />
        </button>
        <Logo :wordmark="false" />
      </header>

      <div class="flex-1 min-h-0 overflow-y-auto">
        <RouterView />
      </div>
    </div>
  </div>
</template>
