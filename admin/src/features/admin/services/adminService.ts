import { http } from '@/utils/http'
import type { CreateUtilisateur, GrilleLigne, Utilisateur } from '../types'

export interface LienFormulaire {
  token: string
  lien: string
}

/** Récupère le lien partageable du formulaire client (propre à l'entreprise). */
export function getLienFormulaire(): Promise<LienFormulaire> {
  return http<LienFormulaire>('/admin/lien-formulaire')
}

export function listUtilisateurs(): Promise<Utilisateur[]> {
  return http<Utilisateur[]>('/admin/utilisateurs')
}

export function creerUtilisateur(body: CreateUtilisateur): Promise<Utilisateur> {
  return http<Utilisateur>('/admin/utilisateurs', {
    method: 'POST',
    body: JSON.stringify(body),
  })
}

export function supprimerUtilisateur(id: number): Promise<void> {
  return http<void>(`/admin/utilisateurs/${id}`, { method: 'DELETE' })
}

export function listGrille(): Promise<GrilleLigne[]> {
  return http<GrilleLigne[]>('/admin/grille')
}

export function majGrilleLigne(
  id: number,
  body: Pick<GrilleLigne, 'budgetMin' | 'budgetMax' | 'delaiMin' | 'delaiMax'>,
): Promise<GrilleLigne> {
  return http<GrilleLigne>(`/admin/grille/${id}`, {
    method: 'PUT',
    body: JSON.stringify(body),
  })
}

export function creerGrilleLigne(
  body: Omit<GrilleLigne, 'id'>,
): Promise<GrilleLigne> {
  return http<GrilleLigne>('/admin/grille', {
    method: 'POST',
    body: JSON.stringify(body),
  })
}

export function supprimerGrilleLigne(id: number): Promise<void> {
  return http<void>(`/admin/grille/${id}`, { method: 'DELETE' })
}
