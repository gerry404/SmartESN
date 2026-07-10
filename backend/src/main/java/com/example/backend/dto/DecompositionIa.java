package com.example.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** Réponse du service IA à /decompose (découpage du projet en tâches). */
public record DecompositionIa(List<TacheProposeeIa> taches) {

    public record TacheProposeeIa(
            String titre,
            String description,
            @JsonProperty("assignee_suggere") String assigneeSuggere
    ) {}
}
