package com.example.backend.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.Map;

/** Transforme les exceptions en réponses HTTP propres (au lieu d'une erreur 500 brute). */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Erreurs de validation (@NotBlank, @Email...) -> 400 avec le détail des champs
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> erreurs = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> erreurs.put(e.getField(), e.getDefaultMessage()));
        return ResponseEntity.badRequest().body(erreurs);
    }

    // Règles métier violées (ex: pas de grille pour ce type/complexité) -> 409
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("erreur", ex.getMessage()));
    }

    // Service IA injoignable ou en erreur -> 502
    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<Map<String, String>> handleIaIndisponible(RestClientException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(Map.of("erreur", "Le service d'analyse IA est momentanément indisponible."));
    }
}
