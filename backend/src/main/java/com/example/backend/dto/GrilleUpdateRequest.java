package com.example.backend.dto;

import jakarta.validation.constraints.NotNull;

public record GrilleUpdateRequest(
        @NotNull Double budgetMin,
        @NotNull Double budgetMax,
        @NotNull Integer delaiMin,
        @NotNull Integer delaiMax
) {}
