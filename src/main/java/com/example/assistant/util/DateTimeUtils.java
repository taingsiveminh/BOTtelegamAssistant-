package com.example.assistant.util;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class DateTimeUtils {

    private static final DateTimeFormatter HUMAN_FORMATTER = DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy 'at' hh:mm a");

    private DateTimeUtils() {}

    public static String formatHumanReadable(OffsetDateTime dateTime, ZoneId zoneId) {
        if (dateTime == null) return "N/A";
        ZoneId targetZone = zoneId != null ? zoneId : ZoneId.systemDefault();
        return dateTime.atZoneSameInstant(targetZone).format(HUMAN_FORMATTER);
    }

    public static String formatHumanReadable(OffsetDateTime dateTime) {
        return formatHumanReadable(dateTime, ZoneId.of("Asia/Phnom_Penh"));
    }
}
