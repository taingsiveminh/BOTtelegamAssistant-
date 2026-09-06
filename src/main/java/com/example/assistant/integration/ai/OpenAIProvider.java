package com.example.assistant.integration.ai;

import com.example.assistant.config.AiConfig;
import com.example.assistant.dto.ai.AiMessageDto;
import com.example.assistant.dto.ai.AiOptions;
import com.example.assistant.dto.ai.AiRequestDto;
import com.example.assistant.dto.ai.AiResponseDto;
import com.example.assistant.dto.ai.AiUsageDto;
import com.example.assistant.exception.AiProviderException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OpenAIProvider implements AIProvider {

    private final AiConfig aiConfig;

    @Override
    public String getProviderName() {
        return "openai";
    }

    @Override
    public AiResponseDto generateResponse(List<AiMessageDto> messages, AiOptions options) {
        String apiKey = aiConfig.getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("AI_API_KEY is not set. Returning simulated assistant response.");
            AiResponseDto fallbackResponse = new AiResponseDto();
            fallbackResponse.setModel(options.getModel() != null ? options.getModel() : aiConfig.getModel());
            
            AiResponseDto.Choice choice = new AiResponseDto.Choice();
            choice.setIndex(0);
            choice.setMessage(new AiMessageDto("assistant", 
                    "🤖 [Demo Mode] AI_API_KEY is not configured yet in the server environment. Please set AI_API_KEY to enable live OpenAI/DeepSeek completions!"));
            
            AiUsageDto usage = new AiUsageDto(50, 25, 75);
            fallbackResponse.setChoices(List.of(choice));
            fallbackResponse.setUsage(usage);
            return fallbackResponse;
        }

        String url = aiConfig.getBaseUrl() + "/chat/completions";
        String model = (options != null && options.getModel() != null) ? options.getModel() : aiConfig.getModel();
        Double temperature = (options != null && options.getTemperature() != null) ? options.getTemperature() : aiConfig.getTemperature();
        Integer maxTokens = (options != null && options.getMaxTokens() != null) ? options.getMaxTokens() : aiConfig.getMaxTokens();

        AiRequestDto request = AiRequestDto.builder()
                .model(model)
                .messages(messages)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .build();

        try {
            RestClient restClient = RestClient.builder()
                    .baseUrl(aiConfig.getBaseUrl())
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();

            return restClient.post()
                    .uri("/chat/completions")
                    .body(request)
                    .retrieve()
                    .body(AiResponseDto.class);
        } catch (Exception ex) {
            log.error("Failed to execute OpenAI chat completion: {}", ex.getMessage(), ex);
            throw new AiProviderException("Error communicating with OpenAI provider: " + ex.getMessage(), ex);
        }
    }
}
