<script setup lang="ts">
import { RouterLink } from 'vue-router'
import BaseIcon from '../ui/BaseIcon.vue'
import Logo from '../ui/Logo.vue'
import type { HomeContent } from '../../types'

defineProps<{
  brand: string
  footer: HomeContent['footer']
}>()

const socials = ['share', 'public', 'hub']

// Un lien interne (route SPA) commence par '/', sinon c'est une ancre/externe.
const isInternal = (href: string) => href.startsWith('/')
</script>

<template>
  <footer class="bg-black text-white pt-xl pb-lg px-margin rounded-t-[64px] relative z-20">
    <div class="max-w-[1728px] mx-auto">
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-xl mb-xl">
        <!-- Brand column -->
        <div class="flex flex-col gap-8">
          <Logo variant="light" mark="stack" />
          <p class="font-body-md text-white/50 max-w-xs">{{ footer.tagline }}</p>
          <div class="flex gap-4">
            <a
              v-for="icon in socials"
              :key="icon"
              class="w-10 h-10 rounded-full border border-white/10 flex items-center justify-center hover:bg-white/10 transition-colors"
              href="#"
            >
              <BaseIcon :name="icon" class="text-[20px]" />
            </a>
          </div>
        </div>

        <!-- Navigation columns -->
        <div v-for="col in footer.columns" :key="col.title">
          <h4 class="font-label text-white mb-6 uppercase tracking-widest text-[10px]">{{ col.title }}</h4>
          <ul class="flex flex-col gap-4">
            <li v-for="link in col.links" :key="link.label">
              <RouterLink
                v-if="isInternal(link.href)"
                class="font-body-md text-white/60 hover:text-white transition-colors"
                :to="link.href"
                >{{ link.label }}</RouterLink
              >
              <a
                v-else
                class="font-body-md text-white/60 hover:text-white transition-colors"
                :href="link.href"
                >{{ link.label }}</a
              >
            </li>
          </ul>
        </div>
      </div>

      <div class="pt-md border-t border-white/10 flex flex-col md:flex-row justify-between items-center gap-4">
        <p class="font-label text-[11px] text-white/40">{{ footer.legal }}</p>
        <div class="flex gap-8">
          <a
            v-for="link in footer.legalLinks"
            :key="link.label"
            class="font-label text-[11px] text-white/40 hover:text-white transition-colors"
            :href="link.href"
            >{{ link.label }}</a
          >
        </div>
      </div>
    </div>
  </footer>
</template>
