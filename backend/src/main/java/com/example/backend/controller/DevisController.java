package com.example.backend.controller;

import com.example.backend.dto.DevisResponse;
import com.example.backend.entity.Demande;
import com.example.backend.entity.Devis;
import com.example.backend.entity.StatutDemande;
import com.example.backend.repository.DemandeRepository;
import com.example.backend.repository.DevisRepository;
import com.example.backend.service.DevisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
public class DevisController {

    private final DevisService devisService;
    private final DevisRepository devisRepository;
    private final DemandeRepository demandeRepository;

    public DevisController(DevisService devisService, DevisRepository devisRepository,
                           DemandeRepository demandeRepository) {
        this.devisService = devisService;
        this.devisRepository = devisRepository;
        this.demandeRepository = demandeRepository;
    }

    // Générer un devis à partir d'une demande qualifiée
    @PostMapping("/demandes/{id}/devis")
    public ResponseEntity<DevisResponse> generer(@PathVariable Long id) {
        Demande demande = demandeRepository.findById(id).orElse(null);
        if (demande == null) return ResponseEntity.notFound().build();
        Devis devis = devisService.genererPour(demande);
        return ResponseEntity.ok(toResponse(devis));
    }

    // Consulter le devis d'une demande
    @GetMapping("/demandes/{id}/devis")
    public ResponseEntity<DevisResponse> consulter(@PathVariable Long id) {
        return devisRepository.findByDemandeId(id)
                .map(d -> ResponseEntity.ok(toResponse(d)))
                .orElse(ResponseEntity.notFound().build());
    }

    // Valider et "envoyer" le devis (validation humaine obligatoire avant envoi au client)
    @PostMapping("/devis/{id}/envoyer")
    public ResponseEntity<DevisResponse> envoyer(@PathVariable Long id) {
        Devis devis = devisRepository.findById(id).orElse(null);
        if (devis == null) return ResponseEntity.notFound().build();

        devis.setValide(true);
        devis.setDateEnvoi(LocalDateTime.now());
        devisRepository.save(devis);

        Demande demande = devis.getDemande();
        if (demande != null) {
            demande.setStatut(StatutDemande.DEVIS_ENVOYE);
            demandeRepository.save(demande);
        }
        return ResponseEntity.ok(toResponse(devis));
    }

    private DevisResponse toResponse(Devis d) {
        return new DevisResponse(
                d.getId(),
                d.getDemande() != null ? d.getDemande().getId() : null,
                d.getContenu(), d.getMontant(), d.getValide(),
                d.getDateCreation(), d.getDateEnvoi());
    }
}
