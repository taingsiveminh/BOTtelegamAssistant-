package com.example.assistant.service.conversation;

import com.example.assistant.config.AiConfig;
import com.example.assistant.dto.ai.AiMessageDto;
import com.example.assistant.entity.Conversation;
import com.example.assistant.entity.Message;
import com.example.assistant.entity.UserSettings;
import com.example.assistant.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemoryManagementService {

    private final MessageRepository messageRepository;
    private final AiConfig aiConfig;

    public List<AiMessageDto> buildContextMessages(Conversation conversation, UserSettings settings, String userPrompt, String customSystemPrompt) {
        List<AiMessageDto> messages = new ArrayList<>();

        // 1. System Prompt
        String systemInstruction = (customSystemPrompt != null && !customSystemPrompt.isBlank())
                ? customSystemPrompt
                : buildDefaultSystemPrompt(settings);
        messages.add(new AiMessageDto("system", systemInstruction));

        // If memory is disabled in user settings, only provide current prompt
        if (settings != null && !settings.isMemoryEnabled()) {
            messages.add(new AiMessageDto("user", userPrompt));
            return messages;
        }

        // 2. Summary of past conversation if available
        if (conversation.getSummary() != null && !conversation.getSummary().isBlank()) {
            messages.add(new AiMessageDto("system", "Context summary of previous conversation: " + conversation.getSummary()));
        }

        // 3. Sliding window of recent messages
        int maxRecent = aiConfig.getMemoryMaxMessages();
        List<Message> recent = messageRepository.findRecentMessages(conversation.getId(), PageRequest.of(0, maxRecent));
        
        // Reverse so they are in chronological order
        List<Message> chronological = new ArrayList<>(recent);
        Collections.reverse(chronological);

        for (Message msg : chronological) {
            String role = msg.getRole().name().toLowerCase();
            messages.add(new AiMessageDto(role, msg.getContent()));
        }

        // 4. Current user message
        messages.add(new AiMessageDto("user", userPrompt));

        return messages;
    }

    private String buildDefaultSystemPrompt(UserSettings settings) {
        String lang = (settings != null && settings.getLanguage() != null) ? settings.getLanguage() : "en";
        String langInstruction = "km".equalsIgnoreCase(lang) 
                ? "The user prefers Khmer (ភាសាខ្មែរ). Respond naturally in Khmer, but understand both Khmer and English."
                : "The assistant supports English, Khmer, and other languages naturally. Automatically detect the user's language and respond in that same language unless instructed otherwise.";

        return """
                You are a smart, professional, friendly, and helpful Telegram AI Assistant.
                %s
                Be concise, helpful, and polite. When presenting code, format with markdown code blocks.
                """.formatted(langInstruction);
    }
}
