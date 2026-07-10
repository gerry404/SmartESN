# Implémentation — Formulaire client dynamique (« formulaire vivant »)

> Blueprint d'implémentation pour l'app **client** (Angular 21, standalone + signals + SSR).
> Il traduit la spec fonctionnelle en une architecture Angular concrète et **conforme au
> contrat d'API** (`POST /intake`, `POST /demandes`). Chaque exigence de la spec est reliée
> à l'endroit qui la traite (voir §9, checklist de conformité).

Stack détectée : `@angular/* ^21`, **standalone** (`app.config.ts`, `app.routes.ts`), **SSR**
activé (`server.ts`, `provideClientHydration`), **RxJS 7.8**, CSS natif (pas de Tailwind).

---

## 0. Arborescence cible

```
src/app/
├── core/
│   ├── api.config.ts          // API_BASE_URL (InjectionToken)
│   └── demande.models.ts      // types du contrat d'API
├── data/
│   └── demande.api.ts         // service HttpClient (intake + demandes)
└── features/demande/
    ├── demande-form.ts        // composant "formulaire vivant" (machine à états)
    ├── demande-form.html
    └── demande-form.css
```

---

## 1. Prérequis — fournir HttpClient (obligatoire)

`HttpClient` n'est pas encore fourni. Dans **`src/app/app.config.ts`**, ajoute
`provideHttpClient(withFetch())` (`withFetch` = compatible SSR) :

```ts
import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withFetch } from '@angular/common/http';
import { provideClientHydration, withEventReplay } from '@angular/platform-browser';
import { routes } from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideClientHydration(withEventReplay()),
    provideHttpClient(withFetch()),   // 👈 ajouté
  ],
};
```

> **SSR** : les appels `/intake` et `/demandes` se déclenchent sur **action utilisateur**
> (clics), donc côté navigateur. Le rendu serveur initial ne fait aucun appel API → rien à
> garder. N'utilise pas `window`/`localStorage` dans un constructeur ; si besoin,
> `afterNextRender()` ou `isPlatformBrowser(inject(PLATFORM_ID))`.

---

## 2. Config — base URL

**`src/app/core/api.config.ts`** — on isole l'URL de base (injectable, testable) :

```ts
import { InjectionToken } from '@angular/core';

export const API_BASE_URL = new InjectionToken<string>('API_BASE_URL', {
  providedIn: 'root',
  factory: () => 'http://localhost:8080',
});
```

> Plus tard tu remplaceras la `factory` par une variable d'environnement (fichiers
> `src/environments`) ; l'InjectionToken évite d'écrire l'URL en dur dans le service.

---

## 3. Modèles — le contrat d'API, typé

**`src/app/core/demande.models.ts`** (colle **exactement** aux clés renvoyées par le backend) :

```ts
// ----- POST /intake -----
export interface IntakeRequest {
  description: string;
}

export type TypeProbable = 'MOBILE' | 'WEB' | 'DESKTOP' | 'CONSEIL' | null;

export interface IntakeResponse {
  complet: boolean;          // true => passer aux coordonnées
  score_confiance: number;   // 0..1  (barre de progression)
  type_probable: TypeProbable;
  questions: string[];       // 0..5 questions à afficher
}

// ----- POST /demandes -----
export interface DemandeRequest {
  description: string;       // obligatoire
  nom: string;               // obligatoire
  email: string;             // obligatoire + email valide
  telephone?: string;        // optionnel
}

export interface DemandeResponse {
  id: number;
  description: string;
  clientNom: string;
  clientEmail: string;
  statut: string;            // "NOUVELLE"
  type: string | null;       // null côté client (qualif interne)
  complexite: string | null;
  scoreConfiance: number | null;
  dateCreation: string;      // ISO
}

// Erreur 400 : dictionnaire { champ: message }
export type FieldErrors = Record<string, string>;
```

---

## 4. Service HTTP — la SEULE couche qui parle à l'API

**`src/app/data/demande.api.ts`** :

```ts
import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../core/api.config';
import type {
  DemandeRequest, DemandeResponse, IntakeRequest, IntakeResponse,
} from '../core/demande.models';

@Injectable({ providedIn: 'root' })
export class DemandeApi {
  private readonly http = inject(HttpClient);
  private readonly base = inject(API_BASE_URL);

  /** Analyse la description → complet ? + questions. Appelé à chaque étape. */
  analyser(description: string): Observable<IntakeResponse> {
    const body: IntakeRequest = { description };
    return this.http.post<IntakeResponse>(`${this.base}/intake`, body);
  }

  /** Soumission finale (une seule fois). */
  soumettre(payload: DemandeRequest): Observable<DemandeResponse> {
    return this.http.post<DemandeResponse>(`${this.base}/demandes`, payload);
  }
}
```

