package com.example.backend.dto;

import com.example.backend.entity.RoleUtilisateur;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UtilisateurCreateRequest(
        @NotBlank String nom,
        @NotBlank @Email String email,
        @NotBlank String motDePasse,
        @NotNull RoleUtilisateur role
) {}
