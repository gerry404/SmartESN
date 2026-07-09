<script setup lang="ts">
import type { TeamCard } from '../../types'

defineProps<{ card: TeamCard }>()
</script>

<template>
  <div
    class="relative h-[400px] rounded-[32px] p-8 flex flex-col justify-end group overflow-hidden shadow-sm border border-line/20"
  >
    <img
      :alt="card.imageAlt"
      class="absolute inset-0 w-full h-full object-cover transition-transform duration-500 group-hover:scale-110"
      :src="card.image"
    />
    <div class="absolute inset-0 bg-black/40"></div>
    <div class="relative z-10">
      <h4 class="font-h3 text-xl font-bold mb-2 text-white">{{ card.title }}</h4>
      <p class="font-body-md text-white/80">{{ card.description }}</p>
    </div>
  </div>
</template>
