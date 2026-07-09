<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'
import BaseButton from '../ui/BaseButton.vue'
import type { HeroContent } from '../../types'

defineProps<{ hero: HeroContent }>()

// Scroll-driven 3D reveal: the frame starts tilted back and straightens as
// you scroll (classic hero-image effect).
const MAX_TILT = 32 // deg
const THRESHOLD = 560 // px of scroll to fully straighten

const rot = ref(MAX_TILT)
const scale = ref(0.86)
let ticking = false
let reduce = false

const update = () => {
  ticking = false
  if (reduce) {
    rot.value = 0
    scale.value = 1
    return
  }
  const y = window.scrollY || window.pageYOffset || 0
  const p = Math.min(Math.max(y / THRESHOLD, 0), 1)
  rot.value = MAX_TILT * (1 - p)
  scale.value = 0.86 + 0.14 * p
}

const onScroll = () => {
  if (!ticking) {
    ticking = true
    requestAnimationFrame(update)
  }
}

onMounted(() => {
  reduce = window.matchMedia('(prefers-reduced-motion: reduce)').matches
  window.addEventListener('scroll', onScroll, { passive: true })
  update()
})
onBeforeUnmount(() => window.removeEventListener('scroll', onScroll))
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

    <!-- 3D reveal wrapper: perspective gives the vanishing point -->
    <div class="w-full max-w-[1492px] [perspective:1400px]">
      <div
        class="w-full bg-panel-bg overflow-hidden shadow-2xl relative z-10 rounded-[40px] h-[800px] will-change-transform"
        :style="{
          transform: `rotateX(${rot}deg) scale(${scale})`,
          transformOrigin: '50% 100%',
        }"
      >
        <img class="w-full h-full object-cover" :src="hero.image" :alt="hero.imageAlt" />
        <div class="absolute inset-0 bg-gradient-to-t from-black/20 to-transparent"></div>
      </div>
    </div>
  </section>
</template>
