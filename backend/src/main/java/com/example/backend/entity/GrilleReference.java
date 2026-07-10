package com.example.backend.entity;

import jakarta.persistence.*;

@Entity
public class GrilleReference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TypeProjet type;

    @Enumerated(EnumType.STRING)
    private Complexite complexite;

    private Double budgetMin;

    private Double budgetMax;

    private Integer delaiMin;

    private Integer delaiMax;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TypeProjet getType() {
        return type;
    }

    public void setType(TypeProjet type) {
        this.type = type;
    }

    public Complexite getComplexite() {
        return complexite;
    }

    public void setComplexite(Complexite complexite) {
        this.complexite = complexite;
    }

    public Double getBudgetMin() {
        return budgetMin;
    }

    public void setBudgetMin(Double budgetMin) {
        this.budgetMin = budgetMin;
    }

    public Double getBudgetMax() {
        return budgetMax;
    }

    public void setBudgetMax(Double budgetMax) {
        this.budgetMax = budgetMax;
    }

    public Integer getDelaiMin() {
        return delaiMin;
    }

    public void setDelaiMin(Integer delaiMin) {
        this.delaiMin = delaiMin;
    }

    public Integer getDelaiMax() {
        return delaiMax;
    }

    public void setDelaiMax(Integer delaiMax) {
        this.delaiMax = delaiMax;
    }
}
