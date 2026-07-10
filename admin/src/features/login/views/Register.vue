<script setup lang="ts">
import { reactive } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/authStore'
import AuthField from '../components/AuthField.vue'
import { validateEmail, validateName, validatePassword } from '../utils/validators'
import BaseButton from '@/features/landing/components/ui/BaseButton.vue'
import Logo from '@/features/landing/components/ui/Logo.vue'

const auth = useAuthStore()
const router = useRouter()

const form = reactive({ nomEntreprise: '', nomAdmin: '', email: '', motDePasse: '' })
const errors = reactive<Record<keyof typeof form, string | null>>({
  nomEntreprise: null,
  nomAdmin: null,
  email: null,
  motDePasse: null,
})

function validate(): boolean {
  errors.nomEntreprise = validateName(form.nomEntreprise)
  errors.nomAdmin = validateName(form.nomAdmin)
  errors.email = validateEmail(form.email)
  errors.motDePasse = validatePassword(form.motDePasse)
  return !errors.nomEntreprise && !errors.nomAdmin && !errors.email && !errors.motDePasse
}

async function onSubmit() {
  if (!validate()) return
  const ok = await auth.register({ ...form })
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
          placeholder="posioner SARL"
          autocomplete="organization"
          :error="errors.nomEntreprise"
        />
        <AuthField
          v-model="form.nomAdmin"
          label="Votre nom"
          placeholder="giorno giovanna"
          autocomplete="name"
          :error="errors.nomAdmin"
        />
        <AuthField
          v-model="form.email"
          label="Email"
          type="email"
          placeholder="vous@entreprise.com"
          autocomplete="email"
          :error="errors.email"
        />
        <AuthField
          v-model="form.motDePasse"
          label="Mot de passe"
          type="password"
          placeholder="6 caractères minimum"
          autocomplete="new-password"
          :error="errors.motDePasse"
        />

        <BaseButton type="submit" variant="dark" class="w-full mt-2" :disabled="auth.loading">
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
