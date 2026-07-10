package com.example.backend.controller;

import com.example.backend.entity.*;
import com.example.backend.dto.*;
import com.example.backend.repository.*;
import com.example.backend.service.AffectationService;
import com.example.backend.service.EstimationService;
import com.example.backend.service.IaClient;
import com.example.backend.service.UtilisateurCourantService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/demandes")
public class DemandeController {

    private final DemandeRepository demandeRepository;
    private final ClientRepository clientRepository;
    private final EstimationRepository estimationRepository;
    private final AffectationRepository affectationRepository;
    private final EquipeRepository equipeRepository;
    private final ProjetRepository projetRepository;
    private final EstimationService estimationService;
    private final AffectationService affectationService;
    private final IaClient iaClient;
    private final UtilisateurCourantService utilisateurCourant;

    public DemandeController(DemandeRepository demandeRepository, ClientRepository clientRepository,
                             EstimationRepository estimationRepository, AffectationRepository affectationRepository,
                             EquipeRepository equipeRepository, ProjetRepository projetRepository,
                             EstimationService estimationService, AffectationService affectationService,
                             IaClient iaClient, UtilisateurCourantService utilisateurCourant) {
        this.demandeRepository = demandeRepository;
        this.clientRepository = clientRepository;
        this.estimationRepository = estimationRepository;
        this.affectationRepository = affectationRepository;
        this.equipeRepository = equipeRepository;
        this.projetRepository = projetRepository;
        this.estimationService = estimationService;
        this.affectationService = affectationService;
        this.iaClient = iaClient;
        this.utilisateurCourant = utilisateurCourant;
    }

    // ---- Soumission par le client (public) ----
    @PostMapping
    public DemandeResponse creer(@Valid @RequestBody DemandeCreateRequest request) {
        Client client = clientRepository.findByEmail(request.email())
                .orElseGet(() -> {
                    Client nouveau = new Client();
                    nouveau.setNom(request.nom());
                    nouveau.setEmail(request.email());
                    nouveau.setTelephone(request.telephone());
                    return clientRepository.save(nouveau);
                });

        Demande demande = new Demande();
        demande.setDescription(request.description());
        demande.setStatut(StatutDemande.NOUVELLE);
        demande.setClient(client);
        return toResponse(demandeRepository.save(demande));
    }

    // ---- Liste, avec filtre optionnel par statut (interne) ----
    @GetMapping
    public List<DemandeResponse> lister(@RequestParam(required = false) StatutDemande statut) {
        // ne renvoie que les demandes de l'entreprise de l'utilisateur connecté
        Long entrepriseId = utilisateurCourant.entreprise().getId();
        List<Demande> demandes = (statut == null)
                ? demandeRepository.findByEntrepriseId(entrepriseId)
                : demandeRepository.findByEntrepriseIdAndStatut(entrepriseId, statut);
        return demandes.stream().map(this::toResponse).toList();
    }

