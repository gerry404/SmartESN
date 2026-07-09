<script setup lang="ts">
import { computed } from 'vue'
import FeatureItem from './FeatureItem.vue'
import type { FeatureItem as FeatureItemType } from '../../types'

const props = defineProps<{
  items: FeatureItemType[]
  activeIndex?: number
}>()

const active = computed(() => props.activeIndex ?? 0)
const barHeight = computed(() => `${((active.value + 1) / props.items.length) * 100}%`)
</script>

<template>
  <div class="relative pl-8 border-l-[3px] border-line space-y-12">
    <div
      class="absolute left-[-3px] top-0 w-[3px] bg-gradient-to-b from-brand-from to-brand-to transition-[height] duration-500 ease-out"
      :style="{ height: barHeight }"
    ></div>
    <FeatureItem
      v-for="(item, i) in items"
      :key="item.title"
      :item="item"
      :active="i === active"
    />
  </div>
</template>
