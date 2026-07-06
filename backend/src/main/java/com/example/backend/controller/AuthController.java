package com.example.backend.controller;
import com.example.backend.entity.*;
import com.example.backend.dto.*;
import com.example.backend.repository.*;
import com.example.backend.service.*;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    public AuthController(UtilisateurRepository utilisateurRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Utilisateur user = utilisateurRepository.findByEmail(request.email()).orElse(null);

    if (user == null || !passwordEncoder.matches(request.motDePasse(), user.getMotDePasse())) {
        return ResponseEntity.status(401).body("Identifiants incorrects");
    }
    String token = jwtService.genererToken(user.getEmail(),user.getRole().name());
    return ResponseEntity.ok(new LoginResponse(token, user.getEmail(),user.getRole().name()));
}

}