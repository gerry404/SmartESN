package com.example.backend.dto;

import com.example.backend.entity.TypeProjet;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** Réponse du service IA à l'endpoint /intake (conduite du formulaire dynamique). */
public record IntakeIa(
        boolean complet,
        @JsonProperty("score_confiance") Double scoreConfiance,
        @JsonProperty("type_probable") TypeProjet typeProbable,
        List<String> questions
) {}
