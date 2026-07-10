package com.example.backend.repository;

import com.example.backend.entity.Complexite;
import com.example.backend.entity.GrilleReference;
import com.example.backend.entity.TypeProjet;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface GrilleReferenceRepository extends JpaRepository<GrilleReference, Long> {
    Optional<GrilleReference> findByTypeAndComplexite(TypeProjet type, Complexite complexite);
}
