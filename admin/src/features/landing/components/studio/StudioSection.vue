<script setup lang="ts">
import FeatureList from '../features/FeatureList.vue'
import BaseTitle from '../ui/BaseTitle.vue'
import BaseIcon from '../ui/BaseIcon.vue'
import type { HomeContent } from '../../types'

defineProps<{ studio: HomeContent['studio'] }>()
</script>

<template>
  <section id="solutions" class="py-[120px] px-margin max-w-[1728px] mx-auto bg-page-bg">
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-[100px] items-center">
      <!-- Copy -->
      <div class="flex flex-col justify-center h-full max-w-lg order-2 lg:order-1">
        <BaseTitle as="h3" class="mb-12">{{ studio.title }}</BaseTitle>
        <FeatureList :items="studio.items" />
      </div>

      <!-- Visual panel: Studio prompt -->
      <div
        class="bg-gradient-to-tr from-[#E0EAFC] to-[#CFDEF3] rounded-[40px] p-8 h-[700px] flex items-center justify-center relative overflow-hidden border border-line/30 shadow-inner order-1 lg:order-2"
      >
        <div class="w-full max-w-lg bg-white/80 backdrop-blur-xl rounded-3xl shadow-2xl p-8 border border-white/50">
          <div class="flex items-center gap-3 mb-6">
            <div
              class="w-[24px] h-[24px] rounded-full border border-black flex items-center justify-center shrink-0"
            >
              <span class="text-black font-bold text-[10px] leading-none">S</span>
            </div>
            <span class="font-label text-xs font-bold text-muted">{{ studio.promptLabel }}</span>
          </div>
          <div class="bg-white/60 backdrop-blur-md rounded-xl p-4 mb-6 border border-white/50 shadow-sm">
            <p class="font-body-md text-text">{{ studio.promptText }}</p>
          </div>
          <div class="flex justify-end">
            <div
              class="w-8 h-8 rounded-full bg-gradient-to-r from-brand-from to-brand-to flex items-center justify-center shadow-md animate-pulse"
            >
              <BaseIcon name="auto_awesome" class="text-white text-sm" />
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>
