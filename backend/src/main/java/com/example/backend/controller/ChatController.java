package com.example.backend.controller;

import com.example.backend.dto.*;
import com.example.backend.entity.Conversation;
import com.example.backend.entity.MessageChat;
import com.example.backend.entity.Utilisateur;
import com.example.backend.repository.ConversationRepository;
import com.example.backend.repository.MessageChatRepository;
import com.example.backend.dto.StatistiquesResponse;
import com.example.backend.service.IaClient;
import com.example.backend.service.StatistiqueService;
import com.example.backend.service.UtilisateurCourantService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Assistant conversationnel avec mémoire : les discussions et leurs messages sont persistés
 * par utilisateur. À chaque envoi, tout l'historique est transmis au LLM (aucune réponse
 * pré-enregistrée) et la réponse est enregistrée.
 */
@RestController
@RequestMapping("/chat/conversations")
public class ChatController {

    private final ConversationRepository conversationRepository;
    private final MessageChatRepository messageRepository;
    private final IaClient iaClient;
    private final UtilisateurCourantService utilisateurCourant;
    private final StatistiqueService statistiqueService;

    public ChatController(ConversationRepository conversationRepository,
                          MessageChatRepository messageRepository, IaClient iaClient,
                          UtilisateurCourantService utilisateurCourant,
                          StatistiqueService statistiqueService) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.iaClient = iaClient;
        this.utilisateurCourant = utilisateurCourant;
        this.statistiqueService = statistiqueService;
    }

    // Liste les discussions de l'utilisateur (les plus récentes d'abord)
    @GetMapping
    public List<ConversationResponse> lister() {
        Long uid = utilisateurCourant.utilisateur().getId();
        return conversationRepository.findByUtilisateurIdOrderByDateMajDesc(uid).stream()
                .map(c -> new ConversationResponse(c.getId(), c.getTitre(), c.getDateMaj()))
                .toList();
    }

    // Crée une nouvelle discussion vide
    @PostMapping
    public ConversationResponse creer() {
        Utilisateur user = utilisateurCourant.utilisateur();
        Conversation c = new Conversation();
        c.setUtilisateur(user);
        c.setTitre("Nouvelle discussion");
        c.setDateMaj(LocalDateTime.now());
        c = conversationRepository.save(c);
        return new ConversationResponse(c.getId(), c.getTitre(), c.getDateMaj());
    }

    // Récupère les messages d'une discussion
    @GetMapping("/{id}/messages")
    public ResponseEntity<List<MessageResponse>> messages(@PathVariable Long id) {
        Conversation c = chargerMienne(id);
        if (c == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(messageRepository.findByConversationIdOrderByDateCreationAsc(id).stream()
                .map(m -> new MessageResponse(m.getRole(), m.getContent()))
                .toList());
    }

    // Envoie un message : persiste, appelle le LLM avec tout l'historique, persiste la réponse
    @PostMapping("/{id}/messages")
    public ResponseEntity<?> envoyer(@PathVariable Long id, @Valid @RequestBody ChatSendRequest req) {
        Conversation c = chargerMienne(id);
        if (c == null) return ResponseEntity.notFound().build();

        // 1. enregistrer le message utilisateur
        MessageChat userMsg = new MessageChat();
        userMsg.setConversation(c);
        userMsg.setRole("user");
        userMsg.setContent(req.content());
        messageRepository.save(userMsg);

        // 2. construire l'historique complet et appeler le LLM
        List<Map<String, String>> historique = messageRepository
                .findByConversationIdOrderByDateCreationAsc(id).stream()
                .map(m -> Map.of("role", m.getRole(), "content", m.getContent()))
                .toList();
        String reponse = iaClient.chat(historique, contexteStats());

        // 3. enregistrer la réponse de l'assistant
        MessageChat botMsg = new MessageChat();
        botMsg.setConversation(c);
        botMsg.setRole("assistant");
        botMsg.setContent(reponse);
        messageRepository.save(botMsg);

        // 4. titre auto (1er message) + date de mise à jour
        if ("Nouvelle discussion".equals(c.getTitre())) {
            String t = req.content().length() > 50 ? req.content().substring(0, 50) + "…" : req.content();
            c.setTitre(t);
        }
        c.setDateMaj(LocalDateTime.now());
        conversationRepository.save(c);

        return ResponseEntity.ok(new MessageResponse("assistant", reponse));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        Conversation c = chargerMienne(id);
        if (c == null) return ResponseEntity.notFound().build();
        messageRepository.findByConversationIdOrderByDateCreationAsc(id)
                .forEach(messageRepository::delete);
        conversationRepository.delete(c);
        return ResponseEntity.noContent().build();
    }

    // Charge une conversation uniquement si elle appartient à l'utilisateur connecté
    private Conversation chargerMienne(Long id) {
        Long uid = utilisateurCourant.utilisateur().getId();
        return conversationRepository.findById(id)
                .filter(c -> c.getUtilisateur() != null && c.getUtilisateur().getId().equals(uid))
                .orElse(null);
    }

    // Résumé chiffré de l'entreprise, injecté au LLM pour des réponses fondées sur les données.
    private String contexteStats() {
        Long entrepriseId = utilisateurCourant.entreprise().getId();
        StatistiquesResponse s = statistiqueService.calculer(entrepriseId);
        return String.format(
                "Total demandes: %d | Nouvelles: %d | Qualifiées: %d | Devis envoyés: %d | "
                + "Gagnées: %d | Perdues: %d | Taux de conversion: %.1f%% | "
                + "CA potentiel: %.0f FCFA | CA signé: %.0f FCFA",
                s.totalDemandes(), s.nbNouvelles(), s.nbQualifiees(), s.nbDevisEnvoyes(),
                s.nbGagnees(), s.nbPerdues(), s.tauxConversion(), s.caPotentiel(), s.caSigne());
    }
}
