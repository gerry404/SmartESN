<script setup lang="ts">
// Generic glass card container used across the collage / floating UI.
</script>

<template>
  <div
    class="rounded-[32px] overflow-hidden bg-white/10 backdrop-blur-2xl backdrop-saturate-150 border border-white/40 shadow-2xl"
  >
    <slot />
  </div>
</template>
