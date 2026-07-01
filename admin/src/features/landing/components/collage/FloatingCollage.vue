<script setup lang="ts">
import FloatingCard from './FloatingCard.vue'
import type { CollageCard } from '../../types'

defineProps<{
  title: string[]
  cards: CollageCard[]
  commentAvatar?: string
}>()
</script>

<template>
  <section class="min-h-[1400px] w-full max-w-[1728px] mx-auto relative overflow-hidden bg-page-bg py-xl">
    <!-- Central anchor headline -->
    <div class="absolute inset-0 flex justify-center items-center text-muted pointer-events-none mt-20">
      <h2 class="font-display text-[120px] font-bold text-text tracking-tighter leading-[0.9] text-center">
        <template v-for="(line, i) in title" :key="i">
          {{ line }}<br v-if="i < title.length - 1" />
        </template>
      </h2>
    </div>

    <FloatingCard v-for="card in cards" :key="card.id" :card="card" />

    <!-- Floating comment / profile -->
    <div
      class="absolute bg-white/30 backdrop-blur-3xl backdrop-saturate-[2] rounded-[24px] flex items-center gap-4 p-3 shadow-[0_12px_40px_rgba(0,0,0,0.1)] border border-white/50"
      style="left: 45%; top: 88%; width: max-content; z-index: 20;"
    >
      <div class="w-10 h-10 rounded-full bg-soft-card overflow-hidden shadow-sm border border-white/60">
        <img v-if="commentAvatar" class="w-full h-full object-cover" :src="commentAvatar" alt="" />
      </div>
      <div class="flex flex-col pr-4">
        <span class="font-label text-[11px] text-text font-bold">Camille Reynaud</span>
        <span class="font-body-md text-[13px] text-text/80 leading-tight">Devis prêt en 4 minutes !</span>
      </div>
    </div>
  </section>
</template>
