package com.example.backend.repository;

import com.example.backend.entity.Affectation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AffectationRepository extends JpaRepository<Affectation, Long> {
    Optional<Affectation> findByDemandeId(Long demandeId);
}
