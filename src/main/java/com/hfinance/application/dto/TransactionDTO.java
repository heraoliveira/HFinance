package com.hfinance.application.dto;

import com.hfinance.domain.enums.PaymentMethod;
import com.hfinance.domain.enums.TransactionType;
import com.hfinance.domain.model.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionDTO(
        Long id,
        Long accountId,
        String accountName,
        Long categoryId,
        String categoryName,
        LocalDate transactionDate,
        TransactionType transactionType,
        String transactionTypeLabel,
        PaymentMethod paymentMethod,
        String paymentMethodLabel,
        String description,
        BigDecimal amount
) {
    public static TransactionDTO from(Transaction transaction, String accountName, String categoryName) {
        return new TransactionDTO(
                transaction.getId(),
                transaction.getAccountId(),
                accountName,
                transaction.getCategoryId(),
                categoryName,
                transaction.getTransactionDate(),
                transaction.getTransactionType(),
                transaction.getTransactionType().displayName(),
                transaction.getPaymentMethod(),
                transaction.getPaymentMethod().displayName(),
                transaction.getDescription(),
                transaction.getAmount()
        );
    }
}
