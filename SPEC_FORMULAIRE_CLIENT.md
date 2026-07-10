# 📋 Spécification — Formulaire client dynamique (« formulaire vivant »)

Document destiné au développeur frontend (application **client**, Angular).
Il décrit la fonctionnalité, le parcours utilisateur et le **contrat d'API** exact à consommer.

---

## 1. Principe

Le formulaire de soumission n'est **pas** un formulaire figé. Il **dialogue** avec le visiteur :

1. le client décrit son projet en texte libre ;
2. le système analyse la description ;
3. si elle est trop imprécise pour être chiffrée, le système **renvoie des questions ciblées**
   que le front affiche ;
4. les réponses enrichissent la description ; on ré-analyse ;
5. quand la description est jugée **complète**, le client saisit ses **coordonnées** et valide ;
6. la demande est enregistrée.

> Toute la logique « quelles questions poser / est-ce complet » est côté serveur (IA).
> Le front ne fait qu'**afficher** ce que l'API renvoie et **renvoyer** les réponses.

---

## 2. Base URL & règles générales

- **Base URL** : `http://localhost:8080` (le backend ; ne jamais appeler le service IA directement).
- **Aucune authentification** : ce sont des endpoints **publics** (le client n'a pas de compte).
- **Content-Type** : `application/json` sur tous les appels.
- Réponses en JSON.

---

## 3. Les 2 endpoints à consommer

### 3.1 `POST /intake` — analyser / obtenir les questions

Appelé à chaque étape du dialogue.

**Requête**
```json
{ "description": "texte du projet (enrichi des réponses déjà données)" }
```

**Réponse**
```json
{
  "complet": false,
  "score_confiance": 0.4,
  "type_probable": "WEB",
  "questions": [
    "Souhaitez-vous un paiement en ligne ?",
    "Avez-vous besoin d'un espace d'administration ?",
    "Combien de pages environ ?"
  ]
}
```

| Champ | Type | Signification |
|-------|------|---------------|
| `complet` | booléen | `true` = assez d'infos pour chiffrer → passer à l'étape coordonnées. `false` = poser les questions. |
| `score_confiance` | nombre 0–1 | niveau de confiance (peut servir à une barre de progression). |
| `type_probable` | `MOBILE` \| `WEB` \| `DESKTOP` \| `CONSEIL` \| null | type pressenti (indicatif). |
| `questions` | liste de textes | 0 à 5 questions à afficher (vide si `complet = true`). |

### 3.2 `POST /demandes` — soumettre la demande finale

Appelé **une seule fois**, à la fin, quand `complet = true` et que les coordonnées sont saisies.

**Requête**
```json
{
  "description": "description finale complète (avec toutes les réponses intégrées)",
  "nom": "Awa Ndiaye",
  "email": "awa@exemple.com",
  "telephone": "690000000"
}
```
- `description`, `nom`, `email` : **obligatoires** (`email` doit être un email valide).
- `telephone` : optionnel.

**Réponse (200)**
```json
{
  "id": 12,
  "description": "...",
  "clientNom": "Awa Ndiaye",
  "clientEmail": "awa@exemple.com",
  "statut": "NOUVELLE",
  "type": null,
  "complexite": null,
  "scoreConfiance": null,
  "dateCreation": "2026-07-09T10:15:00"
}
```
> `type`/`complexite`/budget restent `null` : la qualification et le chiffrage se font **en
> interne** (par l'équipe), pas côté client. Le client reçoit juste une confirmation.

**Erreur (400)** si un champ obligatoire est manquant/invalide :
```json
{ "email": "Email invalide", "nom": "Le nom est obligatoire" }
```

---

## 4. Le parcours à implémenter (algorithme)

```
description = ""            // accumulateur
etape = 0
MAX_ETAPES = 5

// Étape 1 : saisie initiale
description = saisieLibreDuClient()

boucle:
    reponse = POST /intake { description }

    si reponse.complet == true  OU  etape >= MAX_ETAPES :
        sortir de la boucle

    // afficher les questions et récupérer les réponses
    pour chaque question de reponse.questions :
        rep = afficherEtRecupererReponse(question)
        description = description + "\n" + question + " : " + rep

    etape = etape + 1

// Étape finale : coordonnées
{ nom, email, telephone } = formulaireCoordonnees()
resultat = POST /demandes { description, nom, email, telephone }

afficherConfirmation(resultat)   // "Votre demande n°{id} a bien été reçue"
```

**Points d'attention UX :**
- afficher une **progression** (basée sur `score_confiance` ou le nb d'étapes) ;
- garder l'historique des Q/R affiché (fil de conversation) ;
- **garde-fou** : arrêter après 5 étapes même si `complet` reste `false` (éviter une boucle sans fin) ;
- pendant l'appel `/intake`, montrer un **loader** (l'IA prend 1–3 s) ;
- gérer l'erreur **502** (« service d'analyse momentanément indisponible ») → message clair +
  bouton « réessayer ».

---

## 5. Résumé pour le dev

| Écran | Action | Endpoint |
|-------|--------|----------|
| Saisie du projet | envoyer la description | `POST /intake` |
| Questions dynamiques | boucler tant que `complet = false` | `POST /intake` |
| Coordonnées + validation | soumettre la demande | `POST /demandes` |
| Confirmation | afficher le n° de demande | — |

Deux endpoints, un seul base URL, aucune authentification. Le front n'implémente **aucune
logique métier** : il orchestre l'affichage à partir des réponses de l'API.
