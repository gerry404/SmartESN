<script setup lang="ts">
import type { TeamTab } from '../../types'

defineProps<{
  tabs: TeamTab[]
  active: string
}>()

defineEmits<{ (e: 'update:active', id: string): void }>()
</script>

<template>
  <div class="flex flex-wrap justify-center gap-3 mb-16">
    <button
      v-for="tab in tabs"
      :key="tab.id"
      class="px-6 py-2 rounded-full font-label text-sm transition-colors"
      :class="
        tab.id === active
          ? 'bg-black text-page-bg shadow-md'
          : 'bg-white text-text hover:bg-gray-50 shadow-sm'
      "
      @click="$emit('update:active', tab.id)"
    >
      {{ tab.label }}
    </button>
  </div>
</template>
