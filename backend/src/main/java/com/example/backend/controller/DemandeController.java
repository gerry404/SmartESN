package com.example.backend.controller;
import com.example.backend.entity.*;
import com.example.backend.dto.*;
import com.example.backend.repository.*;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/demandes")
public class DemandeController {

    private final DemandeRepository demandeRepository;
    private final ClientRepository clientRepository;

    public DemandeController(DemandeRepository demandeRepository, ClientRepository clientRepository) {
        this.demandeRepository = demandeRepository;
        this.clientRepository = clientRepository;
    }

    @PostMapping
    public DemandeResponse creer(@Valid @RequestBody DemandeCreateRequest request) {
        // 1. Retrouver le client par son email, ou le créer s'il n'existe pas encore
        Client client = clientRepository.findByEmail(request.email())
                .orElseGet(() -> {
                    Client nouveau = new Client();
                    nouveau.setNom(request.nom());
                    nouveau.setEmail(request.email());
                    nouveau.setTelephone(request.telephone());
                    return clientRepository.save(nouveau);
                });

        // 2. Créer la demande et la lier au client
        Demande demande = new Demande();
        demande.setDescription(request.description());
        demande.setStatut(StatutDemande.NOUVELLE);
        demande.setClient(client);
        Demande saved = demandeRepository.save(demande);

        return toResponse(saved);
    }

    @GetMapping
    public List<DemandeResponse> lister() {
        return demandeRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private DemandeResponse toResponse(Demande d) {
        Client c = d.getClient();
        return new DemandeResponse(
                d.getId(),
                d.getDescription(),
                c != null ? c.getNom() : null,
                c != null ? c.getEmail() : null,
                d.getStatut(),
                d.getType(),
                d.getComplexite(),
                d.getScoreConfiance(),
                d.getDateCreation()
        );
    }
}
