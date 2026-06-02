package com.hfinance.core.format;

import java.time.LocalDate;

public final class DateFormatter {
    private static final java.time.format.DateTimeFormatter BR_DATE =
            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private DateFormatter() {
    }

    public static String format(LocalDate date) {
        return date == null ? "" : BR_DATE.format(date);
    }
}
