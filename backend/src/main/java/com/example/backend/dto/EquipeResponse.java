package com.example.backend.dto;

import com.example.backend.entity.TypeProjet;

public record EquipeResponse(
        Long id,
        String nom,
        TypeProjet specialite
) {}
