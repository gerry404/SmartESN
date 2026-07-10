import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./features/demande/demande-form').then((m) => m.DemandeForm),
  },
  { path: '**', redirectTo: '' },
];
