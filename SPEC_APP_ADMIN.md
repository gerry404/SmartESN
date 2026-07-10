# 🛠️ Guide d'intégration — Application d'administration (interne)

Document destiné au développeur frontend de l'app **admin** (Vue.js).
Le **login est déjà fait** ; ce guide couvre **toutes les autres fonctionnalités** avec leur
contrat d'API (endpoints, corps de requête/réponse, collections).

---

## 1. Généralités

- **Base URL** : `http://localhost:8080`
- **Format** : JSON partout (`Content-Type: application/json`).
- **Authentification** : toutes les routes ci-dessous (sauf mention contraire) exigent le
  **jeton JWT** obtenu au login, envoyé dans l'en-tête :
  ```
  Authorization: Bearer <token>
  ```
- **Rôles** : certaines routes sont réservées au rôle `ADMIN` (section 8). Le rôle est renvoyé
  au login (`role`) et encodé dans le token.
- **Codes d'erreur communs** :
  - `400` : données invalides (le corps précise le champ fautif) ;
  - `401` : non authentifié (token absent/invalide) → rediriger vers login ;
  - `403` : authentifié mais rôle insuffisant ;
  - `404` : ressource introuvable ;
  - `409` : règle métier violée (ex. pas de grille pour ce type/complexité) ;
  - `502` : service IA momentanément indisponible.

### Rappel login (déjà implémenté)
`POST /auth/login` → `{ "token": "...", "email": "...", "role": "ADMIN" }`
Compte de test : `admin@smartesn.com` / `admin123`.

---

## 2. Énumérations (valeurs fixes)

| Enum | Valeurs |
|------|---------|
| `type` (TypeProjet) | `MOBILE`, `WEB`, `DESKTOP`, `CONSEIL` |
| `complexite` | `SIMPLE`, `MOYENNE`, `COMPLEXE` |
| `statut` (StatutDemande) | `NOUVELLE`, `EN_ANALYSE`, `QUALIFIEE`, `DEVIS_ENVOYE`, `EN_NEGOCIATION`, `GAGNEE`, `PERDUE` |
| `role` (RoleUtilisateur) | `COMMERCIAL`, `DIRECTEUR_TECHNIQUE`, `ADMIN` |

---

## 3. Écran « Tableau de bord » (liste des demandes)

### Lister les demandes
`GET /demandes` — liste toutes les demandes.
Filtre optionnel par statut : `GET /demandes?statut=NOUVELLE`.

**Réponse (200)** — tableau de :
```json
{
  "id": 12,
  "description": "Une appli mobile de vente",
  "clientNom": "Awa Ndiaye",
  "clientEmail": "awa@exemple.com",
  "statut": "NOUVELLE",
  "type": null,
  "complexite": null,
  "scoreConfiance": null,
  "dateCreation": "2026-07-09T10:15:00"
}
```
> `type`, `complexite`, `scoreConfiance` sont `null` tant que la demande n'est pas qualifiée.
> Idéal pour un tableau avec colonnes + badges de statut + filtres.

---

## 4. Écran « Détail d'une demande »

### Consulter le détail
`GET /demandes/{id}`

**Réponse (200)**
```json
{
  "id": 12,
  "description": "Une appli mobile de vente",
  "clientNom": "Awa Ndiaye",
  "clientEmail": "awa@exemple.com",
  "statut": "QUALIFIEE",
  "type": "MOBILE",
  "complexite": "MOYENNE",
  "dateCreation": "2026-07-09T10:15:00",
  "budgetMin": 600000.0,
  "budgetMax": 1500000.0,
  "delaiSemaines": 10,
  "equipeAffectee": "Équipe Technique"
}
```
`404` si l'id n'existe pas.

---

## 5. Qualification (remplir type + complexité)

Deux options — les deux déclenchent **automatiquement** l'estimation (budget/délai via la
grille) et l'affectation à l'équipe, puis passent la demande au statut `QUALIFIEE`.

### 5.a Qualification manuelle
`PUT /demandes/{id}/qualifier`
```json
{ "type": "MOBILE", "complexite": "MOYENNE" }
```

### 5.b Qualification par l'IA
`POST /demandes/{id}/analyser` — pas de corps.
L'IA détermine le type et la complexité depuis la description.
> Nécessite que le service IA tourne. Renvoie `502` s'il est indisponible.

**Réponse (les deux)** : le `DemandeDetailResponse` (comme section 4), avec budget/délai/équipe remplis.

---

## 6. Devis

### Générer le devis
`POST /demandes/{id}/devis` — pas de corps. Crée le devis à partir de l'estimation.

**Réponse (200)**
```json
{
  "id": 5,
  "demandeId": 12,
  "contenu": "DEVIS PRÉLIMINAIRE - SmartESN\n...",
  "montant": 1050000.0,
  "valide": false,
  "dateCreation": "2026-07-09T10:20:00",
  "dateEnvoi": null
}
```

### Consulter le devis d'une demande
`GET /demandes/{id}/devis` → même forme. `404` si aucun devis.

