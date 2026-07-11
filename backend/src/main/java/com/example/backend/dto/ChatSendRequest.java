package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatSendRequest(
        @NotBlank String content
) {}
