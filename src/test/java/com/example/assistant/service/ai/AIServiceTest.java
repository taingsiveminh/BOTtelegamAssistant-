package com.example.assistant.service.ai;

import com.example.assistant.dto.ai.AiMessageDto;
import com.example.assistant.dto.ai.AiResponseDto;
import com.example.assistant.dto.ai.AiUsageDto;
import com.example.assistant.entity.Conversation;
import com.example.assistant.entity.User;
import com.example.assistant.entity.UserSettings;
import com.example.assistant.integration.ai.AIProvider;
import com.example.assistant.service.conversation.ConversationService;
import com.example.assistant.service.conversation.MemoryManagementService;
import com.example.assistant.service.usage.UsageTrackingService;
import com.example.assistant.service.user.UserSettingsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AIServiceTest {

    @Mock
    private AIProvider aiProvider;

    @Mock
    private ConversationService conversationService;

    @Mock
    private MemoryManagementService memoryManagementService;

    @Mock
    private UserSettingsService userSettingsService;

    @Mock
    private UsageTrackingService usageTrackingService;

    @InjectMocks
    private AIService aiService;

    private User user;
    private UserSettings settings;
    private Conversation conversation;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).telegramUserId(12345L).build();
        settings = UserSettings.builder().user(user).language("en").build();
        conversation = Conversation.builder().id(10L).user(user).build();
    }

    @Test
    void testProcessChat() {
        when(userSettingsService.getOrCreateSettings(user)).thenReturn(settings);
        when(conversationService.getOrCreateActiveConversation(user)).thenReturn(conversation);
        when(memoryManagementService.buildContextMessages(any(), any(), any(), any()))
                .thenReturn(List.of(new AiMessageDto("user", "Hello")));

        AiResponseDto response = new AiResponseDto();
        AiResponseDto.Choice choice = new AiResponseDto.Choice();
        choice.setMessage(new AiMessageDto("assistant", "Hello! How can I help?"));
        response.setChoices(List.of(choice));
        response.setUsage(new AiUsageDto(10, 20, 30));

        when(aiProvider.generateResponse(any(), any())).thenReturn(response);

        String result = aiService.processChat(user, "Hello");

        assertNotNull(result);
        assertEquals("Hello! How can I help?", result);
        verify(usageTrackingService, times(1)).validateUserQuota(user);
        verify(conversationService, times(1)).saveUserMessage(conversation, "Hello");
        verify(conversationService, times(1)).saveAssistantMessage(eq(conversation), eq("Hello! How can I help?"), eq(30));
    }
}
