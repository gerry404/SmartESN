# 📘 Documentation du Backend SmartESN

Ce document explique **tout** ce qui a été implémenté dans le backend, comment ça marche, et
pourquoi. Il est écrit pour être compris même sans être expert Spring Boot.

---

## 1. Vue d'ensemble

Le backend est une **API REST** en **Spring Boot** (Java). Il reçoit des requêtes HTTP
(du frontend ou d'outils comme Postman), applique la logique métier, et lit/écrit dans une
base **PostgreSQL**.

Tout ce qui est décrit ici fonctionne **sans l'IA**. L'IA (service FastAPI) viendra plus tard
remplir automatiquement le *type* et la *complexité* d'une demande ; pour l'instant on les
saisit manuellement, et tout le reste du pipeline (estimation, affectation, devis, conversion,
stats) tourne déjà.

### Le parcours d'une demande (le « pipeline »)

```
1. Le CLIENT soumet une demande            → statut NOUVELLE
2. Un INTERNE la qualifie (type+complexité) → estimation + affectation AUTO → statut QUALIFIEE
3. On génère un DEVIS (à partir de l'estimation)
4. On valide et on ENVOIE le devis          → statut DEVIS_ENVOYE
5. On suit la conversion (négociation...)   → statut EN_NEGOCIATION
6. Si signé → GAGNEE : on crée le PROJET + on note le budget/délai RÉELS
   Si perdu → PERDUE
7. Le REPORTING agrège tout ça (taux de conversion, CA, etc.)
```

---

## 2. L'architecture en couches (le concept clé)

Chaque requête traverse **3 couches**. C'est le principe fondamental de Spring :

```
   Requête HTTP
        │
        ▼
┌─────────────────┐   « Je reçois la requête, je vérifie l'entrée,
│   CONTROLLER    │     j'appelle les services, je renvoie la réponse. »
└────────┬────────┘     (ne contient PAS la logique métier lourde)
         │
         ▼
┌─────────────────┐   « Je fais le vrai travail métier :
│    SERVICE      │     estimer, affecter, générer un devis... »
└────────┬────────┘     (réutilisable, testable)
         │
         ▼
┌─────────────────┐   « Je parle à la base de données :
│   REPOSITORY    │     save, findById, findByStatut... »
└────────┬────────┘
         │
         ▼
   PostgreSQL
```

**Pourquoi séparer ?** Pour que chaque classe ait **une seule responsabilité**. Le controller
ne sait pas *comment* on estime un budget ; il demande juste au `EstimationService` de le faire.
Si demain la logique change, on modifie **un seul endroit**.

### Les autres types de classes

| Type | Rôle | Exemple |
|------|------|---------|
| **Entity** | Représente une table de la base | `Demande`, `Devis`, `Client` |
| **Repository** | Accès base (généré par Spring) | `DemandeRepository` |
| **Service** | Logique métier | `EstimationService` |
| **Controller** | Points d'entrée HTTP | `DemandeController` |
| **DTO** | Données échangées avec l'extérieur | `DemandeCreateRequest` |
| **Config / Security** | Réglages transverses | `SecurityConfig`, `DataSeeder` |

---

## 3. Détail des composants

### 3.1 Les entités (`entity/`)

Une **entité** = une table. Chaque champ = une colonne. Les annotations disent à Hibernate
(le moteur qui génère le SQL) comment mapper ça.

- `@Entity` : « cette classe est une table ».
- `@Id` + `@GeneratedValue(IDENTITY)` : clé primaire auto-incrémentée (`Long id`).
- `@Enumerated(EnumType.STRING)` : stocke un enum en texte (`"WEB"`) et pas un chiffre.
- `@ManyToOne` / `@OneToOne` + `@JoinColumn` : une **relation** (clé étrangère).
- `@CreationTimestamp` : Hibernate remplit la date à la création.

> **Astuce comprise en route** : on utilise `Long`, `Double`, `Integer`, `Boolean` (objets)
> et pas `long/double/int/boolean` (primitifs), car un objet peut valoir `null`
> (ex. le budget est `null` tant que la demande n'est pas estimée).

**Les relations principales :**
- une `Demande` appartient à un `Client` (`@ManyToOne`) ;
- une `Estimation`, un `Devis`, une `Affectation`, un `Projet` se rattachent chacun à **une**
  `Demande` (`@OneToOne`) ;
- une `Affectation` pointe vers une `Equipe` ; un `Utilisateur` vers une `Equipe`.

### 3.2 Les repositories (`repository/`)

Une **interface vide** qui étend `JpaRepository<Entité, TypeDeLId>`. Spring génère
automatiquement l'implémentation avec : `save`, `findById`, `findAll`, `count`, `deleteById`…

**Requêtes dérivées** : on peut ajouter une méthode dont le **nom** décrit la requête, et Spring
génère le SQL tout seul :
```java
Optional<Client> findByEmail(String email);          // WHERE email = ?
List<Demande>     findByStatut(StatutDemande statut); // WHERE statut = ?
long              countByStatut(StatutDemande statut);// COUNT WHERE statut = ?
```

### 3.3 Les services (`service/`)

- **`EstimationService`** — estime budget + délai. Il cherche dans la **grille de référence**
  la ligne qui correspond au couple *(type, complexité)*, et crée une `Estimation`
  (budgetMin, budgetMax, délai). 100 % déterministe (pas d'IA).
- **`AffectationService`** — applique les règles d'affectation :
  `CONSEIL → Équipe Commerciale`, sinon `→ Équipe Technique`. Crée une `Affectation`.
- **`DevisService`** — génère le texte du devis à partir de l'estimation (modèle de texte +
  montant = centre de la fourchette). Un seul devis par demande (réutilisé s'il existe).
- **`StatistiqueService`** — calcule les indicateurs du reporting (comptes par statut,
  taux de conversion, CA potentiel et signé).
- **`JwtService`** — fabrique et vérifie les jetons JWT (voir §4).

### 3.4 Les DTO (`dto/`)

Un **DTO** (Data Transfer Object) = ce qui **entre** ou **sort** de l'API, séparé des entités.

**Pourquoi ne pas exposer directement les entités ?**
1. **Sécurité** : à la création d'une demande, le client ne doit envoyer QUE
   `description + coordonnées`. Le DTO `DemandeCreateRequest` n'accepte que ces champs →
   impossible pour un client de forcer `statut` ou `id` (attaque « over-posting »).
2. **Contrôle** : on choisit exactement ce qu'on renvoie (`DemandeResponse`,
   `DemandeDetailResponse`).
3. **Robustesse** : évite les bugs de sérialisation JSON dus aux relations.

On utilise des **`record`** Java (classes immuables très concises).

La **validation** se fait par annotations sur le DTO (`@NotBlank`, `@Email`, `@NotNull`) +
`@Valid` dans le controller → si l'entrée est invalide, réponse **400** automatique.

### 3.5 La configuration (`config/`)

- **`DataSeeder`** (`CommandLineRunner`) — s'exécute **au démarrage** et peuple la base
  **une seule fois** avec : l'utilisateur admin, les 2 équipes, et la **grille de référence**
  (12 lignes : type × complexité → budget/délai). C'est grâce à lui que l'estimation trouve
  ses chiffres.
- **`GlobalExceptionHandler`** (`@RestControllerAdvice`) — transforme les exceptions en
  réponses HTTP propres : erreurs de validation → **400** (avec le détail des champs),
  règle métier violée → **409**, service IA injoignable → **502**. Évite les erreurs 500 brutes.

### 3.6 L'intégration du service IA (`IaClient`)

Le backend Java ne contient **aucune logique d'IA** : il **délègue** au microservice IA
(FastAPI), appelé en HTTP.

- **`IaClient`** (`service/`) — encapsule l'appel réseau vers le service IA. Il expose une
  méthode `classifier(description)` qui appelle `POST /classify` du service IA et renvoie le
  type de projet, la complexité et un score de confiance.
- L'adresse du service IA est configurable via `app.ia.base-url` (lue depuis le `.env`,
  valeur par défaut `http://localhost:8000`).
- Détail technique : le client est configuré en **HTTP/1.1 pur** (`SimpleClientHttpRequestFactory`),
  car le serveur FastAPI n'accepte pas les tentatives d'« upgrade » HTTP/2 du client par défaut.

**Deux façons de qualifier une demande :**
- **manuelle** : un interne saisit lui-même le type et la complexité (`PUT /demandes/{id}/qualifier`) ;
- **automatique** : l'IA les détermine à partir de la description (`POST /demandes/{id}/analyser`).

Dans les deux cas, la suite est **identique et déterministe** (méthode commune côté controller) :
estimation via la grille, puis affectation à l'équipe, puis passage au statut `QUALIFIEE`.
Si le service IA est indisponible, l'appel renvoie une **502** propre.

---

## 4. La sécurité (JWT + rôles)

**JWT = un « bracelet » signé.** Après login, le client reçoit un jeton ; il le renvoie à
chaque requête dans l'en-tête `Authorization: Bearer <jeton>`. Le serveur ne garde **aucune
session** (architecture *stateless*).

**Le circuit :**
1. `POST /auth/login` → `AuthController` vérifie email + mot de passe (comparé au hash **BCrypt**).
2. Si OK → `JwtService.genererToken(email, role)` renvoie un jeton contenant l'email + le rôle.
3. À chaque requête suivante, `JwtAuthenticationFilter` lit le jeton, le valide, et
   **injecte le rôle** (`ROLE_ADMIN`, `ROLE_COMMERCIAL`…) dans le contexte de sécurité.
4. `SecurityConfig` définit les règles d'accès :
   - `/health`, `/auth/**` : **public** ;
   - `POST /demandes` : **public** (le client soumet sans compte) ;
   - `/admin/**` : **rôle ADMIN uniquement** ;
   - tout le reste : **authentifié**.

**CORS** : `SecurityConfig` autorise les origines `http://localhost:4200` (Angular) et
`:5173` (Vue) à appeler l'API. Sans ça, le navigateur bloquerait tous les appels du frontend.

---

## 5. Référence des endpoints

Base URL : `http://localhost:8080` · JSON partout · jeton via `Authorization: Bearer <token>`.

### 🔓 Public (sans jeton)

| Méthode | URL | Corps | Réponse |
|--------|-----|-------|---------|
| GET  | `/health` | — | `Service is running!` |
| POST | `/auth/login` | `{email, motDePasse}` | `{token, email, role}` ou 401 |
| POST | `/demandes` | `{description, nom, email, telephone?}` | la demande créée (statut NOUVELLE) |

### 🔒 Authentifié (jeton requis)

| Méthode | URL | Corps | Rôle | Description |
|--------|-----|-------|------|-------------|
| GET | `/demandes` | — (`?statut=NOUVELLE` optionnel) | interne | Liste des demandes |
| GET | `/demandes/{id}` | — | interne | Détail (avec budget/délai/équipe) |
| PUT | `/demandes/{id}/qualifier` | `{type, complexite}` | interne | Qualification **manuelle** → estime + affecte auto |
| POST | `/demandes/{id}/analyser` | — | interne | Qualification **par l'IA** → estime + affecte auto |
| PUT | `/demandes/{id}/statut` | `{statut, budgetSigne?, delaiReel?}` | interne | Change le statut (si GAGNEE → crée le projet) |
| POST | `/demandes/{id}/reaffecter` | `{equipeId}` | interne | Réaffectation manuelle |
| POST | `/demandes/{id}/devis` | — | interne | Génère le devis |
| GET | `/demandes/{id}/devis` | — | interne | Consulte le devis |
| POST | `/devis/{id}/envoyer` | — | interne | Valide + envoie → statut DEVIS_ENVOYE |
| GET | `/statistiques` | — | interne | Indicateurs du reporting |
| GET | `/equipes` | — | interne | Liste des équipes |

### 👑 Administration (rôle ADMIN)

| Méthode | URL | Corps | Description |
|--------|-----|-------|-------------|
| GET | `/admin/utilisateurs` | — | Liste des utilisateurs internes |
| POST | `/admin/utilisateurs` | `{nom, email, motDePasse, role}` | Crée un utilisateur |
| DELETE | `/admin/utilisateurs/{id}` | — | Supprime un utilisateur |
| GET | `/admin/grille` | — | Liste la grille de référence |
| PUT | `/admin/grille/{id}` | `{budgetMin, budgetMax, delaiMin, delaiMax}` | Modifie une ligne de grille |

### Valeurs autorisées (enums)

- **type** : `MOBILE`, `WEB`, `DESKTOP`, `CONSEIL`
- **complexite** : `SIMPLE`, `MOYENNE`, `COMPLEXE`
- **statut** : `NOUVELLE`, `EN_ANALYSE`, `QUALIFIEE`, `DEVIS_ENVOYE`, `EN_NEGOCIATION`, `GAGNEE`, `PERDUE`
- **role** : `COMMERCIAL`, `DIRECTEUR_TECHNIQUE`, `ADMIN`

**Compte de test créé au démarrage :** `admin@smartesn.com` / `admin123`

---

## 6. Lancer et tester

### Démarrer
```powershell
cd $HOME\Desktop\SmartESN\backend
.\gradlew.bat bootRun
```
Prérequis :
- **PostgreSQL** doit tourner (base `smartesn`).
- Un fichier **`.env`** (copié depuis `.env.example`) renseigne `DB_PASSWORD`, `JWT_SECRET`
  et `IA_BASE_URL`. Sans `.env`, les valeurs par défaut de développement s'appliquent.
- Pour la **qualification par IA** (`/analyser`), le **service IA** doit tourner (port 8000).

### Exemple de test complet (PowerShell)
```powershell
# 1. Login
$r = Invoke-RestMethod -Uri http://localhost:8080/auth/login -Method Post `
     -ContentType "application/json" -Body '{"email":"admin@smartesn.com","motDePasse":"admin123"}'
$t = $r.token
$h = @{ Authorization = "Bearer $t" }

# 2. Soumettre une demande (public)
$d = Invoke-RestMethod -Uri http://localhost:8080/demandes -Method Post `
     -ContentType "application/json" `
     -Body '{"description":"Site web vitrine","nom":"Bob","email":"bob@test.com"}'

# 3a. Qualifier MANUELLEMENT → estimation + affectation automatiques
Invoke-RestMethod -Uri "http://localhost:8080/demandes/$($d.id)/qualifier" -Method Put `
     -Headers $h -ContentType "application/json" -Body '{"type":"WEB","complexite":"MOYENNE"}'

# 3b. (Alternative) Qualifier PAR L'IA (nécessite le service IA + une clé LLM)
# Invoke-RestMethod -Uri "http://localhost:8080/demandes/$($d.id)/analyser" -Method Post -Headers $h

# 4. Générer puis envoyer le devis
$dv = Invoke-RestMethod -Uri "http://localhost:8080/demandes/$($d.id)/devis" -Method Post -Headers $h
Invoke-RestMethod -Uri "http://localhost:8080/devis/$($dv.id)/envoyer" -Method Post -Headers $h

# 5. Marquer gagnée (crée le projet + enregistre le réel)
Invoke-RestMethod -Uri "http://localhost:8080/demandes/$($d.id)/statut" -Method Put `
     -Headers $h -ContentType "application/json" `
     -Body '{"statut":"GAGNEE","budgetSigne":950000,"delaiReel":8}'

# 6. Reporting
Invoke-RestMethod -Uri http://localhost:8080/statistiques -Method Get -Headers $h
```

---

## 7. Ce qui reste à faire

Déjà en place : la **qualification automatique** est branchée (`POST /demandes/{id}/analyser`
appelle le service IA). Restent à intégrer :

- **Contenu rédigé** du devis + proposition technique via le service IA (endpoint `/draft`
  du service IA déjà prêt ; à appeler depuis le backend).
- **Formulaire dynamique** (endpoint `/intake` du service IA à relier au frontend).
- **Synthèse du reporting** en langage naturel (LLM) — les *chiffres* sont déjà là.
- **Export Jira**, **envoi e-mail réel** (services externes).

---

## 8. Points d'attention (dette technique à traiter avant la prod)

- Les secrets (**mot de passe PostgreSQL**, **clé JWT**, **base-url IA**) sont désormais lus
  depuis un fichier **`.env`** (non versionné). `application.yaml` ne contient plus que des
  valeurs par défaut de développement. ✔
- `ddl-auto: update` ne supprime jamais les colonnes obsolètes (on a dû retirer à la main la
  colonne fantôme `valider`). Pour la prod, prévoir un outil de migration (Flyway/Liquibase).
- La gestion d'erreurs peut être enrichie (404 « ressource introuvable » standardisés).
