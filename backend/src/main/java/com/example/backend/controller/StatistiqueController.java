package com.example.backend.controller;

import com.example.backend.dto.StatistiquesResponse;
import com.example.backend.service.StatistiqueService;
import com.example.backend.service.UtilisateurCourantService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/statistiques")
public class StatistiqueController {

    private final StatistiqueService statistiqueService;
    private final UtilisateurCourantService utilisateurCourant;

    public StatistiqueController(StatistiqueService statistiqueService,
                                 UtilisateurCourantService utilisateurCourant) {
        this.statistiqueService = statistiqueService;
        this.utilisateurCourant = utilisateurCourant;
    }

    @GetMapping
    public StatistiquesResponse statistiques() {
        Long entrepriseId = utilisateurCourant.entreprise().getId();
        return statistiqueService.calculer(entrepriseId);
    }
}
