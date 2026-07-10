import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  input,
  output,
  signal,
} from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { DemandeApi, type ApiOptions } from '../../data/demande.api';
import type {
  DemandeResponse,
  FieldErrors,
  IntakeResponse,
} from '../../core/demande.models';

type Phase =
  | 'saisie'
  | 'analyse'
  | 'questions'
  | 'coordonnees'
  | 'envoi'
  | 'confirmation'
  | 'erreur';

interface Echange {
  question: string;
  reponse: string;
}

const MAX_ETAPES = 5;

@Component({
  selector: 'app-demande-form',
  imports: [ReactiveFormsModule],
  templateUrl: './demande-form.html',
  styleUrl: './demande-form.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DemandeForm {
  private readonly api = inject(DemandeApi);
  private readonly fb = inject(FormBuilder);

  // --- config d'intégration (optionnelle) ---
  /** Surcharge l'URL de la plateforme (sinon : token API_BASE_URL). */
  readonly apiBaseUrl = input<string>();
  /** Clé publique de l'entreprise → route la demande vers le bon pipe. */
  readonly entrepriseKey = input<string>();
  /** Émis quand la demande est enregistrée (id, statut…). */
  readonly submitted = output<DemandeResponse>();

  private opts(): ApiOptions {
    return { baseUrl: this.apiBaseUrl(), entrepriseKey: this.entrepriseKey() };
  }

  // --- état ---
  readonly phase = signal<Phase>('saisie');
  readonly description = signal('');
  readonly etape = signal(0);
  readonly analyse = signal<IntakeResponse | null>(null);
  readonly historique = signal<Echange[]>([]);
  readonly reponses = signal<string[]>([]);
  readonly result = signal<DemandeResponse | null>(null);
  readonly errorMessage = signal<string | null>(null);
  readonly fieldErrors = signal<FieldErrors>({});

  // --- dérivés ---
  readonly progression = computed(() => {
    const parScore = (this.analyse()?.score_confiance ?? 0) * 100;
    const parEtapes = (this.etape() / MAX_ETAPES) * 100;
    return Math.round(Math.max(parScore, parEtapes));
  });

  // --- formulaires ---
  readonly saisieForm = this.fb.nonNullable.group({
    texte: ['', Validators.required],
  });

  readonly coordForm = this.fb.nonNullable.group({
    nom: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    telephone: [''],
  });

  // ---------- étape 1 : première analyse ----------
  demarrer(): void {
    if (this.saisieForm.invalid) return;
    this.description.set(this.saisieForm.getRawValue().texte.trim());
    this.lancerAnalyse();
  }

  private lancerAnalyse(): void {
    this.phase.set('analyse');
    this.errorMessage.set(null);
    this.api.analyser(this.description(), this.opts()).subscribe({
      next: (res) => this.traiterAnalyse(res),
      error: (e: HttpErrorResponse) => this.gererErreur(e),
    });
  }

  private traiterAnalyse(res: IntakeResponse): void {
    this.analyse.set(res);
    if (res.complet || this.etape() >= MAX_ETAPES) {
      this.phase.set('coordonnees');
      return;
    }
    this.reponses.set(res.questions.map(() => ''));
    this.phase.set('questions');
  }

  majReponse(index: number, valeur: string): void {
    const copie = [...this.reponses()];
    copie[index] = valeur;
    this.reponses.set(copie);
  }

  // ---------- valider les réponses → enrichir + ré-analyser ----------
  envoyerReponses(): void {
    const questions = this.analyse()?.questions ?? [];
    const rep = this.reponses();

    let desc = this.description();
    const nouveaux: Echange[] = [];
    questions.forEach((q, i) => {
      const r = (rep[i] ?? '').trim();
      if (!r) return;
      desc += `\n${q} : ${r}`;
      nouveaux.push({ question: q, reponse: r });
    });

    this.description.set(desc);
    this.historique.update((h) => [...h, ...nouveaux]);
    this.etape.update((n) => n + 1);
    this.lancerAnalyse();
  }

  // ---------- étape finale : soumettre ----------
  soumettre(): void {
    if (this.coordForm.invalid) {
      this.coordForm.markAllAsTouched();
      return;
    }
    this.phase.set('envoi');
    this.fieldErrors.set({});
    const { nom, email, telephone } = this.coordForm.getRawValue();

    this.api
      .soumettre(
        {
          description: this.description(),
          nom,
          email,
          telephone: telephone || undefined,
        },
        this.opts(),
      )
      .subscribe({
        next: (res) => {
          this.result.set(res);
          this.phase.set('confirmation');
          this.submitted.emit(res); // notifie l'hôte (id de la demande, etc.)
        },
        error: (e: HttpErrorResponse) => this.gererErreurSoumission(e),
      });
  }

  // ---------- erreurs ----------
  private gererErreur(e: HttpErrorResponse): void {
    this.errorMessage.set(
      e.status === 502
        ? "Le service d'analyse est momentanément indisponible."
        : 'Une erreur est survenue. Réessayez.',
    );
    this.phase.set('erreur');
  }

  private gererErreurSoumission(e: HttpErrorResponse): void {
    if (e.status === 400 && e.error && typeof e.error === 'object') {
      this.fieldErrors.set(e.error as FieldErrors);
      this.phase.set('coordonnees');
    } else {
      this.errorMessage.set(
        e.status === 502
          ? 'Service momentanément indisponible.'
          : "Échec de l'envoi. Réessayez.",
      );
      this.phase.set('erreur');
    }
  }

  reessayer(): void {
    this.lancerAnalyse();
  }
}
