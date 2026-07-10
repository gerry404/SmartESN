import { InjectionToken } from '@angular/core';

// URL de base du backend (jamais le service IA directement).
export const API_BASE_URL = new InjectionToken<string>('API_BASE_URL', {
  providedIn: 'root',
  factory: () => 'http://localhost:8080',
});
