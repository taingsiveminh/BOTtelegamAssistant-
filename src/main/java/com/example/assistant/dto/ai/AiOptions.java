package com.example.assistant.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiOptions {
    private String model;
    private Double temperature;
    private Integer maxTokens;
    private String systemPrompt;
    @Builder.Default
    private String feature = "CHAT";
}
