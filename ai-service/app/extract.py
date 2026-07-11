"""Extraction du contenu d'un fichier uploadé (document, image, audio) vers du texte,
afin d'alimenter l'analyse de la demande."""
import base64
import io

import fitz  # PyMuPDF
from docx import Document

from .llm_client import chat_multimodal


def _extension(nom: str) -> str:
    return nom.rsplit(".", 1)[-1].lower() if "." in nom else ""


def extraire_pdf(data: bytes) -> str:
    texte = []
    with fitz.open(stream=data, filetype="pdf") as doc:
        for page in doc:
            texte.append(page.get_text())
    return "\n".join(texte).strip()


def extraire_docx(data: bytes) -> str:
    doc = Document(io.BytesIO(data))
    return "\n".join(p.text for p in doc.paragraphs if p.text.strip()).strip()


def extraire_txt(data: bytes) -> str:
    for enc in ("utf-8", "latin-1"):
        try:
            return data.decode(enc).strip()
        except UnicodeDecodeError:
            continue
    return ""


async def decrire_image(data: bytes, mime: str) -> str:
    b64 = base64.b64encode(data).decode()
    parts = [{"type": "image_url", "image_url": {"url": f"data:{mime};base64,{b64}"}}]
    instruction = (
        "Ce document accompagne une demande de projet logiciel. Décris précisément son contenu "
        "utile au chiffrage : texte visible (transcris-le), fonctionnalités, écrans, maquettes, "
        "schémas ou exigences. Sois factuel et concis."
    )
    return await chat_multimodal(parts, instruction)


async def transcrire_audio(data: bytes, fmt: str) -> str:
    b64 = base64.b64encode(data).decode()
    parts = [{"type": "input_audio", "input_audio": {"data": b64, "format": fmt}}]
    instruction = (
        "Transcris et résume cet enregistrement audio décrivant un besoin de projet logiciel, "
        "en français, en conservant les informations utiles au chiffrage."
    )
    return await chat_multimodal(parts, instruction)


async def extraire(nom: str, mime: str, data: bytes) -> tuple[str, str]:
    """Renvoie (type_fichier, texte_extrait)."""
    ext = _extension(nom)

    if ext == "pdf" or mime == "application/pdf":
        texte = extraire_pdf(data)
        # PDF scanné (sans texte) → on tente la vision sur la 1re page
        if not texte:
            with fitz.open(stream=data, filetype="pdf") as doc:
                pix = doc[0].get_pixmap(dpi=150)
                texte = await decrire_image(pix.tobytes("png"), "image/png")
        return "document", texte

    if ext in ("docx",) or "word" in mime:
        return "document", extraire_docx(data)

    if ext in ("txt", "md", "csv") or mime.startswith("text/"):
        return "document", extraire_txt(data)

    if ext in ("png", "jpg", "jpeg", "webp", "gif") or mime.startswith("image/"):
        return "image", await decrire_image(data, mime or "image/png")

    if ext in ("mp3", "wav", "m4a", "ogg", "webm") or mime.startswith("audio/"):
        fmt = ext if ext in ("mp3", "wav") else "mp3"
        return "audio", await transcrire_audio(data, fmt)

    # type inconnu : on tente une lecture texte
    return "inconnu", extraire_txt(data)
