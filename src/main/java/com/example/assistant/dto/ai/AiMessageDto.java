package com.example.assistant.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiMessageDto {
    private String role; // "system", "user", "assistant"
    private String content;
}
