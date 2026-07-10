package com.example.backend.controller;

import com.example.backend.dto.EquipeResponse;
import com.example.backend.repository.EquipeRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/equipes")
public class EquipeController {

    private final EquipeRepository equipeRepository;

    public EquipeController(EquipeRepository equipeRepository) {
        this.equipeRepository = equipeRepository;
    }

    @GetMapping
    public List<EquipeResponse> lister() {
        return equipeRepository.findAll().stream()
                .map(e -> new EquipeResponse(e.getId(), e.getNom(), e.getSpecialite()))
                .toList();
    }
}
