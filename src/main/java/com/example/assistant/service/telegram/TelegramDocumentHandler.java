package com.example.assistant.service.telegram;

import com.example.assistant.config.TelegramConfig;
import com.example.assistant.dto.telegram.TelegramDocument;
import com.example.assistant.entity.BotMode;
import com.example.assistant.entity.User;
import com.example.assistant.service.ai.AIService;
import com.example.assistant.service.document.DocumentExtractionService;
import com.example.assistant.service.user.UserSettingsService;
import com.example.assistant.util.MarkdownUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramDocumentHandler {

    private final TelegramClient telegramClient;
    private final TelegramMessageSender messageSender;
    private final DocumentExtractionService documentExtractionService;
    private final AIService aiService;
    private final UserSettingsService userSettingsService;
    private final TelegramConfig telegramConfig;
    private final RedisTemplate<String, Object> redisTemplate;

    private final ConcurrentHashMap<Long, String> localDocumentCache = new ConcurrentHashMap<>();

    @Async("telegramTaskExecutor")
    public void handleDocumentUpload(User user, Long chatId, TelegramDocument document, String caption) {
        if (document == null) return;

        if (document.getFileSize() != null && document.getFileSize() > telegramConfig.getMaxFileSizeBytes()) {
            messageSender.sendMessageHtml(chatId, "⚠️ <b>File too large!</b> Maximum allowed size is 10MB.");
            return;
        }

        Long placeholderMsgId = messageSender.sendProcessingMessage(chatId);
        messageSender.sendTyping(chatId);

        try {
            InputStream stream = telegramClient.downloadTelegramFile(document.getFileId());
            String extractedText = documentExtractionService.extractText(stream, document.getFileName());

            if (extractedText.isBlank()) {
                messageSender.editMessageHtml(chatId, placeholderMsgId, "⚠️ <b>Could not extract text from this document.</b> Please verify the file content.");
                return;
            }

            // Cache document text for user for 2 hours for Q&A
            saveUserDocumentContext(user.getTelegramUserId(), extractedText);
            userSettingsService.setActiveMode(user, BotMode.DOCUMENT);

            // Generate initial summary
            String summary = aiService.summarize(user, extractedText);
            String formattedHtml = """
                    📄 <b>Document Analyzed: %s</b>
                    
                    %s
                    
                    💡 <i>You can now ask questions about this document directly!</i>
                    """.formatted(
                    MarkdownUtils.escapeHtml(document.getFileName()),
                    MarkdownUtils.markdownToTelegramHtml(summary)
            );

            messageSender.editMessageHtml(chatId, placeholderMsgId, formattedHtml);

        } catch (Exception e) {
            log.error("Failed to process document {}: {}", document.getFileName(), e.getMessage(), e);
            messageSender.editMessageHtml(chatId, placeholderMsgId, "😅 <b>Sorry bro, failed to process your document:</b> " + MarkdownUtils.escapeHtml(e.getMessage()));
        }
    }

    public String getUserDocumentContext(Long telegramUserId) {
        try {
            Object val = redisTemplate.opsForValue().get("doc_context:" + telegramUserId);
            if (val != null) return val.toString();
        } catch (Exception e) {
            log.debug("Redis doc context fetch fallback to in-memory: {}", e.getMessage());
        }
        return localDocumentCache.get(telegramUserId);
    }

    private void saveUserDocumentContext(Long telegramUserId, String text) {
        localDocumentCache.put(telegramUserId, text);
        try {
            redisTemplate.opsForValue().set("doc_context:" + telegramUserId, text, Duration.ofHours(2));
        } catch (Exception e) {
            log.debug("Redis doc context save fallback to local memory: {}", e.getMessage());
        }
    }
}
