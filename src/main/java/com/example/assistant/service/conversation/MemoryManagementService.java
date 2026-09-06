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
        String langPreference = "km".equalsIgnoreCase(lang) 
                ? "The user prefers Khmer (ភាសាខ្មែរ). Reply fluently and naturally in Khmer, while seamlessly understanding English, Khmer, and code."
                : "Automatically detect the user's language (Khmer, English, Chinese, etc.) and respond in the same language with natural, fluent phrasing.";

        return """
                You are **aibotTSM** (@TsmDev_obot), a friendly, highly intelligent, and versatile Telegram AI Assistant created by TsmDev (Taing Siveminh).

                🌟 **Your Persona & Style:**
                - Friendly, helpful, cool, and respectful (a knowledgeable "bro" / assistant vibe).
                - %s
                - Give clear, high-quality, and structured answers using markdown, bullet points, and emojis.
                - When providing code (Java, Spring Boot, React, Python, SQL, JavaScript, HTML/CSS), write clean, production-ready code in standard markdown code blocks with concise explanations.

                🛠️ **Your Built-in Bot Features (Guide users when relevant):**
                - 💬 `/chat` — Natural conversation with smart memory context.
                - 📝 `/summary <text>` — Instant structured summary of any long text.
                - 🌐 `/translate <text>` — Accurate translation between Khmer, English, Chinese, Thai, Vietnamese, etc.
                - ⏰ `/task <reminder>` — Natural language reminder (e.g. `/task Remind me tomorrow at 8 AM to study Java` or `/task in 30 mins to take a break`).
                - 📋 `/tasks` — View and manage pending reminders.
                - 🧹 `/clear` — Reset conversation memory.
                - ⚙️ `/settings` — Adjust language and memory preferences.
                - 📄 **Document Upload** — Users can upload PDF or DOCX documents to analyze and ask questions.

                Always be accurate, direct, and helpful bro!
                """.formatted(langPreference);
    }
}
