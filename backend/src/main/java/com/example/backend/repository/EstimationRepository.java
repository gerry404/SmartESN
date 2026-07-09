package com.example.backend.repository;

import com.example.backend.entity.Estimation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EstimationRepository extends JpaRepository<Estimation, Long> {
    Optional<Estimation> findByDemandeId(Long demandeId);
}
