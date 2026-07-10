<script setup lang="ts">
import { onMounted, ref } from 'vue'
import * as projetApi from '../services/projetService'
import type { Tache } from '../types'

const props = defineProps<{ id: string }>()

const taches = ref<Tache[]>([])
const loading = ref(false)
const busy = ref(false)
const error = ref<string | null>(null)
const info = ref<string | null>(null)

const idNum = () => Number(props.id)

onMounted(async () => {
  loading.value = true
  try {
    taches.value = await projetApi.getTaches(idNum())
  } catch {
    taches.value = []
  } finally {
    loading.value = false
  }
})

async function run(fn: () => Promise<void>) {
  busy.value = true
  error.value = null
  info.value = null
  try {
    await fn()
  } catch (e) {
    const status = (e as { status?: number })?.status
    if (status === 502) error.value = "Le service d'analyse est momentanément indisponible."
    else if (status === 409) error.value = 'Jira n’est pas configuré pour votre entreprise.'
    else error.value = e instanceof Error ? e.message : 'Action impossible.'
  } finally {
    busy.value = false
  }
}

const decomposer = () =>
  run(async () => {
    taches.value = await projetApi.decomposer(idNum())
    info.value = `${taches.value.length} tâche(s) proposée(s) par l'IA.`
  })

const pousser = () =>
  run(async () => {
    taches.value = await projetApi.pushJira(idNum())
    info.value = 'Tâches créées dans Jira.'
  })
</script>

<template>
  <main class="min-h-screen bg-page-bg px-margin py-12">
    <div class="max-w-[820px] mx-auto">
      <h1 class="font-display text-3xl font-bold tracking-tight text-text mb-2">Projet #{{ id }} — Tâches</h1>
      <p class="font-body-md text-muted mb-8">Découpage IA et synchronisation Jira.</p>

      <div class="flex flex-wrap gap-3 mb-6">
        <button :disabled="busy" class="rounded-full bg-black text-white px-5 py-2.5 font-label text-[11px] font-bold uppercase disabled:opacity-50" @click="decomposer">
          Décomposer par IA
        </button>
        <button :disabled="busy || !taches.length" class="rounded-full border border-text/30 px-5 py-2.5 font-label text-[11px] font-bold uppercase disabled:opacity-50" @click="pousser">
          Pousser vers Jira
        </button>
      </div>

      <div v-if="error" class="mb-6 rounded-2xl border border-red-200 bg-red-50 px-4 py-3 font-body-md text-[14px] text-red-600">{{ error }}</div>
      <div v-if="info" class="mb-6 rounded-2xl border border-emerald-200 bg-emerald-50 px-4 py-3 font-body-md text-[14px] text-emerald-700">{{ info }}</div>

      <p v-if="loading" class="font-body-md text-muted text-center py-6">Chargement…</p>
      <p v-else-if="!taches.length" class="font-body-md text-muted text-center py-10">
        Aucune tâche. Lancez « Décomposer par IA ».
      </p>

      <ul v-else class="flex flex-col gap-3">
        <li v-for="(t, i) in taches" :key="t.id ?? i" class="rounded-2xl border border-line bg-white-card p-5">
          <div class="flex items-start justify-between gap-4">
            <div class="min-w-0">
              <h3 class="font-h3 text-lg font-bold text-text">{{ t.titre }}</h3>
              <p v-if="t.description" class="font-body-md text-muted mt-1">{{ t.description }}</p>
              <p v-if="t.assigneSuggere" class="font-label text-[11px] text-muted uppercase tracking-wider mt-2">
                Assigné suggéré : <span class="text-text font-bold">{{ t.assigneSuggere }}</span>
              </p>
            </div>
            <span v-if="t.jiraKey" class="shrink-0 rounded-full bg-indigo-100 text-indigo-700 px-2.5 py-1 font-label text-[10px] font-bold">
              {{ t.jiraKey }}
            </span>
          </div>
        </li>
      </ul>
    </div>
  </main>
</template>
