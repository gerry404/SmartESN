package com.example.backend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

/** Une discussion de l'assistant, appartenant à un utilisateur interne. */
@Entity
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

    private String titre;

    @CreationTimestamp
    private LocalDateTime dateCreation;

    private LocalDateTime dateMaj;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Utilisateur getUtilisateur() { return utilisateur; }
    public void setUtilisateur(Utilisateur utilisateur) { this.utilisateur = utilisateur; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public LocalDateTime getDateMaj() { return dateMaj; }
    public void setDateMaj(LocalDateTime dateMaj) { this.dateMaj = dateMaj; }
}
