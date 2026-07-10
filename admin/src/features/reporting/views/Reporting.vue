<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { getStatistiques } from '../services/statsService'
import type { Statistiques } from '@/features/demandes/types'

const stats = ref<Statistiques | null>(null)
const loading = ref(false)
const error = ref<string | null>(null)

onMounted(async () => {
  loading.value = true
  try {
    stats.value = await getStatistiques()
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Erreur de chargement.'
  } finally {
    loading.value = false
  }
})

const kpis = computed(() => {
  const s = stats.value
  if (!s) return []
  return [
    { label: 'Total demandes', value: s.totalDemandes },
    { label: 'Nouvelles', value: s.nbNouvelles },
    { label: 'Qualifiées', value: s.nbQualifiees },
    { label: 'Devis envoyés', value: s.nbDevisEnvoyes },
    { label: 'Gagnées', value: s.nbGagnees },
    { label: 'Perdues', value: s.nbPerdues },
    { label: 'Taux de conversion', value: `${s.tauxConversion} %` },
  ]
})

const euros = (n: number) =>
  n.toLocaleString('fr-FR', { style: 'currency', currency: 'XOF', maximumFractionDigits: 0 })

// Largeur relative des barres CA (potentiel = 100%).
const caSignePct = computed(() => {
  const s = stats.value
  if (!s || !s.caPotentiel) return 0
  return Math.round((s.caSigne / s.caPotentiel) * 100)
})
</script>

<template>
  <main class="min-h-screen bg-page-bg px-margin py-12">
    <div class="max-w-[1000px] mx-auto">
      <h1 class="font-display text-3xl font-bold tracking-tight text-text mb-8">Reporting</h1>

      <p v-if="loading" class="font-body-md text-muted py-10 text-center">Chargement…</p>
      <p v-else-if="error" class="font-body-md text-red-600 py-10 text-center">{{ error }}</p>

      <template v-else-if="stats">
        <!-- KPI -->
        <div class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4 mb-8">
          <div v-for="k in kpis" :key="k.label" class="rounded-2xl border border-line bg-white-card p-5">
            <p class="font-label text-[11px] font-bold uppercase tracking-wider text-muted mb-1">{{ k.label }}</p>
            <p class="font-display text-2xl font-bold text-text">{{ k.value }}</p>
          </div>
        </div>

        <!-- CA potentiel vs signé -->
        <div class="rounded-2xl border border-line bg-white-card p-6">
          <h2 class="font-h3 text-lg font-bold text-text mb-5">Chiffre d'affaires</h2>
          <div class="space-y-4">
            <div>
              <div class="flex justify-between mb-1.5">
                <span class="font-body-md text-text">Potentiel</span>
                <span class="font-label text-[12px] font-bold text-text">{{ euros(stats.caPotentiel) }}</span>
              </div>
              <div class="h-3 rounded-full bg-soft-card"><div class="h-full rounded-full bg-muted-light" style="width: 100%"></div></div>
            </div>
            <div>
              <div class="flex justify-between mb-1.5">
                <span class="font-body-md text-text">Signé</span>
                <span class="font-label text-[12px] font-bold text-text">{{ euros(stats.caSigne) }}</span>
              </div>
              <div class="h-3 rounded-full bg-soft-card">
                <div class="h-full rounded-full bg-gradient-to-r from-brand-from to-brand-to transition-all" :style="{ width: caSignePct + '%' }"></div>
              </div>
            </div>
          </div>
        </div>
      </template>
    </div>
  </main>
</template>
