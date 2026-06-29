# SmartESN
Qualification et chiffrage assistés par IA des demandes clients pour une ESN . LLM, devis automatique, intégration Jira.

Monorepo de SmartESN. Architecture distribuée :
 
 - client/  application interne (dashboard, devis, reporting) en Vue;
 - admin/  application client (formulaire public) en Angular
 - backend/  API REST métier et sécurité en Spring Boot
 - ai-service/  service IA (LLM, RAG, estimation) en FastAPI
 - infra/  orchestration Docker Compose (PostgreSQL+pgvector, Redis)
