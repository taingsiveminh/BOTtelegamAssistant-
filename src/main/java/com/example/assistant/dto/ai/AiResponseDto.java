package com.example.assistant.dto.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiResponseDto {

    private String id;
    private String model;
    private List<Choice> choices;
    private AiUsageDto usage;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Choice {
        private int index;
        private AiMessageDto message;
        private String finishReason;
    }
}
