<script setup lang="ts">
import { computed } from 'vue'
import PinnedScenes from '../scenes/PinnedScenes.vue'
import FeatureVisual from './FeatureVisual.vue'
import greenTexture from '@/assets/images/hero-frame.jpg'
import type { FeatureBlock } from '../../types'

const props = defineProps<{ block: FeatureBlock }>()

const keys = ['analyse', 'qualif', 'source']
const scenes = computed(() =>
  props.block.items.map((item, i) => ({ ...item, key: keys[i] ?? 'source' })),
)
// Nuances de vert (extraites de l'image) : le fond s'assombrit à chaque step.
const stepColors = ['#F2F6EA', '#E4EFCE', '#D2E4B0']
const pad = (n: number) => String(n).padStart(2, '0')
</script>

<template>
  <PinnedScenes id="produit" :count="scenes.length" :eyebrow="block.eyebrow" :step-colors="stepColors">
    <template #default="{ activeStep, enabled, sceneClass }">
      <!-- Halo d'ambiance vert (image), persistant derrière les scènes -->
      <img
        :src="greenTexture"
        alt=""
        aria-hidden="true"
        class="hidden lg:block pointer-events-none absolute -right-48 top-1/2 -translate-y-1/2 w-[620px] h-[620px] object-cover rounded-full opacity-25 blur-2xl"
      />
      <div v-for="(scene, i) in scenes" :key="scene.title" :class="sceneClass(i)">
        <div class="lg:h-full flex items-center px-margin py-[80px] lg:py-0">
          <div
            class="max-w-[1728px] mx-auto w-full grid grid-cols-1 lg:grid-cols-2 gap-[64px] xl:gap-[100px] items-center lg:h-[560px]"
          >
            <!-- Copy -->
            <div class="flex flex-col justify-center max-w-xl">
              <div
                class="reveal flex items-center gap-3 mb-6"
                :class="!enabled || activeStep === i ? 'is-in' : ''"
                style="transition-delay: 80ms"
              >
                <span class="font-display text-5xl font-bold bg-gradient-to-r from-brand-from to-brand-to bg-clip-text text-transparent leading-none">{{ pad(i + 1) }}</span>
                <span class="font-label text-[11px] font-bold uppercase tracking-wider text-muted">{{ block.eyebrow }}</span>
              </div>
              <h3
                class="reveal font-display text-3xl sm:text-4xl lg:text-5xl font-bold tracking-tight text-text mb-6 leading-[1.05]"
                :class="!enabled || activeStep === i ? 'is-in' : ''"
                style="transition-delay: 160ms"
              >
                {{ scene.title }}
              </h3>
              <p
                class="reveal font-body-lg text-muted leading-relaxed"
                :class="!enabled || activeStep === i ? 'is-in' : ''"
                style="transition-delay: 240ms"
              >
                {{ scene.description }}
              </p>
            </div>

            <!-- Visual -->
            <div
              class="reveal reveal--visual order-first lg:order-none flex items-center justify-center lg:h-full"
              :class="!enabled || activeStep === i ? 'is-in' : ''"
              style="transition-delay: 200ms"
            >
              <FeatureVisual :variant="scene.key" :active="!enabled || activeStep === i" />
            </div>
          </div>
        </div>
      </div>
    </template>
  </PinnedScenes>
</template>

<style scoped>
.reveal {
  transition:
    opacity 0.6s cubic-bezier(0.22, 1, 0.36, 1),
    transform 0.6s cubic-bezier(0.22, 1, 0.36, 1);
}
@media (min-width: 1024px) {
  .reveal {
    opacity: 0;
    transform: translateY(10px);
  }
  .reveal--visual {
    transform: translateY(14px) scale(0.99);
  }
  .reveal.is-in {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}
@media (prefers-reduced-motion: reduce) {
  .reveal {
    opacity: 1 !important;
    transform: none !important;
  }
}
</style>
