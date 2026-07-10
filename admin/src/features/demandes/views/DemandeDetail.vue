<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { useDemandesStore } from '../stores/demandesStore'
import * as demandeApi from '../services/demandeService'
import * as devisApi from '../services/devisService'
import { listEquipes } from '@/features/equipes/services/equipeService'
import {
  COMPLEXITES,
  STATUTS,
  TYPES,
  type Complexite,
  type Devis,
  type Equipe,
  type StatutDemande,
  type TypeProjet,
} from '../types'

const props = defineProps<{ id: string }>()
const store = useDemandesStore()

const busy = ref(false)
const actionError = ref<string | null>(null)

const qualForm = ref<{ type: TypeProjet; complexite: Complexite }>({
  type: 'WEB',
  complexite: 'MOYENNE',
})
const statutForm = ref<{ statut: StatutDemande; budgetSigne?: number; delaiReel?: number }>({
  statut: 'EN_NEGOCIATION',
})
const devis = ref<Devis | null>(null)
const equipes = ref<Equipe[]>([])
const equipeSel = ref<number | null>(null)

const idNum = () => Number(props.id)

// Enveloppe commune pour les actions : busy + gestion d'erreur (502, etc.).
async function run(fn: () => Promise<void>) {
  busy.value = true
  actionError.value = null
  try {
    await fn()
  } catch (e) {
    const status = (e as { status?: number })?.status
    if (status === 502) {
      actionError.value = "Le service d'analyse est momentanément indisponible."
    } else {
      actionError.value = e instanceof Error ? e.message : 'Action impossible.'
    }
  } finally {
    busy.value = false
  }
}

onMounted(async () => {
  await store.chargerDetail(idNum())
  // Devis éventuel (404 = pas encore de devis → on ignore).
  try {
    devis.value = await devisApi.getDevis(idNum())
  } catch {
    devis.value = null
  }
  try {
    equipes.value = await listEquipes()
  } catch {
    equipes.value = []
  }
})

const qualifierManuel = () =>
  run(async () => store.appliquer(await demandeApi.qualifier(idNum(), qualForm.value)))

const qualifierIA = () =>
  run(async () => store.appliquer(await demandeApi.analyser(idNum())))

const changerStatut = () =>
  run(async () => store.appliquer(await demandeApi.changerStatut(idNum(), statutForm.value)))

const reaffecter = () =>
  run(async () => {
    if (equipeSel.value == null) return
    store.appliquer(await demandeApi.reaffecter(idNum(), equipeSel.value))
  })

const genererDevis = () =>
  run(async () => {
    devis.value = await devisApi.genererDevis(idNum())
  })

const envoyerDevis = () =>
  run(async () => {
    if (!devis.value) return
    devis.value = await devisApi.envoyerDevis(devis.value.id)
    await store.chargerDetail(idNum()) // le statut passe à DEVIS_ENVOYE
  })

const euros = (n?: number | null) =>
  n == null ? '—' : n.toLocaleString('fr-FR', { style: 'currency', currency: 'XOF', maximumFractionDigits: 0 })
</script>

