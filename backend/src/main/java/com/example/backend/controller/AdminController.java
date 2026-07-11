package com.example.backend.controller;

import com.example.backend.dto.*;
import com.example.backend.entity.Entreprise;
import com.example.backend.entity.GrilleReference;
import com.example.backend.entity.RoleUtilisateur;
import com.example.backend.entity.Utilisateur;
import com.example.backend.repository.EntrepriseRepository;
import com.example.backend.repository.GrilleReferenceRepository;
import com.example.backend.repository.UtilisateurRepository;
import com.example.backend.service.UtilisateurCourantService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** Toutes les routes /admin/** sont réservées au rôle ADMIN (voir SecurityConfig). */
@RestController
@RequestMapping("/admin")
public class AdminController {

    private final UtilisateurRepository utilisateurRepository;
    private final GrilleReferenceRepository grilleRepository;
    private final EntrepriseRepository entrepriseRepository;
    private final PasswordEncoder passwordEncoder;
    private final UtilisateurCourantService utilisateurCourant;
    private final com.example.backend.service.JiraClient jiraClient;

    @org.springframework.beans.factory.annotation.Value("${app.form-base-url}")
    private String formBaseUrl;

    public AdminController(UtilisateurRepository utilisateurRepository,
                           GrilleReferenceRepository grilleRepository,
                           EntrepriseRepository entrepriseRepository,
                           PasswordEncoder passwordEncoder,
                           UtilisateurCourantService utilisateurCourant,
                           com.example.backend.service.JiraClient jiraClient) {
        this.utilisateurRepository = utilisateurRepository;
        this.grilleRepository = grilleRepository;
        this.entrepriseRepository = entrepriseRepository;
        this.passwordEncoder = passwordEncoder;
        this.utilisateurCourant = utilisateurCourant;
        this.jiraClient = jiraClient;
    }

    // ---- Lien public du formulaire client (à partager avec les prospects) ----
    @GetMapping("/lien-formulaire")
    public Map<String, String> lienFormulaire() {
        Entreprise e = utilisateurCourant.entreprise();
        String token = e.getFormToken() == null ? "" : e.getFormToken();
        return Map.of(
                "token", token,
                "lien", formBaseUrl + "?entrepriseKey=" + token
        );
    }

    // ---- Configuration de l'intégration Jira (propre à l'entreprise de l'admin) ----
    @PutMapping("/jira")
    public Map<String, Object> configurerJira(@Valid @RequestBody JiraConfigRequest request) {
        Entreprise entreprise = utilisateurCourant.entreprise();
        entreprise.setJiraBaseUrl(request.baseUrl());
        entreprise.setJiraEmail(request.email());
        // token conservé s'il n'est pas re-fourni (ex. simple changement de projet)
        if (request.apiToken() != null && !request.apiToken().isBlank()) {
            entreprise.setJiraApiToken(request.apiToken());
        }
        entreprise.setJiraProjectKey(request.projectKey());
        entrepriseRepository.save(entreprise);
        return etatJiraMap(entreprise); // renvoie l'état à jour (comme attendu par le front)
    }

    @GetMapping("/jira")
    public Map<String, Object> etatJira() {
        return etatJiraMap(utilisateurCourant.entreprise());
    }

    // Liste les projets du Jira connecté (le chef peut en avoir plusieurs)
    @GetMapping("/jira/projets")
    public ResponseEntity<?> projetsJira() {
        Entreprise e = utilisateurCourant.entreprise();
        if (!e.jiraConnecte()) {
            return ResponseEntity.status(409)
                    .body(Map.of("erreur", "Connectez d'abord votre Jira (URL + jeton)."));
        }
        try {
            return ResponseEntity.ok(jiraClient.listerProjets(e));
        } catch (Exception ex) {
            return ResponseEntity.status(502)
                    .body(Map.of("erreur", "Connexion refusée par Jira. Vérifiez l'URL, l'email et le jeton."));
        }
    }

    // État de config Jira sans jamais exposer le token
    private Map<String, Object> etatJiraMap(Entreprise e) {
        return Map.of(
                "configure", e.jiraConfigure(),
                "baseUrl", e.getJiraBaseUrl() == null ? "" : e.getJiraBaseUrl(),
                "email", e.getJiraEmail() == null ? "" : e.getJiraEmail(),
                "projectKey", e.getJiraProjectKey() == null ? "" : e.getJiraProjectKey()
        );
    }

    // ---- Gestion des utilisateurs internes (cloisonnée par entreprise) ----

