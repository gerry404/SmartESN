import {
  ChangeDetectionStrategy,
  Component,
  computed,
  effect,
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

  // Clé entreprise capturée UNE fois et conservée, même si l'URL perd le query param.
  private readonly cleStable = signal<string | undefined>(undefined);
  constructor() {
    // au 1er rendu, mémorise la clé (query param) et ne la reperd plus
    effect(() => {
      const k = this.entrepriseKey();
      if (k && k.trim()) this.cleStable.set(k.trim());
    });
    // secours : lit aussi directement l'URL au démarrage (uniquement côté navigateur, pas SSR)
    if (typeof window !== 'undefined') {
      const fromUrl = new URLSearchParams(window.location.search).get('entrepriseKey');
      if (fromUrl) this.cleStable.set(fromUrl);
    }
  }

  private opts(): ApiOptions {
    return { baseUrl: this.apiBaseUrl(), entrepriseKey: this.cleStable() };
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

  // Le formulaire est obligatoirement rattaché à une entreprise (via le lien partagé).
  readonly lienValide = computed(() => {
    const k = this.cleStable();
    return !!k && k.trim().length > 0;
  });

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

  // --- upload de fichier (document, image, audio) ---
  readonly fichierEnCours = signal(false);
  readonly fichierNom = signal<string | null>(null);
  readonly fichierErreur = signal<string | null>(null);

  onFichier(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    this.fichierEnCours.set(true);
    this.fichierErreur.set(null);
    this.fichierNom.set(file.name);

    this.api.extraireFichier(file, this.opts()).subscribe({
      next: (res) => {
        // le contenu extrait enrichit la zone de description
        const actuel = this.saisieForm.getRawValue().texte.trim();
        const ajout = `\n[Contenu du fichier « ${file.name} » : ${res.texte}]`;
        this.saisieForm.patchValue({ texte: (actuel + ajout).trim() });
        this.fichierEnCours.set(false);
      },
      error: (e: HttpErrorResponse) => {
        this.fichierErreur.set(
          e.status === 502
            ? "L'analyse du fichier est momentanément indisponible."
            : "Impossible de lire ce fichier.",
        );
        this.fichierEnCours.set(false);
        this.fichierNom.set(null);
      },
    });
    input.value = ''; // permet de re-sélectionner le même fichier
  }

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
