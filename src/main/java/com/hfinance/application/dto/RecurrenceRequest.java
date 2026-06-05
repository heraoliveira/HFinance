package com.hfinance.application.dto;

import com.hfinance.domain.enums.RecurrenceType;

public record RecurrenceRequest(
        RecurrenceType type,
        Integer repetitions
) {
    public static RecurrenceRequest none() {
        return new RecurrenceRequest(RecurrenceType.NONE, null);
    }

    public RecurrenceType safeType() {
        return type == null ? RecurrenceType.NONE : type;
    }

    public boolean recurring() {
        return safeType().recurring();
    }
}
