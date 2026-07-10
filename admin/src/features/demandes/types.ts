// Types du domaine "demandes" (voir docs/guide-admin-features.md).

export type TypeProjet = 'MOBILE' | 'WEB' | 'DESKTOP' | 'CONSEIL'
export type Complexite = 'SIMPLE' | 'MOYENNE' | 'COMPLEXE'
export type StatutDemande =
  | 'NOUVELLE'
  | 'EN_ANALYSE'
  | 'QUALIFIEE'
  | 'DEVIS_ENVOYE'
  | 'EN_NEGOCIATION'
  | 'GAGNEE'
  | 'PERDUE'
export type Role = 'COMMERCIAL' | 'DIRECTEUR_TECHNIQUE' | 'ADMIN'

// Valeurs pour les <select>
export const TYPES: TypeProjet[] = ['MOBILE', 'WEB', 'DESKTOP', 'CONSEIL']
export const COMPLEXITES: Complexite[] = ['SIMPLE', 'MOYENNE', 'COMPLEXE']
export const STATUTS: StatutDemande[] = [
  'NOUVELLE',
  'EN_ANALYSE',
  'QUALIFIEE',
  'DEVIS_ENVOYE',
  'EN_NEGOCIATION',
  'GAGNEE',
  'PERDUE',
]

export interface DemandeListItem {
  id: number
  description: string
  clientNom: string
  clientEmail: string
  statut: StatutDemande
  type: TypeProjet | null
  complexite: Complexite | null
  scoreConfiance: number | null
  dateCreation: string
}

export interface DemandeDetail extends DemandeListItem {
  budgetMin?: number
  budgetMax?: number
  delaiSemaines?: number
  equipeAffectee?: string
}

export interface Devis {
  id: number
  demandeId: number
  contenu: string
  montant: number
  valide: boolean
  dateCreation: string
  dateEnvoi: string | null
}

export interface Equipe {
  id: number
  nom: string
  specialite: string
}

export interface Statistiques {
  totalDemandes: number
  nbNouvelles: number
  nbQualifiees: number
  nbDevisEnvoyes: number
  nbGagnees: number
  nbPerdues: number
  tauxConversion: number
  caPotentiel: number
  caSigne: number
}

export type FieldErrors = Record<string, string>
