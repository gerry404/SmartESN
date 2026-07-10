<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import BaseIcon from '../ui/BaseIcon.vue'

// Conversation scriptée : le formulaire dialogue jusqu'à qualifier le besoin.
type Msg = { role: 'user' | 'bot' | 'done'; text: string }
const script: Msg[] = [
  { role: 'user', text: 'On veut une app mobile pour vendre nos produits.' },
  { role: 'bot', text: 'Souhaitez-vous un paiement en ligne ?' },
  { role: 'user', text: 'Oui — Mobile Money et carte.' },
  { role: 'bot', text: 'Avez-vous besoin d’un espace d’administration ?' },
  { role: 'user', text: 'Oui, pour gérer le catalogue.' },
  { role: 'done', text: 'Besoin qualifié · Mobile · ~42 j·h' },
]

const shown = ref(1)
let timer: number | undefined
let reduce = false

const visible = computed(() => script.slice(0, shown.value))
const progress = computed(() => Math.round((shown.value / script.length) * 100))
const done = computed(() => visible.value.some((m) => m.role === 'done'))

function tick() {
  if (shown.value < script.length) {
    shown.value++
  } else {
    // petite pause puis on rejoue
    window.clearInterval(timer)
    window.setTimeout(() => {
      shown.value = 1
      timer = window.setInterval(tick, 1500)
    }, 2600)
  }
}

onMounted(() => {
  reduce = window.matchMedia('(prefers-reduced-motion: reduce)').matches
  if (reduce) {
    shown.value = script.length
    return
  }
  timer = window.setInterval(tick, 1500)
})
onBeforeUnmount(() => window.clearInterval(timer))

const atouts = [
  { icon: 'forum', titre: 'Dialogue intelligent', desc: 'Le formulaire pose des questions ciblées tant que le besoin reste flou.' },
  { icon: 'auto_awesome', titre: 'Qualifié à la source', desc: 'Le besoin arrive dans votre pipe déjà structuré et prêt à chiffrer.' },
  { icon: 'code_blocks', titre: 'À intégrer partout', desc: 'Un composant à poser sur votre site, votre portail ou votre extranet.' },
]
</script>

<template>
  <section id="formulaire" class="py-[120px] px-margin bg-page-bg">
    <div class="max-w-[1728px] mx-auto grid grid-cols-1 lg:grid-cols-2 gap-[64px] xl:gap-[100px] items-center">
      <!-- Copy -->
      <div class="max-w-xl">
        
        <h2 class="mt-5 font-display text-4xl sm:text-5xl font-bold tracking-tight text-text leading-[1.05]">
          Un formulaire vivant qui qualifie vos prospects.
        </h2>
        <p class="mt-5 font-body-lg text-body-lg text-muted">
          Au lieu d'un formulaire figé, SmartESN engage la conversation : il comprend la demande,
          pose les bonnes questions, puis transmet un besoin déjà qualifié à votre avant-vente.
        </p>

        <ul class="mt-10 space-y-6">
          <li v-for="a in atouts" :key="a.titre" class="flex gap-4">
            <span class="shrink-0 w-11 h-11 rounded-full bg-soft-card border border-line flex items-center justify-center">
              <BaseIcon :name="a.icon" class="text-text text-lg" />
            </span>
            <div>
              <h3 class="font-h3 text-lg font-bold text-text">{{ a.titre }}</h3>
              <p class="font-body-md text-muted mt-0.5">{{ a.desc }}</p>
            </div>
          </li>
        </ul>
      </div>

      <!-- Mock animé du dialogue -->
      <div class="relative">
        <div class="rounded-[32px] bg-white-card border border-line shadow-xl p-6 sm:p-7">
          <!-- header -->
          <div class="flex items-center justify-between mb-5">
            <div class="flex items-center gap-2.5">
              <span class="w-8 h-8 rounded-full bg-gradient-to-tr from-brand-from to-brand-to flex items-center justify-center">
                <BaseIcon name="auto_awesome" class="text-white text-sm" />
              </span>
              <span class="font-label text-[12px] font-bold text-text">Assistant SmartESN</span>
            </div>
            <span class="flex items-center gap-1.5">
              <span class="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse"></span>
              <span class="font-label text-[10px] font-bold uppercase tracking-wider text-muted">En ligne</span>
            </span>
          </div>

          <!-- progress -->
          <div class="h-1.5 rounded-full bg-soft-card overflow-hidden mb-5">
            <div class="h-full rounded-full bg-gradient-to-r from-brand-from to-brand-to transition-[width] duration-500 ease-out" :style="{ width: progress + '%' }"></div>
          </div>

          <!-- messages -->
          <div class="flex flex-col gap-3 min-h-[280px]">
            <template v-for="(m, i) in visible" :key="i">
              <div v-if="m.role === 'done'" class="msg self-stretch rounded-2xl bg-gradient-to-r from-brand-from/10 to-brand-to/10 border border-brand-to/25 px-4 py-3 flex items-center gap-2.5">
                <BaseIcon name="verified" class="text-brand-to" />
                <span class="font-label text-[13px] font-bold text-text">{{ m.text }}</span>
              </div>
              <div v-else class="msg max-w-[80%] rounded-2xl px-4 py-2.5 font-body-md text-[14px]"
                :class="m.role === 'user'
                  ? 'self-end bg-text text-white rounded-br-md'
                  : 'self-start bg-soft-card-2 text-text border border-line rounded-bl-md'">
                {{ m.text }}
              </div>
            </template>

            <!-- typing indicator tant que ce n'est pas fini -->
            <div v-if="!done" class="self-start flex items-center gap-1 px-3 py-2">
              <span class="dot"></span><span class="dot"></span><span class="dot"></span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.msg {
  animation: pop 0.35s ease-out both;
}
@keyframes pop {
  from {
    opacity: 0;
    transform: translateY(8px) scale(0.98);
  }
}

.dot {
  width: 6px;
  height: 6px;
  border-radius: 999px;
  background: #a6b49a;
  animation: blink 1.2s infinite ease-in-out;
}
.dot:nth-child(2) {
  animation-delay: 0.2s;
}
.dot:nth-child(3) {
  animation-delay: 0.4s;
}
@keyframes blink {
  0%,
  100% {
    opacity: 0.3;
  }
  50% {
    opacity: 1;
  }
}

@media (prefers-reduced-motion: reduce) {
  .msg,
  .dot {
    animation: none;
  }
}
</style>
