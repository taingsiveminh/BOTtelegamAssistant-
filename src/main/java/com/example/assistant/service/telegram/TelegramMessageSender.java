package com.example.assistant.service.telegram;

import com.example.assistant.util.MarkdownUtils;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramMessageSender {

    private final TelegramClient telegramClient;

    public Long sendProcessingMessage(Long chatId) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("chat_id", chatId);
            payload.put("text", "⏳ <i>Bro, I'm thinking and processing your request...</i>");
            payload.put("parse_mode", "HTML");
            JsonNode res = telegramClient.sendMessage(payload);
            if (res != null && res.has("result") && res.get("result").has("message_id")) {
                return res.get("result").get("message_id").asLong();
            }
        } catch (Exception e) {
            log.error("Failed to send processing placeholder message: {}", e.getMessage());
        }
        return null;
    }

    public void sendMessage(Long chatId, String text) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("chat_id", chatId);
        payload.put("text", MarkdownUtils.truncate(text, 4000));
        telegramClient.sendMessage(payload);
    }

    public void sendMessageHtml(Long chatId, String htmlText) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("chat_id", chatId);
        payload.put("text", MarkdownUtils.truncate(htmlText, 4000));
        payload.put("parse_mode", "HTML");
        try {
            telegramClient.sendMessage(payload);
        } catch (Exception ex) {
            log.warn("HTML send failed, falling back to plain text: {}", ex.getMessage());
            sendMessage(chatId, htmlText);
        }
    }

    public void sendMessageWithKeyboard(Long chatId, String text, Map<String, Object> keyboard) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("chat_id", chatId);
        payload.put("text", MarkdownUtils.truncate(text, 4000));
        if (keyboard != null) {
            payload.put("reply_markup", keyboard);
        }
        telegramClient.sendMessage(payload);
    }

    public void sendMessageHtmlWithKeyboard(Long chatId, String htmlText, Map<String, Object> keyboard) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("chat_id", chatId);
        payload.put("text", MarkdownUtils.truncate(htmlText, 4000));
        payload.put("parse_mode", "HTML");
        if (keyboard != null) {
            payload.put("reply_markup", keyboard);
        }
        try {
            telegramClient.sendMessage(payload);
        } catch (Exception ex) {
            log.warn("HTML keyboard send failed, falling back to plain text: {}", ex.getMessage());
            sendMessageWithKeyboard(chatId, htmlText, keyboard);
        }
    }

    public void editMessage(Long chatId, Long messageId, String text) {
        if (messageId == null) {
            sendMessage(chatId, text);
            return;
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("chat_id", chatId);
        payload.put("message_id", messageId);
        payload.put("text", MarkdownUtils.truncate(text, 4000));
        telegramClient.editMessageText(payload);
    }

    public void editMessageHtml(Long chatId, Long messageId, String htmlText) {
        if (messageId == null) {
            sendMessageHtml(chatId, htmlText);
            return;
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("chat_id", chatId);
        payload.put("message_id", messageId);
        payload.put("text", MarkdownUtils.truncate(htmlText, 4000));
        payload.put("parse_mode", "HTML");
        try {
            telegramClient.editMessageText(payload);
        } catch (Exception ex) {
            log.warn("Edit HTML failed, falling back to plain text edit: {}", ex.getMessage());
            editMessage(chatId, messageId, htmlText);
        }
    }

    public void sendTyping(Long chatId) {
        telegramClient.sendChatAction(chatId, "typing");
    }
}
