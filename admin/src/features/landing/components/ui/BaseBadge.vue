<script setup lang="ts">
import BaseIcon from './BaseIcon.vue'

withDefaults(
  defineProps<{
    icon?: string
    label?: string
    dot?: boolean
  }>(),
  { icon: '', label: '', dot: false },
)
</script>

<template>
  <div
    class="bg-white/40 backdrop-blur-xl px-4 py-2 rounded-full border border-white/50 shadow-sm flex items-center gap-2"
  >
    <span
      v-if="dot"
      class="w-2 h-2 rounded-full bg-green-500 shadow-[0_0_8px_rgba(34,197,94,0.8)]"
    ></span>
    <BaseIcon v-if="icon" :name="icon" class="text-text text-sm" />
    <span
      v-if="label"
      class="font-label text-[10px] font-bold text-text uppercase tracking-wider"
      >{{ label }}</span
    >
  </div>
</template>
