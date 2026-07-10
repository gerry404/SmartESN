import { http } from '@/utils/http'
import type {
  Complexite,
  DemandeDetail,
  DemandeListItem,
  StatutDemande,
  TypeProjet,
} from '../types'

export function listDemandes(statut?: StatutDemande): Promise<DemandeListItem[]> {
  const q = statut ? `?statut=${statut}` : ''
  return http<DemandeListItem[]>(`/demandes${q}`)
}

export function getDemande(id: number): Promise<DemandeDetail> {
  return http<DemandeDetail>(`/demandes/${id}`)
}

// Qualification manuelle → estimation + affectation + statut QUALIFIEE
export function qualifier(
  id: number,
  body: { type: TypeProjet; complexite: Complexite },
): Promise<DemandeDetail> {
  return http<DemandeDetail>(`/demandes/${id}/qualifier`, {
    method: 'PUT',
    body: JSON.stringify(body),
  })
}

// Qualification par l'IA (pas de corps ; 502 si IA indisponible)
export function analyser(id: number): Promise<DemandeDetail> {
  return http<DemandeDetail>(`/demandes/${id}/analyser`, { method: 'POST' })
}

export function changerStatut(
  id: number,
  body: { statut: StatutDemande; budgetSigne?: number; delaiReel?: number },
): Promise<DemandeDetail> {
  return http<DemandeDetail>(`/demandes/${id}/statut`, {
    method: 'PUT',
    body: JSON.stringify(body),
  })
}

export function reaffecter(id: number, equipeId: number): Promise<DemandeDetail> {
  return http<DemandeDetail>(`/demandes/${id}/reaffecter`, {
    method: 'POST',
    body: JSON.stringify({ equipeId }),
  })
}
