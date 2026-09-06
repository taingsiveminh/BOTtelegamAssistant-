package com.example.assistant.service.usage;

import com.example.assistant.dto.ai.AiResponseDto;
import com.example.assistant.entity.AiCostLog;
import com.example.assistant.entity.UsageRecord;
import com.example.assistant.entity.User;
import com.example.assistant.entity.UserTier;
import com.example.assistant.exception.QuotaExceededException;
import com.example.assistant.repository.AiCostLogRepository;
import com.example.assistant.repository.UsageRecordRepository;
import com.example.assistant.service.ai.CostCalculationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsageTrackingService {

    private final UsageRecordRepository usageRecordRepository;
    private final AiCostLogRepository aiCostLogRepository;
    private final CostCalculationService costCalculationService;

    @Value("${usage.free-daily-limit:20}")
    private int freeDailyLimit;

    @Value("${usage.premium-daily-limit:200}")
    private int premiumDailyLimit;

    @Transactional(readOnly = true)
    public void validateUserQuota(User user) {
        if (user.getTier() == UserTier.UNLIMITED) {
            return;
        }

        LocalDate today = LocalDate.now();
        int currentUsage = usageRecordRepository.findByUserIdAndUsageDate(user.getId(), today)
                .map(UsageRecord::getRequestCount)
                .orElse(0);

        int maxLimit = user.getTier() == UserTier.PREMIUM ? premiumDailyLimit : freeDailyLimit;

        if (currentUsage >= maxLimit) {
            log.warn("User {} exceeded daily limit of {}", user.getTelegramUserId(), maxLimit);
            throw new QuotaExceededException("You've reached your daily AI limit (" + maxLimit + " requests). Upgrade to Premium or try again tomorrow!");
        }
    }

    @Transactional
    public void recordUsage(User user, AiResponseDto response, String feature, long latencyMs) {
        if (response == null || response.getUsage() == null) {
            return;
        }

        int promptTokens = response.getUsage().getPromptTokens();
        int completionTokens = response.getUsage().getCompletionTokens();
        int totalTokens = response.getUsage().getTotalTokens();
        String model = response.getModel() != null ? response.getModel() : "gpt-4o-mini";

        BigDecimal cost = costCalculationService.calculateCost(model, promptTokens, completionTokens);

        // 1. Log AI cost transaction
        AiCostLog costLog = AiCostLog.builder()
                .user(user)
                .model(model)
                .promptTokens(promptTokens)
                .completionTokens(completionTokens)
                .totalTokens(totalTokens)
                .estimatedCost(cost)
                .latencyMs(latencyMs)
                .feature(feature != null ? feature : "CHAT")
                .build();
        aiCostLogRepository.save(costLog);

        // 2. Increment Daily user usage record
        LocalDate today = LocalDate.now();
        UsageRecord usage = usageRecordRepository.findByUserIdAndUsageDate(user.getId(), today)
                .orElseGet(() -> UsageRecord.builder()
                        .user(user)
                        .usageDate(today)
                        .requestCount(0)
                        .tokenUsage(0L)
                        .dailyCost(BigDecimal.ZERO)
                        .build());

        usage.setRequestCount(usage.getRequestCount() + 1);
        usage.setTokenUsage(usage.getTokenUsage() + totalTokens);
        usage.setDailyCost(usage.getDailyCost().add(cost));

        usageRecordRepository.save(usage);
        log.debug("Recorded AI usage for user {}: +{} tokens, ${} cost", user.getTelegramUserId(), totalTokens, cost);
    }
}
