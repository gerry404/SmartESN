package com.example.backend.dto;

import com.example.backend.entity.RoleUtilisateur;

public record UtilisateurResponse(
        Long id,
        String nom,
        String email,
        RoleUtilisateur role
) {}
