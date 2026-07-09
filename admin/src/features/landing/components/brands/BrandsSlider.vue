<script setup lang="ts">
import BrandItem from './BrandItem.vue'
import type { Brand } from '../../types'

const props = defineProps<{ brands: Brand[] }>()


const loop = [...props.brands, ...props.brands]
</script>

<template>
  <section
    class="py-xl px-margin border-t border-line/30 max-w-[1728px] mx-auto flex flex-col items-center bg-page-bg relative z-0 pt-[40px]"
  >
    <p
      class="font-label text-label text-muted mb-md uppercase tracking-widest text-center"
    >
      Ils qualifient et chiffrent avec SmartESN
    </p>
    <div class="flex justify-center items-center text-muted w-full">
      <div class="marquee-container">
        <div class="marquee-content animate-marquee flex gap-[100px] items-center text-muted">
          <BrandItem v-for="(brand, i) in loop" :key="`${brand.name}-${i}`" :name="brand.name" />
        </div>
      </div>
    </div>
  </section>
</template>
