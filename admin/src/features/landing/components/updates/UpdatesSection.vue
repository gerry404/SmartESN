<script setup lang="ts">
import { computed, ref } from 'vue'
import BaseTitle from '../ui/BaseTitle.vue'
import BaseIcon from '../ui/BaseIcon.vue'
import type { HomeContent } from '../../types'

const props = defineProps<{ updates: HomeContent['updates'] }>()

// Index de l'article mis en avant. Cliquer sur un autre le remplace.
const active = ref(0)

const featured = computed(() => props.updates.items[active.value])
// Les autres articles (avec leur index d'origine, pour pouvoir les sélectionner).
const rest = computed(() =>
  props.updates.items
    .map((item, index) => ({ item, index }))
    .filter((entry) => entry.index !== active.value),
)
</script>

<template>
  <section id="ressources" class="py-[120px] px-margin max-w-[1728px] mx-auto bg-page-bg">
    <div class="flex justify-between items-end mb-14">
      <BaseTitle as="h2">{{ updates.title }}</BaseTitle>
      <button
        class="group inline-flex items-center gap-2 font-label text-sm font-bold text-text transition-colors hover:text-brand-from"
      >
        Tout voir
        <BaseIcon
          name="arrow_forward"
          class="text-lg transition-transform duration-300 group-hover:translate-x-1"
        />
      </button>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-12 gap-10 lg:gap-16 items-start">
      <!-- Article à la une (celui sélectionné) -->
      <div class="lg:col-span-7">
        <Transition name="swap" mode="out-in">
          <article v-if="featured" :key="active" class="group cursor-pointer">
            <div class="relative overflow-hidden rounded-[32px] mb-7 h-[320px] sm:h-[520px]">
              <img
                :src="featured.image"
                :alt="featured.imageAlt"
                class="w-full h-full object-cover transition-transform duration-700 ease-out group-hover:scale-[1.04]"
              />
            </div>
            <p class="font-label text-[12px] font-bold uppercase tracking-[0.15em] text-muted mb-4">
              {{ featured.category }}
            </p>
            <h3
              class="font-display text-3xl lg:text-[40px] font-bold tracking-tight text-text leading-[1.1] max-w-2xl"
            >
              {{ featured.title }}
            </h3>
            <span class="inline-flex items-center gap-2 mt-6 font-label text-sm font-bold text-text">
              Lire l'article
              <BaseIcon
                name="north_east"
                class="text-lg transition-transform duration-300 group-hover:translate-x-1 group-hover:-translate-y-1"
              />
            </span>
          </article>
        </Transition>
      </div>

      <!-- Les autres  cliquer pour les mettre à la une -->
      <div class="lg:col-span-5 border-t border-line">
        <article
          v-for="entry in rest"
          :key="entry.item.title"
          class="group cursor-pointer flex items-center gap-6 py-7 border-b border-line"
          role="button"
          tabindex="0"
          @click="active = entry.index"
          @keydown.enter="active = entry.index"
          @keydown.space.prevent="active = entry.index"
        >
          <div class="relative shrink-0 w-[120px] sm:w-[140px] h-[86px] sm:h-[100px] rounded-2xl overflow-hidden">
            <img
              :src="entry.item.image"
              :alt="entry.item.imageAlt"
              class="w-full h-full object-cover transition-transform duration-700 ease-out group-hover:scale-105"
            />
          </div>
          <div class="min-w-0">
            <p class="font-label text-[11px] font-bold uppercase tracking-[0.12em] text-muted mb-2">
              {{ entry.item.category }}
            </p>
            <h4
              class="font-h3 text-base sm:text-lg font-bold text-text leading-snug transition-colors group-hover:text-brand-from"
            >
              {{ entry.item.title }}
            </h4>
          </div>
          <BaseIcon
            name="north_east"
            class="ml-auto shrink-0 text-muted-light text-xl transition-all duration-300 group-hover:text-text group-hover:-translate-y-0.5 group-hover:translate-x-0.5"
          />
        </article>
      </div>
    </div>
  </section>
</template>

<style scoped>
.swap-enter-active,
.swap-leave-active {
  transition:
    opacity 0.35s ease,
    transform 0.35s cubic-bezier(0.22, 1, 0.36, 1);
}
.swap-enter-from {
  opacity: 0;
  transform: translateY(12px);
}
.swap-leave-to {
  opacity: 0;
  transform: translateY(-12px);
}
</style>
