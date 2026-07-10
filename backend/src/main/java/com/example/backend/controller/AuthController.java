package com.example.backend.controller;

import com.example.backend.entity.*;
import com.example.backend.dto.*;
import com.example.backend.repository.*;
import com.example.backend.service.*;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

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
