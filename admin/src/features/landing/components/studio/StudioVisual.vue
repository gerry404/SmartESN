<script setup lang="ts">
import BaseIcon from '../ui/BaseIcon.vue'

defineProps<{ variant: string; prompt?: string }>()

const lots = [
  { label: 'Cadrage & analyse', value: 30 },
  { label: 'Développement', value: 85 },
  { label: 'Recette & mise en prod', value: 55 },
]

const grid = [
  { role: 'Tech Lead', tjm: '650 €' },
  { role: 'Dev Sénior', tjm: '520 €' },
  { role: 'Dev', tjm: '420 €' },
]

const hypotheses = [
  { icon: 'check_circle', tone: 'text-emerald-500', label: 'Environnements fournis par le client' },
  { icon: 'schedule', tone: 'text-sky-500', label: 'Recette : 2 itérations incluses' },
  { icon: 'warning', tone: 'text-amber-500', label: 'Risque : dépendance API tierce' },
]

const history = [40, 55, 48, 70, 62, 80, 74, 88, 82, 92, 90, 96]
</script>

<template>
  <div class="w-full max-w-lg bg-white/80 backdrop-blur-xl rounded-3xl shadow-2xl border border-white/50 p-8">
    <!-- Brand header (constant) -->
    <div class="flex items-center gap-3 mb-6">
      <div class="w-[24px] h-[24px] rounded-full border border-black flex items-center justify-center shrink-0">
        <span class="text-black font-bold text-[10px] leading-none">S</span>
      </div>
      <span class="font-label text-xs font-bold text-muted">SMARTESN STUDIO</span>
    </div>

    <!-- 01 · Chiffrage assisté -->
    <template v-if="variant === 'chiffrage'">
      <div v-if="prompt" class="bg-white/60 backdrop-blur-md rounded-xl p-4 mb-6 border border-white/50 shadow-sm anim" style="animation-delay: 40ms">
        <p class="font-body-md text-text">{{ prompt }}</p>
      </div>
      <div class="flex items-end justify-between mb-5 anim" style="animation-delay: 140ms">
        <div>
          <p class="font-label text-[11px] font-bold uppercase tracking-wider text-muted">Charge estimée</p>
          <p class="font-display text-4xl font-bold text-text leading-none">42 <span class="text-xl">j·h</span></p>
        </div>
        <div class="flex -space-x-2">
          <span v-for="n in 3" :key="n" class="w-8 h-8 rounded-full border-2 border-white bg-gradient-to-tr from-brand-from to-brand-to"></span>
        </div>
      </div>
      <div class="space-y-3">
        <div v-for="(l, i) in lots" :key="l.label" class="anim" :style="{ animationDelay: `${220 + i * 90}ms` }">
          <div class="flex justify-between mb-1.5">
            <span class="font-body-md text-[13px] text-text">{{ l.label }}</span>
            <span class="font-label text-[11px] font-bold text-muted">{{ l.value }}%</span>
          </div>
          <div class="h-2 rounded-full bg-soft-card overflow-hidden">
            <div class="h-full rounded-full bg-gradient-to-r from-brand-from to-brand-to bar" :style="{ width: `${l.value}%`, animationDelay: `${300 + i * 110}ms` }"></div>
          </div>
        </div>
      </div>
    </template>

    <!-- 02 · Grilles tarifaires respectées -->
    <template v-else-if="variant === 'grille'">
      <div class="space-y-2.5 mb-5">
        <div
          v-for="(g, i) in grid"
          :key="g.role"
          class="anim flex items-center justify-between rounded-xl bg-white/70 border border-white/60 px-4 py-3"
          :style="{ animationDelay: `${100 + i * 90}ms` }"
        >
          <span class="font-body-md text-[13px] font-medium text-text">{{ g.role }}</span>
          <span class="font-label text-[13px] font-bold text-text tabular-nums">{{ g.tjm }} <span class="text-muted font-normal">/ j</span></span>
        </div>
      </div>
      <div class="anim flex items-center justify-between rounded-2xl bg-gradient-to-r from-brand-from to-brand-to px-4 py-3.5 text-white" style="animation-delay: 420ms">
        <span class="font-label text-[12px] font-bold uppercase tracking-wider">Marge cible 28 %</span>
        <span class="flex items-center gap-1.5 font-label text-[12px] font-bold"><BaseIcon name="verified" class="text-base" /> Respectée</span>
      </div>
    </template>

    <!-- 03 · Hypothèses transparentes -->
    <template v-else-if="variant === 'hypotheses'">
      <span class="font-label text-[10px] font-bold uppercase tracking-wider text-muted">Hypothèses & risques</span>
      <div class="space-y-2.5 mt-3">
        <div
          v-for="(h, i) in hypotheses"
          :key="h.label"
          class="anim flex items-center gap-3 rounded-xl bg-white/70 border border-white/60 px-3.5 py-3"
          :style="{ animationDelay: `${120 + i * 100}ms` }"
        >
          <BaseIcon :name="h.icon" :class="h.tone" />
          <span class="font-body-md text-[13px] font-medium text-text">{{ h.label }}</span>
        </div>
      </div>
      <div class="anim mt-4 flex items-center gap-2 text-muted" style="animation-delay: 440ms">
        <BaseIcon name="visibility" class="text-base" />
        <span class="font-body-md text-[12px]">Chaque estimation est justifiée et traçable.</span>
      </div>
    </template>

    <!-- 04 · Historique & apprentissage -->
    <template v-else>
      <div class="flex items-center justify-between mb-4 anim" style="animation-delay: 100ms">
        <div>
          <p class="font-label text-[11px] font-bold uppercase tracking-wider text-muted">Précision d’estimation</p>
          <p class="font-display text-2xl font-bold text-text leading-none">96 %</p>
        </div>
        <span class="flex items-center gap-1 font-label text-[12px] font-bold text-emerald-600"><BaseIcon name="trending_up" class="text-base" /> +14 pts</span>
      </div>
      <div class="flex items-end gap-1.5 h-28">
        <span
          v-for="(h, i) in history"
          :key="i"
          class="flex-1 rounded-t bar-v"
          :class="i === history.length - 1 ? 'bg-gradient-to-t from-brand-from to-brand-to' : 'bg-soft-card'"
          :style="{ height: `${h}%`, animationDelay: `${180 + i * 45}ms` }"
        ></span>
      </div>
      <p class="anim font-body-md text-[12px] text-muted mt-3" style="animation-delay: 760ms">
        Calibré sur vos 12 dernières affaires gagnées comparables.
      </p>
    </template>
  </div>
</template>

<style scoped>
@keyframes rise {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
}
.anim {
  animation: rise 0.5s ease-out both;
}

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
  .anim,
  .bar,
  .bar-v {
    animation: none;
  }
}
</style>
