<script setup lang="ts">
import { reactive, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { mdiArrowLeft } from '@mdi/js'
import Logo from '@/features/landing/components/ui/Logo.vue'
import MdiIcon from '@/features/landing/components/ui/MdiIcon.vue'

const form = reactive({ nom: '', email: '', message: '' })
const envoye = ref(false)

function envoyer() {
  // Pas de backend contact pour l'instant : on confirme localement.
  envoye.value = true
}
</script>

<template>
  <div class="min-h-screen bg-page-bg text-text">
    <header class="px-margin py-6 max-w-[1100px] mx-auto flex items-center justify-between">
      <RouterLink to="/"><Logo /></RouterLink>
      <RouterLink to="/" class="inline-flex items-center gap-1.5 text-[14px] text-muted hover:text-text transition-colors">
        <MdiIcon :path="mdiArrowLeft" /> Accueil
      </RouterLink>
    </header>

    <main class="px-margin max-w-[560px] mx-auto pt-20 pb-32">
      <h1 class="font-display text-5xl font-bold tracking-tight mb-4">Parlons-en.</h1>
      <p class="text-body-lg text-muted mb-12">
        Une question, une démo, un projet ? Écrivez-nous, on revient vers vous rapidement.
      </p>

      <div v-if="envoye" class="rounded-2xl bg-soft-card p-8 text-center">
        <p class="font-display text-2xl font-bold mb-1">Merci !</p>
        <p class="text-muted">Votre message a bien été envoyé.</p>
      </div>

      <form v-else class="flex flex-col gap-6" @submit.prevent="envoyer">
        <label class="flex flex-col gap-2">
          <span class="text-[14px] font-semibold">Nom</span>
          <input v-model="form.nom" required class="rounded-xl bg-white-card px-4 py-3 outline-none focus:ring-2 focus:ring-text/10" />
        </label>
        <label class="flex flex-col gap-2">
          <span class="text-[14px] font-semibold">Email</span>
          <input v-model="form.email" type="email" required class="rounded-xl bg-white-card px-4 py-3 outline-none focus:ring-2 focus:ring-text/10" />
        </label>
        <label class="flex flex-col gap-2">
          <span class="text-[14px] font-semibold">Message</span>
          <textarea v-model="form.message" rows="5" required class="rounded-xl bg-white-card px-4 py-3 outline-none focus:ring-2 focus:ring-text/10 resize-none"></textarea>
        </label>
        <button class="self-start rounded-full bg-text text-page-bg px-7 py-3.5 font-semibold hover:opacity-90 transition-opacity">
          Envoyer
        </button>
      </form>
    </main>
  </div>
</template>
