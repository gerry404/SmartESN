package com.example.backend.config;
import com.example.backend.entity.*;
import com.example.backend.repository.*;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UtilisateurRepository utilisateurRepository, PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (utilisateurRepository.findByEmail("admin@smartesn.com").isEmpty()) {
            Utilisateur admin = new Utilisateur();
            admin.setNom("Admin");
            admin.setEmail("admin@smartesn.com");
            admin.setMotDePasse(passwordEncoder.encode("admin123"));
            admin.setRole(RoleUtilisateur.ADMIN);
            utilisateurRepository.save(admin);
            System.out.println(">>> Utilisateur admin cree : admin@smartesn.com / admin123");
        }
    }

}
