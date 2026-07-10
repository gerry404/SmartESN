package com.example.backend.service;

import com.example.backend.dto.StatistiquesResponse;
import com.example.backend.entity.Devis;
import com.example.backend.entity.Projet;
import com.example.backend.entity.StatutDemande;
import com.example.backend.repository.DemandeRepository;
import com.example.backend.repository.DevisRepository;
import com.example.backend.repository.ProjetRepository;
import org.springframework.stereotype.Service;

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

    public StatistiquesResponse calculer() {
        long total = demandeRepository.count();
        long nouvelles = demandeRepository.countByStatut(StatutDemande.NOUVELLE);
        long qualifiees = demandeRepository.countByStatut(StatutDemande.QUALIFIEE);
        long devisEnvoyes = demandeRepository.countByStatut(StatutDemande.DEVIS_ENVOYE);
        long gagnees = demandeRepository.countByStatut(StatutDemande.GAGNEE);
        long perdues = demandeRepository.countByStatut(StatutDemande.PERDUE);

        long clotures = gagnees + perdues;
        double tauxConversion = (clotures == 0) ? 0.0 : (gagnees * 100.0 / clotures);

        double caPotentiel = devisRepository.findAll().stream()
                .filter(d -> Boolean.TRUE.equals(d.getValide()))
                .map(Devis::getMontant)
                .filter(m -> m != null)
                .mapToDouble(Double::doubleValue).sum();

        double caSigne = projetRepository.findAll().stream()
                .map(Projet::getBudgetSigne)
                .filter(b -> b != null)
                .mapToDouble(Double::doubleValue).sum();

        return new StatistiquesResponse(total, nouvelles, qualifiees, devisEnvoyes,
                gagnees, perdues, Math.round(tauxConversion * 10) / 10.0, caPotentiel, caSigne);
    }
}
