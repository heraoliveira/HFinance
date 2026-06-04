package com.hfinance.domain.enums;

import java.time.LocalDate;

public enum RecurrenceType {
    NONE("Não repetir"),
    WEEKLY("Semanalmente"),
    MONTHLY("Mensalmente"),
    YEARLY("Anualmente");

    private final String displayName;

    RecurrenceType(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public boolean recurring() {
        return this != NONE;
    }

    public LocalDate nextDate(LocalDate baseDate, int offset) {
        return switch (this) {
            case WEEKLY -> baseDate.plusWeeks(offset);
            case MONTHLY -> baseDate.plusMonths(offset);
            case YEARLY -> baseDate.plusYears(offset);
            case NONE -> baseDate;
        };
    }

    @Override
    public String toString() {
        return displayName;
    }
}
