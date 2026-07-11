# 🤖 Documentation du Service IA (SmartESN)

Ce document explique le **service IA** de SmartESN : son rôle, sa structure, comment le
configurer, le lancer et le tester. Écrit pour être compris pas à pas.

---

## 1. Rôle du service

Le service IA est un microservice **FastAPI** (Python) **séparé** du backend Java. Il expose
des endpoints qui utilisent un **grand modèle de langage (LLM)** pour :

- **qualifier** une demande (type de projet + complexité + score de confiance) ;
- **conduire le formulaire dynamique** (dire si la demande est assez précise, sinon proposer
  des questions ciblées) ;
- **rédiger** un devis préliminaire et une proposition technique.

Le backend Java appellera ce service en HTTP. Les deux communiquent en **JSON**.

> Le service ne fait **pas** l'estimation chiffrée (budget/délai) : celle-ci reste dans le
> backend Java (grille de référence, déterministe). L'IA fournit le *type* et la *complexité*
> qui alimentent cette grille.

---

## 2. Le fournisseur LLM (configurable)

Le service n'est **lié à aucun fournisseur en dur**. Il appelle une **API compatible OpenAI**
(endpoint `/chat/completions`), dont l'adresse, la clé et le modèle sont définis dans le
fichier **`.env`** (non versionné). On peut donc changer de fournisseur ou de modèle **sans
toucher au code**, en modifiant uniquement le `.env`.

Variables attendues (voir `.env.example`) :

| Variable | Rôle |
|----------|------|
| `LLM_API_URL` | URL de l'endpoint de complétion (compatible OpenAI) |
| `LLM_API_KEY` | Clé d'accès à l'API |
| `LLM_MODEL` | Identifiant du modèle à utiliser |

**Fiabilité du JSON** : à chaque appel, on demande explicitement au modèle une sortie au format
JSON (`response_format: json_object`), puis on la valide avec **Pydantic**. Si la réponse n'est
pas un JSON conforme, le service renvoie une erreur claire plutôt que de planter.

---

## 3. Structure du projet

```
ai-service/
├── app/
│   ├── __init__.py
│   ├── config.py       # lit le .env (URL, clé, modèle, timeout, température)
│   ├── llm_client.py   # appelle le fournisseur LLM et renvoie un JSON validé
│   ├── schemas.py      # contrats d'entrée/sortie (Pydantic)
│   ├── prompts.py      # les prompts (regroupés pour être ajustés facilement)
│   └── main.py         # l'application FastAPI et les endpoints
├── .env                # secrets réels (IGNORÉ par git)
├── .env.example        # modèle de configuration (versionné)
├── .gitignore
└── requirements.txt    # dépendances Python
```

**Comment ça s'enchaîne** : `main.py` reçoit la requête → formate un prompt (`prompts.py`) →
appelle `chat_json()` (`llm_client.py`) → valide la réponse avec un schéma (`schemas.py`) →
renvoie le résultat.

---

## 4. Les endpoints

Base URL (en local) : `http://localhost:8000` · Documentation interactive : `/docs`

### `GET /health`
Vérifie que le service tourne. Réponse : `{"status": "ok"}`. (Ne nécessite pas de clé LLM.)

### `POST /classify`
Qualifie une demande.
- **Entrée** : `{ "description": "..." }`
- **Sortie** :
```json
{
  "type": "MOBILE",
  "complexite": "MOYENNE",
  "score_confiance": 0.85,
  "incertitudes": ["Nombre d'utilisateurs non précisé"]
}
```

### `POST /intake` (formulaire dynamique)
Détermine si la demande est assez complète ; sinon propose des questions.
- **Entrée** : `{ "description": "..." }`
- **Sortie** :
```json
{
  "complet": false,
  "score_confiance": 0.4,
  "type_probable": "WEB",
  "questions": ["Souhaitez-vous un paiement en ligne ?", "Combien de pages environ ?"]
}
```

### `POST /draft` (devis + proposition technique)
Rédige le devis et la proposition à partir des infos déjà connues.
- **Entrée** :
```json
{
  "description": "...",
  "type": "WEB",
  "complexite": "MOYENNE",
  "budget_min": 400000,
  "budget_max": 1200000,
  "delai_semaines": 9
}
```
- **Sortie** : `{ "devis": "...", "proposition_technique": "...", "modules": [...], "hypotheses": [...] }`

### Codes de réponse
- `200` : succès
- `422` : entrée invalide (ex. description vide)
- `502` : le fournisseur LLM est injoignable ou a renvoyé une erreur (ex. clé absente/invalide)

---

## 5. Configuration (à faire une fois)

1. Copier le modèle en fichier réel :
   ```powershell
   cd $HOME\Desktop\SmartESN\ai-service
   Copy-Item .env.example .env
   ```
2. Ouvrir `.env` et renseigner `LLM_API_URL`, `LLM_API_KEY`, `LLM_MODEL`.

> Le fichier `.env` est **ignoré par git** : les clés ne partent jamais sur GitHub.

---

## 6. Lancer le service

```powershell
cd $HOME\Desktop\SmartESN\ai-service
.\venv\Scripts\Activate.ps1          # active l'environnement Python
uvicorn app.main:app --reload --port 8000
```

Puis ouvrir **http://localhost:8000/docs** pour tester les endpoints visuellement.

> Si le port 8000 est déjà utilisé, choisir un autre port (`--port 8001`).

---

## 7. Tester rapidement (PowerShell)

```powershell
# health (marche sans clé)
Invoke-RestMethod http://localhost:8000/health

# classify (nécessite une clé valide dans .env)
Invoke-RestMethod -Uri http://localhost:8000/classify -Method Post `
  -ContentType "application/json" `
  -Body '{"description":"une application mobile de vente avec paiement mobile money"}'
```

**Sans clé valide**, `/classify` renvoie une erreur `502` : c'est normal, cela confirme que
l'appel part bien vers le fournisseur ; il ne manque que la clé.

---

## 8. Prochaines étapes

- Brancher le **backend Java** sur ce service : lors de la qualification d'une demande, le
  backend appellera `POST /classify` pour remplir automatiquement le type et la complexité
  (au lieu de la saisie manuelle).
- Ajouter la **détection de risques** et le **RAG** (recherche des projets passés similaires
  pour ancrer l'estimation).
