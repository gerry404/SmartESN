<script setup lang="ts">
import { onMounted, ref } from 'vue'
import * as adminApi from '../services/adminService'
import type { GrilleLigne } from '../types'

const lignes = ref<GrilleLigne[]>([])
const loading = ref(false)
const error = ref<string | null>(null)
const savingId = ref<number | null>(null)

async function charger() {
  loading.value = true
  error.value = null
  try {
    lignes.value = await adminApi.listGrille()
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Erreur de chargement.'
  } finally {
    loading.value = false
  }
}

onMounted(charger)

async function enregistrer(l: GrilleLigne) {
  savingId.value = l.id
  error.value = null
  try {
    const maj = await adminApi.majGrilleLigne(l.id, {
      budgetMin: l.budgetMin,
      budgetMax: l.budgetMax,
      delaiMin: l.delaiMin,
      delaiMax: l.delaiMax,
    })
    const i = lignes.value.findIndex((x) => x.id === maj.id)
    if (i !== -1) lignes.value[i] = maj
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Enregistrement impossible.'
  } finally {
    savingId.value = null
  }
}
</script>

<template>
  <main class="min-h-screen bg-page-bg px-margin py-12">
    <div class="max-w-[1000px] mx-auto">
      <h1 class="font-display text-3xl font-bold tracking-tight text-text mb-2">Grille de référence</h1>
      <p class="font-body-md text-muted mb-8">Budgets et délais par type et complexité.</p>

      <div v-if="error" class="mb-6 rounded-2xl border border-red-200 bg-red-50 px-4 py-3 font-body-md text-[14px] text-red-600">{{ error }}</div>
      <p v-if="loading" class="font-body-md text-muted text-center py-6">Chargement…</p>

      <div v-else class="overflow-x-auto rounded-2xl border border-line bg-white-card">
        <table class="w-full text-left">
          <thead class="border-b border-line">
            <tr class="font-label text-[11px] font-bold uppercase tracking-wider text-muted">
              <th class="px-4 py-3">Type</th><th class="px-4 py-3">Complexité</th>
              <th class="px-4 py-3">Budget min</th><th class="px-4 py-3">Budget max</th>
              <th class="px-4 py-3">Délai min</th><th class="px-4 py-3">Délai max</th><th class="px-4 py-3"></th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="l in lignes" :key="l.id" class="border-b border-line/60">
              <td class="px-4 py-3 font-body-md font-bold text-text">{{ l.type }}</td>
              <td class="px-4 py-3 font-body-md text-text">{{ l.complexite }}</td>
              <td class="px-4 py-2"><input v-model.number="l.budgetMin" type="number" class="w-28 rounded-lg border border-line bg-page-bg px-2 py-1.5" /></td>
              <td class="px-4 py-2"><input v-model.number="l.budgetMax" type="number" class="w-28 rounded-lg border border-line bg-page-bg px-2 py-1.5" /></td>
              <td class="px-4 py-2"><input v-model.number="l.delaiMin" type="number" class="w-20 rounded-lg border border-line bg-page-bg px-2 py-1.5" /></td>
              <td class="px-4 py-2"><input v-model.number="l.delaiMax" type="number" class="w-20 rounded-lg border border-line bg-page-bg px-2 py-1.5" /></td>
              <td class="px-4 py-2 text-right">
                <button :disabled="savingId === l.id" class="font-label text-[12px] font-bold text-text hover:text-brand-from disabled:opacity-50" @click="enregistrer(l)">
                  {{ savingId === l.id ? '…' : 'Enregistrer' }}
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </main>
</template>
