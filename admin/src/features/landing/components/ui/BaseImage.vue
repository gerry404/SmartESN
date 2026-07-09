<script setup lang="ts">
defineProps<{
  src: string
  alt?: string
}>()
</script>

<template>
  <img class="w-full h-full object-cover" :src="src" :alt="alt ?? ''" loading="lazy" />
</template>
