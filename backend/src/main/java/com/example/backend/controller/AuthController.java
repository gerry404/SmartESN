package com.example.backend.controller;

import com.example.backend.entity.*;
import com.example.backend.dto.*;
import com.example.backend.repository.*;
import com.example.backend.service.*;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UtilisateurRepository utilisateurRepository;
    private final EntrepriseRepository entrepriseRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(UtilisateurRepository utilisateurRepository,
                          EntrepriseRepository entrepriseRepository,
                          PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.utilisateurRepository = utilisateurRepository;
        this.entrepriseRepository = entrepriseRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // ---- Profil de l'utilisateur connecté (à partir du token) ----
    @GetMapping("/me")
    public ResponseEntity<?> me() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            return ResponseEntity.status(401).build();
        }
        String email = (String) auth.getPrincipal();
        return utilisateurRepository.findByEmail(email)
                .<ResponseEntity<?>>map(u -> ResponseEntity.ok(
                        Map.of("email", u.getEmail(), "role", u.getRole().name())))
                .orElse(ResponseEntity.status(401).build());
    }

    // ---- Déconnexion (stateless : rien à invalider côté serveur) ----
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent().build();
    }

    // ---- Inscription d'une entreprise : crée l'entreprise + son premier admin ----
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        if (utilisateurRepository.findByEmail(request.email()).isPresent()) {
            return ResponseEntity.status(409).body("Un compte existe déjà avec cet email.");
        }

        // 1. Créer l'entreprise (le « tenant »), avec un jeton public unique pour son formulaire
        Entreprise entreprise = new Entreprise();
        entreprise.setNom(request.nomEntreprise());
        entreprise.setEmail(request.email());
        entreprise.setFormToken(java.util.UUID.randomUUID().toString().replace("-", ""));
        entreprise = entrepriseRepository.save(entreprise);

        // 2. Créer le premier utilisateur, administrateur de cette entreprise
        Utilisateur admin = new Utilisateur();
        admin.setNom(request.nomAdmin());
        admin.setEmail(request.email());
        admin.setMotDePasse(passwordEncoder.encode(request.motDePasse()));
        admin.setRole(RoleUtilisateur.ADMIN);
        admin.setEntreprise(entreprise);
        utilisateurRepository.save(admin);

        // 3. Connexion automatique : on renvoie directement un token
        String token = jwtService.genererToken(admin.getEmail(), admin.getRole().name());
        return ResponseEntity.ok(new LoginResponse(token, admin.getEmail(), admin.getRole().name()));
    }

    // ---- Connexion ----
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Utilisateur user = utilisateurRepository.findByEmail(request.email()).orElse(null);

        if (user == null || !passwordEncoder.matches(request.motDePasse(), user.getMotDePasse())) {
            return ResponseEntity.status(401).body("Identifiants incorrects");
        }
        String token = jwtService.genererToken(user.getEmail(), user.getRole().name());
        return ResponseEntity.ok(new LoginResponse(token, user.getEmail(), user.getRole().name()));
    }
}
