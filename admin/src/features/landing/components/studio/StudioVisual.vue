<script setup lang="ts">
import BaseIcon from '../ui/BaseIcon.vue'

defineProps<{ variant: string; prompt?: string }>()

const lots = [
  { label: 'Cadrage & analyse', value: 30 },
  { label: 'Développement', value: 85 },
  { label: 'Recette & mise en prod', value: 55 },
]

const hypotheses = [
  { icon: 'check_circle', label: 'Environnements fournis par le client' },
  { icon: 'schedule', label: 'Recette : 2 itérations incluses' },
  { icon: 'warning', label: 'Risque : dépendance à une API tierce' },
]

const history = [40, 55, 48, 70, 62, 80, 74, 88, 82, 92, 90, 96]
</script>

<template>
  <div class="w-full max-w-sm">
    <!-- Chiffrage assisté -->
    <template v-if="variant === 'chiffrage'">
      <p class="text-[14px] text-text/50 mb-2">Charge estimée</p>
      <p class="font-display text-6xl font-bold leading-none mb-10">
        42 <span class="text-2xl text-text/40">j·h</span>
      </p>
      <div class="space-y-6">
        <div v-for="l in lots" :key="l.label">
          <div class="flex justify-between mb-2 text-[14px]">
            <span>{{ l.label }}</span>
            <span class="text-text/45 tabular-nums">{{ l.value }}%</span>
          </div>
          <div class="h-[3px] rounded-full bg-black/10">
            <div class="h-full rounded-full bg-text bar" :style="{ width: `${l.value}%` }"></div>
          </div>
        </div>
      </div>
    </template>

    <!-- Hypothèses transparentes -->
    <template v-else-if="variant === 'hypotheses'">
      <p class="text-[14px] text-text/50 mb-6">Hypothèses &amp; risques</p>
      <ul class="space-y-5">
        <li v-for="h in hypotheses" :key="h.label" class="flex items-start gap-3.5">
          <BaseIcon :name="h.icon" class="text-text/40 text-xl mt-0.5 shrink-0" />
          <span class="text-[16px] leading-snug">{{ h.label }}</span>
        </li>
      </ul>
      <p class="mt-8 text-[14px] text-text/50">Chaque estimation est justifiée et traçable.</p>
    </template>

    <!-- Historique & apprentissage -->
    <template v-else>
      <div class="flex items-end justify-between mb-6">
        <div>
          <p class="text-[14px] text-text/50">Précision d'estimation</p>
          <p class="font-display text-5xl font-bold leading-none mt-1">96 %</p>
        </div>
        <span class="text-[14px] font-semibold text-text/60">+14 pts</span>
      </div>
      <div class="flex items-end gap-1.5 h-28">
        <span
          v-for="(h, i) in history"
          :key="i"
          class="flex-1 rounded-t bar-v"
          :class="i === history.length - 1 ? 'bg-text' : 'bg-black/10'"
          :style="{ height: `${h}%` }"
        ></span>
      </div>
      <p class="mt-6 text-[14px] text-text/50">Calibré sur vos 12 dernières affaires gagnées.</p>
    </template>
  </div>
</template>

<style scoped>
@keyframes grow-w {
  from {
    width: 0;
  }
}
.bar {
  animation: grow-w 0.7s cubic-bezier(0.22, 1, 0.36, 1) both;
}
@keyframes grow-h {
  from {
    height: 0;
  }
}
.bar-v {
  animation: grow-h 0.6s cubic-bezier(0.22, 1, 0.36, 1) both;
}
@media (prefers-reduced-motion: reduce) {
  .bar,
  .bar-v {
    animation: none;
  }
}
</style>
