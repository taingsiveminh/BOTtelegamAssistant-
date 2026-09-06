package com.example.assistant.repository;

import com.example.assistant.entity.Task;
import com.example.assistant.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findAllByUserIdAndStatusOrderByScheduledAtAsc(Long userId, TaskStatus status);

    List<Task> findAllByUserIdOrderByScheduledAtAsc(Long userId);

    @Query("SELECT t FROM Task t WHERE t.status = 'PENDING' AND t.scheduledAt <= :now ORDER BY t.scheduledAt ASC")
    List<Task> findDuePendingTasks(OffsetDateTime now);

    long countByUserIdAndStatus(Long userId, TaskStatus status);
}
