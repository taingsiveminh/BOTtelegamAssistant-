package com.example.assistant.dto.telegram;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TelegramUpdate {

    @JsonProperty("update_id")
    private Long updateId;

    @JsonProperty("message")
    private TelegramMessage message;

    @JsonProperty("edited_message")
    private TelegramMessage editedMessage;

    @JsonProperty("callback_query")
    private TelegramCallbackQuery callbackQuery;
}
