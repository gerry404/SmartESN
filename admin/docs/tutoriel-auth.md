# Tutoriel — Construire Login & Register (Vue 3 + Pinia + Vue Router)

> Objectif : coder **toi-même** l'authentification (connexion + inscription) en suivant
> les conventions déjà en place dans le projet. Le backend n'a pas encore livré les
> endpoints → on développe en **mode mock** (faux backend), puis on branche le vrai
> en changeant **une seule ligne**.
>
> Tu ne copies pas bêtement : à chaque étape il y a un **Pourquoi** et un **À toi de jouer**.

---

## 0. La vue d'ensemble (à lire avant de coder)

On sépare les responsabilités en couches. C'est LA bonne habitude à prendre :

```
 Vue (Login.vue)        →  affiche le formulaire, gère la saisie
      │  appelle
 Store (authStore)      →  état partagé (user, token, loading, error) + logique
      │  appelle
 Service (authService)  →  le SEUL qui parle au réseau (fetch)
      │  utilise
 http.ts                →  wrapper fetch (URL de base, headers, erreurs)
      │
   Backend / API
```

**Pourquoi séparer ?** Le jour où tu changes d'API, tu ne touches qu'au **service**.
Le jour où tu changes le design, tu ne touches qu'à la **vue**. Chaque couche a un seul job.

Le projet est organisé par **feature**. Tu vas tout mettre dans `src/features/login/` :

```
src/features/login/
├── types.ts
├── services/authService.ts
├── stores/authStore.ts
├── composables/useAuth.ts        (optionnel, confort)
├── utils/validators.ts
├── components/AuthField.vue
├── router/authRoutes.ts
└── views/
    ├── Login.vue
    ├── Register.vue
    └── Dashboard.vue             (page protégée de démo)
```

