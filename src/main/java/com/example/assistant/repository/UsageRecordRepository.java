package com.example.assistant.repository;

import com.example.assistant.entity.UsageRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsageRecordRepository extends JpaRepository<UsageRecord, Long> {

    Optional<UsageRecord> findByUserIdAndUsageDate(Long userId, LocalDate usageDate);

    @Query("SELECT SUM(u.requestCount) FROM UsageRecord u WHERE u.usageDate = :date")
    Optional<Long> sumRequestsByDate(LocalDate date);

    @Query("SELECT SUM(u.tokenUsage) FROM UsageRecord u WHERE u.usageDate = :date")
    Optional<Long> sumTokensByDate(LocalDate date);

    @Query("SELECT SUM(u.dailyCost) FROM UsageRecord u WHERE u.usageDate = :date")
    Optional<BigDecimal> sumCostByDate(LocalDate date);

    @Query("SELECT u.usageDate, SUM(u.requestCount), SUM(u.tokenUsage), SUM(u.dailyCost) FROM UsageRecord u WHERE u.usageDate >= :startDate GROUP BY u.usageDate ORDER BY u.usageDate ASC")
    List<Object[]> getAggregatedUsageSince(LocalDate startDate);
}
