package com.example.assistant.service.telegram;

import com.example.assistant.dto.telegram.TelegramCallbackQuery;
import com.example.assistant.entity.BotMode;
import com.example.assistant.entity.User;
import com.example.assistant.entity.UserSettings;
import com.example.assistant.service.task.TaskService;
import com.example.assistant.service.user.UserSettingsService;
import com.example.assistant.util.LanguageDetectorUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramCallbackHandler {

    private final TelegramMessageSender messageSender;
    private final UserSettingsService userSettingsService;
    private final TaskService taskService;
    private final TelegramCommandHandler commandHandler;

    public void handleCallback(User user, TelegramCallbackQuery callback) {
        if (callback == null || callback.getData() == null) return;

        Long chatId = callback.getMessage().getChat().getId();
        String data = callback.getData();

        log.info("Processing callback '{}' from user {}", data, user.getTelegramUserId());

        if ("menu_main".equals(data)) {
            commandHandler.handleMenu(chatId);
        } else if ("menu_settings".equals(data)) {
            commandHandler.handleSettings(user, chatId);
        } else if ("menu_tasks".equals(data)) {
            commandHandler.handleTasks(user, chatId);
        } else if ("menu_help".equals(data)) {
            commandHandler.handleHelp(chatId);
        } else if ("mode_chat".equals(data)) {
            userSettingsService.setActiveMode(user, BotMode.CHAT);
            messageSender.sendMessageHtml(chatId, "💬 <b>Chat Mode Activated:</b>\nAsk me anything! I will remember context and answer naturally.");
        } else if ("mode_summary".equals(data)) {
            userSettingsService.setActiveMode(user, BotMode.SUMMARY);
            messageSender.sendMessageHtml(chatId, "📝 <b>Summarizer Mode Activated:</b>\nSend any long text or article, and I will generate a structured summary for you.");
        } else if ("mode_translate".equals(data)) {
            messageSender.sendMessageHtmlWithKeyboard(chatId, "🌐 <b>Select your target translation language:</b>", TelegramKeyboards.createLanguagePickerKeyboard("trans_lang_"));
        } else if ("mode_coding".equals(data)) {
            userSettingsService.setActiveMode(user, BotMode.CODING);
            messageSender.sendMessageHtml(chatId, "💻 <b>Coding Assistant Activated:</b>\nSend your coding questions, debugging requests, or architecture queries (Java, React, Python, SQL, etc.)!");
        } else if ("mode_document".equals(data)) {
            userSettingsService.setActiveMode(user, BotMode.DOCUMENT);
            messageSender.sendMessageHtml(chatId, "📄 <b>Document Assistant Activated:</b>\nPlease upload a <b>PDF</b> or <b>DOCX</b> document. I will read it and answer any questions!");
        } else if ("mode_task".equals(data)) {
            userSettingsService.setActiveMode(user, BotMode.TASK);
            messageSender.sendMessageHtml(chatId, "⏰ <b>Create a Reminder:</b>\nType your reminder, for example:\n• <i>Remind me tomorrow at 8 AM to study Java</i>\n• <i>in 30 minutes to take medicine</i>");
        } else if ("settings_lang_menu".equals(data)) {
            messageSender.sendMessageHtmlWithKeyboard(chatId, "🌐 <b>Select your preferred language:</b>", TelegramKeyboards.createLanguagePickerKeyboard("set_lang_"));
        } else if (data.startsWith("set_lang_")) {
            String code = data.replace("set_lang_", "");
            userSettingsService.updateLanguage(user, code);
            messageSender.sendMessageHtml(chatId, "✅ Language set to <b>" + LanguageDetectorUtils.getLanguageName(code) + "</b>");
            commandHandler.handleSettings(user, chatId);
        } else if (data.startsWith("trans_lang_")) {
            String code = data.replace("trans_lang_", "");
            userSettingsService.setActiveMode(user, BotMode.TRANSLATE);
            messageSender.sendMessageHtml(chatId, "🌐 <b>Translator ready!</b>\nTarget language: <b>" + LanguageDetectorUtils.getLanguageName(code) + "</b>.\nNow send the text you want to translate.");
        } else if ("settings_toggle_memory".equals(data)) {
            UserSettings settings = userSettingsService.toggleMemory(user);
            messageSender.sendMessageHtml(chatId, "🧠 Conversation Memory is now: <b>" + (settings.isMemoryEnabled() ? "ENABLED ✅" : "DISABLED ❌") + "</b>");
            commandHandler.handleSettings(user, chatId);
        } else if ("settings_toggle_notif".equals(data)) {
            UserSettings settings = userSettingsService.toggleNotifications(user);
            messageSender.sendMessageHtml(chatId, "🔔 Reminder Notifications are now: <b>" + (settings.isNotificationsEnabled() ? "ENABLED ✅" : "DISABLED ❌") + "</b>");
            commandHandler.handleSettings(user, chatId);
        } else if (data.startsWith("task_del_")) {
            Long taskId = Long.parseLong(data.replace("task_del_", ""));
            boolean deleted = taskService.deleteTask(user, taskId);
            if (deleted) {
                messageSender.sendMessageHtml(chatId, "🗑️ <b>Reminder deleted successfully!</b>");
            }
            commandHandler.handleTasks(user, chatId);
        }
    }
}
