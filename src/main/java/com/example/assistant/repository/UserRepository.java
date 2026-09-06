package com.example.assistant.repository;

import com.example.assistant.entity.User;
import com.example.assistant.entity.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByTelegramUserId(Long telegramUserId);

    boolean existsByTelegramUserId(Long telegramUserId);

    long countByStatus(UserStatus status);

    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :since")
    long countNewUsersSince(OffsetDateTime since);

    Page<User> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<User> findAllByStatus(UserStatus status);
}
