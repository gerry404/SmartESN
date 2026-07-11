# 🚀 Guide de déploiement — SmartESN

Ce document explique comment déployer SmartESN en production. Il est destiné à un
développeur (ou une IA) qui exécute le déploiement. L'application est **fonctionnelle de bout
en bout** ; ce guide couvre la mise en production.

---

## 1. Architecture (4 services + 1 base)

SmartESN est composé de **4 applications** + **PostgreSQL** :

| Service | Techno | Rôle | Port local |
|---------|--------|------|-----------|
| **backend** | Spring Boot (Java 21, Gradle) | API REST, métier, sécurité JWT, multi-tenant | 8080 |
| **ai-service** | FastAPI (Python 3.13) | Appels LLM (qualification, chat, extraction fichiers) | 8000 |
| **admin** | Vue 3 + Vite | Interface interne des ESN (dashboard, demandes, admin) | 5173 (dev) |
| **client** | Angular 21 (SSR) | Formulaire vivant public partagé aux prospects | 4200 (dev) |
| **db** | PostgreSQL 16+ | Persistance | 5432 |

**Flux :** `admin`/`client` → `backend` → `ai-service` → fournisseur LLM (OpenRouter).
Le front n'appelle **jamais** `ai-service` directement, toujours via `backend`.

---

## 2. Variables d'environnement (secrets)

Ne **jamais** committer de secrets. Tout passe par variables d'environnement.

### backend
| Variable | Description | Exemple |
|----------|-------------|---------|
| `DB_URL` | URL JDBC PostgreSQL | `jdbc:postgresql://db:5432/smartesn` |
| `DB_USERNAME` | user PostgreSQL | `smartesn` |
| `DB_PASSWORD` | mot de passe DB | *(secret)* |
| `JWT_SECRET` | clé de signature JWT (≥ 32 caractères) | *(secret aléatoire)* |
| `JWT_EXPIRATION_MS` | durée de validité du token | `86400000` |
| `IA_BASE_URL` | URL interne du service IA | `http://ai-service:8000` |
| `FORM_BASE_URL` | URL publique du formulaire client (sert à générer le lien partageable) | `https://form.smartesn.com` |

> Le backend lit aussi un fichier `.env` optionnel (`spring.config.import`). En conteneur,
> préférez les variables d'environnement directes.

### ai-service
| Variable | Description | Exemple |
|----------|-------------|---------|
| `LLM_API_URL` | endpoint de complétion (compatible OpenAI) | `https://openrouter.ai/api/v1/chat/completions` |
| `LLM_API_KEY` | clé API du fournisseur LLM | *(secret)* |
| `LLM_MODEL` | modèle texte | `mistralai/mistral-small-3.2-24b-instruct` |
| `LLM_VISION_MODEL` | modèle multimodal (images/audio) | `openai/gpt-4o-mini` |

### admin (build-time, Vite)
| Variable | Description | Exemple |
|----------|-------------|---------|
| `VITE_API_URL` | URL publique du backend | `https://api.smartesn.com` |

### client (Angular)
- L'URL du backend est dans `client/src/app/core/api.config.ts` (`API_BASE_URL`, défaut
  `http://localhost:8080`). **À remplacer** par l'URL de prod du backend au build
  (ou via un `environment.ts`).

---

## 3. Ajustements OBLIGATOIRES avant la prod

1. **CORS** — Fichier `backend/src/main/java/com/example/backend/security/SecurityConfig.java`,
   méthode `corsConfigurationSource()` : remplacer `http://localhost:4200` et
   `http://localhost:5173` par les **domaines de prod** (ex. `https://app.smartesn.com`,
   `https://form.smartesn.com`).

2. **URL backend côté front** :
   - admin : définir `VITE_API_URL` au build.
   - client : mettre l'URL de prod dans `api.config.ts`.

3. **Base de données** : provisionner un PostgreSQL (managé de préférence). La table est créée
   automatiquement au démarrage (`ddl-auto: update`). Pour une vraie prod, envisager Flyway.

4. **Secret Jira chiffré** (dette connue) : le `jiraApiToken` est stocké en clair en base.
   Acceptable pour une démo ; à chiffrer pour une vraie prod.

---

## 4. Build de chaque service

