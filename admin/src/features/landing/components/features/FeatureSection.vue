<script setup lang="ts">
import FeatureList from './FeatureList.vue'
import BaseIcon from '../ui/BaseIcon.vue'
import BaseTitle from '../ui/BaseTitle.vue'
import type { FeatureBlock } from '../../types'

defineProps<{ block: FeatureBlock }>()

// Mock "inbox" of incoming client requests shown in the visual panel.
const inbox = [
  { icon: 'description', color: 'text-brand-to', label: 'AO_Refonte_SI_Retail.pdf' },
  { icon: 'mail', color: 'text-[#8A2387]', label: 'Demande  App mobile RH' },
  { icon: 'request_quote', color: 'text-[#E94057]', label: 'Chiffrage Migration Cloud' },
  { icon: 'insights', color: 'text-[#F27121]', label: 'Qualification  Data Platform' },
]
</script>

<template>
  <section id="produit" class="py-[120px] px-margin max-w-[1728px] mx-auto bg-page-bg">
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-[100px] items-center">
      <!-- Visual panel: incoming requests inbox -->
      <div
        class="bg-gradient-to-br from-soft-card-2 to-soft-card rounded-[40px] p-8 h-[700px] flex items-center justify-center relative overflow-hidden border border-line/30 shadow-sm"
      >
        <div class="w-full max-w-md bg-white-card rounded-2xl shadow-xl border border-line/20 p-6">
          <div class="flex items-center justify-between mb-8">
            <span class="font-label font-bold text-sm text-text">Demandes entrantes</span>
            <BaseIcon name="more_horiz" class="text-muted" />
          </div>
          <div class="space-y-4">
            <div
              v-for="row in inbox"
              :key="row.label"
              class="flex items-center gap-4 p-3 hover:bg-soft-card rounded-xl transition-colors"
            >
              <BaseIcon :name="row.icon" :class="row.color" />
              <span class="font-body-md font-medium text-text">{{ row.label }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Copy -->
      <div class="flex flex-col justify-center h-full max-w-lg">
        <BaseTitle as="h3" class="mb-12">{{ block.title }}</BaseTitle>
        <FeatureList :items="block.items" />
      </div>
    </div>
  </section>
</template>
