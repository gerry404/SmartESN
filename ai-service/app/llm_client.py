"""Client générique vers un fournisseur LLM exposant une API compatible OpenAI
(endpoint /chat/completions). Le fournisseur exact est défini dans le .env."""
import json
import httpx
from .config import settings


class LLMError(Exception):
    """Erreur lors de l'appel au fournisseur LLM."""


async def chat_json(system_prompt: str, user_prompt: str) -> dict:
    """Envoie un prompt au LLM et renvoie la réponse parsée en dictionnaire JSON.

    On demande explicitement une sortie JSON (response_format) pour fiabiliser le parsing.
    """
    headers = {
        "Authorization": f"Bearer {settings.llm_api_key}",
        "Content-Type": "application/json",
    }
    payload = {
        "model": settings.llm_model,
        "temperature": settings.llm_temperature,
        "response_format": {"type": "json_object"},
        "messages": [
            {"role": "system", "content": system_prompt},
            {"role": "user", "content": user_prompt},
        ],
    }

    try:
        async with httpx.AsyncClient(timeout=settings.llm_timeout) as client:
            resp = await client.post(settings.llm_api_url, headers=headers, json=payload)
            resp.raise_for_status()
            data = resp.json()
    except httpx.HTTPStatusError as e:
        raise LLMError(f"Le fournisseur LLM a renvoyé une erreur {e.response.status_code}") from e
    except httpx.HTTPError as e:
        raise LLMError(f"Impossible de joindre le fournisseur LLM : {e}") from e

    try:
        contenu = data["choices"][0]["message"]["content"]
    except (KeyError, IndexError) as e:
        raise LLMError("Réponse inattendue du fournisseur LLM") from e

    try:
        return json.loads(contenu)
    except json.JSONDecodeError as e:
        raise LLMError(f"Le LLM n'a pas renvoyé un JSON valide : {contenu[:200]}") from e


async def chat_text(messages: list[dict]) -> str:
    """Conversation libre : envoie une liste de messages ({role, content}) au LLM
    et renvoie sa réponse en texte (pas de JSON forcé)."""
    headers = {
        "Authorization": f"Bearer {settings.llm_api_key}",
        "Content-Type": "application/json",
    }
    payload = {
        "model": settings.llm_model,
        "temperature": 0.4,
        "messages": messages,
    }
    try:
        async with httpx.AsyncClient(timeout=settings.llm_timeout) as client:
            resp = await client.post(settings.llm_api_url, headers=headers, json=payload)
            resp.raise_for_status()
            data = resp.json()
    except httpx.HTTPStatusError as e:
        raise LLMError(f"Le fournisseur LLM a renvoyé une erreur {e.response.status_code}") from e
    except httpx.HTTPError as e:
        raise LLMError(f"Impossible de joindre le fournisseur LLM : {e}") from e

    try:
        return data["choices"][0]["message"]["content"]
    except (KeyError, IndexError) as e:
        raise LLMError("Réponse inattendue du fournisseur LLM") from e


async def chat_multimodal(content_parts: list[dict], instruction: str) -> str:
    """Envoie un message multimodal (texte + image/audio) à un modèle vision/audio,
    et renvoie la description/transcription en texte."""
    headers = {
        "Authorization": f"Bearer {settings.llm_api_key}",
        "Content-Type": "application/json",
    }
    message_content = [{"type": "text", "text": instruction}] + content_parts
    payload = {
        "model": settings.llm_vision_model,
        "temperature": 0.2,
        "messages": [{"role": "user", "content": message_content}],
    }
    try:
        async with httpx.AsyncClient(timeout=settings.llm_timeout) as client:
            resp = await client.post(settings.llm_api_url, headers=headers, json=payload)
            resp.raise_for_status()
            data = resp.json()
    except httpx.HTTPStatusError as e:
        raise LLMError(f"Le fournisseur LLM a renvoyé une erreur {e.response.status_code}") from e
    except httpx.HTTPError as e:
        raise LLMError(f"Impossible de joindre le fournisseur LLM : {e}") from e

    try:
        return data["choices"][0]["message"]["content"]
    except (KeyError, IndexError) as e:
        raise LLMError("Réponse inattendue du fournisseur LLM") from e
