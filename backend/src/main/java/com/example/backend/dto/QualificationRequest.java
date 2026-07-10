package com.example.backend.dto;

import com.example.backend.entity.Complexite;
import com.example.backend.entity.TypeProjet;
import jakarta.validation.constraints.NotNull;

public record QualificationRequest(
        @NotNull(message = "Le type de projet est obligatoire")
        TypeProjet type,

        @NotNull(message = "La complexité est obligatoire")
        Complexite complexite
) {}