<template>
  <main class="min-h-screen bg-page-bg px-margin py-12">
    <div class="max-w-[900px] mx-auto">
      <RouterLink to="/demandes" class="font-label text-[12px] font-bold text-muted hover:text-text">
        ← Retour aux demandes
      </RouterLink>

      <p v-if="store.loading" class="font-body-md text-muted py-10 text-center">Chargement…</p>
      <p v-else-if="store.error" class="font-body-md text-red-600 py-10 text-center">{{ store.error }}</p>

      <template v-else-if="store.current">
        <div class="mt-4 mb-8">
          <h1 class="font-display text-3xl font-bold tracking-tight text-text">
            Demande #{{ store.current.id }}
          </h1>
          <p class="font-body-md text-muted">
            {{ store.current.clientNom }} · {{ store.current.clientEmail }}
          </p>
          <span class="inline-block mt-3 rounded-full bg-soft-card px-3 py-1 font-label text-[11px] font-bold">
            {{ store.current.statut }}
          </span>
        </div>

        <div v-if="actionError" class="mb-6 rounded-2xl border border-red-200 bg-red-50 px-4 py-3 font-body-md text-[14px] text-red-600">
          {{ actionError }}
        </div>

        <!-- Description -->
        <section class="rounded-2xl border border-line bg-white-card p-6 mb-6">
          <h2 class="font-label text-[11px] font-bold uppercase tracking-wider text-muted mb-3">Description</h2>
          <p class="font-body-md text-text whitespace-pre-line">{{ store.current.description }}</p>
        </section>

        <!-- Estimation -->
        <section class="rounded-2xl border border-line bg-white-card p-6 mb-6 grid grid-cols-2 sm:grid-cols-4 gap-4">
          <div><p class="font-label text-[11px] text-muted uppercase">Type</p><p class="font-body-md font-bold text-text">{{ store.current.type ?? '—' }}</p></div>
          <div><p class="font-label text-[11px] text-muted uppercase">Complexité</p><p class="font-body-md font-bold text-text">{{ store.current.complexite ?? '—' }}</p></div>
          <div><p class="font-label text-[11px] text-muted uppercase">Budget</p><p class="font-body-md font-bold text-text">{{ euros(store.current.budgetMin) }} – {{ euros(store.current.budgetMax) }}</p></div>
          <div><p class="font-label text-[11px] text-muted uppercase">Délai</p><p class="font-body-md font-bold text-text">{{ store.current.delaiSemaines ?? '—' }} sem.</p></div>
          <div class="col-span-2"><p class="font-label text-[11px] text-muted uppercase">Équipe</p><p class="font-body-md font-bold text-text">{{ store.current.equipeAffectee ?? '—' }}</p></div>
        </section>

        <!-- Qualification -->
        <section class="rounded-2xl border border-line bg-white-card p-6 mb-6">
          <h2 class="font-h3 text-lg font-bold text-text mb-4">Qualification</h2>
          <div class="flex flex-wrap items-end gap-3">
            <label class="flex flex-col gap-1">
              <span class="font-label text-[11px] uppercase text-muted">Type</span>
              <select v-model="qualForm.type" class="rounded-xl border border-line bg-page-bg px-3 py-2">
                <option v-for="t in TYPES" :key="t" :value="t">{{ t }}</option>
              </select>
            </label>
            <label class="flex flex-col gap-1">
              <span class="font-label text-[11px] uppercase text-muted">Complexité</span>
              <select v-model="qualForm.complexite" class="rounded-xl border border-line bg-page-bg px-3 py-2">
                <option v-for="c in COMPLEXITES" :key="c" :value="c">{{ c }}</option>
              </select>
            </label>
            <button :disabled="busy" class="rounded-full bg-black text-white px-5 py-2.5 font-label text-[11px] font-bold uppercase disabled:opacity-50" @click="qualifierManuel">Qualifier</button>
            <button :disabled="busy" class="rounded-full border border-text/30 px-5 py-2.5 font-label text-[11px] font-bold uppercase disabled:opacity-50" @click="qualifierIA">Qualifier par IA</button>
          </div>
        </section>

        <!-- Devis -->
        <section class="rounded-2xl border border-line bg-white-card p-6 mb-6">
          <h2 class="font-h3 text-lg font-bold text-text mb-4">Devis</h2>
          <template v-if="devis">
            <p class="font-body-md text-text mb-2">Montant : <b>{{ euros(devis.montant) }}</b> · {{ devis.valide ? 'Envoyé' : 'Brouillon' }}</p>
            <pre class="rounded-xl bg-soft-card-2 border border-line p-4 text-[13px] whitespace-pre-wrap max-h-52 overflow-auto mb-4">{{ devis.contenu }}</pre>
            <button v-if="!devis.valide" :disabled="busy" class="rounded-full bg-black text-white px-5 py-2.5 font-label text-[11px] font-bold uppercase disabled:opacity-50" @click="envoyerDevis">Valider &amp; envoyer</button>
          </template>
          <button v-else :disabled="busy" class="rounded-full bg-black text-white px-5 py-2.5 font-label text-[11px] font-bold uppercase disabled:opacity-50" @click="genererDevis">Générer le devis</button>
        </section>

        <!-- Statut -->
        <section class="rounded-2xl border border-line bg-white-card p-6 mb-6">
          <h2 class="font-h3 text-lg font-bold text-text mb-4">Statut commercial</h2>
          <div class="flex flex-wrap items-end gap-3">
            <label class="flex flex-col gap-1">
              <span class="font-label text-[11px] uppercase text-muted">Nouveau statut</span>
              <select v-model="statutForm.statut" class="rounded-xl border border-line bg-page-bg px-3 py-2">
                <option v-for="s in STATUTS" :key="s" :value="s">{{ s }}</option>
              </select>
            </label>
            <template v-if="statutForm.statut === 'GAGNEE'">
              <label class="flex flex-col gap-1">
                <span class="font-label text-[11px] uppercase text-muted">Budget signé</span>
                <input v-model.number="statutForm.budgetSigne" type="number" class="rounded-xl border border-line bg-page-bg px-3 py-2 w-36" />
              </label>
              <label class="flex flex-col gap-1">
                <span class="font-label text-[11px] uppercase text-muted">Délai réel (sem.)</span>
                <input v-model.number="statutForm.delaiReel" type="number" class="rounded-xl border border-line bg-page-bg px-3 py-2 w-32" />
              </label>
            </template>
            <button :disabled="busy" class="rounded-full bg-black text-white px-5 py-2.5 font-label text-[11px] font-bold uppercase disabled:opacity-50" @click="changerStatut">Mettre à jour</button>
          </div>
        </section>

        <!-- Réaffectation -->
        <section class="rounded-2xl border border-line bg-white-card p-6">
          <h2 class="font-h3 text-lg font-bold text-text mb-4">Réaffectation</h2>
          <div class="flex flex-wrap items-end gap-3">
            <label class="flex flex-col gap-1">
              <span class="font-label text-[11px] uppercase text-muted">Équipe</span>
              <select v-model.number="equipeSel" class="rounded-xl border border-line bg-page-bg px-3 py-2">
                <option :value="null" disabled>Choisir…</option>
                <option v-for="e in equipes" :key="e.id" :value="e.id">{{ e.nom }} ({{ e.specialite }})</option>
              </select>
            </label>
            <button :disabled="busy || equipeSel == null" class="rounded-full bg-black text-white px-5 py-2.5 font-label text-[11px] font-bold uppercase disabled:opacity-50" @click="reaffecter">Réaffecter</button>
          </div>
        </section>
      </template>
    </div>
  </main>
</template>
