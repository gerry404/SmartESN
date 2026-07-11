<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import * as adminApi from '../services/adminService'
import type { GrilleLigne } from '../types'
import type { Complexite, TypeProjet } from '@/features/demandes/types'

const lignes = ref<GrilleLigne[]>([])
const loading = ref(false)
const error = ref<string | null>(null)
const savingId = ref<number | null>(null)
const creating = ref(false)

const TYPES: TypeProjet[] = ['MOBILE', 'WEB', 'DESKTOP', 'CONSEIL']
const COMPLEXITES: Complexite[] = ['SIMPLE', 'MOYENNE', 'COMPLEXE']

const nouvelle = reactive<Omit<GrilleLigne, 'id'>>({
  type: 'WEB',
  complexite: 'SIMPLE',
  budgetMin: 0,
  budgetMax: 0,
  delaiMin: 0,
  delaiMax: 0,
})

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

async function creer() {
  creating.value = true
  error.value = null
  try {
    const ligne = await adminApi.creerGrilleLigne({ ...nouvelle })
    lignes.value.push(ligne)
    nouvelle.budgetMin = 0
    nouvelle.budgetMax = 0
    nouvelle.delaiMin = 0
    nouvelle.delaiMax = 0
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Création impossible.'
  } finally {
    creating.value = false
  }
}

async function supprimer(l: GrilleLigne) {
  if (!confirm(`Supprimer la référence ${l.type} / ${l.complexite} ?`)) return
  error.value = null
  try {
    await adminApi.supprimerGrilleLigne(l.id)
    lignes.value = lignes.value.filter((x) => x.id !== l.id)
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Suppression impossible.'
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
              <td class="px-4 py-2 text-right whitespace-nowrap">
                <button :disabled="savingId === l.id" class="font-label text-[12px] font-bold text-text hover:text-brand-from disabled:opacity-50" @click="enregistrer(l)">
                  {{ savingId === l.id ? '…' : 'Enregistrer' }}
                </button>
                <button class="ml-4 font-label text-[12px] font-bold text-red-500 hover:text-red-700" @click="supprimer(l)">
                  Supprimer
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Ajouter une référence -->
      <div class="mt-8 rounded-2xl border border-line bg-white-card p-6">
        <h2 class="font-display text-xl font-bold text-text mb-4">Ajouter une référence</h2>
        <form class="flex flex-wrap items-end gap-4" @submit.prevent="creer">
          <label class="flex flex-col gap-1">
            <span class="font-label text-[11px] font-bold uppercase text-muted">Type</span>
            <select v-model="nouvelle.type" class="rounded-lg border border-line bg-page-bg px-3 py-2">
              <option v-for="t in TYPES" :key="t" :value="t">{{ t }}</option>
            </select>
          </label>
          <label class="flex flex-col gap-1">
            <span class="font-label text-[11px] font-bold uppercase text-muted">Complexité</span>
            <select v-model="nouvelle.complexite" class="rounded-lg border border-line bg-page-bg px-3 py-2">
              <option v-for="c in COMPLEXITES" :key="c" :value="c">{{ c }}</option>
            </select>
          </label>
          <label class="flex flex-col gap-1">
            <span class="font-label text-[11px] font-bold uppercase text-muted">Budget min</span>
            <input v-model.number="nouvelle.budgetMin" type="number" class="w-28 rounded-lg border border-line bg-page-bg px-2 py-2" />
          </label>
          <label class="flex flex-col gap-1">
            <span class="font-label text-[11px] font-bold uppercase text-muted">Budget max</span>
            <input v-model.number="nouvelle.budgetMax" type="number" class="w-28 rounded-lg border border-line bg-page-bg px-2 py-2" />
          </label>
          <label class="flex flex-col gap-1">
            <span class="font-label text-[11px] font-bold uppercase text-muted">Délai min</span>
            <input v-model.number="nouvelle.delaiMin" type="number" class="w-20 rounded-lg border border-line bg-page-bg px-2 py-2" />
          </label>
          <label class="flex flex-col gap-1">
            <span class="font-label text-[11px] font-bold uppercase text-muted">Délai max</span>
            <input v-model.number="nouvelle.delaiMax" type="number" class="w-20 rounded-lg border border-line bg-page-bg px-2 py-2" />
          </label>
          <button :disabled="creating" class="rounded-full bg-black text-white px-6 py-2.5 font-label text-[11px] font-bold uppercase disabled:opacity-50">
            {{ creating ? 'Ajout…' : 'Ajouter' }}
          </button>
        </form>
      </div>
    </div>
  </main>
</template>
