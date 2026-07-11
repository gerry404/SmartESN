package com.example.backend.service;

import com.example.backend.dto.ClassificationIa;
import com.example.backend.dto.DecompositionIa;
import com.example.backend.dto.IntakeIa;
import com.example.backend.entity.Complexite;
import com.example.backend.entity.TypeProjet;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * Client HTTP vers le microservice IA (FastAPI).
 * Encapsule les appels réseau ; le reste du backend ne dépend que de cette classe.
 */
@Service
public class IaClient {

    private final RestClient restClient;

    public IaClient(@Value("${app.ia.base-url}") String baseUrl) {
        // SimpleClientHttpRequestFactory : HTTP/1.1 pur, sans tentative d'upgrade HTTP/2
        // (le serveur FastAPI/uvicorn ne gère que HTTP/1.1).
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(new SimpleClientHttpRequestFactory())
                .build();
    }

    /** Appelle POST /classify pour qualifier une demande (type + complexité + score). */
    public ClassificationIa classifier(String description) {
        return restClient.post()
                .uri("/classify")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new DescriptionBody(description))
                .retrieve()
                .body(ClassificationIa.class);
    }

    /** Appelle POST /intake : conduite du formulaire dynamique (complétude + questions). */
    public IntakeIa intake(String description) {
        return restClient.post()
                .uri("/intake")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new DescriptionBody(description))
                .retrieve()
                .body(IntakeIa.class);
    }

    /** Appelle POST /decompose : découpe le projet en tâches + suggère les assignés. */
    public DecompositionIa decompose(String description, TypeProjet type, Complexite complexite,
                                     List<Map<String, String>> membres) {
        var body = Map.of(
                "description", description,
                "type", type.name(),
                "complexite", complexite.name(),
                "membres", membres
        );
        return restClient.post()
                .uri("/decompose")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(DecompositionIa.class);
    }

    /** Appelle POST /chat avec un contexte optionnel (ex. stats de l'entreprise). */
    public String chat(List<Map<String, String>> messages, String contexte) {
        Map<String, Object> body = new java.util.HashMap<>();
        body.put("messages", messages);
        if (contexte != null) body.put("contexte", contexte);
        @SuppressWarnings("unchecked")
        Map<String, Object> reponse = restClient.post()
                .uri("/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(Map.class);
        return reponse != null ? String.valueOf(reponse.get("reply")) : "";
    }

    /** Envoie un fichier au service IA pour extraction. Renvoie { type_fichier, texte }. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> extract(byte[] contenu, String nomFichier, String contentType) {
        var body = new org.springframework.util.LinkedMultiValueMap<String, Object>();
        var resource = new org.springframework.core.io.ByteArrayResource(contenu) {
            @Override
            public String getFilename() {
                return nomFichier;
            }
        };
        body.add("file", resource);
        return restClient.post()
                .uri("/extract")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .body(Map.class);
    }

    // corps de requête sérialisé en JSON { "description": "..." }
    public record DescriptionBody(String description) {
    }
}
