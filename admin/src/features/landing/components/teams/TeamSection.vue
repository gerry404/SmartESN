<script setup lang="ts">
import { ref } from 'vue'
import TeamsTabs from './TeamsTabs.vue'
import TeamsCard from './TeamsCard.vue'
import BaseTitle from '../ui/BaseTitle.vue'
import type { HomeContent } from '../../types'

const props = defineProps<{ teams: HomeContent['teams'] }>()

const active = ref(props.teams.tabs[0]?.id ?? '')
</script>

<template>
  <section
    id="a-propos"
    class="py-[160px] px-margin max-w-[1728px] mx-auto flex flex-col items-center bg-page-bg"
  >
    <BaseTitle as="h2" class="mb-12 text-center">{{ teams.title }}</BaseTitle>

    <TeamsTabs :tabs="teams.tabs" :active="active" @update:active="active = $event" />

    <div class="grid grid-cols-1 md:grid-cols-2 gap-8 w-full">
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
