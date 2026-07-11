"""Contrats de données (entrées/sorties) du service IA."""
from typing import Literal
from pydantic import BaseModel, Field

TypeProjet = Literal["MOBILE", "WEB", "DESKTOP", "CONSEIL"]
Complexite = Literal["SIMPLE", "MOYENNE", "COMPLEXE"]


# ---------- Entrée commune ----------
class DemandeIn(BaseModel):
    description: str = Field(..., min_length=1, description="Description libre du projet")


# ---------- /classify ----------
class ClassificationOut(BaseModel):
    type: TypeProjet
    complexite: Complexite
    score_confiance: float = Field(..., ge=0, le=1)
    incertitudes: list[str] = Field(default_factory=list)


# ---------- /intake (formulaire dynamique) ----------
class IntakeOut(BaseModel):
    # True si la demande est assez complète pour être chiffrée
    complet: bool
    score_confiance: float = Field(..., ge=0, le=1)
    type_probable: TypeProjet | None = None
    # questions de clarification à poser si la demande est trop floue (0 si complet)
    questions: list[str] = Field(default_factory=list)


# ---------- /draft (devis + proposition technique) ----------
class DraftIn(BaseModel):
    description: str
    type: TypeProjet
    complexite: Complexite
    budget_min: float
    budget_max: float
    delai_semaines: int


class DraftOut(BaseModel):
    devis: str
    proposition_technique: str
    modules: list[str] = Field(default_factory=list)
    hypotheses: list[str] = Field(default_factory=list)


# ---------- /decompose (découpage du projet en tâches) ----------
class MembreIn(BaseModel):
    nom: str
    role: str


class DecomposeIn(BaseModel):
    description: str
    type: TypeProjet
    complexite: Complexite
    membres: list[MembreIn] = Field(default_factory=list)


class TacheProposee(BaseModel):
    titre: str
    description: str = ""
    assignee_suggere: str | None = None  # nom d'un membre, ou null


class DecomposeOut(BaseModel):
    taches: list[TacheProposee] = Field(default_factory=list)


# ---------- /chat (assistant conversationnel) ----------
class ChatMessage(BaseModel):
    role: Literal["user", "assistant"]
    content: str


class ChatIn(BaseModel):
    messages: list[ChatMessage] = Field(default_factory=list)
    # contexte optionnel (ex. statistiques de l'entreprise) injecté dans le prompt système
    contexte: str | None = None


class ChatOut(BaseModel):
    reply: str


class HealthOut(BaseModel):
    status: str = "ok"