Plus deux fichiers hors feature :
- `src/utils/http.ts` (client HTTP réutilisable par toute l'app)
- `.env` (l'URL de l'API)

---

## 1. La variable d'environnement (URL de l'API)

Vite n'expose au navigateur **que** les variables préfixées par `VITE_`.

**À toi de jouer** — crée `admin/.env` :

```bash
VITE_API_URL=http://localhost:8000/api
```

> 💡 Crée aussi un `.env.example` (sans secret) versionné, pour tes coéquipiers.
> Le vrai `.env` doit rester dans `.gitignore`.

**Pourquoi ?** On ne code jamais l'URL en dur : elle change entre local, préprod, prod.

---

## 2. Le client HTTP — `src/utils/http.ts`

Un petit wrapper autour de `fetch` pour ne PAS répéter partout : l'URL de base,
le header JSON, le token, et la gestion d'erreur.

```ts
const BASE_URL = (import.meta.env.VITE_API_URL as string | undefined) ?? '/api'

export interface HttpError extends Error {
  status: number
  data?: unknown
}

// Ajoute "Authorization: Bearer <token>" si un token est stocké.
function authHeader(): Record<string, string> {
  const token = localStorage.getItem('auth_token')
  return token ? { Authorization: `Bearer ${token}` } : {}
}

export async function http<T>(path: string, options: RequestInit = {}): Promise<T> {
  const { headers, ...rest } = options

  const res = await fetch(`${BASE_URL}${path}`, {
    ...rest,
    headers: { 'Content-Type': 'application/json', ...authHeader(), ...headers },
  })

  const isJson = res.headers.get('content-type')?.includes('application/json')
  const data = isJson ? await res.json() : await res.text()

  if (!res.ok) {
    const message =
      data && typeof data === 'object' && 'message' in data
        ? String((data as { message: unknown }).message)
        : `Erreur ${res.status}`
    const err = new Error(message) as HttpError
    err.status = res.status
    err.data = data
    throw err
  }

  return data as T
}
```

**Points à comprendre :**
- `http<T>` est **générique** : tu précises le type de retour attendu (`http<AuthResponse>(...)`).
- On lit `Content-Type` car certaines réponses (204, logout) n'ont pas de JSON.
- Si `res.ok` est faux (status 4xx/5xx), on **jette** une erreur → le store la rattrapera.

**À toi de jouer :** écris ce fichier, puis relis-le ligne par ligne jusqu'à pouvoir
l'expliquer à voix haute.

---

## 3. Les types — `src/features/login/types.ts`

On décrit les formes de données **une fois**, réutilisées partout.

```ts
export interface User {
  id: string
  name: string
  email: string
}

export interface Credentials {
  email: string
  password: string
}

export interface RegisterPayload {
  name: string
  email: string
  password: string
}

// 👉 Adapte à ce que renverra VRAIMENT ton backend.
export interface AuthResponse {
  token: string
  user: User
}
```

**À toi de jouer :** quand le backend te donnera la vraie réponse de `/auth/login`,
tu ajusteras `AuthResponse` ici (ex. `access_token` au lieu de `token` ?).

---

## 4. La validation — `src/features/login/utils/validators.ts`

On valide côté client **avant** d'appeler l'API (meilleure UX, moins d'appels inutiles).
Convention simple : chaque fonction renvoie un **message d'erreur** ou `null`.

```ts
export function validateName(value: string): string | null {
  if (!value.trim()) return 'Le nom est requis.'
  if (value.trim().length < 2) return 'Au moins 2 caractères.'
  return null
}

export function validateEmail(value: string): string | null {
  if (!value) return 'L’email est requis.'
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) return 'Email invalide.'
  return null
}

export function validatePassword(value: string): string | null {
  if (!value) return 'Le mot de passe est requis.'
  if (value.length < 6) return 'Au moins 6 caractères.'
  return null
}
```

---

## 5. Le service — `src/features/login/services/authService.ts`

**C'est le seul fichier qui connaît les endpoints.** On code déjà la version réelle,
mais on garde un **mode mock** pour développer sans backend.

```ts
import { http } from '@/utils/http'
import type { AuthResponse, Credentials, RegisterPayload, User } from '../types'

// 👇 LES ENDPOINTS à ajuster quand le backend te les donne.
const ENDPOINTS = {
  login: '/auth/login',
  register: '/auth/register',
  me: '/auth/me',
  logout: '/auth/logout',
}

// 👇 Passe à `false` quand les vrais endpoints sont prêts.
const USE_MOCK = true

// --- Faux backend (temporaire) ---
function fakeDelay<T>(value: T, ms = 700): Promise<T> {
  return new Promise((resolve) => setTimeout(() => resolve(value), ms))
}
function fakeAuth(email: string, name = 'Utilisateur démo'): AuthResponse {
  return { token: 'mock-token-' + Date.now(), user: { id: 'u_1', name, email } }
}

// --- API du service ---
export function login(creds: Credentials): Promise<AuthResponse> {
  if (USE_MOCK) {
    if (creds.password.length < 6) return Promise.reject(new Error('Identifiants invalides.'))
    return fakeDelay(fakeAuth(creds.email))
  }
  return http<AuthResponse>(ENDPOINTS.login, { method: 'POST', body: JSON.stringify(creds) })
}

export function register(payload: RegisterPayload): Promise<AuthResponse> {
  if (USE_MOCK) return fakeDelay(fakeAuth(payload.email, payload.name))
  return http<AuthResponse>(ENDPOINTS.register, { method: 'POST', body: JSON.stringify(payload) })
}

export function fetchMe(): Promise<User> {
  if (USE_MOCK) return fakeDelay({ id: 'u_1', name: 'Utilisateur démo', email: 'demo@smartesn.fr' })
  return http<User>(ENDPOINTS.me)
}

export function logout(): Promise<void> {
  if (USE_MOCK) return fakeDelay(undefined)
  return http<void>(ENDPOINTS.logout, { method: 'POST' })
}
```

> 🎯 **Le fameux "une seule ligne"** : quand tu recevras les endpoints, tu vérifies
> les chemins dans `ENDPOINTS`, tu ajustes `AuthResponse` si besoin, et tu passes
> `USE_MOCK` à `false`. Rien d'autre à changer dans l'app.

---

## 6. Le store Pinia — `src/features/login/stores/authStore.ts`

L'état d'auth partagé par toute l'app (style « setup », comme `useLandingStore`).

```ts
import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import * as authService from '../services/authService'
import type { Credentials, RegisterPayload, User } from '../types'

const TOKEN_KEY = 'auth_token' // même clé que dans utils/http.ts

export const useAuthStore = defineStore('auth', () => {
  const user = ref<User | null>(null)
  const token = ref<string | null>(localStorage.getItem(TOKEN_KEY)) // survit au F5
  const loading = ref(false)
  const error = ref<string | null>(null)

  const isAuthenticated = computed(() => !!token.value)

  function setToken(value: string | null) {
    token.value = value
    if (value) localStorage.setItem(TOKEN_KEY, value)
    else localStorage.removeItem(TOKEN_KEY)
  }

  async function login(creds: Credentials): Promise<boolean> {
    loading.value = true
    error.value = null
    try {
      const res = await authService.login(creds)
      setToken(res.token)
      user.value = res.user
      return true
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Connexion impossible.'
      return false
    } finally {
      loading.value = false
    }
  }

  // register() : à toi de l'écrire, c'est le calque de login() avec authService.register(payload).

  async function fetchMe(): Promise<void> {
    if (!token.value) return
    try {
      user.value = await authService.fetchMe()
    } catch {
      await logout() // token invalide → on nettoie
    }
  }

  async function logout(): Promise<void> {
    try {
      await authService.logout()
    } catch {
      /* on déconnecte côté client même si l'appel échoue */
    }
    setToken(null)
    user.value = null
  }

  return { user, token, loading, error, isAuthenticated, login, register, fetchMe, logout }
})
```

**Points clés :**
- `loading` / `error` sont dans le store → n'importe quelle vue peut les afficher.
- Le token est **persisté** dans `localStorage` : rester connecté après un refresh.
- Les actions renvoient un `boolean` (succès) → la vue sait s'il faut rediriger.

**À toi de jouer :** écris `register()` toi-même (copie la structure de `login()`).
N'oublie pas de le retourner dans l'objet final.

---

## 7. Champ réutilisable — `src/features/login/components/AuthField.vue`

Un input custom qui marche avec `v-model`. **Le concept clé** : `v-model` sur un
composant = prop `modelValue` + événement `update:modelValue`.

```vue
<script setup lang="ts">
defineProps<{
  label: string
  modelValue: string
  type?: string
  placeholder?: string
  autocomplete?: string
  error?: string | null
}>()
defineEmits<{ (e: 'update:modelValue', value: string): void }>()
</script>

<template>
  <label class="block">
    <span class="font-label text-[12px] font-bold text-text">{{ label }}</span>
    <input
      :type="type ?? 'text'"
      :value="modelValue"
      :placeholder="placeholder"
      :autocomplete="autocomplete"
      class="mt-2 w-full rounded-2xl border bg-white-card px-4 py-3 font-body-md text-text outline-none transition-colors focus:border-text"
      :class="error ? 'border-brand-from' : 'border-line'"
      @input="$emit('update:modelValue', ($event.target as HTMLInputElement).value)"
    />
    <span v-if="error" class="mt-1.5 block font-body-md text-[12px] text-brand-from">{{ error }}</span>
  </label>
</template>
```

**Pourquoi un composant ?** Tu le réutilises dans Login ET Register, sans dupliquer le style.

---

## 8. La vue Login — `src/features/login/views/Login.vue`

Elle assemble tout : formulaire, validation, appel du store, redirection.

```vue
<script setup lang="ts">
import { reactive } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/authStore'
import AuthField from '../components/AuthField.vue'
import { validateEmail, validatePassword } from '../utils/validators'
import BaseButton from '@/features/landing/components/ui/BaseButton.vue'
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
      <p class="font-body-md text-muted mb-8">Accédez à votre espace avant-vente.</p>

      <div
        v-if="auth.error"
        class="mb-6 rounded-2xl border border-brand-from/30 bg-brand-from/5 px-4 py-3 font-body-md text-[14px] text-brand-from"
      >
        {{ auth.error }}
      </div>

      <form class="flex flex-col gap-5" novalidate @submit.prevent="onSubmit">
        <AuthField v-model="form.email" label="Email" type="email"
          autocomplete="email" placeholder="vous@entreprise.fr" :error="errors.email" />
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
```

**À comprendre :**
- `@submit.prevent` empêche le rechargement de page par défaut du navigateur.
- `route.query.redirect` : si on a été renvoyé ici depuis une page protégée, on y retourne après login.
- `BaseButton` reçoit `type="submit"` et `:disabled` par **fallthrough** d'attributs
  (Vue les transmet automatiquement au `<button>` interne).

---

## 9. À toi : Register & Dashboard

**Register.vue** = calque de Login, avec un champ **name** en plus, et
`validateName`, `validateEmail`, `validatePassword`. Au succès → `router.push('/dashboard')`.

**Dashboard.vue** = page protégée de démo. Elle :
- appelle `auth.fetchMe()` dans `onMounted()` si `auth.user` est vide (cas du refresh),
- affiche `auth.user?.name` / `auth.user?.email`,
- a un bouton **Se déconnecter** → `await auth.logout()` puis `router.push('/login')`.

> 🧠 Écris-les **sans regarder** Login.vue si tu peux. C'est là que tu apprends.

---

## 10. Les routes + le guard — `src/features/login/router/authRoutes.ts`

```ts
import type { RouteRecordRaw } from 'vue-router'

export const authRoutes: RouteRecordRaw[] = [
  { path: '/login', name: 'login', component: () => import('../views/Login.vue'), meta: { guestOnly: true } },
  { path: '/register', name: 'register', component: () => import('../views/Register.vue'), meta: { guestOnly: true } },
  { path: '/dashboard', name: 'dashboard', component: () => import('../views/Dashboard.vue'), meta: { requiresAuth: true } },
]
```

Puis branche-les dans `src/router/index.ts` et ajoute le **guard global** :

```ts
import { createRouter, createWebHistory } from 'vue-router'
import { authRoutes } from '@/features/login/router/authRoutes'
import { useAuthStore } from '@/features/login/stores/authStore'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', name: 'home', component: () => import('@/features/landing/views/Home.vue') },
    ...authRoutes,
  ],
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if (to.meta.guestOnly && auth.isAuthenticated) {
    return { name: 'dashboard' }
  }
})

export default router
```

**Pourquoi ça marche ?** Pinia est installé **avant** le router dans `main.ts`
(`app.use(createPinia()); app.use(router)`), donc on peut appeler `useAuthStore()`
**à l'intérieur** du guard (pas au niveau module).

---

## 11. Tester, puis brancher le vrai backend

### En mock (maintenant)
```bash
npm run dev
```
1. Va sur `/login`. Mot de passe < 6 → message d'erreur. Sinon → redirigé vers `/dashboard`.
2. Rafraîchis `/dashboard` (F5) → tu restes connecté (token en `localStorage`).
3. `/dashboard` sans être connecté (vide le `localStorage`) → renvoyé vers `/login`.
4. Déconnecte-toi → retour `/login`, et `/dashboard` redevient inaccessible.

### Quand le backend livre les endpoints
1. Vérifie/complète les chemins dans `ENDPOINTS` (service).
2. Compare la vraie réponse de `/auth/login` avec ton type `AuthResponse` (ajuste si besoin :
   `token` vs `access_token`, `user` imbriqué ou non…).
3. Renseigne `VITE_API_URL` dans `.env`.
4. Passe `USE_MOCK = false`.
5. Ouvre l'onglet **Network** du navigateur : vérifie le status, le corps envoyé, le token reçu.

**Pièges classiques du branchement réel :**
- **CORS** : l'API doit autoriser l'origine de ton front (erreur console typique).
- **Forme du token** : `Bearer <token>` attendu par le backend ? (adapte `authHeader`).
- **Champs** : le backend attend `password_confirmation` ? un `username` au lieu de `email` ?
  → adapte `RegisterPayload` et le `body`.
- **Erreurs 422** (validation Laravel/NestJS) : le message est souvent dans `data.errors`,
  pas `data.message` → adapte la lecture d'erreur si besoin.

---

## 12. Pour aller plus loin (exercices)

- 🔒 **Afficher/masquer** le mot de passe (bouton œil dans `AuthField`).
- ⏳ **Refresh token** : intercepter les 401 dans `http.ts` pour re-tenter après refresh.
- ✅ **Validation live** : valider un champ au `blur` plutôt qu'au submit.
- 🧭 **Rôles** : ajouter `meta: { roles: ['admin'] }` et vérifier `auth.user.role` dans le guard.
- 🧪 **Test unitaire** du store avec Vitest (déjà installé) : `login()` met bien `isAuthenticated` à `true`.

---

### Récap mental à retenir
> **Vue** saisit → **Store** orchestre (loading/error/token) → **Service** appelle l'API →
> **http.ts** gère fetch. Les endpoints vivent à UN endroit. Le token persiste dans
> `localStorage`. Le **guard** protège les routes.

Bon code 💪
