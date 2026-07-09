package com.example.backend.dto;

import com.example.backend.entity.Complexite;
import com.example.backend.entity.StatutDemande;
import com.example.backend.entity.TypeProjet;
import java.time.LocalDateTime;

public record DemandeDetailResponse(
        Long id,
        String description,
        String clientNom,
        String clientEmail,
        StatutDemande statut,
        TypeProjet type,
        Complexite complexite,
        LocalDateTime dateCreation,
        // estimation
        Double budgetMin,
        Double budgetMax,
        Integer delaiSemaines,
        // affectation
        String equipeAffectee
) {}
