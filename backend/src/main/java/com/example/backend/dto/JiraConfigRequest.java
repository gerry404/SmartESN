package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;

/** Configuration de l'intégration Jira, propre à une entreprise.
 *  apiToken vide = inchangé. projectKey optionnel : choisi après connexion dans la liste. */
public record JiraConfigRequest(
        @NotBlank String baseUrl,      // ex. https://mon-entreprise.atlassian.net
        @NotBlank String email,        // email du compte Jira
        String apiToken,               // jeton d'API Atlassian (vide = inchangé)
        String projectKey              // ex. SMART (optionnel, défini après connexion)
) {}
