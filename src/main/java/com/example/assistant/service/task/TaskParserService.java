package com.example.assistant.service.task;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class TaskParserService {

    @Data
    @Builder
    public static class ParsedTask {
        private String title;
        private OffsetDateTime scheduledAt;
    }

    private static final Pattern IN_MINUTES_WITH_TITLE = Pattern.compile("(?i)(?:remind\\s+me\\s+)?in\\s+(\\d+)\\s*(?:min|mins|minute|minutes)\\s+(?:to\\s+)?(.+)");
    private static final Pattern IN_HOURS_WITH_TITLE = Pattern.compile("(?i)(?:remind\\s+me\\s+)?in\\s+(\\d+)\\s*(?:hour|hours|hr|hrs)\\s+(?:to\\s+)?(.+)");
    private static final Pattern IN_MINUTES_ONLY = Pattern.compile("(?i)(?:remind\\s+me\\s+)?in\\s+(\\d+)\\s*(?:min|mins|minute|minutes)");
    private static final Pattern IN_HOURS_ONLY = Pattern.compile("(?i)(?:remind\\s+me\\s+)?in\\s+(\\d+)\\s*(?:hour|hours|hr|hrs)");
    private static final Pattern TOMORROW_AT_PATTERN = Pattern.compile("(?i)(?:remind\\s+me\\s+)?tomorrow(?:\\s+at)?\\s+(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)?(?:\\s+(?:to\\s+)?(.*))?");

    public ParsedTask parse(String text) {
        if (text == null || text.isBlank()) {
            return ParsedTask.builder()
                    .title("General Reminder")
                    .scheduledAt(OffsetDateTime.now(ZoneOffset.UTC).plusHours(1))
                    .build();
        }

        String cleaned = text.trim();

        // 1. "in X minutes to <title>"
        Matcher minTitleMatcher = IN_MINUTES_WITH_TITLE.matcher(cleaned);
        if (minTitleMatcher.find()) {
            int minutes = Integer.parseInt(minTitleMatcher.group(1));
            String title = minTitleMatcher.group(2);
            return ParsedTask.builder()
                    .title(title != null && !title.isBlank() ? title.trim() : "Reminder in " + minutes + " minutes")
                    .scheduledAt(OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(minutes))
                    .build();
        }

        // 2. "in X minutes" without title
        Matcher minOnlyMatcher = IN_MINUTES_ONLY.matcher(cleaned);
        if (minOnlyMatcher.find()) {
            int minutes = Integer.parseInt(minOnlyMatcher.group(1));
            return ParsedTask.builder()
                    .title("Reminder in " + minutes + " minutes")
                    .scheduledAt(OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(minutes))
                    .build();
        }

        // 3. "in X hours to <title>"
        Matcher hrTitleMatcher = IN_HOURS_WITH_TITLE.matcher(cleaned);
        if (hrTitleMatcher.find()) {
            int hours = Integer.parseInt(hrTitleMatcher.group(1));
            String title = hrTitleMatcher.group(2);
            return ParsedTask.builder()
                    .title(title != null && !title.isBlank() ? title.trim() : "Reminder in " + hours + " hours")
                    .scheduledAt(OffsetDateTime.now(ZoneOffset.UTC).plusHours(hours))
                    .build();
        }

        // 4. "in X hours" without title
        Matcher hrOnlyMatcher = IN_HOURS_ONLY.matcher(cleaned);
        if (hrOnlyMatcher.find()) {
            int hours = Integer.parseInt(hrOnlyMatcher.group(1));
            return ParsedTask.builder()
                    .title("Reminder in " + hours + " hours")
                    .scheduledAt(OffsetDateTime.now(ZoneOffset.UTC).plusHours(hours))
                    .build();
        }

        // 5. "tomorrow at 8 AM to study Java"
        Matcher tmrwMatcher = TOMORROW_AT_PATTERN.matcher(cleaned);
        if (tmrwMatcher.find()) {
            int hour = Integer.parseInt(tmrwMatcher.group(1));
            int minute = tmrwMatcher.group(2) != null ? Integer.parseInt(tmrwMatcher.group(2)) : 0;
            String amPm = tmrwMatcher.group(3);
            String title = tmrwMatcher.group(4);

            if (amPm != null) {
                if (amPm.equalsIgnoreCase("pm") && hour < 12) hour += 12;
                if (amPm.equalsIgnoreCase("am") && hour == 12) hour = 0;
            }

            LocalDate tomorrow = LocalDate.now(ZoneOffset.UTC).plusDays(1);
            OffsetDateTime scheduled = OffsetDateTime.of(tomorrow, LocalTime.of(hour, minute), ZoneOffset.UTC);

            if (title == null || title.isBlank()) title = "Study/Task Reminder";
            return ParsedTask.builder()
                    .title(title.trim())
                    .scheduledAt(scheduled)
                    .build();
        }

        // Default fallback
        return ParsedTask.builder()
                .title(cleaned)
                .scheduledAt(OffsetDateTime.now(ZoneOffset.UTC).plusHours(1))
                .build();
    }
}
