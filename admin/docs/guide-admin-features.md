# Guide d'intégration — Fonctionnalités de l'app admin (Vue 3 + Pinia)

> Blueprint d'implémentation des **fonctionnalités internes** (hors login, déjà fait).
> Il relie chaque endpoint du contrat d'API à l'architecture de l'app : conventions **par
> feature** (`types.ts` / `services` / `stores` / `composables` / `views`), wrapper `http.ts`
> existant, `authStore` (token + rôle).
>
> Le login (`POST /auth/login`) est déjà en place ; on réutilise son `token` et son `role`.

**Base URL** : `http://localhost:8080` (déjà dans ton `.env` : `VITE_API_URL=http://localhost:8080`).
Toutes les routes ci-dessous sont **authentifiées** (`Authorization: Bearer <token>`), sauf le login.

---

## 0. Ce qui existe déjà et qu'on réutilise

- **`src/utils/http.ts`** : ajoute déjà `Authorization: Bearer <token>` (lu dans `localStorage`).
  → **Une seule chose à ajouter** : la gestion du `401` (voir §2).
- **`authStore`** : expose `token`, `user = { email, role }`, `isAuthenticated`.
  → sert aux **guards de rôle** (§3) et à masquer les écrans ADMIN.

Arborescence cible (nouvelles features) :

```
src/features/
├── demandes/         // tableau de bord, détail, qualif, statut, réaffectation
│   ├── types.ts
│   ├── services/demandeService.ts
│   ├── services/devisService.ts
│   ├── stores/demandesStore.ts
│   └── views/{DemandesList,DemandeDetail}.vue
├── equipes/services/equipeService.ts
├── reporting/{services/statsService.ts, views/Reporting.vue}
└── admin/            // ADMIN uniquement
    ├── services/adminService.ts
    └── views/{Utilisateurs,Grille}.vue
```

---

## 1. Les types du domaine — `src/features/demandes/types.ts`

Les énumérations en `union types` (utilisables pour les `<select>` et les badges) :

```ts
export type TypeProjet = 'MOBILE' | 'WEB' | 'DESKTOP' | 'CONSEIL'
export type Complexite = 'SIMPLE' | 'MOYENNE' | 'COMPLEXE'
export type StatutDemande =
  | 'NOUVELLE' | 'EN_ANALYSE' | 'QUALIFIEE' | 'DEVIS_ENVOYE'
  | 'EN_NEGOCIATION' | 'GAGNEE' | 'PERDUE'
export type Role = 'COMMERCIAL' | 'DIRECTEUR_TECHNIQUE' | 'ADMIN'

// Pratique pour remplir des <select> :
export const TYPES: TypeProjet[] = ['MOBILE', 'WEB', 'DESKTOP', 'CONSEIL']
export const COMPLEXITES: Complexite[] = ['SIMPLE', 'MOYENNE', 'COMPLEXE']
export const STATUTS: StatutDemande[] = [
  'NOUVELLE', 'EN_ANALYSE', 'QUALIFIEE', 'DEVIS_ENVOYE',
  'EN_NEGOCIATION', 'GAGNEE', 'PERDUE',
]

// Ligne de liste (GET /demandes)
export interface DemandeListItem {
  id: number
  description: string
  clientNom: string
  clientEmail: string
  statut: StatutDemande
  type: TypeProjet | null
  complexite: Complexite | null
  scoreConfiance: number | null
  dateCreation: string // ISO
}

// Détail (GET /demandes/{id}) : champs d'estimation en plus, remplis après qualif
export interface DemandeDetail extends DemandeListItem {
  budgetMin?: number
  budgetMax?: number
  delaiSemaines?: number
  equipeAffectee?: string
}

export interface Devis {
  id: number
  demandeId: number
  contenu: string
  montant: number
  valide: boolean
  dateCreation: string
  dateEnvoi: string | null
}

export interface Equipe {
  id: number
  nom: string
  specialite: string
}

export interface Statistiques {
  totalDemandes: number
  nbNouvelles: number
  nbQualifiees: number
  nbDevisEnvoyes: number
  nbGagnees: number
  nbPerdues: number
  tauxConversion: number
  caPotentiel: number
  caSigne: number
}

export interface Utilisateur {
  id: number
  nom: string
  email: string
  role: Role
}
export interface CreateUtilisateur {
  nom: string
  email: string
  motDePasse: string
  role: Role
}

export interface GrilleLigne {
  id: number
  type: TypeProjet
  complexite: Complexite
  budgetMin: number
  budgetMax: number
  delaiMin: number
  delaiMax: number
}

// Erreur 400 : { champ: message }
export type FieldErrors = Record<string, string>
```

