package com.example.backend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
public class Demande {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    @Column(columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    private LocalDateTime dateCreation;

    @Enumerated(EnumType.STRING)
    private StatutDemande statut;

    @Enumerated(EnumType.STRING)
    private TypeProjet type;

    @Enumerated(EnumType.STRING)
    private Complexite complexite;

    private Double scoreConfiance;

    @Column(columnDefinition = "TEXT")
    private String propositionTechnique;

    @Column(columnDefinition = "TEXT")
    private String architectureSuggeree;

    @Column(columnDefinition = "TEXT")
    private String hypotheses;

    @Column(columnDefinition = "TEXT")
    private String incertitudes;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public StatutDemande getStatut() { return statut; }
    public void setStatut(StatutDemande statut) { this.statut = statut; }

    public TypeProjet getType() { return type; }
    public void setType(TypeProjet type) { this.type = type; }

    public Complexite getComplexite() { return complexite; }
    public void setComplexite(Complexite complexite) { this.complexite = complexite; }

    public Double getScoreConfiance() { return scoreConfiance; }
    public void setScoreConfiance(Double scoreConfiance) { this.scoreConfiance = scoreConfiance; }

    public String getPropositionTechnique() { return propositionTechnique; }
    public void setPropositionTechnique(String propositionTechnique) { this.propositionTechnique = propositionTechnique; }

    public String getArchitectureSuggeree() { return architectureSuggeree; }
    public void setArchitectureSuggeree(String architectureSuggeree) { this.architectureSuggeree = architectureSuggeree; }

    public String getHypotheses() { return hypotheses; }
    public void setHypotheses(String hypotheses) { this.hypotheses = hypotheses; }

    public String getIncertitudes() { return incertitudes; }
    public void setIncertitudes(String incertitudes) { this.incertitudes = incertitudes; }
}
