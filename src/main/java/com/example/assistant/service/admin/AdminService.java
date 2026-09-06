package com.example.assistant.service.admin;

import com.example.assistant.dto.admin.CostBreakdownDto;
import com.example.assistant.dto.admin.DashboardStatsDto;
import com.example.assistant.dto.admin.UserDto;
import com.example.assistant.entity.TaskStatus;
import com.example.assistant.entity.User;
import com.example.assistant.entity.UserStatus;
import com.example.assistant.entity.UserTier;
import com.example.assistant.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final TaskRepository taskRepository;
    private final UsageRecordRepository usageRecordRepository;
    private final AiCostLogRepository aiCostLogRepository;

    @Transactional(readOnly = true)
    public DashboardStatsDto getDashboardStats() {
        LocalDate today = LocalDate.now();
        OffsetDateTime startOfToday = today.atStartOfDay().atOffset(ZoneOffset.UTC);

        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByStatus(UserStatus.ACTIVE);
        long totalMessages = messageRepository.count();
        long messagesToday = messageRepository.countMessagesSince(startOfToday);
        long totalAiRequests = aiCostLogRepository.count();
        long aiRequestsToday = aiCostLogRepository.countAiRequestsSince(startOfToday);

        long tokensToday = usageRecordRepository.sumTokensByDate(today).orElse(0L);
        BigDecimal costToday = usageRecordRepository.sumCostByDate(today).orElse(BigDecimal.ZERO);
        BigDecimal costTotal = aiCostLogRepository.sumCostSince(OffsetDateTime.MIN).orElse(BigDecimal.ZERO);

        // Daily usage past 7 days
        LocalDate sevenDaysAgo = today.minusDays(7);
        List<Object[]> rawDaily = usageRecordRepository.getAggregatedUsageSince(sevenDaysAgo);
        List<Map<String, Object>> dailyHistory = new ArrayList<>();
        for (Object[] row : rawDaily) {
            dailyHistory.add(Map.of(
                    "date", row[0].toString(),
                    "requests", row[1] != null ? row[1] : 0,
                    "tokens", row[2] != null ? row[2] : 0,
                    "cost", row[3] != null ? row[3] : BigDecimal.ZERO
            ));
        }

        // Cost breakdown by model
        List<Object[]> rawModels = aiCostLogRepository.getCostBreakdownByModel();
        List<CostBreakdownDto> modelCosts = new ArrayList<>();
        for (Object[] row : rawModels) {
            modelCosts.add(CostBreakdownDto.builder()
                    .model((String) row[0])
                    .requestCount(row[1] != null ? ((Number) row[1]).longValue() : 0L)
                    .totalTokens(row[2] != null ? ((Number) row[2]).longValue() : 0L)
                    .estimatedCost(row[3] != null ? (BigDecimal) row[3] : BigDecimal.ZERO)
                    .build());
        }

        return DashboardStatsDto.builder()
                .totalUsers(totalUsers)
                .activeUsersToday(activeUsers)
                .totalMessages(totalMessages)
                .messagesToday(messagesToday)
                .totalAiRequests(totalAiRequests)
                .aiRequestsToday(aiRequestsToday)
                .totalTokensUsedToday(tokensToday)
                .estimatedCostToday(costToday)
                .estimatedCostTotal(costTotal)
                .dailyUsageHistory(dailyHistory)
                .costBreakdown(modelCosts)
                .build();
    }

    @Transactional(readOnly = true)
    public Page<UserDto> getUsers(Pageable pageable) {
        return userRepository.findAllByOrderByCreatedAtDesc(pageable).map(this::mapUserToDto);
    }

    @Transactional
    public UserDto toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        user.setStatus(user.getStatus() == UserStatus.ACTIVE ? UserStatus.BLOCKED : UserStatus.ACTIVE);
        User saved = userRepository.save(user);
        return mapUserToDto(saved);
    }

    @Transactional
    public UserDto updateUserTier(Long userId, String tierName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        user.setTier(UserTier.valueOf(tierName.toUpperCase()));
        User saved = userRepository.save(user);
        return mapUserToDto(saved);
    }

    private UserDto mapUserToDto(User u) {
        return UserDto.builder()
                .id(u.getId())
                .telegramUserId(u.getTelegramUserId())
                .username(u.getUsername())
                .firstName(u.getFirstName())
                .lastName(u.getLastName())
                .language(u.getLanguage())
                .tier(u.getTier().name())
                .status(u.getStatus().name())
                .createdAt(u.getCreatedAt())
                .updatedAt(u.getUpdatedAt())
                .build();
    }
}
