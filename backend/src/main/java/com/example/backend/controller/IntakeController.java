package com.example.backend.controller;

import com.example.backend.dto.IntakeIa;
import com.example.backend.repository.EntrepriseRepository;
import com.example.backend.service.IaClient;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

/**
 * Endpoint public du formulaire dynamique côté client.
 * Le formulaire est OBLIGATOIREMENT rattaché à une entreprise : l'en-tête X-Entreprise-Key
 * (jeton public de l'entreprise) est requis et validé. Sans lui, le formulaire ne fonctionne pas.
 */
@RestController
@RequestMapping("/intake")
public class IntakeController {

    private final IaClient iaClient;
    private final EntrepriseRepository entrepriseRepository;

    public IntakeController(IaClient iaClient, EntrepriseRepository entrepriseRepository) {
        this.iaClient = iaClient;
        this.entrepriseRepository = entrepriseRepository;
    }

    public record IntakeRequest(@NotBlank String description) {}

    @PostMapping
    public ResponseEntity<?> intake(@RequestHeader(value = "X-Entreprise-Key", required = false) String cle,
                                    @RequestBody IntakeRequest request) {
        if (cle == null || cle.isBlank()) {
            return ResponseEntity.badRequest().body("En-tête X-Entreprise-Key manquant.");
        }
        if (entrepriseRepository.findByFormToken(cle).isEmpty()) {
            return ResponseEntity.status(404).body("Lien de formulaire invalide.");
        }
        return ResponseEntity.ok(iaClient.intake(request.description()));
    }

    // Upload d'un fichier (document, image, audio) : l'IA en extrait le contenu utile
    @PostMapping("/fichier")
    public ResponseEntity<?> fichier(@RequestHeader(value = "X-Entreprise-Key", required = false) String cle,
                                     @RequestParam("file") MultipartFile file) {
        if (cle == null || cle.isBlank() || entrepriseRepository.findByFormToken(cle).isEmpty()) {
            return ResponseEntity.status(404).body("Lien de formulaire invalide.");
        }
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("Fichier vide.");
        }
        try {
            return ResponseEntity.ok(
                    iaClient.extract(file.getBytes(), file.getOriginalFilename(), file.getContentType()));
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Lecture du fichier impossible.");
        }
    }
}