> `Content-Type: application/json` est mis automatiquement par `HttpClient` pour un body objet.

---

## 5. Le composant « formulaire vivant » — machine à états (signals)

Le cœur. On modélise le parcours comme **une machine à états** pilotée par des `signal()`.

### 5.1 États

```
saisie        → l'utilisateur décrit son projet
analyse        → appel /intake en cours (loader)
questions      → afficher les questions renvoyées, collecter les réponses
coordonnees    → formulaire nom/email/téléphone
envoi          → appel /demandes en cours (loader)
confirmation   → "Votre demande n°{id} a bien été reçue"
erreur         → 502 (analyse indisponible) → bouton réessayer
```

### 5.2 Composant — **`src/app/features/demande/demande-form.ts`**

```ts
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { DemandeApi } from '../../data/demande.api';
import type { DemandeResponse, FieldErrors, IntakeResponse } from '../../core/demande.models';

type Phase = 'saisie' | 'analyse' | 'questions' | 'coordonnees' | 'envoi' | 'confirmation' | 'erreur';

interface Echange { question: string; reponse: string; }

const MAX_ETAPES = 5;

@Component({
  selector: 'app-demande-form',
  imports: [ReactiveFormsModule],
  templateUrl: './demande-form.html',
  styleUrl: './demande-form.css',
})
export class DemandeForm {
  private readonly api = inject(DemandeApi);
  private readonly fb = inject(FormBuilder);

  // --- état ---
  readonly phase = signal<Phase>('saisie');
  readonly description = signal('');           // accumulateur
  readonly etape = signal(0);
  readonly analyse = signal<IntakeResponse | null>(null);
  readonly historique = signal<Echange[]>([]); // fil de conversation
  readonly result = signal<DemandeResponse | null>(null);
  readonly errorMessage = signal<string | null>(null);

  // --- dérivés ---
  readonly progression = computed(() => {
    const parScore = (this.analyse()?.score_confiance ?? 0) * 100;
    const parEtapes = (this.etape() / MAX_ETAPES) * 100;
    return Math.round(Math.max(parScore, parEtapes)); // 0..100
  });

  // Saisie initiale + réponses aux questions courantes (1 champ par question).
  readonly saisieForm = this.fb.nonNullable.group({ texte: ['', Validators.required] });
  readonly reponses = signal<string[]>([]);

  // Coordonnées finales.
  readonly coordForm = this.fb.nonNullable.group({
    nom: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    telephone: [''],
  });
  readonly fieldErrors = signal<FieldErrors>({});

  // ---------- étape 1 : première analyse ----------
  demarrer(): void {
    if (this.saisieForm.invalid) return;
    this.description.set(this.saisieForm.getRawValue().texte.trim());
    this.lancerAnalyse();
  }

  // ---------- boucle : (ré)analyse ----------
  private lancerAnalyse(): void {
    this.phase.set('analyse');
    this.errorMessage.set(null);
    this.api.analyser(this.description()).subscribe({
      next: (res) => this.traiterAnalyse(res),
      error: (e) => this.gererErreur(e),
    });
  }

  private traiterAnalyse(res: IntakeResponse): void {
    this.analyse.set(res);
    // Garde-fou : complet OU trop d'étapes → coordonnées.
    if (res.complet || this.etape() >= MAX_ETAPES) {
      this.phase.set('coordonnees');
      return;
    }
    this.reponses.set(res.questions.map(() => '')); // un champ vide par question
    this.phase.set('questions');
  }

  majReponse(index: number, valeur: string): void {
    const copie = [...this.reponses()];
    copie[index] = valeur;
    this.reponses.set(copie);
  }

  // ---------- valider les réponses → enrichir + ré-analyser ----------
  envoyerReponses(): void {
    const questions = this.analyse()?.questions ?? [];
    const rep = this.reponses();

    let desc = this.description();
    const nouveaux: Echange[] = [];
    questions.forEach((q, i) => {
      const r = (rep[i] ?? '').trim();
      if (!r) return;                      // on n'ajoute que les réponses remplies
      desc += `\n${q} : ${r}`;
      nouveaux.push({ question: q, reponse: r });
    });

    this.description.set(desc);
    this.historique.update((h) => [...h, ...nouveaux]);
    this.etape.update((n) => n + 1);
    this.lancerAnalyse();
  }

  // ---------- étape finale : soumettre ----------
  soumettre(): void {
    if (this.coordForm.invalid) { this.coordForm.markAllAsTouched(); return; }
    this.phase.set('envoi');
    this.fieldErrors.set({});
    const { nom, email, telephone } = this.coordForm.getRawValue();

    this.api.soumettre({
      description: this.description(),
      nom, email,
      telephone: telephone || undefined,
    }).subscribe({
      next: (res) => { this.result.set(res); this.phase.set('confirmation'); },
      error: (e) => this.gererErreurSoumission(e),
    });
  }

  // ---------- erreurs ----------
  private gererErreur(e: HttpErrorResponse): void {
    if (e.status === 502) {
      this.errorMessage.set("Le service d'analyse est momentanément indisponible.");
    } else {
      this.errorMessage.set('Une erreur est survenue. Réessayez.');
    }
    this.phase.set('erreur');
  }

  private gererErreurSoumission(e: HttpErrorResponse): void {
    if (e.status === 400 && e.error && typeof e.error === 'object') {
      this.fieldErrors.set(e.error as FieldErrors); // { email: "...", nom: "..." }
      this.phase.set('coordonnees');
    } else if (e.status === 502) {
      this.errorMessage.set("Service momentanément indisponible.");
      this.phase.set('erreur');
    } else {
      this.errorMessage.set("Échec de l'envoi. Réessayez.");
      this.phase.set('erreur');
    }
  }

  // Bouton "réessayer" de l'écran erreur.
  reessayer(): void {
    this.phase.set(this.result() ? 'coordonnees' : 'analyse');
    this.lancerAnalyse();
  }
}
```

