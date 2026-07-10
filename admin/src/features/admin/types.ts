import type { Complexite, Role, TypeProjet } from '@/features/demandes/types'

export interface Utilisateur {
  id: number
  nom: string
  email: string
  role: Role
}

export interface CreateUtilisateur {
  nom: string
  email: string
  motDePasse: string
  role: Role
}

export interface GrilleLigne {
  id: number
  type: TypeProjet
  complexite: Complexite
  budgetMin: number
  budgetMax: number
  delaiMin: number
  delaiMax: number
}
