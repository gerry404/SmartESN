package com.example.backend.controller;

import com.example.backend.dto.DecompositionIa;
import com.example.backend.dto.TacheResponse;
import com.example.backend.entity.*;
import com.example.backend.repository.*;
import com.example.backend.service.IaClient;
import com.example.backend.service.JiraClient;
import com.example.backend.service.UtilisateurCourantService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Intégration Jira : à partir d'une demande GAGNÉE (donc rattachée à un projet),
 * l'IA découpe le projet en tâches, puis on les pousse dans Jira.
 */
@RestController
public class JiraController {

    private final DemandeRepository demandeRepository;
    private final ProjetRepository projetRepository;
    private final AffectationRepository affectationRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final TacheRepository tacheRepository;
    private final IaClient iaClient;
    private final JiraClient jiraClient;
    private final UtilisateurCourantService utilisateurCourant;

    public JiraController(DemandeRepository demandeRepository, ProjetRepository projetRepository,
                          AffectationRepository affectationRepository,
                          UtilisateurRepository utilisateurRepository, TacheRepository tacheRepository,
                          IaClient iaClient, JiraClient jiraClient,
                          UtilisateurCourantService utilisateurCourant) {
        this.demandeRepository = demandeRepository;
        this.projetRepository = projetRepository;
        this.affectationRepository = affectationRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.tacheRepository = tacheRepository;
        this.iaClient = iaClient;
        this.jiraClient = jiraClient;
        this.utilisateurCourant = utilisateurCourant;
    }

    // 1) Décomposer le projet en tâches (IA) et les enregistrer (aperçu, pas encore dans Jira)
    @PostMapping("/projets/{projetId}/decomposer")
    public ResponseEntity<List<TacheResponse>> decomposer(@PathVariable Long projetId) {
        Projet projet = chargerProjetDeMonEntreprise(projetId);
        if (projet == null) return ResponseEntity.notFound().build();
        Demande demande = projet.getDemande();

        // membres de l'équipe affectée (pour suggérer les assignés)
        List<Map<String, String>> membres = List.of();
        List<Utilisateur> membresEquipe = List.of();
        var aff = affectationRepository.findByDemandeId(demande.getId()).orElse(null);
        if (aff != null && aff.getEquipe() != null) {
            membresEquipe = utilisateurRepository.findByEquipeId(aff.getEquipe().getId());
            membres = membresEquipe.stream()
                    .map(u -> Map.of("nom", u.getNom(), "role", u.getRole().name()))
                    .toList();
        }

        DecompositionIa decomposition = iaClient.decompose(
                demande.getDescription(), demande.getType(), demande.getComplexite(), membres);

        // enregistrer les tâches proposées
        List<Utilisateur> finalMembres = membresEquipe;
        List<Tache> taches = decomposition.taches().stream().map(t -> {
            Tache tache = new Tache();
            tache.setProjet(projet);
            tache.setTitre(t.titre());
            tache.setDescription(t.description());
            tache.setStatut(StatutTache.A_FAIRE);
            // relier l'assigné suggéré à un utilisateur réel s'il existe
            finalMembres.stream()
                    .filter(u -> u.getNom().equalsIgnoreCase(t.assigneeSuggere()))
                    .findFirst()
                    .ifPresent(tache::setAssignee);
            return tacheRepository.save(tache);
        }).toList();

        return ResponseEntity.ok(taches.stream().map(this::toResponse).toList());
    }

    // 2) Consulter les tâches d'un projet
    @GetMapping("/projets/{projetId}/taches")
    public ResponseEntity<List<TacheResponse>> taches(@PathVariable Long projetId) {
        Projet projet = chargerProjetDeMonEntreprise(projetId);
        if (projet == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(
                tacheRepository.findByProjetId(projetId).stream().map(this::toResponse).toList());
    }

    // 3) Pousser les tâches du projet dans le Jira DE L'ENTREPRISE de l'utilisateur connecté
    @PostMapping("/projets/{projetId}/jira/push")
    public ResponseEntity<?> push(@PathVariable Long projetId) {
        Entreprise entreprise = utilisateurCourant.entreprise();
        Projet projet = chargerProjetDeMonEntreprise(projetId);
        if (projet == null) return ResponseEntity.notFound().build();
        if (!entreprise.jiraConfigure()) {
            return ResponseEntity.status(503)
                    .body(Map.of("erreur", "L'intégration Jira n'est pas configurée pour votre entreprise."));
        }

        List<Tache> taches = tacheRepository.findByProjetId(projetId);
        for (Tache tache : taches) {
            if (tache.getIdJira() == null) { // ne pas recréer une tâche déjà poussée
                String cle = jiraClient.creerTache(entreprise, tache.getTitre(), tache.getDescription());
                tache.setIdJira(cle);
                tacheRepository.save(tache);
                if (projet.getCleJira() == null) {
                    projet.setCleJira(cle); // mémorise la 1re clé comme référence
                }
            }
        }
        projetRepository.save(projet);
        return ResponseEntity.ok(taches.stream().map(this::toResponse).toList());
    }

    // Charge un projet UNIQUEMENT si sa demande appartient à l'entreprise de l'utilisateur.
    private Projet chargerProjetDeMonEntreprise(Long projetId) {
        Long entrepriseId = utilisateurCourant.entreprise().getId();
        return projetRepository.findById(projetId)
                .filter(p -> p.getDemande() != null && p.getDemande().getEntreprise() != null
                        && p.getDemande().getEntreprise().getId().equals(entrepriseId))
                .orElse(null);
    }

    private TacheResponse toResponse(Tache t) {
        return new TacheResponse(
                t.getId(), t.getTitre(), t.getDescription(), t.getStatut(),
                t.getAssignee() != null ? t.getAssignee().getNom() : null,
                t.getIdJira());
    }
}
