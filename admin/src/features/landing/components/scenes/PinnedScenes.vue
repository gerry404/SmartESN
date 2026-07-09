<script setup lang="ts">
import { computed, ref } from 'vue'
import { useScrollSteps } from '../../composables/useScrollSteps'

const props = defineProps<{ count: number; eyebrow?: string }>()

const wrapper = ref<HTMLElement | null>(null)
const { progress, enabled } = useScrollSteps(wrapper)

const activeStep = computed(() =>
  enabled.value ? Math.min(props.count - 1, Math.floor(progress.value * props.count)) : -1,
)

// One extra viewport so every scene gets a full screen of dwell time.
const wrapperStyle = computed(() =>
  enabled.value ? { height: `${(props.count + 1) * 100}vh` } : {},
)

// NOTE: scene classes are styled globally (main.css) because scenes are
// provided via <slot> by the parent section — scoped CSS here would not reach
// slotted content.
const sceneClass = (i: number) => {
  if (!enabled.value) return 'pinned-scene pinned-scene--flow'
  if (i === activeStep.value) return 'pinned-scene pinned-scene--active'
  return i < activeStep.value ? 'pinned-scene pinned-scene--past' : 'pinned-scene pinned-scene--future'
}

const pad = (n: number) => String(n).padStart(2, '0')
</script>

<template>
  <section ref="wrapper" class="relative bg-page-bg" :style="wrapperStyle">
    <div class="lg:sticky lg:top-0 lg:h-screen relative overflow-hidden">
      <!-- Scroll progress bar  -->
      <div v-if="enabled" class="absolute top-0 left-0 right-0 h-[3px] bg-line/40 z-30">
        <div
          class="h-full bg-gradient-to-r from-brand-from to-brand-to transition-[width] duration-150 ease-out"
          :style="{ width: `${progress * 100}%` }"
        ></div>
      </div>

      <!-- Step counter -->
      <div
        v-if="enabled"
        class="absolute top-9 right-margin z-30 flex items-center gap-3 font-label text-[12px] font-bold tabular-nums"
      >
        <span v-if="eyebrow" class="uppercase tracking-wider text-muted">{{ eyebrow }}</span>
        <span class="text-text">{{ pad(activeStep + 1) }}</span>
        <span class="text-muted-light">/ {{ pad(count) }}</span>
      </div>

      <slot :active-step="activeStep" :enabled="enabled" :scene-class="sceneClass" />
    </div>
  </section>
</template>
