package com.example.backend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
public class Devis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "demande_id")
    private Demande demande;

    @ManyToOne
    @JoinColumn(name = "valide_par_id")
    private Utilisateur validePar;

    @Column(columnDefinition = "TEXT")
    private String contenu;

    private Double montant;

    @CreationTimestamp
    private LocalDateTime dateCreation;

    private LocalDateTime dateEnvoi;

    private Boolean valide;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Demande getDemande() { return demande; }
    public void setDemande(Demande demande) { this.demande = demande; }

    public Utilisateur getValidePar() { return validePar; }
    public void setValidePar(Utilisateur validePar) { this.validePar = validePar; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    public Double getMontant() { return montant; }
    public void setMontant(Double montant) { this.montant = montant; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public LocalDateTime getDateEnvoi() { return dateEnvoi; }
    public void setDateEnvoi(LocalDateTime dateEnvoi) { this.dateEnvoi = dateEnvoi; }

    public Boolean getValide() { return valide; }
    public void setValide(Boolean valide) { this.valide = valide; }
}
