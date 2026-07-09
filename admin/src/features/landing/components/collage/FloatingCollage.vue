<script setup lang="ts">
import BaseIcon from '../ui/BaseIcon.vue'
import type { CollageCard } from '../../types'

defineProps<{
  title: string[]
  cards: CollageCard[]
  commentAvatar?: string
}>()

const steps = [
  {
    n: '01',
    title: 'Lecture de la demande',
    text: "Appels d'offres, cahiers des charges, e-mails : SmartESN en extrait le besoin réel.",
  },
  {
    n: '02',
    title: 'Qualification',
    text: "Le contexte métier, les risques et les zones d'ombre sont mis en évidence.",
  },
  {
    n: '03',
    title: 'Chiffrage',
    text: 'Un devis cohérent et fiable, aligné sur vos TJM et vos marges cibles.',
  },
]
</script>

<template>
  <section class="px-margin py-[160px] bg-page-bg">
    <div class="max-w-[1728px] mx-auto">
      <!-- Editorial header: heading left, lead right -->
      <div class="grid grid-cols-1 lg:grid-cols-12 gap-8 lg:gap-16 items-end mb-20 lg:mb-28">
        <div class="lg:col-span-7">
          <span class="font-label text-[12px] font-bold uppercase tracking-[0.2em] text-muted">Avant-vente</span>
          <h2 class="mt-5 font-display font-bold tracking-tight leading-[0.95] text-5xl sm:text-7xl text-text">
            <template v-for="(line, i) in title" :key="i">
              {{ line }}<br v-if="i < title.length - 1" />
            </template>
          </h2>
        </div>
        <p class="lg:col-span-5 font-body-lg text-body-lg text-muted max-w-md lg:pb-3">
          De la demande entrante au devis prêt à envoyer : SmartESN lit, qualifie et chiffre en
          continu.
        </p>
      </div>

      <!-- Editorial index of the process -->
      <div class="border-t border-line">
        <div
          v-for="step in steps"
          :key="step.n"
          class="group grid grid-cols-12 items-baseline gap-4 lg:gap-8 border-b border-line py-10 lg:py-14 transition-colors duration-500"
        >
          <span
            class="col-span-2 lg:col-span-1 font-display text-2xl lg:text-3xl font-bold text-muted-light transition-colors duration-500 group-hover:text-text"
          >
            {{ step.n }}
          </span>
          <h3
            class="col-span-10 lg:col-span-4 font-display text-2xl lg:text-4xl font-bold tracking-tight text-text"
          >
            {{ step.title }}
          </h3>
          <p
            class="col-span-12 lg:col-span-6 font-body-lg text-body-lg text-muted lg:pl-8 lg:border-l lg:border-line"
          >
            {{ step.text }}
          </p>
          <span
            class="hidden lg:flex col-span-1 justify-end text-muted-light transition-all duration-500 group-hover:text-text group-hover:-translate-y-1 group-hover:translate-x-1"
          >
            <BaseIcon name="north_east" class="text-2xl" />
          </span>
        </div>
      </div>

      <!-- Quiet closing line -->
      <p class="mt-14 font-body-md text-[15px] text-muted flex items-center gap-3">
        <span class="w-8 h-px bg-text/40"></span>
        Une seule lecture du besoin, fiable et partagée — en continu.
      </p>
    </div>
  </section>
</template>
