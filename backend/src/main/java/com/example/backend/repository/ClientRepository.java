package com.example.backend.repository;
import com.example.backend.entity.*;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {

    // Spring Data génère automatiquement la requête à partir du nom de la méthode
    Optional<Client> findByEmail(String email);
}
