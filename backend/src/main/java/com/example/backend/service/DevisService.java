package com.example.backend.service;

import com.example.backend.entity.Demande;
import com.example.backend.entity.Devis;
import com.example.backend.entity.Estimation;
import com.example.backend.repository.DevisRepository;
import com.example.backend.repository.EstimationRepository;
import org.springframework.stereotype.Service;

@Service
public class DevisService {

    private final DevisRepository devisRepository;
    private final EstimationRepository estimationRepository;

    public DevisService(DevisRepository devisRepository, EstimationRepository estimationRepository) {
        this.devisRepository = devisRepository;
        this.estimationRepository = estimationRepository;
    }

    /**
     * Génère (ou régénère) un devis préliminaire pour une demande qualifiée.
     * Le contenu est construit à partir d'un modèle texte + l'estimation (déterministe,
     * sans IA). Le montant retenu est le centre de la fourchette budgétaire.
     */
    public Devis genererPour(Demande demande) {
        Estimation est = estimationRepository.findByDemandeId(demande.getId())
                .orElseThrow(() -> new IllegalStateException(
                        "Impossible de générer un devis : la demande n'a pas encore été estimée."));

        double montant = (est.getBudgetMin() + est.getBudgetMax()) / 2.0;

        String contenu = """
                DEVIS PRÉLIMINAIRE - SmartESN
                ================================
                Type de projet : %s
                Complexité      : %s
                Budget estimé   : %,.0f à %,.0f FCFA
                Montant proposé : %,.0f FCFA
                Délai estimé    : %d semaines

                Description du besoin :
                %s

                Ce devis est préliminaire et sera affiné après échange avec le client.
                """.formatted(
                demande.getType(), demande.getComplexite(),
                est.getBudgetMin(), est.getBudgetMax(), montant,
                est.getDelaiSemaines(), demande.getDescription());

        // un seul devis par demande : on réutilise l'existant s'il y en a un
        Devis devis = devisRepository.findByDemandeId(demande.getId()).orElseGet(Devis::new);
        devis.setDemande(demande);
        devis.setContenu(contenu);
        devis.setMontant(montant);
        devis.setValide(false);
        return devisRepository.save(devis);
    }
}
