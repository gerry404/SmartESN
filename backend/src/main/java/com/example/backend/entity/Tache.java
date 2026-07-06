package com.example.backend.entity;

import jakarta.persistence.*;

@Entity
public class Tache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "projet_id")
    private Projet projet;

    @ManyToOne
    @JoinColumn(name = "assignee_id")
    private Utilisateur assignee;

    private String titre;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private StatutTache statut;

    private String idJira;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Projet getProjet() { return projet; }
    public void setProjet(Projet projet) { this.projet = projet; }

    public Utilisateur getAssignee() { return assignee; }
    public void setAssignee(Utilisateur assignee) { this.assignee = assignee; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public StatutTache getStatut() { return statut; }
    public void setStatut(StatutTache statut) { this.statut = statut; }

    public String getIdJira() { return idJira; }
    public void setIdJira(String idJira) { this.idJira = idJira; }
}
