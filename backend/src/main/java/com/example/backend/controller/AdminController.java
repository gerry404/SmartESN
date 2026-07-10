package com.example.backend.controller;

import com.example.backend.dto.*;
import com.example.backend.entity.GrilleReference;
import com.example.backend.entity.Utilisateur;
import com.example.backend.repository.GrilleReferenceRepository;
import com.example.backend.repository.UtilisateurRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Toutes les routes /admin/** sont réservées au rôle ADMIN (voir SecurityConfig). */
@RestController
@RequestMapping("/admin")
public class AdminController {

    private final UtilisateurRepository utilisateurRepository;
    private final GrilleReferenceRepository grilleRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminController(UtilisateurRepository utilisateurRepository,
                           GrilleReferenceRepository grilleRepository,
                           PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.grilleRepository = grilleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ---- Gestion des utilisateurs internes ----
    @GetMapping("/utilisateurs")
    public List<UtilisateurResponse> listerUtilisateurs() {
        return utilisateurRepository.findAll().stream()
                .map(u -> new UtilisateurResponse(u.getId(), u.getNom(), u.getEmail(), u.getRole()))
                .toList();
    }

    @PostMapping("/utilisateurs")
    public ResponseEntity<?> creerUtilisateur(@Valid @RequestBody UtilisateurCreateRequest request) {
        if (utilisateurRepository.findByEmail(request.email()).isPresent()) {
            return ResponseEntity.badRequest().body("Un utilisateur avec cet email existe déjà.");
        }
        Utilisateur u = new Utilisateur();
        u.setNom(request.nom());
        u.setEmail(request.email());
        u.setMotDePasse(passwordEncoder.encode(request.motDePasse())); // haché
        u.setRole(request.role());
        Utilisateur saved = utilisateurRepository.save(u);
        return ResponseEntity.ok(new UtilisateurResponse(saved.getId(), saved.getNom(),
                saved.getEmail(), saved.getRole()));
    }

    @DeleteMapping("/utilisateurs/{id}")
    public ResponseEntity<Void> supprimerUtilisateur(@PathVariable Long id) {
        if (!utilisateurRepository.existsById(id)) return ResponseEntity.notFound().build();
        utilisateurRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Paramétrage de la grille de référence ----
    @GetMapping("/grille")
    public List<GrilleReference> listerGrille() {
        return grilleRepository.findAll();
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
}
