package com.hfinance.ui.viewmodel;

import com.hfinance.application.dto.TransactionDTO;
import com.hfinance.core.format.DateFormatter;
import com.hfinance.core.format.MoneyFormatter;

public record TransactionViewModel(
        Long id,
        String date,
        String type,
        String paymentMethod,
        String account,
        String category,
        String description,
        String amount
) {
    public static TransactionViewModel from(TransactionDTO transaction) {
        return new TransactionViewModel(transaction.id(), DateFormatter.format(transaction.transactionDate()),
                transaction.transactionTypeLabel(), transaction.paymentMethodLabel(), transaction.accountName(),
                transaction.categoryName(), transaction.description(), MoneyFormatter.format(transaction.amount()));
    }
}
