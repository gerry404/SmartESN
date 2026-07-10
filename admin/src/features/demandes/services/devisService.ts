import { http } from '@/utils/http'
import type { Devis } from '../types'

export function genererDevis(demandeId: number): Promise<Devis> {
  return http<Devis>(`/demandes/${demandeId}/devis`, { method: 'POST' })
}

export function getDevis(demandeId: number): Promise<Devis> {
  return http<Devis>(`/demandes/${demandeId}/devis`) // 404 si aucun
}

export function envoyerDevis(devisId: number): Promise<Devis> {
  return http<Devis>(`/devis/${devisId}/envoyer`, { method: 'POST' })
}
