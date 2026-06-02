package com.hfinance.domain.rules;

import com.hfinance.core.exception.ValidationException;
import com.hfinance.domain.model.Transaction;

import java.math.BigDecimal;

public final class TransactionRules {
    public static final int MAX_DESCRIPTION_LENGTH = 255;

    private TransactionRules() {
    }

    public static void validate(Transaction transaction, boolean activeAccount) {
        if (transaction.getAccountId() == null) {
            throw new ValidationException("Selecione uma conta.");
        }
        if (!activeAccount) {
            throw new ValidationException("Esta conta está inativa.");
        }
        if (transaction.getCategoryId() == null) {
            throw new ValidationException("Selecione uma categoria.");
        }
        if (transaction.getTransactionDate() == null) {
            throw new ValidationException("Selecione uma data.");
        }
        if (transaction.getTransactionType() == null) {
            throw new ValidationException("Selecione um tipo de transação.");
        }
        if (transaction.getPaymentMethod() == null) {
            throw new ValidationException("Selecione um método de pagamento.");
        }
        if (transaction.getAmount() == null || transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("O valor deve ser maior que zero.");
        }
        String description = transaction.getDescription() == null ? "" : transaction.getDescription();
        if (description.length() > MAX_DESCRIPTION_LENGTH) {
            throw new ValidationException("A descrição deve ter no máximo 255 caracteres.");
        }
    }
}
