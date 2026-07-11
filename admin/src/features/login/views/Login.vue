<script setup lang="ts">
import { computed, reactive } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/authStore'
import AuthField from '../components/AuthField.vue'
import { validateEmail } from '../utils/validators.ts'
import BaseButton from '@/features/landing/components/ui/BaseButton.vue'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()

const form = reactive({ email: '', password: '' })
const errors = reactive<{ email: string | null; password: string | null }>({
  email: null,
  password: null,
})
const touched = reactive({ email: false, password: false })

// à la connexion, on ne vérifie pas la robustesse (compte déjà créé) : juste « requis »
function passwordRequired(v: string): string | null {
  return v ? null : 'Le mot de passe est requis.'
}

function check(field: 'email' | 'password') {
  if (field === 'email') errors.email = validateEmail(form.email)
  else errors.password = passwordRequired(form.password)
}
function onInput(field: 'email' | 'password') {
  if (touched[field]) check(field)
}
function onBlur(field: 'email' | 'password') {
  touched[field] = true
  check(field)
}

const formValide = computed(() => !validateEmail(form.email) && !!form.password)

async function onSubmit() {
  touched.email = true
  touched.password = true
  check('email')
  check('password')
  if (!formValide.value) return
  const ok = await auth.login({ email: form.email, password: form.password })
  if (ok) router.push((route.query.redirect as string) || '/dashboard')
}
</script>

<template>
  <main class="min-h-screen flex items-center justify-center bg-page-bg px-margin py-16">
    <div class="w-full max-w-md">
      <h1 class="font-display text-4xl font-bold tracking-tight text-text mb-2">Connexion</h1>
      <p class="font-body-md text-muted mb-8">Accédez à votre espace.</p>

      <div
        v-if="auth.error"
        class="mb-6 rounded-2xl border border-brand-from/30 bg-brand-from/5 px-4 py-3 font-body-md text-[14px] text-brand-from"
      >
        {{ auth.error }}
      </div>

      <form class="flex flex-col gap-5" novalidate @submit.prevent="onSubmit">
        <AuthField
          v-model="form.email"
          label="Email"
          type="email"
          autocomplete="email"
          placeholder="votre@entreprise.com"
          :error="errors.email"
          @update:modelValue="onInput('email')"
          @blur="onBlur('email')"
        />
        <AuthField
          v-model="form.password"
          label="Mot de passe"
          type="password"
          autocomplete="current-password"
          placeholder="••••••••"
          :error="errors.password"
          @update:modelValue="onInput('password')"
          @blur="onBlur('password')"
        />

        <BaseButton type="submit" variant="dark" class="w-full mt-2" :disabled="auth.loading || !formValide">
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
