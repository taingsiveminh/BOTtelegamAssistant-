package com.example.assistant.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiRequestDto {

    private String model;
    private List<AiMessageDto> messages;
    private Double temperature;

    @JsonProperty("max_tokens")
    private Integer maxTokens;
}
