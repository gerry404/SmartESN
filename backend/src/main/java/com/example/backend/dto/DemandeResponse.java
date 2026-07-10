package com.example.backend.dto;
import com.example.backend.entity.*;

import java.time.LocalDateTime;

public record DemandeResponse(Long id,
                              String description,
                              String clientNom,
                              String clientEmail,
                              StatutDemande statut,
                              TypeProjet type,
                              Complexite complexite,
                              Double scoreConfiance,
                              LocalDateTime dateCreation) {
}
