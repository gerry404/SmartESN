package com.example.backend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

/** Un message d'une conversation (rôle : user ou assistant). */
@Entity
public class MessageChat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;

    private String role; // "user" ou "assistant"

    @Column(columnDefinition = "TEXT")
    private String content;

    @CreationTimestamp
    private LocalDateTime dateCreation;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Conversation getConversation() { return conversation; }
    public void setConversation(Conversation conversation) { this.conversation = conversation; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
}
