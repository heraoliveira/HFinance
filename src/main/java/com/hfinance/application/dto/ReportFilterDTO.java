package com.hfinance.application.dto;

import com.hfinance.domain.enums.PaymentMethod;
import com.hfinance.domain.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReportFilterDTO(
        LocalDate startDate,
        LocalDate endDate,
        Integer month,
        Integer year,
        Long accountId,
        Long categoryId,
        TransactionType transactionType,
        PaymentMethod paymentMethod,
        BigDecimal minAmount,
        BigDecimal maxAmount
) {
    public static ReportFilterDTO empty() {
        return new ReportFilterDTO(null, null, null, null, null, null, null, null, null, null);
    }

    public TransactionFilterDTO toTransactionFilter() {
        return new TransactionFilterDTO(startDate, endDate, month, year, accountId, categoryId,
                transactionType, paymentMethod, null, minAmount, maxAmount);
    }
}
