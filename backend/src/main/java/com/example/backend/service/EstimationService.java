package com.example.backend.service;

import com.example.backend.entity.Demande;
import com.example.backend.entity.Estimation;
import com.example.backend.entity.GrilleReference;
import com.example.backend.repository.EstimationRepository;
import com.example.backend.repository.GrilleReferenceRepository;
import org.springframework.stereotype.Service;

@Service
public class EstimationService {

    private final GrilleReferenceRepository grilleRepository;
    private final EstimationRepository estimationRepository;

    public EstimationService(GrilleReferenceRepository grilleRepository,
                             EstimationRepository estimationRepository) {
        this.grilleRepository = grilleRepository;
        this.estimationRepository = estimationRepository;
    }

    /**
     * Estime le budget et le délai d'une demande à partir de la grille de référence
     * (type + complexité). Déterministe : ne dépend pas de l'IA.
     */
    public Estimation estimer(Demande demande) {
        GrilleReference grille = grilleRepository
                .findByTypeAndComplexite(demande.getType(), demande.getComplexite())
                .orElseThrow(() -> new IllegalStateException(
                        "Aucune grille de référence pour " + demande.getType() + " / " + demande.getComplexite()));

        Estimation estimation = new Estimation();
        estimation.setDemande(demande);
        estimation.setGrilleReference(grille);
        estimation.setBudgetMin(grille.getBudgetMin());
        estimation.setBudgetMax(grille.getBudgetMax());
        // on retient le délai maximal de la fourchette (plus prudent)
        estimation.setDelaiSemaines(grille.getDelaiMax());

        return estimationRepository.save(estimation);
    }
}
