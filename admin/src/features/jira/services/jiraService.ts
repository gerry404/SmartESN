import { http } from '@/utils/http'
import type { JiraConfigPayload, JiraConfigStatus } from '../types'

// GET /admin/jira → état de la configuration (jamais le token)
export function getJiraConfig(): Promise<JiraConfigStatus> {
  return http<JiraConfigStatus>('/admin/jira')
}

// PUT /admin/jira → l'admin configure le Jira de son entreprise
export function saveJiraConfig(body: JiraConfigPayload): Promise<JiraConfigStatus> {
  return http<JiraConfigStatus>('/admin/jira', {
    method: 'PUT',
    body: JSON.stringify(body),
  })
}

