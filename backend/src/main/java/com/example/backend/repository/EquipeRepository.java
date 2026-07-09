package com.example.backend.repository;

import com.example.backend.entity.Equipe;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EquipeRepository extends JpaRepository<Equipe, Long> {
    Optional<Equipe> findByNom(String nom);
}
