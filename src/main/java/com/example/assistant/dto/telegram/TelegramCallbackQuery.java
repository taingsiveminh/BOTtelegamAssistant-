package com.example.assistant.dto.telegram;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TelegramCallbackQuery {

    @JsonProperty("id")
    private String id;

    @JsonProperty("from")
    private TelegramUser from;

    @JsonProperty("message")
    private TelegramMessage message;

    @JsonProperty("data")
    private String data;
}
