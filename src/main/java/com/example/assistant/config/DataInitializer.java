package com.example.assistant.config;

import com.example.assistant.entity.*;
import com.example.assistant.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UsageRecordRepository usageRecordRepository;
    private final AiCostLogRepository aiCostLogRepository;
    private final TaskRepository taskRepository;

    @Override
    public void run(String... args) {
        log.info("Seeding demo data for preview dashboard...");

        // User 1: Sokha (Khmer)
        User u1 = userRepository.save(User.builder()
                .telegramUserId(1001L)
                .username("sokha_dev")
                .firstName("Sokha")
                .lastName("Chorn")
                .language("km")
                .tier(UserTier.PREMIUM)
                .status(UserStatus.ACTIVE)
                .build());

        userSettingsRepository.save(UserSettings.builder()
                .user(u1)
                .language("km")
                .memoryEnabled(true)
                .notificationsEnabled(true)
                .activeMode(BotMode.CHAT)
                .build());

        // User 2: Sarah (English)
        User u2 = userRepository.save(User.builder()
                .telegramUserId(1002L)
                .username("sarah_smith")
                .firstName("Sarah")
                .lastName("Smith")
                .language("en")
                .tier(UserTier.FREE)
                .status(UserStatus.ACTIVE)
                .build());

        userSettingsRepository.save(UserSettings.builder()
                .user(u2)
                .language("en")
                .memoryEnabled(true)
                .notificationsEnabled(true)
                .activeMode(BotMode.CODING)
                .build());

        // User 3: Ming (Chinese)
        User u3 = userRepository.save(User.builder()
                .telegramUserId(1003L)
                .username("ming_chen")
                .firstName("Ming")
                .lastName("Chen")
                .language("zh")
                .tier(UserTier.FREE)
                .status(UserStatus.BLOCKED)
                .build());

        userSettingsRepository.save(UserSettings.builder()
                .user(u3)
                .language("zh")
                .memoryEnabled(false)
                .notificationsEnabled(false)
                .activeMode(BotMode.TRANSLATE)
                .build());

        // Usage records for last 7 days
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            usageRecordRepository.save(UsageRecord.builder()
                    .user(u1)
                    .usageDate(d)
                    .requestCount(15 + (i * 3))
                    .tokenUsage((15 + (i * 3)) * 850L)
                    .dailyCost(BigDecimal.valueOf(0.015 * (i + 1)))
                    .build());

            usageRecordRepository.save(UsageRecord.builder()
                    .user(u2)
                    .usageDate(d)
                    .requestCount(8 + (i * 2))
                    .tokenUsage((8 + (i * 2)) * 600L)
                    .dailyCost(BigDecimal.valueOf(0.008 * (i + 1)))
                    .build());
        }

        // Cost logs
        aiCostLogRepository.save(AiCostLog.builder()
                .user(u1)
                .model("gpt-4o-mini")
                .promptTokens(12500)
                .completionTokens(6400)
                .totalTokens(18900)
                .estimatedCost(BigDecimal.valueOf(0.057))
                .latencyMs(1240)
                .feature("CHAT")
                .build());

        aiCostLogRepository.save(AiCostLog.builder()
                .user(u2)
                .model("gpt-4o")
                .promptTokens(8500)
                .completionTokens(4200)
                .totalTokens(12700)
                .estimatedCost(BigDecimal.valueOf(0.063))
                .latencyMs(2100)
                .feature("CODING")
                .build());

        // Tasks
        taskRepository.save(Task.builder()
                .user(u1)
                .title("Study Spring Boot JPA & Flyway")
                .scheduledAt(OffsetDateTime.now().plusHours(2))
                .status(TaskStatus.PENDING)
                .build());

        taskRepository.save(Task.builder()
                .user(u2)
                .title("Submit Assignment PDF")
                .scheduledAt(OffsetDateTime.now().plusDays(1))
                .status(TaskStatus.PENDING)
                .build());

        log.info("Demo data seeded successfully.");
    }
}
