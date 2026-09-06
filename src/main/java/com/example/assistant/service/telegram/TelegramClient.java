package com.example.assistant.service.telegram;

import com.example.assistant.config.TelegramConfig;
import com.example.assistant.dto.telegram.TelegramUpdate;
import com.example.assistant.exception.TelegramBotException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class TelegramClient {

    private final TelegramConfig telegramConfig;
    private final ObjectMapper objectMapper;

    private String getApiBaseUrl() {
        return "https://api.telegram.org/bot" + telegramConfig.getBotToken();
    }

    private String getFileBaseUrl() {
        return "https://api.telegram.org/file/bot" + telegramConfig.getBotToken();
    }

    public JsonNode sendMessage(Map<String, Object> payload) {
        if (telegramConfig.getBotToken() == null || telegramConfig.getBotToken().isBlank()) {
            log.warn("TELEGRAM_BOT_TOKEN is not configured. Mocking sendMessage response.");
            return objectMapper.createObjectNode().put("ok", true);
        }

        try {
            RestClient restClient = RestClient.builder().baseUrl(getApiBaseUrl()).build();
            return restClient.post()
                    .uri("/sendMessage")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (Exception ex) {
            log.error("Failed to execute sendMessage: {}", ex.getMessage());
            throw new TelegramBotException("Error calling Telegram sendMessage: " + ex.getMessage(), ex);
        }
    }

    public JsonNode editMessageText(Map<String, Object> payload) {
        if (telegramConfig.getBotToken() == null || telegramConfig.getBotToken().isBlank()) {
            return objectMapper.createObjectNode().put("ok", true);
        }

        try {
            RestClient restClient = RestClient.builder().baseUrl(getApiBaseUrl()).build();
            return restClient.post()
                    .uri("/editMessageText")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (Exception ex) {
            log.error("Failed to execute editMessageText: {}", ex.getMessage());
            return objectMapper.createObjectNode().put("ok", false);
        }
    }

    public void sendChatAction(Long chatId, String action) {
        if (telegramConfig.getBotToken() == null || telegramConfig.getBotToken().isBlank()) {
            return;
        }

        try {
            RestClient restClient = RestClient.builder().baseUrl(getApiBaseUrl()).build();
            restClient.post()
                    .uri("/sendChatAction")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("chat_id", chatId, "action", action))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception ex) {
            log.debug("sendChatAction error: {}", ex.getMessage());
        }
    }

    public List<TelegramUpdate> getUpdates(long offset, int timeoutSeconds) {
        if (telegramConfig.getBotToken() == null || telegramConfig.getBotToken().isBlank()) {
            return Collections.emptyList();
        }

        try {
            RestClient restClient = RestClient.builder().baseUrl(getApiBaseUrl()).build();
            JsonNode root = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/getUpdates")
                            .queryParam("offset", offset)
                            .queryParam("timeout", timeoutSeconds)
                            .build())
                    .retrieve()
                    .body(JsonNode.class);

            if (root != null && root.has("ok") && root.get("ok").asBoolean() && root.has("result")) {
                return objectMapper.readerForListOf(TelegramUpdate.class).readValue(root.get("result"));
            }
        } catch (Exception ex) {
            log.error("Failed to get Telegram updates: {}", ex.getMessage());
        }
        return Collections.emptyList();
    }

    public InputStream downloadTelegramFile(String fileId) {
        try {
            RestClient restClient = RestClient.builder().baseUrl(getApiBaseUrl()).build();
            JsonNode fileInfoNode = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/getFile").queryParam("file_id", fileId).build())
                    .retrieve()
                    .body(JsonNode.class);

            if (fileInfoNode != null && fileInfoNode.has("result") && fileInfoNode.get("result").has("file_path")) {
                String filePath = fileInfoNode.get("result").get("file_path").asText();
                String fileDownloadUrl = getFileBaseUrl() + "/" + filePath;
                return URI.create(fileDownloadUrl).toURL().openStream();
            }
        } catch (Exception ex) {
            log.error("Failed to download Telegram file {}: {}", fileId, ex.getMessage());
            throw new TelegramBotException("Could not download file: " + ex.getMessage(), ex);
        }
        throw new TelegramBotException("File path not found for file ID: " + fileId);
    }
}
