package com.example.assistant.repository;

import com.example.assistant.entity.AiCostLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AiCostLogRepository extends JpaRepository<AiCostLog, Long> {

    @Query("SELECT SUM(a.estimatedCost) FROM AiCostLog a WHERE a.createdAt >= :since")
    Optional<BigDecimal> sumCostSince(OffsetDateTime since);

    @Query("SELECT COUNT(a) FROM AiCostLog a WHERE a.createdAt >= :since")
    long countAiRequestsSince(OffsetDateTime since);

    @Query("SELECT a.model, COUNT(a), SUM(a.totalTokens), SUM(a.estimatedCost) FROM AiCostLog a GROUP BY a.model")
    List<Object[]> getCostBreakdownByModel();
}
