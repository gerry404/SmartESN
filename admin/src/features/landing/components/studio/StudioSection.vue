<script setup lang="ts">
import { computed, ref } from 'vue'
import FeatureList from '../features/FeatureList.vue'
import BaseTitle from '../ui/BaseTitle.vue'
import StudioVisual from './StudioVisual.vue'
import { useScrollSteps } from '../../composables/useScrollSteps'
import type { HomeContent } from '../../types'

const props = defineProps<{ studio: HomeContent['studio'] }>()

const wrapper = ref<HTMLElement | null>(null)
const { progress, enabled } = useScrollSteps(wrapper)

const keys = ['chiffrage', 'hypotheses', 'historique']
const stepCount = computed(() => props.studio.items.length)
const activeStep = computed(() =>
  enabled.value ? Math.min(stepCount.value - 1, Math.floor(progress.value * stepCount.value)) : 0,
)
const variantKey = computed(() => keys[activeStep.value] ?? 'chiffrage')
const wrapperStyle = computed(() =>
  enabled.value ? { height: `${(stepCount.value + 1) * 100}vh` } : {},
)
</script>

<template>
  <section ref="wrapper" id="solutions" class="relative bg-page-bg" :style="wrapperStyle">
    <div class="lg:sticky lg:top-0 lg:h-screen">
      <div class="lg:h-full flex items-center px-margin py-[120px] lg:py-0">
        <div
          class="max-w-[1728px] mx-auto w-full grid grid-cols-1 lg:grid-cols-2 gap-[100px] items-center"
        >
          <!-- Copy -->
          <div class="flex flex-col justify-center h-full max-w-lg order-2 lg:order-1">
            <BaseTitle as="h3" class="mb-12">{{ studio.title }}</BaseTitle>
            <FeatureList :items="studio.items" :active-index="activeStep" />
          </div>

          <!-- Visual panel: changes with the active step -->
          <div
            class="bg-gradient-to-tr from-[#E3F0CE] to-[#D3E7B4] rounded-[40px] p-8 h-[700px] flex items-center justify-center relative overflow-hidden border border-line/30 shadow-inner order-1 lg:order-2"
          >
            <Transition name="studio-swap" mode="out-in">
              <StudioVisual :key="activeStep" :variant="variantKey" :prompt="studio.promptText" />
            </Transition>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.studio-swap-enter-active,
.studio-swap-leave-active {
  transition:
    opacity 0.35s ease,
    transform 0.35s cubic-bezier(0.22, 1, 0.36, 1);
}
.studio-swap-enter-from {
  opacity: 0;
  transform: translateY(14px) scale(0.98);
}
.studio-swap-leave-to {
  opacity: 0;
  transform: translateY(-14px) scale(0.98);
}
</style>
