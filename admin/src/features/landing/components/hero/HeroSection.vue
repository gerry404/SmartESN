<script setup lang="ts">
import BaseButton from '../ui/BaseButton.vue'
import type { HeroContent } from '../../types'

defineProps<{ hero: HeroContent }>()
</script>

<template>
  <section
    class="pt-[150px] px-margin max-w-[1728px] mx-auto flex flex-col items-center text-center relative overflow-visible bg-page-bg pb-[200px]"
  >
    <h1
      class="font-display text-display text-balance max-w-4xl mb-lg text-text tracking-tighter"
    >
      {{ hero.title }}
    </h1>
    <p class="font-body-lg text-body-lg text-muted max-w-2xl mb-lg">
      {{ hero.subtitle }}
    </p>
    <div class="flex flex-col sm:flex-row gap-4 mb-lg">
      <BaseButton variant="dark">{{ hero.primaryCta }}</BaseButton>
      <BaseButton variant="outline-dark">{{ hero.secondaryCta }}</BaseButton>
    </div>
    <div
      class="w-full max-w-[1492px] bg-panel-bg overflow-hidden shadow-2xl relative z-10 rounded-[40px] h-[800px]"
    >
      <img class="w-full h-full object-cover" :src="hero.image" :alt="hero.imageAlt" />
      <div class="absolute inset-0 bg-gradient-to-t from-black/20 to-transparent"></div>
    </div>
  </section>
</template>
