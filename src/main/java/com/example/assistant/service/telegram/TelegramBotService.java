package com.example.assistant.service.telegram;

import com.example.assistant.dto.telegram.TelegramMessage;
import com.example.assistant.dto.telegram.TelegramUpdate;
import com.example.assistant.dto.telegram.TelegramUser;
import com.example.assistant.entity.BotMode;
import com.example.assistant.entity.Task;
import com.example.assistant.entity.User;
import com.example.assistant.entity.UserSettings;
import com.example.assistant.entity.UserStatus;
import com.example.assistant.exception.QuotaExceededException;
import com.example.assistant.exception.RateLimitExceededException;
import com.example.assistant.service.ai.AIService;
import com.example.assistant.service.task.TaskService;
import com.example.assistant.service.usage.RateLimitService;
import com.example.assistant.service.user.UserService;
import com.example.assistant.service.user.UserSettingsService;
import com.example.assistant.util.DateTimeUtils;
import com.example.assistant.util.MarkdownUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramBotService {

    private final UserService userService;
    private final UserSettingsService userSettingsService;
    private final TelegramCommandHandler commandHandler;
    private final TelegramCallbackHandler callbackHandler;
    private final TelegramDocumentHandler documentHandler;
    private final TelegramMessageSender messageSender;
    private final RateLimitService rateLimitService;
    private final AIService aiService;
    private final TaskService taskService;

    public void processUpdate(TelegramUpdate update) {
        if (update == null) return;

        try {
            if (update.getCallbackQuery() != null) {
                TelegramUser tgUser = update.getCallbackQuery().getFrom();
                User user = userService.registerOrUpdateUser(tgUser);
                if (user.getStatus() == UserStatus.BLOCKED) {
                    messageSender.sendMessage(update.getCallbackQuery().getMessage().getChat().getId(), "🚫 Your account is blocked.");
                    return;
                }
                callbackHandler.handleCallback(user, update.getCallbackQuery());
                return;
            }

            TelegramMessage msg = update.getMessage();
            if (msg == null) {
                msg = update.getEditedMessage();
            }
            if (msg == null || msg.getFrom() == null) return;

            User user = userService.registerOrUpdateUser(msg.getFrom());
            if (user.getStatus() == UserStatus.BLOCKED) {
                messageSender.sendMessage(msg.getChat().getId(), "🚫 Your account is suspended. Please contact admin.");
                return;
            }

            Long chatId = msg.getChat().getId();

            // Check rate limiting
            try {
                rateLimitService.checkRateLimit(user.getTelegramUserId());
            } catch (RateLimitExceededException rle) {
                messageSender.sendMessage(chatId, "⚠️ " + rle.getMessage());
                return;
            }

            // Document handling
            if (msg.getDocument() != null) {
                documentHandler.handleDocumentUpload(user, chatId, msg.getDocument(), msg.getCaption());
                return;
            }

            String text = msg.getText();
            if (text == null || text.isBlank()) return;

            // Command handling
            if (text.startsWith("/")) {
                handleCommand(user, chatId, text.trim());
                return;
            }

            // Normal text processing asynchronously
            processTextMessageAsync(user, chatId, text.trim());

        } catch (Exception ex) {
            log.error("Error processing update {}: {}", update.getUpdateId(), ex.getMessage(), ex);
        }
    }

    private void handleCommand(User user, Long chatId, String fullCommand) {
        String[] parts = fullCommand.split("\\s+", 2);
        String command = parts[0].toLowerCase();
        String arg = parts.length > 1 ? parts[1] : null;

        switch (command) {
            case "/start" -> commandHandler.handleStart(user, chatId);
            case "/help" -> commandHandler.handleHelp(chatId);
            case "/menu" -> commandHandler.handleMenu(chatId);
            case "/chat" -> {
                userSettingsService.setActiveMode(user, BotMode.CHAT);
                messageSender.sendMessageHtml(chatId, "💬 <b>Chat Mode Activated!</b> Ask me anything.");
            }
            case "/clear" -> commandHandler.handleClear(user, chatId);
            case "/summary" -> {
                if (arg != null && !arg.isBlank()) {
                    processSummaryAsync(user, chatId, arg);
                } else {
                    userSettingsService.setActiveMode(user, BotMode.SUMMARY);
                    messageSender.sendMessageHtml(chatId, "📝 <b>Summarizer Mode:</b> Please send the text you'd like summarized.");
                }
            }
            case "/translate" -> {
                if (arg != null && !arg.isBlank()) {
                    processTranslateAsync(user, chatId, arg, "English");
                } else {
                    messageSender.sendMessageHtmlWithKeyboard(chatId, "🌐 <b>Select your target translation language:</b>", TelegramKeyboards.createLanguagePickerKeyboard("trans_lang_"));
                }
            }
            case "/task" -> {
                if (arg != null && !arg.isBlank()) {
                    createTaskAndRespond(user, chatId, arg);
                } else {
                    userSettingsService.setActiveMode(user, BotMode.TASK);
                    messageSender.sendMessageHtml(chatId, "⏰ <b>Create Reminder:</b> Type your reminder, e.g. <i>Remind me tomorrow at 8 AM to study Java</i>");
                }
            }
            case "/tasks" -> commandHandler.handleTasks(user, chatId);
            case "/settings" -> commandHandler.handleSettings(user, chatId);
            case "/admin" -> commandHandler.handleAdmin(chatId);
            default -> messageSender.sendMessageHtml(chatId, "❓ Unknown command. Type /help to see all available commands.");
        }
    }

    @Async("telegramTaskExecutor")
    public void processTextMessageAsync(User user, Long chatId, String text) {
        UserSettings settings = userSettingsService.getOrCreateSettings(user);
        BotMode mode = settings.getActiveMode();

        if (mode == BotMode.TASK) {
            createTaskAndRespond(user, chatId, text);
            return;
        }

        Long placeholderId = messageSender.sendProcessingMessage(chatId);
        messageSender.sendTyping(chatId);

        try {
            String answer;
            switch (mode) {
                case SUMMARY -> answer = aiService.summarize(user, text);
                case TRANSLATE -> answer = aiService.translate(user, text, "English and Khmer");
                case CODING -> answer = aiService.helpCoding(user, text);
                case DOCUMENT -> {
                    String docContext = documentHandler.getUserDocumentContext(user.getTelegramUserId());
                    if (docContext != null && !docContext.isBlank()) {
                        answer = aiService.answerDocumentQuestion(user, docContext, text);
                    } else {
                        answer = "⚠️ No document found in your active session. Please upload a PDF or DOCX first!";
                    }
                }
                default -> answer = aiService.processChat(user, text);
            }

            String htmlOutput = MarkdownUtils.markdownToTelegramHtml(answer);
            messageSender.editMessageHtml(chatId, placeholderId, htmlOutput);

        } catch (QuotaExceededException qe) {
            messageSender.editMessageHtml(chatId, placeholderId, "⚠️ " + MarkdownUtils.escapeHtml(qe.getMessage()));
        } catch (com.example.assistant.exception.AiProviderException aie) {
            log.warn("AI Provider exception: {}", aie.getMessage());
            messageSender.editMessageHtml(chatId, placeholderId, "⚠️ <b>AI Service Notice:</b>\n" + MarkdownUtils.escapeHtml(aie.getMessage()));
        } catch (Exception ex) {
            log.error("Error executing AI response for user {}: {}", user.getTelegramUserId(), ex.getMessage(), ex);
            messageSender.editMessageHtml(chatId, placeholderId, "😅 <b>Sorry bro,</b> something went wrong while processing your AI request. Please try again.");
        }
    }

    @Async("telegramTaskExecutor")
    public void processSummaryAsync(User user, Long chatId, String text) {
        Long placeholderId = messageSender.sendProcessingMessage(chatId);
        messageSender.sendTyping(chatId);
        try {
            String summary = aiService.summarize(user, text);
            messageSender.editMessageHtml(chatId, placeholderId, "📝 <b>Summary:</b>\n\n" + MarkdownUtils.markdownToTelegramHtml(summary));
        } catch (Exception ex) {
            messageSender.editMessageHtml(chatId, placeholderId, "😅 Failed to summarize: " + MarkdownUtils.escapeHtml(ex.getMessage()));
        }
    }

    @Async("telegramTaskExecutor")
    public void processTranslateAsync(User user, Long chatId, String text, String targetLang) {
        Long placeholderId = messageSender.sendProcessingMessage(chatId);
        messageSender.sendTyping(chatId);
        try {
            String translated = aiService.translate(user, text, targetLang);
            messageSender.editMessageHtml(chatId, placeholderId, "🌐 <b>Translation (" + targetLang + "):</b>\n\n" + MarkdownUtils.markdownToTelegramHtml(translated));
        } catch (Exception ex) {
            messageSender.editMessageHtml(chatId, placeholderId, "😅 Failed to translate: " + MarkdownUtils.escapeHtml(ex.getMessage()));
        }
    }

    private void createTaskAndRespond(User user, Long chatId, String text) {
        try {
            Task task = taskService.createTaskFromNaturalLanguage(user, text);
            String confirm = """
                    ✅ <b>Reminder Scheduled Successfully!</b>
                    
                    📌 <b>Task:</b> %s
                    🕒 <b>Time:</b> <code>%s</code>
                    
                    🔔 I will notify you here when the time arrives!
                    """.formatted(
                    MarkdownUtils.escapeHtml(task.getTitle()),
                    DateTimeUtils.formatHumanReadable(task.getScheduledAt())
            );
            messageSender.sendMessageHtml(chatId, confirm);
        } catch (Exception ex) {
            log.error("Failed to create reminder: {}", ex.getMessage());
            messageSender.sendMessageHtml(chatId, "😅 Could not schedule reminder. Example: <i>/task Remind me tomorrow at 8 AM to study Java</i>");
        }
    }
}
