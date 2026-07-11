<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { getLienFormulaire } from '@/features/admin/services/adminService'

const lien = ref('')
const loading = ref(true)
const error = ref<string | null>(null)
const copied = ref(false)

onMounted(async () => {
  try {
    const r = await getLienFormulaire() // jamais écrit en dur : vient du backend
    lien.value = r.lien
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Impossible de récupérer le lien.'
  } finally {
    loading.value = false
  }
})

function copier() {
  navigator.clipboard?.writeText(lien.value).then(() => {
    copied.value = true
    setTimeout(() => (copied.value = false), 2000)
  })
}

const mailto = computed(
  () =>
    `mailto:?subject=${encodeURIComponent('Décrivez votre projet — SmartESN')}` +
    `&body=${encodeURIComponent(
      'Bonjour,\n\nPour étudier votre besoin, merci de le décrire via notre formulaire :\n' +
        lien.value +
        '\n\nNotre assistant vous posera quelques questions pour bien cadrer votre projet.\n\nBien à vous.',
    )}`,
)

const whatsapp = computed(
  () =>
    `https://wa.me/?text=${encodeURIComponent(
      'Décrivez votre projet via notre formulaire : ' + lien.value,
    )}`,
)
</script>

<template>
  <main class="min-h-screen bg-page-bg px-margin py-12">
    <div class="max-w-[720px] mx-auto">
      <h1 class="font-display text-3xl font-bold tracking-tight text-text mb-2">
        Partager votre formulaire
      </h1>
      <p class="font-body-md text-muted mb-8">
        Envoyez ce lien à vos prospects. Ils décrivent leur projet, l'assistant les guide, et la
        demande arrive automatiquement dans votre espace.
      </p>

      <div v-if="error" class="mb-6 rounded-2xl border border-red-200 bg-red-50 px-4 py-3 font-body-md text-[14px] text-red-600">
        {{ error }}
      </div>

      <p v-if="loading" class="font-body-md text-muted py-6">Chargement du lien…</p>

      <div v-else class="rounded-2xl border border-line bg-white-card p-6">
        <span class="font-label text-[12px] font-bold uppercase tracking-wider text-muted">Votre lien</span>
        <div class="mt-2 flex flex-col sm:flex-row gap-3">
          <input
            :value="lien"
            readonly
            class="flex-1 rounded-xl border border-line bg-page-bg px-4 py-3 font-body-md text-text outline-none"
            @focus="($event.target as HTMLInputElement).select()"
          />
          <button
            class="rounded-xl bg-black text-white px-6 py-3 font-label text-[11px] font-bold uppercase whitespace-nowrap"
            @click="copier"
          >
            {{ copied ? 'Copié ✓' : 'Copier' }}
          </button>
        </div>

        <div class="mt-6 flex flex-wrap gap-3">
          <a
            :href="lien"
            target="_blank"
            rel="noopener"
            class="rounded-full border border-line px-5 py-2.5 font-label text-[11px] font-bold uppercase text-text hover:bg-page-bg"
          >
            Ouvrir le formulaire
          </a>
          <a
            :href="mailto"
            class="rounded-full border border-line px-5 py-2.5 font-label text-[11px] font-bold uppercase text-text hover:bg-page-bg"
          >
            Envoyer par e-mail
          </a>
          <a
            :href="whatsapp"
            target="_blank"
            rel="noopener"
            class="rounded-full border border-line px-5 py-2.5 font-label text-[11px] font-bold uppercase text-text hover:bg-page-bg"
          >
            Partager sur WhatsApp
          </a>
        </div>
      </div>

      <p class="mt-6 font-body-md text-[13px] text-muted">
        Chaque demande soumise via ce lien est automatiquement rattachée à votre entreprise.
      </p>
    </div>
  </main>
</template>
