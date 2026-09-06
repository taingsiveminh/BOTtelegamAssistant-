package com.example.assistant.service.telegram;

import com.example.assistant.dto.task.TaskDto;
import com.example.assistant.entity.BotMode;
import com.example.assistant.entity.User;
import com.example.assistant.entity.UserSettings;
import com.example.assistant.service.conversation.ConversationService;
import com.example.assistant.service.task.TaskService;
import com.example.assistant.service.user.UserSettingsService;
import com.example.assistant.util.DateTimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramCommandHandler {

    private final TelegramMessageSender messageSender;
    private final ConversationService conversationService;
    private final UserSettingsService userSettingsService;
    private final TaskService taskService;

    public void handleStart(User user, Long chatId) {
        String welcome = """
                🤖 <b>Welcome to Telegram AI Assistant!</b>
                
                Hi <b>%s</b>! I am your smart, multi-purpose AI companion powered by advanced LLMs.
                
                ✨ <b>What I can do for you:</b>
                • 💬 <b>AI Chat:</b> Answer questions naturally in Khmer, English, and more.
                • 📝 <b>Summarize:</b> Extract key points from long texts.
                • 🌐 <b>Translate:</b> Multilingual translation with high accuracy.
                • 💻 <b>Coding:</b> Assist with Java, Spring Boot, React, Python, SQL, and more.
                • 📄 <b>Document Analysis:</b> Upload PDF or DOCX to ask questions.
                • ⏰ <b>Reminders / Tasks:</b> Schedule smart background reminders.
                • ⚙️ <b>Settings:</b> Customize language and memory preferences.
                
                Tap an option below to get started! 👇
                """.formatted(user.getFirstName() != null ? user.getFirstName() : "there");

        messageSender.sendMessageHtmlWithKeyboard(chatId, welcome, TelegramKeyboards.createMainMenuKeyboard());
    }

    public void handleHelp(Long chatId) {
        String help = """
                ℹ️ <b>Telegram AI Assistant Bot — Command Guide</b>
                
                <b>Core Commands:</b>
                /start - Restart bot and show main menu
                /menu - Show interactive feature menu
                /help - Show this guide
                /chat - Switch to general AI Chat mode
                /clear - Clear conversation memory & start fresh
                
                <b>AI Features:</b>
                /summary <code>&lt;text&gt;</code> - Summarize long text
                /translate <code>&lt;text&gt;</code> - Translate text to chosen language
                
                <b>Task & Reminders:</b>
                /task <code>&lt;reminder&gt;</code> - e.g. <i>/task Remind me tomorrow at 8 AM to study Java</i>
                /tasks - View and manage your scheduled reminders
                
                <b>Settings & Admin:</b>
                /settings - Change language, memory & notifications
                /admin - Admin dashboard information
                
                💡 <i>Tip: You can also upload any PDF or DOCX file directly to analyze it!</i>
                """;

        messageSender.sendMessageHtmlWithKeyboard(chatId, help, TelegramKeyboards.createMainMenuKeyboard());
    }

    public void handleMenu(Long chatId) {
        messageSender.sendMessageHtmlWithKeyboard(
                chatId,
                "🤖 <b>Main Menu:</b> Select an option to interact:",
                TelegramKeyboards.createMainMenuKeyboard()
        );
    }

    public void handleClear(User user, Long chatId) {
        conversationService.clearConversationMemory(user);
        messageSender.sendMessageHtml(chatId, "🧹 <b>Conversation memory cleared!</b>\nWe are now starting with a fresh context. What would you like to talk about?");
    }

    public void handleSettings(User user, Long chatId) {
        UserSettings settings = userSettingsService.getOrCreateSettings(user);
        String text = """
                ⚙️ <b>User Settings & Preferences</b>
                
                • <b>Language:</b> %s
                • <b>Memory:</b> %s
                • <b>Notifications:</b> %s
                • <b>Tier:</b> %s
                
                Tap any button below to change your settings:
                """.formatted(
                settings.getLanguage().toUpperCase(),
                settings.isMemoryEnabled() ? "Enabled ✅" : "Disabled ❌",
                settings.isNotificationsEnabled() ? "Enabled ✅" : "Disabled ❌",
                user.getTier().name()
        );

        messageSender.sendMessageHtmlWithKeyboard(chatId, text, TelegramKeyboards.createSettingsKeyboard(settings));
    }

    public void handleTasks(User user, Long chatId) {
        List<TaskDto> tasks = taskService.getUserPendingTasks(user);
        if (tasks.isEmpty()) {
            messageSender.sendMessageHtmlWithKeyboard(
                    chatId,
                    "⏰ <b>Your Scheduled Tasks</b>\n\nYou currently have no pending reminders.\nUse <code>/task Remind me tomorrow at 8 AM to study</code> to create one!",
                    TelegramKeyboards.createTasksKeyboard(tasks)
            );
            return;
        }

        StringBuilder sb = new StringBuilder("⏰ <b>Your Scheduled Reminders:</b>\n\n");
        for (int i = 0; i < tasks.size(); i++) {
            TaskDto t = tasks.get(i);
            sb.append(i + 1).append(". <b>").append(t.getTitle()).append("</b>\n")
                    .append("   🕒 Scheduled: <code>")
                    .append(DateTimeUtils.formatHumanReadable(t.getScheduledAt()))
                    .append("</code>\n\n");
        }
        sb.append("<i>Tap a task button below to delete/cancel it:</i>");

        messageSender.sendMessageHtmlWithKeyboard(chatId, sb.toString(), TelegramKeyboards.createTasksKeyboard(tasks));
    }

    public void handleAdmin(Long chatId) {
        String adminMsg = """
                🔐 <b>Admin System</b>
                
                Access the Admin Web Dashboard via your browser:
                🌐 URL: <code>http://localhost:8080/admin/index.html</code>
                
                From the dashboard you can:
                • View real-time user metrics & charts
                • Monitor AI token usage & estimated costs
                • Manage and block/unblock users
                • Broadcast queued announcements to all users
                """;
        messageSender.sendMessageHtml(chatId, adminMsg);
    }
}
