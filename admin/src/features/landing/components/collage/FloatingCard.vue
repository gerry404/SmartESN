<script setup lang="ts">
import { computed } from 'vue'
import BaseIcon from '../ui/BaseIcon.vue'
import type { CollageCard } from '../../types'

const props = defineProps<{ card: CollageCard }>()

const hasProgress = computed(() => props.card.progressValue != null)
const iconOnly = computed(() => !!props.card.badge?.icon && !props.card.badge?.label)
const labelOnly = computed(() => !props.card.badge?.icon && !!props.card.badge?.label)
const iconLabel = computed(() => !!props.card.badge?.icon && !!props.card.badge?.label)
</script>

<template>

  <div
    v-if="card.type === 'toggle'"
    class="absolute rounded-[28px] overflow-hidden bg-white/20 backdrop-blur-3xl backdrop-saturate-[1.8] border border-white/50 shadow-[0_12px_40px_rgba(0,0,0,0.08)] flex flex-col p-5"
    :style="card.style"
  >
    <div class="flex items-center justify-between mb-4">
      <span class="font-label text-xs font-semibold text-text">{{ card.title }}</span>
      <div class="w-10 h-6 bg-black rounded-full relative shadow-inner cursor-pointer">
        <div class="absolute right-1 top-1 w-4 h-4 bg-white rounded-full shadow-sm"></div>
      </div>
    </div>
    <div class="flex items-center gap-3">
      <div
        class="w-10 h-10 rounded-full bg-gradient-to-tr from-brand-from to-brand-to flex items-center justify-center shadow-md"
      >
        <BaseIcon name="sync" class="text-white text-sm" />
      </div>
      <div class="flex flex-col">
        <span class="font-label text-[11px] font-bold text-text">{{ card.title }}</span>
        <span class="font-body-md text-[12px] text-muted leading-tight">{{ card.subtitle }}</span>
      </div>
    </div>
  </div>

  
  <div
    v-else
    class="absolute rounded-[32px] overflow-hidden bg-white/10 backdrop-blur-2xl backdrop-saturate-150 border border-white/40 shadow-2xl"
    :style="card.style"
  >
    <img class="w-full h-full object-cover" :src="card.image" :alt="card.imageAlt ?? ''" />

   
    <div
      v-if="hasProgress"
      class="absolute bottom-4 left-4 right-4 bg-white/30 backdrop-blur-xl border border-white/40 rounded-2xl p-4 shadow-lg"
    >
      <div class="flex justify-between items-center mb-2">
        <span class="font-label text-[10px] text-text font-bold uppercase tracking-wider">{{
          card.progressLabel
        }}</span>
        <span class="font-label text-[10px] text-text font-bold">{{ card.progressValue }}%</span>
      </div>
      <div class="w-full bg-black/10 rounded-full h-1.5">
        <div class="bg-black h-1.5 rounded-full" :style="{ width: `${card.progressValue}%` }"></div>
      </div>
    </div>

    
    <div
      v-if="iconOnly"
      class="absolute top-4 right-4 w-8 h-8 rounded-full bg-white/40 backdrop-blur-md border border-white/50 flex items-center justify-center shadow-sm"
    >
      <BaseIcon :name="card.badge!.icon" class="text-text text-sm" />
    </div>

    
    <div
      v-if="labelOnly"
      class="absolute top-4 left-4 bg-white/40 backdrop-blur-xl px-4 py-2 rounded-full border border-white/50 shadow-sm flex items-center gap-2"
    >
      <div class="w-2 h-2 rounded-full bg-green-500 shadow-[0_0_8px_rgba(34,197,94,0.8)]"></div>
      <span class="font-label text-[10px] font-bold text-text uppercase tracking-wider">{{
        card.badge!.label
      }}</span>
    </div>

   
    <div
      v-if="iconLabel"
      class="absolute bottom-5 left-5 bg-white/40 backdrop-blur-xl px-4 py-2 rounded-full border border-white/50 shadow-sm flex items-center gap-2"
    >
      <BaseIcon :name="card.badge!.icon" class="text-text text-sm" />
      <span class="font-label text-[10px] font-bold text-text uppercase tracking-wider">{{
        card.badge!.label
      }}</span>
    </div>
  </div>
</template>
