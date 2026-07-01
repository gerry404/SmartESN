<script setup lang="ts">
import type { Update } from '../../types'

defineProps<{ update: Update }>()
</script>

<template>
  <div class="group cursor-pointer">
    <img
      :alt="update.imageAlt"
      class="w-full h-[450px] rounded-[40px] mb-6 overflow-hidden shadow-xl border border-white/20 object-cover"
      :src="update.image"
    />
    <p class="font-label text-xs text-muted mb-3">{{ update.category }}</p>
    <h4 class="font-h3 text-xl font-bold text-text group-hover:text-black transition-colors">
      {{ update.title }}
    </h4>
  </div>
</template>
