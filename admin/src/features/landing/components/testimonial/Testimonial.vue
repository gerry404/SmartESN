<script setup lang="ts">
import BaseIcon from '../ui/BaseIcon.vue'
import type { Testimonial } from '../../types'

defineProps<{ testimonial: Testimonial }>()
</script>

<template>
  <section class="py-[160px] px-margin max-w-[1200px] mx-auto flex flex-col items-center text-center bg-page-bg">
    <div class="mb-12 opacity-30 text-transparent bg-clip-text bg-gradient-to-r from-brand-from to-brand-to">
      <BaseIcon name="format_quote" class="text-[64px]" />
    </div>
    <h2
      class="font-display text-[40px] md:text-[56px] leading-[1.1] font-bold tracking-tight text-text mb-16 text-balance"
    >
      « {{ testimonial.quote }} »
    </h2>
    <div class="flex items-center gap-4">
      <div class="w-14 h-14 rounded-full bg-line overflow-hidden shadow-md">
        <img class="w-full h-full object-cover" :src="testimonial.avatar" :alt="testimonial.author" />
      </div>
      <div class="text-left">
        <p class="font-label text-sm font-bold text-text">{{ testimonial.author }}</p>
        <p class="font-label text-xs text-muted">{{ testimonial.role }}</p>
      </div>
    </div>
  </section>
</template>
