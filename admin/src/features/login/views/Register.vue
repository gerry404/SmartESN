<script setup lang="ts">
import { computed, reactive } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/authStore'
import AuthField from '../components/AuthField.vue'
import {
  validateConfirm,
  validateEmail,
  validateName,
  validatePassword,
  passwordStrength,
} from '../utils/validators'
import BaseButton from '@/features/landing/components/ui/BaseButton.vue'
import Logo from '@/features/landing/components/ui/Logo.vue'

const auth = useAuthStore()
const router = useRouter()

const form = reactive({
  nomEntreprise: '',
  nomAdmin: '',
  email: '',
  motDePasse: '',
  confirmation: '',
})
const errors = reactive<Record<keyof typeof form, string | null>>({
  nomEntreprise: null,
  nomAdmin: null,
  email: null,
  motDePasse: null,
  confirmation: null,
})
// champs déjà « touchés » → on ne montre l'erreur qu'après interaction
const touched = reactive<Record<keyof typeof form, boolean>>({
  nomEntreprise: false,
  nomAdmin: false,
  email: false,
  motDePasse: false,
  confirmation: false,
})

function check(field: keyof typeof form) {
  if (field === 'nomEntreprise') errors.nomEntreprise = validateName(form.nomEntreprise)
  if (field === 'nomAdmin') errors.nomAdmin = validateName(form.nomAdmin)
  if (field === 'email') errors.email = validateEmail(form.email)
  if (field === 'motDePasse') {
    errors.motDePasse = validatePassword(form.motDePasse)
    if (touched.confirmation) errors.confirmation = validateConfirm(form.motDePasse, form.confirmation)
  }
  if (field === 'confirmation') errors.confirmation = validateConfirm(form.motDePasse, form.confirmation)
}

// validation en direct dès que le champ a été touché
function onInput(field: keyof typeof form) {
  if (touched[field]) check(field)
}
function onBlur(field: keyof typeof form) {
  touched[field] = true
  check(field)
}

const force = computed(() => passwordStrength(form.motDePasse))

const formValide = computed(
  () =>
    !validateName(form.nomEntreprise) &&
    !validateName(form.nomAdmin) &&
    !validateEmail(form.email) &&
    !validatePassword(form.motDePasse) &&
    !validateConfirm(form.motDePasse, form.confirmation),
)

async function onSubmit() {
  ;(Object.keys(form) as (keyof typeof form)[]).forEach((f) => {
    touched[f] = true
    check(f)
  })
  if (!formValide.value) return

  const ok = await auth.register({
    nomEntreprise: form.nomEntreprise,
    nomAdmin: form.nomAdmin,
    email: form.email,
    motDePasse: form.motDePasse,
  })
  if (ok) router.push('/dashboard')
}
</script>

<template>
  <main class="min-h-screen flex items-center justify-center bg-page-bg px-margin py-16">
    <div class="w-full max-w-md">
      <RouterLink to="/" class="inline-block mb-10"><Logo mark="stack" /></RouterLink>

      <h1 class="font-display text-4xl font-bold tracking-tight text-text mb-2">Créer un compte</h1>
      <p class="font-body-md text-muted mb-8">Créez l'espace de votre entreprise.</p>

      <div
        v-if="auth.error"
        class="mb-6 rounded-2xl border border-brand-from/30 bg-brand-from/5 px-4 py-3 font-body-md text-[14px] text-brand-from"
      >
        {{ auth.error }}
      </div>

      <form class="flex flex-col gap-5" novalidate @submit.prevent="onSubmit">
        <AuthField
          v-model="form.nomEntreprise"
          label="Nom de l'entreprise"
          placeholder="Ex. Posioner SARL"
          autocomplete="organization"
          :error="errors.nomEntreprise"
          @update:modelValue="onInput('nomEntreprise')"
          @blur="onBlur('nomEntreprise')"
        />
        <AuthField
          v-model="form.nomAdmin"
          label="Votre nom"
          placeholder="Ex. Jean Kamga"
          autocomplete="name"
          :error="errors.nomAdmin"
          @update:modelValue="onInput('nomAdmin')"
          @blur="onBlur('nomAdmin')"
        />
        <AuthField
          v-model="form.email"
          label="Email"
          type="email"
          placeholder="vous@entreprise.com"
          autocomplete="email"
          :error="errors.email"
          @update:modelValue="onInput('email')"
          @blur="onBlur('email')"
        />

        <div>
          <AuthField
            v-model="form.motDePasse"
            label="Mot de passe"
            type="password"
            placeholder="8 caractères, majuscule, chiffre"
            autocomplete="new-password"
            :error="errors.motDePasse"
            @update:modelValue="onInput('motDePasse')"
            @blur="onBlur('motDePasse')"
          />
          <!-- indicateur de force -->
          <div v-if="form.motDePasse" class="mt-2 flex items-center gap-2">
            <div class="flex-1 flex gap-1">
              <span
                v-for="i in 4"
                :key="i"
                class="h-1.5 flex-1 rounded-full transition-colors"
                :style="{ backgroundColor: i <= force.score ? force.color : '#e2e8f0' }"
              />
            </div>
            <span class="text-[12px] font-bold" :style="{ color: force.color }">{{ force.label }}</span>
          </div>
        </div>

        <AuthField
          v-model="form.confirmation"
          label="Confirmer le mot de passe"
          type="password"
          placeholder="Retapez le mot de passe"
          autocomplete="new-password"
          :error="errors.confirmation"
          @update:modelValue="onInput('confirmation')"
          @blur="onBlur('confirmation')"
        />

        <BaseButton
          type="submit"
          variant="dark"
          class="w-full mt-2"
          :disabled="auth.loading || !formValide"
        >
          {{ auth.loading ? 'Création…' : 'Créer mon compte' }}
        </BaseButton>
      </form>

      <p class="mt-8 font-body-md text-[14px] text-muted text-center">
        Déjà un compte ?
        <RouterLink to="/login" class="font-bold text-text hover:text-brand-from">Se connecter</RouterLink>
      </p>
    </div>
  </main>
</template>
