package com.example.backend.dto;

import com.example.backend.entity.StatutDemande;
import jakarta.validation.constraints.NotNull;

/** Change le statut commercial d'une demande.
 *  budgetSigne et delaiReel ne sont utiles que si statut = GAGNEE. */
public record StatutUpdateRequest(
        @NotNull(message = "Le statut est obligatoire")
        StatutDemande statut,
        Double budgetSigne,
        Integer delaiReel
) {}
