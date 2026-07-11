"""Service IA de SmartESN (FastAPI).

Expose des endpoints de qualification, de conduite du formulaire dynamique et de rédaction
de devis. La logique de génération s'appuie sur un fournisseur LLM (API compatible OpenAI),
configuré via le fichier .env.
"""
from fastapi import FastAPI, HTTPException, UploadFile, File
from fastapi.middleware.cors import CORSMiddleware

from . import prompts
from . import extract as extraction
from .llm_client import chat_json, chat_text, LLMError
from .schemas import (
    DemandeIn, ClassificationOut, IntakeOut,
    DraftIn, DraftOut, DecomposeIn, DecomposeOut,
    ChatIn, ChatOut, HealthOut,
)

app = FastAPI(title="SmartESN - Service IA", version="1.0")

# Autorise le backend et le frontend à appeler ce service
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)


def _erreur_llm(e: LLMError) -> HTTPException:
    # 502 : le service dépend d'un fournisseur externe momentanément indisponible/invalide
    return HTTPException(status_code=502, detail=str(e))


@app.get("/health", response_model=HealthOut)
def health():
    return HealthOut()


@app.post("/classify", response_model=ClassificationOut)
async def classify(demande: DemandeIn):
    """Détermine le type de projet, la complexité et un score de confiance."""
    user = prompts.CLASSIFY_USER.format(description=demande.description)
    try:
        data = await chat_json(prompts.CLASSIFY_SYSTEM, user)
        return ClassificationOut(**data)
    except LLMError as e:
        raise _erreur_llm(e)


@app.post("/intake", response_model=IntakeOut)
async def intake(demande: DemandeIn):
    """Évalue si la demande est assez complète ; sinon propose des questions ciblées."""
    user = prompts.INTAKE_USER.format(description=demande.description)
    try:
        data = await chat_json(prompts.INTAKE_SYSTEM, user)
        return IntakeOut(**data)
    except LLMError as e:
        raise _erreur_llm(e)


@app.post("/draft", response_model=DraftOut)
async def draft(req: DraftIn):
    """Rédige un devis préliminaire et une proposition technique."""
    user = prompts.DRAFT_USER.format(
        description=req.description, type=req.type, complexite=req.complexite,
        budget_min=req.budget_min, budget_max=req.budget_max,
        delai_semaines=req.delai_semaines,
    )
    try:
        data = await chat_json(prompts.DRAFT_SYSTEM, user)
        return DraftOut(**data)
    except LLMError as e:
        raise _erreur_llm(e)


@app.post("/decompose", response_model=DecomposeOut)
async def decompose(req: DecomposeIn):
    """Découpe le projet en tâches et suggère un responsable pour chacune."""
    membres = "\n".join(f"- {m.nom} ({m.role})" for m in req.membres) or "- (aucun membre fourni)"
    user = prompts.DECOMPOSE_USER.format(
        description=req.description, type=req.type,
        complexite=req.complexite, membres=membres,
    )
    try:
        data = await chat_json(prompts.DECOMPOSE_SYSTEM, user)
        return DecomposeOut(**data)
    except LLMError as e:
        raise _erreur_llm(e)


@app.post("/extract")
async def extract(file: UploadFile = File(...)):
    """Extrait le contenu d'un fichier (document, image, audio) en texte exploitable."""
    data = await file.read()
    if not data:
        raise HTTPException(status_code=400, detail="Fichier vide.")
    if len(data) > 15 * 1024 * 1024:  # 15 Mo
        raise HTTPException(status_code=413, detail="Fichier trop volumineux (max 15 Mo).")
    try:
        type_fichier, texte = await extraction.extraire(
            file.filename or "fichier", file.content_type or "", data)
        return {"type_fichier": type_fichier, "texte": texte}
    except LLMError as e:
        raise _erreur_llm(e)
    except Exception as e:
        raise HTTPException(status_code=422, detail=f"Extraction impossible : {e}")


@app.post("/chat", response_model=ChatOut)
async def chat(req: ChatIn):
    """Assistant conversationnel : répond en tenant compte de tout l'historique fourni
    et, si fourni, des statistiques de l'entreprise."""
    system = prompts.CHAT_SYSTEM
    if req.contexte:
        system += "\n\nDonnées actuelles de l'entreprise (utilise-les pour répondre) :\n" + req.contexte
    messages = [{"role": "system", "content": system}]
    messages += [{"role": m.role, "content": m.content} for m in req.messages]
    try:
        reply = await chat_text(messages)
        return ChatOut(reply=reply)
    except LLMError as e:
        raise _erreur_llm(e)
