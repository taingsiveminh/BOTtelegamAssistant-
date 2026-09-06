package com.example.assistant.repository;

import com.example.assistant.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findAllByConversationIdOrderByCreatedAtAsc(Long conversationId);

    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId ORDER BY m.createdAt DESC")
    List<Message> findRecentMessages(Long conversationId, Pageable pageable);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.createdAt >= :since")
    long countMessagesSince(OffsetDateTime since);

    void deleteAllByConversationId(Long conversationId);
}
