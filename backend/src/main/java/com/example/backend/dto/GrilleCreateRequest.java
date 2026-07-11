package com.example.backend.dto;

import com.example.backend.entity.Complexite;
import com.example.backend.entity.TypeProjet;
import jakarta.validation.constraints.NotNull;

public record GrilleCreateRequest(
        @NotNull TypeProjet type,
        @NotNull Complexite complexite,
        @NotNull Double budgetMin,
        @NotNull Double budgetMax,
        @NotNull Integer delaiMin,
        @NotNull Integer delaiMax
) {}
