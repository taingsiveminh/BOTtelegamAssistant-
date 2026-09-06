package com.example.assistant.service.telegram;

import com.example.assistant.dto.task.TaskDto;
import com.example.assistant.entity.UserSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class TelegramKeyboards {

    private TelegramKeyboards() {}

    public static Map<String, Object> createMainMenuKeyboard() {
        List<List<Map<String, String>>> keyboard = List.of(
                List.of(
                        Map.of("text", "💬 Chat", "callback_data", "mode_chat")
                ),
                List.of(
                        Map.of("text", "📝 Summarize", "callback_data", "mode_summary"),
                        Map.of("text", "🌐 Translate", "callback_data", "mode_translate")
                ),
                List.of(
                        Map.of("text", "💻 Coding", "callback_data", "mode_coding"),
                        Map.of("text", "📄 Documents", "callback_data", "mode_document")
                ),
                List.of(
                        Map.of("text", "⏰ Tasks", "callback_data", "menu_tasks"),
                        Map.of("text", "⚙️ Settings", "callback_data", "menu_settings")
                ),
                List.of(
                        Map.of("text", "ℹ️ Help", "callback_data", "menu_help")
                )
        );
        return Map.of("inline_keyboard", keyboard);
    }

    public static Map<String, Object> createSettingsKeyboard(UserSettings settings) {
        String memStatus = (settings != null && settings.isMemoryEnabled()) ? "🧠 Memory: [ ON ]" : "🧠 Memory: [ OFF ]";
        String notifStatus = (settings != null && settings.isNotificationsEnabled()) ? "🔔 Notifications: [ ON ]" : "🔔 Notifications: [ OFF ]";
        String langLabel = "🌐 Language: " + ((settings != null && settings.getLanguage() != null) ? settings.getLanguage().toUpperCase() : "EN");

        List<List<Map<String, String>>> keyboard = List.of(
                List.of(Map.of("text", langLabel, "callback_data", "settings_lang_menu")),
                List.of(Map.of("text", memStatus, "callback_data", "settings_toggle_memory")),
                List.of(Map.of("text", notifStatus, "callback_data", "settings_toggle_notif")),
                List.of(Map.of("text", "🔙 Back to Menu", "callback_data", "menu_main"))
        );
        return Map.of("inline_keyboard", keyboard);
    }

    public static Map<String, Object> createLanguagePickerKeyboard(String callbackPrefix) {
        String prefix = callbackPrefix != null ? callbackPrefix : "set_lang_";
        List<List<Map<String, String>>> keyboard = List.of(
                List.of(
                        Map.of("text", "🇰🇭 ភាសាខ្មែរ (Khmer)", "callback_data", prefix + "km"),
                        Map.of("text", "🇺🇸 English", "callback_data", prefix + "en")
                ),
                List.of(
                        Map.of("text", "🇨🇳 中文 (Chinese)", "callback_data", prefix + "zh"),
                        Map.of("text", "🇹🇭 ภาษาไทย (Thai)", "callback_data", prefix + "th")
                ),
                List.of(
                        Map.of("text", "🇻🇳 Tiếng Việt", "callback_data", prefix + "vi")
                ),
                List.of(
                        Map.of("text", "🔙 Back", "callback_data", "menu_settings")
                )
        );
        return Map.of("inline_keyboard", keyboard);
    }

    public static Map<String, Object> createTasksKeyboard(List<TaskDto> tasks) {
        List<List<Map<String, String>>> keyboard = new ArrayList<>();

        if (tasks != null && !tasks.isEmpty()) {
            for (TaskDto t : tasks) {
                String title = t.getTitle().length() > 20 ? t.getTitle().substring(0, 18) + ".." : t.getTitle();
                keyboard.add(List.of(
                        Map.of("text", "❌ " + title, "callback_data", "task_del_" + t.getId())
                ));
            }
        }

        keyboard.add(List.of(
                Map.of("text", "➕ Add Reminder", "callback_data", "mode_task"),
                Map.of("text", "🔙 Back to Menu", "callback_data", "menu_main")
        ));

        return Map.of("inline_keyboard", keyboard);
    }
}
