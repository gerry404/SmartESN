<script setup lang="ts">
import BaseIcon from '../ui/BaseIcon.vue'

defineProps<{ variant: string; active: boolean }>()

const extracted = [
  { icon: 'category', label: 'Périmètre' },
  { icon: 'code', label: 'Technologies' },
  { icon: 'inventory_2', label: 'Livrables' },
  { icon: 'gavel', label: 'Contraintes' },
]

const flags = [
  { icon: 'warning', label: 'Risque planning', tone: 'text-amber-500', bg: 'bg-amber-50', border: 'border-amber-200' },
  { icon: 'help', label: 'Zone d’ombre : SLA', tone: 'text-sky-500', bg: 'bg-sky-50', border: 'border-sky-200' },
  { icon: 'quiz', label: 'Question : périmètre TMA', tone: 'text-brand-to', bg: 'bg-[#FFF1EE]', border: 'border-brand-to/20' },
]

const sources = [
  { icon: 'description', label: 'AO_Refonte_SI_Retail' },
  { icon: 'mail', label: 'Demande App mobile RH' },
  { icon: 'request_quote', label: 'Chiffrage Migration Cloud' },
  { icon: 'insights', label: 'Qualification Data Platform' },
]
</script>

<template>
  <div class="w-full flex items-center justify-center" :class="active ? 'is-in' : ''">
    <!-- 01 · Analyse automatique : document scanné + extraction -->
    <div
      v-if="variant === 'analyse'"
      class="w-full max-w-md bg-white-card rounded-3xl shadow-xl border border-line/20 p-6"
    >
      <div class="relative rounded-2xl border border-line/40 bg-soft-card-2 p-5 overflow-hidden mb-5">
        <div class="scan-line" :class="active ? 'scan-run' : ''"></div>
        <div class="flex items-center gap-3">
          <BaseIcon name="description" class="text-brand-to text-2xl" />
          <div class="flex flex-col">
            <span class="font-label text-[12px] font-bold text-text">AO_Refonte_SI_Retail.pdf</span>
            <span class="font-body-md text-[12px] text-muted">Lecture du besoin…</span>
          </div>
        </div>
      </div>
      <span class="font-label text-[10px] font-bold uppercase tracking-wider text-muted">Extraction</span>
      <div class="grid grid-cols-2 gap-3 mt-3">
        <div
          v-for="(chip, i) in extracted"
          :key="chip.label"
          class="anim flex items-center gap-2.5 rounded-xl bg-soft-card-2 border border-line/40 px-3 py-2.5"
          :style="{ transitionDelay: `${150 + i * 90}ms` }"
        >
          <BaseIcon :name="chip.icon" class="text-text text-base" />
          <span class="font-body-md text-[13px] font-medium text-text">{{ chip.label }}</span>
        </div>
      </div>
    </div>

    <!-- 02 · Qualification contextuelle : score + signaux -->
    <div
      v-else-if="variant === 'qualif'"
      class="w-full max-w-md bg-white-card rounded-3xl shadow-xl border border-line/20 p-7"
    >
      <div class="flex items-center gap-5 mb-6 anim" :style="{ transitionDelay: '120ms' }">
        <div class="relative w-20 h-20 shrink-0">
          <div class="score-ring" :class="active ? 'score-run' : ''"></div>
          <div class="absolute inset-[6px] rounded-full bg-white-card flex items-center justify-center">
            <span class="font-display text-xl font-bold text-text">82</span>
          </div>
        </div>
        <div>
          <p class="font-label text-[11px] font-bold uppercase tracking-wider text-muted">Score de qualification</p>
          <p class="font-h3 text-lg font-bold text-text">Opportunité prioritaire</p>
        </div>
      </div>
      <div class="space-y-2.5">
        <div
          v-for="(f, i) in flags"
          :key="f.label"
          class="anim flex items-center gap-3 rounded-xl border px-3.5 py-2.5"
          :class="[f.bg, f.border]"
          :style="{ transitionDelay: `${220 + i * 100}ms` }"
        >
          <BaseIcon :name="f.icon" :class="f.tone" />
          <span class="font-body-md text-[13px] font-medium text-text">{{ f.label }}</span>
        </div>
      </div>
    </div>

    <!-- 03 · Source de vérité unique : espace partagé -->
    <div
      v-else
      class="w-full max-w-md bg-white-card rounded-3xl shadow-xl border border-line/20 p-6"
    >
      <div class="flex items-center gap-2 rounded-full bg-soft-card-2 border border-line/40 px-4 py-2.5 mb-5">
        <BaseIcon name="search" class="text-muted text-base" />
        <span class="font-body-md text-[13px] text-muted">Rechercher une demande, un chiffrage…</span>
      </div>
      <div class="space-y-2.5 mb-5">
        <div
          v-for="(s, i) in sources"
          :key="s.label"
          class="anim flex items-center gap-3 rounded-xl bg-soft-card-2 border border-line/40 px-3.5 py-2.5"
          :style="{ transitionDelay: `${140 + i * 80}ms` }"
        >
          <BaseIcon :name="s.icon" class="text-text text-base" />
          <span class="font-body-md text-[13px] font-medium text-text flex-1">{{ s.label }}</span>
          <BaseIcon name="check_circle" class="text-emerald-500 text-lg" />
        </div>
      </div>
      <div class="flex items-center justify-between">
        <div class="flex -space-x-2">
          <span
            v-for="n in 4"
            :key="n"
            class="w-7 h-7 rounded-full border-2 border-white-card bg-gradient-to-tr from-brand-from to-brand-to"
          ></span>
        </div>
        <span class="flex items-center gap-1.5 font-label text-[11px] font-bold text-emerald-600">
          <span class="w-1.5 h-1.5 rounded-full bg-emerald-500"></span>
          1 source · temps réel
        </span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.anim {
  opacity: 0;
  transform: translateY(12px);
  transition:
    opacity 0.5s ease-out,
    transform 0.5s ease-out;
}
.is-in .anim {
  opacity: 1;
  transform: translateY(0);
}

.scan-line {
  position: absolute;
  left: 0;
  right: 0;
  top: 0;
  height: 40%;
  background: linear-gradient(to bottom, rgba(166, 216, 75, 0), rgba(166, 216, 75, 0.22), rgba(166, 216, 75, 0));
  transform: translateY(-100%);
}
.scan-run {
  animation: scan 2.4s ease-in-out infinite;
}
@keyframes scan {
  0% { transform: translateY(-100%); }
  100% { transform: translateY(320%); }
}

.score-ring {
  position: absolute;
  inset: 0;
  border-radius: 9999px;
  background: conic-gradient(#3f8f2e 0deg, #a6d84b 295deg, #e0eacd 295deg 360deg);
}
.score-run {
  animation: score 1s ease-out both;
}
@keyframes score {
  from { background: conic-gradient(#3f8f2e 0deg, #a6d84b 0deg, #e0eacd 0deg 360deg); }
}

@media (prefers-reduced-motion: reduce) {
  .anim { opacity: 1; transform: none; transition: none; }
  .scan-run, .score-run { animation: none; }
}
</style>
