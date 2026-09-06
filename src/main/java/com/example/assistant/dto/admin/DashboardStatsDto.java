package com.example.assistant.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsDto {

    private long totalUsers;
    private long activeUsersToday;
    private long totalMessages;
    private long messagesToday;
    private long totalAiRequests;
    private long aiRequestsToday;
    private long totalTokensUsedToday;
    private BigDecimal estimatedCostToday;
    private BigDecimal estimatedCostTotal;
    private long pendingTasks;

    private List<Map<String, Object>> dailyUsageHistory;
    private List<CostBreakdownDto> costBreakdown;
}
