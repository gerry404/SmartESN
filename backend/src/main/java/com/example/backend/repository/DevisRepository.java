package com.example.backend.repository;

import com.example.backend.entity.Devis;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DevisRepository extends JpaRepository<Devis, Long> {
    Optional<Devis> findByDemandeId(Long demandeId);
}
