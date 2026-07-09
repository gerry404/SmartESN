<script setup lang="ts">
import { reactive } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/authStore';
import AuthField from '../components/AuthField.vue';
import { validateEmail, validatePassword } from '../utils/validators.ts';
import BaseButton from '@/features/landing/components/ui/BaseButton.vue';
import Logo from '@/features/landing/components/ui/Logo.vue'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()

const form = reactive({ email: '', password: '' })
const errors = reactive<{ email: string | null; password: string | null }>({
  email: null,
  password: null,
})

function validate(): boolean {
  errors.email = validateEmail(form.email)
  errors.password = validatePassword(form.password)
  return !errors.email && !errors.password
}

async function onSubmit() {
  if (!validate()) return
  const ok = await auth.login({ email: form.email, password: form.password })
  if (ok) router.push((route.query.redirect as string) || '/dashboard')
}
</script>

<template>
    <main class="min-h-screen flex items-center justify-center bg-page-bg px-margin py-16">
        <div class="w-full max-w-md">
            <RouterLink to="/" class="inline-block mb-10"><Logo /></RouterLink>

        <h1 class="font-display text-4xl font-bold tracking-tight text-text mb-2">Connexion</h1>
        <p class="font-body-md text-muted mb-8">Accedez a votre espace. </p>

        <div
        v-if="auth.error"
        class="mb-6 rounded-2xl border border-brand-from/30 bg-brand-from/5 px-4 py-3 font-body-md text-[14px] text-brand-from"
        >
        {{ auth.error }}
      </div>

      <form class="flex flex-col gap-5" novalidate @submit.prevent="onSubmit">
        <AuthField v-model="form.email" label="Email" type="email"
          autocomplete="email" placeholder="votre@entreprise.com" :error="errors.email" />
        <AuthField v-model="form.password" label="Mot de passe" type="password"
          autocomplete="current-password" placeholder="••••••••" :error="errors.password" />

        <BaseButton type="submit" variant="dark" class="w-full mt-2" :disabled="auth.loading">
          {{ auth.loading ? 'Connexion…' : 'Se connecter' }}
        </BaseButton>
      </form>


      <p class="mt-8 font-body-md text-[14px] text-muted text-center">
        Pas encore de compte ?
        <RouterLink to="/register" class="font-bold text-text hover:text-brand-from">Créer un compte</RouterLink>
      </p>
        </div>
       </main>
</template>