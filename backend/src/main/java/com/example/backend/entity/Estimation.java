package com.example.backend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
public class Estimation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "demande_id")
    private Demande demande;

    @ManyToOne
    @JoinColumn(name = "grille_reference_id")
    private GrilleReference grilleReference;

    private Double budgetMin;

    private Double budgetMax;

    private Integer delaiSemaines;

    @CreationTimestamp
    private LocalDateTime dateEstimation;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Demande getDemande() { return demande; }
    public void setDemande(Demande demande) { this.demande = demande; }

    public GrilleReference getGrilleReference() { return grilleReference; }
    public void setGrilleReference(GrilleReference grilleReference) { this.grilleReference = grilleReference; }

    public Double getBudgetMin() { return budgetMin; }
    public void setBudgetMin(Double budgetMin) { this.budgetMin = budgetMin; }

    public Double getBudgetMax() { return budgetMax; }
    public void setBudgetMax(Double budgetMax) { this.budgetMax = budgetMax; }

    public Integer getDelaiSemaines() { return delaiSemaines; }
    public void setDelaiSemaines(Integer delaiSemaines) { this.delaiSemaines = delaiSemaines; }

    public LocalDateTime getDateEstimation() { return dateEstimation; }
    public void setDateEstimation(LocalDateTime dateEstimation) { this.dateEstimation = dateEstimation; }
}
