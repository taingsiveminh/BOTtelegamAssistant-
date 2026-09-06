package com.example.assistant.service.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TaskParserServiceTest {

    private TaskParserService parserService;

    @BeforeEach
    void setUp() {
        parserService = new TaskParserService();
    }

    @Test
    void testParseInMinutes() {
        TaskParserService.ParsedTask task = parserService.parse("in 30 mins to study Spring Boot");
        assertNotNull(task);
        assertEquals("study Spring Boot", task.getTitle());
        assertTrue(task.getScheduledAt().isAfter(OffsetDateTime.now()));
    }

    @Test
    void testParseInHours() {
        TaskParserService.ParsedTask task = parserService.parse("in 2 hours take a break");
        assertNotNull(task);
        assertEquals("take a break", task.getTitle());
        assertTrue(task.getScheduledAt().isAfter(OffsetDateTime.now().plusHours(1)));
    }

    @Test
    void testParseTomorrowAt() {
        TaskParserService.ParsedTask task = parserService.parse("Remind me tomorrow at 8 AM to study Java");
        assertNotNull(task);
        assertEquals("study Java", task.getTitle());
        assertTrue(task.getScheduledAt().isAfter(OffsetDateTime.now()));
    }

    @Test
    void testParseFallback() {
        TaskParserService.ParsedTask task = parserService.parse("Buy groceries");
        assertNotNull(task);
        assertEquals("Buy groceries", task.getTitle());
    }
}
