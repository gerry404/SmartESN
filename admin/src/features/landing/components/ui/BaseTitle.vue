<script setup lang="ts">
import { computed } from 'vue'

type As = 'h1' | 'h2' | 'h3' | 'h4'

const props = withDefaults(
  defineProps<{
    as?: As
    size?: string
  }>(),
  { as: 'h2', size: 'font-display text-h2' },
)

const classes = computed(() => [props.size, 'font-bold tracking-tight text-text text-balance'])
</script>

<template>
  <component :is="as" :class="classes">
    <slot />
  </component>
</template>
