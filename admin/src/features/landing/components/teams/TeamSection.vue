<script setup lang="ts">
import { computed, ref } from 'vue'
import TeamsTabs from './TeamsTabs.vue'
import TeamsCard from './TeamsCard.vue'
import BaseTitle from '../ui/BaseTitle.vue'
import type { HomeContent } from '../../types'

const props = defineProps<{ teams: HomeContent['teams'] }>()

const active = ref(props.teams.tabs[0]?.id ?? '')

const labelOf = (id: string) =>
  props.teams.tabs.find((t) => t.id === id)?.label ?? id

const pad = (n: number) => String(n).padStart(2, '0')
</script>

<template>
  <section
    id="a-propos"
    class="py-[160px] px-margin max-w-[1728px] mx-auto flex flex-col items-center bg-page-bg"
  >
    <BaseTitle as="h2" class="mb-12 text-center">{{ teams.title }}</BaseTitle>

    <TeamsTabs :tabs="teams.tabs" :active="active" @update:active="active = $event" />

    <!-- Desktop: horizontal expanding accordion -->
    <div class="hidden md:flex gap-4 w-full h-[520px]">
      <div
        v-for="(card, i) in teams.cards"
        :key="card.tab"
        class="group relative basis-0 min-w-0 rounded-[32px] overflow-hidden cursor-pointer shadow-sm border border-line/20 transition-[flex-grow] duration-700 ease-[cubic-bezier(0.22,1,0.36,1)]"
        :style="{ flexGrow: active === card.tab ? 5 : 1 }"
        @mouseenter="active = card.tab"
        @click="active = card.tab"
      >
        <img
          :src="card.image"
          :alt="card.imageAlt"
          class="absolute inset-0 w-full h-full object-cover transition-transform duration-700 ease-out"
          :class="active === card.tab ? 'scale-105' : 'scale-100'"
        />
        <div
          class="absolute inset-0 bg-gradient-to-t from-black/85 via-black/35 to-black/10 transition-opacity duration-500"
          :class="active === card.tab ? 'opacity-100' : 'opacity-95'"
        ></div>

        <!-- Step number -->
        <span class="absolute top-6 left-6 z-10 font-label text-xs font-bold text-white/70">{{ pad(i + 1) }}</span>

        <!-- Collapsed: vertical label -->
        <span
          v-show="active !== card.tab"
          class="absolute left-1/2 -translate-x-1/2 bottom-8 z-10 [writing-mode:vertical-rl] rotate-180 font-label text-sm font-bold uppercase tracking-wider whitespace-nowrap text-white/90"
        >
          {{ labelOf(card.tab) }}
        </span>

        <!-- Expanded: title + description reveal -->
        <div class="absolute inset-x-0 bottom-0 z-10 p-8 flex flex-col justify-end">
          <div
            class="transition-all duration-500 ease-out"
            :class="active === card.tab
              ? 'opacity-100 translate-y-0 delay-200'
              : 'opacity-0 translate-y-5 pointer-events-none'"
          >
            <span
              class="inline-flex items-center px-3 py-1 mb-4 rounded-full bg-white/15 backdrop-blur-md border border-white/25 font-label text-[11px] font-bold uppercase tracking-wider text-white"
            >
              {{ labelOf(card.tab) }}
            </span>
            <h4 class="font-h3 text-2xl lg:text-3xl font-bold mb-3 text-white">{{ card.title }}</h4>
            <p class="font-body-md text-white/85 max-w-md leading-relaxed">{{ card.description }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Mobile: stacked cards -->
    <div class="md:hidden w-full space-y-6">
      <div
        v-for="card in teams.cards"
        :key="card.tab"
        class="rounded-[32px] transition-all duration-300"
        :class="card.tab === active ? 'ring-2 ring-black ring-offset-4 ring-offset-page-bg' : ''"
      >
        <TeamsCard :card="card" />
      </div>
    </div>
  </section>
</template>
