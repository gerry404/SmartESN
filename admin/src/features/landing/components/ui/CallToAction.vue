<script setup lang="ts">
import { defineAsyncComponent } from 'vue'
import BaseButton from './BaseButton.vue'
import type { CtaContent } from '../../types'
// Lazy-loaded so Three.js ships in its own chunk, only for this section.
const BrainScene = defineAsyncComponent(() => import('../cta/BrainScene.vue'))

defineProps<{ cta: CtaContent }>()
</script>

<template>
  <section class="px-margin pb-[80px]">
    <div
      class="relative max-w-[1728px] mx-auto rounded-[40px] overflow-hidden bg-deep-black min-h-[760px] flex items-center justify-center"
    >
      
      <BrainScene />

      
      <div class="pointer-events-none absolute inset-0 vignette"></div>

     
      <div class="relative z-10 flex flex-col items-center text-center px-6 py-24 pointer-events-none">
        <!-- <div class="w-12 h-12 rounded-full border border-white/40 flex items-center justify-center mb-10 backdrop-blur-sm">
           <span class="text-white font-bold text-xl leading-none">S</span>
        </div>-->
        <h2
          class="font-display text-[44px] md:text-[76px] font-bold text-white tracking-tighter leading-[1.03] mb-10 max-w-4xl text-balance"
        >
          {{ cta.title }}
        </h2>
        <div class="flex flex-col sm:flex-row gap-5 pointer-events-auto">
          <BaseButton variant="light">{{ cta.primaryCta }}</BaseButton>
          <BaseButton variant="outline-light">{{ cta.secondaryCta }}</BaseButton>
        </div>
        <p class="mt-8 font-label text-[11px] uppercase tracking-[0.2em] text-white/40">
          Glissez pour explorer
        </p>
      </div>
    </div>
  </section>
</template>

<style scoped>
.vignette {
  background: radial-gradient(
    ellipse 70% 60% at 50% 50%,
    rgba(14, 23, 18, 0.55) 0%,
    rgba(14, 23, 18, 0.15) 45%,
    transparent 75%
  );
}
</style>
