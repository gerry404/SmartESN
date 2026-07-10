<script setup lang="ts">
import { useRouter } from 'vue-router'
import BaseButton from '@/features/landing/components/ui/BaseButton.vue'


const router = useRouter()
</script>

<template>
  <main class="min-h-screen flex items-center justify-center bg-page-bg px-margin py-16 text-center">
    <div class="w-full max-w-lg flex flex-col items-center">
     

      <span class="font-label text-[12px] font-bold uppercase tracking-[0.2em] text-muted mb-4">
        Erreur 404
      </span>

      <p
        class="font-display font-bold leading-none tracking-tighter text-[120px] sm:text-[160px] bg-gradient-to-br from-brand-from to-brand-to bg-clip-text text-transparent"
      >
        404
      </p>

      <h1 class="font-display text-3xl sm:text-4xl font-bold tracking-tight text-text mt-2 mb-3">
        Page introuvable
      </h1>
      <p class="font-body-lg text-muted max-w-md mb-10">
        La page que vous cherchez n'existe pas, a été déplacée, ou n'est plus disponible.
      </p>

      <div class="flex flex-col sm:flex-row gap-4">
        <BaseButton variant="dark" @click="router.push('/')">Retour à l'accueil</BaseButton>
        <BaseButton variant="outline-dark" @click="router.back()">Page précédente</BaseButton>
      </div>
    </div>
  </main>
</template>
