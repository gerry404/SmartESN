package com.example.backend.service;

import com.example.backend.entity.Entreprise;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Client vers l'API REST de Jira Cloud.
 * En SaaS, chaque entreprise a SON propre Jira : la configuration (URL, email, token, projet)
 * est donc lue sur l'entité Entreprise passée en paramètre, et non dans un .env global.
 */
@Service
public class JiraClient {

    /** Construit un RestClient authentifié pour le Jira d'une entreprise donnée. */
    private RestClient clientPour(Entreprise e) {
        String auth = Base64.getEncoder().encodeToString(
                (e.getJiraEmail() + ":" + e.getJiraApiToken()).getBytes(StandardCharsets.UTF_8));
        return RestClient.builder()
                .baseUrl(e.getJiraBaseUrl())
                .defaultHeader("Authorization", "Basic " + auth)
                .defaultHeader("Accept", "application/json")
                .build();
    }

    /**
     * Crée une issue (tâche) dans le Jira de l'entreprise et renvoie sa clé (ex. "SMART-42").
     * Corps au format attendu par l'API Jira Cloud v3 (description en "doc" ADF).
     */
    public String creerTache(Entreprise e, String titre, String description) {
        if (!e.jiraConfigure()) {
            throw new IllegalStateException("L'intégration Jira n'est pas configurée pour cette entreprise.");
        }
        Map<String, Object> descriptionAdf = Map.of(
                "type", "doc", "version", 1,
                "content", List.of(Map.of(
                        "type", "paragraph",
                        "content", List.of(Map.of(
                                "type", "text",
                                "text", description == null ? "" : description))))
        );
        Map<String, Object> fields = Map.of(
                "project", Map.of("key", e.getJiraProjectKey()),
                "summary", titre,
                "description", descriptionAdf,
                "issuetype", Map.of("name", "Task")
        );
        @SuppressWarnings("unchecked")
        Map<String, Object> reponse = clientPour(e).post()
                .uri("/rest/api/3/issue")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("fields", fields))
                .retrieve()
                .body(Map.class);

        return reponse != null ? String.valueOf(reponse.get("key")) : null;
    }
}
