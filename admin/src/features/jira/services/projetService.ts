import { http } from '@/utils/http'
import type { Tache } from '../types'

// POST /projets/{id}/decomposer → l'IA génère les tâches (aperçu, enregistrées)
export function decomposer(projetId: number): Promise<Tache[]> {
  return http<Tache[]>(`/projets/${projetId}/decomposer`, { method: 'POST' })
}

// GET /projets/{id}/taches → consulter les tâches proposées
export function getTaches(projetId: number): Promise<Tache[]> {
  return http<Tache[]>(`/projets/${projetId}/taches`)
}

// POST /projets/{id}/jira/push → crée les tâches dans le Jira de l'entreprise
export function pushJira(projetId: number): Promise<Tache[]> {
  return http<Tache[]>(`/projets/${projetId}/jira/push`, { method: 'POST' })
}

