package com.example.assistant.service.telegram;

import com.example.assistant.config.TelegramConfig;
import com.example.assistant.dto.telegram.TelegramUpdate;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramLongPollingService {

    private final TelegramConfig telegramConfig;
    private final TelegramClient telegramClient;
    private final TelegramBotService telegramBotService;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private ExecutorService pollingExecutor;

    @PostConstruct
    public void startPollingIfEnabled() {
        if ("polling".equalsIgnoreCase(telegramConfig.getMode())) {
            if (telegramConfig.getBotToken() == null || telegramConfig.getBotToken().isBlank() || "demo_token".equalsIgnoreCase(telegramConfig.getBotToken())) {
                log.info("Telegram bot token not configured or demo token used. Long polling disabled.");
                return;
            }

            log.info("Starting Telegram Long Polling service...");
            running.set(true);
            pollingExecutor = Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "TelegramLongPolling");
                t.setDaemon(true);
                return t;
            });

            pollingExecutor.submit(this::pollLoop);
        } else {
            log.info("Telegram running in Webhook mode (polling disabled).");
        }
    }

    private void pollLoop() {
        long offset = 0;
        while (running.get()) {
            try {
                List<TelegramUpdate> updates = telegramClient.getUpdates(offset, 20);
                for (TelegramUpdate update : updates) {
                    if (update.getUpdateId() != null) {
                        offset = Math.max(offset, update.getUpdateId() + 1);
                    }
                    telegramBotService.processUpdate(update);
                }
            } catch (Exception e) {
                log.error("Error in Telegram poll loop: {}", e.getMessage());
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    @PreDestroy
    public void stopPolling() {
        running.set(false);
        if (pollingExecutor != null) {
            pollingExecutor.shutdownNow();
        }
    }
}
