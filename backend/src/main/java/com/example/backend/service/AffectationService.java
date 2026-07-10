package com.example.backend.service;

import com.example.backend.entity.Affectation;
import com.example.backend.entity.Demande;
import com.example.backend.entity.Equipe;
import com.example.backend.entity.TypeProjet;
import com.example.backend.repository.AffectationRepository;
import com.example.backend.repository.EquipeRepository;
import org.springframework.stereotype.Service;

@Service
public class AffectationService {

    public static final String EQUIPE_TECHNIQUE = "Équipe Technique";
    public static final String EQUIPE_COMMERCIALE = "Équipe Commerciale";

    private final EquipeRepository equipeRepository;
    private final AffectationRepository affectationRepository;

    public AffectationService(EquipeRepository equipeRepository,
                              AffectationRepository affectationRepository) {
        this.equipeRepository = equipeRepository;
        this.affectationRepository = affectationRepository;
    }

    /**
     * Affecte automatiquement une demande qualifiée à la bonne équipe selon des règles :
     * - CONSEIL          -> Équipe Commerciale
     * - MOBILE/WEB/DESKTOP -> Équipe Technique
     * Déterministe : ne dépend pas de l'IA.
     */
    public Affectation affecter(Demande demande) {
        String nomEquipe = (demande.getType() == TypeProjet.CONSEIL)
                ? EQUIPE_COMMERCIALE
                : EQUIPE_TECHNIQUE;

        Equipe equipe = equipeRepository.findByNom(nomEquipe)
                .orElseThrow(() -> new IllegalStateException("Équipe introuvable : " + nomEquipe));

        Affectation affectation = new Affectation();
        affectation.setDemande(demande);
        affectation.setEquipe(equipe);
        affectation.setManuelle(false);

        return affectationRepository.save(affectation);
    }
}
