<script setup lang="ts">
import { onMounted, watch } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { mdiArrowRight } from '@mdi/js'
import MdiIcon from '@/features/landing/components/ui/MdiIcon.vue'
import { useDemandesStore } from '../stores/demandesStore'
import { STATUTS, type StatutDemande } from '../types'

const store = useDemandesStore()
const route = useRoute()
const router = useRouter()

// L'URL (?statut=) est la source de vérité → alimente le filtre (sous-menus sidebar).
function applyQuery() {
  const v = (route.query.statut as string) || ''
  store.setFiltre(v ? (v as StatutDemande) : null)
}
onMounted(applyQuery)
watch(() => route.query.statut, applyQuery)

function onFiltre(e: Event) {
  const v = (e.target as HTMLSelectElement).value
  router.push({ query: v ? { statut: v } : {} })
}

// Couleur de badge par statut.
function badgeClass(s: StatutDemande): string {
  const map: Record<StatutDemande, string> = {
    NOUVELLE: 'bg-soft-card text-text',
    EN_ANALYSE: 'bg-sky-100 text-sky-700',
    QUALIFIEE: 'bg-amber-100 text-amber-700',
    DEVIS_ENVOYE: 'bg-indigo-100 text-indigo-700',
    EN_NEGOCIATION: 'bg-purple-100 text-purple-700',
    GAGNEE: 'bg-emerald-100 text-emerald-700',
    PERDUE: 'bg-red-100 text-red-600',
  }
  return map[s]
}
</script>

<template>
  <main class="min-h-screen bg-page-bg px-margin py-12">
    <div class="max-w-[1200px] mx-auto">
      <div class="flex items-end justify-between mb-8 gap-4 flex-wrap">
        <div>
          <h1 class="font-display text-3xl font-bold tracking-tight text-text">Demandes</h1>
          <p class="font-body-md text-muted">Tableau de bord des demandes entrantes.</p>
        </div>
        <label class="flex flex-col gap-1">
          <span class="font-label text-[11px] font-bold uppercase tracking-wider text-muted">Filtrer par statut</span>
          <select
            class="rounded-xl border border-line bg-white-card px-3 py-2 font-body-md text-text"
            :value="store.filtreStatut ?? ''"
            @change="onFiltre"
          >
            <option value="">Tous</option>
            <option v-for="s in STATUTS" :key="s" :value="s">{{ s }}</option>
          </select>
        </label>
      </div>

      <p v-if="store.loading" class="font-body-md text-muted py-10 text-center">Chargement…</p>
      <p v-else-if="store.error" class="font-body-md text-red-600 py-10 text-center">{{ store.error }}</p>
      <p v-else-if="!store.items.length" class="font-body-md text-muted py-10 text-center">
        Aucune demande.
      </p>

      <div v-else class="overflow-x-auto rounded-2xl border border-line bg-white-card">
        <table class="w-full text-left">
          <thead class="border-b border-line">
            <tr class="font-label text-[11px] font-bold uppercase tracking-wider text-muted">
              <th class="px-4 py-3">Client</th>
              <th class="px-4 py-3">Description</th>
              <th class="px-4 py-3">Type</th>
              <th class="px-4 py-3">Statut</th>
              <th class="px-4 py-3">Date</th>
              <th class="px-4 py-3"></th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="d in store.items"
              :key="d.id"
              class="border-b border-line/60 hover:bg-soft-card-2 transition-colors"
            >
              <td class="px-4 py-3 font-body-md font-medium text-text whitespace-nowrap">{{ d.clientNom }}</td>
              <td class="px-4 py-3 font-body-md text-muted max-w-[320px] truncate">{{ d.description }}</td>
              <td class="px-4 py-3 font-body-md text-text">{{ d.type ?? '—' }}</td>
              <td class="px-4 py-3">
                <span class="inline-block rounded-full px-2.5 py-1 font-label text-[10px] font-bold" :class="badgeClass(d.statut)">
                  {{ d.statut }}
                </span>
              </td>
              <td class="px-4 py-3 font-body-md text-muted whitespace-nowrap">
                {{ new Date(d.dateCreation).toLocaleDateString('fr-FR') }}
              </td>
              <td class="px-4 py-3 text-right">
                <RouterLink
                  :to="`/demandes/${d.id}`"
                  class="inline-flex items-center gap-1 font-label text-[12px] font-bold text-text hover:text-brand-from"
                >
                  Ouvrir <MdiIcon :path="mdiArrowRight" />
                </RouterLink>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </main>
</template>
