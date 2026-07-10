package com.example.backend.service;

import com.example.backend.entity.Entreprise;
import com.example.backend.entity.Utilisateur;
import com.example.backend.repository.UtilisateurRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/** Retrouve l'utilisateur (et son entreprise) à partir du token JWT de la requête courante. */
@Service
public class UtilisateurCourantService {

    private final UtilisateurRepository utilisateurRepository;

    public UtilisateurCourantService(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    public Utilisateur utilisateur() {
        // le filtre JWT a placé l'email de l'utilisateur comme "principal"
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Utilisateur courant introuvable."));
    }

    public Entreprise entreprise() {
        Entreprise e = utilisateur().getEntreprise();
        if (e == null) {
            throw new IllegalStateException("Aucune entreprise associée à cet utilisateur.");
        }
        return e;
    }
}
