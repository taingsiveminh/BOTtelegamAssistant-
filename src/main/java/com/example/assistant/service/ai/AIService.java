package com.example.assistant.service.ai;

import com.example.assistant.dto.ai.AiMessageDto;
import com.example.assistant.dto.ai.AiOptions;
import com.example.assistant.dto.ai.AiResponseDto;
import com.example.assistant.entity.Conversation;
import com.example.assistant.entity.User;
import com.example.assistant.entity.UserSettings;
import com.example.assistant.integration.ai.AIProvider;
import com.example.assistant.service.conversation.ConversationService;
import com.example.assistant.service.conversation.MemoryManagementService;
import com.example.assistant.service.usage.UsageTrackingService;
import com.example.assistant.service.user.UserSettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIService {

    private final AIProvider aiProvider;
    private final ConversationService conversationService;
    private final MemoryManagementService memoryManagementService;
    private final UserSettingsService userSettingsService;
    private final UsageTrackingService usageTrackingService;

    public String processChat(User user, String userPrompt) {
        return executeAiRequest(user, userPrompt, null, "CHAT");
    }

    public String summarize(User user, String text) {
        String systemPrompt = """
                You are an expert summarizer. Provide a clean, structured, and concise summary of the provided text.
                Highlight key points with bullet points. Support Khmer and English naturally.
                """;
        return executeAiRequest(user, "Please summarize the following text:\n\n" + text, systemPrompt, "SUMMARY");
    }

    public String translate(User user, String text, String targetLanguage) {
        String systemPrompt = """
                You are a professional multilingual translator. 
                Translate the user's input accurately and naturally into %s.
                Maintain tone and context. Only return the translated text.
                """.formatted(targetLanguage);
        return executeAiRequest(user, text, systemPrompt, "TRANSLATE");
    }

    public String helpCoding(User user, String codingPrompt) {
        String systemPrompt = """
                You are an elite Senior Software Engineer and coding assistant.
                You are proficient in Java, Spring Boot, React, Python, JavaScript, SQL, C#, and HTML/CSS.
                Provide clean, robust, and well-explained code solutions. Always format code using markdown blocks.
                Explain concepts clearly in Khmer or English based on user query language.
                """;
        return executeAiRequest(user, codingPrompt, systemPrompt, "CODING");
    }

    public String generateContent(User user, String prompt) {
        String systemPrompt = """
                You are a creative content creator, copywriter, and marketing specialist.
                Generate engaging, high-converting, and professional content (e.g., social media posts, ads, articles).
                """;
        return executeAiRequest(user, prompt, systemPrompt, "CONTENT_GENERATION");
    }

    public String answerDocumentQuestion(User user, String documentContext, String question) {
        String systemPrompt = """
                You are an intelligent document analyst. You are provided with the text extracted from a user's uploaded document.
                Answer the user's question accurately based ONLY on the document context provided below.
                If the document does not contain the answer, politely state that.
                
                DOCUMENT CONTEXT:
                ---
                %s
                ---
                """.formatted(documentContext);

        return executeAiRequest(user, question, systemPrompt, "DOCUMENT_QNA");
    }

    private String executeAiRequest(User user, String userPrompt, String customSystemPrompt, String feature) {
        // 1. Quota Check
        usageTrackingService.validateUserQuota(user);

        UserSettings settings = userSettingsService.getOrCreateSettings(user);
        Conversation conversation = conversationService.getOrCreateActiveConversation(user);

        // 2. Build Context
        List<AiMessageDto> messages = memoryManagementService.buildContextMessages(
                conversation, settings, userPrompt, customSystemPrompt);

        // 3. Save User Message
        conversationService.saveUserMessage(conversation, userPrompt);

        long startTime = System.currentTimeMillis();
        AiOptions options = AiOptions.builder()
                .feature(feature)
                .build();

        // 4. Call AI Provider
        AiResponseDto response = aiProvider.generateResponse(messages, options);
        long latency = System.currentTimeMillis() - startTime;

        String answer = extractAnswer(response);
        int totalTokens = (response != null && response.getUsage() != null) ? response.getUsage().getTotalTokens() : 100;

        // 5. Save Assistant Message & Record Cost
        conversationService.saveAssistantMessage(conversation, answer, totalTokens);
        usageTrackingService.recordUsage(user, response, feature, latency);

        return answer;
    }

    private String extractAnswer(AiResponseDto response) {
        if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()) {
            AiResponseDto.Choice choice = response.getChoices().get(0);
            if (choice.getMessage() != null && choice.getMessage().getContent() != null) {
                return choice.getMessage().getContent();
            }
        }
        return "😅 Sorry bro, I could not generate a response. Please try again.";
    }
}
