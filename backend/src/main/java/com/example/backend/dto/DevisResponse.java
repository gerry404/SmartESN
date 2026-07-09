package com.example.backend.dto;

import java.time.LocalDateTime;

public record DevisResponse(
        Long id,
        Long demandeId,
        String contenu,
        Double montant,
        Boolean valide,
        LocalDateTime dateCreation,
        LocalDateTime dateEnvoi
) {}
