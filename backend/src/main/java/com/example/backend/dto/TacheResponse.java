package com.example.backend.dto;

import com.example.backend.entity.StatutTache;

public record TacheResponse(
        Long id,
        String titre,
        String description,
        StatutTache statut,
        String assigneeNom,
        String idJira
) {}
