package com.example.backend.entity;


import jakarta.persistence.*;

@Entity
public class Equipe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(columnDefinition = "TEXT")
    private String nom;

    @Enumerated(EnumType.STRING)
    private TypeProjet specialite;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public TypeProjet getSpecialite() {
        return specialite;
    }

    public void setSpecialite(TypeProjet specialite) {
        this.specialite = specialite;
    }
}
