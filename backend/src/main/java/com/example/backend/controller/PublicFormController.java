package com.example.backend.controller;

import com.example.backend.dto.DemandeCreateRequest;
import com.example.backend.dto.DemandeResponse;
import com.example.backend.dto.IntakeIa;
import com.example.backend.entity.*;
import com.example.backend.repository.ClientRepository;
import com.example.backend.repository.DemandeRepository;
import com.example.backend.repository.EntrepriseRepository;
import com.example.backend.service.IaClient;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Formulaire client PUBLIC, accessible via le lien partagé de l'entreprise.
 * Le jeton {token} dans l'URL identifie l'entreprise destinataire de la demande.
 * Aucune authentification : c'est le client final (prospect) qui remplit.
 */
@RestController
@RequestMapping("/public/{token}")
public class PublicFormController {

    private final EntrepriseRepository entrepriseRepository;
    private final ClientRepository clientRepository;
    private final DemandeRepository demandeRepository;
    private final IaClient iaClient;

    public PublicFormController(EntrepriseRepository entrepriseRepository,
                                ClientRepository clientRepository,
                                DemandeRepository demandeRepository, IaClient iaClient) {
        this.entrepriseRepository = entrepriseRepository;
        this.clientRepository = clientRepository;
        this.demandeRepository = demandeRepository;
        this.iaClient = iaClient;
    }

    public record IntakeReq(@NotBlank String description) {}

    // Formulaire dynamique : renvoie les questions de clarification
    @PostMapping("/intake")
    public ResponseEntity<IntakeIa> intake(@PathVariable String token, @RequestBody IntakeReq req) {
        if (entrepriseRepository.findByFormToken(token).isEmpty()) {
            return ResponseEntity.notFound().build(); // lien invalide
        }
        return ResponseEntity.ok(iaClient.intake(req.description()));
    }

    // Soumission finale : la demande est rattachée à l'entreprise du jeton
    @PostMapping("/demandes")
    public ResponseEntity<?> soumettre(@PathVariable String token,
                                       @Valid @RequestBody DemandeCreateRequest request) {
        Entreprise entreprise = entrepriseRepository.findByFormToken(token).orElse(null);
        if (entreprise == null) {
            return ResponseEntity.status(404).body("Lien de formulaire invalide.");
        }

        Client client = clientRepository.findByEmail(request.email())
                .orElseGet(() -> {
                    Client c = new Client();
                    c.setNom(request.nom());
                    c.setEmail(request.email());
                    c.setTelephone(request.telephone());
                    return clientRepository.save(c);
                });

        Demande demande = new Demande();
        demande.setDescription(request.description());
        demande.setStatut(StatutDemande.NOUVELLE);
        demande.setClient(client);
        demande.setEntreprise(entreprise); // rattachement au bon tenant
        Demande saved = demandeRepository.save(demande);

        return ResponseEntity.ok(new DemandeResponse(
                saved.getId(), saved.getDescription(),
                client.getNom(), client.getEmail(),
                saved.getStatut(), saved.getType(), saved.getComplexite(),
                saved.getScoreConfiance(), saved.getDateCreation()));
    }
}
