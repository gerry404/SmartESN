<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import * as jiraApi from '../services/jiraService'
import type { JiraConfigPayload } from '../types'
import type { JiraProjet } from '../services/jiraService'
import { useAuthStore } from '@/features/login/stores/authStore'

const auth = useAuthStore()

const connecte = ref(false) // URL + jeton OK
const projectKey = ref('') // projet choisi
const loading = ref(false)
const saving = ref(false)
const error = ref<string | null>(null)
const success = ref<string | null>(null)

const projets = ref<JiraProjet[]>([])
const projetsLoading = ref(false)
const projetsError = ref<string | null>(null)

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
    connecte.value = status.configure
    form.baseUrl = status.baseUrl ?? ''
    form.email = status.email ?? ''
    projectKey.value = status.projectKey ?? ''
    // pré-remplit l'email avec celui du compte si vide
    if (!form.email && auth.user?.email) form.email = auth.user.email
    if (connecte.value) chargerProjets()
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Erreur de chargement.'
  } finally {
    loading.value = false
  }
})

async function chargerProjets() {
  projetsLoading.value = true
  projetsError.value = null
  try {
    projets.value = await jiraApi.getJiraProjets()
  } catch (e) {
    projets.value = []
    projetsError.value = e instanceof Error ? e.message : 'Impossible de charger les projets.'
  } finally {
    projetsLoading.value = false
  }
}

// Étape 1 : connecter (URL + email + jeton). Le projet est choisi ensuite.
async function connecter() {
  saving.value = true
  error.value = null
  success.value = null
  try {
    await jiraApi.saveJiraConfig({ ...form, projectKey: projectKey.value })
    // on teste immédiatement en chargeant les projets
    await chargerProjets()
    if (projetsError.value) {
      error.value = projetsError.value
      connecte.value = false
    } else {
      connecte.value = true
      success.value = 'Connexion réussie ✓'
      form.apiToken = ''
    }
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Connexion impossible.'
  } finally {
    saving.value = false
  }
}

// Étape 2 : choisir le projet par défaut
async function choisirProjet(key: string) {
  projectKey.value = key
  try {
    await jiraApi.saveJiraConfig({ ...form, apiToken: '', projectKey: key })
    success.value = `Projet ${key} défini par défaut ✓`
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Impossible de définir le projet.'
  }
}
</script>

<template>
  <main class="min-h-screen bg-page-bg px-margin py-12">
    <div class="max-w-[680px] mx-auto">
      <div class="flex items-center gap-3 mb-2">
        <h1 class="font-display text-3xl font-bold tracking-tight text-text">Intégration Jira</h1>
        <span
          v-if="connecte"
          class="rounded-full bg-emerald-100 text-emerald-700 px-2.5 py-1 font-label text-[10px] font-bold"
        >
          Connecté
        </span>
      </div>
      <p class="font-body-md text-muted mb-8">
        Connectez votre Jira en 3 champs, puis choisissez le projet où créer les tâches.
      </p>

      <div v-if="error" class="mb-6 rounded-2xl border border-red-200 bg-red-50 px-4 py-3 font-body-md text-[14px] text-red-600">{{ error }}</div>
      <div v-if="success" class="mb-6 rounded-2xl border border-emerald-200 bg-emerald-50 px-4 py-3 font-body-md text-[14px] text-emerald-700">{{ success }}</div>

      <p v-if="loading" class="font-body-md text-muted text-center py-6">Chargement…</p>

      <!-- Étape 1 : connexion -->
      <form v-else class="rounded-2xl border border-line bg-white-card p-6 flex flex-col gap-5" @submit.prevent="connecter">
        <label class="flex flex-col gap-1.5">
          <span class="font-label text-[12px] font-bold text-text">URL de votre Jira</span>
          <input v-model="form.baseUrl" required placeholder="https://votre-domaine.atlassian.net" class="rounded-xl border border-line bg-page-bg px-4 py-3 font-body-md" />
        </label>
        <label class="flex flex-col gap-1.5">
          <span class="font-label text-[12px] font-bold text-text">Email du compte Jira</span>
          <input v-model="form.email" type="email" required placeholder="vous@entreprise.com" class="rounded-xl border border-line bg-page-bg px-4 py-3 font-body-md" />
        </label>
        <label class="flex flex-col gap-1.5">
          <div class="flex items-center justify-between">
            <span class="font-label text-[12px] font-bold text-text">Jeton d'API</span>
            <a
              href="https://id.atlassian.com/manage-profile/security/api-tokens"
              target="_blank"
              rel="noopener"
              class="font-label text-[11px] font-bold text-brand-from hover:underline"
            >
              Générer mon jeton →
            </a>
          </div>
          <input v-model="form.apiToken" type="password" :required="!connecte" :placeholder="connecte ? '•••••••• (laisser vide si inchangé)' : 'Collez votre jeton Atlassian'" class="rounded-xl border border-line bg-page-bg px-4 py-3 font-body-md" />
          <span class="font-body-md text-[12px] text-muted">
            Cliquez « Générer mon jeton », créez-en un, puis collez-le ici.
          </span>
        </label>

        <button :disabled="saving" class="rounded-full bg-black text-white px-6 py-3 font-label text-[11px] font-bold uppercase self-start disabled:opacity-50">
          {{ saving ? 'Connexion…' : connecte ? 'Mettre à jour' : 'Connecter' }}
        </button>
      </form>

      <!-- Étape 2 : choix du projet -->
      <section v-if="connecte" class="mt-8">
        <div class="flex items-center justify-between mb-3">
          <h2 class="font-display text-xl font-bold text-text">Choisissez votre projet</h2>
          <button class="text-[12px] font-bold text-muted hover:text-text" :disabled="projetsLoading" @click="chargerProjets">
            {{ projetsLoading ? 'Chargement…' : 'Actualiser' }}
          </button>
        </div>

        <div v-if="projetsError" class="rounded-2xl border border-amber-200 bg-amber-50 px-4 py-3 font-body-md text-[14px] text-amber-700">{{ projetsError }}</div>
        <p v-else-if="projetsLoading" class="font-body-md text-muted py-4">Récupération des projets…</p>
        <p v-else-if="!projets.length" class="font-body-md text-muted py-4">Aucun projet trouvé sur ce Jira.</p>

        <ul v-else class="flex flex-col gap-2">
          <li v-for="p in projets" :key="p.key" class="flex items-center justify-between rounded-xl border border-line bg-white-card px-4 py-3">
            <div class="flex items-center gap-3">
              <span class="rounded-lg bg-page-bg px-2 py-1 font-label text-[11px] font-bold text-text">{{ p.key }}</span>
              <span class="font-body-md text-[14px] text-text">{{ p.name }}</span>
            </div>
            <button v-if="projectKey === p.key" class="rounded-full bg-emerald-100 text-emerald-700 px-3 py-1 font-label text-[10px] font-bold" disabled>
              Projet par défaut
            </button>
            <button v-else class="rounded-full border border-line px-3 py-1 font-label text-[10px] font-bold text-text hover:bg-page-bg" @click="choisirProjet(p.key)">
              Choisir
            </button>
          </li>
        </ul>
      </section>
    </div>
  </main>
</template>
