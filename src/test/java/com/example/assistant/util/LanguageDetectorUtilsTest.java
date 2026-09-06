package com.example.assistant.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LanguageDetectorUtilsTest {

    @Test
    void testDetectKhmer() {
        String lang = LanguageDetectorUtils.detectLanguage("សួស្តី តើអ្នកសុខសប្បាយទេ?");
        assertEquals("km", lang);
    }

    @Test
    void testDetectEnglish() {
        String lang = LanguageDetectorUtils.detectLanguage("Hello, how are you today?");
        assertEquals("en", lang);
    }

    @Test
    void testDetectChinese() {
        String lang = LanguageDetectorUtils.detectLanguage("你好，今天天气怎么样？");
        assertEquals("zh", lang);
    }

    @Test
    void testDetectThai() {
        String lang = LanguageDetectorUtils.detectLanguage("สวัสดีครับ สบายดีไหม");
        assertEquals("th", lang);
    }
}
