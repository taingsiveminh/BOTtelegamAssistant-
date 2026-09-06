package com.example.assistant.repository;

import com.example.assistant.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findFirstByUserIdAndActiveTrueOrderByUpdatedAtDesc(Long userId);

    List<Conversation> findAllByUserIdOrderByUpdatedAtDesc(Long userId);

    @Query("SELECT COUNT(c) FROM Conversation c WHERE c.createdAt >= :since")
    long countConversationsSince(OffsetDateTime since);
}
