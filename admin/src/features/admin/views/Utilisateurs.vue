<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import * as adminApi from '../services/adminService'
import type { CreateUtilisateur, Utilisateur } from '../types'
import type { Role } from '@/features/demandes/types'

const ROLES: Role[] = ['COMMERCIAL', 'DIRECTEUR_TECHNIQUE', 'ADMIN']

const users = ref<Utilisateur[]>([])
const loading = ref(false)
const error = ref<string | null>(null)
const busy = ref(false)

const showForm = ref(false)
const form = reactive<CreateUtilisateur>({ nom: '', email: '', motDePasse: '', role: 'COMMERCIAL' })

function resetForm() {
  form.nom = ''
  form.email = ''
  form.motDePasse = ''
  form.role = 'COMMERCIAL'
}
function openForm() {
  resetForm()
  error.value = null
  showForm.value = true
}

async function charger() {
  loading.value = true
  error.value = null
  try {
    users.value = await adminApi.listUtilisateurs()
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Erreur de chargement.'
  } finally {
    loading.value = false
  }
}

onMounted(charger)

async function creer() {
  busy.value = true
  error.value = null
  try {
    const u = await adminApi.creerUtilisateur({ ...form })
    users.value.push(u)
    showForm.value = false
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Création impossible.'
  } finally {
    busy.value = false
  }
}

async function supprimer(id: number) {
  busy.value = true
  try {
    await adminApi.supprimerUtilisateur(id)
    users.value = users.value.filter((u) => u.id !== id)
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Suppression impossible.'
  } finally {
    busy.value = false
  }
}
</script>

<template>
  <main class="min-h-full bg-page-bg px-margin py-12">
    <div class="max-w-[900px] mx-auto">
      <div class="flex items-center justify-between mb-8 gap-4">
        <h1 class="text-3xl font-bold tracking-tight">Utilisateurs</h1>
        <button
          class="rounded-full bg-black text-white px-5 py-2.5 text-[13px] font-bold flex items-center gap-2 hover:bg-text/80 transition-colors"
          @click="openForm"
        >
          <span class="text-lg leading-none">+</span> Nouvel utilisateur
        </button>
      </div>

      <div v-if="error && !showForm" class="mb-6 rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-[14px] text-red-600">{{ error }}</div>

      <p v-if="loading" class="text-muted text-center py-6">Chargement…</p>

      <div v-else class="overflow-x-auto rounded-2xl border border-line bg-white-card">
        <table class="w-full text-left">
          <thead class="border-b border-line">
            <tr class="text-[11px] font-bold uppercase tracking-wider text-muted">
              <th class="px-4 py-3">Nom</th><th class="px-4 py-3">Email</th><th class="px-4 py-3">Rôle</th><th class="px-4 py-3"></th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="u in users" :key="u.id" class="border-b border-line/60">
              <td class="px-4 py-3 font-medium">{{ u.nom }}</td>
              <td class="px-4 py-3 text-muted">{{ u.email }}</td>
              <td class="px-4 py-3">{{ u.role }}</td>
              <td class="px-4 py-3 text-right">
                <button :disabled="busy" class="text-[12px] font-bold text-red-600 hover:underline disabled:opacity-50" @click="supprimer(u.id)">Supprimer</button>
              </td>
            </tr>
            <tr v-if="!users.length">
              <td colspan="4" class="px-4 py-8 text-center text-muted">Aucun utilisateur.</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Modal : nouvel utilisateur -->
    <Teleport to="body">
      <div v-if="showForm" class="fixed inset-0 z-50 flex items-center justify-center p-4">
        <div class="absolute inset-0 bg-black/40" @click="showForm = false"></div>
        <div class="relative w-full max-w-md rounded-2xl bg-white-card border border-line shadow-2xl p-6">
          <div class="flex items-center justify-between mb-5">
            <h2 class="text-xl font-bold">Nouvel utilisateur</h2>
            <button class="text-muted hover:text-text text-xl leading-none" aria-label="Fermer" @click="showForm = false">×</button>
          </div>

          <div v-if="error" class="mb-4 rounded-xl border border-red-200 bg-red-50 px-3 py-2 text-[13px] text-red-600">{{ error }}</div>

          <form class="flex flex-col gap-4" @submit.prevent="creer">
            <label class="flex flex-col gap-1"><span class="text-[12px] font-bold">Nom</span>
              <input v-model="form.nom" required class="rounded-xl border border-line bg-page-bg px-3 py-2.5" /></label>
            <label class="flex flex-col gap-1"><span class="text-[12px] font-bold">Email</span>
              <input v-model="form.email" type="email" required class="rounded-xl border border-line bg-page-bg px-3 py-2.5" /></label>
            <label class="flex flex-col gap-1"><span class="text-[12px] font-bold">Mot de passe</span>
              <input v-model="form.motDePasse" type="password" required class="rounded-xl border border-line bg-page-bg px-3 py-2.5" /></label>
            <label class="flex flex-col gap-1"><span class="text-[12px] font-bold">Rôle</span>
              <select v-model="form.role" class="rounded-xl border border-line bg-page-bg px-3 py-2.5">
                <option v-for="r in ROLES" :key="r" :value="r">{{ r }}</option>
              </select></label>

            <div class="flex justify-end gap-3 mt-2">
              <button type="button" class="rounded-full border border-line px-5 py-2.5 text-[13px] font-bold" @click="showForm = false">Annuler</button>
              <button :disabled="busy" class="rounded-full bg-black text-white px-5 py-2.5 text-[13px] font-bold disabled:opacity-50">
                {{ busy ? 'Création…' : 'Créer' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </Teleport>
  </main>
</template>
