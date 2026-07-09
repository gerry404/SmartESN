package com.example.backend.dto;

import jakarta.validation.constraints.NotNull;

public record ReaffecterRequest(
        @NotNull Long equipeId
) {}
