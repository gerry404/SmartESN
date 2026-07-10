<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import * as jiraApi from '../services/jiraService'
import type { JiraConfigPayload } from '../types'

const configure = ref(false)
const loading = ref(false)
const saving = ref(false)
const error = ref<string | null>(null)
const success = ref(false)

const form = reactive<JiraConfigPayload>({
  baseUrl: '',
  email: '',
  apiToken: '',
  projectKey: '',
})

onMounted(async () => {
  loading.value = true
  try {
    const status = await jiraApi.getJiraConfig()
    configure.value = status.configure
    // Le token n'est jamais renvoyé ; on pré-remplit le reste s'il est fourni.
    form.baseUrl = status.baseUrl ?? ''
    form.email = status.email ?? ''
    form.projectKey = status.projectKey ?? ''
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Erreur de chargement.'
  } finally {
    loading.value = false
  }
})

async function enregistrer() {
  saving.value = true
  error.value = null
  success.value = false
  try {
    const status = await jiraApi.saveJiraConfig({ ...form })
    configure.value = status.configure
    success.value = true
    form.apiToken = '' // on ne garde pas le token en clair côté UI
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Enregistrement impossible.'
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <main class="min-h-screen bg-page-bg px-margin py-12">
    <div class="max-w-[640px] mx-auto">
      <div class="flex items-center gap-3 mb-2">
        <h1 class="font-display text-3xl font-bold tracking-tight text-text">Intégration Jira</h1>
        <span
          v-if="configure"
          class="rounded-full bg-emerald-100 text-emerald-700 px-2.5 py-1 font-label text-[10px] font-bold"
        >
          Configuré
        </span>
      </div>
      <p class="font-body-md text-muted mb-8">Connectez le Jira de votre entreprise.</p>

      <div v-if="error" class="mb-6 rounded-2xl border border-red-200 bg-red-50 px-4 py-3 font-body-md text-[14px] text-red-600">{{ error }}</div>
      <div v-if="success" class="mb-6 rounded-2xl border border-emerald-200 bg-emerald-50 px-4 py-3 font-body-md text-[14px] text-emerald-700">Configuration enregistrée.</div>

      <p v-if="loading" class="font-body-md text-muted text-center py-6">Chargement…</p>

      <form v-else class="rounded-2xl border border-line bg-white-card p-6 flex flex-col gap-5" @submit.prevent="enregistrer">
        <label class="flex flex-col gap-1.5">
          <span class="font-label text-[12px] font-bold text-text">URL Jira</span>
          <input v-model="form.baseUrl" required placeholder="https://votre-domaine.atlassian.net" class="rounded-xl border border-line bg-page-bg px-4 py-3 font-body-md" />
        </label>
        <label class="flex flex-col gap-1.5">
          <span class="font-label text-[12px] font-bold text-text">Email du compte</span>
          <input v-model="form.email" type="email" required placeholder="vous@entreprise.com" class="rounded-xl border border-line bg-page-bg px-4 py-3 font-body-md" />
        </label>
        <label class="flex flex-col gap-1.5">
          <span class="font-label text-[12px] font-bold text-text">Jeton d'API</span>
          <input v-model="form.apiToken" type="password" :required="!configure" :placeholder="configure ? '•••••••• (inchangé si vide)' : 'Jeton API Atlassian'" class="rounded-xl border border-line bg-page-bg px-4 py-3 font-body-md" />
        </label>
        <label class="flex flex-col gap-1.5">
          <span class="font-label text-[12px] font-bold text-text">Clé de projet</span>
          <input v-model="form.projectKey" required placeholder="SMART" class="rounded-xl border border-line bg-page-bg px-4 py-3 font-body-md" />
        </label>

        <button :disabled="saving" class="rounded-full bg-black text-white px-6 py-3 font-label text-[11px] font-bold uppercase self-start disabled:opacity-50">
          {{ saving ? 'Enregistrement…' : 'Enregistrer' }}
        </button>
      </form>
    </div>
  </main>
</template>
