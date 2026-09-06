package com.example.assistant.util;

public final class MarkdownUtils {

    private MarkdownUtils() {}

    /**
     * Escape special characters for Telegram HTML mode.
     */
    public static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    /**
     * Converts basic markdown text into clean Telegram HTML format
     * to safely format code blocks, inline code, bold, italics.
     */
    public static String markdownToTelegramHtml(String text) {
        if (text == null || text.isBlank()) return "";

        // First escape general HTML entities to prevent malicious tags
        String escaped = escapeHtml(text);

        // Convert ```lang ... ``` or ``` ... ``` code blocks
        escaped = escaped.replaceAll("```(\\w+)?\\n?([\\s\\S]*?)```", "<pre><code>$2</code></pre>");

        // Convert `code` inline code
        escaped = escaped.replaceAll("`([^`]+)`", "<code>$1</code>");

        // Convert **bold** or __bold__
        escaped = escaped.replaceAll("\\*\\*([^*]+)\\*\\*", "<b>$1</b>");
        escaped = escaped.replaceAll("__([^_]+)__", "<b>$1</b>");

        // Convert *italic* or _italic_
        escaped = escaped.replaceAll("(?<!\\*)\\*([^*]+)\\*(?!\\*)", "<i>$1</i>");
        escaped = escaped.replaceAll("(?<!_)_([^_]+)_(?!_)", "<i>$1</i>");

        return escaped;
    }

    /**
     * Truncates message if it exceeds Telegram's 4096 character limit.
     */
    public static String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 20) + "\n\n...[Truncated]";
    }
}
