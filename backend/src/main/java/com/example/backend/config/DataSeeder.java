package com.example.backend.config;

import com.example.backend.entity.*;
import com.example.backend.repository.*;
import com.example.backend.service.AffectationService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final EquipeRepository equipeRepository;
    private final GrilleReferenceRepository grilleRepository;
    private final EntrepriseRepository entrepriseRepository;

    public DataSeeder(UtilisateurRepository utilisateurRepository, PasswordEncoder passwordEncoder,
                      EquipeRepository equipeRepository, GrilleReferenceRepository grilleRepository,
                      EntrepriseRepository entrepriseRepository) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
        this.equipeRepository = equipeRepository;
        this.grilleRepository = grilleRepository;
        this.entrepriseRepository = entrepriseRepository;
    }

    @Override
    public void run(String... args) {
        seedAdmin();
        seedEquipes();
        seedGrille();
        backfillFormTokens();
    }

    // Attribue un jeton de formulaire aux entreprises créées avant l'ajout de ce champ
    private void backfillFormTokens() {
        entrepriseRepository.findAll().stream()
                .filter(e -> e.getFormToken() == null || e.getFormToken().isBlank())
                .forEach(e -> {
                    e.setFormToken(java.util.UUID.randomUUID().toString().replace("-", ""));
                    entrepriseRepository.save(e);
                });
    }

    private void seedAdmin() {
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

    private void seedEquipes() {
        creerEquipe(AffectationService.EQUIPE_TECHNIQUE, TypeProjet.WEB);
        creerEquipe(AffectationService.EQUIPE_COMMERCIALE, TypeProjet.CONSEIL);
    }

    private void creerEquipe(String nom, TypeProjet specialite) {
        if (equipeRepository.findByNom(nom).isEmpty()) {
            Equipe e = new Equipe();
            e.setNom(nom);
            e.setSpecialite(specialite);
            equipeRepository.save(e);
        }
    }

    private void seedGrille() {
        if (grilleRepository.count() > 0) return;
        // type, complexité, budgetMin, budgetMax, délaiMin, délaiMax (semaines)
        grille(TypeProjet.MOBILE,  Complexite.SIMPLE,   300000,  600000,  3, 5);
        grille(TypeProjet.MOBILE,  Complexite.MOYENNE,  600000,  1500000, 6, 10);
        grille(TypeProjet.MOBILE,  Complexite.COMPLEXE, 1500000, 4000000, 10, 20);
        grille(TypeProjet.WEB,     Complexite.SIMPLE,   150000,  400000,  2, 4);
        grille(TypeProjet.WEB,     Complexite.MOYENNE,  400000,  1200000, 5, 9);
        grille(TypeProjet.WEB,     Complexite.COMPLEXE, 1200000, 3500000, 10, 18);
        grille(TypeProjet.DESKTOP, Complexite.SIMPLE,   250000,  500000,  3, 5);
        grille(TypeProjet.DESKTOP, Complexite.MOYENNE,  500000,  1200000, 6, 10);
        grille(TypeProjet.DESKTOP, Complexite.COMPLEXE, 1200000, 3000000, 10, 16);
        grille(TypeProjet.CONSEIL, Complexite.SIMPLE,   50000,   200000,  1, 3);
        grille(TypeProjet.CONSEIL, Complexite.MOYENNE,  200000,  400000,  2, 4);
        grille(TypeProjet.CONSEIL, Complexite.COMPLEXE, 400000,  500000,  4, 6);
        System.out.println(">>> Grille de reference initialisee (" + grilleRepository.count() + " lignes)");
    }

    private void grille(TypeProjet type, Complexite cx, double bmin, double bmax, int dmin, int dmax) {
        GrilleReference g = new GrilleReference();
        g.setType(type);
        g.setComplexite(cx);
        g.setBudgetMin(bmin);
        g.setBudgetMax(bmax);
        g.setDelaiMin(dmin);
        g.setDelaiMax(dmax);
        grilleRepository.save(g);
    }
}
