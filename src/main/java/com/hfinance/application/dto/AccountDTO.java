package com.hfinance.application.dto;

import com.hfinance.domain.enums.AccountType;
import com.hfinance.domain.model.Account;

import java.math.BigDecimal;

public record AccountDTO(
        Long id,
        String name,
        String institution,
        AccountType type,
        String typeLabel,
        BigDecimal initialBalance,
        BigDecimal currentBalance,
        boolean active,
        String statusLabel
) {
    public static AccountDTO from(Account account, BigDecimal currentBalance) {
        return new AccountDTO(
                account.getId(),
                account.getName(),
                account.getInstitution(),
                account.getType(),
                account.getType().displayName(),
                account.getInitialBalance(),
                currentBalance,
                account.isActive(),
                account.isActive() ? "Ativa" : "Inativa"
        );
    }
}
