"""Configuration du service IA, chargée depuis le fichier .env (non versionné)."""
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", extra="ignore")

    # --- Fournisseur LLM (API compatible OpenAI) ---
    # Renseignés dans le fichier .env
    llm_api_url: str = "https://api.example.com/v1/chat/completions"
    llm_api_key: str = ""
    llm_model: str = "mistral-small"
    # modèle multimodal (image + audio) pour l'extraction de fichiers
    llm_vision_model: str = "google/gemini-2.0-flash-001"

    # Paramètres de génération
    llm_timeout: float = 60.0
    llm_temperature: float = 0.2


settings = Settings()