---

## 2. `http.ts` — ajouter la gestion du `401`

Ton wrapper ajoute déjà le token. Ajoute juste : sur **401**, on purge le token et on renvoie
vers `/login`. Comme `http.ts` ne connaît pas le router, on utilise `window.location` (simple et
sûr), ou un event global.

```ts
if (!res.ok) {
  if (res.status === 401) {
    localStorage.removeItem('auth_token')
    // évite les boucles si on est déjà sur /login
    if (!location.pathname.startsWith('/login')) {
      location.assign('/login?redirect=' + encodeURIComponent(location.pathname))
    }
  }
  // ... (le reste de ta gestion d'erreur inchangé : throw HttpError)
}
```

> Le corps d'un `400` (validation) arrive dans `err.data` → tu peux le caster en `FieldErrors`.

---

## 3. Guard de rôle (écrans ADMIN)

Tu as déjà `meta.requiresAuth`. Ajoute `meta.roles` et une vérif dans `beforeEach`
(`src/router/index.ts`) :

```ts
router.beforeEach((to) => {
  const auth = useAuthStore()

  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if (to.meta.guestOnly && auth.isAuthenticated) {
    return { name: 'dashboard' }
  }
  // Rôle insuffisant → on renvoie au dashboard (ou une page 403)
  const roles = to.meta.roles as string[] | undefined
  if (roles && !roles.includes(auth.user?.role ?? '')) {
    return { name: 'dashboard' }
  }
})
```

