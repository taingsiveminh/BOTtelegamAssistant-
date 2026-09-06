package com.example.assistant.dto.telegram;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TelegramMessage {

    @JsonProperty("message_id")
    private Long messageId;

    @JsonProperty("from")
    private TelegramUser from;

    @JsonProperty("chat")
    private TelegramChat chat;

    @JsonProperty("date")
    private Long date;

    @JsonProperty("text")
    private String text;

    @JsonProperty("caption")
    private String caption;

    @JsonProperty("document")
    private TelegramDocument document;
}
