package com.example.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Inscription d'une nouvelle entreprise cliente : crée l'entreprise + son premier admin. */
public record RegisterRequest(
        @NotBlank(message = "Le nom de l'entreprise est obligatoire")
        String nomEntreprise,

        @NotBlank(message = "Le nom de l'administrateur est obligatoire")
        String nomAdmin,

        @NotBlank @Email(message = "Email invalide")
        String email,

        @NotBlank @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
        String motDePasse
) {}
