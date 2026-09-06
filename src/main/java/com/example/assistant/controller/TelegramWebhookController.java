package com.example.assistant.controller;

import com.example.assistant.config.TelegramConfig;
import com.example.assistant.dto.telegram.TelegramUpdate;
import com.example.assistant.service.telegram.TelegramBotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/telegram")
@RequiredArgsConstructor
@Slf4j
public class TelegramWebhookController {

    private final TelegramBotService telegramBotService;
    private final TelegramConfig telegramConfig;

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestHeader(value = "X-Telegram-Bot-Api-Secret-Token", required = false) String secretToken,
            @RequestBody TelegramUpdate update) {

        // Validate webhook secret token if configured
        if (telegramConfig.getSecretToken() != null && !telegramConfig.getSecretToken().isBlank()) {
            if (!telegramConfig.getSecretToken().equals(secretToken)) {
                log.warn("Unauthorized webhook request with invalid secret token!");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid secret token");
            }
        }

        telegramBotService.processUpdate(update);
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "botUsername", telegramConfig.getBotUsername(),
                "mode", telegramConfig.getMode()
        ));
    }
}
