<script setup lang="ts">
defineProps<{ text: string }>()
</script>

<template>
  <section
    class="py-[200px] px-margin max-w-[1728px] mx-auto relative flex flex-col items-center text-center bg-page-bg"
  >
    <div class="absolute top-0 right-[10%] w-[400px] h-[400px] opacity-10 pointer-events-none">
      <svg class="w-full h-full" fill="none" stroke="url(#manifesto-grad)" stroke-width="0.5" viewBox="0 0 100 100">
        <defs>
          <linearGradient id="manifesto-grad" x1="0%" x2="100%" y1="0%" y2="100%">
            <stop offset="0%" stop-color="#FF416C" />
            <stop offset="100%" stop-color="#FF4B2B" />
          </linearGradient>
        </defs>
        <circle cx="50" cy="50" r="48" />
        <ellipse cx="50" cy="50" rx="24" ry="48" />
        <ellipse cx="50" cy="50" rx="48" ry="24" />
        <line x1="2" x2="98" y1="50" y2="50" />
        <line x1="50" x2="50" y1="2" y2="98" />
      </svg>
    </div>
    <h2
      class="font-display text-[49px] leading-[1.1] text-balance max-w-[1200px] font-bold tracking-tight text-text relative z-10"
    >
      {{ text }}
    </h2>
  </section>
</template>
