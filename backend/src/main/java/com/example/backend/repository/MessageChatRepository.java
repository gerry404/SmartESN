package com.example.backend.repository;

import com.example.backend.entity.MessageChat;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MessageChatRepository extends JpaRepository<MessageChat, Long> {
    List<MessageChat> findByConversationIdOrderByDateCreationAsc(Long conversationId);
}
