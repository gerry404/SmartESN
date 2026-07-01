<script setup lang="ts">
import FeatureItem from './FeatureItem.vue'
import type { FeatureItem as FeatureItemType } from '../../types'

defineProps<{ items: FeatureItemType[] }>()
</script>

<template>
  <div class="relative pl-8 border-l-[3px] border-line space-y-12">
    <div
      class="absolute left-[-3px] top-0 w-[3px] h-1/3 bg-gradient-to-b from-brand-from to-brand-to"
    ></div>
    <FeatureItem
      v-for="(item, i) in items"
      :key="item.title"
      :item="item"
      :active="i === 0"
    />
  </div>
</template>
