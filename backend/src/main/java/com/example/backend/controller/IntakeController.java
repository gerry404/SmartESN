package com.example.backend.controller;

import com.example.backend.dto.DemandeCreateRequest;
import com.example.backend.dto.IntakeIa;
import com.example.backend.service.IaClient;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoint public du formulaire dynamique côté client.
 * Le front envoie la description (éventuellement enrichie des réponses précédentes) et reçoit
 * soit une confirmation que la demande est complète, soit de nouvelles questions à poser.
 */
@RestController
@RequestMapping("/intake")
public class IntakeController {

    private final IaClient iaClient;

    public IntakeController(IaClient iaClient) {
        this.iaClient = iaClient;
    }

    public record IntakeRequest(@NotBlank String description) {}

    @PostMapping
    public IntakeIa intake(@RequestBody IntakeRequest request) {
        return iaClient.intake(request.description());
    }
}
