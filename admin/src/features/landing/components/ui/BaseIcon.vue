<script setup lang="ts">
defineProps<{ name: string }>()
</script>

<template>
  <span class="material-symbols-outlined leading-none">{{ name }}</span>
</template>
