<script setup lang="ts">
withDefaults(
  defineProps<{
    /** Text colour context: 'dark' on light bg, 'light' on dark bg. */
    variant?: 'dark' | 'light'
    /** Show the "SmartESN" wordmark next to the mark. */
    wordmark?: boolean
    /** 'gradient' = brand gradient, 'mono' = follows text colour, 'stack' = layered red/white/ink. */
    mark?: 'gradient' | 'mono' | 'stack'
  }>(),
  { variant: 'dark', wordmark: true, mark: 'gradient' },
)
</script>

<template>
  <div
    class="inline-flex items-center gap-2.5 select-none"
    :class="variant === 'light' ? 'text-page-bg' : 'text-text'"
  >
    <svg viewBox="0 0 64 64" class="h-7 w-7 shrink-0" fill="none" aria-hidden="true">
      <defs>
        <linearGradient id="smartesn-mark-grad" x1="12" y1="8" x2="52" y2="56" gradientUnits="userSpaceOnUse">
          <stop stop-color="#3F8F2E" />
          <stop offset="1" stop-color="#A6D84B" />
        </linearGradient>
        <path
          id="smartesn-mark-path"
          d="M46 21C46 13.8 39 11 32 11C24 11 18 15 18 22C18 29 25 31.5 32 32C39 32.5 46 35 46 42C46 49 40 53 32 53C25 53 18 50.2 18 43"
        />
      </defs>

      <!-- Stacked variant: ink → white → red, slightly offset -->
      <g
        v-if="mark === 'stack'"
        stroke-width="8.5"
        stroke-linecap="round"
        stroke-linejoin="round"
        transform="translate(-2.4 -2.4)"
      >
        <!-- ordre (avant → arrière) : vert · noir · blanc -->
        <use href="#smartesn-mark-path" transform="translate(4.8 4.8)" stroke="#FAF9F5" />
        <use href="#smartesn-mark-path" transform="translate(2.4 2.4)" stroke="#181818" />
        <use href="#smartesn-mark-path" stroke="url(#smartesn-mark-grad)" />
      </g>

      <!-- Single stroke -->
      <use
        v-else
        href="#smartesn-mark-path"
        :stroke="mark === 'gradient' ? 'url(#smartesn-mark-grad)' : 'currentColor'"
        stroke-width="9"
        stroke-linecap="round"
        stroke-linejoin="round"
      />
    </svg>

    <span v-if="wordmark" class="font-display text-xl font-bold tracking-tight leading-none">
      Smart<span class="bg-gradient-to-r from-brand-from to-brand-to bg-clip-text text-transparent">ESN</span>
    </span>
  </div>
</template>