Et côté UI : masquer le menu « Administration » si `auth.user?.role !== 'ADMIN'`
(le guard + le `403` de l'API restent le filet de sécurité).

---

## 4. Services (la seule couche réseau)

### 4.1 `demandeService.ts`

```ts
import { http } from '@/utils/http'
import type {
  Complexite, DemandeDetail, DemandeListItem, StatutDemande, TypeProjet,
} from '../types'

export function listDemandes(statut?: StatutDemande): Promise<DemandeListItem[]> {
  const q = statut ? `?statut=${statut}` : ''
  return http<DemandeListItem[]>(`/demandes${q}`)
}

export function getDemande(id: number): Promise<DemandeDetail> {
  return http<DemandeDetail>(`/demandes/${id}`)
}

// Qualification manuelle → déclenche estimation + affectation + statut QUALIFIEE
export function qualifier(id: number, body: { type: TypeProjet; complexite: Complexite }) {
  return http<DemandeDetail>(`/demandes/${id}/qualifier`, {
    method: 'PUT', body: JSON.stringify(body),
  })
}

// Qualification par l'IA (pas de corps ; 502 si IA indisponible)
export function analyser(id: number): Promise<DemandeDetail> {
  return http<DemandeDetail>(`/demandes/${id}/analyser`, { method: 'POST' })
}

// Changement de statut commercial (budgetSigne/delaiReel requis pour GAGNEE)
export function changerStatut(
  id: number,
  body: { statut: StatutDemande; budgetSigne?: number; delaiReel?: number },
) {
  return http<DemandeDetail>(`/demandes/${id}/statut`, {
    method: 'PUT', body: JSON.stringify(body),
  })
}

export function reaffecter(id: number, equipeId: number) {
  return http<DemandeDetail>(`/demandes/${id}/reaffecter`, {
    method: 'POST', body: JSON.stringify({ equipeId }),
  })
}
```

### 4.2 `devisService.ts`

```ts
import { http } from '@/utils/http'
import type { Devis } from '../types'

export function genererDevis(demandeId: number): Promise<Devis> {
  return http<Devis>(`/demandes/${demandeId}/devis`, { method: 'POST' })
}
export function getDevis(demandeId: number): Promise<Devis> {
  return http<Devis>(`/demandes/${demandeId}/devis`) // 404 si aucun
}
export function envoyerDevis(devisId: number): Promise<Devis> {
  return http<Devis>(`/devis/${devisId}/envoyer`, { method: 'POST' })
}
```

### 4.3 `equipeService.ts` / `statsService.ts`

```ts
// equipes/services/equipeService.ts
import { http } from '@/utils/http'
import type { Equipe } from '@/features/demandes/types'
export const listEquipes = () => http<Equipe[]>('/equipes')

// reporting/services/statsService.ts
import { http } from '@/utils/http'
import type { Statistiques } from '@/features/demandes/types'
export const getStatistiques = () => http<Statistiques>('/statistiques')
```

### 4.4 `adminService.ts` (ADMIN)

```ts
import { http } from '@/utils/http'
import type { CreateUtilisateur, GrilleLigne, Utilisateur } from '@/features/demandes/types'

export const listUtilisateurs = () => http<Utilisateur[]>('/admin/utilisateurs')
export const creerUtilisateur = (b: CreateUtilisateur) =>
  http<Utilisateur>('/admin/utilisateurs', { method: 'POST', body: JSON.stringify(b) })
export const supprimerUtilisateur = (id: number) =>
  http<void>(`/admin/utilisateurs/${id}`, { method: 'DELETE' })

export const listGrille = () => http<GrilleLigne[]>('/admin/grille')
export const majGrilleLigne = (
  id: number,
  b: Pick<GrilleLigne, 'budgetMin' | 'budgetMax' | 'delaiMin' | 'delaiMax'>,
) => http<GrilleLigne>(`/admin/grille/${id}`, { method: 'PUT', body: JSON.stringify(b) })
```

---

## 5. Store — `demandesStore.ts` (exemple de référence)

Store Pinia « setup » (comme ton `authStore`) : liste + filtre + détail + actions, avec
`loading`/`error`.

```ts
import { defineStore } from 'pinia'
import { ref } from 'vue'
import * as api from '../services/demandeService'
import type { DemandeDetail, DemandeListItem, StatutDemande } from '../types'

export const useDemandesStore = defineStore('demandes', () => {
  const items = ref<DemandeListItem[]>([])
  const current = ref<DemandeDetail | null>(null)
  const filtreStatut = ref<StatutDemande | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  async function charger() {
    loading.value = true; error.value = null
    try {
      items.value = await api.listDemandes(filtreStatut.value ?? undefined)
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Erreur de chargement.'
    } finally { loading.value = false }
  }

  async function chargerDetail(id: number) {
    loading.value = true; error.value = null
    try { current.value = await api.getDemande(id) }
    catch (e) { error.value = e instanceof Error ? e.message : 'Introuvable.' }
    finally { loading.value = false }
  }

  // Après une action serveur, on met à jour `current` avec la réponse renvoyée.
  async function qualifierIA(id: number) {
    current.value = await api.analyser(id) // peut throw 502 → à catch dans la vue
  }

  function setFiltre(s: StatutDemande | null) { filtreStatut.value = s; charger() }

  return { items, current, filtreStatut, loading, error, charger, chargerDetail, qualifierIA, setFiltre }
})
```

> Même moule pour un `reportingStore` (stats) et un `adminStore` (users/grille) si tu veux les
> mettre en cache. Sinon, appeler les services directement depuis la vue suffit.

---

## 6. Routing (nouvelles routes)

```ts
// exemple d'ajout dans les routes (toutes internes, admin en plus protégé par meta.roles)
{ path: '/demandes', name: 'demandes', meta: { requiresAuth: true },
  component: () => import('@/features/demandes/views/DemandesList.vue') },
{ path: '/demandes/:id', name: 'demande-detail', meta: { requiresAuth: true },
  component: () => import('@/features/demandes/views/DemandeDetail.vue'), props: true },
{ path: '/reporting', name: 'reporting', meta: { requiresAuth: true },
  component: () => import('@/features/reporting/views/Reporting.vue') },
{ path: '/admin/utilisateurs', name: 'admin-users',
  meta: { requiresAuth: true, roles: ['ADMIN'] },
  component: () => import('@/features/admin/views/Utilisateurs.vue') },
{ path: '/admin/grille', name: 'admin-grille',
  meta: { requiresAuth: true, roles: ['ADMIN'] },
  component: () => import('@/features/admin/views/Grille.vue') },
```

---

## 7. Écrans — quoi afficher

| Écran | Données / actions | Endpoints |
|-------|-------------------|-----------|
| **Tableau de bord** (`DemandesList`) | table + **badges de statut** + filtre par statut + lien détail | `GET /demandes[?statut=]` |
| **Détail** (`DemandeDetail`) | infos client + description + estimation (budget/délai/équipe) + actions | `GET /demandes/{id}` |
| ↳ Qualifier | `<select>` type + complexité **ou** bouton « Qualifier par IA » | `PUT …/qualifier` · `POST …/analyser` |
| ↳ Devis | générer / consulter / **valider & envoyer** (validation humaine) | `POST` & `GET …/devis` · `POST /devis/{id}/envoyer` |
| ↳ Statut | changer le statut ; si `GAGNEE` → saisir `budgetSigne` + `delaiReel` | `PUT …/statut` |
| ↳ Réaffecter | `<select>` équipe | `POST …/reaffecter` · `GET /equipes` |
| **Reporting** | cartes KPI + camembert (répartition statut) + histogramme (CA potentiel vs signé) | `GET /statistiques` |
| **Admin · utilisateurs** (ADMIN) | liste + créer + supprimer | `GET/POST/DELETE /admin/utilisateurs` |
| **Admin · grille** (ADMIN) | 12 lignes éditables (budget/délai) | `GET /admin/grille` · `PUT /admin/grille/{id}` |

> **Badges de statut** : mappe chaque `StatutDemande` à une couleur (réutilise le token
> `brand` + des gris) — ex. `NOUVELLE` neutre, `GAGNEE` vert, `PERDUE` rouge atténué.

---

## 8. Codes d'erreur — réactions attendues

| Code | Sens | Réaction UI |
|------|------|-------------|
| `400` | données invalides (`err.data` = `{ champ: message }`) | afficher l'erreur **sous le champ** fautif |
| `401` | non authentifié | purge token + redirection `/login` (géré dans `http.ts`, §2) |
| `403` | rôle insuffisant | message « accès réservé » (ne devrait pas arriver si UI masquée) |
| `404` | ressource introuvable | état vide / « demande introuvable » |
| `409` | règle métier (ex. pas de grille pour ce type/complexité) | message clair, ne pas bloquer l'app |
| `502` | service IA indisponible | sur `/analyser` : message + bouton **Réessayer** |

---

## 9. Checklist de conformité (endpoint → où c'est traité)

| Endpoint | Service | Écran / Store |
|----------|---------|---------------|
| `GET /demandes[?statut=]` | `listDemandes` | `DemandesList` / `demandesStore.charger` |
| `GET /demandes/{id}` | `getDemande` | `DemandeDetail` / `chargerDetail` |
| `PUT /demandes/{id}/qualifier` | `qualifier` | Détail → qualif manuelle |
| `POST /demandes/{id}/analyser` | `analyser` | Détail → qualif IA (gérer 502) |
| `POST /demandes/{id}/devis` · `GET …/devis` | `genererDevis` / `getDevis` | Détail → bloc Devis |
| `POST /devis/{id}/envoyer` | `envoyerDevis` | Détail → valider & envoyer |
| `PUT /demandes/{id}/statut` | `changerStatut` | Détail → statut (GAGNEE ⇒ budget/délai) |
| `POST /demandes/{id}/reaffecter` · `GET /equipes` | `reaffecter` / `listEquipes` | Détail → réaffectation |
| `GET /statistiques` | `getStatistiques` | `Reporting` |
| `GET/POST/DELETE /admin/utilisateurs` | `adminService` | `Utilisateurs` (ADMIN) |
| `GET /admin/grille` · `PUT /admin/grille/{id}` | `adminService` | `Grille` (ADMIN) |

---

## 10. Rappels d'intégration

- **Token** : déjà stocké (`auth_token`) et injecté par `http.ts`. Rien à refaire.
- **Rôles** : `auth.user.role` pour masquer/afficher ; `meta.roles` + `403` comme garde-fous.
- **Loaders** : surtout sur `/analyser` (IA, 1–3 s).
- **Après action serveur** : les endpoints d'action renvoient la ressource à jour → mets à jour
  `current`/`items` avec la réponse plutôt que de tout recharger.
- **CORS** : `http://localhost:5173` déjà autorisé côté backend. Autre port ⇒ le signaler.
```