### 5.3 Template — **`demande-form.html`** (control-flow Angular `@if` / `@for`)

```html
<section class="demande">
  <!-- Progression (score_confiance ou nb d'étapes) -->
  @if (phase() !== 'confirmation') {
    <div class="progress"><div class="progress__bar" [style.width.%]="progression()"></div></div>
  }

  <!-- Fil de conversation (historique Q/R) -->
  @if (historique().length) {
    <ul class="fil">
      @for (e of historique(); track $index) {
        <li><b>{{ e.question }}</b><span>{{ e.reponse }}</span></li>
      }
    </ul>
  }

  @switch (phase()) {
    <!-- 1. Saisie initiale -->
    @case ('saisie') {
      <form [formGroup]="saisieForm" (ngSubmit)="demarrer()">
        <label>Décrivez votre projet</label>
        <textarea formControlName="texte" rows="6" placeholder="Ex. un site vitrine avec prise de rendez-vous…"></textarea>
        <button type="submit" [disabled]="saisieForm.invalid">Analyser</button>
      </form>
    }

    <!-- 2. Analyse en cours -->
    @case ('analyse') { <p class="loader">Analyse de votre besoin…</p> }
    @case ('envoi')   { <p class="loader">Envoi de votre demande…</p> }

    <!-- 3. Questions dynamiques -->
    @case ('questions') {
      <form (ngSubmit)="envoyerReponses()">
        <p class="hint">Quelques précisions pour bien cadrer votre projet :</p>
        @for (q of analyse()!.questions; track $index) {
          <div class="q">
            <label>{{ q }}</label>
            <input [value]="reponses()[$index]"
                   (input)="majReponse($index, $any($event.target).value)" />
          </div>
        }
        <button type="submit">Continuer</button>
      </form>
    }

    <!-- 4. Coordonnées -->
    @case ('coordonnees') {
      <form [formGroup]="coordForm" (ngSubmit)="soumettre()">
        <label>Nom</label>
        <input formControlName="nom" />
        @if (fieldErrors()['nom']) { <small class="err">{{ fieldErrors()['nom'] }}</small> }

        <label>Email</label>
        <input formControlName="email" type="email" />
        @if (fieldErrors()['email']) { <small class="err">{{ fieldErrors()['email'] }}</small> }

        <label>Téléphone (optionnel)</label>
        <input formControlName="telephone" />

        <button type="submit" [disabled]="coordForm.invalid">Envoyer ma demande</button>
      </form>
    }

    <!-- 5. Confirmation -->
    @case ('confirmation') {
      <div class="ok">
        <h2>Merci !</h2>
        <p>Votre demande n°<b>{{ result()!.id }}</b> a bien été reçue.</p>
      </div>
    }

    <!-- 6. Erreur (502) -->
    @case ('erreur') {
      <div class="ko">
        <p>{{ errorMessage() }}</p>
        <button (click)="reessayer()">Réessayer</button>
      </div>
    }
  }
</section>
```

