package com.example.backend.dto;

import com.example.backend.entity.StatutTache;
import com.fasterxml.jackson.annotation.JsonProperty;

public record TacheResponse(
        Long id,
        String titre,
        String description,
        StatutTache statut,
        // noms alignés sur le contrat attendu par le front
        @JsonProperty("assigneSuggere") String assigneNom,
        @JsonProperty("jiraKey") String idJira
) {}
