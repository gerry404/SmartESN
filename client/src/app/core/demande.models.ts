// Types du contrat d'API (formulaire vivant).

// ----- POST /intake -----
export interface IntakeRequest {
  description: string;
}

export type TypeProbable = 'MOBILE' | 'WEB' | 'DESKTOP' | 'CONSEIL' | null;

export interface IntakeResponse {
  complet: boolean;
  score_confiance: number; // 0..1
  type_probable: TypeProbable;
  questions: string[]; // 0..5
}

// ----- POST /demandes -----
export interface DemandeRequest {
  description: string;
  nom: string;
  email: string;
  telephone?: string;
}

export interface DemandeResponse {
  id: number;
  description: string;
  clientNom: string;
  clientEmail: string;
  statut: string;
  type: string | null;
  complexite: string | null;
  scoreConfiance: number | null;
  dateCreation: string;
}

// Erreur 400 : { champ: message }
export type FieldErrors = Record<string, string>;
