<script setup lang="ts">
defineProps<{ name: string }>()
</script>

<template>
  <span
    class="font-h3 text-h3 font-semibold tracking-tight text-muted transition-all duration-300 cursor-default hover:opacity-100"
    >{{ name }}</span
  >
</template>
