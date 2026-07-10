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

const form = reactive<CreateUtilisateur>({ nom: '', email: '', motDePasse: '', role: 'COMMERCIAL' })

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
    form.nom = ''
    form.email = ''
    form.motDePasse = ''
    form.role = 'COMMERCIAL'
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
  <main class="min-h-screen bg-page-bg px-margin py-12">
    <div class="max-w-[900px] mx-auto">
      <h1 class="font-display text-3xl font-bold tracking-tight text-text mb-8">Utilisateurs</h1>

      <div v-if="error" class="mb-6 rounded-2xl border border-red-200 bg-red-50 px-4 py-3 font-body-md text-[14px] text-red-600">{{ error }}</div>

      <!-- Création -->
      <form class="rounded-2xl border border-line bg-white-card p-6 mb-8 flex flex-wrap items-end gap-3" @submit.prevent="creer">
        <label class="flex flex-col gap-1"><span class="font-label text-[11px] uppercase text-muted">Nom</span>
          <input v-model="form.nom" required class="rounded-xl border border-line bg-page-bg px-3 py-2" /></label>
        <label class="flex flex-col gap-1"><span class="font-label text-[11px] uppercase text-muted">Email</span>
          <input v-model="form.email" type="email" required class="rounded-xl border border-line bg-page-bg px-3 py-2" /></label>
        <label class="flex flex-col gap-1"><span class="font-label text-[11px] uppercase text-muted">Mot de passe</span>
          <input v-model="form.motDePasse" type="password" required class="rounded-xl border border-line bg-page-bg px-3 py-2" /></label>
        <label class="flex flex-col gap-1"><span class="font-label text-[11px] uppercase text-muted">Rôle</span>
          <select v-model="form.role" class="rounded-xl border border-line bg-page-bg px-3 py-2">
            <option v-for="r in ROLES" :key="r" :value="r">{{ r }}</option>
          </select></label>
        <button :disabled="busy" class="rounded-full bg-black text-white px-5 py-2.5 font-label text-[11px] font-bold uppercase disabled:opacity-50">Créer</button>
      </form>

      <p v-if="loading" class="font-body-md text-muted text-center py-6">Chargement…</p>

      <div v-else class="overflow-x-auto rounded-2xl border border-line bg-white-card">
        <table class="w-full text-left">
          <thead class="border-b border-line">
            <tr class="font-label text-[11px] font-bold uppercase tracking-wider text-muted">
              <th class="px-4 py-3">Nom</th><th class="px-4 py-3">Email</th><th class="px-4 py-3">Rôle</th><th class="px-4 py-3"></th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="u in users" :key="u.id" class="border-b border-line/60">
              <td class="px-4 py-3 font-body-md text-text">{{ u.nom }}</td>
              <td class="px-4 py-3 font-body-md text-muted">{{ u.email }}</td>
              <td class="px-4 py-3 font-body-md text-text">{{ u.role }}</td>
              <td class="px-4 py-3 text-right">
                <button :disabled="busy" class="font-label text-[12px] font-bold text-red-600 hover:underline disabled:opacity-50" @click="supprimer(u.id)">Supprimer</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </main>
</template>
