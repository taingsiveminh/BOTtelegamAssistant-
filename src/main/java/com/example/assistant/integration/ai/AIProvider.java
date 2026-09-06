package com.example.assistant.integration.ai;

import com.example.assistant.dto.ai.AiMessageDto;
import com.example.assistant.dto.ai.AiOptions;
import com.example.assistant.dto.ai.AiResponseDto;

import java.util.List;

public interface AIProvider {

    /**
     * Name/identifier of this AI provider (e.g., "openai", "claude", "gemini", "ollama")
     */
    String getProviderName();

    /**
     * Generates a chat completion response from the AI model.
     */
    AiResponseDto generateResponse(List<AiMessageDto> messages, AiOptions options);
}
