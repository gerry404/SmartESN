package com.example.backend.entity;

import jakarta.persistence.*;

@Entity
public class Projet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "demande_id")
    private Demande demande;

    @Column(columnDefinition = "TEXT")
    private String titre;

    private Double budgetSigne;

    private Integer delaiReel;

    private String cleJira;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Demande getDemande() { return demande; }
    public void setDemande(Demande demande) { this.demande = demande; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public Double getBudgetSigne() { return budgetSigne; }
    public void setBudgetSigne(Double budgetSigne) { this.budgetSigne = budgetSigne; }

    public Integer getDelaiReel() { return delaiReel; }
    public void setDelaiReel(Integer delaiReel) { this.delaiReel = delaiReel; }

    public String getCleJira() { return cleJira; }
    public void setCleJira(String cleJira) { this.cleJira = cleJira; }
}