    // ---- Détail (interne) ----
    @GetMapping("/{id}")
    public ResponseEntity<DemandeDetailResponse> detail(@PathVariable Long id) {
        Demande demande = chargerDeMonEntreprise(id);
        if (demande == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(toDetail(demande));
    }

    // ---- Qualification MANUELLE (interne) : estime + affecte automatiquement ----
    @PutMapping("/{id}/qualifier")
    public ResponseEntity<DemandeDetailResponse> qualifier(@PathVariable Long id,
                                                           @Valid @RequestBody QualificationRequest request) {
        Demande demande = chargerDeMonEntreprise(id);
        if (demande == null) return ResponseEntity.notFound().build();

        traiterQualification(demande, request.type(), request.complexite(), null);
        return ResponseEntity.ok(toDetail(demande));
    }

    // ---- Qualification AUTOMATIQUE par l'IA (interne) ----
    @PostMapping("/{id}/analyser")
    public ResponseEntity<DemandeDetailResponse> analyser(@PathVariable Long id) {
        Demande demande = chargerDeMonEntreprise(id);
        if (demande == null) return ResponseEntity.notFound().build();

        // L'IA détermine le type et la complexité à partir de la description
        ClassificationIa ia = iaClient.classifier(demande.getDescription());
        traiterQualification(demande, ia.type(), ia.complexite(), ia.scoreConfiance());
        return ResponseEntity.ok(toDetail(demande));
    }

    // Charge une demande UNIQUEMENT si elle appartient à l'entreprise de l'utilisateur connecté.
    private Demande chargerDeMonEntreprise(Long id) {
        Long entrepriseId = utilisateurCourant.entreprise().getId();
        return demandeRepository.findById(id)
                .filter(d -> d.getEntreprise() != null && d.getEntreprise().getId().equals(entrepriseId))
                .orElse(null);
    }

    // Logique commune : applique la qualification, estime, affecte et met à jour le statut.
    private void traiterQualification(Demande demande, TypeProjet type, Complexite complexite,
                                      Double scoreConfiance) {
        demande.setType(type);
        demande.setComplexite(complexite);
        demande.setScoreConfiance(scoreConfiance);
        estimationService.estimer(demande);
        affectationService.affecter(demande);
        demande.setStatut(StatutDemande.QUALIFIEE);
        demandeRepository.save(demande);
    }

    // ---- Suivi de conversion (interne) : changement de statut commercial ----
    @PutMapping("/{id}/statut")
    public ResponseEntity<DemandeDetailResponse> changerStatut(@PathVariable Long id,
                                                               @Valid @RequestBody StatutUpdateRequest request) {
        Demande demande = chargerDeMonEntreprise(id);
        if (demande == null) return ResponseEntity.notFound().build();

        demande.setStatut(request.statut());
        demandeRepository.save(demande);

        // Si la demande est GAGNÉE : on crée le projet et on enregistre le réel (boucle de feedback)
        if (request.statut() == StatutDemande.GAGNEE) {
            Projet projet = new Projet();
            projet.setDemande(demande);
            projet.setTitre("Projet - demande #" + demande.getId());
            projet.setBudgetSigne(request.budgetSigne());
            projet.setDelaiReel(request.delaiReel());
            projetRepository.save(projet);
        }
        return ResponseEntity.ok(toDetail(demande));
    }

    // ---- Réaffectation manuelle à une autre équipe (interne) ----
    @PostMapping("/{id}/reaffecter")
    public ResponseEntity<DemandeDetailResponse> reaffecter(@PathVariable Long id,
                                                            @Valid @RequestBody ReaffecterRequest request) {
        Demande demande = chargerDeMonEntreprise(id);
        if (demande == null) return ResponseEntity.notFound().build();
        Equipe equipe = equipeRepository.findById(request.equipeId()).orElse(null);
        if (equipe == null) return ResponseEntity.badRequest().build();

        Affectation aff = affectationRepository.findByDemandeId(id).orElseGet(Affectation::new);
        aff.setDemande(demande);
        aff.setEquipe(equipe);
        aff.setManuelle(true);
        affectationRepository.save(aff);

        return ResponseEntity.ok(toDetail(demande));
    }

    // ---- Mappers ----
    private DemandeResponse toResponse(Demande d) {
        Client c = d.getClient();
        return new DemandeResponse(
                d.getId(), d.getDescription(),
                c != null ? c.getNom() : null, c != null ? c.getEmail() : null,
                d.getStatut(), d.getType(), d.getComplexite(),
                d.getScoreConfiance(), d.getDateCreation());
    }

    private DemandeDetailResponse toDetail(Demande d) {
        Client c = d.getClient();
        Estimation est = estimationRepository.findByDemandeId(d.getId()).orElse(null);
        Affectation aff = affectationRepository.findByDemandeId(d.getId()).orElse(null);

        return new DemandeDetailResponse(
                d.getId(), d.getDescription(),
                c != null ? c.getNom() : null, c != null ? c.getEmail() : null,
                d.getStatut(), d.getType(), d.getComplexite(), d.getDateCreation(),
                est != null ? est.getBudgetMin() : null,
                est != null ? est.getBudgetMax() : null,
                est != null ? est.getDelaiSemaines() : null,
                aff != null && aff.getEquipe() != null ? aff.getEquipe().getNom() : null);
    }
}
