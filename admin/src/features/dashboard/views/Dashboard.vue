<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { mdiArrowRight } from '@mdi/js'
import MdiIcon from '@/features/landing/components/ui/MdiIcon.vue'
import { useAuthStore } from '@/features/login/stores/authStore'
import { getStatistiques } from '@/features/reporting/services/statsService'
import { listDemandes } from '@/features/demandes/services/demandeService'
import type { Statistiques } from '@/features/demandes/types'
import type { DemandeListItem, StatutDemande } from '@/features/demandes/types'

const auth = useAuthStore()

const stats = ref<Statistiques | null>(null)
const statsError = ref(false)
const recent = ref<DemandeListItem[]>([])
const recentLoading = ref(true)

onMounted(async () => {
  // KPI — indépendant, échoue en silence si l'API n'est pas joignable.
  getStatistiques()
    .then((s) => (stats.value = s))
    .catch(() => (statsError.value = true))

  // Demandes récentes.
  try {
    const all = await listDemandes()
    recent.value = all.slice(0, 5)
  } catch {
    recent.value = []
  } finally {
    recentLoading.value = false
  }
})

const kpis = computed(() => {
  const s = stats.value
  return [
    { label: 'Demandes', value: s?.totalDemandes ?? '—' },
    { label: 'Nouvelles', value: s?.nbNouvelles ?? '—' },
    { label: 'Devis envoyés', value: s?.nbDevisEnvoyes ?? '—' },
    { label: 'Taux de conversion', value: s ? `${s.tauxConversion} %` : '—' },
  ]
})

// Lien partageable du formulaire vivant (clé entreprise dérivée de l'email en attendant le backend).
const formLink = computed(() => {
  const domain = auth.user?.email?.split('@')[1]?.split('.')[0] ?? 'demo'
  return `https://form.smartesn.com/?ent=ent_pub_${domain}`
})
const copied = ref(false)
function copier() {
  navigator.clipboard?.writeText(formLink.value).then(() => {
    copied.value = true
    setTimeout(() => (copied.value = false), 2000)
  })
}
const mailto = computed(
  () =>
    `mailto:?subject=${encodeURIComponent('Décrivez votre projet — SmartESN')}` +
    `&body=${encodeURIComponent('Bonjour,\n\nMerci de décrire votre besoin via ce formulaire :\n' + formLink.value + '\n\nBien à vous.')}`,
)

function badgeClass(st: StatutDemande): string {
  const map: Record<StatutDemande, string> = {
    NOUVELLE: 'bg-soft-card text-text',
    EN_ANALYSE: 'bg-sky-100 text-sky-700',
    QUALIFIEE: 'bg-amber-100 text-amber-700',
    DEVIS_ENVOYE: 'bg-indigo-100 text-indigo-700',
    EN_NEGOCIATION: 'bg-purple-100 text-purple-700',
    GAGNEE: 'bg-emerald-100 text-emerald-700',
    PERDUE: 'bg-red-100 text-red-600',
  }
  return map[st]
}
</script>

<template>
  <section class="px-margin py-10 max-w-[1100px] mx-auto">
    <h1 class="text-3xl font-bold tracking-tight mb-1">Tableau de bord</h1>
    <p class="text-muted mb-8">Bonjour {{ auth.user?.email ?? '' }} — voici l'essentiel.</p>

    <!-- KPI -->
    <div class="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-4">
      <div v-for="k in kpis" :key="k.label" class="rounded-2xl border border-line bg-white-card p-5">
        <p class="text-[11px] font-bold uppercase tracking-wider text-muted mb-1">{{ k.label }}</p>
        <p class="text-2xl font-bold">{{ k.value }}</p>
      </div>
    </div>
    <p v-if="statsError" class="text-[13px] text-muted mb-8">Statistiques indisponibles (API non joignable).</p>
    <div v-else class="mb-8"></div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <!-- Demandes récentes -->
      <div class="lg:col-span-2 rounded-2xl border border-line bg-white-card p-6">
        <div class="flex items-center justify-between mb-4">
          <h2 class="text-lg font-bold">Demandes récentes</h2>
          <RouterLink to="/demandes" class="inline-flex items-center gap-1 text-[13px] font-bold text-text hover:text-brand-from">Tout voir <MdiIcon :path="mdiArrowRight" /></RouterLink>
        </div>

        <p v-if="recentLoading" class="text-muted py-6 text-center">Chargement…</p>
        <p v-else-if="!recent.length" class="text-muted py-6 text-center">Aucune demande.</p>
        <ul v-else class="divide-y divide-line/60">
          <li v-for="d in recent" :key="d.id">
            <RouterLink :to="`/demandes/${d.id}`" class="flex items-center justify-between gap-3 py-3 hover:opacity-80">
              <div class="min-w-0">
                <p class="font-medium truncate">{{ d.clientNom }}</p>
                <p class="text-[13px] text-muted truncate">{{ d.description }}</p>
              </div>
              <span class="shrink-0 rounded-full px-2.5 py-1 text-[10px] font-bold" :class="badgeClass(d.statut)">{{ d.statut }}</span>
            </RouterLink>
          </li>
        </ul>
      </div>

      <!-- Raccourcis -->
      <div class="rounded-2xl border border-line bg-white-card p-6">
        <h2 class="text-lg font-bold mb-4">Raccourcis</h2>
        <div class="flex flex-col gap-2">
          <RouterLink to="/demandes" class="rounded-xl border border-line px-4 py-3 font-medium hover:bg-soft-card transition-colors">Voir les demandes</RouterLink>
          <RouterLink to="/reporting" class="rounded-xl border border-line px-4 py-3 font-medium hover:bg-soft-card transition-colors">Reporting</RouterLink>
          <RouterLink to="/chat" class="rounded-xl border border-line px-4 py-3 font-medium hover:bg-soft-card transition-colors">Assistant</RouterLink>
          <RouterLink v-if="auth.user?.role === 'ADMIN'" to="/admin/jira" class="rounded-xl border border-line px-4 py-3 font-medium hover:bg-soft-card transition-colors">Intégration Jira</RouterLink>
        </div>
      </div>
    </div>

    <!-- Partager le formulaire vivant -->
    <div class="rounded-2xl border border-line bg-white-card p-6 mt-6">
      <h2 class="text-lg font-bold mb-1">Partager votre formulaire</h2>
      <p class="text-muted text-[14px] mb-5">
        Envoyez ce lien à un prospect : il décrit son besoin, vous le recevez déjà qualifié.
      </p>
      <div class="flex flex-col sm:flex-row gap-3">
        <input
          :value="formLink"
          readonly
          class="flex-1 rounded-xl bg-page-bg px-4 py-3 text-[14px] text-muted outline-none"
          @focus="($event.target as HTMLInputElement).select()"
        />
        <button
          class="rounded-xl bg-black text-white px-5 py-3 text-[13px] font-bold whitespace-nowrap hover:bg-text/80 transition-colors"
          @click="copier"
        >
          {{ copied ? 'Copié !' : 'Copier le lien' }}
        </button>
        <a
          :href="mailto"
          class="rounded-xl border border-line px-5 py-3 text-[13px] font-bold whitespace-nowrap text-center hover:bg-soft-card transition-colors"
        >
          Envoyer par email
        </a>
      </div>
    </div>
  </section>
</template>
