package com.example.backend.repository;

import com.example.backend.entity.Demande;
import com.example.backend.entity.StatutDemande;
import com.example.backend.entity.TypeProjet;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DemandeRepository extends JpaRepository<Demande, Long> {
    List<Demande> findByStatut(StatutDemande statut);
    List<Demande> findByType(TypeProjet type);
    long countByStatut(StatutDemande statut);
}
