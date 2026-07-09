package com.example.backend.dto;

public record StatistiquesResponse(
        long totalDemandes,
        long nbNouvelles,
        long nbQualifiees,
        long nbDevisEnvoyes,
        long nbGagnees,
        long nbPerdues,
        double tauxConversion,   // gagnées / (gagnées + perdues) en %
        double caPotentiel,      // somme des montants de devis envoyés
        double caSigne           // somme des budgets réellement signés
) {}
