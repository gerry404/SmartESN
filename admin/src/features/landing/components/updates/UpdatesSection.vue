<script setup lang="ts">
import BlogCard from './BlogCard.vue'
import BaseTitle from '../ui/BaseTitle.vue'
import type { HomeContent } from '../../types'

defineProps<{ updates: HomeContent['updates'] }>()
</script>

<template>
  <section id="ressources" class="py-[120px] px-margin max-w-[1728px] mx-auto bg-page-bg">
    <div class="flex justify-between items-end mb-12">
      <BaseTitle as="h2">{{ updates.title }}</BaseTitle>
      <button
        class="font-label text-sm font-bold border-b border-black text-text pb-1 hover:text-brand-from hover:border-brand-from transition-colors"
      >
        Tout voir
      </button>
    </div>
    <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
      <BlogCard v-for="item in updates.items" :key="item.title" :update="item" />
    </div>
  </section>
</template>