### backend (produit un JAR exécutable)
```bash
cd backend
./gradlew bootJar
# → build/libs/backend-0.0.1-SNAPSHOT.jar
java -jar build/libs/backend-0.0.1-SNAPSHOT.jar   # nécessite Java 21
```

### ai-service (Python)
```bash
cd ai-service
python -m venv venv
./venv/bin/pip install -r requirements.txt
./venv/bin/uvicorn app.main:app --host 0.0.0.0 --port 8000
```

### admin (build statique)
```bash
cd admin
npm ci
VITE_API_URL=https://api.smartesn.com npm run build
# → dist/  (à servir en statique : Nginx, Netlify, Vercel…)
```

### client (Angular avec SSR)
```bash
cd client
npm ci
npm run build
# SSR : le build produit un serveur Node (dist/client/server/server.mjs)
node dist/client/server/server.mjs
```
> Le client a le SSR activé (`server.ts`). Deux options :
> a) déployer le serveur Node SSR ; b) simplifier en build statique (retirer le SSR) si non requis.

---

## 5. Déploiement conteneurisé (recommandé : Docker Compose)

Créer un `Dockerfile` par service (à générer) puis un `docker-compose.yml` à la racine.

### Exemple `docker-compose.yml` (structure cible)
```yaml
services:
  db:
    image: postgres:16
    environment:
      POSTGRES_DB: smartesn
      POSTGRES_USER: smartesn
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes: [ "pgdata:/var/lib/postgresql/data" ]

  ai-service:
    build: ./ai-service
    environment:
      LLM_API_URL: ${LLM_API_URL}
      LLM_API_KEY: ${LLM_API_KEY}
      LLM_MODEL: ${LLM_MODEL}
      LLM_VISION_MODEL: ${LLM_VISION_MODEL}

  backend:
    build: ./backend
    depends_on: [ db, ai-service ]
    environment:
      DB_URL: jdbc:postgresql://db:5432/smartesn
      DB_USERNAME: smartesn
      DB_PASSWORD: ${DB_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
      IA_BASE_URL: http://ai-service:8000
      FORM_BASE_URL: ${FORM_BASE_URL}
    ports: [ "8080:8080" ]

  admin:
    build:
      context: ./admin
      args:
        VITE_API_URL: ${PUBLIC_API_URL}
    ports: [ "80:80" ]   # servi par Nginx

  client:
    build: ./client
    ports: [ "4200:4200" ]

volumes:
  pgdata:
```

### Dockerfiles à créer (indications)
- **backend** : image `eclipse-temurin:21-jdk`, `./gradlew bootJar`, `java -jar app.jar`.
- **ai-service** : image `python:3.13-slim`, `pip install -r requirements.txt`, `uvicorn ... --host 0.0.0.0`.
- **admin** : multi-stage `node:22` (build) → `nginx` (sert `dist/`).
- **client** : `node:22`, build + lancer le serveur SSR (`node dist/.../server.mjs`).

---

## 6. Vérifications post-déploiement (smoke tests)

```bash
# 1. Backend up
curl https://api.smartesn.com/health            # → "Service is running!"

# 2. Service IA up (interne)
curl http://ai-service:8000/health              # → {"status":"ok"}

# 3. Inscription d'une entreprise
curl -X POST https://api.smartesn.com/auth/register \
  -H "Content-Type: application/json" \
  -d '{"nomEntreprise":"Demo","nomAdmin":"Admin","email":"admin@demo.com","motDePasse":"Secret123"}'
# → { token, email, role: ADMIN }

# 4. Formulaire public accessible
# ouvrir https://form.smartesn.com?entrepriseKey=<token de l'entreprise>
```

Vérifier ensuite dans l'admin : login, dashboard, création demande, qualification IA, chat.

---

## 7. Points d'attention / dette connue

- **Multipart** : le backend accepte les fichiers jusqu'à 15 Mo (`application.yaml`).
- **LLM audio** : l'extraction audio dépend du modèle multimodal ; les images et documents
  fonctionnent de façon fiable.
- **jiraApiToken** stocké en clair (à chiffrer pour une vraie prod).
- **ddl-auto: update** ne gère pas les migrations destructives → prévoir Flyway pour la prod.
- Prévoir **HTTPS** (reverse proxy Nginx/Caddy) devant tous les services publics.
```
