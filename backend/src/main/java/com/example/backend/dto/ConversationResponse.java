package com.example.backend.dto;

import java.time.LocalDateTime;

public record ConversationResponse(
        Long id,
        String titre,
        LocalDateTime dateMaj
) {}
