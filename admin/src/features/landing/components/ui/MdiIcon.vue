<script setup lang="ts">
// Icône SVG à partir d'un chemin @mdi/js (tree-shakeable).
// Usage : <MdiIcon :path="mdiArrowLeft" />
defineProps<{ path: string }>()
</script>

<template>
  <svg
    viewBox="0 0 24 24"
    fill="currentColor"
    class="inline-block w-[1em] h-[1em] align-[-0.15em] shrink-0"
    aria-hidden="true"
  >
    <path :d="path" />
  </svg>
</template>