### Valider et envoyer le devis
`POST /devis/{id}/envoyer` — pas de corps.
Marque le devis comme validé (`valide: true`, `dateEnvoi` renseignée) et passe la demande au
statut `DEVIS_ENVOYE`.
> C'est l'étape de **validation humaine** avant envoi au client.

---

## 7. Suivi de conversion & affectation

### Changer le statut commercial
`PUT /demandes/{id}/statut`
```json
{ "statut": "EN_NEGOCIATION" }
```
Pour une demande **gagnée**, fournir aussi le réel (crée le projet + alimente la boucle de feedback) :
```json
{ "statut": "GAGNEE", "budgetSigne": 950000, "delaiReel": 8 }
```
`budgetSigne` et `delaiReel` sont optionnels sauf pour `GAGNEE`.

### Réaffecter manuellement à une autre équipe
`POST /demandes/{id}/reaffecter`
```json
{ "equipeId": 1 }
```

### Lister les équipes (pour le sélecteur de réaffectation)
`GET /equipes`
```json
[ { "id": 1, "nom": "Équipe Technique", "specialite": "WEB" },
  { "id": 2, "nom": "Équipe Commerciale", "specialite": "CONSEIL" } ]
```

---

## 8. Écran « Reporting »

### Indicateurs
`GET /statistiques`
```json
{
  "totalDemandes": 42,
  "nbNouvelles": 10,
  "nbQualifiees": 8,
  "nbDevisEnvoyes": 12,
  "nbGagnees": 9,
  "nbPerdues": 3,
  "tauxConversion": 75.0,
  "caPotentiel": 8400000.0,
  "caSigne": 6200000.0
}
```
> Parfait pour des cartes KPI + un graphe en camembert (répartition par statut) et un
> histogramme (CA potentiel vs signé).

---

## 9. Écran « Administration » (rôle ADMIN uniquement)

Ces routes renvoient `403` si l'utilisateur n'est pas `ADMIN`.

### Gestion des utilisateurs internes
| Méthode | URL | Corps | Description |
|---------|-----|-------|-------------|
| GET | `/admin/utilisateurs` | — | Liste des utilisateurs |
| POST | `/admin/utilisateurs` | `{nom, email, motDePasse, role}` | Crée un utilisateur |
| DELETE | `/admin/utilisateurs/{id}` | — | Supprime un utilisateur |

**Réponse utilisateur** (jamais le mot de passe) :
```json
{ "id": 3, "nom": "Sarah", "email": "sarah@smartesn.com", "role": "COMMERCIAL" }
```

### Paramétrage de la grille de référence
| Méthode | URL | Corps | Description |
|---------|-----|-------|-------------|
| GET | `/admin/grille` | — | Liste des 12 lignes de la grille |
| PUT | `/admin/grille/{id}` | `{budgetMin, budgetMax, delaiMin, delaiMax}` | Modifie une ligne |

**Ligne de grille**
```json
{ "id": 2, "type": "MOBILE", "complexite": "MOYENNE",
  "budgetMin": 600000, "budgetMax": 1500000, "delaiMin": 6, "delaiMax": 10 }
```

---

## 10. Récapitulatif des endpoints (par écran)

| Écran / Feature | Endpoint(s) | Auth |
|-----------------|-------------|------|
| Login | `POST /auth/login` | public |
| Tableau de bord | `GET /demandes[?statut=]` | interne |
| Détail demande | `GET /demandes/{id}` | interne |
| Qualifier (manuel) | `PUT /demandes/{id}/qualifier` | interne |
| Qualifier (IA) | `POST /demandes/{id}/analyser` | interne |
| Devis | `POST` & `GET /demandes/{id}/devis`, `POST /devis/{id}/envoyer` | interne |
| Statut / conversion | `PUT /demandes/{id}/statut` | interne |
| Réaffectation | `POST /demandes/{id}/reaffecter`, `GET /equipes` | interne |
| Reporting | `GET /statistiques` | interne |
| Admin utilisateurs | `GET/POST/DELETE /admin/utilisateurs` | ADMIN |
| Admin grille | `GET /admin/grille`, `PUT /admin/grille/{id}` | ADMIN |

---

## 11. Conseils d'intégration (côté Vue)

- **Stockage du token** : conserver le `token` après login (ex. `localStorage` ou store Pinia).
- **Intercepteur HTTP** : ajouter automatiquement l'en-tête `Authorization: Bearer <token>` sur
  chaque requête, et rediriger vers le login sur `401`.
- **Gestion des rôles** : masquer les écrans d'administration si `role !== 'ADMIN'` (et gérer le
  `403` renvoyé par l'API comme filet de sécurité).
- **États de chargement** : afficher un loader sur les appels (surtout `/analyser` qui passe par
  l'IA, 1–3 s).
- **CORS** : déjà autorisé côté backend pour `http://localhost:4200` (Angular) et
  `http://localhost:5173` (Vite/Vue). Si l'app admin tourne sur un autre port, me le dire pour
  l'ajouter.
