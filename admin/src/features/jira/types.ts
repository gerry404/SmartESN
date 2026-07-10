// Types de la feature "intégration Jira" (voir docs/guide-jira.md).

// PUT /admin/jira
export interface JiraConfigPayload {
  baseUrl: string
  email: string
  apiToken: string
  projectKey: string
}

// GET /admin/jira → état (jamais le token)
export interface JiraConfigStatus {
  configure: boolean
  baseUrl?: string
  email?: string
  projectKey?: string
}

// Tâche générée par l'IA / poussée dans Jira
export interface Tache {
  id?: number
  titre: string
  description?: string
  assigneSuggere?: string // assigné suggéré par l'IA
  jiraKey?: string // rempli après le push
}
