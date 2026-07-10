package com.example.backend.service;

import com.example.backend.dto.StatistiquesResponse;
import com.example.backend.entity.Demande;
import com.example.backend.entity.StatutDemande;
import com.example.backend.repository.DemandeRepository;
import com.example.backend.repository.DevisRepository;
import com.example.backend.repository.ProjetRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StatistiqueService {

    private final DemandeRepository demandeRepository;
    private final DevisRepository devisRepository;
    private final ProjetRepository projetRepository;

    public StatistiqueService(DemandeRepository demandeRepository, DevisRepository devisRepository,
                              ProjetRepository projetRepository) {
        this.demandeRepository = demandeRepository;
        this.devisRepository = devisRepository;
        this.projetRepository = projetRepository;
    }

    /** Statistiques calculées UNIQUEMENT sur les données de l'entreprise donnée. */
    public StatistiquesResponse calculer(Long entrepriseId) {
        List<Demande> demandes = demandeRepository.findByEntrepriseId(entrepriseId);

        long total = demandes.size();
        long nouvelles = compter(demandes, StatutDemande.NOUVELLE);
        long qualifiees = compter(demandes, StatutDemande.QUALIFIEE);
        long devisEnvoyes = compter(demandes, StatutDemande.DEVIS_ENVOYE);
        long gagnees = compter(demandes, StatutDemande.GAGNEE);
        long perdues = compter(demandes, StatutDemande.PERDUE);

        long clotures = gagnees + perdues;
        double tauxConversion = (clotures == 0) ? 0.0 : (gagnees * 100.0 / clotures);

        // CA potentiel : devis validés dont la demande appartient à l'entreprise
        double caPotentiel = devisRepository.findAll().stream()
                .filter(d -> Boolean.TRUE.equals(d.getValide()))
                .filter(d -> appartient(d.getDemande(), entrepriseId))
                .map(com.example.backend.entity.Devis::getMontant)
                .filter(m -> m != null)
                .mapToDouble(Double::doubleValue).sum();

        // CA signé : projets dont la demande appartient à l'entreprise
        double caSigne = projetRepository.findAll().stream()
                .filter(p -> appartient(p.getDemande(), entrepriseId))
                .map(com.example.backend.entity.Projet::getBudgetSigne)
                .filter(b -> b != null)
                .mapToDouble(Double::doubleValue).sum();

        return new StatistiquesResponse(total, nouvelles, qualifiees, devisEnvoyes,
                gagnees, perdues, Math.round(tauxConversion * 10) / 10.0, caPotentiel, caSigne);
    }

    private long compter(List<Demande> demandes, StatutDemande statut) {
        return demandes.stream().filter(d -> d.getStatut() == statut).count();
    }

    private boolean appartient(Demande demande, Long entrepriseId) {
        return demande != null && demande.getEntreprise() != null
                && demande.getEntreprise().getId().equals(entrepriseId);
    }
}
