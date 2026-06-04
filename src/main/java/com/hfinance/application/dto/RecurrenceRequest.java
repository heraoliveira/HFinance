package com.hfinance.application.dto;

import com.hfinance.domain.enums.RecurrenceType;

import java.time.LocalDate;

public record RecurrenceRequest(
        RecurrenceType type,
        Integer repetitions,
        LocalDate endDate
) {
    public static RecurrenceRequest none() {
        return new RecurrenceRequest(RecurrenceType.NONE, null, null);
    }

    public RecurrenceType safeType() {
        return type == null ? RecurrenceType.NONE : type;
    }

    public boolean recurring() {
        return safeType().recurring();
    }
}
