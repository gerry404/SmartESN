# Formulaire vivant — Guide d'intégration

> Le **formulaire vivant** est un composant autonome et réutilisable. Il peut être :
> - **intégré** dans une application Angular existante,
> - **embarqué** sur n'importe quel site (iframe ou web component),
> - **utilisé seul** (l'app cliente elle-même),
>
> et il se **connecte à la plateforme SmartESN** via l'API — en identifiant l'entreprise
> destinataire pour que la demande arrive dans le bon pipe.

Composant : `DemandeForm` (`src/app/features/demande/demande-form.ts`).
Service : `DemandeApi` (`src/app/data/demande.api.ts`).

---

## 1. Ce qui le connecte à la plateforme

Deux paramètres suffisent :

| Paramètre | Rôle | Défaut |
|-----------|------|--------|
| `apiBaseUrl` | URL de la plateforme SmartESN | token `API_BASE_URL` (`http://localhost:8080`) |
| `entrepriseKey` | **Clé publique** de l'entreprise → route la demande vers son pipe | aucune (mono-tenant) |

La `entrepriseKey` est envoyée en en-tête `X-Entreprise-Key` sur `/intake` et `/demandes`.
> ⚠️ Adaptez le nom de l'en-tête au contrat réel du backend si besoin
> (voir `DemandeApi.headers()`).

Le composant expose aussi une sortie :

| Sortie | Émis quand | Charge utile |
|--------|-----------|--------------|
| `submitted` | la demande est enregistrée | `DemandeResponse` (dont `id`, `statut`) |

---

## 2. Mode A — Intégration dans une app Angular

C'est le plus direct : on importe le composant standalone et on le pose dans un template.

```ts
import { Component } from '@angular/core';
import { DemandeForm } from './features/demande/demande-form';
import type { DemandeResponse } from './core/demande.models';

@Component({
  selector: 'app-contact',
  imports: [DemandeForm],
  template: `
    <app-demande-form
      apiBaseUrl="https://api.smartesn.com"
      entrepriseKey="ent_pub_ax3f9"
      (submitted)="onDemande($event)"
    />
  `,
})
export class ContactPage {
  onDemande(d: DemandeResponse) {
    console.log('Demande reçue :', d.id);
    // ex. redirection, tracking, message de remerciement…
  }
}
```

**Prérequis** (déjà en place dans ce projet) : `provideHttpClient(withFetch())` dans
`app.config.ts`.

---

## 3. Mode B — Embarquer sur n'importe quel site

Pour un site qui n'est **pas** en Angular (WordPress, Webflow, site statique…), deux options.

### Option B1 — iframe (le plus simple)

On héberge l'app cliente et on l'embarque :

```html
<iframe
  src="https://form.smartesn.com/?ent=ent_pub_ax3f9"
  style="width:100%;max-width:640px;height:720px;border:0"
  title="Décrivez votre projet"
></iframe>
```

> Côté app, lisez `?ent=` (query param) pour alimenter `entrepriseKey`, et passez la valeur
> au composant racine. Isolation totale (styles/JS), zéro conflit avec le site hôte.

### Option B2 — Web component (`<smartesn-intake>`)  ✅ déjà packagé

Pour un rendu **inline** (sans iframe), le composant est empaqueté en *custom element* via
**Angular Elements**. Tout est en place :

- entrée : `src/main.elements.ts` (enregistre `<smartesn-intake>`),
- cible de build : `build-elements` dans `angular.json`,
- commande : **`npm run build:elements`** → produit un **fichier unique**
  `dist/elements/browser/main.js` (~55 Ko gzip).

Héberge ce `main.js` (renomme-le `intake.js` si tu veux) puis, sur le site hôte
(les `input()` deviennent des **attributs kebab-case**) :

```html
<script src="https://cdn.smartesn.com/intake.js" defer></script>

<smartesn-intake
  api-base-url="https://api.smartesn.com"
  entreprise-key="ent_pub_ax3f9"
></smartesn-intake>

<script>
  document.querySelector('smartesn-intake')
    .addEventListener('submitted', (e) => console.log('Demande', e.detail.id));
</script>
```

> Angular Elements mappe `apiBaseUrl` → attribut `api-base-url`, et l'`output()` `submitted`
> → un `CustomEvent` (`e.detail` = la `DemandeResponse`).

---

## 4. Mode C — Utilisation autonome

L'app cliente **est** le formulaire : elle sert de page publique dédiée
(`https://form.smartesn.com`). C'est la configuration par défaut de ce projet
(route `''` → `DemandeForm`, `app.routes.ts`). Pour le multi-tenant, lire l'entreprise depuis
l'URL et la fournir au composant.

---

## 5. Référence de configuration

```
<app-demande-form
  [apiBaseUrl]="'https://api.smartesn.com'"   // optionnel
  [entrepriseKey]="'ent_pub_ax3f9'"           // optionnel (multi-tenant)
  (submitted)="onDemande($event)"             // DemandeResponse
/>
```

- Sans `apiBaseUrl` → utilise le token `API_BASE_URL`.
- Sans `entrepriseKey` → aucun en-tête d'entreprise (mode mono-tenant).

---

## 6. Contrat d'API consommé

| Étape | Endpoint | Corps |
|-------|----------|-------|
| Analyse / questions | `POST {baseUrl}/intake` | `{ description }` |
| Soumission finale | `POST {baseUrl}/demandes` | `{ description, nom, email, telephone? }` |

En-tête ajouté si `entrepriseKey` fourni : `X-Entreprise-Key: <clé>`.
Détail des réponses et de la boucle de dialogue : voir **`formulaire-vivant.md`**.

---

## 7. Personnalisation visuelle

Les styles sont **scopés** au composant (`demande-form.css`) et pilotés par quelques couleurs.
Pour l'accorder à une charte, surchargez les valeurs de fond/dégradé/rayons dans ce fichier
(ou, en web component, exposez des CSS custom properties `--se-accent`, `--se-bg`… et remplacez
les valeurs en dur par `var(--se-accent, #a6d84b)`).

---

## 8. Points d'attention

- **CORS** : la plateforme doit autoriser l'origine du site hôte (et des embeds).
- **Clé publique uniquement** : `entrepriseKey` est une clé **publique** (côté navigateur) ;
  aucun secret ne doit transiter par le widget.
- **SSR** : les appels partent sur action utilisateur (navigateur) ; le rendu serveur ne fait
  aucun appel. En web component, pas de SSR — rendu client only.
- **Une seule soumission** : `/demandes` n'est appelé qu'à la fin ; `submitted` est émis une fois.
