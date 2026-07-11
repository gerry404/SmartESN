import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../core/api.config';
import type {
  DemandeRequest,
  DemandeResponse,
  IntakeRequest,
  IntakeResponse,
} from '../core/demande.models';

/**
 * Options d'intégration : permettent de pointer le widget vers une plateforme
 * précise et d'identifier l'entreprise destinataire (multi-tenant).
 */
export interface ApiOptions {
  /** Surcharge l'URL de base (sinon : valeur du token API_BASE_URL). */
  baseUrl?: string;
  /** Clé publique de l'entreprise → route la demande vers le bon pipe. */
  entrepriseKey?: string;
}

@Injectable({ providedIn: 'root' })
export class DemandeApi {
  private readonly http = inject(HttpClient);
  private readonly base = inject(API_BASE_URL);

  /** Analyse la description → complet ? + questions. */
  analyser(description: string, opts: ApiOptions = {}): Observable<IntakeResponse> {
    const body: IntakeRequest = { description };
    return this.http.post<IntakeResponse>(`${opts.baseUrl ?? this.base}/intake`, body, {
      headers: this.headers(opts),
    });
  }

  /** Soumission finale (une seule fois). */
  soumettre(payload: DemandeRequest, opts: ApiOptions = {}): Observable<DemandeResponse> {
    return this.http.post<DemandeResponse>(`${opts.baseUrl ?? this.base}/demandes`, payload, {
      headers: this.headers(opts),
    });
  }

  /** Envoie un fichier (document, image, audio) : l'IA en extrait le contenu utile. */
  extraireFichier(file: File, opts: ApiOptions = {}): Observable<{ type_fichier: string; texte: string }> {
    const data = new FormData();
    data.append('file', file);
    // pas de Content-Type manuel : le navigateur pose le multipart/form-data avec la boundary
    return this.http.post<{ type_fichier: string; texte: string }>(
      `${opts.baseUrl ?? this.base}/intake/fichier`,
      data,
      { headers: this.headers(opts) },
    );
  }

  // Ajoute la clé entreprise en en-tête si fournie (adaptez au contrat backend).
  private headers(opts: ApiOptions): Record<string, string> {
    return opts.entrepriseKey ? { 'X-Entreprise-Key': opts.entrepriseKey } : {};
  }
}