    // Liste UNIQUEMENT les utilisateurs de l'entreprise de l'admin connecté
    @GetMapping("/utilisateurs")
    public List<UtilisateurResponse> listerUtilisateurs() {
        Entreprise entreprise = utilisateurCourant.entreprise();
        return utilisateurRepository.findByEntrepriseId(entreprise.getId()).stream()
                .map(u -> new UtilisateurResponse(u.getId(), u.getNom(), u.getEmail(), u.getRole()))
                .toList();
    }

    @PostMapping("/utilisateurs")
    public ResponseEntity<?> creerUtilisateur(@Valid @RequestBody UtilisateurCreateRequest request) {
        if (utilisateurRepository.findByEmail(request.email()).isPresent()) {
            return ResponseEntity.badRequest().body("Un utilisateur avec cet email existe déjà.");
        }
        // Garde-fou : l'admin ne crée que des COMMERCIAL ou DIRECTEUR_TECHNIQUE (pas d'autre ADMIN)
        if (request.role() == RoleUtilisateur.ADMIN) {
            return ResponseEntity.badRequest()
                    .body("Impossible de créer un second administrateur.");
        }
        Entreprise entreprise = utilisateurCourant.entreprise();

        Utilisateur u = new Utilisateur();
        u.setNom(request.nom());
        u.setEmail(request.email());
        u.setMotDePasse(passwordEncoder.encode(request.motDePasse())); // haché
        u.setRole(request.role());
        u.setEntreprise(entreprise); // rattachement au tenant de l'admin
        Utilisateur saved = utilisateurRepository.save(u);
        return ResponseEntity.ok(new UtilisateurResponse(saved.getId(), saved.getNom(),
                saved.getEmail(), saved.getRole()));
    }

    @DeleteMapping("/utilisateurs/{id}")
    public ResponseEntity<?> supprimerUtilisateur(@PathVariable Long id) {
        Entreprise entreprise = utilisateurCourant.entreprise();
        Utilisateur cible = utilisateurRepository.findById(id).orElse(null);
        if (cible == null) return ResponseEntity.notFound().build();
        // on ne supprime que dans sa propre entreprise
        if (cible.getEntreprise() == null || !cible.getEntreprise().getId().equals(entreprise.getId())) {
            return ResponseEntity.status(403).body("Utilisateur hors de votre entreprise.");
        }
        // un admin ne peut pas se supprimer lui-même
        if (cible.getId().equals(utilisateurCourant.utilisateur().getId())) {
            return ResponseEntity.badRequest().body("Vous ne pouvez pas supprimer votre propre compte.");
        }
        utilisateurRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Paramétrage de la grille de référence (CRUD) ----
    @GetMapping("/grille")
    public List<GrilleReference> listerGrille() {
        return grilleRepository.findAll();
    }

    @PostMapping("/grille")
    public ResponseEntity<?> creerGrille(@Valid @RequestBody GrilleCreateRequest request) {
        // évite les doublons (type + complexité) : une seule ligne par couple
        if (grilleRepository.findByTypeAndComplexite(request.type(), request.complexite()).isPresent()) {
            return ResponseEntity.badRequest()
                    .body("Une référence existe déjà pour ce type et cette complexité.");
        }
        GrilleReference g = new GrilleReference();
        g.setType(request.type());
        g.setComplexite(request.complexite());
        g.setBudgetMin(request.budgetMin());
        g.setBudgetMax(request.budgetMax());
        g.setDelaiMin(request.delaiMin());
        g.setDelaiMax(request.delaiMax());
        return ResponseEntity.ok(grilleRepository.save(g));
    }

    @PutMapping("/grille/{id}")
    public ResponseEntity<GrilleReference> modifierGrille(@PathVariable Long id,
                                                          @Valid @RequestBody GrilleUpdateRequest request) {
        GrilleReference g = grilleRepository.findById(id).orElse(null);
        if (g == null) return ResponseEntity.notFound().build();
        g.setBudgetMin(request.budgetMin());
        g.setBudgetMax(request.budgetMax());
        g.setDelaiMin(request.delaiMin());
        g.setDelaiMax(request.delaiMax());
        return ResponseEntity.ok(grilleRepository.save(g));
    }

    @DeleteMapping("/grille/{id}")
    public ResponseEntity<Void> supprimerGrille(@PathVariable Long id) {
        if (!grilleRepository.existsById(id)) return ResponseEntity.notFound().build();
        grilleRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
