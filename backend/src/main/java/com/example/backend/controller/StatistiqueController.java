package com.example.backend.controller;

import com.example.backend.dto.StatistiquesResponse;
import com.example.backend.service.StatistiqueService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/statistiques")
public class StatistiqueController {

    private final StatistiqueService statistiqueService;

    public StatistiqueController(StatistiqueService statistiqueService) {
        this.statistiqueService = statistiqueService;
    }

    @GetMapping
    public StatistiquesResponse statistiques() {
        return statistiqueService.calculer();
    }
}
