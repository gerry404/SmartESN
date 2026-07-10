package com.example.backend.dto;

import com.example.backend.entity.Complexite;
import com.example.backend.entity.TypeProjet;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** Réponse du service IA à l'endpoint /classify. */
public record ClassificationIa(
        TypeProjet type,
        Complexite complexite,
        @JsonProperty("score_confiance") Double scoreConfiance,
        List<String> incertitudes
) {}
