<script setup lang="ts">
import BaseButton from './BaseButton.vue'
import type { CtaContent } from '../../types'

defineProps<{ cta: CtaContent }>()
</script>

<template>
  <section class="py-section px-margin">
    <div class="max-w-screen-xl mx-auto flex flex-col items-center text-center">
      <div class="relative z-10 flex flex-col items-center">
        <div class="w-12 h-12 rounded-full border border-text flex items-center justify-center mb-12">
          <span class="text-text font-bold text-xl leading-none">S</span>
        </div>
        <h2
          class="font-display text-[56px] md:text-[80px] font-bold text-text tracking-tighter leading-[1.05] mb-12 max-w-4xl text-balance"
        >
          {{ cta.title }}
        </h2>
        <div class="flex flex-col sm:flex-row gap-6">
          <BaseButton variant="dark">{{ cta.primaryCta }}</BaseButton>
          <BaseButton variant="outline-dark">{{ cta.secondaryCta }}</BaseButton>
        </div>
      </div>
    </div>
  </section>
</template>