---

## 6. Routing

**`src/app/app.routes.ts`** — expose le formulaire (ex. sur la racine) :

```ts
import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./features/demande/demande-form').then((m) => m.DemandeForm),
  },
  { path: '**', redirectTo: '' },
];
```

Et vérifie que `app.html` contient bien `<router-outlet />`.

---

## 7. Gestion d'erreurs — récap

| Cas | Détection | Réaction |
|-----|-----------|----------|
| `/intake` **502** | `HttpErrorResponse.status === 502` | écran `erreur` + message + bouton **Réessayer** |
| `/demandes` **400** | `status === 400`, corps = `{ champ: message }` | rester sur `coordonnees`, afficher les erreurs par champ (`fieldErrors`) |
| `/demandes` **502** | `status === 502` | écran `erreur` + réessayer |
| Réseau / autre | sinon | message générique |

> `HttpClient` met le corps d'erreur JSON dans `error.error`. Pour un 400 ce sera directement
> le dictionnaire `{ email: "Email invalide", ... }`.

---

## 8. UX & garde-fous (exigences de la spec)

- **Progression** : `progression()` (max entre `score_confiance*100` et `etape/5*100`).
- **Fil de conversation** : `historique()` affiché en continu (Q + réponse).
- **Garde-fou anti-boucle** : `MAX_ETAPES = 5` → on passe aux coordonnées même si `complet=false`.
- **Loader** : phases `analyse` / `envoi` (l'IA prend 1–3 s).
- **Accumulateur** : `description` grossit à chaque étape (`\nQuestion : réponse`), et c'est **cette
  description finale** qui part dans `POST /demandes`.
- **Une seule soumission** : `/demandes` n'est appelé que depuis la phase `coordonnees`.

---

## 9. Checklist de conformité (spec → implémentation)

| Exigence de la spec | Où c'est traité |
|---|---|
| Base URL unique `http://localhost:8080` | `API_BASE_URL` (§2) |
| Aucune authentification | aucun intercepteur/token |
| `Content-Type: application/json` | auto via `HttpClient` (body objet) |
| `POST /intake { description }` | `DemandeApi.analyser()` (§4) |
| Boucle tant que `complet=false` | `traiterAnalyse()` + `envoyerReponses()` (§5.2) |
| Afficher 0–5 questions | phase `questions`, `@for` (§5.3) |
| Enrichir la description avec les réponses | `envoyerReponses()` (accumulateur) |
| Garde-fou 5 étapes | `MAX_ETAPES` + test `etape() >= MAX_ETAPES` |
| Progression (score_confiance) | `progression()` (§5.2) |
| `POST /demandes` une seule fois | `soumettre()` (phase `coordonnees`) |
| `nom`/`email` obligatoires, `email` valide | `coordForm` Validators (§5.2) |
| `telephone` optionnel | envoyé `|| undefined` |
| Réponse → afficher n° de demande | phase `confirmation`, `result()!.id` |
| Erreur 400 par champ | `gererErreurSoumission()` + `fieldErrors` |
| Erreur 502 + réessayer | `gererErreur()` + phase `erreur` |
| Le front ne fait aucune logique métier | toute la décision (`complet`, `questions`) vient de l'API |

---

## 10. Tests manuels

1. `ng serve` (client) + backend `:8080` lancés.
2. Décris un projet **vague** (« je veux une app ») → tu dois recevoir des **questions**.
3. Réponds → la description s'enrichit, ré-analyse ; répète jusqu'à `complet=true` **ou** 5 étapes.
4. Sur l'écran coordonnées : email invalide → message d'erreur (front puis, si envoyé, back 400).
5. Soumets → écran de confirmation avec le **n° de demande**.
6. Coupe le service IA → `/intake` renvoie **502** → écran erreur + **Réessayer** fonctionne.

> **CORS** : le backend `:8080` doit autoriser l'origine du client (`http://localhost:4200`).
> Sinon, erreur CORS en console → à régler côté backend.
```
