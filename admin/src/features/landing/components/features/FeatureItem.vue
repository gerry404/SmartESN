<script setup lang="ts">
import type { FeatureItem } from '../../types'

defineProps<{
  item: FeatureItem
  active?: boolean
}>()
</script>

<template>
  <div :class="active ? '' : 'opacity-50 hover:opacity-100 transition-opacity'">
    <h4 class="font-h3 text-xl font-bold mb-3 text-text">{{ item.title }}</h4>
    <p class="font-body-md text-muted leading-relaxed">{{ item.description }}</p>
  </div>
</template>
