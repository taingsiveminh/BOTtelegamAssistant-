package com.example.assistant.util;

import java.util.regex.Pattern;

public final class LanguageDetectorUtils {

    private static final Pattern KHMER_PATTERN = Pattern.compile("[\\u1780-\\u17FF]");
    private static final Pattern CHINESE_PATTERN = Pattern.compile("[\\u4E00-\\u9FA5]");
    private static final Pattern THAI_PATTERN = Pattern.compile("[\\u0E00-\\u0E7F]");
    private static final Pattern VIETNAMESE_PATTERN = Pattern.compile("[àáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđĐ]");

    private LanguageDetectorUtils() {}

    public static String detectLanguage(String text) {
        if (text == null || text.isBlank()) {
            return "en";
        }

        if (KHMER_PATTERN.matcher(text).find()) {
            return "km";
        }
        if (CHINESE_PATTERN.matcher(text).find()) {
            return "zh";
        }
        if (THAI_PATTERN.matcher(text).find()) {
            return "th";
        }
        if (VIETNAMESE_PATTERN.matcher(text).find()) {
            return "vi";
        }
        return "en";
    }

    public static String getLanguageName(String code) {
        if (code == null) return "English";
        return switch (code.toLowerCase()) {
            case "km" -> "Khmer (ភាសាខ្មែរ)";
            case "zh" -> "Chinese (中文)";
            case "th" -> "Thai (ภาษาไทย)";
            case "vi" -> "Vietnamese (Tiếng Việt)";
            case "fr" -> "French (Français)";
            case "de" -> "German (Deutsch)";
            case "ja" -> "Japanese (日本語)";
            case "ko" -> "Korean (한국어)";
            default -> "English";
        };
    }
}
