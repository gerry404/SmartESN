package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;

/** Configuration de l'intégration Jira, propre à une entreprise. */
public record JiraConfigRequest(
        @NotBlank String baseUrl,      // ex. https://mon-entreprise.atlassian.net
        @NotBlank String email,        // email du compte Jira
        @NotBlank String apiToken,     // jeton d'API Atlassian
        @NotBlank String projectKey    // ex. SMART
) {}
