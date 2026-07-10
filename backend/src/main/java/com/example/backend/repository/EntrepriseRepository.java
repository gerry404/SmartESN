package com.example.backend.repository;

import com.example.backend.entity.Entreprise;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EntrepriseRepository extends JpaRepository<Entreprise, Long> {
    Optional<Entreprise> findByEmail(String email);
    Optional<Entreprise> findByFormToken(String formToken);
}
