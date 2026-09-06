package com.example.assistant.service.conversation;

import com.example.assistant.entity.Conversation;
import com.example.assistant.entity.Message;
import com.example.assistant.entity.MessageRole;
import com.example.assistant.entity.User;
import com.example.assistant.repository.ConversationRepository;
import com.example.assistant.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    @Transactional
    public Conversation getOrCreateActiveConversation(User user) {
        return conversationRepository.findFirstByUserIdAndActiveTrueOrderByUpdatedAtDesc(user.getId())
                .orElseGet(() -> {
                    Conversation newConv = Conversation.builder()
                            .user(user)
                            .title("Conversation started " + java.time.LocalDate.now())
                            .active(true)
                            .build();
                    return conversationRepository.save(newConv);
                });
    }

    @Transactional
    public Message saveUserMessage(Conversation conversation, String content) {
        Message message = Message.builder()
                .conversation(conversation)
                .role(MessageRole.USER)
                .content(content)
                .tokens(content.length() / 4) // approximation
                .build();
        return messageRepository.save(message);
    }

    @Transactional
    public Message saveAssistantMessage(Conversation conversation, String content, int tokens) {
        Message message = Message.builder()
                .conversation(conversation)
                .role(MessageRole.ASSISTANT)
                .content(content)
                .tokens(tokens)
                .build();
        Message saved = messageRepository.save(message);

        conversation.setTotalTokens(conversation.getTotalTokens() + tokens);
        conversationRepository.save(conversation);
        return saved;
    }

    @Transactional
    public void clearConversationMemory(User user) {
        conversationRepository.findFirstByUserIdAndActiveTrueOrderByUpdatedAtDesc(user.getId())
                .ifPresent(conv -> {
                    conv.setActive(false);
                    conversationRepository.save(conv);
                    log.info("Cleared and deactivated conversation {} for user {}", conv.getId(), user.getTelegramUserId());
                });

        // Create fresh new active conversation
        Conversation newConv = Conversation.builder()
                .user(user)
                .title("New Conversation")
                .active(true)
                .build();
        conversationRepository.save(newConv);
    }
}
