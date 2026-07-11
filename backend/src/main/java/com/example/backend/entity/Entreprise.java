package com.example.backend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

/** Une entreprise cliente de SmartESN (un « tenant » en logique SaaS). */
@Entity
public class Entreprise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;

    @Column(unique = true)
    private String email;

    // Jeton public unique servant à construire le lien du formulaire client
    @Column(unique = true)
    private String formToken;

    @CreationTimestamp
    private LocalDateTime dateCreation;

    // --- Configuration Jira PROPRE à cette entreprise (chaque tenant a son Jira) ---
    private String jiraBaseUrl;
    private String jiraEmail;
    @Column(columnDefinition = "TEXT")
    private String jiraApiToken;
    private String jiraProjectKey;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFormToken() { return formToken; }
    public void setFormToken(String formToken) { this.formToken = formToken; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public String getJiraBaseUrl() { return jiraBaseUrl; }
    public void setJiraBaseUrl(String jiraBaseUrl) { this.jiraBaseUrl = jiraBaseUrl; }

    public String getJiraEmail() { return jiraEmail; }
    public void setJiraEmail(String jiraEmail) { this.jiraEmail = jiraEmail; }

    public String getJiraApiToken() { return jiraApiToken; }
    public void setJiraApiToken(String jiraApiToken) { this.jiraApiToken = jiraApiToken; }

    public String getJiraProjectKey() { return jiraProjectKey; }
    public void setJiraProjectKey(String jiraProjectKey) { this.jiraProjectKey = jiraProjectKey; }

    /** Connecté à Jira (URL + jeton) : suffisant pour lister les projets. */
    public boolean jiraConnecte() {
        return jiraBaseUrl != null && !jiraBaseUrl.isBlank()
                && jiraApiToken != null && !jiraApiToken.isBlank();
    }

    /** Entièrement configuré (connecté + projet choisi) : requis pour créer les tâches. */
    public boolean jiraConfigure() {
        return jiraConnecte()
                && jiraProjectKey != null && !jiraProjectKey.isBlank();
    }
}
