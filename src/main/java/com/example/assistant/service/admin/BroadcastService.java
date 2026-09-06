package com.example.assistant.service.admin;

import com.example.assistant.dto.admin.BroadcastRequestDto;
import com.example.assistant.dto.admin.BroadcastResultDto;
import com.example.assistant.entity.User;
import com.example.assistant.entity.UserStatus;
import com.example.assistant.entity.UserTier;
import com.example.assistant.repository.UserRepository;
import com.example.assistant.service.telegram.TelegramMessageSender;
import com.example.assistant.util.MarkdownUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class BroadcastService {

    private final UserRepository userRepository;
    private final TelegramMessageSender messageSender;

    @Async("telegramTaskExecutor")
    public CompletableFuture<BroadcastResultDto> broadcastMessage(BroadcastRequestDto request) {
        log.info("Starting broadcast dispatch for tier: {}", request.getTargetTier());

        List<User> targetUsers = userRepository.findAllByStatus(UserStatus.ACTIVE);

        if (request.getTargetTier() != null && !request.getTargetTier().equalsIgnoreCase("ALL")) {
            UserTier tier = UserTier.valueOf(request.getTargetTier().toUpperCase());
            targetUsers = targetUsers.stream().filter(u -> u.getTier() == tier).toList();
        }

        int total = targetUsers.size();
        int success = 0;
        int failed = 0;

        String broadcastHtml = "📢 <b>Announcement:</b>\n\n" + MarkdownUtils.markdownToTelegramHtml(request.getMessage());

        for (User user : targetUsers) {
            try {
                messageSender.sendMessageHtml(user.getTelegramUserId(), broadcastHtml);
                success++;
                // Sleep 35ms between sends to stay comfortably under Telegram's 30 msg/sec global broadcast limit
                Thread.sleep(35);
            } catch (Exception e) {
                log.error("Failed to send broadcast to user {}: {}", user.getTelegramUserId(), e.getMessage());
                failed++;
            }
        }

        log.info("Broadcast finished: {} total, {} succeeded, {} failed", total, success, failed);
        BroadcastResultDto result = BroadcastResultDto.builder()
                .totalTargeted(total)
                .successfullySent(success)
                .failed(failed)
                .status("COMPLETED")
                .build();

        return CompletableFuture.completedFuture(result);
    }
}
