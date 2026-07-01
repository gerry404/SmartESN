<script setup lang="ts">
import BaseButton from '../ui/BaseButton.vue'
import type { NavLink } from '../../types'

defineProps<{
  brand: string
  links: NavLink[]
  cta: string
}>()
</script>

<template>
  <header
    class="fixed top-[24px] left-0 right-0 z-50 flex items-center justify-center px-sm w-full pointer-events-none"
  >
    <div
      class="pointer-events-auto w-full max-w-[960px] flex items-center justify-between bg-black  rounded-[60px] px-xs py-xs shadow-xl border border-white/10 h-[70px]"
    >
      <div class="flex items-center gap-md pl-xs">
        <div
          class="w-[34px] h-[34px] rounded-full border border-page-bg/80 flex items-center justify-center shrink-0"
        >
          <span class="text-page-bg font-bold text-sm leading-none"><img src="./assets/images/logo_smart.png"></span>
        </div>
        <nav class="hidden md:flex items-center gap-6">
          <a
            v-for="link in links"
            :key="link.label"
            class="font-label text-label text-page-bg/80 hover:text-page-bg transition-colors duration-300"
            :href="link.href"
            >{{ link.label }}</a
          >
        </nav>
      </div>
      <BaseButton variant="light" size="sm" class="font-semibold">{{ cta }}</BaseButton>
    </div>
  </header>
</template>
