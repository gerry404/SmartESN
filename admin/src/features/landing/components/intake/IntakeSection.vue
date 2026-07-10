<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { mdiArrowRight } from '@mdi/js'
import MdiIcon from '../ui/MdiIcon.vue'

// Conversation scriptée : le formulaire dialogue jusqu'à qualifier le besoin.
type Msg = { role: 'user' | 'bot' | 'done'; text: string }
const script: Msg[] = [
  { role: 'user', text: 'On veut une app mobile pour vendre nos produits.' },
  { role: 'bot', text: 'Souhaitez-vous un paiement en ligne ?' },
  { role: 'user', text: 'Oui, Mobile Money et carte.' },
  { role: 'bot', text: 'Un espace d’administration pour gérer le catalogue ?' },
  { role: 'user', text: 'Oui.' },
  { role: 'done', text: 'Besoin qualifié · Mobile · ~42 j·h' },
]

const shown = ref(1)
let timer: number | undefined

const visible = computed(() => script.slice(0, shown.value))
const progress = computed(() => Math.round((shown.value / script.length) * 100))
const done = computed(() => visible.value.some((m) => m.role === 'done'))

function tick() {
  if (shown.value < script.length) {
    shown.value++
  } else {
    window.clearInterval(timer)
    window.setTimeout(() => {
      shown.value = 1
      timer = window.setInterval(tick, 1600)
    }, 2800)
  }
}

onMounted(() => {
  if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
    shown.value = script.length
    return
  }
  timer = window.setInterval(tick, 1600)
})
onBeforeUnmount(() => window.clearInterval(timer))
</script>

<template>
  <section id="formulaire" class="py-[160px] px-margin bg-page-bg">
    <div class="max-w-[1728px] mx-auto grid grid-cols-1 lg:grid-cols-2 gap-16 lg:gap-24 items-center">
      <!-- Copy -->
      <div class="max-w-lg">
        <p class="text-[15px] text-muted mb-6">À intégrer sur votre site</p>
        <h2 class="font-display text-4xl sm:text-6xl font-bold tracking-tight leading-[1.02]">
          Un formulaire qui dialogue.
        </h2>
        <p class="mt-6 text-body-lg text-muted">
          Il comprend la demande, pose les bonnes questions, et transmet un besoin déjà qualifié à
          votre avant-vente.
        </p>
        <RouterLink
          to="/integration"
          class="inline-flex items-center gap-2 mt-10 text-[15px] font-semibold border-b border-text/30 pb-1 hover:border-text transition-colors"
        >
          Voir la documentation d'intégration
          <MdiIcon :path="mdiArrowRight" />
        </RouterLink>
      </div>

      <!-- Conversation (épurée, sans carte) -->
      <div class="lg:pl-8">
        <div class="h-px w-full bg-line mb-8 relative overflow-hidden">
          <div class="absolute inset-y-0 left-0 bg-text transition-[width] duration-700 ease-out" :style="{ width: progress + '%' }"></div>
        </div>

        <div class="flex flex-col gap-4 min-h-[320px]">
          <template v-for="(m, i) in visible" :key="i">
            <p
              v-if="m.role === 'done'"
              class="self-start inline-flex items-center gap-2 text-[15px] font-semibold text-text mt-2"
            >
              <span class="w-1.5 h-1.5 rounded-full bg-text"></span>
              {{ m.text }}
            </p>
            <p
              v-else
              class="bubble max-w-[85%] text-[15px] leading-relaxed px-4 py-2.5 rounded-2xl"
              :class="m.role === 'user'
                ? 'self-end bg-text text-page-bg rounded-br-sm'
                : 'self-start bg-soft-card text-text rounded-bl-sm'"
            >
              {{ m.text }}
            </p>
          </template>

          <span v-if="!done" class="self-start flex items-center gap-1 px-3 py-2">
            <span class="dot"></span><span class="dot"></span><span class="dot"></span>
          </span>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.bubble {
  animation: rise 0.4s ease-out both;
}
@keyframes rise {
  from {
    opacity: 0;
    transform: translateY(6px);
  }
}
.dot {
  width: 6px;
  height: 6px;
  border-radius: 999px;
  background: #a6b49a;
  animation: blink 1.2s infinite ease-in-out;
}
.dot:nth-child(2) { animation-delay: 0.2s; }
.dot:nth-child(3) { animation-delay: 0.4s; }
@keyframes blink {
  0%, 100% { opacity: 0.3; }
  50% { opacity: 1; }
}
@media (prefers-reduced-motion: reduce) {
  .bubble, .dot { animation: none; }
}
</style>
