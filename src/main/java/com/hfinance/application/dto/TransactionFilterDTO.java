package com.hfinance.application.dto;

import com.hfinance.domain.enums.PaymentMethod;
import com.hfinance.domain.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionFilterDTO(
        LocalDate startDate,
        LocalDate endDate,
        Integer month,
        Integer year,
        Long accountId,
        Long categoryId,
        TransactionType transactionType,
        PaymentMethod paymentMethod,
        String description,
        BigDecimal minAmount,
        BigDecimal maxAmount
) {
    public static TransactionFilterDTO empty() {
        return new TransactionFilterDTO(null, null, null, null, null, null, null, null, null, null, null);
    }

    public LocalDate effectiveStartDate() {
        if (startDate != null) {
            return startDate;
        }
        if (month != null && year != null) {
            return LocalDate.of(year, month, 1);
        }
        return null;
    }

    public LocalDate effectiveEndDate() {
        if (endDate != null) {
            return endDate;
        }
        if (month != null && year != null) {
            return LocalDate.of(year, month, 1).withDayOfMonth(LocalDate.of(year, month, 1).lengthOfMonth());
        }
        return null;
    }
}
